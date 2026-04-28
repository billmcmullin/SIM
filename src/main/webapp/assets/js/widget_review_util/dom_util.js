// widget_review_util/dom_util.js

export function escapeHtml(s) {
    return (s ?? "")
        .toString()
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

/**
 * Parse filename from Content-Disposition.
 * Supports:
 *   filename*=UTF-8''encoded
 *   filename="plain.ext"
 */
export function extractFilenameFromContentDisposition(contentDisposition) {
    const cd = String(contentDisposition || "");
    if (!cd) return "";

    const star = cd.match(/filename\*=UTF-8''([^;]+)/i);
    if (star && star[1]) {
        try {
            return decodeURIComponent(star[1]);
        } catch {
            // ignore
        }
    }

    const quoted = cd.match(/filename="([^"]+)"/i);
    if (quoted && quoted[1]) return quoted[1];

    const plain = cd.match(/filename=([^;]+)/i);
    if (plain && plain[1]) return plain[1].trim();

    return "";
}

export function formatDurationHms(ms) {
    const totalSec = Math.max(0, Math.floor(Number(ms || 0) / 1000));
    const hh = Math.floor(totalSec / 3600);
    const mm = Math.floor((totalSec % 3600) / 60);
    const ss = totalSec % 60;
    return `${String(hh).padStart(2, "0")}:${String(mm).padStart(2, "0")}:${String(ss).padStart(2, "0")}`;
}

/**
 * Normalize ID arrays for robust comparisons:
 * - stringified
 * - trimmed
 * - lowercased
 * - deduped (preserve order)
 */
export function normalizeIds(ids) {
    const out = [];
    const seen = new Set();

    for (const id of Array.isArray(ids) ? ids : []) {
        const n = String(id ?? "").trim().toLowerCase();
        if (!n || seen.has(n)) continue;
        seen.add(n);
        out.push(n);
    }

    return out;
}

/**
 * Returns items present in a but not in b.
 */
export function subtractIds(a, b) {
    const setA = new Set(normalizeIds(a));
    const setB = new Set(normalizeIds(b));

    for (const id of setB) setA.delete(id);
    return Array.from(setA);
}

/**
 * Returns intersection of a and b.
 */
export function intersectIds(a, b) {
    const setA = new Set(normalizeIds(a));
    const setB = new Set(normalizeIds(b));

    const out = [];
    for (const id of setA) {
        if (setB.has(id)) out.push(id);
    }
    return out;
}

/**
 * Safe text setter.
 */
export function setTextById(id, value = "") {
    const el = document.getElementById(id);
    if (el) el.textContent = String(value ?? "");
    return el;
}

/**
 * Safe pre/text area setter with display toggle.
 */
export function setBlockTextById(id, value = "", show = true, displayMode = "block") {
    const el = document.getElementById(id);
    if (!el) return null;
    el.textContent = String(value ?? "");
    el.style.display = show ? displayMode : "none";
    return el;
}

/**
 * Tiny qs helper.
 */
export function byId(id) {
    return document.getElementById(id);
}
