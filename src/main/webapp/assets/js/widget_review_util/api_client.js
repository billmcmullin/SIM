// widget_review_util/api_client.js

import { warn } from "./logger.js";

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
    if (!text) return {};
    try {
        return JSON.parse(text);
    } catch {
        return { raw: text };
    }
}

/**
 * Converts a failed HTTP response to a rich Error object.
 */
async function toHttpError(res, url) {
    const data = await parseBody(res);
    const err = new Error(`POST ${url} failed with ${res.status}`);
    err.status = res.status;
    err.data = data;
    err.requestId = data?.requestId || "";
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
        const data = await parseBody(res);
        const err = new Error(`GET ${url} failed with ${res.status}`);
        err.status = res.status;
        err.data = data;
        err.requestId = data?.requestId || "";
        throw err;
    }

    return parseBody(res);
}

/**
 * Generic POST JSON helper.
 * Sends application/json unless caller overrides headers/body.
 */
export async function postJson(url, payload = {}, options = {}) {
    const headers = buildHeaders(options.headers || {});
    const body = options.body != null ? options.body : JSON.stringify(payload);

    const res = await fetch(url, {
        method: "POST",
        credentials: "same-origin",
        headers,
        body,
        ...options
    });

    if (!res.ok) {
        throw await toHttpError(res, url);
    }

    return parseBody(res);
}

/**
 * POST x-www-form-urlencoded helper.
 * Added for manual-message endpoint compatibility when servlet expects form data.
 */
export async function postForm(url, formObj = {}, options = {}) {
    const form = new URLSearchParams();

    Object.entries(formObj || {}).forEach(([k, v]) => {
        if (v === undefined || v === null) return;
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
        throw await toHttpError(res, url);
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
        if (e?.status !== 415) throw e;

        warn("postJson got 415, retrying as form-urlencoded", { url });

        // Flatten payload for form transport.
        // Arrays/objects are encoded as JSON strings.
        const formObj = {};
        Object.entries(payload || {}).forEach(([k, v]) => {
            if (v === undefined || v === null) return;
            if (typeof v === "object") formObj[k] = JSON.stringify(v);
            else formObj[k] = v;
        });

        return await postForm(url, formObj, options);
    }
}
