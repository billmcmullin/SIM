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
const prevBtn = document.getElementById('prevPageBtn');
const nextBtn = document.getElementById('nextPageBtn');
const pageInfo = document.getElementById('pageInfo');
const tableHeaders = document.querySelectorAll('.widget-review-table th[data-column]');
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

const MAX_SUMMARY_CHARS = 100000000;
const MAX_TOTAL_MESSAGE_CHARS = 8912;

document.addEventListener('DOMContentLoaded', () => {
    if (!selectionId) {
        showError('Missing selection reference.');
        return;
    }
    attachHandlers();
    attachManualMessageHandlers();
    loadSelectionData();
});

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
                if (row) {
                    selectedEntryDetails.set(chatId, row);
                }
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
            selectAllEntriesBtn.disabled = false;
        });
    });

    deselectAllBtn?.addEventListener('click', () => {
        multiSelected.clear();
        selectedEntryDetails.clear();
        reviewBody?.querySelectorAll('.row-multi-select').forEach(cb => {
            cb.checked = false;
        });
        updateSelectAllCheckbox();
        refreshDetailPanel();
        updateSelectionView();
    });

    reviewBody?.addEventListener('change', event => {
        if (!event.target.matches('.row-multi-select')) return;
        const chatId = event.target.dataset.chatId;
        if (!chatId) return;
        const row = rows.find(r => r.chatId === chatId);
        if (event.target.checked) {
            multiSelected.add(chatId);
            if (row) {
                selectedEntryDetails.set(chatId, row);
            }
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
        if (manualMessageTextarea) {
            manualMessageTextarea.value = '';
        }
        setManualMessageStatus('');
        displayManualMessageResponse('No response yet.');
        multiSelected.clear();
        selectedEntryDetails.clear();
        reviewBody?.querySelectorAll('.row-multi-select').forEach(cb => {
            cb.checked = false;
        });
        updateSelectAllCheckbox();
        updateSelectionView();
        refreshDetailPanel();
    });
    manualMessageCloseBtn?.addEventListener('click', hideManualMessageSection);
}

function toggleManualMessageSection() {
    if (!manualMessageSection) return;
    const isVisible = manualMessageSection.classList.toggle('is-visible');
    manualMessageSection.setAttribute('aria-hidden', isVisible ? 'false' : 'true');
    if (manualMessageTextarea && isVisible) {
        manualMessageTextarea.focus();
    }
    if (manualMessageToggleBtn) {
        manualMessageToggleBtn.textContent = isVisible ? 'Hide manual workspace message' : 'Send manual workspace message';
    }
    if (!isVisible) {
        displayManualMessageResponse('No response yet.');
    }
}

function hideManualMessageSection() {
    if (!manualMessageSection) return;
    manualMessageSection.classList.remove('is-visible');
    manualMessageSection.setAttribute('aria-hidden', 'true');
    if (manualMessageToggleBtn) {
        manualMessageToggleBtn.textContent = 'Send manual workspace message';
    }
    displayManualMessageResponse('No response yet.');
}

function setManualMessageStatus(message, isError = false) {
    if (!manualMessageStatus) return;
    manualMessageStatus.textContent = message;
    manualMessageStatus.classList.toggle('error', isError);
    manualMessageStatus.classList.toggle('success', !isError && Boolean(message));
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
        const messagePayload = buildManualMessagePayload(text);
        const response = await fetch(`${contextPath}/admin/widgets/review/manual-message`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ message: messagePayload })
        });
        const rawBody = await response.text();
        if (response.ok) {
            const parsed = parseJsonSafe(rawBody);
            const textResponse = parsed && typeof parsed.textResponse === 'string'
                ? parsed.textResponse
                : rawBody || 'No response body.';
            setManualMessageStatus('Message delivered.', false);
            manualMessageTextarea.value = '';
            displayManualMessageResponse(textResponse);
        } else {
            const parsed = parseJsonSafe(rawBody);
            const errorText = parsed?.message || parsed?.error || rawBody || `Unable to send message (status ${response.status}).`;
            setManualMessageStatus(errorText, true);
            displayManualMessageResponse(parsed?.textResponse || errorText);
        }
    } catch (error) {
        setManualMessageStatus(`Request failed: ${error.message}`, true);
        displayManualMessageResponse('No response yet.');
    } finally {
        manualMessageSendBtn.disabled = false;
    }
}

function buildManualMessagePayload(userText) {
    const safeSummary = selectionSummaryText;
    const truncatedSummary = ensureWithinLength(safeSummary, MAX_TOTAL_MESSAGE_CHARS - userText.length);
    const selectionSummary = truncatedSummary
        ? `\n\nSelected chats context:\n${truncatedSummary}`
        : '';
    return `${userText}${selectionSummary}`;
}

function buildSafeSelectionSummary() {
    const entries = Array.from(selectedEntryDetails.values());
    if (!entries.length) {
        return '';
    }
    const summaryBlocks = entries
        .slice()
        .sort((a, b) => {
            const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
            const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
            return dateB - dateA;
        })
        .map(entry => formatEntrySummary(entry));

    let combined = summaryBlocks.join('\n\n');
    if (combined.length <= MAX_SUMMARY_CHARS) {
        return combined;
    }
    // Trim the combined string to the maximum allowed length without dropping entries completely.
    return ensureWithinLength(combined, MAX_SUMMARY_CHARS);
}

function formatEntrySummary(entry) {
    return [
        `### Chat ${entry.chatId || '(unknown)'}`,
        `- Prompt: ${summarizeSentences(entry.prompt, 1)}`,
        `- Response: ${summarizeSentences(entry.response, 1)}`,
        `- Created At: ${entry.createdAt ? formatDate(entry.createdAt) : '(unknown)'}`,
        `- Session ID: ${entry.sessionId || '(none)'}`
    ].join('\n');
}

function summarizeSentences(text, maxSentences) {
    if (!text) return '(missing)';
    const normalized = text.replace(/\s+/g, ' ').trim();
    if (!normalized) return '(empty)';
    const sentences = normalized
        .replace(/\r\n/g, ' ')
        .split(/(?<=[.!?])\s+/)
        .filter(Boolean);
    if (!sentences.length) {
        return normalized;
    }
    return sentences.slice(0, maxSentences).join(' ');
}

function ensureWithinLength(text, lengthLimit) {
    if (!text || text.length <= lengthLimit) {
        return text;
    }
    return text.slice(0, lengthLimit);
}

function selectAllEntries() {
    if (!state.totalRows) {
        setManualMessageStatus('No entries available to select.', true);
        return Promise.resolve();
    }
    const params = new URLSearchParams();
    params.append('selectionId', selectionId);
    params.append('limit', state.totalRows);
    params.append('page', 1);
    params.append('sortColumn', state.sortColumn);
    params.append('sortDir', state.sortDir);
    if (state.search) {
        params.append('search', state.search);
    }
    return fetch(`${contextPath}/admin/widgets/view/review-data?${params.toString()}`, {
        headers: { 'Accept': 'application/json' }
    })
        .then(res => res.json())
        .then(payload => {
            if (payload.status !== 'ok') {
                throw new Error(payload.message || 'Unable to load all entries.');
            }
            const newEntries = (payload.rows || [])
                .filter(row => row.chatId)
                .map(row => ({ ...row }));
            if (!newEntries.length) {
                setManualMessageStatus('No chat entries to select.', true);
                return;
            }
            newEntries.forEach(row => {
                multiSelected.add(row.chatId);
                selectedEntryDetails.set(row.chatId, row);
            });
            renderRows(rows);
            updateSelectAllCheckbox();
            refreshDetailPanel();
            updateSelectionView();
            setManualMessageStatus(`Selected ${newEntries.length} entries.`, false);
        })
        .catch(error => {
            setManualMessageStatus(`Unable to select all entries: ${error.message}`, true);
        });
}

function displayManualMessageResponse(text) {
    if (!manualMessageResponse) return;
    const sanitized = text || 'No response yet.';
    manualMessageResponse.innerHTML = renderMarkdownIfNeeded(sanitized);
}

function renderMarkdownIfNeeded(raw) {
    if (!raw) return '';
    const hasMarkers = /(#|\*|_|\`)/.test(raw);
    const escaped = escapeHtml(raw);
    if (!hasMarkers) {
        return `<p>${escaped.replace(/\n/g, '<br>')}</p>`;
    }
    let formatted = escaped;
    formatted = formatted.replace(/```([\s\S]*?)```/g, (_, inner) => `<div class="markdown-code">${inner}</div>`);
    for (let i = 6; i >= 1; i -= 1) {
        const pattern = new RegExp(`^${'#'.repeat(i)}\\s+(.+)$`, 'gm');
        formatted = formatted.replace(pattern, `<h${i}>$1</h${i}>`);
    }
    formatted = formatted.replace(/__(.+?)__/g, '<strong>$1</strong>');
    formatted = formatted.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
    formatted = formatted.replace(/_(.+?)_/g, '<em>$1</em>');
    formatted = formatted.replace(/\*(.+?)\*/g, '<em>$1</em>');
    formatted = formatted.replace(/`([^`\n]+)`/g, '<code>$1</code>');
    formatted = formatted.replace(/\n{2,}/g, '[[PARAGRAPH_BREAK]]');
    return formatted.split('[[PARAGRAPH_BREAK]]')
        .map(part => {
            const trimmed = part.trim();
            if (!trimmed) return '';
            return /^<(h[1-6]|ul|div|pre)/.test(trimmed) ? trimmed : `<p>${trimmed.replace(/\n/g, '<br>')}</p>`;
        })
        .join('');
}

function convertToolsLists(text) {
    return text.replace(/((?:^[-+*]\s.+\n?)+)/gm, match => {
        const items = match.trim().split('\n').map(line => line.replace(/^[-+*]\s+/, '').trim()).filter(Boolean);
        if (!items.length) return '';
        return `<ul>${items.map(item => `<li>${item}</li>`).join('')}</ul>`;
    });
}

function parseJsonSafe(value) {
    if (!value) return null;
    try {
        return JSON.parse(value);
    } catch {
        return null;
    }
}

function updateSelectionView() {
    updateSelectionPreview();
    updateSelectionSummary();
}

function updateSelectionPreview() {
    const preview = buildFullSelectionPreview();
    selectionPreviewText = preview || 'No chat selected.';
    if (manualMessageSelectionPreview) {
        manualMessageSelectionPreview.value = selectionPreviewText;
    }
}

function updateSelectionSummary() {
    selectionSummaryText = buildSafeSelectionSummary();
}

function buildFullSelectionPreview() {
    const entries = Array.from(selectedEntryDetails.values());
    if (!entries.length) return '';
    const sorted = entries
        .slice()
        .sort((a, b) => {
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
        `- Session ID: ${entry.sessionId || '(none)'}`
    ].join('\n');
}

function loadSelectionData() {
    const params = new URLSearchParams();
    params.append('selectionId', selectionId);
    params.append('limit', state.limit);
    params.append('page', state.page);
    params.append('sortColumn', state.sortColumn);
    params.append('sortDir', state.sortDir);
    if (state.search) {
        params.append('search', state.search);
    }

    fetch(`${contextPath}/admin/widgets/view/review-data?${params.toString()}`, {
        headers: {
            'Accept': 'application/json'
        }
    })
        .then(res => res.json())
        .then(payload => {
            if (payload.status !== 'ok') {
                throw new Error(payload.message || 'Unable to load selection.');
            }
            rows = payload.rows || [];
            state.totalPages = payload.totalPages || 1;
            state.page = payload.page || 1;
            state.totalRows = payload.totalRows || 0;
            renderRows(rows);
            renderSearchTerms(payload.searchTerms);
            refreshDetailPanel();
            updateSelectionView();
            updatePagination();
            updateSelectAllEntriesButtonState();
        })
        .catch(error => {
            showError(error.message);
        });
}

function renderRows(data) {
    if (!reviewBody) return;
    if (!data.length) {
        reviewBody.innerHTML = '<tr><td colspan="5" class="empty-row">No selected chats available.</td></tr>';
        if (selectAllCheckbox) selectAllCheckbox.checked = false;
        return;
    }
    reviewBody.innerHTML = data.map(row => {
        const checked = multiSelected.has(row.chatId) ? 'checked' : '';
        return `<tr data-chat-id="${escapeHtml(row.chatId)}">
            <td class="select-column">
                <input type="checkbox" class="row-multi-select" data-chat-id="${escapeHtml(row.chatId)}" ${checked}>
            </td>
            <td><div class="text-summary">${escapeHtml(row.chatId)}</div></td>
            <td><div class="text-summary">${escapeHtml(truncateText(row.prompt))}</div></td>
            <td><div class="text-summary">${escapeHtml(formatDate(row.createdAt))}</div></td>
            <td><div class="text-summary">${escapeHtml(row.sessionId)}</div></td>
        </tr>`;
    }).join('');
    updateSelectAllCheckbox();
}

function refreshDetailPanel() {
    if (!detailCard || !detailTitle) return;
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
    pageInfo.textContent = `Page ${state.page} of ${state.totalPages}`;
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
    if (!value) return '';
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
