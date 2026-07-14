(() => {
const sessionConfig = window.sessionNamesConfig || {};
const contextPath = sessionConfig.contextPath || '';

const sessionListBody = document.getElementById('sessionNameList');
const sessionInfo = document.getElementById('sessionListInfo');
const searchInput = document.getElementById('sessionSearch');
const searchButton = document.getElementById('sessionSearchButton');
const editTemplate = document.getElementById('sessionEditTemplate');
const prevPageBtn = document.getElementById('prevPageBtn');
const nextPageBtn = document.getElementById('nextPageBtn');
const pageSizeSelect = document.getElementById('pageSizeSelect');
const toggleLabeledOnlyBtn = document.getElementById('toggleLabeledOnlyBtn');

let currentQuery = '';
let activeEditRow = null;
let currentPage = 1;
let pageSize = 10;
let totalSessions = 0;
let totalPages = 1;

// New toggle filter state
let labeledOnly = false;

function buildCustomerProfileUrl(sessionId, fallbackFriendlyName) {
    const params = new URLSearchParams();
    const sid = (sessionId || '').trim();
    const fname = (fallbackFriendlyName || '').trim();

    if (sid) {
        params.set('sessionId', sid);
    } else if (fname) {
        params.set('friendlyName', fname);
    } else {
        return '';
    }

    return `${contextPath}/customer-profile?${params.toString()}`;
}

function appendProfileLink(container, textValue, sessionId, friendlyName) {
    const text = (textValue || '').trim();
    if (!text) {
        container.textContent = '';
        return;
    }

    const href = buildCustomerProfileUrl(sessionId, friendlyName);
    if (!href) {
        container.textContent = text;
        return;
    }

    const a = document.createElement('a');
    a.href = href;
    a.className = 'customer-profile-link';
    a.textContent = text;
    a.title = sessionId ? `Open customer profile (${sessionId})` : 'Open customer profile';
    container.appendChild(a);
}

function renderSessionLabel(cell, label, sessionId, friendlyName) {
    cell.innerHTML = '';

    const primary = document.createElement('div');
    appendProfileLink(primary, label || sessionId || '', sessionId, friendlyName);
    cell.appendChild(primary);

    if (sessionId && label && label !== sessionId) {
        const muted = document.createElement('div');
        muted.className = 'session-id-muted';
        appendProfileLink(muted, sessionId, sessionId, friendlyName);
        cell.appendChild(muted);
    }
}

function renderFriendlyNameCell(cell, displayName, sessionId, fallbackFriendlyName) {
    cell.innerHTML = '';
    const value = displayName || '—';

    if (value === '—') {
        cell.textContent = value;
        return;
    }

    appendProfileLink(cell, value, sessionId, fallbackFriendlyName || displayName || '');
}

function updatePaginationControls(count) {
    if (prevPageBtn) prevPageBtn.disabled = currentPage <= 1;
    if (nextPageBtn) nextPageBtn.disabled = currentPage >= totalPages;
    if (sessionInfo) {
        const labelState = labeledOnly ? 'Only labeled users' : 'All users';
        sessionInfo.textContent = `Showing ${count} of ${totalSessions} sessions · Page ${currentPage} of ${totalPages} · ${labelState}`;
    }
}

function refreshLabeledOnlyUi() {
    if (!toggleLabeledOnlyBtn) return;
    toggleLabeledOnlyBtn.textContent = `Only labeled users: ${labeledOnly ? 'On' : 'Off'}`;
    toggleLabeledOnlyBtn.setAttribute('aria-pressed', labeledOnly ? 'true' : 'false');
    toggleLabeledOnlyBtn.style.border = labeledOnly ? '2px solid #1d4ed8' : '1px solid transparent';
}

function syncLabeledOnlyToUrl() {
    try {
        const u = new URL(window.location.href);
        if (labeledOnly) {
            u.searchParams.set('labeledOnly', 'true');
        } else {
            u.searchParams.delete('labeledOnly');
        }
        window.history.replaceState({}, '', u.toString());
    } catch {
        // no-op
    }
}

async function loadSessions(query = '') {
    if (!sessionListBody) return;

    if (sessionInfo) sessionInfo.textContent = 'Loading sessions…';
    sessionListBody.innerHTML = '<tr><td colspan="6" class="empty-row">Loading sessions…</td></tr>';

    try {
        const params = new URLSearchParams();
        if (query && query.trim()) {
            params.append('q', query.trim());
        }
        params.append('limit', String(pageSize));
        params.append('page', String(currentPage));
        params.append('labeledOnly', labeledOnly ? 'true' : 'false');

        const url = `${contextPath}/dashboard/session-names.json?${params.toString()}`;
        const response = await fetch(url, { credentials: 'same-origin' });

        if (!response.ok) {
            throw new Error('Unable to fetch session catalog');
        }

        const payload = await response.json();
        if (!payload || payload.status !== 'ok') {
            throw new Error(payload && payload.message ? payload.message : 'Unexpected response');
        }

        totalSessions = Number(payload.totalSessions ?? payload.total ?? 0);
        totalPages = Math.max(1, Number(payload.totalPages ?? Math.ceil(totalSessions / pageSize)));
        currentPage = Math.min(Math.max(1, Number(payload.page ?? currentPage)), totalPages);

        if (!Array.isArray(payload.sessions) || payload.sessions.length === 0) {
            sessionListBody.innerHTML = '<tr><td colspan="6" class="empty-row">No sessions found.</td></tr>';
            updatePaginationControls(0);
            return;
        }

        sessionListBody.innerHTML = '';
        payload.sessions.forEach(session => {
            const row = document.createElement('tr');

            const sessionId = session.sessionId || '';
            const displayLabel = session.displayLabel || session.sessionIdDisplay || sessionId || '';
            const displayName = session.displayName || '';
            const fallbackFriendly = displayName || session.email || displayLabel || '';

            const idCell = document.createElement('td');
            renderSessionLabel(idCell, displayLabel, sessionId, fallbackFriendly);
            row.appendChild(idCell);

            const nameCell = document.createElement('td');
            renderFriendlyNameCell(nameCell, displayName, sessionId, fallbackFriendly);
            row.appendChild(nameCell);

            const emailCell = document.createElement('td');
            emailCell.textContent = session.email || '—';
            row.appendChild(emailCell);

            const countCell = document.createElement('td');
            const countNum = Number(session.count);
            countCell.textContent = Number.isFinite(countNum) ? `${countNum} chats` : '—';
            row.appendChild(countCell);

            const lastCell = document.createElement('td');
            lastCell.textContent = session.lastEntry || '—';
            row.appendChild(lastCell);

            const actionCell = document.createElement('td');
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'ghost-btn';
            button.textContent = 'Select';
            button.addEventListener('click', () => showEditPanel(row, session));
            actionCell.appendChild(button);
            row.appendChild(actionCell);

            sessionListBody.appendChild(row);
        });

        updatePaginationControls(payload.sessions.length);
    } catch (error) {
        console.error(error);
        if (sessionInfo) sessionInfo.textContent = 'Unable to load sessions.';
        sessionListBody.innerHTML = '<tr><td colspan="6" class="empty-row">Unable to load sessions.</td></tr>';
        if (prevPageBtn) prevPageBtn.disabled = true;
        if (nextPageBtn) nextPageBtn.disabled = true;
    }
}

function showEditPanel(row, session) {
    removeEditPanel();
    const fragment = editTemplate.content.cloneNode(true);
    const editRow = fragment.querySelector('.session-edit-row');
    const form = editRow.querySelector('form');
    form.elements.sessionId.value = session.sessionId || '';
    form.elements.displayName.value = session.displayName || '';
    form.elements.email.value = session.email || '';
    const statusMessage = editRow.querySelector('.status-message');
    form.addEventListener('submit', async event => {
        event.preventDefault();
        await submitLabel(form, session, row, statusMessage);
    });
    editRow.querySelector('.cancel-button').addEventListener('click', removeEditPanel);
    row.parentNode.insertBefore(editRow, row.nextSibling);
    activeEditRow = editRow;
}

function removeEditPanel() {
    if (activeEditRow) {
        activeEditRow.remove();
        activeEditRow = null;
    }
}

async function submitLabel(form, session, row, statusMessage) {
    const sessionId = form.elements.sessionId.value.trim();
    const displayName = form.elements.displayName.value.trim();
    const email = form.elements.email.value.trim();
    if (!sessionId) {
        statusMessage.textContent = 'Invalid session.';
        return;
    }
    if (!displayName) {
        statusMessage.textContent = 'Friendly name is required.';
        return;
    }
    statusMessage.textContent = 'Saving…';
    try {
        const payload = new URLSearchParams();
        payload.append('sessionId', sessionId);
        payload.append('displayName', displayName);
        payload.append('email', email);

        const response = await fetch(`${contextPath}/dashboard/session-names/label`, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: payload.toString()
        });

        const result = await response.json();
        if (!response.ok || result.status !== 'ok') {
            throw new Error(result?.message || 'Unable to save session name.');
        }

        statusMessage.textContent = 'Saved.';

        session.displayName = displayName;
        session.email = email;
        session.displayLabel = displayName || email || session.sessionId || '';

        const nameCell = row.querySelector('td:nth-child(2)');
        renderFriendlyNameCell(nameCell, displayName, sessionId, displayName || email || '');

        row.querySelector('td:nth-child(3)').textContent = email || '—';

        renderSessionLabel(
            row.querySelector('td:first-child'),
            session.displayLabel,
            session.sessionId,
            session.displayName || session.email || ''
        );

        setTimeout(() => {
            statusMessage.textContent = '';
            removeEditPanel();
            // reload to keep filter/paging list in sync (important for labeledOnly mode)
            loadSessions(currentQuery);
        }, 800);
    } catch (error) {
        console.error(error);
        statusMessage.textContent = error.message || 'Unable to save.';
    }
}

searchButton?.addEventListener('click', () => {
    currentQuery = (searchInput?.value || '').trim();
    currentPage = 1;
    loadSessions(currentQuery);
});

searchInput?.addEventListener('keypress', event => {
    if (event.key === 'Enter') {
        event.preventDefault();
        currentQuery = (searchInput?.value || '').trim();
        currentPage = 1;
        loadSessions(currentQuery);
    }
});

pageSizeSelect?.addEventListener('change', () => {
    pageSize = parseInt(pageSizeSelect.value, 10) || 10;
    currentPage = 1;
    loadSessions(currentQuery);
});

prevPageBtn?.addEventListener('click', () => {
    if (currentPage <= 1) return;
    currentPage -= 1;
    loadSessions(currentQuery);
});

nextPageBtn?.addEventListener('click', () => {
    if (currentPage >= totalPages) return;
    currentPage += 1;
    loadSessions(currentQuery);
});

toggleLabeledOnlyBtn?.addEventListener('click', () => {
    labeledOnly = !labeledOnly;
    currentPage = 1;
    refreshLabeledOnlyUi();
    syncLabeledOnlyToUrl();
    loadSessions(currentQuery);
});

// initial state from URL if present
try {
    const urlParams = new URLSearchParams(window.location.search);
    labeledOnly = (urlParams.get('labeledOnly') || 'false').toLowerCase() === 'true';
} catch {
    labeledOnly = false;
}

refreshLabeledOnlyUi();
syncLabeledOnlyToUrl();
loadSessions('');
})();
