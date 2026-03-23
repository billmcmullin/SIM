const config = window.widgetReviewConfig || {};
const contextPath = config.contextPath || '';
const selectionId = config.selectionId || '';

const reviewBody = document.getElementById('widgetReviewBody');
const searchTermsDisplay = document.getElementById('searchTermsDisplay');
const detailCard = document.getElementById('detailCard');
const detailPrompt = document.getElementById('detailPrompt');
const detailResponse = document.getElementById('detailResponse');
const detailTitle = document.getElementById('detailTitle');
const reviewSearchInput = document.getElementById('reviewSearchInput');
const reviewPageSizeSelect = document.getElementById('reviewPageSize');
const prevBtn = document.getElementById('prevPageBtn');
const nextBtn = document.getElementById('nextPageBtn');
const pageInfo = document.getElementById('pageInfo');
const tableHeaders = document.querySelectorAll('.widget-review-table th[data-column]');
const selectColumnHeader = document.querySelector('.widget-review-table th.select-column');
const selectAllCheckbox = document.getElementById('reviewSelectAll');
const selectAllEntriesBtn = document.getElementById('selectAllEntriesBtn');
const deselectAllBtn = document.getElementById('deselectAllBtn');
const manualMessageToggleBtn = document.getElementById('manualMessageToggleBtn');
const manualMessageSection = document.getElementById('manualMessageSection');
const manualMessageTextarea = document.getElementById('manualMessageText');
const manualMessageSendBtn = document.getElementById('manualMessageSendBtn');
const manualMessageClearBtn = document.getElementById('manualMessageClearBtn');
const manualMessageCloseBtn = document.getElementById('manualMessageCloseBtn');
const manualMessageStatus = document.getElementById('manualMessageStatus');
const manualMessageResponse = document.getElementById('manualMessageResponse');
const manualMessageSelectionPreview = document.getElementById('manualMessageSelectionPreview');
const sessionIndicator = document.getElementById('sessionIndicator');
const sessionIndicatorValue = document.getElementById('sessionIndicatorValue');

const exportSelectedBtn = document.getElementById('exportSelectedBtn');
const exportFormatSelect = document.getElementById('exportFormat');

const state = {
    limit: 10,
    page: 1,
    totalPages: 1,
    totalRows: 0,
    sortColumn: 'created_at',
    sortDir: 'DESC',
    search: ''
};

let rows = [];
let debounceTimer;
const multiSelected = new Set();
const selectedEntryDetails = new Map();
let selectionPreviewText = 'No chat selected.';
let selectionSummaryText = '';

const MAX_SUMMARY_CHARS = 12000;
const MAX_TOTAL_MESSAGE_CHARS = 16000;
const BULK_FETCH_PAGE_SIZE = 500;

const urlSessionId = new URLSearchParams(window.location.search).get('sessionId');
if (urlSessionId) showSessionIndicator(urlSessionId);

document.addEventListener('DOMContentLoaded', () => {
    if (!selectionId) {
        showError('Missing selection reference.');
        return;
    }
    if (selectColumnHeader) selectColumnHeader.textContent = '';

    reviewPageSizeSelect?.addEventListener('change', () => {
        const val = parseInt(reviewPageSizeSelect.value, 10);
        if (Number.isNaN(val)) return;
        state.limit = val;
        state.page = 1;
        loadSelectionData();
    });

    attachHandlers();
    attachManualMessageHandlers();
    hideManualMessageSection();
    loadSelectionData();
});

function formatSessionDisplay(row) {
    if (!row) return '';
    if (row.sessionIdDisplay) return row.sessionIdDisplay;
    if (row.displayLabel) return row.displayLabel;
    return row.sessionId || '';
}

function showSessionIndicator(value) {
    if (!sessionIndicator || !sessionIndicatorValue) return;
    sessionIndicatorValue.textContent = value;
    sessionIndicator.removeAttribute('hidden');
}

function attachHandlers() {
    reviewSearchInput?.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            state.search = reviewSearchInput.value.trim();
            state.page = 1;
            loadSelectionData();
        }, 300);
    });

    prevBtn?.addEventListener('click', () => {
        if (state.page <= 1) return;
        state.page -= 1;
        loadSelectionData();
    });

    nextBtn?.addEventListener('click', () => {
        if (state.page >= state.totalPages) return;
        state.page += 1;
        loadSelectionData();
    });

    tableHeaders.forEach(header => {
        header.addEventListener('click', () => {
            const column = header.dataset.column;
            if (!column) return;
            if (state.sortColumn === column) {
                state.sortDir = state.sortDir === 'DESC' ? 'ASC' : 'DESC';
            } else {
                state.sortColumn = column;
                state.sortDir = 'DESC';
            }
            tableHeaders.forEach(h => h.classList.remove('sorted-asc', 'sorted-desc'));
            header.classList.add(state.sortDir === 'ASC' ? 'sorted-asc' : 'sorted-desc');
            loadSelectionData();
        });
    });

    selectAllCheckbox?.addEventListener('change', event => {
        const checked = event.target.checked;
        reviewBody?.querySelectorAll('.row-multi-select').forEach(cb => {
            cb.checked = checked;
            const chatId = cb.dataset.chatId;
            if (!chatId) return;
            const row = rows.find(r => r.chatId === chatId);
            if (checked) {
                multiSelected.add(chatId);
                if (row) selectedEntryDetails.set(chatId, row);
            } else {
                multiSelected.delete(chatId);
                selectedEntryDetails.delete(chatId);
            }
        });
        updateSelectAllCheckbox();
        refreshDetailPanel();
        updateSelectionView();
    });

    selectAllEntriesBtn?.addEventListener('click', () => {
        selectAllEntriesBtn.disabled = true;
        selectAllEntries().finally(() => {
            updateSelectAllEntriesButtonState();
        });
    });

    deselectAllBtn?.addEventListener('click', () => {
        multiSelected.clear();
        selectedEntryDetails.clear();
        reviewBody?.querySelectorAll('.row-multi-select').forEach(cb => cb.checked = false);
        updateSelectAllCheckbox();
        refreshDetailPanel();
        updateSelectionView();
        setManualMessageStatus('Cleared all selected entries.', false);
    });

    exportSelectedBtn?.addEventListener('click', () => {
        const format = (exportFormatSelect && exportFormatSelect.value) ? exportFormatSelect.value : 'csv';
        exportSelected(format);
    });

    reviewBody?.addEventListener('change', event => {
        if (!event.target.matches('.row-multi-select')) return;
        const chatId = event.target.dataset.chatId;
        if (!chatId) return;
        const row = rows.find(r => r.chatId === chatId);
        if (event.target.checked) {
            multiSelected.add(chatId);
            if (row) selectedEntryDetails.set(chatId, row);
        } else {
            multiSelected.delete(chatId);
            selectedEntryDetails.delete(chatId);
        }
        updateSelectAllCheckbox();
        refreshDetailPanel();
        updateSelectionView();
    });
}

function attachManualMessageHandlers() {
    manualMessageToggleBtn?.addEventListener('click', toggleManualMessageSection);
    manualMessageSendBtn?.addEventListener('click', sendManualMessage);
    manualMessageClearBtn?.addEventListener('click', () => {
        if (manualMessageTextarea) manualMessageTextarea.value = '';
        setManualMessageStatus('');
        displayManualMessageResponseRaw('No response yet.');
        multiSelected.clear();
        selectedEntryDetails.clear();
        reviewBody?.querySelectorAll('.row-multi-select').forEach(cb => cb.checked = false);
        updateSelectAllCheckbox();
        updateSelectionView();
        refreshDetailPanel();
    });
    manualMessageCloseBtn?.addEventListener('click', hideManualMessageSection);
}

function toggleManualMessageSection() {
    if (!manualMessageSection) return;
    const isVisible = manualMessageSection.classList.toggle('is-visible');
    manualMessageSection.hidden = !isVisible;
    manualMessageSection.setAttribute('aria-hidden', isVisible ? 'false' : 'true');
    if (manualMessageTextarea && isVisible) manualMessageTextarea.focus();
    if (manualMessageToggleBtn) manualMessageToggleBtn.textContent = isVisible ? 'Hide' : 'Ask IDA?';
    if (!isVisible) displayManualMessageResponseRaw('No response yet.');
}

function hideManualMessageSection() {
    if (!manualMessageSection) return;
    manualMessageSection.classList.remove('is-visible');
    manualMessageSection.setAttribute('aria-hidden', 'true');
    manualMessageSection.hidden = true;
    if (manualMessageToggleBtn) manualMessageToggleBtn.textContent = 'Ask IDA?';
    displayManualMessageResponseRaw('No response yet.');
}

function setManualMessageStatus(message, isError = false) {
    if (!manualMessageStatus) return;
    manualMessageStatus.textContent = message;
    manualMessageStatus.classList.toggle('error', isError);
    manualMessageStatus.classList.toggle('success', !isError && Boolean(message));
}

function parseJsonSafe(value) {
    if (!value) return null;
    try { return JSON.parse(value); } catch { return null; }
}

async function sendManualMessage() {
    if (!manualMessageTextarea) return;
    const text = manualMessageTextarea.value.trim();
    if (!text) {
        setManualMessageStatus('Enter a message before sending.', true);
        return;
    }
    if (!manualMessageSendBtn) return;

    manualMessageSendBtn.disabled = true;
    setManualMessageStatus('Sending...');
    updateSelectionView();

    try {
        const messagePayload = text; // server builds/attaches compressed selected context

        // IMPORTANT: send all selected entries for server-side compression
        const selectedEntries = Array.from(selectedEntryDetails.values()).map(entry => ({
            chatId: entry.chatId || '',
            prompt: entry.prompt || '',
            response: entry.response || '',
            createdAt: entry.createdAt || '',
            sessionId: entry.sessionId || '',
            sessionIdDisplay: entry.sessionIdDisplay || '',
            displayLabel: entry.displayLabel || ''
        }));

        const fetchResp = await fetch(`${contextPath}/dashboard/widgets/drilldown/review/manual-message`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({
                message: messagePayload,
                selectedEntries,
                requestReset: true
            })

        });

        const rawText = await fetchResp.text();
        const parsed = parseJsonSafe(rawText);
        const textResponse = parsed?.textResponse || parsed?.message || rawText || '';

        if (fetchResp.ok) {
            setManualMessageStatus('Message delivered.', false);
            manualMessageTextarea.value = '';
            displayManualMessageResponseRaw(textResponse);
        } else {
            setManualMessageStatus(textResponse || `Unable to send message (status ${fetchResp.status}).`, true);
            displayManualMessageResponseRaw(textResponse || 'No response yet.');
        }
    } catch (error) {
        setManualMessageStatus(`Request failed: ${error.message}`, true);
        displayManualMessageResponseRaw('No response yet.');
    } finally {
        manualMessageSendBtn.disabled = false;
    }
}

function displayManualMessageResponseRaw(text) {
    if (!manualMessageResponse) return;
    const raw = (typeof text === 'string') ? text : String(text || '');
    manualMessageResponse.innerHTML = '';
    const pre = document.createElement('div');
    pre.style.whiteSpace = 'pre-wrap';
    pre.style.fontFamily = 'monospace';
    pre.textContent = raw;
    manualMessageResponse.appendChild(pre);
}

function buildManualMessagePayload(userText) {
    const safeSummary = selectionSummaryText;
    const room = Math.max(0, MAX_TOTAL_MESSAGE_CHARS - userText.length);
    const truncatedSummary = ensureWithinLength(safeSummary, room);
    const selectionSummary = truncatedSummary ? `\n\nSelected chats context:\n${truncatedSummary}` : '';
    return `${userText}${selectionSummary}`;
}

function buildSafeSelectionSummary() {
    const entries = Array.from(selectedEntryDetails.values());
    if (!entries.length) return '';

    const sorted = entries.slice().sort((a, b) => {
        const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
        const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
        return dateB - dateA;
    });

    const blocks = [];
    let used = 0;
    let omitted = 0;

    for (const entry of sorted) {
        const block = formatEntrySummary(entry);
        const addLen = (blocks.length ? 2 : 0) + block.length;
        if (used + addLen > MAX_SUMMARY_CHARS) {
            omitted += 1;
            continue;
        }
        blocks.push(block);
        used += addLen;
    }

    let combined = blocks.join('\n\n');
    if (omitted > 0) {
        const tail = `\n\n... (${omitted} additional selected chats omitted due to size limits)`;
        combined = ensureWithinLength(combined + tail, MAX_SUMMARY_CHARS);
    }
    return combined;
}

function formatEntrySummary(entry) {
    return [
        `### Chat ${entry.chatId || '(unknown)'}`,
        `- Prompt: ${summarizeSentences(entry.prompt, 1, 380)}`,
        `- Response: ${summarizeSentences(entry.response, 1, 380)}`,
        `- Created At: ${entry.createdAt ? formatDate(entry.createdAt) : '(unknown)'}`,
        `- Session ID: ${formatSessionDisplay(entry) || '(none)'}`
    ].join('\n');
}

function summarizeSentences(text, maxSentences, maxChars = 400) {
    if (!text) return '(missing)';
    const normalized = text.replace(/\s+/g, ' ').trim();
    if (!normalized) return '(empty)';
    const sentences = normalized.replace(/\r\n/g, ' ').split(/(?<=[.!?])\s+/).filter(Boolean);
    let out = sentences.length ? sentences.slice(0, maxSentences).join(' ') : normalized;
    if (out.length > maxChars) out = out.slice(0, maxChars) + '…';
    return out;
}

function ensureWithinLength(text, lengthLimit) {
    if (!text || text.length <= lengthLimit) return text;
    return text.slice(0, lengthLimit);
}

function buildDataUrl(page, limit) {
    const params = new URLSearchParams();
    params.append('selectionId', selectionId);
    params.append('limit', String(limit));
    params.append('page', String(page));
    params.append('sortColumn', state.sortColumn);
    params.append('sortDir', state.sortDir);
    if (state.search) params.append('search', state.search);
    return `${contextPath}/dashboard/widgets/drilldown/view/review-data?${params.toString()}`;
}

async function fetchPage(page, limit) {
    const url = buildDataUrl(page, limit);
    const resp = await fetch(url, { headers: { 'Accept': 'application/json' } });
    if (!resp.ok) throw new Error(`Server responded with ${resp.status}`);
    const payload = await resp.json();
    if (!payload || payload.status !== 'ok') throw new Error(payload?.message || 'Unable to load entries.');
    return payload;
}

async function selectAllEntries() {
    if (!state.totalRows) {
        setManualMessageStatus('No entries available to select.', true);
        return;
    }

    try {
        setManualMessageStatus('Selecting all matching entries...');
        const first = await fetchPage(1, BULK_FETCH_PAGE_SIZE);
        const totalPages = Number(first.totalPages || 1);

        const allRows = [];
        allRows.push(...(first.rows || []));

        for (let p = 2; p <= totalPages; p++) {
            const pagePayload = await fetchPage(p, BULK_FETCH_PAGE_SIZE);
            allRows.push(...(pagePayload.rows || []));
        }

        const validRows = allRows.filter(r => r && r.chatId);
        if (!validRows.length) {
            setManualMessageStatus('No chat entries to select.', true);
            return;
        }

        validRows.forEach(row => {
            multiSelected.add(row.chatId);
            selectedEntryDetails.set(row.chatId, row);
        });

        renderRows(rows);
        updateSelectAllCheckbox();
        refreshDetailPanel();
        updateSelectionView();
        setManualMessageStatus(`Selected ${validRows.length} entries across all pages.`, false);
    } catch (error) {
        console.error('selectAllEntries error:', error);
        setManualMessageStatus(`Unable to select all entries: ${error.message}`, true);
    }
}

function updateSelectionView() {
    updateSelectionPreview();
    updateSelectionSummary();
    updateSelectionUI();
}

function updateSelectionPreview() {
    const preview = buildFullSelectionPreview();
    selectionPreviewText = preview || 'No chat selected.';
    if (manualMessageSelectionPreview) manualMessageSelectionPreview.value = selectionPreviewText;
}

function updateSelectionSummary() {
    selectionSummaryText = buildSafeSelectionSummary();
}

function buildFullSelectionPreview() {
    const entries = Array.from(selectedEntryDetails.values());
    if (!entries.length) return '';
    const sorted = entries.slice().sort((a, b) => {
        const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
        const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
        return dateB - dateA;
    });
    return sorted.map(entry => formatEntryPreview(entry)).join('\n\n');
}

function formatEntryPreview(entry) {
    return [
        `### Chat ${entry.chatId || '(unknown)'}`,
        `- Prompt: ${entry.prompt || '(empty)'}`,
        `- Response: ${entry.response || '(empty)'}`,
        `- Created At: ${entry.createdAt ? formatDate(entry.createdAt) : '(unknown)'}`,
        `- Session ID: ${formatSessionDisplay(entry) || '(none)'}`
    ].join('\n');
}

async function loadSelectionData() {
    try {
        const payload = await fetchPage(state.page, state.limit);

        rows = payload.rows || [];
        state.totalPages = payload.totalPages || 1;
        state.page = payload.page || 1;
        state.totalRows = payload.totalRows || 0;

        // keep latest row details in cache for selected ids visible on current page
        rows.forEach(r => {
            if (r?.chatId && multiSelected.has(r.chatId)) {
                selectedEntryDetails.set(r.chatId, r);
            }
        });

        renderRows(rows);
        renderSearchTerms(payload.searchTerms);
        refreshDetailPanel();
        updateSelectionView();
        updatePagination();
        updateSelectAllEntriesButtonState();
    } catch (error) {
        console.error('loadSelectionData error:', error);
        showError(error.message || String(error));
    }
}

function renderRows(data) {
    if (!reviewBody) return;
    if (!data.length) {
        reviewBody.innerHTML = '<tr><td colspan="5" class="empty-row">No selected chats available.</td></tr>';
        if (selectAllCheckbox) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        }
        return;
    }

    reviewBody.innerHTML = data.map(row => {
        const checked = multiSelected.has(row.chatId) ? 'checked' : '';
        const label = formatSessionDisplay(row);
        const muted = row.sessionId && row.sessionId !== label
            ? `<div class="session-id-muted">${escapeHtml(row.sessionId)}</div>`
            : '';

        return `<tr data-chat-id="${escapeHtml(row.chatId)}">
            <td class="select-column">
                <input type="checkbox" class="row-multi-select" data-chat-id="${escapeHtml(row.chatId)}" ${checked}>
            </td>
            <td><div class="text-summary">${escapeHtml(row.chatId)}</div></td>
            <td><div class="text-summary">${escapeHtml(truncateText(row.prompt))}</div></td>
            <td><div class="text-summary">${escapeHtml(formatDate(row.createdAt))}</div></td>
            <td><div class="text-summary">${escapeHtml(label)}</div>${muted}</td>
        </tr>`;
    }).join('');

    updateSelectAllCheckbox();
}

function refreshDetailPanel() {
    if (!detailCard || !detailTitle || !detailPrompt || !detailResponse) return;

    if (!multiSelected.size) {
        detailCard.style.display = 'none';
        detailPrompt.textContent = '';
        detailResponse.textContent = '';
        return;
    }

    detailCard.style.display = 'block';

    if (multiSelected.size === 1) {
        const entry = selectedEntryDetails.get([...multiSelected][0]);
        if (entry) {
            detailTitle.textContent = 'Selected Chat Details';
            detailPrompt.textContent = entry.prompt || '(no prompt)';
            detailResponse.textContent = entry.response || '(no response)';
            return;
        }
    }

    detailTitle.textContent = 'Multiple chats selected';
    detailPrompt.textContent = '';
    detailResponse.textContent = '';
}

function updateSelectAllCheckbox() {
    if (!selectAllCheckbox) return;
    if (!rows.length) {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = false;
        return;
    }

    const totalRowsOnPage = rows.length;
    const selectedOnPage = rows.filter(row => multiSelected.has(row.chatId)).length;
    selectAllCheckbox.checked = selectedOnPage === totalRowsOnPage && totalRowsOnPage > 0;
    selectAllCheckbox.indeterminate = selectedOnPage > 0 && selectedOnPage < totalRowsOnPage;
}

function updateSelectAllEntriesButtonState() {
    if (!selectAllEntriesBtn) return;
    selectAllEntriesBtn.disabled = state.totalRows <= 0;
}

function updatePagination() {
    if (!pageInfo) return;
    pageInfo.textContent = `Page ${state.page} of ${state.totalPages} (${state.limit} per page)`;
    if (prevBtn) prevBtn.disabled = state.page <= 1;
    if (nextBtn) nextBtn.disabled = state.page >= state.totalPages;
}

function renderSearchTerms(terms) {
    if (!searchTermsDisplay || !terms) return;
    const entries = [];
    if (terms.global) entries.push(`Global: "${terms.global}"`);
    if (terms.prompt) entries.push(`Prompt: "${terms.prompt}"`);
    if (terms.response) entries.push(`Response: "${terms.response}"`);

    if (!entries.length) {
        searchTermsDisplay.innerHTML = '<span>No search terms were applied.</span>';
        return;
    }
    searchTermsDisplay.innerHTML = entries.map(text => `<span>${escapeHtml(text)}</span>`).join('');
}

function showError(message) {
    if (reviewBody) {
        reviewBody.innerHTML = `<tr><td colspan="5" class="empty-row" style="color:#b91c1c;">${escapeHtml(message)}</td></tr>`;
    }
    if (detailCard) detailCard.style.display = 'none';
}

function truncateText(text, length = 160) {
    if (!text) return '';
    if (text.length <= length) return text;
    return text.substring(0, length).trim() + '…';
}

function escapeHtml(value) {
    if (!value && value !== 0) return '';
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function formatDate(value) {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function exportSelected(format = 'csv') {
    if (!multiSelected.size) {
        setManualMessageStatus('No selected chats to export.', true);
        return;
    }
    if (!exportSelectedBtn) return;

    exportSelectedBtn.disabled = true;
    setManualMessageStatus('Preparing export...');

    fetch(`${contextPath}/dashboard/widgets/drilldown/export`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': '*/*' },
        body: JSON.stringify({ selectionId, selectedChatIds: Array.from(multiSelected), format })
    })
        .then(async resp => {
            if (!resp.ok) {
                const text = await resp.text();
                throw new Error(`Export failed: ${resp.status} ${text}`);
            }
            return resp.blob().then(blob => ({ resp, blob }));
        })
        .then(({ resp, blob }) => {
            let filename = `export.${format === 'csv' ? 'csv' : format === 'json' ? 'json' : 'txt'}`;
            const cd = resp.headers.get('Content-Disposition') || '';

            const m = /filename\*?=.*''([^;\s]+)/i.exec(cd);
            if (m && m[1]) {
                try { filename = decodeURIComponent(m[1]); } catch { filename = m[1]; }
            } else {
                const m2 = /filename="?([^";]+)"?/i.exec(cd);
                if (m2 && m2[1]) filename = m2[1];
            }

            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            a.remove();
            URL.revokeObjectURL(url);

            setManualMessageStatus('Export ready. Download started.', false);
        })
        .catch(err => {
            console.error('exportSelected error', err);
            setManualMessageStatus(err.message || 'Export failed.', true);
        })
        .finally(() => {
            exportSelectedBtn.disabled = false;
        });
}

function updateSelectionUI() {
    if (!exportSelectedBtn) return;
    exportSelectedBtn.disabled = multiSelected.size === 0;

    const count = multiSelected.size;
    const selectedInfoElem = document.getElementById('selectedInfo');
    if (selectedInfoElem) {
        selectedInfoElem.textContent = count ? `${count} selected across pages.` : '';
    }
}
