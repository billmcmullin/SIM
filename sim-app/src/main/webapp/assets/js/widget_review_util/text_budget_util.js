// widget_review_util/text_budget_util.js

/**
 * Hard trim by character budget.
 */
export function trimToBudget(text, maxChars) {
    const s = (text ?? "").toString();
    const n = Number(maxChars);
    if (!Number.isFinite(n) || n <= 0) {
        return "";
    }
    return s.length <= n ? s : s.slice(0, n);
}

/**
 * Normalize whitespace to single spaces and trim ends.
 */
export function compressWhitespace(text) {
    return (text ?? "").toString().replace(/\s+/g, " ").trim();
}

/**
 * Rough token estimate (English-ish heuristic).
 * Common approximation: ~4 chars/token.
 */
export function estimateTokens(text) {
    const s = (text ?? "").toString().trim();
    if (!s) {
        return 0;
    }
    return Math.ceil(s.length / 4);
}

/**
 * Safe text normalization pipeline used by payload builders.
 */
export function normalizeTextForBudget(text, maxChars = 12000) {
    return trimToBudget(compressWhitespace(text), maxChars);
}

/**
 * Trim by estimated token budget using char approximation.
 */
export function trimToTokenBudget(text, maxTokens = 3000) {
    const n = Number(maxTokens);
    if (!Number.isFinite(n) || n <= 0) {
        return "";
    }
    const maxChars = n * 4;
    return trimToBudget((text ?? "").toString(), maxChars);
}

/**
 * Return both normalized text and budget diagnostics.
 */
export function budgetText(text, { maxChars = 12000, maxTokens = null } = {}) {
    let normalized = compressWhitespace(text);

    if (maxTokens !== null && maxTokens !== undefined) {
        normalized = trimToTokenBudget(normalized, maxTokens);
    }

    normalized = trimToBudget(normalized, maxChars);

    return {
        text: normalized,
        chars: normalized.length,
        tokensEstimated: estimateTokens(normalized),
        maxChars: Number(maxChars),
        maxTokens: (maxTokens === null || maxTokens === undefined) ? null : Number(maxTokens)
    };
}
