// widget_review.js
/* global XMLHttpRequest, HTMLElement */
import {
    getJson,
    postJson,
    postForDownload,
    deleteJson,
    buildHeaders
} from "./widget_review_util/api_client.js";

import {
    normalizeSelectedEntries,
    normalizeIncomingRows,
    chunkBy,
    dedupeByKey,
    rowKey,
    getSelectedEntries as getSelectedEntriesFromState,
    filterRows,
    getPageRows
} from "./widget_review_util/selection_util.js";

import {
    buildManualMessagePayload,
    buildBatchAnalyzePayload,
    buildExportPayload
} from "./widget_review_util/payload_builder.js";

import {
    estimateTokens,
    normalizeTextForBudget
} from "./widget_review_util/text_budget_util.js";

import {
    renderLoading,
    renderError,
    renderMarkdown,
    renderStatusPill
} from "./widget_review_util/render_util.js";

import { info, warn, error, timed } from "./widget_review_util/logger.js";

import {
    humanizePhase,
    parseSynthesisFromActivity,
    deriveWeightedProgress,
    deriveSynthesisPercent,
    formatSynthesisLine,
    createInitialSynthState
} from "./widget_review_util/job_progress_util.js";

import {
    escapeHtml,
    extractFilenameFromContentDisposition,
    formatDurationHms,
    normalizeIds,
    subtractIds,
    intersectIds,
    setTextById,
    setBlockTextById,
    byId
} from "./widget_review_util/dom_util.js";

const CFG = window.widgetReviewConfig || {};
const CONTEXT_PATH = (CFG.contextPath || "").replace(/\/+$/, "");
const SELECTION_ID = (CFG.selectionId || "").trim();

const DEFAULT_ANALYZE_PROMPT = (
    CFG.defaultAnalyzePrompt
    || "Analyze the selected chats and produce a manager-ready report with: Executive Chat Analysis, Key Metrics table, Sentiment and Frustration Signals, Risks and Opportunities, Recommendations, and Coverage and Methodology. Use concise evidence-based language and include coverage accounting."
).trim();

const DEFAULTS = {
    reviewDataEndpointPath: `${CONTEXT_PATH}/dashboard/widgets/drilldown/view/review-data`,
    manualMessageEndpoint: `${CONTEXT_PATH}/dashboard/drilldown/widget-review/manual-message`,
    jobStatusEndpoint: `${CONTEXT_PATH}/dashboard/drilldown/widget-review/job-status`,
    batchAnalyzeEndpoint: `${CONTEXT_PATH}/dashboard/drilldown/widget-review/batch-analyze`,
    translateEndpoint: CFG.translateEndpoint || `${CONTEXT_PATH}/dashboard/widgets/drilldown/review/translate`,
    exportEndpoint: `${CONTEXT_PATH}/dashboard/widgets/drilldown/export`,
    maxSelectedEntries: 5000,
    pageSize: 10,
    batchSize: 150,
    pollMs: 1200,
    pollMaxMs: 1000 * 60 * 20
};

const state = {
    rows: [],
    filteredRows: [],
    pageSize: DEFAULTS.pageSize,
    page: 1,
    selectedIds: new Set(),
    manualSectionOpen: false,
    lastManualSessionId: "",
    activeJobId: "",
    jobPollTimer: null,
    jobPollStartedAt: 0,
    activeDetailKey: "",
    lastReportMarkdown: "",
    synth: createInitialSynthState()
};

function reorderReportSections(markdown) {
    const md = String(markdown || "");
    if (!md.trim()) {
        return md;
    }

    const desiredOrder = [
        "Executive Chat Analysis",
        "Risks and Opportunities",
        "Recommendations",
        "Sentiment and Frustration Signals",
        "Coverage and Methodology",
        "Key Metrics"
    ];

    const lines = md.split("\n");
    const sections = [];
    let i = 0;

    while (i < lines.length) {
        const m = lines[i].match(/^##\s+(.+?)\s*$/);
        if (!m) {
            i++;
            continue;
        }

        const title = (m[1] || "").trim();
        const start = i;
        i++;

        while (i < lines.length && !/^##\s+/.test(lines[i])) {
            i++;
        }
        const end = i;

        sections.push({
            title,
            key: title.toLowerCase(),
            content: lines.slice(start, end).join("\n").trim()
        });
    }

    if (!sections.length) {
        return md;
    }

    const desiredKeys = desiredOrder.map((s) => s.toLowerCase());
    const byKey = new Map(sections.map((s) => [s.key, s]));

    const ordered = [];
    for (const k of desiredKeys) {
        if (byKey.has(k)) {
            ordered.push(byKey.get(k).content);
        }
    }

    for (const s of sections) {
        if (!desiredKeys.includes(s.key)) {
            ordered.push(s.content);
        }
    }

    return ordered.join("\n\n").replace(/\n{3,}/g, "\n\n").trim();
}

export async function sendManualMessage({
    message,
    selectedEntries,
    mode = "chat",
    sessionId = "",
    reset = false,
    attachments = [],
    async = true
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
            selectedEntries: cleanEntries,
            async
        });

        info("manual message payload prepared", {
            selected: cleanEntries.length,
            estimatedTokens: estimateTokens(message || ""),
            endpoint: DEFAULTS.manualMessageEndpoint,
            async: Boolean(async)
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
            const err = new Error(`POST ${DEFAULTS.manualMessageEndpoint} failed with ${res.status}${requestId ? ` [${requestId}]` : ""}${messageText ? `: ${messageText}` : ""}`);
            err.status = res.status;
            err.data = data;
            err.requestId = requestId;
            throw err;
        }

        t.end({ ok: true, status: res.status });
        return { status: res.status, data };
    } catch (e) {
        t.fail?.({ ok: false });
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
                prompt: normalizeTextForBudget(prompt || "", 12000),
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
        t.fail?.({ ok: false });
        error("analyzeInBatches failed", e);
        throw e;
    }
}

function getCurrentReportMarkdown() {
    return (state.lastReportMarkdown || "").trim();
}

function setReportMarkdown(md) {
    state.lastReportMarkdown = String(md || "");
    const responseEl = byId("manualMessageResponse");
    if (!responseEl) {
        return;
    }
    renderMarkdown(responseEl, state.lastReportMarkdown || "No analysis yet.");
}

function setQuickPdfVisibility({ show, enabled }) {
    const btn = byId("quickPdfAfterAnalyzeBtn");
    if (!btn) {
        return;
    }
    btn.hidden = !show;
    btn.disabled = !enabled;
}

function notifyUser(message) {
    const statusEl = byId("manualMessageStatus");
    const text = String(message || "");
    if (statusEl) {
        statusEl.textContent = text;
    }
    console.warn(text);
}

export async function exportSelected(format = "csv") {
    const selected = getSelectedEntries();
    if (!selected.length) {
        notifyUser("Select at least one chat to export.");
        return;
    }

    const selectedChatIds = selected
        .map((r) => String(r.chatId || "").trim())
        .filter(Boolean);

    const normalizedFormat = String(format || "csv").toLowerCase();
    const reportForExport = normalizedFormat === "pdf"
        ? reorderReportSections(getCurrentReportMarkdown())
        : getCurrentReportMarkdown();

    if (normalizedFormat === "pdf") {
        info("PDF export markdown check", {
            keyMetricsIndex: reportForExport.toLowerCase().indexOf("key metrics"),
            preview: reportForExport.slice(0, 800)
        });
    }

    const payload = buildExportPayload({
        selectionId: SELECTION_ID,
        selectedChatIds,
        format: normalizedFormat,
        reportMarkdown: reportForExport
    });

    info("export requested", {
        endpoint: DEFAULTS.exportEndpoint,
        format: payload.format,
        count: selectedChatIds.length,
        hasReportMarkdown: Boolean(payload.reportMarkdown)
    });

    const { blob, filename } = await postForDownload(DEFAULTS.exportEndpoint, payload, {
        headers: { ...buildHeaders(), Accept: "*/*" }
    });

    const fallbackExt = payload.format === "pdf"
        ? "pdf"
        : payload.format === "json"
            ? "json"
            : payload.format === "text"
                ? "txt"
                : "csv";

    const resolvedName = filename
        || extractFilenameFromContentDisposition("")
        || `chats-export.${fallbackExt}`;

    const url = URL.createObjectURL(blob);
    try {
        const a = document.createElement("a");
        a.href = url;
        a.download = resolvedName;
        document.body.appendChild(a);
        a.click();
        a.remove();
    } finally {
        URL.revokeObjectURL(url);
    }
}

export function showLoading(el, text = "Loading...") { renderLoading(el, text); }
export function showError(el, message, requestId = "") { renderError(el, message, requestId); }
export function showMarkdown(el, md) { renderMarkdown(el, md); }
export function showStatus(el, text, tone = "neutral") { renderStatusPill(el, text, tone); }

async function initPage() {
    const tbody = byId("widgetReviewBody");
    if (!tbody) {
        return;
    }

    wireBasicUi();
    wireManualMessageUi();
    wireDetailCardUi();

    if (!SELECTION_ID) {
        renderErrorRow("Missing selectionId.");
        return;
    }

    renderLoadingRow("Loading selected chats…");

    try {
        const raw = await fetchSelectionData(SELECTION_ID);
        state.rows = normalizeIncomingRows(raw, DEFAULTS.maxSelectedEntries);
        applyFilterAndRender();

        const searchTermsDisplay = byId("searchTermsDisplay");
        if (searchTermsDisplay && raw?.searchTerms) {
            const g = raw.searchTerms.global || "";
            const p = raw.searchTerms.prompt || "";
            const r = raw.searchTerms.response || "";
            const parts = [];
            if (g) {
                parts.push(`global: "${escapeHtml(g)}"`);
            }
            if (p) {
                parts.push(`prompt: "${escapeHtml(p)}"`);
            }
            if (r) {
                parts.push(`response: "${escapeHtml(r)}"`);
            }
            searchTermsDisplay.innerHTML = parts.length
                ? `<span>Applied search terms: ${parts.join(" • ")}</span>`
                : `<span>No search terms were applied.</span>`;
        }

        info("review rows loaded", { count: state.rows.length, selectionId: SELECTION_ID });
    } catch (e) {
        error("failed loading selected chats", e);
        renderErrorRow(humanizeLoadError(e));
    }
}

function wireBasicUi() {
    const pageSizeSel = byId("reviewPageSize");
    const searchInput = byId("reviewSearchInput");
    const prevBtn = byId("prevPageBtn");
    const nextBtn = byId("nextPageBtn");
    const selectAllVisible = byId("reviewSelectAll");
    const selectAllBtn = byId("selectAllEntriesBtn");
    const deselectAllBtn = byId("deselectAllBtn");

    const exportCsvBtn = byId("exportCsvBtn");
    const exportJsonBtn = byId("exportJsonBtn");
    const exportTextBtn = byId("exportTextBtn");
    const exportFormatSel = byId("exportFormatSelect");
    const exportBtn = byId("exportSelectedBtn");

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
            const checked = Boolean(selectAllVisible.checked);
            for (const r of getCurrentPageRows()) {
                const key = rowKey(r);
                if (checked) {
                    state.selectedIds.add(key);
                } else {
                    state.selectedIds.delete(key);
                }
            }
            renderTable();
        });
    }

    if (selectAllBtn) {
        selectAllBtn.addEventListener("click", () => {
            for (const r of state.filteredRows) {
                state.selectedIds.add(rowKey(r));
            }
            renderTable();
        });
    }

    if (deselectAllBtn) {
        deselectAllBtn.addEventListener("click", () => {
            state.selectedIds.clear();
            renderTable();
        });
    }

    if (exportCsvBtn) {
        exportCsvBtn.addEventListener("click", async () => {
            try {
                await exportSelected("csv");
            } catch (e) {
                error("csv export failed", e);
                notifyUser(e.message || "CSV export failed.");
            }
        });
    }
    if (exportJsonBtn) {
        exportJsonBtn.addEventListener("click", async () => {
            try {
                await exportSelected("json");
            } catch (e) {
                error("json export failed", e);
                notifyUser(e.message || "JSON export failed.");
            }
        });
    }
    if (exportTextBtn) {
        exportTextBtn.addEventListener("click", async () => {
            try {
                await exportSelected("text");
            } catch (e) {
                error("text export failed", e);
                notifyUser(e.message || "Text export failed.");
            }
        });
    }

    if (exportBtn) {
        exportBtn.addEventListener("click", async () => {
            let f = (exportFormatSel?.value || "csv").toLowerCase();
            if (f === "pdf") {
                f = "csv";
            }
            try {
                await exportSelected(f);
            } catch (e) {
                error("export failed", e);
                notifyUser(e.message || "Export failed.");
            }
        });
    }
}

function wireManualMessageUi() {
    const toggleBtn = byId("manualMessageToggleBtn");
    const section = byId("manualMessageSection");
    const closeBtn = byId("manualMessageCloseBtn");
    const clearBtn = byId("manualMessageClearBtn");
    const sendBtn = byId("manualMessageSendBtn");
    const cancelBtn = byId("manualMessageCancelJobBtn");
    const quickPdfBtn = byId("quickPdfAfterAnalyzeBtn");

    if (!toggleBtn || !section) {
        return;
    }

    const openSection = () => {
        section.hidden = false;
        section.setAttribute("aria-hidden", "false");
        state.manualSectionOpen = true;
        updateManualSelectedCount();
        setQuickPdfVisibility({ show: true, enabled: Boolean(getCurrentReportMarkdown()) });
    };
    const closeSection = () => {
        section.hidden = true;
        section.setAttribute("aria-hidden", "true");
        state.manualSectionOpen = false;
        setQuickPdfVisibility({ show: false, enabled: false });
    };

    toggleBtn.addEventListener("click", () => {
        const isHidden = section.hidden || section.getAttribute("aria-hidden") === "true";
        if (isHidden) {
            openSection();
        } else {
            closeSection();
        }
    });

    if (closeBtn) {
        closeBtn.addEventListener("click", closeSection);
    }

    if (clearBtn) {
        clearBtn.addEventListener("click", () => {
            setTextById("manualMessageStatus", "");
            resetProgressUi();
            setReportMarkdown("");
            stopJobPolling();
            state.lastManualSessionId = "";
            setQuickPdfVisibility({ show: true, enabled: false });
            updateManualSelectedCount();
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener("click", async () => {
            if (!state.activeJobId) {
                return;
            }
            try {
                await cancelJob(state.activeJobId);
                setManualStatus("Stop requested.");
            } catch (e) {
                warn("cancel failed", e);
                setManualStatus("Could not stop running work.");
            }
        });
    }

    if (quickPdfBtn) {
        quickPdfBtn.addEventListener("click", async () => {
            try {
                await exportSelected("pdf");
            } catch (e) {
                error("quick pdf export failed", e);
                notifyUser(e.message || "PDF export failed.");
            }
        });
    }

    if (sendBtn) {
        sendBtn.addEventListener("click", onManualMessageSend);
    }
}

function wireDetailCardUi() {
    const promptBtn = byId("translatePromptBtn");
    const responseBtn = byId("translateResponseBtn");
    const langSel = byId("translateTargetLang");

    if (promptBtn) {
        promptBtn.addEventListener("click", async () => {
            const row = getActiveDetailRow();
            if (!row) {
                return;
            }
            await translateText(row.prompt || "", langSel?.value || "en", "promptTranslationMeta", "promptTranslationOutput", "Prompt");
        });
    }

    if (responseBtn) {
        responseBtn.addEventListener("click", async () => {
            const row = getActiveDetailRow();
            if (!row) {
                return;
            }
            await translateText(row.response || "", langSel?.value || "en", "responseTranslationMeta", "responseTranslationOutput", "Response");
        });
    }
}

async function onManualMessageSend() {
    const statusEl = byId("manualMessageStatus");

    const message = DEFAULT_ANALYZE_PROMPT;
    const selectedEntries = getSelectedEntries();
    const useAsync = true;

    if (!selectedEntries.length) {
        if (statusEl) {
            statusEl.textContent = "Select at least one chat entry.";
        }
        return;
    }

    try {
        stopJobPolling();
        resetProgressUi();
        setQuickPdfVisibility({ show: true, enabled: false });

        if (statusEl) {
            statusEl.textContent = "Submitting analysis job…";
        }
        setReportMarkdown("");

        const { status, data } = await sendManualMessage({
            message,
            selectedEntries,
            mode: "chat",
            sessionId: state.lastManualSessionId || "",
            reset: false,
            attachments: [],
            async: useAsync
        });

        if (useAsync && (status === 202 || data?.status === "accepted")) {
            const jobId = data?.jobId || "";
            if (!jobId) {
                throw new Error("Job accepted but no jobId returned.");
            }

            state.activeJobId = jobId;
            state.jobPollStartedAt = Date.now();

            toggleCancelButton(true);
            showProgressBlock(true);

            if (statusEl) {
                statusEl.textContent = `Job accepted (${jobId.slice(0, 8)}…). Starting…`;
            }
            startJobPolling(jobId);
            return;
        }

        const responseText =
            data?.textResponse || data?.response || data?.message || data?.answer || data?.output || data?.raw || "No response returned.";
        setReportMarkdown(reorderReportSections(String(responseText)));
        if (statusEl) {
            statusEl.textContent = "Finished.";
        }
        setQuickPdfVisibility({ show: true, enabled: Boolean(String(responseText || "").trim()) });
    } catch (e) {
        const requestId = e?.data?.requestId || e?.requestId || "";
        const backendMessage = e?.data?.message || e?.data?.error || "";
        if (statusEl) {
            statusEl.textContent = `Analyze failed${e?.status ? ` (HTTP ${e.status})` : ""}${requestId ? ` [${requestId}]` : ""}${backendMessage ? `: ${backendMessage}` : "."}`;
        }
        setQuickPdfVisibility({ show: true, enabled: false });
        error("manual analyze failed", e);
    }
}

function startJobPolling(jobId) {
    stopJobPolling();

    const tick = async () => {
        try {
            const elapsed = Date.now() - state.jobPollStartedAt;
            if (elapsed > DEFAULTS.pollMaxMs) {
                stopJobPolling();
                setManualStatus("This took too long. Please try again.");
                toggleCancelButton(false);
                return;
            }

            const payload = await fetchJobStatus(jobId);
            applyJobStatusToUi(payload);

            if (payload?.job?.done) {
                stopJobPolling();
                toggleCancelButton(false);
            }
        } catch (e) {
            warn("job status poll failed", e);
        }
    };

    tick();
    state.jobPollTimer = window.setInterval(tick, DEFAULTS.pollMs);
}

function stopJobPolling() {
    if (state.jobPollTimer) {
        window.clearInterval(state.jobPollTimer);
        state.jobPollTimer = null;
    }
    state.activeJobId = "";
}

async function fetchJobStatus(jobId) {
    const url = `${DEFAULTS.jobStatusEndpoint}?jobId=${encodeURIComponent(jobId)}`;
    return getJson(url, { headers: { ...buildHeaders(), Accept: "application/json" } });
}

async function cancelJob(jobId) {
    const url = `${DEFAULTS.jobStatusEndpoint}?jobId=${encodeURIComponent(jobId)}`;
    return deleteJson(url, { headers: { ...buildHeaders(), Accept: "application/json" } });
}

function applyJobStatusToUi(payload) {
    const statusEl = byId("manualMessageStatus");
    const progressBar = byId("manualMessageProgressBar");
    const progressText = byId("manualMessageProgressText");
    const coverageText = byId("manualMessageCoverageText");
    const missingText = byId("manualMessageMissingIds");

    const phasePill = byId("manualMessagePhasePill");
    const activityText = byId("manualMessageActivityText");
    const batchText = byId("manualMessageBatchText");
    const runtimeText = byId("manualMessageRuntimeText");
    const lastUpdateText = byId("manualMessageLastUpdateText");

    const synthText = byId("manualMessageSynthesisText");
    const synthProgress = byId("manualMessageSynthesisProgress");

    const job = payload?.job || null;
    const progress = payload?.progress || null;
    const coverageObj = payload?.coverage || null;

    if (!job) {
        return;
    }

    showProgressBlock(true);

    const phase = String(progress?.phase || job.phase || "UNKNOWN");
    const done = Boolean(progress?.done ?? job.done);
    const success = Boolean(progress?.success ?? job.success);
    const activity = String(progress?.activity || progress?.message || job.activity || job.message || "").trim();

    const totalBatches = Number(progress?.totalBatches ?? job.totalBatches ?? 0);
    const completedBatches = Number(progress?.completedBatches ?? job.completedBatches ?? 0);
    const failedBatches = Number(progress?.failedBatches ?? job.failedBatches ?? 0);

    const allIds = normalizeIds(Array.isArray(coverageObj?.allSelectedChatIds) ? coverageObj.allSelectedChatIds : job.allSelectedChatIds);
    const usedIdsRaw = normalizeIds(Array.isArray(coverageObj?.usedChatIds) ? coverageObj.usedChatIds : job.usedChatIds);
    const missingIdsRaw = normalizeIds(Array.isArray(coverageObj?.missingChatIds) ? coverageObj.missingChatIds : job.missingChatIds);

    const totalProvided = Number(coverageObj?.totalSelected ?? job.totalSelected ?? 0);
    const total = allIds.length || totalProvided || 0;

    const missingIds = allIds.length ? intersectIds(allIds, missingIdsRaw) : missingIdsRaw;
    const usedIds = allIds.length ? subtractIds(allIds, missingIds) : usedIdsRaw;

    const missingCount = Number(coverageObj?.missingCount ?? missingIds.length);
    const usedCount = Number(coverageObj?.usedCount ?? (usedIds.length || Math.max(0, total - missingCount)));

    const derivedCoverage = Number(
        coverageObj?.coveragePercentDerived
        ?? job.coveragePercent
        ?? (total > 0 ? Math.round((usedCount * 100) / total) : 0)
    );
    const coverage = Number.isFinite(derivedCoverage) ? Math.max(0, Math.min(100, derivedCoverage)) : 0;
    const coverageComplete = Boolean(coverageObj?.coverageCompleteDerived ?? (missingCount === 0));

    const warnings = Array.isArray(progress?.warnings)
        ? progress.warnings.map((w) => String(w || "").toLowerCase())
        : Array.isArray(job.warnings)
            ? job.warnings.map((w) => String(w || "").toLowerCase())
            : [];
    const metadataMismatch = warnings.some((w) => w.includes("coverage metadata mismatch"));

    let progressPercent = Number(
        progress?.batchProgressPercent
        ?? job.batchProgressPercent
        ?? progress?.progressPercent
        ?? job.progressPercent
        ?? NaN
    );

    const synthParsed = parseSynthesisFromActivity(activity);
    if (synthParsed) {
        state.synth = { ...state.synth, ...synthParsed };
    }

    if (!Number.isFinite(progressPercent)) {
        progressPercent = deriveWeightedProgress({
            phase,
            done,
            completedBatches,
            totalBatches,
            synth: state.synth
        });
    } else if (phase.toUpperCase() === "REDUCE" && !done) {
        progressPercent = Math.max(progressPercent, deriveWeightedProgress({
            phase,
            done,
            completedBatches,
            totalBatches,
            synth: state.synth
        }));
    }

    progressPercent = Math.max(0, Math.min(100, Math.round(progressPercent)));
    if (progressBar) {
        progressBar.value = progressPercent;
    }

    const showBatchCounts = totalBatches > 0;
    const batchSegment = showBatchCounts
        ? ` • Batches: ${Math.max(0, completedBatches)}/${Math.max(0, totalBatches)}`
        : "";

    if (progressText) {
        const progressParts = [`Status: ${humanizePhase(phase)}`];
        if (batchSegment) {
            progressParts.push(batchSegment.replace(/^\s*•\s*/, ""));
        }
        progressParts.push(`Progress: ${progressPercent}%`);
        if (failedBatches > 0) {
            progressParts.push(`Failed: ${failedBatches}`);
        }
        if (activity) {
            progressParts.push(activity);
        }
        progressText.textContent = progressParts.join(" • ");
    }

    if (coverageText) {
        const coverageParts = [
            `Coverage: ${coverage}%${coverageComplete ? " (complete)" : " (in progress)"}`,
            `Used: ${usedCount}/${Math.max(0, total)}`
        ];
        if (metadataMismatch) {
            coverageParts.push("Coverage metadata mismatch detected");
        }
        coverageText.textContent = coverageParts.join(" • ");
    }

    if (missingText) {
        missingText.textContent = "";
        missingText.style.display = "none";
    }

    if (phasePill) {
        phasePill.textContent = humanizePhase(phase);
    }

    const fallbackActivity = showBatchCounts
        ? `Working on batch ${Math.max(1, completedBatches + (done ? 0 : 1))} of ${Math.max(1, totalBatches)}...`
        : "Preparing analysis...";
    if (activityText) {
        activityText.textContent = activity || fallbackActivity;
    }

    if (batchText) {
        batchText.textContent = showBatchCounts
            ? `${Math.max(0, completedBatches)}/${Math.max(0, totalBatches)} (${progressPercent}%)`
            : "—";
    }

    if (runtimeText) {
        runtimeText.textContent = formatDurationHms(Math.max(0, Date.now() - state.jobPollStartedAt));
    }
    if (lastUpdateText) {
        lastUpdateText.textContent = new Date().toLocaleTimeString();
    }

    if (synthText || synthProgress) {
        const p = String(phase || "").toUpperCase();
        if (done || p !== "REDUCE") {
            if (synthText) {
                synthText.textContent = "";
            }
            if (synthProgress) {
                synthProgress.textContent = "";
            }
        } else {
            if (synthText) {
                synthText.textContent = formatSynthesisLine(phase, state.synth) || "Finalizing report...";
            }
            if (synthProgress) {
                synthProgress.textContent = `${deriveSynthesisPercent(state.synth)}%`;
            }
        }
    }

    if (statusEl) {
        const tone = done
            ? (coverageComplete && success ? "Finished" : "Finished with issues")
            : "In progress";

        const statusParts = [`${tone} • ${humanizePhase(phase)}`];
        if (activity) {
            statusParts.push(activity);
        }
        statusParts.push(`Progress: ${progressPercent}%`);
        statusParts.push(`Coverage: ${coverage}%`);
        if (metadataMismatch) {
            statusParts.push("Coverage metadata mismatch");
        }
        statusEl.textContent = statusParts.join(" • ");
    }

    if (done) {
        const finalReport = job?.finalReport || job?.rawResponseBody || "";
        if (success && coverageComplete && !metadataMismatch) {
            setReportMarkdown(reorderReportSections(String(finalReport || "Analysis completed successfully.")));
            setQuickPdfVisibility({ show: true, enabled: Boolean(String(finalReport || "").trim()) });
            return;
        }

        const title = coverageComplete ? "## Finished with Issues" : "## Finished with Partial Coverage";
        const detailLines = [
            `${title}`,
            "",
            `- Coverage: **${coverage}%**`,
            `- Used: **${usedCount}/${Math.max(0, total)}**`
        ];
        if (showBatchCounts) {
            detailLines.push(`- Batches: **${Math.max(0, completedBatches)}/${Math.max(0, totalBatches)}**`);
        }
        if (metadataMismatch) {
            detailLines.push("- Warning: **Coverage metadata mismatch detected**");
        }
        if (job.errorMessage) {
            detailLines.push(`- Error: ${job.errorMessage}`);
        }
        if (finalReport) {
            detailLines.push("", "---", "", finalReport);
        }
        const detail = detailLines.join("\n");

        setReportMarkdown(reorderReportSections(detail));
        setQuickPdfVisibility({ show: true, enabled: true });
    }
}

function showProgressBlock(show) {
    const block = byId("manualMessageProgressBlock");
    if (block) {
        block.hidden = !show;
    }
}

function resetProgressUi() {
    const progressBar = byId("manualMessageProgressBar");
    const progressText = byId("manualMessageProgressText");
    const coverageText = byId("manualMessageCoverageText");
    const missingText = byId("manualMessageMissingIds");

    const phasePill = byId("manualMessagePhasePill");
    const activityText = byId("manualMessageActivityText");
    const batchText = byId("manualMessageBatchText");
    const runtimeText = byId("manualMessageRuntimeText");
    const lastUpdateText = byId("manualMessageLastUpdateText");

    const synthText = byId("manualMessageSynthesisText");
    const synthProgress = byId("manualMessageSynthesisProgress");

    if (progressBar) {
        progressBar.value = 0;
    }
    if (progressText) {
        progressText.textContent = "Waiting to start…";
    }
    if (coverageText) {
        coverageText.textContent = "";
    }
    if (missingText) {
        missingText.textContent = "";
        missingText.style.display = "none";
    }

    if (phasePill) {
        phasePill.textContent = "Waiting to start";
    }
    if (activityText) {
        activityText.textContent = "Waiting for first update…";
    }
    if (batchText) {
        batchText.textContent = "—";
    }
    if (runtimeText) {
        runtimeText.textContent = "00:00:00";
    }
    if (lastUpdateText) {
        lastUpdateText.textContent = "—";
    }
    if (synthText) {
        synthText.textContent = "";
    }
    if (synthProgress) {
        synthProgress.textContent = "";
    }

    state.synth = createInitialSynthState();
    showProgressBlock(false);
}

function toggleCancelButton(enabled) {
    const cancelBtn = byId("manualMessageCancelJobBtn");
    if (cancelBtn) {
        cancelBtn.disabled = !enabled;
    }
}

function setManualStatus(text) {
    setTextById("manualMessageStatus", text);
}

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
            if (xhr.readyState !== 4) {
                return;
            }

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

function applyFilterAndRender() {
    const q = (byId("reviewSearchInput")?.value || "").trim().toLowerCase();
    state.filteredRows = filterRows(state.rows, q);
    renderTable();
    if (state.manualSectionOpen) {
        updateManualSelectedCount();
    }
}

function renderTable() {
    const tbody = byId("widgetReviewBody");
    const pageInfo = byId("pageInfo");
    const selectAllVisible = byId("reviewSelectAll");

    if (!tbody) {
        return;
    }

    if (!state.filteredRows.length) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-row">No chats found.</td></tr>`;
        if (pageInfo) {
            pageInfo.textContent = "0 results";
        }
        if (selectAllVisible) {
            selectAllVisible.checked = false;
            selectAllVisible.indeterminate = false;
        }
        if (state.manualSectionOpen) {
            updateManualSelectedCount();
        }
        return;
    }

    const totalPages = Math.max(1, Math.ceil(state.filteredRows.length / state.pageSize));
    state.page = Math.min(Math.max(1, state.page), totalPages);

    const pageRows = getCurrentPageRows();

    tbody.innerHTML = pageRows.map((r) => {
        const key = rowKey(r);
        const checked = state.selectedIds.has(key) ? "checked" : "";
        const activeClass = state.activeDetailKey === key ? "row-active" : "";
        return `
      <tr class="${activeClass}" data-row-key="${escapeHtml(key)}">
        <td class="select-column">
          <input type="checkbox" class="row-select" data-key="${escapeHtml(key)}" ${checked}>
        </td>
        <td class="row-open-cell">${escapeHtml(r.chatId || "")}</td>
        <td class="row-open-cell" title="${escapeHtml(r.prompt || "")}">${escapeHtml((r.prompt || "").slice(0, 220))}</td>
        <td class="row-open-cell">${escapeHtml(r.createdAt || "")}</td>
        <td class="row-open-cell">${escapeHtml(r.sessionIdDisplay || r.sessionId || "")}</td>
      </tr>
    `;
    }).join("");

    tbody.querySelectorAll("input.row-select").forEach((cb) => {
        cb.addEventListener("click", (ev) => ev.stopPropagation());
        cb.addEventListener("change", () => {
            const key = cb.getAttribute("data-key") || "";
            if (!key) {
                return;
            }
            if (cb.checked) {
                state.selectedIds.add(key);
            } else {
                state.selectedIds.delete(key);
            }
            syncVisibleSelectAll();
            if (state.manualSectionOpen) {
                updateManualSelectedCount();
            }
        });
    });

    tbody.onclick = (ev) => {
        const target = ev.target;
        if (!(target instanceof HTMLElement)) {
            return;
        }
        if (target.closest("input.row-select")) {
            return;
        }

        const tr = target.closest("tr[data-row-key]");
        if (!tr) {
            return;
        }

        const key = tr.getAttribute("data-row-key") || "";
        if (!key) {
            return;
        }

        const row = state.rows.find((x) => rowKey(x) === key);
        if (!row) {
            return;
        }

        openDetailCard(row);
        renderTable();
    };

    if (pageInfo) {
        pageInfo.textContent = `Page ${state.page} of ${totalPages} • ${state.filteredRows.length} result(s)`;
    }

    syncVisibleSelectAll();
    if (state.manualSectionOpen) {
        updateManualSelectedCount();
    }
}

function openDetailCard(row) {
    const card = byId("detailCard");
    const title = byId("detailTitle");
    const prompt = byId("detailPrompt");
    const response = byId("detailResponse");

    if (!card || !title || !prompt || !response) {
        return;
    }

    state.activeDetailKey = rowKey(row);

    title.textContent = `Selected Chat Details${row.chatId ? ` • Chat ${row.chatId}` : ""}`;
    prompt.textContent = row.prompt || "(empty prompt)";
    response.textContent = row.response || "(empty response)";

    clearTranslationUi();

    card.style.display = "block";
    card.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function getActiveDetailRow() {
    if (!state.activeDetailKey) {
        return null;
    }
    return state.rows.find((r) => rowKey(r) === state.activeDetailKey) || null;
}

function clearTranslationUi() {
    setTextById("promptTranslationMeta", "");
    setTextById("responseTranslationMeta", "");
    setBlockTextById("promptTranslationOutput", "", false);
    setBlockTextById("responseTranslationOutput", "", false);
}

async function translateText(sourceText, targetLang, metaId, outId, label) {
    const meta = byId(metaId);
    const out = byId(outId);

    if (!sourceText || !sourceText.trim()) {
        if (meta) {
            meta.textContent = `${label} is empty; nothing to translate.`;
        }
        if (out) { out.textContent = ""; out.style.display = "none"; }
        return;
    }

    try {
        if (meta) {
            meta.textContent = "Translating…";
        }
        if (out) { out.textContent = ""; out.style.display = "none"; }

        const res = await fetch(DEFAULTS.translateEndpoint, {
            method: "POST",
            credentials: "same-origin",
            headers: {
                ...buildHeaders(),
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify({ text: sourceText, targetLang: targetLang || "en" })
        });

        const text = await res.text();
        let data = {};
        try { data = text ? JSON.parse(text) : {}; } catch { data = {}; }

        if (!res.ok || data.status !== "ok") {
            throw new Error(data.message || `Translate failed (HTTP ${res.status})`);
        }

        const translated = data.translatedText || "";
        const src = data.sourceLang || "auto";
        const dst = data.targetLang || (targetLang || "en");

        if (meta) {
            meta.textContent = `${label}: ${src} → ${dst}`;
        }
        if (out) {
            out.textContent = translated || "(empty translation)";
            out.style.display = "block";
        }
    } catch (e) {
        warn("translation failed", e);
        if (meta) {
            meta.textContent = `Translation failed: ${e?.message || "Unknown error"}`;
        }
        if (out) { out.textContent = ""; out.style.display = "none"; }
    }
}

function syncVisibleSelectAll() {
    const selectAllVisible = byId("reviewSelectAll");
    if (!selectAllVisible) {
        return;
    }

    const pageRows = getCurrentPageRows();
    if (!pageRows.length) {
        selectAllVisible.checked = false;
        selectAllVisible.indeterminate = false;
        return;
    }

    let selectedCount = 0;
    for (const r of pageRows) {
        if (state.selectedIds.has(rowKey(r))) {
            selectedCount++;
        }
    }

    selectAllVisible.checked = selectedCount === pageRows.length;
    selectAllVisible.indeterminate = selectedCount > 0 && selectedCount < pageRows.length;
}

function getCurrentPageRows() {
    return getPageRows(state.filteredRows, state.page, state.pageSize);
}

function getSelectedEntries() {
    return getSelectedEntriesFromState(state.rows, state.selectedIds);
}

function updateManualSelectedCount() {
    const el = byId("manualMessageSelectedCount");
    if (!el) {
        return;
    }
    el.textContent = `Selected chats: ${getSelectedEntries().length}`;
}

function renderLoadingRow(text) {
    const tbody = byId("widgetReviewBody");
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-row">${escapeHtml(text)}</td></tr>`;
    }
}

function renderErrorRow(text) {
    const tbody = byId("widgetReviewBody");
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-row">${escapeHtml(text)}</td></tr>`;
    }
}

function humanizeLoadError(e) {
    if (e instanceof TypeError) {
        return "Network/CSP blocked the data request. Check browser console + server/proxy logs.";
    }
    if (typeof e?.status === "number") {
        return `Failed to load selected chats (HTTP ${e.status}).`;
    }
    return "Failed to load selected chats.";
}

window.widgetReview = {
    sendManualMessage,
    analyzeInBatches,
    exportSelected,
    showLoading,
    showError,
    showMarkdown,
    showStatus,
    getSelectedEntries
};

document.addEventListener("DOMContentLoaded", initPage);
