// widget_review_util/text_budget_util.js

export function trimToBudget(text, maxChars) {
    const s = (text ?? "").toString();
    if (maxChars <= 0) return "";
    return s.length <= maxChars ? s : s.slice(0, maxChars);
}

export function compressWhitespace(text) {
    return (text ?? "").toString().replace(/\s+/g, " ").trim();
}

export function estimateTokens(text) {
    const s = (text ?? "").toString().trim();
    if (!s) return 0;
    return Math.ceil(s.length / 4);
}
