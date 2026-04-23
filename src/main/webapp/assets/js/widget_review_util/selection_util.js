// widget_review_util/selection_util.js

export function normalizeSelectedEntries(entries, max = 5000) {
    if (!Array.isArray(entries)) return [];
    const out = [];

    for (const e of entries) {
        if (out.length >= max) break;
        if (!e || typeof e !== "object") continue;

        out.push({
            chatId: str(e.chatId, 200),
            prompt: str(e.prompt, 8000),
            response: str(e.response, 12000),
            createdAt: str(e.createdAt, 120),
            sessionId: str(e.sessionId, 200)
        });
    }

    return out;
}

export function dedupeByKey(arr, keyFn) {
    if (!Array.isArray(arr) || !keyFn) return [];
    const seen = new Set();
    const out = [];

    for (const item of arr) {
        const key = keyFn(item);
        if (seen.has(key)) continue;
        seen.add(key);
        out.push(item);
    }

    return out;
}

export function chunkBy(arr, size = 100) {
    if (!Array.isArray(arr) || size <= 0) return [];
    const out = [];
    for (let i = 0; i < arr.length; i += size) {
        out.push(arr.slice(i, i + size));
    }
    return out;
}

function str(v, max) {
    const s = (v ?? "").toString().trim();
    return s.length <= max ? s : s.slice(0, max);
}
