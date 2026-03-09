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
        const rect = scene.getBoundingClientRect();
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height / 2;
        const offsetX = clamp((event.clientX - centerX) / 45, -4, 4);
        const offsetY = clamp((event.clientY - centerY) / 55, -3, 3);
        document.querySelectorAll(".pupil").forEach((pupil) => {
            pupil.style.setProperty("--look-x", `${offsetX}px`);
            pupil.style.setProperty("--look-y", `${offsetY}px`);
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
    const secondaryCodeButton = document.getElementById("secondaryCodeButton");
    const resendCodeLink = document.getElementById("resendCodeLink");
    const togglePasswordButton = document.getElementById("toggleLoginPassword");
    const form = document.getElementById("loginForm");
    const statusEl = document.getElementById("loginStatus");
    const submitButton = document.getElementById("loginSubmit");
    const scene = document.getElementById("characterScene");
    const modeButtons = Array.from(document.querySelectorAll("#loginMethodSwitch .method-chip"));
    const modePanels = Array.from(document.querySelectorAll("[data-mode-panel]"));
    const emailOnlyElements = Array.from(document.querySelectorAll(".login-email-only"));
    let currentMode = "password";

    bindTypingState([emailInput], scene, "scene-typing");
    bindTypingState([codeInput], scene, "scene-typing");
    bindTypingState([passwordInput], scene, "scene-peeking");

    togglePasswordButton.addEventListener("click", () => {
        const isPassword = passwordInput.type === "password";
        passwordInput.type = isPassword ? "text" : "password";
        togglePasswordButton.setAttribute("aria-label", isPassword ? "Hide password" : "Show password");
        scene?.classList.toggle("scene-peeking", isPassword);
    });

    const updateMode = (nextMode) => {
        currentMode = nextMode;
        modeButtons.forEach((button) => {
            const isActive = button.dataset.mode === nextMode;
            button.classList.toggle("active", isActive);
            button.setAttribute("aria-selected", String(isActive));
        });
        modePanels.forEach((panel) => {
            panel.classList.toggle("is-hidden", panel.dataset.modePanel !== nextMode);
        });
        emailOnlyElements.forEach((element) => {
            element.classList.toggle("is-hidden", nextMode !== "email");
        });
        setButtonLabel(submitButton, nextMode === "password" ? "Log in with password" : "Log in with email");
        clearAlert(statusEl);
    };

    modeButtons.forEach((button) => {
        button.addEventListener("click", () => updateMode(button.dataset.mode));
    });
    updateMode(currentMode);

    const sendCode = () => requestEmailCode(
        emailInput,
        sendCodeButton,
        statusEl,
        [secondaryCodeButton, resendCodeLink],
        "/user/admin/sendEmailCode"
    );
    sendCodeButton.addEventListener("click", sendCode);
    secondaryCodeButton.addEventListener("click", sendCode);
    resendCodeLink.addEventListener("click", sendCode);

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
                remember
            }));
            setAlert(statusEl, "success", `管理员登录成功，用户 ID：${result.data.id}。当前项目尚未提供后台首页，因此停留在本页展示结果。`);
        } catch (error) {
            setAlert(statusEl, "error", error.message || "登录失败，请稍后重试。");
        } finally {
            setButtonState(submitButton, false);
        }
    });
}

function initSignupPage() {
    const form = document.getElementById("signupForm");
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
    bindTypingState([passwordInput], scene, "scene-peeking");

    const params = new URLSearchParams(window.location.search);
    const emailPrefill = params.get("email");
    if (emailPrefill) {
        emailInput.value = emailPrefill;
    }

    togglePasswordButton.addEventListener("click", () => {
        const isPassword = passwordInput.type === "password";
        passwordInput.type = isPassword ? "text" : "password";
        togglePasswordButton.setAttribute("aria-label", isPassword ? "Hide password" : "Show password");
        scene?.classList.toggle("scene-peeking", isPassword);
    });

    sendCodeButton.addEventListener("click", () => requestEmailCode(emailInput, sendCodeButton, statusEl));

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearAlert(statusEl);

        const payload = {
            userName: userNameInput.value.trim(),
            email: emailInput.value.trim(),
            phoneNumber: phoneInput.value.trim(),
            password: passwordInput.value.trim() || null,
            code: codeInput.value.trim(),
            identity: form.querySelector("input[name='identity']:checked")?.value
        };

        if (!payload.identity) {
            setAlert(statusEl, "error", "请选择身份类型。");
            return;
        }
        if (!payload.userName) {
            setAlert(statusEl, "error", "请输入用户名。");
            userNameInput.focus();
            return;
        }
        if (!EMAIL_PATTERN.test(payload.email)) {
            setAlert(statusEl, "error", "请输入正确的邮箱地址。");
            emailInput.focus();
            return;
        }
        if (!PHONE_PATTERN.test(payload.phoneNumber)) {
            setAlert(statusEl, "error", "请输入正确的 11 位手机号。");
            phoneInput.focus();
            return;
        }
        if (payload.password && !/^[0-9A-Za-z]{6,20}$/.test(payload.password)) {
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
            setAlert(statusEl, "error", "请先同意隐私政策和服务条款。");
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
                message: "注册成功，请使用刚刚收到的邮箱验证码登录。"
            }));
            window.location.href = `/login?email=${encodeURIComponent(payload.email)}`;
        } catch (error) {
            setAlert(statusEl, "error", error.message || "注册失败，请稍后重试。");
        } finally {
            setButtonState(submitButton, false);
        }
    });
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
            setButtonLabel(submitButton, "Resend recovery code");
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
                button.textContent = button.id === "resendCodeLink" ? "Need a fresh code?" : "Send code";
                if (button.id === "secondaryCodeButton") {
                    button.innerHTML = '<span class="button-surface">Send verification code</span><span class="button-hover">Send verification code</span>';
                }
            });
            return;
        }
        updateCountdownButtons(buttons, remaining);
    }, 1000);
}

function updateCountdownButtons(buttons, remaining) {
    buttons.forEach((button) => {
        if (button.id === "resendCodeLink") {
            button.textContent = `Retry in ${remaining}s`;
            return;
        }
        if (button.id === "secondaryCodeButton") {
            button.innerHTML = `<span class="button-surface">Retry in ${remaining}s</span><span class="button-hover">Retry in ${remaining}s</span>`;
            return;
        }
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
        surface.textContent = label;
    }
    if (hover) {
        hover.textContent = label;
    }
}

function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
}