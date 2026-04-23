// widget_review_util/payload_builder.js

import { trimToBudget, compressWhitespace } from "./text_budget_util.js";

export function buildManualMessagePayload({
    message,
    mode = "chat",
    sessionId = "",
    reset = false,
    attachments = [],
    selectedEntries = []
}) {
    return {
        message: trimToBudget(compressWhitespace(message || ""), 12000),
        mode: mode || "chat",
        sessionId: (sessionId || "").toString(),
        reset: Boolean(reset),
        attachments: Array.isArray(attachments) ? attachments : [],
        selectedEntries: Array.isArray(selectedEntries) ? selectedEntries : []
    };
}

export function buildBatchAnalyzePayload({
    prompt,
    selectedEntries = []
}) {
    return {
        prompt: trimToBudget(compressWhitespace(prompt || ""), 12000),
        selectedEntries: Array.isArray(selectedEntries) ? selectedEntries : []
    };
}
