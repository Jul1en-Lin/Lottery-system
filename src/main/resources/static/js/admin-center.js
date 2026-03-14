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
        name: "春季会员大抽奖",
        description: "面向活跃会员开放，中奖后自动发放电子券。",
        status: "已创建",
        createdAt: "2026-03-10T10:00:00",
        activityUserList: [
            {
                userId: 1001,
                userName: "张敏"
            },
            {
                userId: 1002,
                userName: "李响"
            }
        ],
        activityPrizeList: [
            {
                prizeId: 1,
                prizeAmount: 1,
                prizeTiers: "一等奖"
            },
            {
                prizeId: 2,
                prizeAmount: 2,
                prizeTiers: "二等奖"
            }
        ]
    },
    {
        id: "ACT-20260310-02",
        name: "品牌周年幸运签",
        description: "支持签到抽奖与加码轮次配置。",
        status: "草稿",
        createdAt: "2026-03-10T12:30:00",
        activityUserList: [
            {
                userId: 1003,
                userName: "王晨"
            }
        ],
        activityPrizeList: [
            {
                prizeId: 3,
                prizeAmount: 10,
                prizeTiers: "参与奖"
            }
        ]
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
        activityDraft: {
            users: [],
            prizes: []
        },
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
        activityUserSelect: document.getElementById("activityUserSelect"),
        activityPrizeSelect: document.getElementById("activityPrizeSelect"),
        activityPrizeAmount: document.getElementById("activityPrizeAmount"),
        activityPrizeTier: document.getElementById("activityPrizeTier"),
        addActivityUserButton: document.getElementById("addActivityUserButton"),
        addActivityPrizeButton: document.getElementById("addActivityPrizeButton"),
        activityUserSelected: document.getElementById("activityUserSelected"),
        activityPrizeSelected: document.getElementById("activityPrizeSelected"),
        activityUserSelectedCount: document.getElementById("activityUserSelectedCount"),
        activityPrizeSelectedCount: document.getElementById("activityPrizeSelectedCount"),
        prizeForm: document.getElementById("prizeForm"),
        memberForm: document.getElementById("memberForm"),
        memberSendCode: document.getElementById("memberSendCode"),
        prizeListAlert: document.getElementById("prizeListAlert"),
        memberListAlert: document.getElementById("memberListAlert"),
        activityStatusAlert: document.getElementById("activityStatusAlert"),
        prizeStatusAlert: document.getElementById("prizeStatusAlert"),
        prizeFileName: document.getElementById("prizeFileName"),
        prizeFileInlineTip: document.getElementById("prizeFileInlineTip"),
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

    state.elements.addActivityUserButton?.addEventListener("click", () => {
        clearInlineStatus(state.elements.activityStatusAlert);
        const selectedUserId = Number(state.elements.activityUserSelect?.value || 0);
        if (!selectedUserId) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请先选择参与用户。");
            return;
        }

        const targetUser = state.members.find((member) => Number(member.id) === selectedUserId);
        if (!targetUser) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "未找到用户信息，请刷新人员列表后重试。");
            return;
        }

        const exists = state.activityDraft.users.some((user) => Number(user.userId) === selectedUserId);
        if (exists) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "该用户已在参与名单中。");
            return;
        }

        state.activityDraft.users.push({
            userId: selectedUserId,
            userName: targetUser.userName
        });
        renderActivityDraft(state);
    });

    state.elements.addActivityPrizeButton?.addEventListener("click", () => {
        clearInlineStatus(state.elements.activityStatusAlert);
        const selectedPrizeId = Number(state.elements.activityPrizeSelect?.value || 0);
        const selectedTier = String(state.elements.activityPrizeTier?.value || "").trim();
        const amount = Number(state.elements.activityPrizeAmount?.value || 0);

        if (!selectedPrizeId) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请先选择奖品。");
            return;
        }
        if (!selectedTier) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请选择奖品档位。");
            return;
        }
        if (!Number.isInteger(amount) || amount <= 0) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "奖品数量必须是大于 0 的整数。");
            return;
        }

        const targetPrize = state.prizes.find((prize) => Number(prize.id) === selectedPrizeId);
        if (!targetPrize) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "未找到奖品信息，请刷新奖品列表后重试。");
            return;
        }

        const exists = state.activityDraft.prizes.some((prize) => Number(prize.prizeId) === selectedPrizeId);
        if (exists) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "该奖品已在清单中，可先删除后重新添加。");
            return;
        }

        state.activityDraft.prizes.push({
            prizeId: selectedPrizeId,
            prizeAmount: amount,
            prizeTiers: selectedTier,
            prizeName: targetPrize.name
        });
        if (state.elements.activityPrizeAmount) {
            state.elements.activityPrizeAmount.value = "1";
        }
        renderActivityDraft(state);
    });

    state.elements.activityUserSelected?.addEventListener("click", (event) => {
        const button = event.target.closest("button[data-remove-user-id]");
        if (!button) {
            return;
        }
        const userId = Number(button.dataset.removeUserId || 0);
        state.activityDraft.users = state.activityDraft.users.filter((item) => Number(item.userId) !== userId);
        renderActivityDraft(state);
    });

    state.elements.activityPrizeSelected?.addEventListener("click", (event) => {
        const button = event.target.closest("button[data-remove-prize-id]");
        if (!button) {
            return;
        }
        const prizeId = Number(button.dataset.removePrizeId || 0);
        state.activityDraft.prizes = state.activityDraft.prizes.filter((item) => Number(item.prizeId) !== prizeId);
        renderActivityDraft(state);
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearInlineStatus(state.elements.activityStatusAlert);

        const fields = form.elements;

        const payload = {
            name: String(fields.namedItem("name")?.value || "").trim(),
            description: String(fields.namedItem("description")?.value || "").trim(),
            activityUserList: state.activityDraft.users.map((user) => ({
                userId: Number(user.userId),
                userName: user.userName
            })),
            activityPrizeList: state.activityDraft.prizes.map((prize) => ({
                prizeId: Number(prize.prizeId),
                prizeAmount: Number(prize.prizeAmount),
                prizeTiers: prize.prizeTiers
            }))
        };

        if (!payload.name) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请输入活动名称。");
            fields.namedItem("name").focus();
            return;
        }
        if (!payload.description) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请输入活动说明。");
            fields.namedItem("description").focus();
            return;
        }
        if (!payload.activityUserList.length) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请至少添加 1 位参与用户。");
            return;
        }
        if (!payload.activityPrizeList.length) {
            showInlineStatus(state.elements.activityStatusAlert, "error", "请至少添加 1 项奖品配置。");
            return;
        }

        setButtonBusy(document.getElementById("activitySubmit"), true);
        try {
            const result = await requestJson("/activity/create", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const createdActivityId = result?.data?.activityId || `ACT-${Date.now()}`;
            state.activities.unshift({
                id: createdActivityId,
                name: payload.name,
                description: payload.description,
                activityUserList: payload.activityUserList,
                activityPrizeList: payload.activityPrizeList,
                createdAt: new Date().toISOString(),
                status: "已创建"
            });
            persistState(STORAGE_KEYS.activities, state.activities);
            form.reset();
            state.activityDraft = {
                users: [],
                prizes: []
            };
            renderActivityDraft(state);
            renderActivities(state);
            renderMetrics(state);
            showInlineStatus(state.elements.activityStatusAlert, "success", `活动创建成功，活动 ID：${createdActivityId}。`);
        } catch (error) {
            showInlineStatus(state.elements.activityStatusAlert, "error", error.message || "活动创建失败，请稍后重试。");
        } finally {
            setButtonBusy(document.getElementById("activitySubmit"), false);
        }
    });

    syncActivityComposeOptions(state);
    renderActivityDraft(state);
}

function syncActivityComposeOptions(state) {
    const userSelect = state.elements.activityUserSelect;
    const prizeSelect = state.elements.activityPrizeSelect;

    if (userSelect) {
        const memberOptions = state.members.map((member) => ({
            id: Number(member.id),
            userName: member.userName,
            identity: member.identity
        })).filter((member) => member.id > 0);

        if (!memberOptions.length) {
            userSelect.innerHTML = '<option value="">请先加载人员列表</option>';
        } else {
            userSelect.innerHTML = '<option value="">请选择参与用户</option>' + memberOptions.map((member) => (
                `<option value="${member.id}">${escapeHtml(member.userName)}（${member.identity === "ADMIN" ? "管理员" : "普通用户"}）</option>`
            )).join("");
        }
    }

    if (prizeSelect) {
        const prizeOptions = state.prizes.filter((prize) => Number(prize.id) > 0);
        if (!prizeOptions.length) {
            prizeSelect.innerHTML = '<option value="">请先加载奖品列表</option>';
        } else {
            prizeSelect.innerHTML = '<option value="">请选择奖品</option>' + prizeOptions.map((prize) => (
                `<option value="${prize.id}">${escapeHtml(prize.name)}（价值 ${formatPrice(prize.price)}）</option>`
            )).join("");
        }
    }
}

function renderActivityDraft(state) {
    renderActivityDraftUsers(state);
    renderActivityDraftPrizes(state);
}

function renderActivityDraftUsers(state) {
    const container = state.elements.activityUserSelected;
    if (!container) {
        return;
    }

    if (!state.activityDraft.users.length) {
        container.innerHTML = '<div class="compose-empty">未添加参与用户</div>';
    } else {
        container.innerHTML = state.activityDraft.users.map((user) => `
            <article class="compose-item">
                <div>
                    <h6>${escapeHtml(user.userName)}</h6>
                    <p>ID：${escapeHtml(String(user.userId))}</p>
                </div>
                <button class="compose-remove" data-remove-user-id="${escapeHtml(String(user.userId))}" type="button">移除</button>
            </article>
        `).join("");
    }

    if (state.elements.activityUserSelectedCount) {
        state.elements.activityUserSelectedCount.textContent = `${state.activityDraft.users.length} 人`;
    }
}

function renderActivityDraftPrizes(state) {
    const container = state.elements.activityPrizeSelected;
    if (!container) {
        return;
    }

    if (!state.activityDraft.prizes.length) {
        container.innerHTML = '<div class="compose-empty">未添加奖品项</div>';
    } else {
        container.innerHTML = state.activityDraft.prizes.map((prize) => `
            <article class="compose-item">
                <div>
                    <h6>${escapeHtml(prize.prizeName || "未命名奖品")}</h6>
                    <p>${escapeHtml(prize.prizeTiers)} · 数量 ${escapeHtml(String(prize.prizeAmount))} · ID ${escapeHtml(String(prize.prizeId))}</p>
                </div>
                <button class="compose-remove" data-remove-prize-id="${escapeHtml(String(prize.prizeId))}" type="button">移除</button>
            </article>
        `).join("");
    }

    if (state.elements.activityPrizeSelectedCount) {
        state.elements.activityPrizeSelectedCount.textContent = `${state.activityDraft.prizes.length} 项`;
    }
}

function bindPrizeForm(state) {
    const form = state.elements.prizeForm;
    if (!form) {
        return;
    }

    const fileInput = form.elements.namedItem("file");
    if (fileInput) {
        fileInput.addEventListener("change", () => {
            renderPrizeSelectedFile(state, fileInput.files?.[0] || null);
        });
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearInlineStatus(state.elements.prizeStatusAlert);

        const fields = form.elements;
        const selectedFile = fields.namedItem("file")?.files?.[0] || null;

        const payload = {
            name: fields.namedItem("name").value.trim(),
            price: Number(fields.namedItem("price").value || 0),
            description: fields.namedItem("description").value.trim()
        };

        if (!payload.name) {
            showInlineStatus(state.elements.prizeStatusAlert, "error", "请输入奖品名称。");
            fields.namedItem("name").focus();
            return;
        }
        if (payload.price <= 0) {
            showInlineStatus(state.elements.prizeStatusAlert, "error", "奖品价值必须大于 0。");
            fields.namedItem("price").focus();
            return;
        }
        if (!payload.description) {
            showInlineStatus(state.elements.prizeStatusAlert, "error", "请输入奖品说明。");
            fields.namedItem("description").focus();
            return;
        }
        if (!selectedFile) {
            showInlineStatus(state.elements.prizeStatusAlert, "error", "请上传奖品图片。");
            fields.namedItem("file").focus();
            return;
        }

        const formData = new FormData();
        formData.append("params", JSON.stringify(payload));
        formData.append("file", selectedFile);

        setButtonBusy(document.getElementById("prizeSubmit"), true);
        try {
            const result = await requestJson("/prize/create", {
                method: "POST",
                body: formData
            });

            await loadPrizes(state);
            renderMetrics(state);
            form.reset();
            renderPrizeSelectedFile(state, null);
            showInlineStatus(state.elements.prizeStatusAlert, "success", `奖品创建成功，奖品 ID：${result.data}。`);
        } catch (error) {
            showInlineStatus(state.elements.prizeStatusAlert, "error", error.message || "奖品创建失败，请稍后重试。");
        } finally {
            setButtonBusy(document.getElementById("prizeSubmit"), false);
        }
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
                    <h5 class="list-item-title">${escapeHtml(activity.name || activity.title || "未命名活动")}</h5>
                    <div class="list-item-meta">
                        <span class="meta-pill">参与用户：${Array.isArray(activity.activityUserList) ? activity.activityUserList.length : 0} 人</span>
                        <span class="meta-pill">奖品项：${Array.isArray(activity.activityPrizeList) ? activity.activityPrizeList.length : 0} 项</span>
                    </div>
                </div>
                <span class="status-pill ${resolveStatusClass(activity.status)}">${escapeHtml(activity.status || "已创建")}</span>
            </div>
            <p>${escapeHtml(activity.description || "暂无活动说明")}</p>
            <div class="list-item-meta">
                <span class="meta-pill">活动 ID：${escapeHtml(String(activity.id || "待分配"))}</span>
                <span class="meta-pill">创建时间：${formatDateTime(activity.createdAt)}</span>
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
                        <span class="meta-pill">价值 ${formatPrice(prize.price)}</span>
                    </div>
                </div>
                <span class="card-count-pill">编号 ${escapeHtml(String(prize.id))}</span>
            </div>
            <p>${escapeHtml(prize.description || prize.notes || "暂无奖品说明")}</p>
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
        syncActivityComposeOptions(state);
        renderMembers(state);
        renderMetrics(state);
    } catch (error) {
        state.members = [];
        state.memberLoading = false;
        state.memberLoadError = error.message || "人员列表加载失败，请稍后重试。";
        syncActivityComposeOptions(state);
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
        syncActivityComposeOptions(state);
        renderPrizes(state);
        renderMetrics(state);
    } catch (error) {
        state.prizes = [];
        state.prizeLoading = false;
        state.prizeLoadError = error.message || "奖品列表加载失败，请稍后重试。";
        syncActivityComposeOptions(state);
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
    if (status === "已创建") {
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

function renderPrizeSelectedFile(state, file) {
    if (!state.elements.prizeFileName) {
        return;
    }

    if (!file) {
        state.elements.prizeFileName.textContent = "未选择文件";
        if (state.elements.prizeFileInlineTip) {
            state.elements.prizeFileInlineTip.textContent = "未选择文件";
        }
        return;
    }

    const fileTypeLabel = formatPrizeFileType(file);
    state.elements.prizeFileName.textContent = `${fileTypeLabel}（${formatFileSize(file.size)}）`;
    if (state.elements.prizeFileInlineTip) {
        state.elements.prizeFileInlineTip.textContent = `${fileTypeLabel}（${formatFileSize(file.size)}）`;
    }
}

function formatFileSize(bytes) {
    const value = Number(bytes || 0);
    if (value < 1024) {
        return `${value}B`;
    }
    if (value < 1024 * 1024) {
        return `${(value / 1024).toFixed(1)}KB`;
    }
    return `${(value / (1024 * 1024)).toFixed(1)}MB`;
}

function formatPrizeFileType(file) {
    const mimeType = String(file?.type || "").trim().toLowerCase();
    if (mimeType.startsWith("image/")) {
        const subtype = mimeType.slice("image/".length);
        if (subtype === "jpeg") {
            return "JPG";
        }
        if (subtype) {
            return subtype.toUpperCase();
        }
        return "IMAGE";
    }

    return "未知类型";
}