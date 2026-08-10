// widget_review_util/api_client.js

import { warn } from "./logger.js";
import { extractFilenameFromContentDisposition } from "./dom_util.js";

/**
 * Builds default headers for widget review API calls.
 * Keep JSON defaults for normal API usage.
 */
export function buildHeaders(extra = {}) {
    return {
        "Accept": "application/json",
        "Content-Type": "application/json",
        ...extra
    };
}

/**
 * Safely parses response text as JSON if possible.
 */
async function parseBody(res) {
    const text = await res.text();
    if (!text) {
        return {};
    }
    try {
        return JSON.parse(text);
    } catch {
        return { raw: text };
    }
}

/**
 * Converts a failed HTTP response to a rich Error object.
 */
async function toHttpError(res, method, url) {
    const data = await parseBody(res);
    const requestId = data?.requestId || "";
    const messageText = data?.message || data?.error || "";
    const err = new Error(`${method} ${url} failed with ${res.status}${requestId ? ` [${requestId}]` : ""}${messageText ? `: ${messageText}` : ""}`);
    err.status = res.status;
    err.data = data;
    err.requestId = requestId;
    return err;
}

/**
 * Generic GET JSON helper.
 */
export async function getJson(url, options = {}) {
    const res = await fetch(url, {
        method: "GET",
        credentials: "same-origin",
        headers: {
            "Accept": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    if (!res.ok) {
        throw await toHttpError(res, "GET", url);
    }

    return parseBody(res);
}

/**
 * Generic POST JSON helper.
 * Sends application/json unless caller overrides headers/body.
 */
export async function postJson(url, payload = {}, options = {}) {
    const headers = buildHeaders(options.headers || {});
    const body = (options.body !== null && options.body !== undefined) ? options.body : JSON.stringify(payload);

    const res = await fetch(url, {
        method: "POST",
        credentials: "same-origin",
        headers,
        body,
        ...options
    });

    if (!res.ok) {
        throw await toHttpError(res, "POST", url);
    }

    return parseBody(res);
}

/**
 * Generic DELETE JSON helper.
 */
export async function deleteJson(url, options = {}) {
    const res = await fetch(url, {
        method: "DELETE",
        credentials: "same-origin",
        headers: {
            "Accept": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    if (!res.ok) {
        throw await toHttpError(res, "DELETE", url);
    }

    return parseBody(res);
}

/**
 * POST x-www-form-urlencoded helper.
 * Useful if endpoint expects form data.
 */
export async function postForm(url, formObj = {}, options = {}) {
    const form = new URLSearchParams();

    Object.entries(formObj || {}).forEach(([k, v]) => {
        if (v === undefined || v === null) {
            return;
        }
        form.set(k, typeof v === "string" ? v : String(v));
    });

    const res = await fetch(url, {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            ...(options.headers || {})
        },
        body: form.toString(),
        ...options
    });

    if (!res.ok) {
        throw await toHttpError(res, "POST", url);
    }

    return parseBody(res);
}

/**
 * Convenience helper for endpoints that may require either JSON or form encoding.
 * Tries JSON first, then retries as form on 415.
 */
export async function postJsonWithFormFallback(url, payload = {}, options = {}) {
    try {
        return await postJson(url, payload, options);
    } catch (e) {
        if (e?.status !== 415) {
            throw e;
        }

        warn("postJson got 415, retrying as form-urlencoded", { url });

        // Flatten payload for form transport.
        // Arrays/objects are encoded as JSON strings.
        const formObj = {};
        Object.entries(payload || {}).forEach(([k, v]) => {
            if (v === undefined || v === null) {
                return;
            }
            if (typeof v === "object") {
                formObj[k] = JSON.stringify(v);
            } else {
                formObj[k] = v;
            }
        });

        return await postForm(url, formObj, options);
    }
}

/**
 * Download helper for export endpoints that return binary files.
 * Returns { blob, filename, contentDisposition, contentType }.
 */
export async function postForDownload(url, payload = {}, options = {}) {
    const headers = {
        ...buildHeaders(),
        "Accept": "*/*",
        ...(options.headers || {})
    };

    const res = await fetch(url, {
        method: "POST",
        credentials: "same-origin",
        headers,
        body: JSON.stringify(payload ?? {}),
        ...options
    });

    if (!res.ok) {
        // Try to parse JSON/text for meaningful error
        const text = await res.text().catch(() => "");
        let data = {};
        try { data = text ? JSON.parse(text) : {}; } catch { data = { raw: text }; }

        const err = new Error(`POST ${url} failed with ${res.status}${data?.requestId ? ` [${data.requestId}]` : ""}${data?.message ? `: ${data.message}` : (text ? `: ${text}` : "")}`);
        err.status = res.status;
        err.data = data;
        err.requestId = data?.requestId || "";
        throw err;
    }

    const blob = await res.blob();
    const contentDisposition = res.headers.get("Content-Disposition") || "";
    const contentType = res.headers.get("Content-Type") || "";
    const filename = extractFilenameFromContentDisposition(contentDisposition);

    return { blob, filename, contentDisposition, contentType };
}
