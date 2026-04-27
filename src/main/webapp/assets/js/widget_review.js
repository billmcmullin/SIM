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
    jobStatusEndpoint: `${CONTEXT_PATH}/dashboard/drilldown/widget-review/job-status`,
    batchAnalyzeEndpoint: `${CONTEXT_PATH}/dashboard/drilldown/widget-review/batch-analyze`,
    translateEndpoint: CFG.translateEndpoint || `${CONTEXT_PATH}/dashboard/widgets/drilldown/review/translate`,
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
    synth: {
        level: 0,
        chunk: 0,
        totalChunks: 0,
        finalAttempt: 0,
        finalAttemptTotal: 4
    }
};

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
            selectedEntries: cleanEntries
        });

        payload.async = !!async;

        info("manual message payload prepared", {
            selected: cleanEntries.length,
            estimatedTokens: estimateTokens(message || ""),
            endpoint: DEFAULTS.manualMessageEndpoint,
            async: !!async
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
        return { status: res.status, data };
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

export function showLoading(el, text = "Loading...") { renderLoading(el, text); }
export function showError(el, message, requestId = "") { renderError(el, message, requestId); }
export function showMarkdown(el, md) { renderMarkdown(el, md); }
export function showStatus(el, text, tone = "neutral") { renderStatusPill(el, text, tone); }

async function initPage() {
    const tbody = document.getElementById("widgetReviewBody");
    if (!tbody) return;

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
        state.rows = normalizeIncomingRows(raw);
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

        info("review rows loaded", { count: state.rows.length, selectionId: SELECTION_ID });
    } catch (e) {
        error("failed loading selected chats", e);
        renderErrorRow(humanizeLoadError(e));
    }
}

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

    if (prevBtn) prevBtn.addEventListener("click", () => {
        state.page = Math.max(1, state.page - 1);
        renderTable();
    });

    if (nextBtn) nextBtn.addEventListener("click", () => {
        const totalPages = Math.max(1, Math.ceil(state.filteredRows.length / state.pageSize));
        state.page = Math.min(totalPages, state.page + 1);
        renderTable();
    });

    if (selectAllVisible) {
        selectAllVisible.addEventListener("change", () => {
            const checked = !!selectAllVisible.checked;
            for (const r of getCurrentPageRows()) {
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
    const cancelBtn = document.getElementById("manualMessageCancelJobBtn");

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

    if (closeBtn) closeBtn.addEventListener("click", closeSection);

    if (clearBtn) {
        clearBtn.addEventListener("click", () => {
            const text = document.getElementById("manualMessageText");
            const status = document.getElementById("manualMessageStatus");
            const preview = document.getElementById("manualMessageSelectionPreview");
            const response = document.getElementById("manualMessageResponse");
            resetProgressUi();

            if (text) text.value = "";
            if (status) status.textContent = "";
            if (preview) preview.value = "No response yet.";
            if (response) response.textContent = "No response yet.";

            stopJobPolling();
            state.lastManualSessionId = "";
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener("click", async () => {
            if (!state.activeJobId) return;
            try {
                await cancelJob(state.activeJobId);
                setManualStatus("Job cancellation requested.");
            } catch (e) {
                warn("cancel failed", e);
                setManualStatus("Failed to cancel job.");
            }
        });
    }

    if (sendBtn) sendBtn.addEventListener("click", onManualMessageSend);
}

function wireDetailCardUi() {
    const promptBtn = document.getElementById("translatePromptBtn");
    const responseBtn = document.getElementById("translateResponseBtn");
    const langSel = document.getElementById("translateTargetLang");

    if (promptBtn) {
        promptBtn.addEventListener("click", async () => {
            const row = getActiveDetailRow();
            if (!row) return;
            await translateText(row.prompt || "", langSel?.value || "en", "promptTranslationMeta", "promptTranslationOutput", "Prompt");
        });
    }

    if (responseBtn) {
        responseBtn.addEventListener("click", async () => {
            const row = getActiveDetailRow();
            if (!row) return;
            await translateText(row.response || "", langSel?.value || "en", "responseTranslationMeta", "responseTranslationOutput", "Response");
        });
    }
}

async function onManualMessageSend() {
    const textEl = document.getElementById("manualMessageText");
    const statusEl = document.getElementById("manualMessageStatus");
    const responseEl = document.getElementById("manualMessageResponse");
    const asyncEl = document.getElementById("manualMessageAsyncMode");

    const message = (textEl?.value || "").trim();
    const selectedEntries = getSelectedEntries();
    const useAsync = asyncEl ? !!asyncEl.checked : true;

    if (!message) {
        if (statusEl) statusEl.textContent = "Enter a message first.";
        return;
    }
    if (!selectedEntries.length) {
        if (statusEl) statusEl.textContent = "Select at least one chat entry.";
        return;
    }

    try {
        stopJobPolling();
        resetProgressUi();

        if (statusEl) statusEl.textContent = useAsync ? "Submitting async review job…" : "Sending…";
        if (responseEl) responseEl.textContent = useAsync ? "Job accepted. Waiting for progress…" : "Loading response…";

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
            if (!jobId) throw new Error("Job accepted but no jobId returned.");

            state.activeJobId = jobId;
            state.jobPollStartedAt = Date.now();

            toggleCancelButton(true);
            showProgressBlock(true);

            if (statusEl) statusEl.textContent = `Job accepted (${jobId.slice(0, 8)}…). Starting analysis…`;
            if (responseEl) responseEl.textContent = "Starting map/reduce pipeline…";

            startJobPolling(jobId);
            return;
        }

        const responseText =
            data?.textResponse || data?.response || data?.message || data?.answer || data?.output || data?.raw || "No response returned.";
        if (responseEl) renderMarkdown(responseEl, String(responseText));
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

function startJobPolling(jobId) {
    stopJobPolling();

    const tick = async () => {
        try {
            const elapsed = Date.now() - state.jobPollStartedAt;
            if (elapsed > DEFAULTS.pollMaxMs) {
                stopJobPolling();
                setManualStatus("Job timed out while polling.");
                toggleCancelButton(false);
                return;
            }

            const payload = await fetchJobStatus(jobId);
            applyJobStatusToUi(payload);

            const done = !!(payload?.job?.done);
            if (done) {
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
    const res = await fetch(url, {
        method: "GET",
        credentials: "same-origin",
        headers: { ...buildHeaders(), "Accept": "application/json" }
    });

    const text = await res.text();
    let data = {};
    try { data = text ? JSON.parse(text) : {}; } catch { data = {}; }

    if (!res.ok) {
        const err = new Error(`Job status failed: HTTP ${res.status}`);
        err.status = res.status;
        err.data = data;
        throw err;
    }
    return data || {};
}

async function cancelJob(jobId) {
    const url = `${DEFAULTS.jobStatusEndpoint}?jobId=${encodeURIComponent(jobId)}`;
    const res = await fetch(url, {
        method: "DELETE",
        credentials: "same-origin",
        headers: { ...buildHeaders(), "Accept": "application/json" }
    });

    const text = await res.text();
    let data = {};
    try { data = text ? JSON.parse(text) : {}; } catch { data = {}; }

    if (!res.ok) {
        const err = new Error(`Cancel failed: HTTP ${res.status}`);
        err.status = res.status;
        err.data = data;
        throw err;
    }
    return data;
}

function applyJobStatusToUi(payload) {
    const statusEl = document.getElementById("manualMessageStatus");
    const responseEl = document.getElementById("manualMessageResponse");
    const progressBar = document.getElementById("manualMessageProgressBar");
    const progressText = document.getElementById("manualMessageProgressText");
    const coverageText = document.getElementById("manualMessageCoverageText");
    const missingText = document.getElementById("manualMessageMissingIds");

    const phasePill = document.getElementById("manualMessagePhasePill");
    const activityText = document.getElementById("manualMessageActivityText");
    const batchText = document.getElementById("manualMessageBatchText");
    const runtimeText = document.getElementById("manualMessageRuntimeText");
    const lastUpdateText = document.getElementById("manualMessageLastUpdateText");

    const synthText = document.getElementById("manualMessageSynthesisText");
    const synthProgress = document.getElementById("manualMessageSynthesisProgress");

    const job = payload?.job || null;
    const progress = payload?.progress || null;
    const coverageObj = payload?.coverage || null;

    if (!job) return;

    showProgressBlock(true);

    const phase = String(progress?.phase || job.phase || "UNKNOWN");
    const done = !!(progress?.done ?? job.done);
    const success = !!(progress?.success ?? job.success);
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
    const coverageComplete = !!(coverageObj?.coverageCompleteDerived ?? (missingCount === 0));

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
        state.synth = {
            ...state.synth,
            ...synthParsed
        };
    }

    if (!Number.isFinite(progressPercent)) {
        progressPercent = deriveWeightedProgress({
            phase,
            done,
            completedBatches,
            totalBatches,
            synth: state.synth
        });
    } else {
        if (phase.toUpperCase() === "REDUCE" && !done) {
            progressPercent = Math.max(progressPercent, deriveWeightedProgress({
                phase,
                done,
                completedBatches,
                totalBatches,
                synth: state.synth
            }));
        }
    }

    progressPercent = Math.max(0, Math.min(100, Math.round(progressPercent)));

    if (progressBar) progressBar.value = progressPercent;

    const showBatchCounts = totalBatches > 0;
    const batchSegment = showBatchCounts
        ? ` • Batches: ${Math.max(0, completedBatches)}/${Math.max(0, totalBatches)}`
        : "";

    if (progressText) {
        progressText.textContent =
            `Phase: ${humanizePhase(phase)}`
            + batchSegment
            + ` • Progress: ${progressPercent}%`
            + (failedBatches > 0 ? ` • Failed: ${failedBatches}` : "")
            + (activity ? ` • ${activity}` : "");
    }

    if (coverageText) {
        coverageText.textContent =
            `Coverage: ${coverage}%${coverageComplete ? " (complete)" : " (in progress)"}`
            + ` • Used: ${usedCount}/${Math.max(0, total)}`
            + (metadataMismatch ? " • Metadata mismatch detected" : "");
    }

    if (missingText) {
        missingText.textContent = "";
        missingText.style.display = "none";
    }

    if (phasePill) phasePill.textContent = humanizePhase(phase);

    const fallbackActivity = showBatchCounts
        ? `Processing batch ${Math.max(1, completedBatches + (done ? 0 : 1))} of ${Math.max(1, totalBatches)}...`
        : "Preparing analysis...";
    if (activityText) activityText.textContent = activity || fallbackActivity;

    if (batchText) {
        batchText.textContent = showBatchCounts
            ? `${Math.max(0, completedBatches)}/${Math.max(0, totalBatches)} (${progressPercent}%)`
            : "—";
    }

    if (runtimeText) {
        const runtimeMs = Math.max(0, Date.now() - state.jobPollStartedAt);
        runtimeText.textContent = formatDurationHms(runtimeMs);
    }

    if (lastUpdateText) {
        lastUpdateText.textContent = new Date().toLocaleTimeString();
    }

    updateSynthesisUi({
        phase,
        done,
        synth: state.synth,
        synthTextEl: synthText,
        synthProgressEl: synthProgress
    });

    if (statusEl) {
        const tone = done
            ? (coverageComplete && success ? "Completed" : "Completed with issues")
            : "Running";

        statusEl.textContent =
            `${tone} • ${humanizePhase(phase)}`
            + (activity ? ` • ${activity}` : "")
            + ` • Progress: ${progressPercent}%`
            + ` • Coverage: ${coverage}%`
            + (metadataMismatch ? " • Coverage metadata mismatch" : "");
    }

    if (!done && responseEl) {
        const synthLine = formatSynthesisLine(phase, state.synth);
        responseEl.textContent =
            `Running analysis...\n\n`
            + `Phase: ${humanizePhase(phase)}\n`
            + `Progress: ${progressPercent}%\n`
            + `Current step: ${activity || fallbackActivity}\n`
            + (showBatchCounts ? `Batches: ${Math.max(0, completedBatches)}/${Math.max(0, totalBatches)}\n` : "")
            + (synthLine ? `Synthesis: ${synthLine}\n` : "")
            + `Runtime: ${formatDurationHms(Math.max(0, Date.now() - state.jobPollStartedAt))}\n`
            + `Coverage: ${coverage}% (${coverageComplete ? "complete" : "in progress"})\n`
            + `Used chats: ${usedCount}/${Math.max(0, total)}`
            + (metadataMismatch ? `\nWarning: Coverage metadata mismatch detected.` : "");
    }

    if (done && responseEl) {
        const finalReport = job?.finalReport || job?.rawResponseBody || "";
        if (success && coverageComplete && !metadataMismatch) {
            renderMarkdown(responseEl, String(finalReport || "Job completed successfully."));
            return;
        }

        const title = coverageComplete ? "## Completed with Errors" : "## Completed with Partial Coverage";
        const detail =
            `${title}\n\n`
            + `- Coverage: **${coverage}%**\n`
            + `- Used: **${usedCount}/${Math.max(0, total)}**\n`
            + (showBatchCounts ? `- Batches: **${Math.max(0, completedBatches)}/${Math.max(0, totalBatches)}**\n` : "")
            + (metadataMismatch ? `- Warning: **Coverage metadata mismatch detected**\n` : "")
            + (job.errorMessage ? `- Error: ${job.errorMessage}\n` : "")
            + (finalReport ? `\n---\n\n${finalReport}` : "");

        renderMarkdown(responseEl, detail);
    }
}

function deriveWeightedProgress({ phase, done, completedBatches, totalBatches, synth }) {
    if (done) return 100;
    const p = String(phase || "").toUpperCase();

    if (p === "QUEUED") return 3;
    if (p === "MAP") {
        if (totalBatches > 0) {
            const ratio = Math.max(0, Math.min(1, completedBatches / Math.max(1, totalBatches)));
            return 5 + Math.round(ratio * 75);
        }
        return 15;
    }
    if (p === "REDUCE") {
        let reduceRatio = 0.1;

        if (synth?.finalAttempt > 0 && synth?.finalAttemptTotal > 0) {
            reduceRatio = Math.max(reduceRatio, Math.min(1, synth.finalAttempt / synth.finalAttemptTotal));
        } else if (synth?.level > 0 && synth?.totalChunks > 0) {
            const chunkRatio = Math.max(0, Math.min(1, synth.chunk / Math.max(1, synth.totalChunks)));
            const lvlFactor = Math.min(0.95, 0.2 + (synth.level * 0.15));
            reduceRatio = Math.max(reduceRatio, Math.min(1, lvlFactor * 0.7 + chunkRatio * 0.3));
        }

        return 80 + Math.round(reduceRatio * 19);
    }

    return 10;
}

function parseSynthesisFromActivity(activity) {
    if (!activity) return null;
    const txt = String(activity).toLowerCase();

    // "Synthesis L2 • chunk 3/7 ..."
    const levelChunk = txt.match(/synthesis\s+l(\d+).*chunk\s+(\d+)\s*\/\s*(\d+)/i);
    if (levelChunk) {
        return {
            level: parseInt(levelChunk[1], 10) || 0,
            chunk: parseInt(levelChunk[2], 10) || 0,
            totalChunks: parseInt(levelChunk[3], 10) || 0
        };
    }

    // "Synthesis level 2 complete..."
    const levelOnly = txt.match(/synthesis\s+level\s+(\d+)/i);
    if (levelOnly) {
        return { level: parseInt(levelOnly[1], 10) || 0 };
    }

    // "Final synthesis attempt 2/4 ..."
    const finalAttempt = txt.match(/final\s+synthesis\s+attempt\s+(\d+)\s*\/\s*(\d+)/i);
    if (finalAttempt) {
        return {
            finalAttempt: parseInt(finalAttempt[1], 10) || 0,
            finalAttemptTotal: parseInt(finalAttempt[2], 10) || 0
        };
    }

    return null;
}

function updateSynthesisUi({ phase, done, synth, synthTextEl, synthProgressEl }) {
    if (!synthTextEl && !synthProgressEl) return;

    const p = String(phase || "").toUpperCase();
    if (done || p !== "REDUCE") {
        if (synthTextEl) synthTextEl.textContent = "";
        if (synthProgressEl) synthProgressEl.textContent = "";
        return;
    }

    const line = formatSynthesisLine(phase, synth);
    if (synthTextEl) synthTextEl.textContent = line || "Synthesizing final report...";

    if (synthProgressEl) {
        const pct = deriveSynthesisPercent(synth);
        synthProgressEl.textContent = `${pct}%`;
    }
}

function deriveSynthesisPercent(synth) {
    if (synth?.finalAttempt > 0 && synth?.finalAttemptTotal > 0) {
        return Math.max(1, Math.min(99, Math.round((synth.finalAttempt / synth.finalAttemptTotal) * 100)));
    }
    if (synth?.chunk > 0 && synth?.totalChunks > 0) {
        return Math.max(1, Math.min(99, Math.round((synth.chunk / synth.totalChunks) * 100)));
    }
    return 10;
}

function formatSynthesisLine(phase, synth) {
    const p = String(phase || "").toUpperCase();
    if (p !== "REDUCE") return "";

    if (synth?.finalAttempt > 0 && synth?.finalAttemptTotal > 0) {
        return `Final attempt ${synth.finalAttempt}/${synth.finalAttemptTotal}`;
    }

    if (synth?.level > 0 && synth?.chunk > 0 && synth?.totalChunks > 0) {
        return `Level ${synth.level} • Chunk ${synth.chunk}/${synth.totalChunks}`;
    }

    if (synth?.level > 0) {
        return `Level ${synth.level}`;
    }

    return "Synthesizing final report...";
}

function humanizePhase(phase) {
    const p = String(phase || "").toUpperCase();
    switch (p) {
        case "QUEUED": return "Queued";
        case "MAP": return "Map analysis";
        case "REDUCE": return "Reduce synthesis";
        case "COMPLETED": return "Completed";
        case "FAILED": return "Failed";
        case "CANCELLED": return "Cancelled";
        default: return p || "Unknown";
    }
}

function showProgressBlock(show) {
    const block = document.getElementById("manualMessageProgressBlock");
    if (block) block.hidden = !show;
}

function resetProgressUi() {
    const progressBar = document.getElementById("manualMessageProgressBar");
    const progressText = document.getElementById("manualMessageProgressText");
    const coverageText = document.getElementById("manualMessageCoverageText");
    const missingText = document.getElementById("manualMessageMissingIds");

    const phasePill = document.getElementById("manualMessagePhasePill");
    const activityText = document.getElementById("manualMessageActivityText");
    const batchText = document.getElementById("manualMessageBatchText");
    const runtimeText = document.getElementById("manualMessageRuntimeText");
    const lastUpdateText = document.getElementById("manualMessageLastUpdateText");

    const synthText = document.getElementById("manualMessageSynthesisText");
    const synthProgress = document.getElementById("manualMessageSynthesisProgress");

    if (progressBar) progressBar.value = 0;
    if (progressText) progressText.textContent = "Waiting to start…";
    if (coverageText) coverageText.textContent = "";
    if (missingText) {
        missingText.textContent = "";
        missingText.style.display = "none";
    }

    if (phasePill) phasePill.textContent = "Queued";
    if (activityText) activityText.textContent = "Waiting for first update…";
    if (batchText) batchText.textContent = "—";
    if (runtimeText) runtimeText.textContent = "00:00:00";
    if (lastUpdateText) lastUpdateText.textContent = "—";
    if (synthText) synthText.textContent = "";
    if (synthProgress) synthProgress.textContent = "";

    state.synth = {
        level: 0,
        chunk: 0,
        totalChunks: 0,
        finalAttempt: 0,
        finalAttemptTotal: 4
    };

    showProgressBlock(false);
}

function toggleCancelButton(enabled) {
    const cancelBtn = document.getElementById("manualMessageCancelJobBtn");
    if (cancelBtn) cancelBtn.disabled = !enabled;
}

function setManualStatus(text) {
    const statusEl = document.getElementById("manualMessageStatus");
    if (statusEl) statusEl.textContent = text;
}

function formatDurationHms(ms) {
    const totalSec = Math.max(0, Math.floor(ms / 1000));
    const hh = Math.floor(totalSec / 3600);
    const mm = Math.floor((totalSec % 3600) / 60);
    const ss = totalSec % 60;
    return `${String(hh).padStart(2, "0")}:${String(mm).padStart(2, "0")}:${String(ss).padStart(2, "0")}`;
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
            if (!key) return;
            if (cb.checked) state.selectedIds.add(key);
            else state.selectedIds.delete(key);
            syncVisibleSelectAll();
            if (state.manualSectionOpen) updateManualSelectionPreview();
        });
    });

    tbody.onclick = (ev) => {
        const target = ev.target;
        if (!(target instanceof HTMLElement)) return;
        if (target.closest("input.row-select")) return;

        const tr = target.closest("tr[data-row-key]");
        if (!tr) return;

        const key = tr.getAttribute("data-row-key") || "";
        if (!key) return;

        const row = state.rows.find((x) => rowKey(x) === key);
        if (!row) return;

        openDetailCard(row);
        renderTable();
    };

    if (pageInfo) {
        pageInfo.textContent = `Page ${state.page} of ${totalPages} • ${state.filteredRows.length} result(s)`;
    }

    syncVisibleSelectAll();
    if (state.manualSectionOpen) updateManualSelectionPreview();
}

function openDetailCard(row) {
    const card = document.getElementById("detailCard");
    const title = document.getElementById("detailTitle");
    const prompt = document.getElementById("detailPrompt");
    const response = document.getElementById("detailResponse");

    if (!card || !title || !prompt || !response) return;

    state.activeDetailKey = rowKey(row);

    title.textContent = `Selected Chat Details${row.chatId ? ` • Chat ${row.chatId}` : ""}`;
    prompt.textContent = row.prompt || "(empty prompt)";
    response.textContent = row.response || "(empty response)";

    clearTranslationUi();

    card.style.display = "block";
    card.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function getActiveDetailRow() {
    if (!state.activeDetailKey) return null;
    return state.rows.find((r) => rowKey(r) === state.activeDetailKey) || null;
}

function clearTranslationUi() {
    setText("promptTranslationMeta", "");
    setText("responseTranslationMeta", "");
    setPreText("promptTranslationOutput", "", false);
    setPreText("responseTranslationOutput", "", false);
}

async function translateText(sourceText, targetLang, metaId, outId, label) {
    const meta = document.getElementById(metaId);
    const out = document.getElementById(outId);

    if (!sourceText || !sourceText.trim()) {
        if (meta) meta.textContent = `${label} is empty; nothing to translate.`;
        if (out) { out.textContent = ""; out.style.display = "none"; }
        return;
    }

    try {
        if (meta) meta.textContent = "Translating…";
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

        if (meta) meta.textContent = `${label}: ${src} → ${dst}`;
        if (out) {
            out.textContent = translated || "(empty translation)";
            out.style.display = "block";
        }
    } catch (e) {
        warn("translation failed", e);
        if (meta) meta.textContent = `Translation failed: ${e?.message || "Unknown error"}`;
        if (out) { out.textContent = ""; out.style.display = "none"; }
    }
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value || "";
}

function setPreText(id, value, show) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = value || "";
    el.style.display = show ? "block" : "none";
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
    return state.rows.filter((r) => state.selectedIds.has(rowKey(r)));
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
    if (selected.length > 200) lines.push(`\n... and ${selected.length - 200} more selected entries.`);
    preview.value = lines.join("\n");
}

function rowKey(r) {
    return r.chatId || `${r.createdAt || ""}|${(r.prompt || "").slice(0, 24)}|${r.sessionId || ""}`;
}

function renderLoadingRow(text) {
    const tbody = document.getElementById("widgetReviewBody");
    if (tbody) tbody.innerHTML = `<tr><td colspan="5" class="empty-row">${escapeHtml(text)}</td></tr>`;
}

function renderErrorRow(text) {
    const tbody = document.getElementById("widgetReviewBody");
    if (tbody) tbody.innerHTML = `<tr><td colspan="5" class="empty-row">${escapeHtml(text)}</td></tr>`;
}

function humanizeLoadError(e) {
    if (e instanceof TypeError) return "Network/CSP blocked the data request. Check browser console + server/proxy logs.";
    if (typeof e?.status === "number") return `Failed to load selected chats (HTTP ${e.status}).`;
    return "Failed to load selected chats.";
}

function normalizeIds(ids) {
    const out = [];
    const seen = new Set();
    for (const id of Array.isArray(ids) ? ids : []) {
        const n = String(id ?? "").trim().toLowerCase();
        if (!n) continue;
        if (seen.has(n)) continue;
        seen.add(n);
        out.push(n);
    }
    return out;
}

function subtractIds(all, used) {
    const a = new Set(normalizeIds(all));
    const u = new Set(normalizeIds(used));
    for (const id of u) a.delete(id);
    return Array.from(a);
}

function intersectIds(a, b) {
    const sa = new Set(normalizeIds(a));
    const sb = new Set(normalizeIds(b));
    const out = [];
    for (const id of sa) {
        if (sb.has(id)) out.push(id);
    }
    return out;
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
