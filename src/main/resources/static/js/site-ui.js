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
    if (document.body.dataset.page !== "site-activity-detail") {
        return;
    }

    const countdownEl = document.getElementById("detailCountdown");
    const drawCountEl = document.getElementById("detailDrawCount");
    const participantEl = document.getElementById("detailParticipantCount");
    const winnerList = document.getElementById("detailWinnerList");
    const drawTrigger = document.querySelector("[data-open-modal='invite']");
    const confirmBtn = document.querySelector(".site-modal .action-btn");

    const prizeNameEl = document.getElementById("detailPrizeName");
    const prizeMetaEl = document.getElementById("detailPrizeMeta");
    const prizeDescEl = document.getElementById("detailPrizeDesc");
    const prizeCards = Array.from(document.querySelectorAll(".detail-prize-card"));

    let drawCount = Number(drawCountEl?.textContent || 3);
    let participantCount = Number(String(participantEl?.textContent || "10240").replace(/,/g, ""));
    const deadline = Date.now() + 1000 * 60 * 60 * 8;

    const tick = () => {
        if (!countdownEl) {
            return;
        }
        const remain = Math.max(0, deadline - Date.now());
        const h = Math.floor(remain / (1000 * 60 * 60));
        const m = Math.floor((remain % (1000 * 60 * 60)) / (1000 * 60));
        const s = Math.floor((remain % (1000 * 60)) / 1000);
        countdownEl.textContent = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
    };

    tick();
    window.setInterval(tick, 1000);

    prizeCards.forEach((card) => {
        card.addEventListener("click", () => {
            prizeCards.forEach((item) => item.classList.remove("is-active"));
            card.classList.add("is-active");
            const name = card.dataset.prizeName || "奖品";
            const prob = card.dataset.prizeProb || "0%";
            const stock = card.dataset.prizeStock || "0";
            const desc = card.dataset.prizeDesc || "暂无说明";
            if (prizeNameEl) {
                prizeNameEl.textContent = name;
            }
            if (prizeMetaEl) {
                prizeMetaEl.textContent = `库存 ${stock} · 概率 ${prob}`;
            }
            if (prizeDescEl) {
                prizeDescEl.textContent = desc;
            }
        });
    });

    if (!confirmBtn || !winnerList) {
        return;
    }

    confirmBtn.addEventListener("click", () => {
        if (drawCount <= 0) {
            return;
        }

        const activePrize = document.querySelector(".detail-prize-card.is-active");
        const prizeName = activePrize?.dataset.prizeName || "隐藏礼品";
        drawCount = Math.max(0, drawCount - 1);
        participantCount += Math.floor(Math.random() * 3) + 1;

        if (drawCountEl) {
            drawCountEl.textContent = String(drawCount);
        }
        if (participantEl) {
            participantEl.textContent = String(participantCount);
        }

        const row = document.createElement("li");
        const fakeName = `用*户${Math.floor(Math.random() * 90) + 10}`;
        const now = new Date();
        const hh = String(now.getHours()).padStart(2, "0");
        const mm = String(now.getMinutes()).padStart(2, "0");
        row.innerHTML = `<span>${fakeName}</span><strong>${prizeName}</strong><time>${hh}:${mm}</time>`;
        winnerList.prepend(row);
        if (winnerList.children.length > 8) {
            winnerList.removeChild(winnerList.lastElementChild);
        }
    });

    drawTrigger?.addEventListener("click", () => {
        if (confirmBtn instanceof HTMLButtonElement) {
            confirmBtn.disabled = drawCount <= 0;
            confirmBtn.textContent = drawCount <= 0 ? "抽奖次数不足" : "确认抽奖";
        }
    });
}
