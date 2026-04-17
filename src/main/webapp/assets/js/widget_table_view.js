(function () {
    const cfg = window.widgetTableViewConfig || {};
    const contextPath = cfg.contextPath || '';
    const widgetId = cfg.widgetId || '';
    const widgetDisplayName = cfg.widgetName || '';

    const API_DATA = contextPath + '/dashboard/widgets/drilldown/view/data';
    const API_SELECT_IDS = contextPath + '/dashboard/widgets/view/select-ids';
    const API_REVIEW_START = contextPath + '/dashboard/widgets/review/start';
    const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

    let state = {
        limit: 10,
        page: 1,
        totalPages: 1,
        search: '',
        filters: { prompt: '', response: '' },
        sortColumn: 'created_at',
        sortDir: 'DESC'
    };

    let latestRows = [];
    const selectedChats = new Map();

    let tableBody, globalSearchInput, filterPrompt, filterResponse;
    let prevBtn, nextBtn, pageInfo, pageSizeSelect;
    let reviewBtn, selectedInfo, selectAllPageCheckbox, selectAllMatchesBtn, deselectAllMatchesBtn;

    const esc = s => (s == null ? '' : String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;'));
    const fmtDate = v => { if (!v) return ''; try { return new Date(v).toLocaleString(); } catch { return v; } };
    const truncateResponse = (text) => { if (!text) return ''; return text.length <= 220 ? text : text.slice(0, 217) + '…'; };

    function formatSessionDisplay(row) {
        if (!row) return '';
        if (row.sessionIdDisplay) return row.sessionIdDisplay;
        if (row.displayLabel) return row.displayLabel;
        return row.sessionId || '';
    }

    function buildCustomerProfileUrl(sessionId, friendlyName) {
        const p = new URLSearchParams();
        const sid = sessionId == null ? '' : String(sessionId).trim();
        const fname = friendlyName == null ? '' : String(friendlyName).trim();

        if (sid) p.set('sessionId', sid);
        else if (fname) p.set('friendlyName', fname);
        else return '';

        return `${contextPath}/customer-profile?${p.toString()}`;
    }

    function appendProfileLink(container, text, sessionId, friendlyName) {
        const label = text == null ? '' : String(text).trim();
        if (!label) return;

        const href = buildCustomerProfileUrl(sessionId, friendlyName);
        if (!href) {
            container.textContent = label;
            return;
        }

        const a = document.createElement('a');
        a.href = href;
        a.className = 'customer-profile-link';
        a.textContent = label;
        a.title = sessionId ? `Open customer profile (${sessionId})` : 'Open customer profile';
        container.appendChild(a);
    }

    async function safeFetchJson(url) {
        const res = await fetch(url, { credentials: 'same-origin' });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const ct = res.headers.get('content-type') || '';
        const text = await res.text();
        if (!ct.includes('application/json')) {
            try {
                return JSON.parse(text);
            } catch (e) {
                throw new Error('Server returned non-JSON response');
            }
        }
        return JSON.parse(text);
    }

    function renderRows(rows) {
        if (!tableBody) return;
        tableBody.innerHTML = '';
        if (!rows || rows.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="empty-row">No entries found.</td></tr>';
            return;
        }

        const fragment = document.createDocumentFragment();
        rows.forEach(row => {
            const tr = document.createElement('tr');
            const tdSelect = document.createElement('td');
            tdSelect.className = 'select-column';
            const input = document.createElement('input');
            input.type = 'checkbox';
            input.className = 'row-select';
            if (row.chatId !== undefined && row.chatId !== null) input.dataset.chatId = String(row.chatId);
            input.checked = selectedChats.has(row.chatId);
            tdSelect.appendChild(input);
            tr.appendChild(tdSelect);

            const tdId = document.createElement('td');
            const idDiv = document.createElement('div');
            idDiv.className = 'text-summary';
            idDiv.textContent = `${row.widgetName || widgetDisplayName || row.widgetId || widgetId || ''} · ${row.chatId ?? ''}`;
            tdId.appendChild(idDiv);
            tr.appendChild(tdId);

            const tdPrompt = document.createElement('td');
            const promptDiv = document.createElement('div');
            promptDiv.className = 'text-summary';
            promptDiv.textContent = row.prompt ?? '';
            promptDiv.title = row.prompt ?? '';
            tdPrompt.appendChild(promptDiv);
            tr.appendChild(tdPrompt);

            const tdResponse = document.createElement('td');
            const respDiv = document.createElement('div');
            respDiv.className = 'response-summary';
            respDiv.textContent = truncateResponse(row.response);
            respDiv.title = row.response ?? '';
            tdResponse.appendChild(respDiv);
            tr.appendChild(tdResponse);

            const tdCreated = document.createElement('td');
            const createdDiv = document.createElement('div');
            createdDiv.className = 'text-summary';
            createdDiv.textContent = fmtDate(row.createdAt);
            tdCreated.appendChild(createdDiv);
            tr.appendChild(tdCreated);

            const tdSession = document.createElement('td');
            const sessionDiv = document.createElement('div');
            sessionDiv.className = 'text-summary';
            const sessionLabel = formatSessionDisplay(row);

            appendProfileLink(
                sessionDiv,
                sessionLabel,
                row.sessionId,
                row.displayLabel || row.sessionIdDisplay || sessionLabel
            );

            tdSession.appendChild(sessionDiv);

            if (row.sessionId && sessionLabel !== row.sessionId) {
                const muted = document.createElement('div');
                muted.className = 'session-id-muted';

                appendProfileLink(
                    muted,
                    row.sessionId,
                    row.sessionId,
                    row.displayLabel || row.sessionIdDisplay || sessionLabel
                );

                tdSession.appendChild(muted);
            }
            tr.appendChild(tdSession);

            fragment.appendChild(tr);
        });

        tableBody.appendChild(fragment);
    }

    function updateSelectionUI() {
        const count = selectedChats.size;
        if (reviewBtn) {
            reviewBtn.textContent = `Review Selected (${count})`;
            reviewBtn.disabled = count === 0;
        }
        if (selectedInfo) {
            selectedInfo.textContent = count ? `${count} selected across pages.` : '';
        }
    }

    function updateSelectAllCheckbox() {
        if (!selectAllPageCheckbox) return;
        if (!latestRows.length) {
            selectAllPageCheckbox.checked = false;
            selectAllPageCheckbox.indeterminate = false;
            return;
        }
        const totalRows = latestRows.length;
        const selectedOnPage = latestRows.filter(row => selectedChats.has(row.chatId)).length;
        selectAllPageCheckbox.checked = selectedOnPage === totalRows && totalRows > 0;
        selectAllPageCheckbox.indeterminate = selectedOnPage > 0 && selectedOnPage < totalRows;
    }

    function updatePagination(totalPagesFromServer, currentPage) {
        state.totalPages = totalPagesFromServer || state.totalPages;
        state.page = currentPage || state.page;
        if (pageInfo) pageInfo.textContent = `Page ${state.page} of ${state.totalPages} (${state.limit} per page)`;
        if (prevBtn) prevBtn.disabled = state.page <= 1;
        if (nextBtn) nextBtn.disabled = state.page >= state.totalPages;
    }

    function showError(message) {
        if (tableBody) {
            tableBody.innerHTML = `<tr><td colspan="6" class="empty-row" style="color:#b91c1c">${esc(message)}</td></tr>`;
        }
    }

    function bindControls() {
        [globalSearchInput, filterPrompt, filterResponse].forEach(input => {
            if (!input) return;
            input.addEventListener('input', () => {
                state.page = 1;
                state.search = globalSearchInput ? globalSearchInput.value.trim() : '';
                state.filters.prompt = filterPrompt ? filterPrompt.value.trim() : '';
                state.filters.response = filterResponse ? filterResponse.value.trim() : '';
                loadTable();
            });
        });

        const pageSizeSelectEl = document.getElementById('widgetTablePageSize');
        if (pageSizeSelectEl) {
            pageSizeSelectEl.addEventListener('change', () => {
                const val = parseInt(pageSizeSelectEl.value, 10);
                if (Number.isNaN(val)) return;
                state.limit = val;
                state.page = 1;
                loadTable();
            });
        }

        if (tableBody) {
            tableBody.addEventListener('change', event => {
                if (!event.target.matches('.row-select')) return;
                const chatId = event.target.dataset.chatId;
                if (!chatId) return;
                if (event.target.checked) {
                    const row = latestRows.find(r => r.chatId === chatId);
                    if (row) {
                        row.widgetName = row.widgetName || widgetDisplayName || row.widgetId || widgetId;
                        selectedChats.set(chatId, row);
                    } else {
                        selectedChats.set(chatId, { chatId, widgetName: widgetDisplayName || widgetId });
                    }
                } else {
                    selectedChats.delete(chatId);
                }
                updateSelectionUI();
                updateSelectAllCheckbox();
            });
        }

        if (selectAllPageCheckbox) {
            selectAllPageCheckbox.addEventListener('change', event => {
                const checked = event.target.checked;
                const checkboxes = tableBody ? tableBody.querySelectorAll('.row-select') : [];
                checkboxes.forEach(cb => {
                    cb.checked = checked;
                    const chatId = cb.dataset.chatId;
                    if (!chatId) return;
                    if (checked) {
                        const row = latestRows.find(r => r.chatId === chatId);
                        if (row) {
                            row.widgetName = row.widgetName || widgetDisplayName || row.widgetId || widgetId;
                            selectedChats.set(chatId, row);
                        } else {
                            selectedChats.set(chatId, { chatId, widgetName: widgetDisplayName || widgetId });
                        }
                    } else {
                        selectedChats.delete(chatId);
                    }
                });
                updateSelectionUI();
                updateSelectAllCheckbox();
            });
        }

        if (selectAllMatchesBtn) {
            selectAllMatchesBtn.addEventListener('click', async () => {
                selectAllMatchesBtn.disabled = true;
                try {
                    const params = new URLSearchParams();
                    params.append('widgetId', widgetId);
                    if (state.search) params.append('search', state.search);
                    if (state.filters.prompt) params.append('filterPrompt', state.filters.prompt);
                    if (state.filters.response) params.append('filterResponse', state.filters.response);

                    const res = await safeFetchJson(API_SELECT_IDS + '?' + params.toString());
                    if (!res.status || res.status !== 'ok') throw new Error(res.message || 'Unable to collect IDs');
                    (res.chatIds || []).forEach(chatId => {
                        if (!chatId) return;
                        selectedChats.set(chatId, { chatId, widgetName: widgetDisplayName || widgetId });
                    });
                    renderRows(latestRows);
                    updateSelectionUI();
                    updateSelectAllCheckbox();
                } catch (err) {
                    showError(err.message || String(err));
                } finally {
                    selectAllMatchesBtn.disabled = false;
                }
            });
        }

        if (deselectAllMatchesBtn) {
            deselectAllMatchesBtn.addEventListener('click', () => {
                selectedChats.clear();
                renderRows(latestRows);
                updateSelectionUI();
                updateSelectAllCheckbox();
            });
        }

        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                if (state.page > 1) {
                    state.page -= 1;
                    loadTable();
                }
            });
        }
        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                if (state.page < state.totalPages) {
                    state.page += 1;
                    loadTable();
                }
            });
        }

        if (reviewBtn) {
            reviewBtn.addEventListener('click', async () => {
                if (!selectedChats.size) return;
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
                try {
                    const res = await fetch(API_REVIEW_START, {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    });
                    if (!res.ok) {
                        const t = await res.text().catch(() => '');
                        throw new Error(t || `HTTP ${res.status}`);
                    }
                    const data = await res.json();
                    if (!data.selectionId) throw new Error(data.message || 'No selectionId returned');
                    window.location.href = `${contextPath}/dashboard/widgets/drilldown/review?selectionId=${encodeURIComponent(data.selectionId)}`;
                } catch (err) {
                    showError(err.message || String(err));
                    reviewBtn.disabled = false;
                }
            });
        }
    }

    async function loadTable() {
        try {
            const params = new URLSearchParams();
            params.append('widgetId', widgetId);
            params.append('limit', state.limit);
            params.append('page', state.page);
            params.append('sortColumn', state.sortColumn);
            params.append('sortDir', state.sortDir);
            if (state.search) params.append('search', state.search);
            if (state.filters.prompt) params.append('filterPrompt', state.filters.prompt);
            if (state.filters.response) params.append('filterResponse', state.filters.response);

            const res = await safeFetchJson(API_DATA + '?' + params.toString());
            if (res.status !== 'ok') throw new Error(res.message || 'Unable to load data');

            latestRows = res.rows || [];
            state.totalPages = res.totalPages || 1;
            state.page = res.page || 1;

            renderRows(latestRows);
            updatePagination(state.totalPages, state.page);
            updateSelectionUI();
            updateSelectAllCheckbox();
        } catch (err) {
            showError(err.message || String(err));
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        tableBody = document.getElementById('widgetTableBody');
        globalSearchInput = document.getElementById('globalSearchInput');
        filterPrompt = document.getElementById('filterPrompt');
        filterResponse = document.getElementById('filterResponse');
        prevBtn = document.getElementById('prevPageBtn');
        nextBtn = document.getElementById('nextPageBtn');
        pageInfo = document.getElementById('pageInfo');
        reviewBtn = document.getElementById('reviewSelectedBtn');
        selectAllPageCheckbox = document.getElementById('selectAllPage');
        selectAllMatchesBtn = document.getElementById('selectAllMatchesBtn');
        deselectAllMatchesBtn = document.getElementById('deselectAllMatchesBtn');
        selectedInfo = document.getElementById('selectedInfo');

        bindControls();
        loadTable();
    });
})();
