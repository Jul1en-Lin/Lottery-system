const EMAIL_PATTERN = /^[A-Za-z0-9](?:[A-Za-z0-9._%+\-]{0,62}[A-Za-z0-9])?@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.)+[A-Za-z]{2,10}$/;
const PHONE_PATTERN = /^1[3-9]\d{9}$/;
const CODE_PATTERN = /^\d{6}$/;
const PASSWORD_PATTERN = /^[0-9A-Za-z]{6,20}$/;
const CODE_RESEND_SECONDS = 240;

const STORAGE_KEYS = {
    activities: "admin-center-activities",
    prizes: "admin-center-prizes",
    members: "admin-center-members"
};

const defaultActivities = [
    {
        id: "ACT-20260310-01",
        title: "春季会员大抽奖",
        scene: "会员回馈",
        startAt: "2026-03-12T10:00",
        drawAt: "2026-03-18T20:00",
        status: "预热中",
        capacity: 2000,
        prizePool: "春季奖池 A",
        description: "面向活跃会员开放，中奖后自动发放电子券。"
    },
    {
        id: "ACT-20260310-02",
        title: "品牌周年幸运签",
        scene: "周年庆",
        startAt: "2026-03-20T09:30",
        drawAt: "2026-03-28T19:00",
        status: "草稿",
        capacity: 5000,
        prizePool: "周年庆奖池",
        description: "支持签到抽奖与加码轮次配置。"
    }
];

const defaultPrizes = [
    {
        id: "PRIZE-01",
        name: "100 元礼品卡",
        tier: "一等奖",
        stock: 20,
        probability: 5,
        delivery: "自动到账",
        notes: "中奖后自动下发到用户账户。"
    },
    {
        id: "PRIZE-02",
        name: "品牌周边礼包",
        tier: "二等奖",
        stock: 60,
        probability: 18,
        delivery: "人工发放",
        notes: "运营审核地址后统一邮寄。"
    },
    {
        id: "PRIZE-03",
        name: "线下咖啡券",
        tier: "参与奖",
        stock: 300,
        probability: 42,
        delivery: "线下兑换",
        notes: "门店扫码核销，7 天内有效。"
    }
];

document.addEventListener("DOMContentLoaded", () => {
    void initializeAdminCenter();
});

async function initializeAdminCenter() {
    const session = readAdminSession();
    if (!session) {
        return;
    }

    const state = {
        session,
        activities: readStorage(STORAGE_KEYS.activities, defaultActivities),
        prizes: [],
        prizeLoading: false,
        prizeLoadError: "",
        members: [],
        memberFilter: "",
        memberLoading: false,
        memberLoadError: ""
    };

    cacheElements(state);
    bindSidebarInteractions(state);
    bindModuleSwitching(state);
    bindActivityForm(state);
    bindPrizeForm(state);
    bindMemberForm(state);
    bindMemberFilter(state);
    bindLogout(state);
    renderAll(state);
    await loadPrizes(state);
    await loadMembers(state);
}

function readAdminSession() {
    const raw = sessionStorage.getItem("auth-session");
    if (!raw) {
        window.location.href = "/login";
        return null;
    }

    try {
        const session = JSON.parse(raw);
        if (session.identity !== "ADMIN") {
            sessionStorage.removeItem("auth-session");
            window.location.href = "/login";
            return null;
        }
        return session;
    } catch (error) {
        sessionStorage.removeItem("auth-session");
        window.location.href = "/login";
        return null;
    }
}

function cacheElements(state) {
    state.elements = {
        sessionEmail: document.getElementById("sessionEmail"),
        activityCount: document.getElementById("activityCount"),
        prizeCount: document.getElementById("prizeCount"),
        memberCount: document.getElementById("memberCount"),
        activityListCount: document.getElementById("activityListCount"),
        prizeListCount: document.getElementById("prizeListCount"),
        memberListCount: document.getElementById("memberListCount"),
        memberIdentityFilter: document.getElementById("memberIdentityFilter"),
        activityList: document.getElementById("activityList"),
        prizeList: document.getElementById("prizeList"),
        memberTableBody: document.getElementById("memberTableBody"),
        activityForm: document.getElementById("activityForm"),
        prizeForm: document.getElementById("prizeForm"),
        memberForm: document.getElementById("memberForm"),
        memberSendCode: document.getElementById("memberSendCode"),
        prizeListAlert: document.getElementById("prizeListAlert"),
        memberListAlert: document.getElementById("memberListAlert"),
        activityStatusAlert: document.getElementById("activityStatusAlert"),
        prizeStatusAlert: document.getElementById("prizeStatusAlert"),
        memberStatusAlert: document.getElementById("memberStatusAlert"),
        mobileNavTrigger: document.getElementById("mobileNavTrigger"),
        adminSidebar: document.getElementById("adminSidebar"),
        logoutButton: document.getElementById("logoutButton"),
        topbarEyebrow: document.getElementById("topbarEyebrow"),
        topbarTitle: document.getElementById("topbarTitle"),
        topbarSubtitle: document.getElementById("topbarSubtitle"),
        navLinks: Array.from(document.querySelectorAll(".admin-nav-link")),
        panels: Array.from(document.querySelectorAll("[data-panel]")),
        quickLinks: Array.from(document.querySelectorAll(".hero-actions-row [data-nav-target]"))
    };

    if (state.elements.sessionEmail) {
        state.elements.sessionEmail.textContent = state.session.email || "管理员会话";
    }

    if (state.elements.memberIdentityFilter) {
        state.elements.memberIdentityFilter.value = state.memberFilter;
    }
}

function bindSidebarInteractions(state) {
    const trigger = state.elements.mobileNavTrigger;
    const sidebar = state.elements.adminSidebar;
    if (!trigger || !sidebar) {
        return;
    }

    trigger.addEventListener("click", () => {
        const nextOpen = !sidebar.classList.contains("is-open");
        sidebar.classList.toggle("is-open", nextOpen);
        trigger.setAttribute("aria-expanded", String(nextOpen));
    });

    state.elements.navLinks.forEach((link) => {
        link.addEventListener("click", () => {
            if (window.innerWidth <= 900) {
                sidebar.classList.remove("is-open");
                trigger.setAttribute("aria-expanded", "false");
            }
        });
    });
}

function bindModuleSwitching(state) {
    if (!state.elements.navLinks.length || !state.elements.panels.length) {
        return;
    }

    const metadata = {
        overview: {
            eyebrow: "Admin Workspace",
            title: "活动中心工作台",
            subtitle: "查看运营总览，并从左侧选择具体模块进入单独工作视图。"
        },
        activities: {
            eyebrow: "Activity Module",
            title: "活动管理",
            subtitle: "右侧只保留活动列表与新建抽奖活动，便于连续配置活动信息。"
        },
        prizes: {
            eyebrow: "Prize Module",
            title: "奖品管理",
            subtitle: "在当前视图内集中管理奖品列表、库存、概率与发放方式。"
        },
        members: {
            eyebrow: "Member Module",
            title: "人员管理",
            subtitle: "当前面板只展示人员列表与普通用户注册流程。"
        }
    };

    const setActivePanel = (panelId) => {
        const nextId = metadata[panelId] ? panelId : "overview";
        state.currentPanel = nextId;

        state.elements.navLinks.forEach((link) => {
            const isActive = link.dataset.navTarget === nextId;
            link.classList.toggle("active", isActive);
            link.setAttribute("aria-current", isActive ? "page" : "false");
        });

        state.elements.panels.forEach((panel) => {
            panel.classList.toggle("is-active", panel.dataset.panel === nextId);
        });

        const panelMeta = metadata[nextId];
        state.elements.topbarEyebrow.textContent = panelMeta.eyebrow;
        state.elements.topbarTitle.textContent = panelMeta.title;
        state.elements.topbarSubtitle.textContent = panelMeta.subtitle;

        if (window.location.hash !== `#${nextId}`) {
            history.replaceState(null, "", `#${nextId}`);
        }
    };

    state.elements.navLinks.forEach((link) => {
        link.addEventListener("click", (event) => {
            event.preventDefault();
            setActivePanel(link.dataset.navTarget);
        });
    });

    state.elements.quickLinks.forEach((link) => {
        link.addEventListener("click", (event) => {
            const target = link.dataset.navTarget;
            if (!target) {
                return;
            }
            event.preventDefault();
            setActivePanel(target);
        });
    });

    window.addEventListener("hashchange", () => {
        setActivePanel(window.location.hash.replace("#", ""));
    });

    setActivePanel(window.location.hash.replace("#", "") || "overview");
}

function bindActivityForm(state) {
    const form = state.elements.activityForm;
    if (!form) {
        return;
    }

    form.addEventListener("submit", (event) => {
        event.preventDefault();
        clearInlineStatus(state.elements.activityStatusAlert);

        const fields = form.elements;

        const payload = {
            title: fields.namedItem("title").value.trim(),
            scene: fields.namedItem("scene").value.trim(),
            startAt: fields.namedItem("startAt").value,
            drawAt: fields.namedItem("drawAt").value,
            status: fields.namedItem("status").value,
            capacity: Number(fields.namedItem("capacity").value || 0),
            prizePool: fields.namedItem("prizePool").value.trim(),
            description: fields.namedItem("description").value.trim()
        };

        if (!payload.title) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请输入活动名称。");
            fields.namedItem("title").focus();
            return;
        }
        if (!payload.scene) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请输入活动场景。");
            fields.namedItem("scene").focus();
            return;
        }
        if (!payload.startAt || !payload.drawAt) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请补齐开始时间和开奖时间。");
            return;
        }
        if (new Date(payload.drawAt).getTime() <= new Date(payload.startAt).getTime()) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "开奖时间必须晚于开始时间。");
            fields.namedItem("drawAt").focus();
            return;
        }

        state.activities.unshift({
            id: `ACT-${Date.now()}`,
            ...payload
        });
        persistState(STORAGE_KEYS.activities, state.activities);
        renderActivities(state);
        renderMetrics(state);
        form.reset();
        showInlineStatus(state.elements.activityStatusAlert, "success", "活动已保存到前端工作台，后续可直接对接真实接口。");
    });
}

function bindPrizeForm(state) {
    const form = state.elements.prizeForm;
    if (!form) {
        return;
    }

    form.addEventListener("submit", (event) => {
        event.preventDefault();
        clearInlineStatus(state.elements.prizeStatusAlert);

        const fields = form.elements;

        const payload = {
            name: fields.namedItem("name").value.trim(),
            tier: fields.namedItem("tier").value,
            stock: Number(fields.namedItem("stock").value || 0),
            probability: Number(fields.namedItem("probability").value || 0),
            delivery: fields.namedItem("delivery").value,
            notes: fields.namedItem("notes").value.trim()
        };

        if (!payload.name) {
            showInlineStatus(state.elements.prizeStatusAlert, "error", "请输入奖品名称。");
            fields.namedItem("name").focus();
            return;
        }
        if (payload.stock <= 0) {
            showInlineStatus(state.elements.prizeStatusAlert, "error", "库存必须大于 0。");
            fields.namedItem("stock").focus();
            return;
        }

        state.prizes.unshift({
            id: `PRIZE-${Date.now()}`,
            ...payload
        });
        renderPrizes(state);
        renderMetrics(state);
        form.reset();
        showInlineStatus(state.elements.prizeStatusAlert, "success", "奖品已加入当前页面列表。");
    });
}

function bindMemberForm(state) {
    const form = state.elements.memberForm;
    const sendCodeButton = state.elements.memberSendCode;
    if (!form || !sendCodeButton) {
        return;
    }

    sendCodeButton.addEventListener("click", async () => {
        clearInlineStatus(state.elements.memberStatusAlert);
        const fields = form.elements;
        const email = fields.namedItem("email").value.trim();
        if (!EMAIL_PATTERN.test(email)) {
            showInlineStatus(state.elements.memberStatusAlert, "error", "请先输入正确的普通用户邮箱，再发送验证码。");
            fields.namedItem("email").focus();
            return;
        }

        sendCodeButton.disabled = true;
        try {
            await requestJson(`/user/sendEmailCode?email=${encodeURIComponent(email)}`, {
                method: "POST"
            });
            showInlineStatus(state.elements.memberStatusAlert, "success", "验证码已发送，请提醒用户查收邮箱。");
            startCountdown(sendCodeButton, CODE_RESEND_SECONDS);
        } catch (error) {
            sendCodeButton.disabled = false;
            showInlineStatus(state.elements.memberStatusAlert, "error", error.message || "验证码发送失败，请稍后重试。");
        }
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearInlineStatus(state.elements.memberStatusAlert);

        const fields = form.elements;

        const payload = {
            userName: fields.namedItem("userName").value.trim(),
            email: fields.namedItem("email").value.trim(),
            phoneNumber: fields.namedItem("phoneNumber").value.trim(),
            password: fields.namedItem("password").value.trim() || null,
            code: fields.namedItem("code").value.trim(),
            identity: "NORMAL"
        };

        if (!payload.userName) {
            showInlineStatus(state.elements.memberStatusAlert, "error", "请输入普通用户名称。");
            fields.namedItem("userName").focus();
            return;
        }
        if (!EMAIL_PATTERN.test(payload.email)) {
            showInlineStatus(state.elements.memberStatusAlert, "error", "请输入正确的邮箱地址。");
            fields.namedItem("email").focus();
            return;
        }
        if (!PHONE_PATTERN.test(payload.phoneNumber)) {
            showInlineStatus(state.elements.memberStatusAlert, "error", "请输入正确的 11 位手机号。");
            fields.namedItem("phoneNumber").focus();
            return;
        }
        if (payload.password && !PASSWORD_PATTERN.test(payload.password)) {
            showInlineStatus(state.elements.memberStatusAlert, "error", "密码需为 6 到 20 位字母或数字。");
            fields.namedItem("password").focus();
            return;
        }
        if (!CODE_PATTERN.test(payload.code)) {
            showInlineStatus(state.elements.memberStatusAlert, "error", "请输入 6 位邮箱验证码。");
            fields.namedItem("code").focus();
            return;
        }

        setButtonBusy(document.getElementById("memberSubmit"), true);
        try {
            const result = await requestJson("/user/emailRegister", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            await loadMembers(state);
            renderMetrics(state);
            form.reset();
            showInlineStatus(state.elements.memberStatusAlert, "success", `普通用户注册成功，用户 ID：${result.data.id}。`);
        } catch (error) {
            showInlineStatus(state.elements.memberStatusAlert, "error", error.message || "普通用户注册失败，请稍后重试。");
        } finally {
            setButtonBusy(document.getElementById("memberSubmit"), false);
        }
    });
}

function bindMemberFilter(state) {
    const filter = state.elements.memberIdentityFilter;
    if (!filter) {
        return;
    }

    filter.addEventListener("change", () => {
        state.memberFilter = filter.value;
        void loadMembers(state);
    });
}

function bindLogout(state) {
    state.elements.logoutButton?.addEventListener("click", () => {
        sessionStorage.removeItem("auth-session");
        window.location.href = "/login";
    });
}

function renderAll(state) {
    renderActivities(state);
    renderPrizes(state);
    renderMembers(state);
    renderMetrics(state);
}

function renderActivities(state) {
    const container = state.elements.activityList;
    if (!container) {
        return;
    }

    container.innerHTML = state.activities.map((activity) => `
        <article class="stack-list-item">
            <div class="list-item-top">
                <div>
                    <h5 class="list-item-title">${escapeHtml(activity.title)}</h5>
                    <div class="list-item-meta">
                        <span class="meta-pill">${escapeHtml(activity.scene || "未分类")}</span>
                        <span class="meta-pill">奖池：${escapeHtml(activity.prizePool || "待配置")}</span>
                    </div>
                </div>
                <span class="status-pill ${resolveStatusClass(activity.status)}">${escapeHtml(activity.status)}</span>
            </div>
            <p>${escapeHtml(activity.description || "暂无活动说明")}</p>
            <div class="list-item-meta">
                <span class="meta-pill">开始：${formatDateTime(activity.startAt)}</span>
                <span class="meta-pill">开奖：${formatDateTime(activity.drawAt)}</span>
                <span class="meta-pill">上限：${activity.capacity || "不限"}</span>
            </div>
        </article>
    `).join("");

    state.elements.activityListCount.textContent = `${state.activities.length} 个活动`;
}

function renderPrizes(state) {
    const container = state.elements.prizeList;
    if (!container) {
        return;
    }

    if (state.prizeLoading) {
        container.innerHTML = `
        <article class="prize-item prize-item-empty">
            <p>正在加载奖品列表...</p>
        </article>
    `;
        state.elements.prizeListCount.textContent = "加载中";
        return;
    }

    if (!state.prizes.length) {
        container.innerHTML = `
        <article class="prize-item prize-item-empty">
            <p>暂无奖品数据</p>
        </article>
    `;
        state.elements.prizeListCount.textContent = "0 个奖品";
        return;
    }

    container.innerHTML = state.prizes.map((prize) => `
        <article class="prize-item">
            <div class="prize-item-top">
                <div>
                    <h5 class="prize-item-title">${escapeHtml(prize.name)}</h5>
                    <div class="list-item-meta">
                        <span class="meta-pill">价格 ${formatPrice(prize.price)}</span>
                        <span class="meta-pill">编号 ${escapeHtml(String(prize.id))}</span>
                    </div>
                </div>
                <span class="card-count-pill">奖品</span>
            </div>
            <p>${escapeHtml(prize.description || prize.notes || "暂无奖品说明")}</p>
            <div class="list-item-meta">
                <span class="meta-pill">图片 ${escapeHtml(prize.imageUrl || "未设置")}</span>
            </div>
        </article>
    `).join("");

    state.elements.prizeListCount.textContent = `${state.prizes.length} 个奖品`;
}

function renderMembers(state) {
    const tbody = state.elements.memberTableBody;
    if (!tbody) {
        return;
    }

    if (state.memberLoading) {
        tbody.innerHTML = `
        <tr class="admin-table-empty">
            <td colspan="4">正在加载人员列表...</td>
        </tr>
    `;
        state.elements.memberListCount.textContent = "加载中";
        return;
    }

    if (!state.members.length) {
        tbody.innerHTML = `
        <tr class="admin-table-empty">
            <td colspan="4">暂无符合条件的人员数据</td>
        </tr>
    `;
        state.elements.memberListCount.textContent = "0 人";
        return;
    }

    tbody.innerHTML = state.members.map((member) => `
        <tr>
            <td>${escapeHtml(member.userName)}</td>
            <td><span class="member-role ${member.identity === "NORMAL" ? "normal" : ""}">${member.identity === "ADMIN" ? "管理员" : "普通用户"}</span></td>
            <td>${escapeHtml(member.email)}</td>
            <td><span class="status-pill ${member.identity === "ADMIN" ? "status-live" : "status-draft"}">${escapeHtml(member.status || "已激活")}</span></td>
        </tr>
    `).join("");

    state.elements.memberListCount.textContent = `${state.members.length} 人`;
}

function renderMetrics(state) {
    state.elements.activityCount.textContent = String(state.activities.length);
    state.elements.prizeCount.textContent = String(state.prizes.length);
    state.elements.memberCount.textContent = String(state.members.length);
}

async function loadMembers(state) {
    state.memberLoading = true;
    state.memberLoadError = "";
    clearInlineStatus(state.elements.memberListAlert);
    renderMembers(state);

    const query = state.memberFilter ? `?identity=${encodeURIComponent(state.memberFilter)}` : "";

    try {
        const result = await requestJson(`/user/getListInfo${query}`);
        state.members = normalizeMembers(Array.isArray(result?.data) ? result.data : result);
        state.memberLoading = false;
        renderMembers(state);
        renderMetrics(state);
    } catch (error) {
        state.members = [];
        state.memberLoading = false;
        state.memberLoadError = error.message || "人员列表加载失败，请稍后重试。";
        showInlineStatus(state.elements.memberListAlert, "error", state.memberLoadError);
        renderMembers(state);
        renderMetrics(state);
    }
}

async function loadPrizes(state) {
    state.prizeLoading = true;
    state.prizeLoadError = "";
    clearInlineStatus(state.elements.prizeListAlert);
    renderPrizes(state);

    try {
        const result = await requestJson("/prize/getList?current=1&size=10");
        const pageData = result?.data ?? result;
        state.prizes = normalizePrizes(pageData?.records);
        state.prizeLoading = false;
        renderPrizes(state);
        renderMetrics(state);
    } catch (error) {
        state.prizes = [];
        state.prizeLoading = false;
        state.prizeLoadError = error.message || "奖品列表加载失败，请稍后重试。";
        showInlineStatus(state.elements.prizeListAlert, "error", state.prizeLoadError);
        renderPrizes(state);
        renderMetrics(state);
    }
}

function normalizePrizes(prizes) {
    if (!Array.isArray(prizes)) {
        return [];
    }

    return prizes.map((prize) => ({
        id: prize?.id ?? "",
        name: prize?.name || "未命名奖品",
        imageUrl: prize?.imageUrl || "",
        price: prize?.price ?? 0,
        description: prize?.description || ""
    }));
}

function normalizeMembers(members) {
    if (!Array.isArray(members)) {
        return [];
    }

    return members.map((member) => ({
        id: member?.id ?? "",
        userName: member?.userName || "未命名用户",
        email: member?.email || "-",
        identity: normalizeIdentity(member?.identity),
        status: "已激活"
    }));
}

function normalizeIdentity(identity) {
    return String(identity || "").toUpperCase() === "ADMIN" ? "ADMIN" : "NORMAL";
}

function readStorage(key, fallback) {
    const raw = localStorage.getItem(key);
    if (!raw) {
        return [...fallback];
    }

    try {
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed : [...fallback];
    } catch (error) {
        return [...fallback];
    }
}

function persistState(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
}

function showInlineStatus(element, type, message) {
    if (!element) {
        return;
    }
    element.className = `inline-status show ${type}`;
    element.textContent = message;
}

function clearInlineStatus(element) {
    if (!element) {
        return;
    }
    element.className = "inline-status";
    element.textContent = "";
}

function setButtonBusy(button, loading) {
    if (!button) {
        return;
    }
    button.disabled = loading;
    button.setAttribute("aria-busy", String(loading));
}

function startCountdown(button, seconds) {
    let remaining = seconds;
    button.textContent = `${remaining}s`;

    const timer = window.setInterval(() => {
        remaining -= 1;
        if (remaining <= 0) {
            window.clearInterval(timer);
            button.disabled = false;
            button.textContent = "发送验证码";
            return;
        }
        button.textContent = `${remaining}s`;
    }, 1000);
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

function formatDateTime(value) {
    if (!value) {
        return "待定";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return new Intl.DateTimeFormat("zh-CN", {
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    }).format(date);
}

function resolveStatusClass(status) {
    if (status === "进行中") {
        return "status-live";
    }
    if (status === "预热中") {
        return "status-warm";
    }
    return "status-draft";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function formatPrice(value) {
    const amount = Number(value ?? 0);
    if (Number.isNaN(amount)) {
        return String(value || "0");
    }
    return amount.toFixed(2);
}