const config = window.adminPageConfig || {};
const contextPath = config.contextPath || '';
const apiKeyStored = Boolean(config.apiKeyStored);
let initialWidgetList = [];

try {
    initialWidgetList = Array.isArray(config.widgetListJson) ? config.widgetListJson : JSON.parse(config.widgetListJson || '[]');
} catch (e) {
    initialWidgetList = [];
}

let lastTestSuccess = false;

const widgetState = {
    editingId: null,
    widgets: initialWidgetList
};

const widgetSyncStatuses = {};
const widgetMessageEl = document.getElementById('widgetMessage');
const widgetSearchInput = document.getElementById('widgetSearch');
const widgetSelectAll = document.getElementById('widgetSelectAll');
const widgetTableExplorerBody = document.getElementById('widgetTableExplorerBody');
const widgetTableExplorerMessage = document.getElementById('widgetTableExplorerMessage');
const userTableBody = document.getElementById('userTableBody');

document.addEventListener('DOMContentLoaded', () => {
    if (apiKeyStored) {
        const note = document.getElementById('apiKeyStoredNote');
        if (note) note.style.display = 'block';
    }

    if (widgetState.widgets.length) {
        renderWidgetTable();
        renderWidgetTableExplorer();
    }
    updateSaveButton();
    reloadWidgetList();
    loadSyncInterval();
    loadUserList();

    document.getElementById('testConnectionBtn').addEventListener('click', testConnection);
    document.getElementById('saveConfigBtn').addEventListener('click', saveConfiguration);
    document.getElementById('widgetSearch')?.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            reloadWidgetList();
        }
    });
    document.getElementById('searchWidgetsBtn')?.addEventListener('click', reloadWidgetList);
    document.getElementById('clearWidgetSearchBtn')?.addEventListener('click', () => {
        document.getElementById('widgetSearch').value = '';
        reloadWidgetList();
    });
    document.getElementById('saveWidgetEntryBtn').addEventListener('click', saveWidgetEntry);
    document.getElementById('clearWidgetFormBtn').addEventListener('click', clearWidgetForm);
    document.getElementById('deleteSelectedWidgetsBtn').addEventListener('click', deleteSelectedWidgets);
    document.getElementById('saveSyncIntervalBtn').addEventListener('click', saveSyncInterval);
    document.getElementById('userCreateForm').addEventListener('submit', event => {
        event.preventDefault();
        createUser();
    });

    const syncBtn = document.getElementById('syncWidgetTablesBtn');
    if (syncBtn) {
        syncBtn.addEventListener('click', () => {
            syncBtn.disabled = true;
            syncWidgetTables().finally(() => syncBtn.disabled = false);
        });
    }

    if (widgetSelectAll) {
        widgetSelectAll.addEventListener('change', event => {
            const checked = event.target.checked;
            document.querySelectorAll('.widget-select').forEach(cb => (cb.checked = checked));
        });
    }
});

function formatHumanReadableTimestamp(value) {
    if (!value) {
        return 'never';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function loadSyncInterval() {
    fetch(`${contextPath}/admin/widgets/sync/timer`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
        .then(response => response.json())
        .then(payload => {
            if (payload.status === 'ok' && typeof payload.intervalSeconds === 'number') {
                const minutes = Math.max(1, Math.round(payload.intervalSeconds / 60));
                document.getElementById('syncInterval').value = minutes;
                showSyncIntervalMessage(`Auto sync runs every ${minutes} minute(s). Last synced: ${formatHumanReadableTimestamp(payload.lastSynced)}`);
            }
        })
        .catch(() => {
            showSyncIntervalMessage('Unable to load auto sync interval.', true);
        });
}

function saveSyncInterval() {
    const minutesInput = document.getElementById('syncInterval');
    const minutes = parseInt(minutesInput.value, 10);
    if (isNaN(minutes) || minutes < 1) {
        showSyncIntervalMessage('Please enter a valid interval (minimum 1 minute).', true);
        return;
    }
    const seconds = minutes * 60;
    const data = new URLSearchParams();
    data.append('intervalSeconds', seconds);

    fetch(`${contextPath}/admin/widgets/sync/timer`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: data.toString()
    })
        .then(response => response.json())
        .then(payload => {
            if (payload.status === 'ok') {
                showSyncIntervalMessage(`Auto sync interval set to ${minutes} minute(s). Last synced: ${formatHumanReadableTimestamp(payload.lastSynced)}`);
            } else {
                showSyncIntervalMessage(payload.message || 'Unable to update interval.', true);
            }
        })
        .catch(error => {
            showSyncIntervalMessage(`Unable to save interval: ${error.message}`, true);
        });
}

function showSyncIntervalMessage(text, isError = false) {
    const el = document.getElementById('syncIntervalMessage');
    if (el) {
        el.textContent = text;
        el.style.color = isError ? '#b91c1c' : '#047857';
    }
}

function testConnection() {
    const host = document.getElementById('serverHost').value.trim();
    const port = document.getElementById('serverPort').value.trim();
    const apiKey = document.getElementById('apiKey').value.trim();
    const resultEl = document.getElementById('testResult');

    if (!host || !port) {
        resultEl.textContent = 'Please provide host and port.';
        resultEl.style.color = '#b91c1c';
        lastTestSuccess = false;
        updateSaveButton();
        return;
    }

    const data = new URLSearchParams();
    data.append('serverHost', host);
    data.append('serverPort', port);
    if (apiKey) {
        data.append('apiKey', apiKey);
    }

    fetch(`${contextPath}/admin/test-connection`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: data.toString()
    })
        .then(response => response.json().then(payload => ({ status: response.status, payload })))
        .then(({ status, payload }) => {
            if (payload.status === 'ok') {
                resultEl.textContent = 'Connection successful.';
                resultEl.style.color = '#047857';
                lastTestSuccess = true;
            } else {
                resultEl.textContent = payload.message || `Connection failed (${status}).`;
                resultEl.style.color = '#b91c1c';
                lastTestSuccess = false;
            }
            updateSaveButton();
        })
        .catch(error => {
            resultEl.textContent = `Connection error: ${error.message}`;
            resultEl.style.color = '#b91c1c';
            lastTestSuccess = false;
            updateSaveButton();
        });
}

function updateSaveButton() {
    const saveBtn = document.getElementById('saveConfigBtn');
    if (saveBtn) {
        saveBtn.disabled = !lastTestSuccess;
    }
}

function saveConfiguration() {
    const host = document.getElementById('serverHost').value.trim();
    const port = document.getElementById('serverPort').value.trim();
    const apiKey = document.getElementById('apiKey').value.trim();
    const resultEl = document.getElementById('testResult');

    if (!lastTestSuccess) {
        resultEl.textContent = 'Please test the connection successfully before saving.';
        resultEl.style.color = '#b91c1c';
        return;
    }

    const data = new URLSearchParams();
    data.append('serverHost', host);
    data.append('serverPort', port);
    data.append('apiKey', apiKey);

    fetch(`${contextPath}/admin/save-config`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: data.toString()
    })
        .then(response => response.json())
        .then(payload => {
            if (payload.status === 'ok') {
                resultEl.textContent = 'Configuration saved successfully.';
                resultEl.style.color = '#047857';
            } else {
                resultEl.textContent = payload.message || 'Unable to save configuration.';
                resultEl.style.color = '#b91c1c';
            }
        })
        .catch(error => {
            resultEl.textContent = `Save error: ${error.message}`;
            resultEl.style.color = '#b91c1c';
        });
}

function createUser() {
    const username = document.getElementById('newUsername').value.trim();
    const password = document.getElementById('newPassword').value.trim();
    const role = document.getElementById('roleSelect').value;

    if (!username || !password) {
        showUserResult('Username and password are required.', true);
        return;
    }

    fetch(`${contextPath}/admin/users`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({ username, password, role })
    })
        .then(async response => {
            const payload = await response.json();
            if (response.ok) {
                showUserResult(`Created ${payload.role.toLowerCase()} ${payload.username}.`);
                document.getElementById('userCreateForm').reset();
                loadUserList();
            } else {
                throw new Error(payload.error || 'Unable to create user.');
            }
        })
        .catch(error => {
            showUserResult(`Error: ${error.message}`, true);
        });
}

function showUserResult(message, isError = false) {
    const resultEl = document.getElementById('userResult');
    if (resultEl) {
        resultEl.textContent = message;
        resultEl.style.color = isError ? '#b91c1c' : '#047857';
    }
}

function loadUserList() {
    fetch(`${contextPath}/admin/users`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
        .then(response => response.json())
        .then(payload => {
            if (payload.status !== 'ok') {
                showUserResult(payload.message || 'Unable to load users.', true);
                return;
            }
            renderUserTable(payload.users || []);
        })
        .catch(error => {
            showUserResult(`Unable to load users: ${error.message}`, true);
        });
}

function renderUserTable(users) {
    if (!userTableBody) {
        return;
    }
    if (!users.length) {
        userTableBody.innerHTML = '<tr><td colspan="3" class="empty-row">No users found.</td></tr>';
        return;
    }
    userTableBody.innerHTML = users.map(user => `<tr>
        <td>${escapeHtml(user.username)}</td>
        <td>${escapeHtml(user.role)}</td>
        <td><button type="button" class="ghost-btn" onclick="deleteUser('${user.id}')">Delete</button></td>
    </tr>`).join('');
}

function deleteUser(id) {
    if (!confirm('Delete this user?')) {
        return;
    }

    fetch(`${contextPath}/admin/users?userId=${encodeURIComponent(id)}`, {
        method: 'DELETE',
        headers: { 'Accept': 'application/json' }
    })
        .then(async response => {
            const payload = await response.json().catch(() => ({}));
            if (response.ok) {
                showUserResult('User deleted.');
                loadUserList();
            } else {
                throw new Error(payload.error || 'Unable to delete user.');
            }
        })
        .catch(error => {
            showUserResult(`Error: ${error.message}`, true);
        });
}

function showWidgetMessage(text, isError = false) {
    if (!widgetMessageEl) {
        return;
    }
    widgetMessageEl.textContent = text;
    widgetMessageEl.style.color = isError ? '#b91c1c' : '#047857';
}

function showWidgetTableExplorerMessage(text, isError = false) {
    if (!widgetTableExplorerMessage) {
        return;
    }
    widgetTableExplorerMessage.textContent = text;
    widgetTableExplorerMessage.style.color = isError ? '#b91c1c' : '#047857';
}

function escapeHtml(value) {
    if (!value) {
        return '';
    }
    return value.replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function renderWidgetTable() {
    const tbody = document.getElementById('widgetTableBody');
    if (!tbody) {
        return;
    }

    if (!widgetState.widgets.length) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-row">No widget entries available.</td></tr>';
    } else {
        tbody.innerHTML = widgetState.widgets
            .map(entry => `<tr>
                <td><input type="checkbox" class="widget-select" value="${entry.id}"></td>
                <td>${escapeHtml(entry.widgetId)}</td>
                <td>${escapeHtml(entry.displayName)}</td>
                <td class="actions">
                    <button type="button" class="ghost-btn" onclick="editWidgetEntry(${entry.id})">Edit</button>
                    <button type="button" class="ghost-btn" onclick="deleteWidgetEntry(${entry.id})">Delete</button>
                </td>
            </tr>`)
            .join('');
    }

    if (widgetSelectAll) {
        widgetSelectAll.checked = false;
    }

    renderWidgetTableExplorer();
}

function renderWidgetTableExplorer() {
    if (!widgetTableExplorerBody) {
        return;
    }

    if (!widgetState.widgets.length) {
        widgetTableExplorerBody.innerHTML = '<tr><td colspan="4" class="empty-row">Widget IDs will appear here once the registry loads.</td></tr>';
        return;
    }

    widgetTableExplorerBody.innerHTML = widgetState.widgets
        .map(entry => {
            const status = widgetSyncStatuses[entry.widgetId];
            const tableStatus = status ? (status.tableExists ? 'Table ready' : 'Table missing') : 'Not checked';
            const syncStatus = status
                ? (status.synced
                    ? `Synced (last ${formatHumanReadableTimestamp(status.lastSynced)})`
                    : 'Pending sync')
                : 'Awaiting sync';
            const message = status?.message ? `<div class="small-note">${escapeHtml(status.message)}</div>` : '';
            return `<tr>
                <td>${escapeHtml(entry.widgetId)}</td>
                <td>${tableStatus}</td>
                <td>${syncStatus}</td>
                <td>${message}</td>
            </tr>`;
        })
        .join('');
}

function reloadWidgetList() {
    const filter = widgetSearchInput.value.trim();
    const endpoint = `${contextPath}/admin/widgets${filter ? '?filter=' + encodeURIComponent(filter) : ''}`;

    fetch(endpoint, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
        .then(response => response.json())
        .then(payload => {
            if (payload.status !== 'ok') {
                showWidgetMessage(payload.message || 'Unable to load widgets.', true);
                return;
            }
            widgetState.widgets = payload.widgets || [];
            renderWidgetTable();
            showWidgetMessage(`Loaded ${widgetState.widgets.length} widget(s).`);
        })
        .catch(error => {
            showWidgetMessage(`Could not load widgets: ${error.message}`, true);
        });
}

function clearWidgetForm() {
    widgetState.editingId = null;
    document.getElementById('widgetIdInput').value = '';
    document.getElementById('widgetNameInput').value = '';
    showWidgetMessage('Ready to add a new widget.');
}

function populateWidgetForm(entry) {
    widgetState.editingId = entry ? entry.id : null;
    document.getElementById('widgetIdInput').value = entry ? entry.widgetId : '';
    document.getElementById('widgetNameInput').value = entry ? entry.displayName : '';
    showWidgetMessage(entry ? `Editing widget ${entry.widgetId}.` : 'Ready to add a new widget.');
}

function saveWidgetEntry() {
    const widgetIdInput = document.getElementById('widgetIdInput');
    const widgetNameInput = document.getElementById('widgetNameInput');
    const widgetIdValue = widgetIdInput.value.trim();
    const widgetNameValue = widgetNameInput.value.trim();

    if (!widgetIdValue || !widgetNameValue) {
        showWidgetMessage('Widget ID and name are required.', true);
        return;
    }

    const data = new URLSearchParams();
    data.append('widgetId', widgetIdValue);
    data.append('displayName', widgetNameValue);
    if (widgetState.editingId) {
        data.append('id', widgetState.editingId);
    }

    fetch(`${contextPath}/admin/widgets`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'application/json'
        },
        body: data.toString()
    })
        .then(response => response.json().then(payload => ({ status: response.status, payload })))
        .then(({ status, payload }) => {
            if (payload.status === 'ok') {
                showWidgetMessage(widgetState.editingId ? 'Widget updated.' : 'Widget added.');
                widgetState.editingId = null;
                reloadWidgetList();
                clearWidgetForm();
            } else {
                showWidgetMessage(payload.message || `Unable to save widget (status ${status}).`, true);
            }
        })
        .catch(error => {
            showWidgetMessage(`Save failed: ${error.message}`, true);
        });
}

function syncWidgetTables() {
    if (!widgetState.widgets.length) {
        showWidgetTableExplorerMessage('No widget entries to sync.', true);
        return Promise.resolve();
    }

    showWidgetTableExplorerMessage('Syncing widget tables...');
    return fetch(`${contextPath}/admin/widgets/sync`, {
        method: 'POST',
        headers: { 'Accept': 'application/json' }
    })
        .then(async response => {
            const payload = await response.json();
            if (payload.status !== 'ok') {
                throw new Error(payload.message || 'Sync failed.');
            }
            const statuses = payload.widgetStatus || [];
            const now = new Date().toISOString();
            statuses.forEach(status => {
                if (status && status.widgetId) {
                    widgetSyncStatuses[status.widgetId] = {
                        tableExists: Boolean(status.tableExists),
                        synced: Boolean(status.synced),
                        tableName: status.tableName || '',
                        message: status.message || '',
                        lastSynced: now
                    };
                }
            });
            const missing = statuses.filter(status => status && !status.tableExists).length;
            const summary = missing
                ? `${missing} table(s) were missing and created before syncing.`
                : 'All widget tables exist and have been synced.';
            showWidgetTableExplorerMessage(summary);
        })
        .catch(error => {
            showWidgetTableExplorerMessage(`Sync error: ${error.message}`, true);
        })
        .finally(renderWidgetTableExplorer);
}

function editWidgetEntry(id) {
    const entry = widgetState.widgets.find(item => item.id === id);
    if (!entry) {
        showWidgetMessage('Widget entry not found.', true);
        return;
    }
    populateWidgetForm(entry);
}

function deleteWidgetEntry(id) {
    if (!confirm('Delete this widget entry?')) {
        return;
    }

    fetch(`${contextPath}/admin/widgets?ids=${id}`, {
        method: 'DELETE',
        headers: { 'Accept': 'application/json' }
    })
        .then(response => response.json().then(payload => ({ status: response.status, payload })))
        .then(({ payload }) => {
            if (payload.status === 'ok') {
                showWidgetMessage('Widget deleted.');
                reloadWidgetList();
            } else {
                showWidgetMessage(payload.message || 'Unable to delete widget.', true);
            }
        })
        .catch(error => {
            showWidgetMessage(`Delete failed: ${error.message}`, true);
        });
}

function deleteSelectedWidgets() {
    const checkboxes = Array.from(document.querySelectorAll('.widget-select:checked'));
    if (!checkboxes.length) {
        showWidgetMessage('Please select widgets to delete.', true);
        return;
    }

    if (!confirm(`Delete ${checkboxes.length} widget(s)?`)) {
        return;
    }

    const ids = checkboxes.map(cb => cb.value);

    fetch(`${contextPath}/admin/widgets?ids=${ids.join(',')}`, {
        method: 'DELETE',
        headers: { 'Accept': 'application/json' }
    })
        .then(response => response.json().then(payload => ({ status: response.status, payload })))
        .then(({ payload }) => {
            if (payload.status === 'ok') {
                showWidgetMessage(`${payload.deleted || ids.length} widget(s) deleted.`);
                reloadWidgetList();
            } else {
                showWidgetMessage(payload.message || 'Unable to delete selected widgets.', true);
            }
        })
        .catch(error => {
            showWidgetMessage(`Bulk delete failed: ${error.message}`, true);
        });
}
