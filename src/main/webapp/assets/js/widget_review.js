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
const selectPageBtn = document.getElementById('selectPageBtn');
const deselectAllBtn = document.getElementById('deselectAllBtn');

const state = {
    limit: 10,
    page: 1,
    totalPages: 1,
    sortColumn: 'created_at',
    sortDir: 'DESC',
    search: ''
};

let rows = [];
let activeChatId = null;
let debounceTimer;
const multiSelected = new Set();

document.addEventListener('DOMContentLoaded', () => {
    if (!selectionId) {
        showError('Missing selection reference.');
        return;
    }
    attachHandlers();
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
            if (checked) {
                multiSelected.add(chatId);
            } else {
                multiSelected.delete(chatId);
            }
        });
        updateSelectAllCheckbox();
        refreshDetailPanel();
    });

    selectPageBtn?.addEventListener('click', () => {
        reviewBody?.querySelectorAll('.row-multi-select').forEach(cb => {
            cb.checked = true;
            const chatId = cb.dataset.chatId;
            if (!chatId) return;
            multiSelected.add(chatId);
        });
        updateSelectAllCheckbox();
        refreshDetailPanel();
    });

    deselectAllBtn?.addEventListener('click', () => {
        multiSelected.clear();
        reviewBody?.querySelectorAll('.row-multi-select').forEach(cb => {
            cb.checked = false;
        });
        updateSelectAllCheckbox();
        refreshDetailPanel();
    });

    reviewBody?.addEventListener('change', event => {
        if (!event.target.matches('.row-multi-select')) return;
        const chatId = event.target.dataset.chatId;
        if (!chatId) return;
        if (event.target.checked) {
            multiSelected.add(chatId);
        } else {
            multiSelected.delete(chatId);
        }
        updateSelectAllCheckbox();
        refreshDetailPanel();
    });
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
            renderRows(rows);
            renderSearchTerms(payload.searchTerms);
            if (!multiSelected.size && rows.length) {
                activeChatId = rows[0].chatId;
            }
            refreshDetailPanel();
            updatePagination();
        })
        .catch(error => {
            showError(error.message);
        });
}

function renderRows(data) {
    if (!reviewBody) return;
    if (!data.length) {
        reviewBody.innerHTML = '<tr><td colspan="5" class="empty-row">No selected chats available.</td></tr>';
        selectAllCheckbox && (selectAllCheckbox.checked = false);
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

    reviewBody.querySelectorAll('tr[data-chat-id]').forEach(row => {
        row.addEventListener('click', () => {
            const chatId = row.dataset.chatId;
            activeChatId = chatId;
            reviewBody.querySelectorAll('tr').forEach(r => r.classList.toggle('selected', r.dataset.chatId === chatId));
            refreshDetailPanel();
        });
    });
    updateSelectAllCheckbox();
}

function refreshDetailPanel() {
    if (!detailCard || !detailTitle) return;
    const selectionCount = multiSelected.size;
    if (selectionCount > 1) {
        detailTitle.textContent = 'Select one chat to review';
        detailPrompt.textContent = '';
        detailResponse.textContent = '';
        detailCard.style.display = 'block';
        return;
    }
    if (selectionCount === 1) {
        const chatId = [...multiSelected][0];
        const record = rows.find(r => r.chatId === chatId);
        if (record) {
            detailTitle.textContent = 'Selected Chat Details';
            detailPrompt.textContent = record.prompt || '(no prompt)';
            detailResponse.textContent = record.response || '(no response)';
            detailCard.style.display = 'block';
            return;
        }
    }
    const fallback = rows.find(r => r.chatId === activeChatId) || rows[0];
    if (fallback) {
        detailTitle.textContent = 'Selected Chat Details';
        detailPrompt.textContent = fallback.prompt || '(no prompt)';
        detailResponse.textContent = fallback.response || '(no response)';
        detailCard.style.display = 'block';
    } else {
        detailCard.style.display = 'none';
    }
}

function updateSelectAllCheckbox() {
    if (!selectAllCheckbox) {
        return;
    }
    if (!rows.length) {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = false;
        return;
    }
    const totalRows = rows.length;
    const selectedOnPage = rows.filter(row => multiSelected.has(row.chatId)).length;
    selectAllCheckbox.checked = selectedOnPage === totalRows && totalRows > 0;
    selectAllCheckbox.indeterminate = selectedOnPage > 0 && selectedOnPage < totalRows;
}

function updatePagination() {
    if (!pageInfo) return;
    pageInfo.textContent = `Page ${state.page} of ${state.totalPages}`;
    if (prevBtn) prevBtn.disabled = state.page <= 1;
    if (nextBtn) nextBtn.disabled = state.page >= state.totalPages;
}

function renderSearchTerms(terms) {
    if (!searchTermsDisplay || !terms) {
        return;
    }
    const entries = [];
    if (terms.global) {
        entries.push(`Global: "${terms.global}"`);
    }
    if (terms.prompt) {
        entries.push(`Prompt: "${terms.prompt}"`);
    }
    if (terms.response) {
        entries.push(`Response: "${terms.response}"`);
    }
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
    if (detailCard) {
        detailCard.style.display = 'none';
    }
}

function truncateText(text) {
    if (!text) return '';
    return text.length <= 160 ? text : text.substr(0, 157) + '…';
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
