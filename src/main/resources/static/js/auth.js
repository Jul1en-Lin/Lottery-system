const EMAIL_PATTERN = /^[A-Za-z0-9](?:[A-Za-z0-9._%+\-]{0,62}[A-Za-z0-9])?@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.)+[A-Za-z]{2,10}$/;
const PHONE_PATTERN = /^1[3-9]\d{9}$/;
const CODE_RESEND_SECONDS = 240;

document.addEventListener("DOMContentLoaded", () => {
    initSceneMotion();
    initFlashMessage();

    const page = document.body.dataset.page;
    if (page === "login") {
        initLoginPage();
    }
    if (page === "signup") {
        initSignupPage();
    }
    if (page === "forgot-password") {
        initForgotPasswordPage();
    }
});

function initSceneMotion() {
    const scene = document.getElementById("characterScene");
    if (!scene) {
        return;
    }

    document.addEventListener("mousemove", (event) => {
        document.querySelectorAll(".pupil").forEach((pupil) => {
            const rect = pupil.parentElement.getBoundingClientRect();
            const centerX = rect.left + rect.width / 2;
            const centerY = rect.top + rect.height / 2;
            const dx = event.clientX - centerX;
            const dy = event.clientY - centerY;
            const limit = pupil.classList.contains("dot") ? 5 : 4;
            const angle = Math.atan2(dy, dx);
            const distance = Math.min(Math.hypot(dx, dy) / 18, limit);
            const offsetX = Math.cos(angle) * distance;
            const offsetY = Math.sin(angle) * distance;
            pupil.style.setProperty("--look-x", `${offsetX.toFixed(2)}px`);
            pupil.style.setProperty("--look-y", `${offsetY.toFixed(2)}px`);
            pupil.style.setProperty("--pupil-x", `${offsetX.toFixed(2)}px`);
            pupil.style.setProperty("--pupil-y", `${offsetY.toFixed(2)}px`);
        });
    });
}

function initFlashMessage() {
    const flashRaw = sessionStorage.getItem("auth-flash");
    if (!flashRaw) {
        return;
    }

    sessionStorage.removeItem("auth-flash");
    const flash = JSON.parse(flashRaw);
    const statusEl = document.body.dataset.page === "signup"
        ? document.getElementById("signupStatus")
        : document.getElementById("loginStatus");

    if (statusEl && flash?.message) {
        setAlert(statusEl, flash.type || "success", flash.message);
    }
}

function initLoginPage() {
    const emailInput = document.getElementById("loginEmail");
    const passwordInput = document.getElementById("loginPassword");
    const codeInput = document.getElementById("loginCode");
    const sendCodeButton = document.getElementById("loginSendCode");
    const togglePasswordButton = document.getElementById("toggleLoginPassword");
    const eyeOpen = document.getElementById("loginEyeOpen");
    const eyeClosed = document.getElementById("loginEyeClosed");
    const form = document.getElementById("loginForm");
    const statusEl = document.getElementById("loginStatus");
    const submitButton = document.getElementById("loginSubmit");
    const scene = document.getElementById("characterScene");
    const loginModeStack = document.getElementById("loginModeStack");
    const loginMethodSwitch = document.getElementById("loginMethodSwitch");
    const modeButtons = Array.from(document.querySelectorAll("#loginMethodSwitch .method-chip"));
    const modePanels = Array.from(document.querySelectorAll("[data-mode-panel]"));
    const blinkableEyes = Array.from(document.querySelectorAll("#characterScene .login-eye"));
    let currentMode = "password";
    let typingTimer = null;
    let blinkTimer = null;

    const triggerTypingAnimation = () => {
        if (!scene) {
            return;
        }
        scene.classList.add("scene-typing");
        window.clearTimeout(typingTimer);
        typingTimer = window.setTimeout(() => {
            scene.classList.remove("scene-typing");
        }, 800);
    };

    const randomBlink = () => {
        if (!blinkableEyes.length) {
            return;
        }

        const delay = 3000 + Math.random() * 4000;
        blinkTimer = window.setTimeout(() => {
            blinkableEyes.forEach((eye) => eye.classList.add("blink"));
            window.setTimeout(() => {
                blinkableEyes.forEach((eye) => eye.classList.remove("blink"));
                randomBlink();
            }, 150);
        }, delay);
    };

    [emailInput, codeInput].forEach((input) => {
        input?.addEventListener("focus", triggerTypingAnimation);
        input?.addEventListener("input", triggerTypingAnimation);
    });

    const triggerPeekAnimation = () => triggerScenePeek(scene);

    togglePasswordButton.addEventListener("click", () => {
        const isPassword = passwordInput.type === "password";
        passwordInput.type = isPassword ? "text" : "password";
        togglePasswordButton.setAttribute("aria-label", isPassword ? "隐藏密码" : "显示密码");
        if (eyeOpen && eyeClosed) {
            eyeOpen.style.display = isPassword ? "none" : "block";
            eyeClosed.style.display = isPassword ? "block" : "none";
        }
        triggerPeekAnimation();
    });

    passwordInput.addEventListener("focus", triggerPeekAnimation);
    passwordInput.addEventListener("input", triggerPeekAnimation);

    const syncLoginModeHeight = () => {
        if (!loginModeStack || !modePanels.length) {
            return;
        }
        const maxHeight = Math.max(...modePanels.map((panel) => panel.scrollHeight));
        loginModeStack.style.setProperty("--login-mode-height", `${maxHeight}px`);
    };

    const updateMode = (nextMode) => {
        currentMode = nextMode;
        loginMethodSwitch?.setAttribute("data-active-mode", nextMode);
        modeButtons.forEach((button) => {
            const isActive = button.dataset.mode === nextMode;
            button.classList.toggle("active", isActive);
            button.setAttribute("aria-selected", String(isActive));
        });
        modePanels.forEach((panel) => {
            const isActive = panel.dataset.modePanel === nextMode;
            panel.classList.toggle("is-active", isActive);
            panel.classList.toggle("is-inactive", !isActive);
        });
        setButtonLabel(submitButton, nextMode === "password" ? "密码登录" : "验证码登录");
        clearAlert(statusEl);
    };

    modeButtons.forEach((button) => {
        button.addEventListener("click", () => updateMode(button.dataset.mode));
    });
    syncLoginModeHeight();
    updateMode(currentMode);

    const sendCode = () => requestEmailCode(
        emailInput,
        sendCodeButton,
        statusEl,
        [],
        "/user/admin/sendEmailCode"
    );
    sendCodeButton.addEventListener("click", sendCode);

    window.addEventListener("resize", syncLoginModeHeight);

    const params = new URLSearchParams(window.location.search);
    const emailPrefill = params.get("email");
    if (emailPrefill) {
        emailInput.value = emailPrefill;
    }

    randomBlink();

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearAlert(statusEl);

        const email = emailInput.value.trim();

        if (!EMAIL_PATTERN.test(email)) {
            setAlert(statusEl, "error", "请输入正确的邮箱地址。");
            emailInput.focus();
            return;
        }

        setButtonState(submitButton, true);

        try {
            let result;
            if (currentMode === "password") {
                const password = passwordInput.value.trim();
                if (!password) {
                    setAlert(statusEl, "error", "请输入管理员密码。");
                    passwordInput.focus();
                    return;
                }
                result = await requestJson("/user/admin/passwordLogin", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ email, password })
                });
            } else {
                const code = codeInput.value.trim();
                if (!/^\d{6}$/.test(code)) {
                    setAlert(statusEl, "error", "请输入 6 位验证码。");
                    codeInput.focus();
                    return;
                }
                result = await requestJson("/user/admin/emailLogin", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ email, code })
                });
            }

            const remember = document.getElementById("rememberDevice")?.checked;
            sessionStorage.setItem("auth-session", JSON.stringify({
                email,
                userId: result.data.id,
                token: result.data.token,
                identity: "ADMIN",
                remember
            }));
            window.location.href = "/admin/activity-center";
        } catch (error) {
            setAlert(statusEl, "error", error.message || "登录失败，请稍后重试。");
        } finally {
            setButtonState(submitButton, false);
        }
    });
}

function initSignupPage() {
    const form = document.getElementById("signupForm");
    const identityInput = document.getElementById("signupIdentity");
    const emailInput = document.getElementById("signupEmail");
    const userNameInput = document.getElementById("signupUserName");
    const phoneInput = document.getElementById("signupPhoneNumber");
    const passwordInput = document.getElementById("signupPassword");
    const codeInput = document.getElementById("signupCode");
    const sendCodeButton = document.getElementById("signupSendCode");
    const statusEl = document.getElementById("signupStatus");
    const submitButton = document.getElementById("signupSubmit");
    const togglePasswordButton = document.getElementById("toggleSignupPassword");
    const consentCheckbox = document.getElementById("signupConsent");
    const scene = document.getElementById("characterScene");

    bindTypingState([userNameInput, emailInput], scene, "scene-typing");
    const triggerPeekAnimation = () => triggerScenePeek(scene);

    const params = new URLSearchParams(window.location.search);
    const emailPrefill = params.get("email");
    if (emailPrefill) {
        emailInput.value = emailPrefill;
    }

    togglePasswordButton.addEventListener("click", () => {
        const isPassword = passwordInput.type === "password";
        passwordInput.type = isPassword ? "text" : "password";
        togglePasswordButton.setAttribute("aria-label", isPassword ? "隐藏密码" : "显示密码");
        triggerPeekAnimation();
    });

    passwordInput.addEventListener("focus", triggerPeekAnimation);
    passwordInput.addEventListener("input", triggerPeekAnimation);

    sendCodeButton.addEventListener("click", () => requestEmailCode(
        emailInput,
        sendCodeButton,
        statusEl,
        [],
        "/user/admin/sendEmailCode"
    ));

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearAlert(statusEl);

        const payload = {
            userName: userNameInput.value.trim(),
            email: emailInput.value.trim(),
            phoneNumber: phoneInput.value.trim(),
            password: passwordInput.value.trim() || null,
            code: codeInput.value.trim(),
            identity: identityInput?.value || "ADMIN"
        };

        if (payload.identity !== "ADMIN") {
            setAlert(statusEl, "error", "注册页仅允许创建管理员账号。");
            return;
        }

        if (!payload.userName) {
            setAlert(statusEl, "error", "请输入管理员名称。");
            userNameInput.focus();
            return;
        }
        if (!EMAIL_PATTERN.test(payload.email)) {
            setAlert(statusEl, "error", "请输入正确的邮箱地址。");
            emailInput.focus();
            return;
        }
        if (!PHONE_PATTERN.test(payload.phoneNumber)) {
            setAlert(statusEl, "error", "请输入正确的 11 位管理员手机号。");
            phoneInput.focus();
            return;
        }
        if (!payload.password) {
            setAlert(statusEl, "error", "请输入管理员密码。");
            passwordInput.focus();
            return;
        }
        if (!/^[0-9A-Za-z]{6,20}$/.test(payload.password)) {
            setAlert(statusEl, "error", "密码需为 6 到 20 位字母或数字。");
            passwordInput.focus();
            return;
        }
        if (!/^\d{6}$/.test(payload.code)) {
            setAlert(statusEl, "error", "请输入 6 位验证码。");
            codeInput.focus();
            return;
        }
        if (!consentCheckbox.checked) {
            setAlert(statusEl, "error", "请先同意相关协议。");
            consentCheckbox.focus();
            return;
        }

        setButtonState(submitButton, true);

        try {
            await requestJson("/user/emailRegister", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });
            sessionStorage.setItem("auth-flash", JSON.stringify({
                type: "success",
                message: "管理员注册成功，请使用密码或刚刚收到的邮箱验证码登录。"
            }));
            window.location.href = `/login?email=${encodeURIComponent(payload.email)}`;
        } catch (error) {
            setAlert(statusEl, "error", error.message || "注册失败，请稍后重试。");
        } finally {
            setButtonState(submitButton, false);
        }
    });

}

function triggerScenePeek(scene, duration = 720) {
    if (!scene) {
        return;
    }

    window.clearTimeout(scene.peekTimerId);
    scene.classList.add("scene-peeking");
    scene.peekTimerId = window.setTimeout(() => {
        scene.classList.remove("scene-peeking");
    }, duration);
}

function initForgotPasswordPage() {
    const form = document.getElementById("forgotPasswordForm");
    const emailInput = document.getElementById("forgotEmail");
    const statusEl = document.getElementById("forgotPasswordStatus");
    const submitButton = document.getElementById("forgotPasswordSubmit");
    const scene = document.getElementById("characterScene");

    bindTypingState([emailInput], scene, "scene-typing");

    const params = new URLSearchParams(window.location.search);
    const emailPrefill = params.get("email");
    if (emailPrefill) {
        emailInput.value = emailPrefill;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearAlert(statusEl);

        const email = emailInput.value.trim();
        if (!EMAIL_PATTERN.test(email)) {
            setAlert(statusEl, "error", "请输入正确的邮箱地址。");
            emailInput.focus();
            return;
        }

        setButtonState(submitButton, true);

        try {
            await requestJson(`/user/admin/sendEmailCode?email=${encodeURIComponent(email)}`, {
                method: "POST"
            });
            setAlert(statusEl, "success", "管理员恢复验证码已发送，请检查邮箱和垃圾邮件文件夹。");
            setButtonLabel(submitButton, "重新发送恢复验证码");
        } catch (error) {
            setAlert(statusEl, "error", error.message || "恢复邮件发送失败，请稍后重试。");
        } finally {
            setButtonState(submitButton, false);
        }
    });
}

function bindTypingState(inputs, scene, className) {
    if (!scene || !inputs?.length) {
        return;
    }

    const update = () => {
        const active = inputs.some((input) => input === document.activeElement || input.value.trim().length > 0);
        scene.classList.toggle(className, active);
    };

    inputs.forEach((input) => {
        input.addEventListener("focus", update);
        input.addEventListener("blur", update);
        input.addEventListener("input", update);
    });
}

async function requestEmailCode(emailInput, primaryButton, statusEl, mirrorButtons = [], endpoint = "/user/sendEmailCode") {
    clearAlert(statusEl);
    const email = emailInput.value.trim();
    if (!EMAIL_PATTERN.test(email)) {
        setAlert(statusEl, "error", "请先输入正确的邮箱地址，再发送验证码。");
        emailInput.focus();
        return;
    }

    const buttons = [primaryButton, ...mirrorButtons].filter(Boolean);
    buttons.forEach((button) => {
        button.disabled = true;
    });

    try {
        await requestJson(`${endpoint}?email=${encodeURIComponent(email)}`, {
            method: "POST"
        });
        setAlert(statusEl, "success", "验证码发送成功，请检查邮箱。若未收到，请稍后重试。");
        startCountdown(buttons, CODE_RESEND_SECONDS);
    } catch (error) {
        buttons.forEach((button) => {
            button.disabled = false;
        });
        setAlert(statusEl, "error", error.message || "验证码发送失败，请稍后重试。");
    }
}

function startCountdown(buttons, seconds) {
    let remaining = seconds;
    updateCountdownButtons(buttons, remaining);

    const timer = window.setInterval(() => {
        remaining -= 1;
        if (remaining <= 0) {
            window.clearInterval(timer);
            buttons.forEach((button) => {
                button.disabled = false;
                button.textContent = "发送验证码";
            });
            return;
        }
        updateCountdownButtons(buttons, remaining);
    }, 1000);
}

function updateCountdownButtons(buttons, remaining) {
    buttons.forEach((button) => {
        button.textContent = `${remaining}s`;
    });
}

async function requestJson(url, options) {
    const response = await fetch(url, options);
    const body = await response.json().catch(() => null);

    if (!response.ok) {
        throw new Error(body?.message || "请求失败");
    }

    if (!body || typeof body.code === "undefined") {
        return body;
    }

    if (body.code !== 200) {
        throw new Error(body.message || "请求失败");
    }

    return body;
}

function setAlert(element, type, message) {
    element.className = `status-alert show ${type}`;
    element.textContent = message;
}

function clearAlert(element) {
    element.className = "status-alert";
    element.textContent = "";
}

function setButtonState(button, loading) {
    button.disabled = loading;
    button.setAttribute("aria-busy", String(loading));
}

function setButtonLabel(button, label) {
    const surface = button.querySelector(".button-surface");
    const hover = button.querySelector(".button-hover");
    if (surface) {
        const surfaceLabel = surface.querySelector(".button-label");
        if (surfaceLabel) {
            surfaceLabel.textContent = label;
        } else {
            surface.textContent = label;
        }
    }
    if (hover) {
        const hoverLabel = hover.querySelector(".button-label");
        if (hoverLabel) {
            hoverLabel.textContent = label;
        } else {
            hover.textContent = label;
        }
    }
}

function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
}