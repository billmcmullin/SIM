// assets/js/customer_profile.js
(function () {
    'use strict';

    window.CustomerProfilePage = window.CustomerProfilePage || {};

    const config = window.customerProfileConfig || {};
    const contextPath = config.contextPath || '';
    const sessionId = config.sessionId || '';
    const displaySessionId = config.displaySessionId || sessionId;
    const friendlyName = config.friendlyName || '';
    const syncEndpoint = `${contextPath}/admin/sync-customer-profile`;
    const translateEndpoint = `${contextPath}/dashboard/widgets/drilldown/review/translate`;
    const chatsEndpoint = `${contextPath}/dashboard/sessions/chats`;
    const selectEndpoint = `${contextPath}/dashboard/sessions/select`;
    const reviewDataEndpoint = `${contextPath}/dashboard/widgets/drilldown/view/review-data`;
    const reviewPageEndpoint = `${contextPath}/dashboard/widgets/drilldown/review`;
    const sessionLabelEndpoint = `${contextPath}/dashboard/session-names/label`;
    const sessionNamesEndpoint = `${contextPath}/dashboard/session-names.json`;

    const CHAT_PAGE_SIZE = 10;
    const SESSION_ID_PATTERN = /^[A-Za-z0-9._:-]{1,128}$/;

    const state = {
        activeSessionId: '',
        profileSessionIds: [],
        sessionLabelEmail: '',
        sessionLabelLoaded: false,
        chatRows: [],
        filteredChatRows: [],
        chatPage: 1,
        selectedChatIds: new Set(),
        activeDetailChatId: '',
        detailByChatId: new Map()
    };

    document.addEventListener('DOMContentLoaded', () => {
        bindProfileLinks();
        bindSyncButton();
        hydrateHeader();
        state.activeSessionId = resolveActiveSessionId();
        bindFriendlyNameForm();
        bindChatsTable();
        bindDetailCardActions();
        initializeSubmittedChats();
    });

    function hydrateHeader() {
        const sessionEl = document.getElementById('cpSessionId');
        const friendlyEl = document.getElementById('cpFriendlyName');
        const friendlyInput = document.getElementById('cpFriendlyNameInput');

        if (sessionEl && displaySessionId) {
            sessionEl.textContent = displaySessionId;
        }
        if (friendlyEl && friendlyName) {
            friendlyEl.textContent = friendlyName;
        }
        if (friendlyInput && friendlyName && friendlyName !== '-') {
            friendlyInput.value = friendlyName;
        }

        // Normalize server-rendered timestamps to browser local time.
        setText('cpLastSyncedAt', document.getElementById('cpLastSyncedAt')?.textContent, true);
        normalizeLinkedSessionUpdatedAt();
    }

    function bindSyncButton() {
        const btn = document.getElementById('syncCustomerProfileBtn');
        if (!btn) {
            return;
        }

        btn.addEventListener('click', async () => {
            const sid = getSessionIdFromPage();
            if (!sid) {
                setStatus('Missing session id for sync.', false);
                return;
            }

            const original = btn.textContent;
            btn.disabled = true;
            btn.textContent = 'Syncing...';

            try {
                const payload = await postUrlEncoded(syncEndpoint, new URLSearchParams({ sessionId: sid }));

                if (payload.ok && payload.data?.status === 'ok') {
                    setStatus(payload.data.message || 'Customer profile synced successfully.', true);

                    // Update visible fields if returned by server
                    if (payload.data.profile) {
                        patchProfileFields(payload.data.profile);
                    }

                    // Optional full refresh if server indicates
                    if (payload.data.refresh === true) {
                        window.location.reload();
                    }
                } else {
                    setStatus(payload.data?.message || 'Sync failed.', false);
                }
            } catch (e) {
                setStatus(`Sync error: ${e.message}`, false);
            } finally {
                btn.disabled = false;
                btn.textContent = original || 'Sync from Salesforce';
            }
        });
    }

    function patchProfileFields(profile) {
        setText('cpFriendlyName', profile.friendlyName);
        const friendlyInput = document.getElementById('cpFriendlyNameInput');
        if (friendlyInput && profile.friendlyName) {
            friendlyInput.value = String(profile.friendlyName);
        }
        setText('cpEmail', profile.email);
        setText('cpPhone', profile.phone);
        setText('cpTitle', profile.title);
        setText('cpDepartment', profile.department);
        setText('cpSalesforceContactId', profile.salesforceContactId);
        setText('cpSalesforceAccountId', profile.salesforceAccountId);
        setText('cpLastSyncedAt', profile.lastSyncedAt, true);
        normalizeLinkedSessionUpdatedAt();
    }

    function setText(id, value, isDateTime = false) {
        const el = document.getElementById(id);
        if (!el) {
            return;
        }
        if (value === null || value === undefined || value === '') {
            el.textContent = '-';
            return;
        }
        if (isDateTime) {
            el.textContent = formatDateTime(value, '-');
            return;
        }
        el.textContent = String(value);
    }

    function formatDateTime(value, fallback = '-') {
        if (value === null || value === undefined || value === '') {
            return fallback;
        }

        const raw = String(value).trim();
        if (!raw || raw === '-' || raw.toLowerCase() === 'never') {
            return raw || fallback;
        }

        const d = new Date(raw);
        if (Number.isNaN(d.getTime())) {
            return raw;
        }

        const formatted = d.toLocaleString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: 'numeric',
            minute: '2-digit',
            hour12: true
        });
        return formatted;
    }

    function normalizeLinkedSessionUpdatedAt() {
        const table = document.querySelector('.customer-profile-linked-table');
        if (!table) {
            return;
        }
        const rows = table.querySelectorAll('tbody tr td:nth-child(4)');
        rows.forEach((cell) => {
            if (!cell) {
                return;
            }
            cell.textContent = formatDateTime(cell.textContent, '-');
        });
    }

    function setStatus(message, success) {
        const el = document.getElementById('customerProfileSyncStatus');
        if (!el) {
            return;
        }
        el.textContent = message;
        el.style.color = success ? '#047857' : '#b91c1c';
    }

    function getSessionIdFromPage() {
        if (state.activeSessionId) {
            return state.activeSessionId;
        }
        const hidden = document.getElementById('cpSessionIdInput');
        const hiddenValue = hidden && hidden.value ? hidden.value.trim() : '';
        if (isValidSessionId(hiddenValue)) {
            return hiddenValue;
        }
        return '';
    }

    async function postUrlEncoded(url, params) {
        const res = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json'
            },
            credentials: 'same-origin',
            body: params.toString()
        });

        let data = null;
        try {
            data = await res.json();
        } catch {
            data = { status: 'error', message: `Unexpected response (${res.status})` };
        }

        return { ok: res.ok, data };
    }

    async function fetchJson(url, init) {
        const response = await fetch(url, init);
        const text = await response.text();
        let json = {};
        if (text) {
            try {
                json = JSON.parse(text);
            } catch {
                throw new Error(`Invalid JSON response (${response.status})`);
            }
        }
        if (!response.ok) {
            throw new Error(json.message || `HTTP ${response.status}`);
        }
        return json;
    }

    function resolveActiveSessionId() {
        const candidates = [
            sessionId,
            (document.getElementById('cpSessionIdInput')?.value || '').trim(),
            findFirstLinkedSessionId()
        ];

        for (const candidate of candidates) {
            if (isValidSessionId(candidate)) {
                return candidate;
            }
        }
        return '';
    }

    function findFirstLinkedSessionId() {
        const link = document.querySelector('.customer-profile-linked-table tbody tr td:first-child a');
        if (!link || !link.textContent) {
            return '';
        }
        const sid = link.textContent.trim();
        return isValidSessionId(sid) ? sid : '';
    }

    function isValidSessionId(value) {
        if (!value) {
            return false;
        }
        return SESSION_ID_PATTERN.test(String(value).trim());
    }

    function bindFriendlyNameForm() {
        const form = document.getElementById('cpFriendlyNameForm');
        const input = document.getElementById('cpFriendlyNameInput');
        const button = document.getElementById('cpFriendlyNameSaveBtn');
        const status = document.getElementById('cpFriendlyNameStatus');

        if (!form || !input || !button || !status) {
            return;
        }

        if (!state.activeSessionId) {
            input.disabled = true;
            button.disabled = true;
            setFriendlyStatus('Friendly name updates require a valid session ID.', false);
            return;
        }

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            const nextName = input.value.trim();
            if (!nextName) {
                setFriendlyStatus('Friendly name is required.', false);
                return;
            }

            button.disabled = true;
            setFriendlyStatus('Saving friendly name...', true);

            try {
                await ensureSessionLabelDataLoaded(state.activeSessionId);
                const params = new URLSearchParams();
                params.set('sessionId', state.activeSessionId);
                params.set('displayName', nextName);
                params.set('email', state.sessionLabelEmail || '');

                const payload = await postUrlEncoded(sessionLabelEndpoint, params);
                if (!payload.ok || payload.data?.status !== 'ok') {
                    throw new Error(payload.data?.message || 'Unable to save friendly name.');
                }

                setText('cpFriendlyName', nextName);
                config.friendlyName = nextName;
                updateLinkedSessionFriendlyName(state.activeSessionId, nextName);
                setFriendlyStatus('Friendly name saved.', true);
            } catch (err) {
                setFriendlyStatus(err.message || 'Unable to save friendly name.', false);
            } finally {
                button.disabled = false;
            }
        });
    }

    async function ensureSessionLabelDataLoaded(sid) {
        if (state.sessionLabelLoaded || !sid) {
            return;
        }

        try {
            const params = new URLSearchParams();
            params.set('q', sid);
            params.set('limit', '20');
            params.set('page', '1');

            const payload = await fetchJson(`${sessionNamesEndpoint}?${params.toString()}`, {
                method: 'GET',
                credentials: 'same-origin',
                headers: { Accept: 'application/json' }
            });

            if (Array.isArray(payload.sessions)) {
                const exact = payload.sessions.find((entry) => String(entry.sessionId || '').trim() === sid);
                if (exact) {
                    state.sessionLabelEmail = String(exact.email || '');
                    if ((!friendlyName || friendlyName === '-') && exact.displayName) {
                        setText('cpFriendlyName', exact.displayName);
                    }
                }
            }
        } catch {
            state.sessionLabelEmail = '';
        } finally {
            state.sessionLabelLoaded = true;
        }
    }

    function setFriendlyStatus(message, success) {
        const el = document.getElementById('cpFriendlyNameStatus');
        if (!el) {
            return;
        }
        el.textContent = message;
        el.style.color = success ? '#047857' : '#b91c1c';
    }

    function updateLinkedSessionFriendlyName(sid, name) {
        const rows = document.querySelectorAll('.customer-profile-linked-table tbody tr');
        rows.forEach((row) => {
            const sidCell = row.querySelector('td:nth-child(1)');
            const nameCell = row.querySelector('td:nth-child(2)');
            if (!sidCell || !nameCell) {
                return;
            }
            const currentSid = (sidCell.textContent || '').trim();
            if (currentSid === sid) {
                nameCell.textContent = name;
            }
        });
    }

    function initializeSubmittedChats() {
        state.profileSessionIds = collectProfileSessionIds();
        if (!state.profileSessionIds.length) {
            setChatsStatus('Submitted chats are unavailable without a valid session ID.', false);
            renderNoChats('No valid session ID available.');
            disableChatActionButtons(true);
            return;
        }

        const scopeNote = document.getElementById('cpChatsScopeNote');
        if (scopeNote) {
            if (state.profileSessionIds.length === 1) {
                scopeNote.textContent = `Reviewing chats for session: ${state.profileSessionIds[0]}`;
            } else {
                scopeNote.textContent = `Reviewing chats across ${state.profileSessionIds.length} linked sessions.`;
            }
        }

        loadSubmittedChats();
    }

    function collectProfileSessionIds() {
        const ids = new Set();
        const primary = getSessionIdFromPage();
        if (primary) {
            ids.add(primary);
        }

        const linkedRows = document.querySelectorAll('.customer-profile-linked-table tbody tr td:first-child');
        linkedRows.forEach((cell) => {
            const candidate = String(cell.textContent || '').trim();
            if (isValidSessionId(candidate)) {
                ids.add(candidate);
            }
        });

        return Array.from(ids);
    }

    function bindChatsTable() {
        const searchBtn = document.getElementById('cpChatSearchBtn');
        const clearBtn = document.getElementById('cpChatClearSearchBtn');
        const searchInput = document.getElementById('cpChatSearchInput');
        const prevBtn = document.getElementById('cpChatPrevBtn');
        const nextBtn = document.getElementById('cpChatNextBtn');
        const selectVisibleBtn = document.getElementById('cpChatSelectVisibleBtn');
        const deselectBtn = document.getElementById('cpChatDeselectBtn');
        const reviewSelectedBtn = document.getElementById('cpChatReviewSelectedBtn');
        const reviewAllBtn = document.getElementById('cpChatReviewAllBtn');
        const selectAllVisible = document.getElementById('cpChatsSelectAllVisible');

        if (searchBtn && searchInput) {
            searchBtn.addEventListener('click', () => applyChatFilter(searchInput.value));
            searchInput.addEventListener('keydown', (event) => {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    applyChatFilter(searchInput.value);
                }
            });
        }

        if (clearBtn && searchInput) {
            clearBtn.addEventListener('click', () => {
                searchInput.value = '';
                applyChatFilter('');
            });
        }

        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                state.chatPage = Math.max(1, state.chatPage - 1);
                renderSubmittedChats();
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                const totalPages = Math.max(1, Math.ceil(state.filteredChatRows.length / CHAT_PAGE_SIZE));
                state.chatPage = Math.min(totalPages, state.chatPage + 1);
                renderSubmittedChats();
            });
        }

        if (selectVisibleBtn) {
            selectVisibleBtn.addEventListener('click', () => {
                const visible = getCurrentChatPageRows();
                visible.forEach((row) => state.selectedChatIds.add(String(row.chatId || '')));
                renderSubmittedChats();
            });
        }

        if (deselectBtn) {
            deselectBtn.addEventListener('click', () => {
                state.selectedChatIds.clear();
                renderSubmittedChats();
            });
        }

        if (reviewSelectedBtn) {
            reviewSelectedBtn.addEventListener('click', async () => {
                const selected = Array.from(state.selectedChatIds).filter(Boolean);
                if (!selected.length) {
                    alert('Select at least one chat to review.');
                    return;
                }

                try {
                    const selectionId = await createSelection(selected);
                    window.location.href = `${reviewPageEndpoint}?selectionId=${encodeURIComponent(selectionId)}`;
                } catch (err) {
                    alert(err.message || 'Unable to open review page.');
                }
            });
        }

        if (reviewAllBtn) {
            reviewAllBtn.addEventListener('click', async () => {
                if (!state.chatRows.length) {
                    alert('No chats available for review.');
                    return;
                }

                try {
                    const allIds = state.chatRows
                        .map((row) => String(row.chatId || '').trim())
                        .filter(Boolean);
                    const selectionId = await createSelection(allIds);
                    window.location.href = `${reviewPageEndpoint}?selectionId=${encodeURIComponent(selectionId)}`;
                } catch (err) {
                    alert(err.message || 'Unable to open review page.');
                }
            });
        }

        if (selectAllVisible) {
            selectAllVisible.addEventListener('change', () => {
                const checked = !!selectAllVisible.checked;
                getCurrentChatPageRows().forEach((row) => {
                    const chatId = String(row.chatId || '');
                    if (!chatId) {
                        return;
                    }
                    if (checked) {
                        state.selectedChatIds.add(chatId);
                    } else {
                        state.selectedChatIds.delete(chatId);
                    }
                });
                renderSubmittedChats();
            });
        }
    }

    async function loadSubmittedChats() {
        if (!state.profileSessionIds.length) {
            return;
        }

        setChatsStatus('Loading submitted chats...', true);
        disableChatActionButtons(true);

        try {
            const chatResults = await Promise.all(
                state.profileSessionIds.map(async (sid) => {
                    const payload = await fetchJson(`${chatsEndpoint}?sessionId=${encodeURIComponent(sid)}`, {
                        method: 'GET',
                        credentials: 'same-origin',
                        headers: { Accept: 'application/json' }
                    });
                    return {
                        sid,
                        rows: Array.isArray(payload.rows) ? payload.rows : []
                    };
                })
            );

            state.chatRows = chatResults.flatMap((group) =>
                group.rows.map((row) => ({
                    chatId: String(row.chatId || ''),
                    sessionId: group.sid,
                    prompt: String(row.prompt || ''),
                    createdAt: String(row.createdAt || '')
                }))
            );
            state.filteredChatRows = state.chatRows.slice();
            state.chatPage = 1;
            state.selectedChatIds.clear();

            if (!state.chatRows.length) {
                setChatsStatus('No submitted chats were found for this session.', true);
                renderNoChats('No submitted chats found.');
                return;
            }

            setChatsStatus(`Loaded ${state.chatRows.length} chat entries from ${state.profileSessionIds.length} session(s).`, true);
            renderSubmittedChats();
        } catch (err) {
            setChatsStatus(`Unable to load chats: ${err.message}`, false);
            renderNoChats('Unable to load submitted chats.');
        } finally {
            disableChatActionButtons(false);
        }
    }

    function applyChatFilter(rawQuery) {
        const query = String(rawQuery || '').trim().toLowerCase();
        if (!query) {
            state.filteredChatRows = state.chatRows.slice();
        } else {
            state.filteredChatRows = state.chatRows.filter((row) => {
                const haystack = `${row.chatId} ${row.sessionId || ''} ${row.prompt}`.toLowerCase();
                return haystack.includes(query);
            });
        }
        state.chatPage = 1;
        renderSubmittedChats();
    }

    function renderSubmittedChats() {
        const tbody = document.getElementById('cpSubmittedChatsBody');
        const pageInfo = document.getElementById('cpChatPageInfo');
        const prevBtn = document.getElementById('cpChatPrevBtn');
        const nextBtn = document.getElementById('cpChatNextBtn');

        if (!tbody) {
            return;
        }

        if (!state.filteredChatRows.length) {
            renderNoChats('No chats match your search.');
            if (pageInfo) {
                pageInfo.textContent = '0 results';
            }
            if (prevBtn) {
                prevBtn.disabled = true;
            }
            if (nextBtn) {
                nextBtn.disabled = true;
            }
            updateChatSelectionInfo();
            syncSelectAllVisibleState();
            return;
        }

        const totalPages = Math.max(1, Math.ceil(state.filteredChatRows.length / CHAT_PAGE_SIZE));
        state.chatPage = Math.min(Math.max(1, state.chatPage), totalPages);

        const start = (state.chatPage - 1) * CHAT_PAGE_SIZE;
        const pageRows = state.filteredChatRows.slice(start, start + CHAT_PAGE_SIZE);

        tbody.innerHTML = pageRows.map((row) => {
            const chatId = escapeHtml(row.chatId || '');
            const promptSummary = escapeHtml((row.prompt || '').slice(0, 220));
            const checked = state.selectedChatIds.has(String(row.chatId || '')) ? 'checked' : '';
            const rowClass = state.activeDetailChatId === row.chatId ? 'row-active' : '';

            return `
                <tr class="${rowClass}" data-chat-id="${chatId}">
                    <td class="select-column">
                        <input type="checkbox" class="cp-chat-checkbox" data-chat-id="${chatId}" ${checked}>
                    </td>
                    <td class="row-open-cell">${chatId}</td>
                    <td class="row-open-cell">${escapeHtml(row.sessionId || '')}</td>
                    <td class="row-open-cell" title="${escapeHtml(row.prompt || '')}">${promptSummary}</td>
                    <td class="row-open-cell">${escapeHtml(formatDateTime(row.createdAt, ''))}</td>
                </tr>
            `;
        }).join('');

        tbody.querySelectorAll('.cp-chat-checkbox').forEach((checkbox) => {
            checkbox.addEventListener('click', (event) => event.stopPropagation());
            checkbox.addEventListener('change', () => {
                const chatId = String(checkbox.getAttribute('data-chat-id') || '');
                if (!chatId) {
                    return;
                }
                if (checkbox.checked) {
                    state.selectedChatIds.add(chatId);
                } else {
                    state.selectedChatIds.delete(chatId);
                }
                updateChatSelectionInfo();
                syncSelectAllVisibleState();
            });
        });

        tbody.querySelectorAll('tr[data-chat-id]').forEach((rowEl) => {
            rowEl.addEventListener('click', () => {
                const chatId = String(rowEl.getAttribute('data-chat-id') || '');
                if (!chatId) {
                    return;
                }
                openChatDetail(chatId);
            });
        });

        if (pageInfo) {
            pageInfo.textContent = `Page ${state.chatPage} of ${totalPages} - ${state.filteredChatRows.length} result(s)`;
        }
        if (prevBtn) {
            prevBtn.disabled = state.chatPage <= 1;
        }
        if (nextBtn) {
            nextBtn.disabled = state.chatPage >= totalPages;
        }

        updateChatSelectionInfo();
        syncSelectAllVisibleState();
    }

    function renderNoChats(message) {
        const tbody = document.getElementById('cpSubmittedChatsBody');
        if (!tbody) {
            return;
        }
        tbody.innerHTML = `<tr><td colspan="5" class="empty-row">${escapeHtml(message)}</td></tr>`;
    }

    function getCurrentChatPageRows() {
        const start = (state.chatPage - 1) * CHAT_PAGE_SIZE;
        return state.filteredChatRows.slice(start, start + CHAT_PAGE_SIZE);
    }

    function updateChatSelectionInfo() {
        const infoEl = document.getElementById('cpChatSelectionInfo');
        const reviewSelectedBtn = document.getElementById('cpChatReviewSelectedBtn');
        const count = state.selectedChatIds.size;
        if (infoEl) {
            infoEl.textContent = count ? `${count} selected` : '';
        }
        if (reviewSelectedBtn) {
            reviewSelectedBtn.disabled = count === 0;
        }
    }

    function syncSelectAllVisibleState() {
        const selectAllVisible = document.getElementById('cpChatsSelectAllVisible');
        if (!selectAllVisible) {
            return;
        }
        const rows = getCurrentChatPageRows();
        if (!rows.length) {
            selectAllVisible.checked = false;
            selectAllVisible.indeterminate = false;
            return;
        }

        let selectedCount = 0;
        rows.forEach((row) => {
            if (state.selectedChatIds.has(String(row.chatId || ''))) {
                selectedCount += 1;
            }
        });

        selectAllVisible.checked = selectedCount === rows.length;
        selectAllVisible.indeterminate = selectedCount > 0 && selectedCount < rows.length;
    }

    function setChatsStatus(message, success) {
        const statusEl = document.getElementById('cpChatsStatus');
        if (!statusEl) {
            return;
        }
        statusEl.textContent = message;
        statusEl.style.color = success ? '#334155' : '#b91c1c';
    }

    function disableChatActionButtons(disabled) {
        const ids = [
            'cpChatSearchBtn',
            'cpChatClearSearchBtn',
            'cpChatSelectVisibleBtn',
            'cpChatDeselectBtn',
            'cpChatReviewAllBtn',
            'cpChatPrevBtn',
            'cpChatNextBtn',
            'cpChatsSelectAllVisible',
            'cpChatSearchInput'
        ];
        ids.forEach((id) => {
            const el = document.getElementById(id);
            if (el) {
                el.disabled = disabled;
            }
        });
        updateChatSelectionInfo();
    }

    async function createSelection(chatIds) {
        const ids = Array.isArray(chatIds)
            ? chatIds.map((id) => String(id || '').trim()).filter(Boolean)
            : [];

        if (!ids.length) {
            throw new Error('No chat IDs were provided.');
        }

        const payload = await fetchJson(selectEndpoint, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            },
            body: JSON.stringify({ selectedChatIds: ids })
        });

        if (payload.status !== 'ok' || !payload.selectionId) {
            throw new Error(payload.message || 'Unable to create review selection.');
        }

        return payload.selectionId;
    }

    async function openChatDetail(chatId) {
        if (!chatId) {
            return;
        }

        state.activeDetailChatId = chatId;
        renderSubmittedChats();

        const detailCard = document.getElementById('cpDetailCard');
        const title = document.getElementById('cpDetailTitle');
        const prompt = document.getElementById('cpDetailPrompt');
        const response = document.getElementById('cpDetailResponse');

        if (!detailCard || !title || !prompt || !response) {
            return;
        }

        detailCard.style.display = 'block';
        title.textContent = `Selected Chat Details - Chat ${chatId}`;

        if (state.detailByChatId.has(chatId)) {
            const cached = state.detailByChatId.get(chatId);
            renderChatDetail(cached);
            detailCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            return;
        }

        prompt.textContent = 'Loading prompt...';
        response.textContent = 'Loading response...';
        clearTranslationUi();

        try {
            const selectionId = await createSelection([chatId]);
            const query = new URLSearchParams();
            query.set('selectionId', selectionId);
            query.set('page', '1');
            query.set('limit', '1');
            query.set('sortColumn', 'created_at');
            query.set('sortDir', 'DESC');

            const payload = await fetchJson(`${reviewDataEndpoint}?${query.toString()}`, {
                method: 'GET',
                credentials: 'same-origin',
                headers: { Accept: 'application/json' }
            });

            const first = Array.isArray(payload.rows) && payload.rows.length ? payload.rows[0] : null;
            if (!first) {
                throw new Error('No detail data was returned for that chat.');
            }

            state.detailByChatId.set(chatId, first);
            renderChatDetail(first);
            detailCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        } catch (err) {
            prompt.textContent = err.message || 'Unable to load prompt.';
            response.textContent = 'Unable to load response.';
        }
    }

    function renderChatDetail(row) {
        const prompt = document.getElementById('cpDetailPrompt');
        const response = document.getElementById('cpDetailResponse');
        const title = document.getElementById('cpDetailTitle');

        if (title && row.chatId) {
            title.textContent = `Selected Chat Details - Chat ${row.chatId}`;
        }
        if (prompt) {
            prompt.textContent = row.prompt || '(empty prompt)';
        }
        if (response) {
            response.textContent = row.response || '(empty response)';
        }
        clearTranslationUi();
    }

    function bindDetailCardActions() {
        const promptBtn = document.getElementById('cpTranslatePromptBtn');
        const responseBtn = document.getElementById('cpTranslateResponseBtn');

        if (promptBtn) {
            promptBtn.addEventListener('click', async () => {
                const detail = state.detailByChatId.get(state.activeDetailChatId);
                const targetLang = (document.getElementById('cpTranslateTargetLang')?.value || 'en').trim();
                await translateText(detail?.prompt || '', targetLang, 'cpPromptTranslationMeta', 'cpPromptTranslationOutput', 'Prompt');
            });
        }

        if (responseBtn) {
            responseBtn.addEventListener('click', async () => {
                const detail = state.detailByChatId.get(state.activeDetailChatId);
                const targetLang = (document.getElementById('cpTranslateTargetLang')?.value || 'en').trim();
                await translateText(detail?.response || '', targetLang, 'cpResponseTranslationMeta', 'cpResponseTranslationOutput', 'Response');
            });
        }
    }

    function clearTranslationUi() {
        setTextById('cpPromptTranslationMeta', '');
        setTextById('cpResponseTranslationMeta', '');
        setBlockTextById('cpPromptTranslationOutput', '', false);
        setBlockTextById('cpResponseTranslationOutput', '', false);
    }

    async function translateText(sourceText, targetLang, metaId, outputId, label) {
        if (!sourceText || !sourceText.trim()) {
            setTextById(metaId, `${label} is empty; nothing to translate.`);
            setBlockTextById(outputId, '', false);
            return;
        }

        setTextById(metaId, 'Translating...');
        setBlockTextById(outputId, '', false);

        try {
            const payload = await fetchJson(translateEndpoint, {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json'
                },
                body: JSON.stringify({
                    text: sourceText,
                    targetLang: targetLang || 'en'
                })
            });

            if (payload.status !== 'ok') {
                throw new Error(payload.message || 'Translation failed.');
            }

            const translated = String(payload.translatedText || '');
            const sourceLang = payload.sourceLang || 'auto';
            const destinationLang = payload.targetLang || targetLang || 'en';

            setTextById(metaId, `${label}: ${sourceLang} -> ${destinationLang}`);
            setBlockTextById(outputId, translated || '(empty translation)', true);
        } catch (err) {
            setTextById(metaId, `Translation failed: ${err.message || 'Unknown error'}`);
            setBlockTextById(outputId, '', false);
        }
    }

    function setTextById(id, value) {
        const el = document.getElementById(id);
        if (!el) {
            return;
        }
        el.textContent = value || '';
    }

    function setBlockTextById(id, value, show) {
        const el = document.getElementById(id);
        if (!el) {
            return;
        }
        el.textContent = value || '';
        el.style.display = show ? 'block' : 'none';
    }

    function escapeHtml(input) {
        return String(input || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /**
     * Turns Session ID / Friendly Name elements into links to customer profile.
     *
     * Usage:
     * - add class "js-customer-profile-link"
     * - set one of:
     *   data-session-id="..."
     *   data-friendly-name="..."
     *
     * Optional:
     * - data-context-path="/chat-server"
     *
     * Example:
     * <span class="js-customer-profile-link" data-session-id="abc123">abc123</span>
     */
    function bindProfileLinks() {
        const nodes = document.querySelectorAll('.js-customer-profile-link');
        if (!nodes.length) {
            return;
        }

        nodes.forEach((node) => {
            // skip if already anchor
            if (node.tagName === 'A') {
                return;
            }

            const sid = (node.getAttribute('data-session-id') || '').trim();
            const fname = (node.getAttribute('data-friendly-name') || '').trim();
            const localContextPath = node.getAttribute('data-context-path') || contextPath || '';
            if (!sid && !fname) {
                return;
            }

            const a = document.createElement('a');
            const qs = new URLSearchParams();
            if (sid) {
                qs.set('sessionId', sid);
            }
            if (!sid && fname) {
                qs.set('friendlyName', fname); // fallback route support
            }

            a.href = `${localContextPath}/customer-profile?${qs.toString()}`;
            a.textContent = node.textContent && node.textContent.trim() ? node.textContent.trim() : (sid || fname);
            a.className = 'customer-profile-link';

            // preserve title/tooltip if present
            if (node.title) {
                a.title = node.title;
            }

            node.replaceWith(a);
        });
    }
})();
