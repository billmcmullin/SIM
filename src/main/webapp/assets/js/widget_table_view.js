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
const selectAllMatchesBtn = document.getElementById('selectAllMatchesBtn');
const deselectAllMatchesBtn = document.getElementById('deselectAllMatchesBtn');

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

    selectAllMatchesBtn?.addEventListener('click', () => {
        if (!selectAllMatchesBtn) return;
        selectAllMatchesBtn.disabled = true;
        fetchAllMatchingChatIds()
            .then(ids => {
                ids.forEach(chatId => {
                    if (!chatId) return;
                    selectedChats.set(chatId, { chatId });
                });
                renderRows(latestRows);
                updateSelectionUI();
                updateSelectAllCheckbox();
            })
            .catch(error => {
                showError(error.message);
            })
            .finally(() => {
                selectAllMatchesBtn.disabled = false;
            });
    });

    deselectAllMatchesBtn?.addEventListener('click', () => {
        selectedChats.clear();
        renderRows(latestRows);
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
        fetch(`${contextPath}/dashboard/widgets/review/start`, {
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
                window.location.href = `${contextPath}/dashboard/widgets/drilldown/review?selectionId=${encodeURIComponent(data.selectionId)}`;
            })
            .catch(error => {
                reviewBtn.disabled = false;
                showError(error.message);
            });
    });
}

function fetchAllMatchingChatIds() {
    const params = new URLSearchParams();
    params.append('widgetId', widgetId);
    if (state.search) params.append('search', state.search);
    if (state.filters.prompt) params.append('filterPrompt', state.filters.prompt);
    if (state.filters.response) params.append('filterResponse', state.filters.response);

    return fetch(`${contextPath}/dashboard/widgets/view/select-ids?${params.toString()}`, {
        headers: { 'Accept': 'application/json' }
    })
        .then(res => res.json())
        .then(payload => {
            if (payload.status !== 'ok') {
                throw new Error(payload.message || 'Unable to collect all chat IDs.');
            }
            return payload.chatIds || [];
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

    fetch(`${contextPath}/dashboard/widgets/drilldown/view/data?${params.toString()}`, {
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

// Updated renderRows: build DOM nodes and put response text into a non-HTML element using textContent
function renderRows(rows) {
    if (!tableBody) return;
    tableBody.innerHTML = '';
    if (!rows.length) {
        tableBody.innerHTML = '<tr><td colspan="6" class="empty-row">No entries found.</td></tr>';
        return;
    }

    const fragment = document.createDocumentFragment();

    rows.forEach(row => {
        const tr = document.createElement('tr');

        // select checkbox cell
        const tdSelect = document.createElement('td');
        tdSelect.className = 'select-column';
        const input = document.createElement('input');
        input.type = 'checkbox';
        input.className = 'row-select';
        if (row.chatId !== undefined && row.chatId !== null) input.dataset.chatId = String(row.chatId);
        input.checked = selectedChats.has(row.chatId);
        tdSelect.appendChild(input);
        tr.appendChild(tdSelect);

        // chatId cell
        const tdId = document.createElement('td');
        const idDiv = document.createElement('div');
        idDiv.className = 'text-summary';
        idDiv.textContent = row.chatId ?? '';
        tdId.appendChild(idDiv);
        tr.appendChild(tdId);

        // prompt cell (use textContent to show verbatim)
        const tdPrompt = document.createElement('td');
        const promptDiv = document.createElement('div');
        promptDiv.className = 'text-summary';
        promptDiv.textContent = row.prompt ?? '';
        tdPrompt.appendChild(promptDiv);
        tr.appendChild(tdPrompt);

        // response cell: non-HTML element showing verbatim (but truncated for table)
        const tdResponse = document.createElement('td');
        const respDiv = document.createElement('div');
        respDiv.className = 'response-summary';
        respDiv.textContent = truncateResponse(row.response);
        tdResponse.appendChild(respDiv);
        tr.appendChild(tdResponse);

        // createdAt cell
        const tdCreated = document.createElement('td');
        const createdDiv = document.createElement('div');
        createdDiv.className = 'text-summary';
        createdDiv.textContent = formatDate(row.createdAt);
        tdCreated.appendChild(createdDiv);
        tr.appendChild(tdCreated);

        // sessionId cell
        const tdSession = document.createElement('td');
        const sessionDiv = document.createElement('div');
        sessionDiv.className = 'text-summary';
        sessionDiv.textContent = row.sessionId ?? '';
        tdSession.appendChild(sessionDiv);
        tr.appendChild(tdSession);

        fragment.appendChild(tr);
    });

    tableBody.appendChild(fragment);
}

function updateSelectionUI() {
    if (!reviewBtn) return;
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
