// assets/js/all_sessions.js
(function () {
    const APP = window.APP_CONTEXT_PATH || '';
    const API_BASE = APP + '/dashboard/sessions';
    const DATA_URL = API_BASE + '/data';
    const CHATS_URL = API_BASE + '/chats';
    const SELECT_URL = API_BASE + '/select';
    const PAGE_SIZE = 10;

    // State
    let page = 1, totalPages = 1, totalSessions = 0;
    let widgetNamesMap = {}; // widgetId -> displayName
    const selectedChatIds = new Set();

    // DOM refs
    let sessionsTableBody = null;
    let sessionsTableEl = null;
    let sessionsContainerDiv = null;
    let summaryEl = null;
    let paginationEl = null;
    let searchInput = null;
    let searchBtn = null;
    let refreshBtn = null; // now Clear
    let viewAllBtn = null;
    let reviewSelectedBtn = null;
    let selectionInfo = null;
    let selectAllAllSessionsBtn = null;

    // helpers
    const esc = s => (s == null ? '' : String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;'));
    const fmt = ts => { if (!ts) return ''; try { return new Date(ts).toLocaleString(); } catch { return ts; } };

    async function safeFetchJson(url, opts = {}) {
        const res = await fetch(url, opts);
        if (!res.ok) {
            const txt = await res.text().catch(() => '');
            throw new Error(`HTTP ${res.status} ${res.statusText} ${txt ? '- ' + txt.slice(0, 300) : ''}`);
        }
        const ct = res.headers.get('content-type') || '';
        const text = await res.text();
        if (!ct.includes('application/json')) {
            try { return JSON.parse(text); } catch (e) { throw new Error('Non-JSON response received'); }
        }
        return JSON.parse(text);
    }

    async function loadSessions(reqPage = 1, search = '') {
        if (!sessionsTableBody && !sessionsContainerDiv) return;
        if (sessionsTableBody) sessionsTableBody.innerHTML = '<tr><td colspan="5" class="small-note">Loading sessions…</td></tr>';
        if (sessionsContainerDiv) sessionsContainerDiv.innerHTML = '<div class="small-note">Loading sessions…</div>';

        page = Math.max(1, reqPage);
        const params = new URLSearchParams();
        params.set('limit', String(PAGE_SIZE));
        params.set('page', String(page));
        if (search) params.set('search', search);

        try {
            const json = await safeFetchJson(DATA_URL + '?' + params.toString(), { credentials: 'same-origin' });
            widgetNamesMap = json.widgetNames || {};
            totalSessions = json.totalSessions || 0;
            totalPages = json.totalPages || 1;
            renderSessions(json.sessions || []);
            renderPagination();
            if (summaryEl) summaryEl.textContent = `Showing ${json.sessions ? json.sessions.length : 0} sessions (page ${page}/${totalPages}). Total sessions: ${totalSessions}`;
        } catch (err) {
            const msg = `Unable to load sessions: ${err.message}`;
            if (sessionsTableBody) sessionsTableBody.innerHTML = `<tr><td colspan="5" class="empty-row">${esc(msg)}</td></tr>`;
            if (sessionsContainerDiv) sessionsContainerDiv.innerHTML = `<div class="empty-row">${esc(msg)}</div>`;
            console.error(err);
        }
    }

    function renderSessions(list) {
        if (sessionsTableBody) {
            sessionsTableBody.innerHTML = '';
            if (!Array.isArray(list) || list.length === 0) {
                sessionsTableBody.innerHTML = '<tr><td colspan="5" class="empty-row">No sessions found.</td></tr>';
                return;
            }

            list.forEach(s => {
                const tr = document.createElement('tr');

                // Session ID (full)
                const tdId = document.createElement('td');
                tdId.className = 'session-id-col';
                tdId.innerHTML = `<div style="font-weight:700">${esc(s.sessionId)}</div>`;
                tr.appendChild(tdId);

                // Widgets column (display name from mapping, fallback to widgetId)
                const tdWidgets = document.createElement('td');
                tdWidgets.className = 'session-widgets-col';
                let widgetLabel = '';
                if (Array.isArray(s.widgets) && s.widgets.length) {
                    widgetLabel = s.widgets.map(wid => widgetNamesMap[wid] || wid).filter(Boolean).join(', ');
                }
                tdWidgets.textContent = widgetLabel;
                if (widgetLabel) tdWidgets.title = widgetLabel;
                tr.appendChild(tdWidgets);

                // Chats count
                const tdCount = document.createElement('td');
                tdCount.className = 'session-chats-col';
                tdCount.textContent = String(s.totalCount || 0);
                tr.appendChild(tdCount);

                // Last seen
                const tdLast = document.createElement('td');
                tdLast.className = 'session-last-col';
                tdLast.textContent = s.lastSeen ? fmt(s.lastSeen) : '';
                tr.appendChild(tdLast);

                // Actions
                const tdActions = document.createElement('td');
                tdActions.className = 'session-actions-col';
                tdActions.style.textAlign = 'right';
                const expandBtn = document.createElement('button');
                expandBtn.className = 'ghost-btn small';
                expandBtn.textContent = 'Expand';
                expandBtn.addEventListener('click', () => toggleChatsRow(tr, s.sessionId, expandBtn));
                tdActions.appendChild(expandBtn);

                const selectAllBtn = document.createElement('button');
                selectAllBtn.className = 'ghost-btn small';
                selectAllBtn.style.marginLeft = '8px';
                selectAllBtn.textContent = 'Select All in Session';
                selectAllBtn.addEventListener('click', () => selectAllInSession(s.sessionId, tr));
                tdActions.appendChild(selectAllBtn);

                tr.appendChild(tdActions);
                sessionsTableBody.appendChild(tr);
            });

            return;
        }

        // card fallback
        if (sessionsContainerDiv) {
            sessionsContainerDiv.innerHTML = '';
            if (!Array.isArray(list) || list.length === 0) {
                sessionsContainerDiv.innerHTML = '<div class="empty-row">No sessions found.</div>';
                return;
            }
            list.forEach(s => {
                const card = document.createElement('div');
                card.className = 'session-card';

                const header = document.createElement('div');
                header.className = 'session-header';

                const left = document.createElement('div');
                left.className = 'session-header-left';
                left.innerHTML = `<div style="font-weight:700">${esc(s.sessionId)}</div><div class="session-meta">${esc(String(s.totalCount || 0))} chats · Last: ${esc(s.lastSeen ? fmt(s.lastSeen) : '')}</div>`;
                header.appendChild(left);

                const right = document.createElement('div');
                right.style.display = 'flex';
                right.style.flexDirection = 'column';
                right.style.alignItems = 'flex-end';

                const widgetLabel = Array.isArray(s.widgets) && s.widgets.length ? s.widgets.map(wid => widgetNamesMap[wid] || wid).filter(Boolean).join(', ') : '';
                if (widgetLabel) {
                    const widEl = document.createElement('div');
                    widEl.className = 'session-widgets-right';
                    widEl.textContent = widgetLabel;
                    right.appendChild(widEl);
                }

                const actions = document.createElement('div');
                actions.style.display = 'flex';
                actions.style.gap = '8px';
                const expandBtn = document.createElement('button');
                expandBtn.className = 'ghost-btn small';
                expandBtn.textContent = 'Expand';
                expandBtn.addEventListener('click', () => toggleChatsCard(card, s.sessionId, expandBtn));
                actions.appendChild(expandBtn);
                const selBtn = document.createElement('button');
                selBtn.className = 'ghost-btn small';
                selBtn.textContent = 'Select All in Session';
                selBtn.addEventListener('click', () => selectAllInSession(s.sessionId, card));
                actions.appendChild(selBtn);
                right.appendChild(actions);

                header.appendChild(right);
                card.appendChild(header);

                const chats = document.createElement('div');
                chats.className = 'chats-list';
                chats.style.display = 'none';
                card.appendChild(chats);

                sessionsContainerDiv.appendChild(card);
            });
        }
    }

    // Use the colspan approach: main cell spans 4 columns, action cell on right
    async function toggleChatsRow(sessionRow, sessionId, btn) {
        const next = sessionRow.nextElementSibling;
        if (next && next.classList.contains('session-chats-row') && next.dataset.for === sessionId) {
            next.remove();
            btn.textContent = 'Expand';
            return;
        }

        const loadingRow = document.createElement('tr');
        loadingRow.className = 'session-chats-row';
        loadingRow.dataset.for = sessionId;

        const tdMain = document.createElement('td');
        tdMain.colSpan = 4;
        const tdAction = document.createElement('td');
        tdAction.className = 'session-actions-col';
        tdAction.style.textAlign = 'right';

        tdMain.innerHTML = '<div class="small-note">Loading chats…</div>';

        loadingRow.appendChild(tdMain);
        loadingRow.appendChild(tdAction);

        sessionRow.parentNode.insertBefore(loadingRow, sessionRow.nextSibling);
        btn.textContent = 'Collapse';

        try {
            const json = await safeFetchJson(CHATS_URL + '?sessionId=' + encodeURIComponent(sessionId), { credentials: 'same-origin' });
            renderChatsIntoTd(tdMain, json.rows || []);
        } catch (err) {
            tdMain.innerHTML = `<div class="empty-row">Failed to load chats: ${esc(err.message)}</div>`;
        }
    }

    async function toggleChatsCard(card, sessionId, btn) {
        const chatsEl = card.querySelector('.chats-list');
        if (!chatsEl) return;
        if (chatsEl.style.display === 'block') { chatsEl.style.display = 'none'; btn.textContent = 'Expand'; return; }
        chatsEl.innerHTML = '<div class="small-note">Loading chats…</div>'; chatsEl.style.display = 'block'; btn.textContent = 'Collapse';
        try {
            const json = await safeFetchJson(CHATS_URL + '?sessionId=' + encodeURIComponent(sessionId), { credentials: 'same-origin' });
            renderChatsIntoDiv(chatsEl, json.rows || []);
        } catch (err) {
            chatsEl.innerHTML = `<div class="empty-row">Failed to load chats: ${esc(err.message)}</div>`;
        }
    }

    function renderChatsIntoTd(td, rows) {
        td.innerHTML = '';

        const table = document.createElement('table');
        table.className = 'widget-table inner-chats-table';
        table.style.width = '100%';
        table.style.tableLayout = 'auto';

        const colgroup = document.createElement('colgroup');
        colgroup.innerHTML = '<col style="width:68px"><col style="width:240px"><col><col style="width:160px">';
        table.appendChild(colgroup);

        const thead = document.createElement('thead');
        thead.innerHTML = '<tr><th class="select-column">Select</th><th class="chat-id-cell">Chat ID</th><th>Prompt</th><th>Created At</th></tr>';
        table.appendChild(thead);

        const tbody = document.createElement('tbody');

        if (!rows.length) {
            const r = document.createElement('tr');
            r.innerHTML = '<td colspan="4" class="empty-row">No chats in this session.</td>';
            tbody.appendChild(r);
        } else {
            rows.forEach(row => {
                const r = document.createElement('tr');

                const tdCheck = document.createElement('td');
                tdCheck.className = 'select-column';
                const cb = document.createElement('input');
                cb.type = 'checkbox';
                cb.className = 'chat-checkbox';
                cb.dataset.chatId = String(row.chatId || '');
                cb.checked = selectedChatIds.has(row.chatId);
                cb.addEventListener('change', () => {
                    const id = cb.dataset.chatId; if (!id) return;
                    if (cb.checked) selectedChatIds.add(id); else selectedChatIds.delete(id);
                    updateSelectionInfo();
                });
                tdCheck.appendChild(cb);
                r.appendChild(tdCheck);

                const tdId = document.createElement('td');
                tdId.className = 'chat-id-cell';
                tdId.textContent = row.chatId || '';
                r.appendChild(tdId);

                const tdPrompt = document.createElement('td');
                tdPrompt.textContent = row.prompt || '';
                r.appendChild(tdPrompt);

                const tdCreated = document.createElement('td');
                tdCreated.textContent = row.createdAt ? fmt(row.createdAt) : '';
                r.appendChild(tdCreated);

                tbody.appendChild(r);
            });
        }

        table.appendChild(tbody);

        const wrapper = document.createElement('div');
        wrapper.className = 'widget-review-table';
        wrapper.appendChild(table);

        td.appendChild(wrapper);
    }

    function renderChatsIntoDiv(container, rows) {
        container.innerHTML = '';
        if (!rows.length) { container.innerHTML = '<div class="empty-row">No chats in this session.</div>'; return; }
        rows.forEach(row => {
            const wrap = document.createElement('div');
            wrap.style.display = 'flex';
            wrap.style.gap = '12px';
            wrap.style.alignItems = 'flex-start';
            wrap.style.padding = '8px 0';
            wrap.style.borderBottom = '1px solid #f3f4f6';

            const left = document.createElement('div'); left.style.flex = '0 0 80px';
            const cb = document.createElement('input'); cb.type = 'checkbox'; cb.className = 'chat-checkbox'; cb.dataset.chatId = String(row.chatId || '');
            cb.checked = selectedChatIds.has(row.chatId);
            cb.addEventListener('change', () => { const id = cb.dataset.chatId; if (!id) return; if (cb.checked) selectedChatIds.add(id); else selectedChatIds.delete(id); updateSelectionInfo(); });
            left.appendChild(cb); wrap.appendChild(left);

            const mid = document.createElement('div'); mid.style.flex = '1 1 auto'; mid.style.minWidth = '0';
            const idDiv = document.createElement('div'); idDiv.style.fontFamily = "Menlo, Monaco, 'Courier New', monospace"; idDiv.style.fontWeight = '600'; idDiv.textContent = row.chatId || '';
            const promptDiv = document.createElement('div'); promptDiv.className = 'text-summary'; promptDiv.style.marginTop = '4px'; promptDiv.textContent = row.prompt || '';
            mid.appendChild(idDiv); mid.appendChild(promptDiv); wrap.appendChild(mid);

            const right = document.createElement('div'); right.style.flex = '0 0 160px'; right.style.color = '#6b7280'; right.textContent = row.createdAt ? fmt(row.createdAt) : '';
            wrap.appendChild(right);

            container.appendChild(wrap);
        });
    }

    async function selectAllInSession(sessionId, contextElement) {
        if (!confirm('Select all chats in this session for review?')) return;
        let loading;
        if (contextElement) { loading = document.createElement('span'); loading.textContent = ' Selecting…'; contextElement.appendChild(loading); }
        try {
            const json = await safeFetchJson(CHATS_URL + '?sessionId=' + encodeURIComponent(sessionId), { credentials: 'same-origin' });
            (json.rows || []).forEach(r => { if (r && r.chatId) selectedChatIds.add(String(r.chatId)); });
            document.querySelectorAll('.chat-checkbox').forEach(cb => { const id = cb.dataset.chatId; if (id && selectedChatIds.has(id)) cb.checked = true; });
            updateSelectionInfo();
            alert(`Selected ${json.rows ? json.rows.length : 0} chats from session.`);
        } catch (err) {
            alert('Unable to select session chats: ' + err.message);
            console.error(err);
        } finally {
            if (loading && loading.parentNode) loading.remove();
        }
    }

    async function selectAllAcrossAllSessions() {
        if (!confirm('Select ALL chats from ALL sessions? This may take a while. Continue?')) return;
        if (selectAllAllSessionsBtn) { selectAllAllSessionsBtn.disabled = true; selectAllAllSessionsBtn.textContent = 'Selecting…'; }
        try {
            const sesRes = await safeFetchJson(DATA_URL + '?all=true', { credentials: 'same-origin' });
            const sessions = sesRes.sessions || [];
            for (const s of sessions) {
                try {
                    const j = await safeFetchJson(CHATS_URL + '?sessionId=' + encodeURIComponent(s.sessionId), { credentials: 'same-origin' });
                    (j.rows || []).forEach(r => { if (r && r.chatId) selectedChatIds.add(String(r.chatId)); });
                } catch (err) {
                    console.warn('Failed fetch chats for session', s.sessionId, err);
                }
            }
            document.querySelectorAll('.chat-checkbox').forEach(cb => { const id = cb.dataset.chatId; if (id && selectedChatIds.has(id)) cb.checked = true; });
            updateSelectionInfo();
            alert(`Selected ${selectedChatIds.size} chats across ${sessions.length} sessions.`);
        } catch (err) {
            alert('Unable to select all sessions: ' + err.message);
            console.error(err);
        } finally {
            if (selectAllAllSessionsBtn) { selectAllAllSessionsBtn.disabled = false; selectAllAllSessionsBtn.textContent = 'Select all chats across all sessions'; }
        }
    }

    function renderPagination() {
        if (!summaryEl) return;
        if (!paginationEl) {
            paginationEl = document.createElement('div');
            paginationEl.className = 'pagination-wrapper';
            paginationEl.style.marginTop = '12px';
            summaryEl.parentNode.insertBefore(paginationEl, summaryEl.nextSibling);
        }
        paginationEl.innerHTML = '';
        if (!totalPages || totalPages <= 1) return;

        const controls = document.createElement('div');
        controls.className = 'pagination-controls';
        const prev = document.createElement('button'); prev.className = 'ghost-btn small'; prev.textContent = 'Previous'; prev.disabled = page <= 1;
        prev.addEventListener('click', () => { if (page > 1) { page--; loadSessions(page, searchInput ? (searchInput.value || '') : ''); } });
        const next = document.createElement('button'); next.className = 'ghost-btn small'; next.textContent = 'Next'; next.disabled = page >= totalPages;
        next.addEventListener('click', () => { if (page < totalPages) { page++; loadSessions(page, searchInput ? (searchInput.value || '') : ''); } });
        const info = document.createElement('div'); info.className = 'page-info'; info.style.marginLeft = '12px';
        info.textContent = `Page ${page} of ${totalPages} (${totalSessions} sessions)`;
        controls.appendChild(prev); controls.appendChild(next); controls.appendChild(info);
        paginationEl.appendChild(controls);
    }

    function updateSelectionInfo() {
        if (!selectionInfo || !reviewSelectedBtn) return;
        const n = selectedChatIds.size;
        selectionInfo.textContent = n ? `${n} selected` : '';
        reviewSelectedBtn.disabled = n === 0;
    }

    document.addEventListener('DOMContentLoaded', () => {
        sessionsTableBody = document.getElementById('sessionsBody');
        sessionsTableEl = document.getElementById('sessionsTable');
        sessionsContainerDiv = document.getElementById('sessions') || document.getElementById('sessionsContainer') || null;
        summaryEl = document.getElementById('summary');
        searchInput = document.getElementById('searchInput');
        searchBtn = document.getElementById('searchBtn');
        refreshBtn = document.getElementById('refreshBtn'); // Clear button
        viewAllBtn = document.getElementById('viewAllBtn');
        reviewSelectedBtn = document.getElementById('reviewSelectedBtn');
        selectionInfo = document.getElementById('selectionInfo');

        if (reviewSelectedBtn && reviewSelectedBtn.parentNode) {
            selectAllAllSessionsBtn = document.createElement('button');
            selectAllAllSessionsBtn.className = 'ghost-btn';
            selectAllAllSessionsBtn.textContent = 'Select all chats across all sessions';
            selectAllAllSessionsBtn.style.marginLeft = '8px';
            selectAllAllSessionsBtn.addEventListener('click', selectAllAcrossAllSessions);
            reviewSelectedBtn.parentNode.insertBefore(selectAllAllSessionsBtn, reviewSelectedBtn.nextSibling);
        }

        if (!sessionsTableBody && !sessionsContainerDiv) {
            console.warn('all_sessions.js: no sessions container found in DOM');
            return;
        }

        if (searchBtn && searchInput) {
            searchBtn.addEventListener('click', () => { page = 1; loadSessions(1, searchInput.value.trim()); });
            searchInput.addEventListener('keydown', e => { if (e.key === 'Enter') { page = 1; loadSessions(1, searchInput.value.trim()); } });
        }

        // Clear button: clears the search input and reloads all sessions
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                if (searchInput) {
                    searchInput.value = '';
                }
                page = 1;
                loadSessions(1, '');
            });
        }

        if (viewAllBtn) viewAllBtn.addEventListener('click', () => { page = 1; loadSessions(page, searchInput ? (searchInput.value || '') : ''); });

        if (reviewSelectedBtn) {
            reviewSelectedBtn.addEventListener('click', async () => {
                const selected = Array.from(selectedChatIds);
                if (!selected.length) return alert('No chats selected.');
                try {
                    const res = await fetch(SELECT_URL, {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ selectedChatIds: selected })
                    });
                    if (!res.ok) {
                        const txt = await res.text().catch(() => '');
                        throw new Error(txt || `HTTP ${res.status}`);
                    }
                    const json = await res.json();
                    if (json.status === 'ok' && json.selectionId) {
                        window.location.href = APP + '/dashboard/widgets/drilldown/review?selectionId=' + encodeURIComponent(json.selectionId);
                    } else {
                        throw new Error(json.message || 'Unable to create selection');
                    }
                } catch (err) {
                    alert('Unable to create selection: ' + err.message);
                    console.error(err);
                }
            });
        }

        const initial = window.ALL_SESSIONS_INITIAL || { all: true, page: 1, limit: PAGE_SIZE };
        page = initial.page || 1;
        loadSessions(page, '');
    });

})();
