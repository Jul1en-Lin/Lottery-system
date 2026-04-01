/**
 * 公共 HTTP 请求工具 — /js/http.js
 * 站点页与后台页共享，不引入任何框架依赖。
 */

/**
 * 发送 JSON 请求，自动解析统一响应体。
 *
 * @param {string} url
 * @param {RequestInit} [options]
 * @returns {Promise<any>} 完整响应体（含 code / message / data）
 * @throws {Error} HTTP 失败或业务 code !== 200 时抛出
 */
async function requestJson(url, options = {}) {
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

/**
 * 发送 multipart/form-data 请求，自动解析统一响应体。
 *
 * @param {string} url
 * @param {FormData} formData
 * @param {{ method?: string }} [options]
 * @returns {Promise<any>}
 */
async function requestForm(url, formData, options = {}) {
    const response = await fetch(url, {
        method: options.method || "POST",
        body: formData
    });
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

/**
 * 对字符串做基本 HTML 转义，用于拼接 innerHTML。
 *
 * @param {unknown} value
 * @returns {string}
 */
function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}
