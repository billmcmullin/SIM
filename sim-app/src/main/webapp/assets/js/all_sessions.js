(function () {
    const APP = window.APP_CONTEXT_PATH || '';
    const API_BASE = APP + '/dashboard/sessions';
    const DATA_URL = API_BASE + '/data';
    const CHATS_URL = API_BASE + '/chats';
    const SELECT_URL = API_BASE + '/select';
    const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

    let page = 1;
    let totalPages = 1;
    let totalSessions = 0;
    let pageSize = 10;
    let widgetNamesMap = {};
    const selectedChatIds = new Set();

    const urlParams = new URLSearchParams(window.location.search);
    let activityFilter = (urlParams.get('activity') || 'all').toLowerCase();

    // Single toggle filter (requested behavior):
    // OFF => show all sessions
    // ON  => show only sessions that have friendly name and/or email
    let labeledOnly = (urlParams.get('labeledOnly') || 'false').toLowerCase() === 'true';

    // Inactive is handled by dedicated page
    if (activityFilter === 'inactive') {
        window.location.href = APP + '/dashboard/inactive-users';
        return;
    }
    if (!['all', 'active'].includes(activityFilter)) {
        activityFilter = 'all';
    }

    let activeDaysFilter = parseInt(urlParams.get('activeDays') || '7', 10);
    if (!Number.isFinite(activeDaysFilter) || activeDaysFilter < 1) {
        activeDaysFilter = 7;
    }

    let sessionsTableBody = null;
    let sessionsContainerDiv = null;
    let summaryEl = null;
    let paginationEl = null;
    let searchInput = null;
    let searchBtn = null;
    let refreshBtn = null;
    let viewAllBtn = null;
    let reviewSelectedBtn = null;
    let deselectSelectedBtn = null;
    let selectionInfo = null;
    let selectAllAllSessionsBtn = null;

    // Filter UI
    let activityFilterBadge = null;
    let showAllUsersBtn = null;
    let showActiveUsersBtn = null;
    let toggleLabeledOnlyBtn = null;

    const esc = s => ((s === null || s === undefined) ? '' : String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;'));

    const fmt = ts => {
        if (!ts) {
            return '';
        }
        try { return new Date(ts).toLocaleString(); } catch { return ts; }
    };

    function customerProfileUrl(sessionId, friendlyName) {
        const p = new URLSearchParams();
        if (sessionId) {
            p.set('sessionId', String(sessionId));
        } else if (friendlyName) {
            p.set('friendlyName', String(friendlyName));
        } else {
            return '';
        }
        return APP + '/customer-profile?' + p.toString();
    }

    function appendProfileLink(container, text, sessionId, friendlyName) {
        const label = (text === null || text === undefined) ? '' : String(text).trim();
        if (!label) {
            return;
        }
        const href = customerProfileUrl(sessionId, friendlyName);
        if (!href) {
            container.textContent = label;
            return;
        }
        const a = document.createElement('a');
        a.href = href;
        a.className = 'customer-profile-link';
        a.textContent = label;
        container.appendChild(a);
    }

    function renderSessionLabel(cell, label, sessionId) {
        if (!cell) {
            return;
        }
        cell.innerHTML = '';

        const primary = document.createElement('div');
        appendProfileLink(primary, label || sessionId || '', sessionId, label);
        cell.appendChild(primary);

        if (sessionId && label && label !== sessionId) {
            const muted = document.createElement('div');
            muted.className = 'session-id-muted';
            appendProfileLink(muted, sessionId, sessionId, label);
            cell.appendChild(muted);
        }
    }

    async function safeFetchJson(url, opts = {}) {
        const res = await fetch(url, opts);
        if (!res.ok) {
            const txt = await res.text().catch(() => '');
            throw new Error(`HTTP ${res.status} ${res.statusText} ${txt ? '- ' + txt.slice(0, 300) : ''}`);
        }
        const ct = res.headers.get('content-type') || '';
        const text = await res.text();

        try {
            return JSON.parse(text);
        } catch {
            throw new Error(`Non-JSON response received (content-type: ${ct || 'unknown'})`);
        }
    }

    function activityLabel() {
        if (activityFilter === 'active') {
            return `Active (last ${activeDaysFilter} days)`;
        }
        return 'All';
    }

    function labeledOnlyLabel() {
        return labeledOnly ? 'Only labeled users' : 'All users';
    }

    function refreshActivityUi() {
        if (activityFilterBadge) {
            const parts = [];
            if (activityFilter === 'active') {
                parts.push(`Active filter applied (last ${activeDaysFilter} days)`);
            }
            if (labeledOnly) {
                parts.push('Only labeled users');
            }

            if (parts.length) {
                activityFilterBadge.textContent = parts.join(' • ');
                activityFilterBadge.style.visibility = 'visible';
            } else {
                activityFilterBadge.textContent = 'Filters applied';
                activityFilterBadge.style.visibility = 'hidden';
            }

        }

        // FIX: keep border width constant so buttons do not shift layout when toggled
        const activeColor = '#1d4ed8';
        const inactiveColor = 'transparent';

        if (showAllUsersBtn) {
            showAllUsersBtn.style.border = '1px solid';
            showAllUsersBtn.style.borderColor = activityFilter === 'all' ? activeColor : inactiveColor;
        }

        if (showActiveUsersBtn) {
            showActiveUsersBtn.style.border = '1px solid';
            showActiveUsersBtn.style.borderColor = activityFilter === 'active' ? activeColor : inactiveColor;
        }

        if (toggleLabeledOnlyBtn) {
            toggleLabeledOnlyBtn.textContent = `Only labeled users: ${labeledOnly ? 'On' : 'Off'}`;
            toggleLabeledOnlyBtn.setAttribute('aria-pressed', labeledOnly ? 'true' : 'false');
            toggleLabeledOnlyBtn.style.border = '1px solid';
            toggleLabeledOnlyBtn.style.borderColor = labeledOnly ? activeColor : inactiveColor;
        }
    }

    function syncFiltersToUrl() {
        const u = new URL(window.location.href);

        if (activityFilter === 'all') {
            u.searchParams.delete('activity');
            u.searchParams.delete('activeDays');
        } else {
            u.searchParams.set('activity', activityFilter);
            u.searchParams.set('activeDays', String(activeDaysFilter));
        }

        if (labeledOnly) {
            u.searchParams.set('labeledOnly', 'true');
        } else {
            u.searchParams.delete('labeledOnly');
        }

        window.history.replaceState({}, '', u.toString());
    }

    function setActivityFilter(next) {
        if (!['all', 'active'].includes(next)) {
            next = 'all';
        }
        activityFilter = next;

        syncFiltersToUrl();
        page = 1;
        refreshActivityUi();
        loadSessions(1, searchInput ? (searchInput.value || '').trim() : '');
    }

    function setLabeledOnly(next) {
        labeledOnly = !!next;

        syncFiltersToUrl();
        page = 1;
        refreshActivityUi();
        loadSessions(1, searchInput ? (searchInput.value || '').trim() : '');
    }

    async function loadSessions(reqPage = 1, search = '') {
        if (!sessionsTableBody && !sessionsContainerDiv) {
            return;
        }
        if (sessionsTableBody) {
            sessionsTableBody.innerHTML = '<tr><td colspan="5" class="small-note">Loading sessions…</td></tr>';
        }
        if (sessionsContainerDiv) {
            sessionsContainerDiv.innerHTML = '<div class="small-note">Loading sessions…</div>';
        }

        page = Math.max(1, reqPage);
        const params = new URLSearchParams();
        params.set('limit', String(pageSize));
        params.set('page', String(page));
        params.set('activity', activityFilter);
        params.set('activeDays', String(activeDaysFilter));
        params.set('labeledOnly', labeledOnly ? 'true' : 'false');
        if (search) {
            params.set('search', search);
        }

        try {
            const json = await safeFetchJson(DATA_URL + '?' + params.toString(), { credentials: 'same-origin' });
            widgetNamesMap = json.widgetNames || {};
            totalSessions = json.totalSessions || 0;
            totalPages = json.totalPages || 1;
            renderSessions(json.sessions || []);
            renderPagination();

            if (summaryEl) {
                summaryEl.textContent = `Showing ${json.sessions ? json.sessions.length : 0} sessions (page ${page}/${totalPages}). Total sessions: ${totalSessions}. Filter: ${activityLabel()} · ${labeledOnlyLabel()}`;
            }
        } catch (err) {
            const msg = `Unable to load sessions: ${err.message}`;
            if (sessionsTableBody) {
                sessionsTableBody.innerHTML = `<tr><td colspan="5" class="empty-row">${esc(msg)}</td></tr>`;
            }
            if (sessionsContainerDiv) {
                sessionsContainerDiv.innerHTML = `<div class="empty-row">${esc(msg)}</div>`;
            }
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
                const label = s.sessionIdDisplay || s.displayLabel || s.sessionId || '';
                const tr = document.createElement('tr');

                const tdId = document.createElement('td');
                tdId.className = 'session-id-col';
                renderSessionLabel(tdId, label, s.sessionId);
                tr.appendChild(tdId);

                const tdWidgets = document.createElement('td');
                tdWidgets.className = 'session-widgets-col';
                let widgetLabel = '';
                if (Array.isArray(s.widgets) && s.widgets.length) {
                    widgetLabel = s.widgets.map(wid => widgetNamesMap[wid] || wid).filter(Boolean).join(', ');
                }
                tdWidgets.textContent = widgetLabel;
                if (widgetLabel) {
                    tdWidgets.title = widgetLabel;
                }
                tr.appendChild(tdWidgets);

                const tdCount = document.createElement('td');
                tdCount.className = 'session-chats-col';
                tdCount.textContent = String(s.totalCount || 0);
                tr.appendChild(tdCount);

                const tdLast = document.createElement('td');
                tdLast.className = 'session-last-col';
                tdLast.textContent = s.lastSeen ? fmt(s.lastSeen) : '';
                tr.appendChild(tdLast);

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
                selectAllBtn.textContent = 'Select All';
                selectAllBtn.addEventListener('click', () => selectAllInSession(s.sessionId, tr));
                tdActions.appendChild(selectAllBtn);

                tr.appendChild(tdActions);
                sessionsTableBody.appendChild(tr);
            });

            return;
        }

        if (sessionsContainerDiv) {
            sessionsContainerDiv.innerHTML = '';
            if (!Array.isArray(list) || list.length === 0) {
                sessionsContainerDiv.innerHTML = '<div class="empty-row">No sessions found.</div>';
                return;
            }
            list.forEach(s => {
                const label = s.sessionIdDisplay || s.displayLabel || s.sessionId || '';
                const card = document.createElement('div');
                card.className = 'session-card';

                const header = document.createElement('div');
                header.className = 'session-header';

                const left = document.createElement('div');
                left.className = 'session-header-left';

                const title = document.createElement('div');
                title.style.fontWeight = '700';
                appendProfileLink(title, label, s.sessionId, label);
                left.appendChild(title);

                const meta = document.createElement('div');
                meta.className = 'session-meta';
                meta.textContent = `${String(s.totalCount || 0)} chats · Last: ${s.lastSeen ? fmt(s.lastSeen) : ''}`;
                left.appendChild(meta);

                if (s.sessionId && label !== s.sessionId) {
                    const muted = document.createElement('div');
                    muted.className = 'session-id-muted';
                    appendProfileLink(muted, s.sessionId, s.sessionId, label);
                    left.appendChild(muted);
                }
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
                selBtn.textContent = 'Select All';
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

    function toggleChatsRow(sessionRow, sessionId, btn) {
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

        safeFetchJson(CHATS_URL + '?sessionId=' + encodeURIComponent(sessionId), { credentials: 'same-origin' })
            .then(json => renderChatsIntoTd(tdMain, json.rows || []))
            .catch(err => {
                tdMain.innerHTML = `<div class="empty-row">Failed to load chats: ${esc(err.message)}</div>`;
            });
    }

    function toggleChatsCard(card, sessionId, btn) {
        const chatsEl = card.querySelector('.chats-list');
        if (!chatsEl) {
            return;
        }
        if (chatsEl.style.display === 'block') {
            chatsEl.style.display = 'none';
            btn.textContent = 'Expand';
            return;
        }
        chatsEl.innerHTML = '<div class="small-note">Loading chats…</div>';
        chatsEl.style.display = 'block';
        btn.textContent = 'Collapse';
        safeFetchJson(CHATS_URL + '?sessionId=' + encodeURIComponent(sessionId), { credentials: 'same-origin' })
            .then(json => renderChatsIntoDiv(chatsEl, json.rows || []))
            .catch(err => {
                chatsEl.innerHTML = `<div class="empty-row">Failed to load chats: ${esc(err.message)}</div>`;
            });
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
        thead.innerHTML = '<tr><th class="select-column"></th><th class="chat-id-cell">Chat ID</th><th>Prompt</th><th>Created At</th></tr>';
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
                cb.checked = selectedChatIds.has(String(row.chatId || ''));
                cb.addEventListener('change', () => {
                    const id = cb.dataset.chatId;
                    if (!id) {
                        return;
                    }
                    if (cb.checked) {
                        selectedChatIds.add(id);
                    } else {
                        selectedChatIds.delete(id);
                    }
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
        if (!rows.length) {
            container.innerHTML = '<div class="empty-row">No chats in this session.</div>';
            return;
        }
        rows.forEach(row => {
            const wrap = document.createElement('div');
            wrap.style.display = 'flex';
            wrap.style.gap = '12px';
            wrap.style.alignItems = 'flex-start';
            wrap.style.padding = '8px 0';
            wrap.style.borderBottom = '1px solid #f3f4f6';

            const left = document.createElement('div');
            left.style.flex = '0 0 80px';
            const cb = document.createElement('input');
            cb.type = 'checkbox';
            cb.className = 'chat-checkbox';
            cb.dataset.chatId = String(row.chatId || '');
            cb.checked = selectedChatIds.has(String(row.chatId || ''));
            cb.addEventListener('change', () => {
                const id = cb.dataset.chatId;
                if (!id) {
                    return;
                }
                if (cb.checked) {
                    selectedChatIds.add(id);
                } else {
                    selectedChatIds.delete(id);
                }
                updateSelectionInfo();
            });
            left.appendChild(cb);
            wrap.appendChild(left);

            const mid = document.createElement('div');
            mid.style.flex = '1 1 auto';
            mid.style.minWidth = '0';
            const idDiv = document.createElement('div');
            idDiv.style.fontFamily = "Menlo, Monaco, 'Courier New', monospace";
            idDiv.style.fontWeight = '600';
            idDiv.textContent = row.chatId || '';
            const promptDiv = document.createElement('div');
            promptDiv.className = 'text-summary';
            promptDiv.style.marginTop = '4px';
            promptDiv.textContent = row.prompt || '';
            mid.appendChild(idDiv);
            mid.appendChild(promptDiv);
            wrap.appendChild(mid);

            const right = document.createElement('div');
            right.style.flex = '0 0 160px';
            right.style.color = '#6b7280';
            right.textContent = row.createdAt ? fmt(row.createdAt) : '';
            wrap.appendChild(right);

            container.appendChild(wrap);
        });
    }

    async function selectAllInSession(sessionId, contextElement) {
        if (!confirm('Select all chats in this session for review?')) {
            return;
        }
        let loading;
        if (contextElement) {
            loading = document.createElement('span');
            loading.textContent = ' Selecting…';
            contextElement.appendChild(loading);
        }
        try {
            const json = await safeFetchJson(CHATS_URL + '?sessionId=' + encodeURIComponent(sessionId), { credentials: 'same-origin' });
            (json.rows || []).forEach(r => {
                if (r && r.chatId) {
                    selectedChatIds.add(String(r.chatId));
                }
            });
            document.querySelectorAll('.chat-checkbox').forEach(cb => {
                const id = cb.dataset.chatId;
                if (id && selectedChatIds.has(id)) {
                    cb.checked = true;
                }
            });
            updateSelectionInfo();
            alert(`Selected ${json.rows ? json.rows.length : 0} chats from session.`);
        } catch (err) {
            alert('Unable to select session chats: ' + err.message);
            console.error(err);
        } finally {
            if (loading && loading.parentNode) {
                loading.remove();
            }
        }
    }

    async function selectAllAcrossAllSessions() {
        if (!confirm('Select ALL chats from currently filtered sessions? This may take a while. Continue?')) {
            return;
        }
        if (selectAllAllSessionsBtn) {
            selectAllAllSessionsBtn.disabled = true;
            selectAllAllSessionsBtn.textContent = 'Selecting…';
        }
        try {
            const p = new URLSearchParams();
            p.set('all', 'true');
            p.set('activity', activityFilter);
            p.set('activeDays', String(activeDaysFilter));
            p.set('labeledOnly', labeledOnly ? 'true' : 'false');
            if (searchInput && searchInput.value.trim()) {
                p.set('search', searchInput.value.trim());
            }

            const sesRes = await safeFetchJson(DATA_URL + '?' + p.toString(), { credentials: 'same-origin' });
            const sessions = sesRes.sessions || [];
            for (const s of sessions) {
                try {
                    const j = await safeFetchJson(CHATS_URL + '?sessionId=' + encodeURIComponent(s.sessionId), { credentials: 'same-origin' });
                    (j.rows || []).forEach(r => {
                        if (r && r.chatId) {
                            selectedChatIds.add(String(r.chatId));
                        }
                    });
                } catch (err) {
                    console.warn('Failed fetch chats for session', s.sessionId, err);
                }
            }
            document.querySelectorAll('.chat-checkbox').forEach(cb => {
                const id = cb.dataset.chatId;
                if (id && selectedChatIds.has(id)) {
                    cb.checked = true;
                }
            });
            updateSelectionInfo();
            alert(`Selected ${selectedChatIds.size} chats across ${sessions.length} sessions.`);
        } catch (err) {
            alert('Unable to select all sessions: ' + err.message);
            console.error(err);
        } finally {
            if (selectAllAllSessionsBtn) {
                selectAllAllSessionsBtn.disabled = false;
                selectAllAllSessionsBtn.textContent = 'Select all chats';
            }
        }
    }

    function deselectAllSelected() {
        selectedChatIds.clear();
        document.querySelectorAll('.chat-checkbox').forEach(cb => { cb.checked = false; });
        updateSelectionInfo();
    }

    function updateSelectionInfo() {
        if (!selectionInfo) {
            return;
        }
        const n = selectedChatIds.size;
        selectionInfo.textContent = n ? `${n} selected` : '';
        if (reviewSelectedBtn) {
            reviewSelectedBtn.disabled = n === 0;
        }
        if (deselectSelectedBtn) {
            deselectSelectedBtn.disabled = n === 0;
        }
    }

    function renderPagination() {
        if (!summaryEl) {
            return;
        }
        if (!paginationEl) {
            paginationEl = document.createElement('div');
            paginationEl.className = 'pagination-wrapper';
            paginationEl.style.marginTop = '12px';
            summaryEl.parentNode.insertBefore(paginationEl, summaryEl.nextSibling);
        }
        paginationEl.innerHTML = '';

        const controls = document.createElement('div');
        controls.className = 'pagination-controls';
        controls.style.display = 'flex';
        controls.style.alignItems = 'center';
        controls.style.gap = '8px';
        controls.style.flexWrap = 'wrap';

        const prev = document.createElement('button');
        prev.className = 'ghost-btn small';
        prev.textContent = 'Back';
        prev.disabled = page <= 1;
        prev.addEventListener('click', () => {
            if (page > 1) {
                page--;
                loadSessions(page, searchInput ? (searchInput.value || '') : '');
            }
        });

        const next = document.createElement('button');
        next.className = 'ghost-btn small';
        next.textContent = 'Next';
        next.disabled = page >= totalPages;
        next.addEventListener('click', () => {
            if (page < totalPages) {
                page++;
                loadSessions(page, searchInput ? (searchInput.value || '') : '');
            }
        });

        const info = document.createElement('div');
        info.className = 'page-info';
        info.style.fontWeight = '600';
        info.textContent = `Page ${page} of ${totalPages} (${totalSessions} sessions)`;

        const sizeLabel = document.createElement('label');
        sizeLabel.textContent = 'Show per page:';
        sizeLabel.style.margin = '0';
        sizeLabel.style.fontSize = '0.9rem';

        const sizeSelect = document.createElement('select');
        sizeSelect.className = 'ghost-btn small';
        sizeSelect.style.borderRadius = '8px';
        PAGE_SIZE_OPTIONS.forEach(opt => {
            const option = document.createElement('option');
            option.value = String(opt);
            option.textContent = String(opt);
            if (opt === pageSize) {
                option.selected = true;
            }
            sizeSelect.appendChild(option);
        });
        sizeSelect.addEventListener('change', () => {
            pageSize = parseInt(sizeSelect.value, 10) || 10;
            page = 1;
            loadSessions(page, searchInput ? (searchInput.value || '') : '');
        });

        controls.appendChild(prev);
        controls.appendChild(next);
        controls.appendChild(info);
        controls.appendChild(sizeLabel);
        controls.appendChild(sizeSelect);
        paginationEl.appendChild(controls);
    }

    document.addEventListener('DOMContentLoaded', () => {
        sessionsTableBody = document.getElementById('sessionsBody');
        sessionsContainerDiv = document.getElementById('sessions') || document.getElementById('sessionsContainer') || null;
        summaryEl = document.getElementById('summary');
        searchInput = document.getElementById('searchInput');
        searchBtn = document.getElementById('searchBtn');
        refreshBtn = document.getElementById('refreshBtn');
        viewAllBtn = document.getElementById('viewAllBtn');
        reviewSelectedBtn = document.getElementById('reviewSelectedBtn');
        deselectSelectedBtn = document.getElementById('deselectSelectedBtn');
        selectionInfo = document.getElementById('selectionInfo');

        activityFilterBadge = document.getElementById('activityFilterBadge');
        showAllUsersBtn = document.getElementById('showAllUsersBtn');
        showActiveUsersBtn = document.getElementById('showActiveUsersBtn');

        // expected in HTML as a single toggle button
        toggleLabeledOnlyBtn = document.getElementById('toggleLabeledOnlyBtn');

        if (showAllUsersBtn) {
            showAllUsersBtn.addEventListener('click', () => setActivityFilter('all'));
        }
        if (showActiveUsersBtn) {
            showActiveUsersBtn.addEventListener('click', () => setActivityFilter('active'));
        }

        if (toggleLabeledOnlyBtn) {
            toggleLabeledOnlyBtn.addEventListener('click', () => setLabeledOnly(!labeledOnly));
        }

        if (reviewSelectedBtn && reviewSelectedBtn.parentNode) {
            selectAllAllSessionsBtn = document.createElement('button');
            selectAllAllSessionsBtn.className = 'ghost-btn';
            selectAllAllSessionsBtn.textContent = 'Select all chats';
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
            searchInput.addEventListener('keydown', e => {
                if (e.key === 'Enter') { page = 1; loadSessions(1, searchInput.value.trim()); }
            });
        }

        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                if (searchInput) {
                    searchInput.value = '';
                }
                page = 1;
                loadSessions(1, '');
            });
        }

        if (viewAllBtn) {
            viewAllBtn.addEventListener('click', () => {
                setActivityFilter('all');
            });
        }

        if (reviewSelectedBtn) {
            reviewSelectedBtn.addEventListener('click', async () => {
                const selected = Array.from(selectedChatIds);
                if (!selected.length) {
                    alert('No chats selected.');
                } else {
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
                }
            });
        }

        if (deselectSelectedBtn) {
            deselectSelectedBtn.addEventListener('click', deselectAllSelected);
        }

        const initial = window.ALL_SESSIONS_INITIAL || { all: true, page: 1, limit: pageSize };
        page = initial.page || 1;
        pageSize = initial.limit || pageSize;

        if (typeof initial.labeledOnly === 'boolean') {
            labeledOnly = initial.labeledOnly;
        }

        syncFiltersToUrl();
        refreshActivityUi();
        loadSessions(page, '');
    });

})();
