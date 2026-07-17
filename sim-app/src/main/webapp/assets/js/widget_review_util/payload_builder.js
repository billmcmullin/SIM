// widget_review_util/payload_builder.js

import { trimToBudget, compressWhitespace } from "./text_budget_util.js";

const DEFAULT_TEXT_BUDGET = 12000;

function toArray(value, fallback = []) {
    return Array.isArray(value) ? value : fallback;
}

function normalizeText(value, budget = DEFAULT_TEXT_BUDGET) {
    return trimToBudget(compressWhitespace(String(value || "")), budget);
}

export function buildManualMessagePayload({
    message,
    mode = "chat",
    sessionId = "",
    reset = false,
    attachments = [],
    selectedEntries = [],
    async = true
}) {
    return {
        message: normalizeText(message),
        mode: String(mode || "chat"),
        sessionId: String(sessionId || ""),
        reset: Boolean(reset),
        async: Boolean(async),
        attachments: toArray(attachments, []),
        selectedEntries: toArray(selectedEntries, [])
    };
}

export function buildBatchAnalyzePayload({
    prompt,
    selectedEntries = []
}) {
    return {
        prompt: normalizeText(prompt),
        selectedEntries: toArray(selectedEntries, [])
    };
}

/**
 * Standard payload for export endpoint.
 * NOTE: reportMarkdown is included only when format === "pdf" and provided.
 */
export function buildExportPayload({
    selectionId = "",
    selectedChatIds = [],
    format = "csv",
    reportMarkdown = ""
}) {
    const normalizedFormat = String(format || "csv").toLowerCase();

    const payload = {
        selectionId: String(selectionId || ""),
        selectedChatIds: toArray(selectedChatIds, [])
            .map((x) => String(x || "").trim())
            .filter(Boolean),
        format: normalizedFormat
    };

    if (normalizedFormat === "pdf") {
        const md = String(reportMarkdown || "").trim();
        if (md) {
            payload.reportMarkdown = md;
        }
    }

    return payload;
}
