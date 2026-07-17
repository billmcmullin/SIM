// widget_review_util/selection_util.js

function str(v, max) {
    const s = (v ?? "").toString().trim();
    return s.length <= max ? s : s.slice(0, max);
}

/**
 * Stable row key used for selection tracking.
 */
export function rowKey(r = {}) {
    return r.chatId || `${r.createdAt || ""}|${(r.prompt || "").slice(0, 24)}|${r.sessionId || ""}`;
}

/**
 * Normalize selected/review entries into a safe shape.
 */
export function normalizeSelectedEntries(entries, max = 5000) {
    if (!Array.isArray(entries)) {
        return [];
    }
    const out = [];

    for (const e of entries) {
        if (out.length >= max) {
            break;
        }
        if (!e || typeof e !== "object") {
            continue;
        }

        out.push({
            chatId: str(e.chatId, 200),
            prompt: str(e.prompt, 8000),
            response: str(e.response, 12000),
            createdAt: str(e.createdAt, 120),
            sessionId: str(e.sessionId, 200),
            // preserve optional display/session variants if present
            sessionIdDisplay: str(e.sessionIdDisplay || e.session_id || "", 220)
        });
    }

    return out;
}

/**
 * Pull rows from varying backend response shapes and normalize.
 */
export function normalizeIncomingRows(payload, max = 5000) {
    let rows = [];
    if (Array.isArray(payload)) {
        rows = payload;
    } else if (Array.isArray(payload?.rows)) {
        rows = payload.rows;
    } else if (Array.isArray(payload?.selectedEntries)) {
        rows = payload.selectedEntries;
    } else if (Array.isArray(payload?.data)) {
        rows = payload.data;
    }

    return normalizeSelectedEntries(rows, max);
}

export function dedupeByKey(arr, keyFn) {
    if (!Array.isArray(arr) || typeof keyFn !== "function") {
        return [];
    }
    const seen = new Set();
    const out = [];

    for (const item of arr) {
        const key = keyFn(item);
        if (seen.has(key)) {
            continue;
        }
        seen.add(key);
        out.push(item);
    }

    return out;
}

export function chunkBy(arr, size = 100) {
    if (!Array.isArray(arr) || size <= 0) {
        return [];
    }
    const out = [];
    for (let i = 0; i < arr.length; i += size) {
        out.push(arr.slice(i, i + size));
    }
    return out;
}

/**
 * Return selected entries from rows + selected key set.
 */
export function getSelectedEntries(rows = [], selectedIds = new Set()) {
    if (!Array.isArray(rows) || !(selectedIds instanceof Set)) {
        return [];
    }
    return rows.filter((r) => selectedIds.has(rowKey(r)));
}

/**
 * Apply free-text filter for review table.
 */
export function filterRows(rows = [], query = "") {
    if (!Array.isArray(rows)) {
        return [];
    }
    const q = String(query || "").trim().toLowerCase();
    if (!q) {
        return [...rows];
    }

    return rows.filter((r) => {
        const hay = `${r.chatId || ""} ${r.prompt || ""} ${r.response || ""} ${r.sessionId || ""}`.toLowerCase();
        return hay.includes(q);
    });
}

/**
 * Slice rows for current page.
 */
export function getPageRows(rows = [], page = 1, pageSize = 10) {
    const safePage = Math.max(1, Number(page) || 1);
    const safeSize = Math.max(1, Number(pageSize) || 10);
    const start = (safePage - 1) * safeSize;
    return rows.slice(start, start + safeSize);
}
