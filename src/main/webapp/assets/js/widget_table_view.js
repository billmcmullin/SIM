const config = window.widgetTableViewConfig || {};
const contextPath = config.contextPath || '';
const widgetId = config.widgetId || '';
const globalSearchInput = document.getElementById('globalSearchInput');
const filterPrompt = document.getElementById('filterPrompt');
const filterResponse = document.getElementById('filterResponse');
const tableBody = document.getElementById('widgetTableBody');
const tableHeaders = document.querySelectorAll('#widgetTableView th[data-column]');
const prevBtn = document.getElementById('prevPageBtn');
const nextBtn = document.getElementById('nextPageBtn');
const pageInfo = document.getElementById('pageInfo');
const limitSelectBottom = document.getElementById('limitSelectBottom');
const reviewBtn = document.getElementById('reviewSelectedBtn');
const selectedInfo = document.getElementById('selectedInfo');
const selectAllPageCheckbox = document.getElementById('selectAllPage');

const state = {
    limit: parseInt(limitSelectBottom?.value, 10) || 10,
    page: 1,
    totalPages: 1,
    search: '',
    filters: {
        prompt: '',
        response: ''
    },
    sortColumn: 'created_at',
    sortDir: 'DESC'
};

const selectedChats = new Map();
let latestRows = [];

document.addEventListener('DOMContentLoaded', () => {
    if (!widgetId) {
        showError('Missing widget identifier.');
        return;
    }
    bindControls();
    loadTable();
});

function bindControls() {
    limitSelectBottom?.addEventListener('change', () => {
        state.limit = parseInt(limitSelectBottom.value, 10);
        state.page = 1;
        loadTable();
    });

    [globalSearchInput, filterPrompt, filterResponse].forEach(input => {
        if (!input) return;
        input.addEventListener('input', () => {
            state.page = 1;
            state.search = globalSearchInput.value.trim();
            state.filters.prompt = filterPrompt.value.trim();
            state.filters.response = filterResponse.value.trim();
            loadTable();
        });
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
            loadTable();
        });
    });

    prevBtn?.addEventListener('click', () => {
        if (state.page > 1) {
            state.page -= 1;
            loadTable();
        }
    });
    nextBtn?.addEventListener('click', () => {
        if (state.page < state.totalPages) {
            state.page += 1;
            loadTable();
        }
    });

    tableBody?.addEventListener('change', event => {
        if (!event.target.matches('.row-select')) return;
        const chatId = event.target.dataset.chatId;
        if (!chatId) return;
        if (event.target.checked) {
            const row = latestRows.find(r => r.chatId === chatId);
            if (row) {
                selectedChats.set(chatId, row);
            } else {
                selectedChats.set(chatId, { chatId });
            }
        } else {
            selectedChats.delete(chatId);
        }
        updateSelectionUI();
        updateSelectAllCheckbox();
    });

    selectAllPageCheckbox?.addEventListener('change', event => {
        const checked = event.target.checked;
        const checkboxes = tableBody.querySelectorAll('.row-select');
        checkboxes.forEach(cb => {
            cb.checked = checked;
            const chatId = cb.dataset.chatId;
            if (!chatId) return;
            if (checked) {
                const row = latestRows.find(r => r.chatId === chatId);
                if (row) {
                    selectedChats.set(chatId, row);
                } else {
                    selectedChats.set(chatId, { chatId });
                }
            } else {
                selectedChats.delete(chatId);
            }
        });
        updateSelectionUI();
        updateSelectAllCheckbox();
    });

    reviewBtn?.addEventListener('click', () => {
        if (!selectedChats.size) {
            return;
        }
        const payload = {
            widgetId,
            selectedChatIds: Array.from(selectedChats.keys()),
            searchTerms: {
                global: state.search,
                prompt: state.filters.prompt,
                response: state.filters.response
            }
        };
        reviewBtn.disabled = true;
        fetch(`${contextPath}/admin/widgets/review/start`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        })
            .then(async response => {
                const data = await response.json();
                if (!response.ok) {
                    throw new Error(data.message || 'Unable to prepare review.');
                }
                window.location.href = `${contextPath}/admin/widgets/review?selectionId=${encodeURIComponent(data.selectionId)}`;
            })
            .catch(error => {
                reviewBtn.disabled = false;
                showError(error.message);
            });
    });
}

function loadTable() {
    const params = new URLSearchParams();
    params.append('widgetId', widgetId);
    params.append('limit', state.limit);
    params.append('page', state.page);
    params.append('sortColumn', state.sortColumn);
    params.append('sortDir', state.sortDir);
    if (state.search) params.append('search', state.search);
    if (state.filters.prompt) params.append('filterPrompt', state.filters.prompt);
    if (state.filters.response) params.append('filterResponse', state.filters.response);

    fetch(`${contextPath}/admin/widgets/view/data?${params.toString()}`, {
        headers: { 'Accept': 'application/json' }
    })
        .then(res => res.json())
        .then(payload => {
            if (payload.status !== 'ok') {
                throw new Error(payload.message || 'Unable to load data.');
            }
            state.totalPages = payload.totalPages || 1;
            state.page = payload.page || 1;
            latestRows = payload.rows || [];
            renderRows(latestRows);
            updatePagination();
            updateSelectionUI();
            updateSelectAllCheckbox();
        })
        .catch(error => {
            showError(error.message);
        });
}

function renderRows(rows) {
    if (!tableBody) return;
    if (!rows.length) {
        tableBody.innerHTML = '<tr><td colspan="6" class="empty-row">No entries found.</td></tr>';
        return;
    }
    tableBody.innerHTML = rows.map(row => {
        const checked = selectedChats.has(row.chatId) ? 'checked' : '';
        return `<tr>
            <td class="select-column">
                <input type="checkbox" class="row-select" data-chat-id="${escapeHtml(row.chatId)}" ${checked}>
            </td>
            <td><div class="text-summary">${escapeHtml(row.chatId)}</div></td>
            <td><div class="text-summary">${escapeHtml(row.prompt)}</div></td>
            <td><div class="response-summary">${escapeHtml(truncateResponse(row.response))}</div></td>
            <td><div class="text-summary">${escapeHtml(formatDate(row.createdAt))}</div></td>
            <td><div class="text-summary">${escapeHtml(row.sessionId)}</div></td>
        </tr>`;
    }).join('');
}

function updateSelectionUI() {
    if (!reviewBtn) {
        return;
    }
    const count = selectedChats.size;
    reviewBtn.textContent = `Review Selected (${count})`;
    reviewBtn.disabled = count === 0;
    if (selectedInfo) {
        selectedInfo.textContent = count ? `${count} selected across pages.` : '';
    }
}

function updateSelectAllCheckbox() {
    if (!selectAllPageCheckbox || !latestRows.length) {
        if (selectAllPageCheckbox) {
            selectAllPageCheckbox.checked = false;
            selectAllPageCheckbox.indeterminate = false;
        }
        return;
    }
    const totalRows = latestRows.length;
    const selectedOnPage = latestRows.filter(row => selectedChats.has(row.chatId)).length;
    selectAllPageCheckbox.checked = selectedOnPage === totalRows && totalRows > 0;
    selectAllPageCheckbox.indeterminate = selectedOnPage > 0 && selectedOnPage < totalRows;
}

function updatePagination() {
    if (!pageInfo) return;
    pageInfo.textContent = `Page ${state.page} of ${state.totalPages}`;
    if (prevBtn) prevBtn.disabled = state.page <= 1;
    if (nextBtn) nextBtn.disabled = state.page >= state.totalPages;
}

function showError(message) {
    if (tableBody) {
        tableBody.innerHTML = `<tr><td colspan="6" class="empty-row" style="color:#b91c1c;">${escapeHtml(message)}</td></tr>`;
    }
}

function truncateResponse(text) {
    if (!text) return '';
    return text.length <= 220 ? text : text.slice(0, 217) + '…';
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
