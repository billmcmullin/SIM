// widget_review.js
import { getJson, postJson, buildHeaders } from "./widget_review_util/api_client.js";
import { normalizeSelectedEntries, chunkBy, dedupeByKey } from "./widget_review_util/selection_util.js";
import { buildManualMessagePayload, buildBatchAnalyzePayload } from "./widget_review_util/payload_builder.js";
import { estimateTokens, trimToBudget, compressWhitespace } from "./widget_review_util/text_budget_util.js";
import { renderLoading, renderError, renderMarkdown, renderStatusPill } from "./widget_review_util/render_util.js";
import { info, warn, error, timed } from "./widget_review_util/logger.js";

const CFG = window.widgetReviewConfig || {};
const CONTEXT_PATH = (CFG.contextPath || "").replace(/\/+$/, "");
const SELECTION_ID = (CFG.selectionId || "").trim();

const DEFAULTS = {
    reviewDataEndpointPath: `${CONTEXT_PATH}/dashboard/widgets/drilldown/view/review-data`,
    manualMessageEndpoint: `${CONTEXT_PATH}/dashboard/drilldown/widget-review/manual-message`,
    batchAnalyzeEndpoint: `${CONTEXT_PATH}/dashboard/drilldown/widget-review/batch-analyze`,
    maxSelectedEntries: 5000,
    pageSize: 10,
    batchSize: 150
};

const state = {
    rows: [],
    filteredRows: [],
    pageSize: DEFAULTS.pageSize,
    page: 1,
    selectedIds: new Set(),
    manualSectionOpen: false,
    lastManualSessionId: ""
};

// -------------------- Exported API --------------------

export async function sendManualMessage({
    message,
    selectedEntries,
    mode = "chat",
    sessionId = "",
    reset = false,
    attachments = []
}) {
    const t = timed("sendManualMessage");
    try {
        const cleanEntries = normalizeSelectedEntries(selectedEntries, DEFAULTS.maxSelectedEntries);
        const payload = buildManualMessagePayload({
            message,
            mode,
            sessionId,
            reset,
            attachments,
            selectedEntries: cleanEntries
        });

        info("manual message payload prepared", {
            selected: cleanEntries.length,
            estimatedTokens: estimateTokens(message || ""),
            endpoint: DEFAULTS.manualMessageEndpoint
        });

        const res = await fetch(DEFAULTS.manualMessageEndpoint, {
            method: "POST",
            credentials: "same-origin",
            headers: {
                ...buildHeaders(),
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(payload)
        });

        const text = await res.text();
        let data = {};
        try {
            data = text ? JSON.parse(text) : {};
        } catch {
            data = { raw: text };
        }

        if (!res.ok) {
            const requestId = data?.requestId || "";
            const messageText = data?.message || data?.error || "";
            const err = new Error(
                `POST ${DEFAULTS.manualMessageEndpoint} failed with ${res.status}`
                + (requestId ? ` [${requestId}]` : "")
                + (messageText ? `: ${messageText}` : "")
            );
            err.status = res.status;
            err.data = data;
            err.requestId = requestId;
            throw err;
        }

        t.end({ ok: true, status: res.status });
        return data;
    } catch (e) {
        t.end({ ok: false });
        error("sendManualMessage failed", e);
        throw e;
    }
}

export async function analyzeInBatches({ prompt, selectedEntries, onProgress = () => { } }) {
    const t = timed("analyzeInBatches");
    try {
        const clean = dedupeByKey(
            normalizeSelectedEntries(selectedEntries, DEFAULTS.maxSelectedEntries),
            (x) => x.chatId || `${x.createdAt}|${(x.prompt || "").slice(0, 24)}`
        );

        const batches = chunkBy(clean, DEFAULTS.batchSize);
        const outputs = [];

        info("batch analysis start", { total: clean.length, batches: batches.length });

        for (let i = 0; i < batches.length; i++) {
            const batch = batches[i];
            onProgress({ index: i + 1, total: batches.length, size: batch.length });

            const payload = buildBatchAnalyzePayload({
                prompt: trimToBudget(compressWhitespace(prompt || ""), 12000),
                selectedEntries: batch
            });

            const resp = await postJson(DEFAULTS.batchAnalyzeEndpoint, payload, {
                headers: buildHeaders()
            });

            outputs.push(resp);
        }

        t.end({ ok: true, batches: batches.length });
        return outputs;
    } catch (e) {
        t.end({ ok: false });
        error("analyzeInBatches failed", e);
        throw e;
    }
}

export function showLoading(el, text = "Loading...") {
    renderLoading(el, text);
}
export function showError(el, message, requestId = "") {
    renderError(el, message, requestId);
}
export function showMarkdown(el, md) {
    renderMarkdown(el, md);
}
export function showStatus(el, text, tone = "neutral") {
    renderStatusPill(el, text, tone);
}

// -------------------- Page bootstrap --------------------

async function initPage() {
    const tbody = document.getElementById("widgetReviewBody");
    if (!tbody) return;

    wireBasicUi();
    wireManualMessageUi();

    if (!SELECTION_ID) {
        renderErrorRow("Missing selectionId.");
        return;
    }

    renderLoadingRow("Loading selected chats…");

    try {
        const raw = await fetchSelectionData(SELECTION_ID);
        const rows = normalizeIncomingRows(raw);

        state.rows = rows;
        applyFilterAndRender();

        const searchTermsDisplay = document.getElementById("searchTermsDisplay");
        if (searchTermsDisplay && raw?.searchTerms) {
            const g = raw.searchTerms.global || "";
            const p = raw.searchTerms.prompt || "";
            const r = raw.searchTerms.response || "";
            const parts = [];
            if (g) parts.push(`global: "${escapeHtml(g)}"`);
            if (p) parts.push(`prompt: "${escapeHtml(p)}"`);
            if (r) parts.push(`response: "${escapeHtml(r)}"`);
            searchTermsDisplay.innerHTML = parts.length
                ? `<span>Applied search terms: ${parts.join(" • ")}</span>`
                : `<span>No search terms were applied.</span>`;
        }

        info("review rows loaded", { count: rows.length, selectionId: SELECTION_ID });
    } catch (e) {
        error("failed loading selected chats", e);
        renderErrorRow(humanizeLoadError(e));
    }
}

// -------------------- UI wiring --------------------

function wireBasicUi() {
    const pageSizeSel = document.getElementById("reviewPageSize");
    const searchInput = document.getElementById("reviewSearchInput");
    const prevBtn = document.getElementById("prevPageBtn");
    const nextBtn = document.getElementById("nextPageBtn");
    const selectAllVisible = document.getElementById("reviewSelectAll");
    const selectAllBtn = document.getElementById("selectAllEntriesBtn");
    const deselectAllBtn = document.getElementById("deselectAllBtn");

    if (pageSizeSel) {
        pageSizeSel.addEventListener("change", () => {
            state.pageSize = Math.max(1, parseInt(pageSizeSel.value || "10", 10));
            state.page = 1;
            renderTable();
        });
    }

    if (searchInput) {
        searchInput.addEventListener("input", () => {
            state.page = 1;
            applyFilterAndRender();
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener("click", () => {
            state.page = Math.max(1, state.page - 1);
            renderTable();
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener("click", () => {
            const totalPages = Math.max(1, Math.ceil(state.filteredRows.length / state.pageSize));
            state.page = Math.min(totalPages, state.page + 1);
            renderTable();
        });
    }

    if (selectAllVisible) {
        selectAllVisible.addEventListener("change", () => {
            const checked = !!selectAllVisible.checked;
            const pageRows = getCurrentPageRows();
            for (const r of pageRows) {
                const key = rowKey(r);
                if (checked) state.selectedIds.add(key);
                else state.selectedIds.delete(key);
            }
            renderTable();
        });
    }

    if (selectAllBtn) {
        selectAllBtn.addEventListener("click", () => {
            for (const r of state.filteredRows) state.selectedIds.add(rowKey(r));
            renderTable();
        });
    }

    if (deselectAllBtn) {
        deselectAllBtn.addEventListener("click", () => {
            state.selectedIds.clear();
            renderTable();
        });
    }
}

function wireManualMessageUi() {
    const toggleBtn = document.getElementById("manualMessageToggleBtn");
    const section = document.getElementById("manualMessageSection");
    const closeBtn = document.getElementById("manualMessageCloseBtn");
    const clearBtn = document.getElementById("manualMessageClearBtn");
    const sendBtn = document.getElementById("manualMessageSendBtn");

    if (!toggleBtn || !section) return;

    const openSection = () => {
        section.hidden = false;
        section.setAttribute("aria-hidden", "false");
        state.manualSectionOpen = true;
        updateManualSelectionPreview();
    };

    const closeSection = () => {
        section.hidden = true;
        section.setAttribute("aria-hidden", "true");
        state.manualSectionOpen = false;
    };

    toggleBtn.addEventListener("click", () => {
        const isHidden = section.hidden || section.getAttribute("aria-hidden") === "true";
        if (isHidden) openSection();
        else closeSection();
    });

    if (closeBtn) {
        closeBtn.addEventListener("click", closeSection);
    }

    if (clearBtn) {
        clearBtn.addEventListener("click", () => {
            const text = document.getElementById("manualMessageText");
            const status = document.getElementById("manualMessageStatus");
            const preview = document.getElementById("manualMessageSelectionPreview");
            const response = document.getElementById("manualMessageResponse");

            if (text) text.value = "";
            if (status) status.textContent = "";
            if (preview) preview.value = "No response yet.";
            if (response) response.textContent = "No response yet.";

            state.lastManualSessionId = "";
        });
    }

    if (sendBtn) {
        sendBtn.addEventListener("click", onManualMessageSend);
    }
}

async function onManualMessageSend() {
    const textEl = document.getElementById("manualMessageText");
    const statusEl = document.getElementById("manualMessageStatus");
    const responseEl = document.getElementById("manualMessageResponse");

    const message = (textEl?.value || "").trim();
    const selectedEntries = getSelectedEntries();

    if (!message) {
        if (statusEl) statusEl.textContent = "Enter a message first.";
        return;
    }
    if (!selectedEntries.length) {
        if (statusEl) statusEl.textContent = "Select at least one chat entry.";
        return;
    }

    try {
        if (statusEl) statusEl.textContent = "Sending…";
        if (responseEl) responseEl.textContent = "Loading response…";

        const resp = await sendManualMessage({
            message,
            selectedEntries,
            mode: "chat",
            sessionId: state.lastManualSessionId || "",
            reset: false,
            attachments: []
        });

        info("manual response received", {
            keys: Object.keys(resp || {}),
            type: resp?.type,
            hasTextResponse: !!resp?.textResponse
        });

        state.lastManualSessionId =
            resp?.sessionId ||
            resp?.workspaceSessionId ||
            resp?.id ||
            state.lastManualSessionId ||
            "";

        const responseText =
            resp?.textResponse ||
            resp?.response ||
            resp?.message ||
            resp?.answer ||
            resp?.output ||
            resp?.raw ||
            "No response returned.";

        // Render markdown instead of plain text so headings/bullets format correctly
        if (responseEl) {
            renderMarkdown(responseEl, String(responseText));
        }

        if (statusEl) statusEl.textContent = "Sent.";
    } catch (e) {
        const requestId = e?.data?.requestId || e?.requestId || "";
        const backendMessage = e?.data?.message || e?.data?.error || "";
        if (statusEl) {
            statusEl.textContent =
                `Send failed${e?.status ? ` (HTTP ${e.status})` : ""}${requestId ? ` [${requestId}]` : ""}`
                + (backendMessage ? `: ${backendMessage}` : ".");
        }
        if (responseEl) responseEl.textContent = "No response yet.";
        error("manual message send failed", e);
    }
}

// -------------------- Data loading --------------------

async function fetchSelectionData(selectionId) {
    const qs = new URLSearchParams({
        selectionId,
        page: "1",
        limit: String(DEFAULTS.maxSelectedEntries),
        sortColumn: "created_at",
        sortDir: "DESC"
    });

    const relativeUrl = `${DEFAULTS.reviewDataEndpointPath}?${qs.toString()}`;
    info("fetching review data", { url: relativeUrl });

    try {
        return await getJson(relativeUrl);
    } catch (e) {
        warn("fetch failed, trying XHR fallback", { message: e?.message, url: relativeUrl });
        return await getJsonViaXhr(relativeUrl);
    }
}

function getJsonViaXhr(url) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", url, true);
        xhr.withCredentials = true;
        xhr.setRequestHeader("Accept", "application/json");

        xhr.onreadystatechange = () => {
            if (xhr.readyState !== 4) return;

            if (xhr.status >= 200 && xhr.status < 300) {
                try {
                    const text = xhr.responseText || "";
                    resolve(text ? JSON.parse(text) : {});
                } catch {
                    resolve({});
                }
                return;
            }

            const err = new Error(`XHR failed with status ${xhr.status}`);
            err.status = xhr.status;
            err.body = xhr.responseText || "";
            reject(err);
        };

        xhr.onerror = () => reject(new TypeError("XHR network error"));
        xhr.send();
    });
}

function normalizeIncomingRows(payload) {
    let rows = [];
    if (Array.isArray(payload)) rows = payload;
    else if (Array.isArray(payload?.rows)) rows = payload.rows;
    else if (Array.isArray(payload?.selectedEntries)) rows = payload.selectedEntries;
    else if (Array.isArray(payload?.data)) rows = payload.data;

    return normalizeSelectedEntries(rows, DEFAULTS.maxSelectedEntries);
}

// -------------------- Table rendering --------------------

function applyFilterAndRender() {
    const q = (document.getElementById("reviewSearchInput")?.value || "").trim().toLowerCase();

    if (!q) {
        state.filteredRows = [...state.rows];
    } else {
        state.filteredRows = state.rows.filter((r) => {
            const hay = `${r.chatId || ""} ${r.prompt || ""} ${r.response || ""} ${r.sessionId || ""}`.toLowerCase();
            return hay.includes(q);
        });
    }

    renderTable();
    if (state.manualSectionOpen) updateManualSelectionPreview();
}

function renderTable() {
    const tbody = document.getElementById("widgetReviewBody");
    const pageInfo = document.getElementById("pageInfo");
    const selectAllVisible = document.getElementById("reviewSelectAll");

    if (!tbody) return;

    if (!state.filteredRows.length) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-row">No chats found.</td></tr>`;
        if (pageInfo) pageInfo.textContent = "0 results";
        if (selectAllVisible) {
            selectAllVisible.checked = false;
            selectAllVisible.indeterminate = false;
        }
        if (state.manualSectionOpen) updateManualSelectionPreview();
        return;
    }

    const totalPages = Math.max(1, Math.ceil(state.filteredRows.length / state.pageSize));
    state.page = Math.min(Math.max(1, state.page), totalPages);

    const pageRows = getCurrentPageRows();

    tbody.innerHTML = pageRows.map((r) => {
        const key = rowKey(r);
        const checked = state.selectedIds.has(key) ? "checked" : "";
        return `
      <tr>
        <td class="select-column">
          <input type="checkbox" class="row-select" data-key="${escapeHtml(key)}" ${checked}>
        </td>
        <td>${escapeHtml(r.chatId || "")}</td>
        <td title="${escapeHtml(r.prompt || "")}">${escapeHtml((r.prompt || "").slice(0, 220))}</td>
        <td>${escapeHtml(r.createdAt || "")}</td>
        <td>${escapeHtml(r.sessionIdDisplay || r.sessionId || "")}</td>
      </tr>
    `;
    }).join("");

    tbody.querySelectorAll("input.row-select").forEach((cb) => {
        cb.addEventListener("change", () => {
            const key = cb.getAttribute("data-key") || "";
            if (!key) return;
            if (cb.checked) state.selectedIds.add(key);
            else state.selectedIds.delete(key);
            syncVisibleSelectAll();
            if (state.manualSectionOpen) updateManualSelectionPreview();
        });
    });

    if (pageInfo) {
        pageInfo.textContent = `Page ${state.page} of ${totalPages} • ${state.filteredRows.length} result(s)`;
    }

    syncVisibleSelectAll();
    if (state.manualSectionOpen) updateManualSelectionPreview();
}

function syncVisibleSelectAll() {
    const selectAllVisible = document.getElementById("reviewSelectAll");
    if (!selectAllVisible) return;

    const pageRows = getCurrentPageRows();
    if (!pageRows.length) {
        selectAllVisible.checked = false;
        selectAllVisible.indeterminate = false;
        return;
    }

    let selectedCount = 0;
    for (const r of pageRows) {
        if (state.selectedIds.has(rowKey(r))) selectedCount++;
    }

    selectAllVisible.checked = selectedCount === pageRows.length;
    selectAllVisible.indeterminate = selectedCount > 0 && selectedCount < pageRows.length;
}

function getCurrentPageRows() {
    const start = (state.page - 1) * state.pageSize;
    return state.filteredRows.slice(start, start + state.pageSize);
}

function getSelectedEntries() {
    const keySet = state.selectedIds;
    return state.rows.filter((r) => keySet.has(rowKey(r)));
}

function updateManualSelectionPreview() {
    const preview = document.getElementById("manualMessageSelectionPreview");
    if (!preview) return;

    const selected = getSelectedEntries();
    if (!selected.length) {
        preview.value = "No entries selected.";
        return;
    }

    const lines = [];
    for (const r of selected.slice(0, 200)) {
        lines.push(`- Chat ID: ${r.chatId || ""}`);
        lines.push(`  Session: ${r.sessionId || ""}`);
        lines.push(`  Created: ${r.createdAt || ""}`);
        lines.push(`  Prompt: ${(r.prompt || "").slice(0, 500)}`);
    }
    if (selected.length > 200) {
        lines.push(`\n... and ${selected.length - 200} more selected entries.`);
    }

    preview.value = lines.join("\n");
}

function rowKey(r) {
    return r.chatId || `${r.createdAt || ""}|${(r.prompt || "").slice(0, 24)}|${r.sessionId || ""}`;
}

// -------------------- Helpers --------------------

function renderLoadingRow(text) {
    const tbody = document.getElementById("widgetReviewBody");
    if (!tbody) return;
    tbody.innerHTML = `<tr><td colspan="5" class="empty-row">${escapeHtml(text)}</td></tr>`;
}

function renderErrorRow(text) {
    const tbody = document.getElementById("widgetReviewBody");
    if (!tbody) return;
    tbody.innerHTML = `<tr><td colspan="5" class="empty-row">${escapeHtml(text)}</td></tr>`;
}

function humanizeLoadError(e) {
    if (e instanceof TypeError) return "Network/CSP blocked the data request. Check browser console + server/proxy logs.";
    if (typeof e?.status === "number") return `Failed to load selected chats (HTTP ${e.status}).`;
    return "Failed to load selected chats.";
}

function escapeHtml(s) {
    return (s ?? "")
        .toString()
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

// Optional global exposure for legacy handlers
window.widgetReview = {
    sendManualMessage,
    analyzeInBatches,
    showLoading,
    showError,
    showMarkdown,
    showStatus,
    getSelectedEntries
};

document.addEventListener("DOMContentLoaded", initPage);
