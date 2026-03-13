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

let currentQuery = '';
let activeEditRow = null;
let activeRow = null;
let currentPage = 1;
let pageSize = 10;
let totalSessions = 0;

function renderSessionLabel(cell, label, sessionId) {
    cell.innerHTML = '';
    const primary = document.createElement('div');
    primary.textContent = label || sessionId || '';
    cell.appendChild(primary);
    if (sessionId && label && label !== sessionId) {
        const muted = document.createElement('div');
        muted.className = 'session-id-muted';
        muted.textContent = sessionId;
        cell.appendChild(muted);
    }
}

function updatePaginationControls(count) {
    const totalPages = Math.max(1, Math.ceil((totalSessions || count) / pageSize));
    prevPageBtn.disabled = currentPage <= 1;
    nextPageBtn.disabled = currentPage >= totalPages;
    sessionInfo.textContent = `Showing ${count} of ${totalSessions} sessions · Page ${currentPage} of ${totalPages}`;
}

async function loadSessions(query = '') {
    if (!sessionListBody) return;
    sessionInfo.textContent = 'Loading sessions…';
    sessionListBody.innerHTML = '<tr><td colspan="6" class="empty-row">Loading sessions…</td></tr>';
    try {
        const offset = (currentPage - 1) * pageSize;
        const params = new URLSearchParams();
        params.append('q', query);
        params.append('limit', pageSize);
        params.append('offset', offset);

        const url = `${contextPath}/dashboard/session-names.json?${params.toString()}`;
        const response = await fetch(url, { credentials: 'same-origin' });
        if (!response.ok) {
            throw new Error('Unable to fetch session catalog');
        }
        const payload = await response.json();
        if (!payload || payload.status !== 'ok') {
            throw new Error(payload && payload.message ? payload.message : 'Unexpected response');
        }
        totalSessions = payload.total || 0;

        if (!Array.isArray(payload.sessions) || payload.sessions.length === 0) {
            sessionListBody.innerHTML = '<tr><td colspan="6" class="empty-row">No sessions found.</td></tr>';
            updatePaginationControls(0);
            return;
        }
        sessionListBody.innerHTML = '';
        payload.sessions.forEach(session => {
            const row = document.createElement('tr');

            const idCell = document.createElement('td');
            renderSessionLabel(idCell, session.displayLabel || session.sessionId || '', session.sessionId);
            row.appendChild(idCell);

            const nameCell = document.createElement('td');
            nameCell.textContent = session.displayName || '—';
            row.appendChild(nameCell);

            const emailCell = document.createElement('td');
            emailCell.textContent = session.email || '—';
            row.appendChild(emailCell);

            const countCell = document.createElement('td');
            countCell.textContent = typeof session.count === 'number' ? `${session.count} chats` : '—';
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
        sessionInfo.textContent = 'Unable to load sessions.';
        sessionListBody.innerHTML = '<tr><td colspan="6" class="empty-row">Unable to load sessions.</td></tr>';
        prevPageBtn.disabled = true;
        nextPageBtn.disabled = true;
    }
}

function showEditPanel(row, session) {
    removeEditPanel();
    activeRow = row;
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
        row.querySelector('td:nth-child(2)').textContent = displayName || '—';
        row.querySelector('td:nth-child(3)').textContent = email || '—';
        session.displayName = displayName;
        session.email = email;
        session.displayLabel = displayName || email || session.sessionId || '';
        row.querySelector('td:first-child').innerHTML = '';
        renderSessionLabel(row.querySelector('td:first-child'), session.displayLabel, session.sessionId);
        setTimeout(() => {
            statusMessage.textContent = '';
            removeEditPanel();
        }, 800);
    } catch (error) {
        console.error(error);
        statusMessage.textContent = error.message || 'Unable to save.';
    }
}

searchButton?.addEventListener('click', () => {
    currentQuery = searchInput.value.trim();
    currentPage = 1;
    loadSessions(currentQuery);
});

searchInput?.addEventListener('keypress', event => {
    if (event.key === 'Enter') {
        event.preventDefault();
        currentQuery = searchInput.value.trim();
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
    const totalPages = Math.max(1, Math.ceil((totalSessions || 0) / pageSize));
    if (currentPage >= totalPages) return;
    currentPage += 1;
    loadSessions(currentQuery);
});

loadSessions('');
