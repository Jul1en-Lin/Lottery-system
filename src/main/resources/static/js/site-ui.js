document.addEventListener("DOMContentLoaded", () => {
    bindSiteModal();
    initCountdownPill();
    initActivityDetailPage();
});

function bindSiteModal() {
    const modal = document.getElementById("siteModal");
    if (!modal) {
        return;
    }

    const openButtons = Array.from(document.querySelectorAll("[data-open-modal]"));
    const closeButtons = Array.from(document.querySelectorAll("[data-close-modal]"));

    openButtons.forEach((button) => {
        button.addEventListener("click", () => {
            modal.classList.add("is-open");
        });
    });

    closeButtons.forEach((button) => {
        button.addEventListener("click", () => {
            modal.classList.remove("is-open");
        });
    });

    modal.addEventListener("click", (event) => {
        if (event.target === modal) {
            modal.classList.remove("is-open");
        }
    });
}

function initCountdownPill() {
    const target = document.getElementById("globalCountdownPill");
    if (!target) {
        return;
    }

    const endTime = Date.now() + 1000 * 60 * 60 * 8;

    const tick = () => {
        const remain = Math.max(0, endTime - Date.now());
        const h = Math.floor(remain / (1000 * 60 * 60));
        const m = Math.floor((remain % (1000 * 60 * 60)) / (1000 * 60));
        const s = Math.floor((remain % (1000 * 60)) / 1000);
        target.textContent = `活动倒计时 ${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
    };

    tick();
    window.setInterval(tick, 1000);
}

function initActivityDetailPage() {
    // activity-detail.html 已通过 bindDrawModal() 在页面内脚本中自行处理抽奖逻辑，
    // site-ui.js 不再重复绑定 confirmBtn，避免双重触发真实 API 与旧假数据逻辑冲突。
    if (document.body.dataset.page !== "site-activity-detail") {
        return;
    }

    const countdownEl = document.getElementById("detailCountdown");
    const deadline = Date.now() + 1000 * 60 * 60 * 8;

    if (countdownEl) {
        const tick = () => {
            const remain = Math.max(0, deadline - Date.now());
            const h = Math.floor(remain / (1000 * 60 * 60));
            const m = Math.floor((remain % (1000 * 60 * 60)) / (1000 * 60));
            const s = Math.floor((remain % (1000 * 60)) / 1000);
            countdownEl.textContent = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
        };
        tick();
        window.setInterval(tick, 1000);
    }
    // 注意：奖品卡片点击切换和弹窗抽奖逻辑均已由 activity-detail.html 内联脚本接管。
}
