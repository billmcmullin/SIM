/* admin_page.js - full client script for admin configuration page
   - keeps existing functionality: server config, workspace, widgets, terms, users
   - CSV import/export (import via AJAX multipart FormData, export via fetch + save dialog)
   - safe Markdown rendering using marked + DOMPurify (if available)
   - Exposes a few functions globally used by inline onclick attributes
*/

(function () {
    'use strict';

    const config = window.adminPageConfig || {};
    const contextPath = config.contextPath || '';
    const apiKeyStored = Boolean(config.apiKeyStored);
    let initialWidgetList = [];
    let initialTermList = [];

    try {
        initialWidgetList = Array.isArray(config.widgetListJson) ? config.widgetListJson : JSON.parse(config.widgetListJson || '[]');
    } catch (e) {
        initialWidgetList = [];
    }

    try {
        initialTermList = Array.isArray(config.termsListJson) ? config.termsListJson : JSON.parse(config.termsListJson || '[]');
    } catch (e) {
        initialTermList = [];
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
    const termTableBody = document.getElementById('termTableBody');
    const termMessageEl = document.getElementById('termMessage');
    const termForm = document.getElementById('termCreateForm');
    const termIdInput = document.getElementById('termId');
    const termNameInput = document.getElementById('termName');
    const termDescriptionInput = document.getElementById('termDescription');
    const termPatternInput = document.getElementById('termPattern');
    const termTypeSelect = document.getElementById('termType');
    const saveTermBtn = document.getElementById('saveTermBtn');
    const cancelTermEditBtn = document.getElementById('cancelTermEditBtn');
    const workspaceNameInput = document.getElementById('workspaceNameInput');
    const saveWorkspaceBtn = document.getElementById('saveWorkspaceBtn');
    const workspaceMessageEl = document.getElementById('workspaceMessage');

    const exportTermsBtn = document.getElementById('exportTermsBtn');
    const termsImportForm = document.getElementById('termsImportForm');
    const termsCsvFileInput = document.getElementById('termsCsvFile');
    const importTermsBtn = document.getElementById('importTermsBtn');

    let isEditingTerm = false;

    document.addEventListener('DOMContentLoaded', () => {
        // show API key note if stored
        if (apiKeyStored) {
            const note = document.getElementById('apiKeyStoredNote');
            if (note) note.style.display = 'block';
        }

        // initial render if provided lists are present
        if (widgetState.widgets.length) {
            renderWidgetTable();
            renderWidgetTableExplorer();
        }

        updateSaveButton();
        reloadWidgetList();
        loadSyncInterval();
        loadUserList();
        loadTermList(initialTermList);
        initWorkspaceSection();

        // CSV file input: keep it in DOM but non-tabbable
        if (termsCsvFileInput) {
            termsCsvFileInput.tabIndex = -1;
        }

        // Export button behavior: fetch CSV then save via File System Access API or anchor fallback
        if (exportTermsBtn) {
            exportTermsBtn.addEventListener('click', async () => {
                const exportUrl = `${contextPath}/admin/terms/export`;
                try {
                    const resp = await fetch(exportUrl, {
                        method: 'GET',
                        credentials: 'same-origin',
                        headers: { 'Accept': 'text/csv,application/octet-stream;q=0.9,*/*;q=0.8' }
                    });

                    if (!resp.ok) {
                        // fallback to navigate to export URL to allow server-driven download
                        window.location.href = exportUrl;
                        return;
                    }

                    const blob = await resp.blob();

                    // Parse filename from Content-Disposition if present
                    let suggestedName = 'terms.csv';
                    try {
                        const cd = resp.headers.get('content-disposition') || '';
                        const m = /filename\*=UTF-8''([^;]+)|filename=\"?([^\";]+)\"?/.exec(cd);
                        if (m) {
                            suggestedName = decodeURIComponent(m[1] || m[2]);
                        }
                    } catch (e) {
                        // ignore parsing errors
                    }

                    // Use File System Access API when available
                    if (typeof window.showSaveFilePicker === 'function') {
                        try {
                            const opts = {
                                suggestedName,
                                types: [{
                                    description: 'CSV',
                                    accept: { 'text/csv': ['.csv'] }
                                }]
                            };
                            const handle = await window.showSaveFilePicker(opts);
                            const writable = await handle.createWritable();
                            await writable.write(blob);
                            await writable.close();
                            showTermMessage(`Export saved as "${handle.name}".`);
                            return;
                        } catch (err) {
                            // user cancelled or API failed; fall back to anchor download
                            console.warn('Save file picker failed or cancelled:', err);
                        }
                    }

                    // Anchor fallback (Save As)
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = suggestedName;
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                    URL.revokeObjectURL(url);
                    showTermMessage(`CSV download started (${suggestedName}).`);
                } catch (err) {
                    console.error('Export failed, falling back to direct download:', err);
                    window.location.href = `${contextPath}/admin/terms/export`;
                }
            });
        }

        // Import: clicking import button opens file chooser; file change triggers form submit (which now uses AJAX)
        if (importTermsBtn && termsCsvFileInput) {
            importTermsBtn.addEventListener('click', () => termsCsvFileInput.click());

            termsCsvFileInput.addEventListener('change', () => {
                if (!termsCsvFileInput.files || termsCsvFileInput.files.length === 0) return;

                // Use requestSubmit() if available so the form's submit handler runs (we intercept submit below)
                if (typeof termsImportForm.requestSubmit === 'function') {
                    termsImportForm.requestSubmit();
                } else {
                    // Fallback: call submit() which will trigger our submit handler we've added below
                    termsImportForm.submit();
                }
            });
        }

        // Replace the normal multipart form submit with an AJAX submit so we can process server redirect results without navigating away.
        if (termsImportForm) {
            termsImportForm.addEventListener('submit', async (event) => {
                event.preventDefault();

                const f = termsCsvFileInput;
                if (!f || !f.files || f.files.length === 0) {
                    alert('Please choose a CSV file to import.');
                    return;
                }
                if (!confirm('Import CSV will create or update terms. Continue?')) {
                    return;
                }

                // Build FormData from the form
                const formData = new FormData(termsImportForm);

                try {
                    // POST the FormData. Fetch follows redirects by default; final resp.url will include the redirect URL with query params.
                    const resp = await fetch(termsImportForm.action, {
                        method: 'POST',
                        body: formData,
                        credentials: 'same-origin',
                        redirect: 'follow'
                    });

                    // If the server did a redirect to /admin/terms?imported=... then resp.url will reflect that.
                    // Parse query params from resp.url if present.
                    if (resp && resp.url) {
                        processImportResultUrl(resp.url);
                    } else {
                        // No URL available — fallback: reload terms
                        loadTermList();
                        showTermMessage('Import completed.');
                    }
                } catch (err) {
                    console.error('CSV import failed via AJAX, falling back to normal form submit', err);
                    // As a fallback attempt a normal form submit (this will navigate away)
                    try {
                        termsImportForm.submit();
                    } catch (e) {
                        alert('Import failed and fallback submit also failed: ' + e.message);
                    }
                }
            });
        }

        // Wire existing handlers
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

        termForm?.addEventListener('submit', event => {
            event.preventDefault();
            submitTermForm();
        });
        cancelTermEditBtn?.addEventListener('click', () => resetTermForm());

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

        // Show import results if redirected back with query params
        handleImportResultFromQuery();
    });

    // Helper to parse query params from a URL (used after AJAX import when server redirects)
    function processImportResultUrl(urlString) {
        try {
            const u = new URL(urlString, window.location.origin);
            const params = u.searchParams;
            const imported = params.get('imported');
            const updated = params.get('updated');
            const errors = params.get('errors');

            if (imported || updated || errors) {
                const parts = [];
                if (imported) parts.push(`Imported ${imported}`);
                if (updated) parts.push(`Updated ${updated}`);
                if (errors) parts.push(`Errors: ${decodeURIComponent(errors)}`);
                showTermMessage(parts.join(' — '), errors != null);
            } else {
                showTermMessage('Import completed.');
            }
            // Refresh term list to reflect changes
            loadTermList();
        } catch (e) {
            console.warn('Unable to parse import redirect URL:', e);
            showTermMessage('Import completed.');
            loadTermList();
        }
    }

    function handleImportResultFromQuery() {
        try {
            const params = new URLSearchParams(window.location.search);
            const imported = params.get('imported');
            const updated = params.get('updated');
            const errors = params.get('errors');

            if (imported || updated || errors) {
                const parts = [];
                if (imported) parts.push(`Imported ${imported}`);
                if (updated) parts.push(`Updated ${updated}`);
                if (errors) parts.push(`Errors: ${decodeURIComponent(errors)}`);
                showTermMessage(parts.join(' — '), errors != null);
                // Remove query params from URL to avoid repeat messages
                const cleanUrl = window.location.pathname + window.location.hash;
                history.replaceState(null, '', cleanUrl);
                // reload terms to reflect imported changes
                loadTermList();
            }
        } catch (e) {
            // ignore parsing issues
        }
    }

    /* =========================
       Workspace helpers
       ========================= */
    function initWorkspaceSection() {
        if (workspaceNameInput && typeof config.workspaceName === 'string') {
            workspaceNameInput.value = config.workspaceName;
        }
        if (saveWorkspaceBtn) {
            saveWorkspaceBtn.addEventListener('click', () => {
                const name = workspaceNameInput.value.trim();
                saveWorkspaceName(name);
            });
        }
    }

    function saveWorkspaceName(name) {
        if (!saveWorkspaceBtn) return;
        const params = new URLSearchParams();
        params.append('workspaceName', name);

        saveWorkspaceBtn.disabled = true;
        fetch(`${contextPath}/admin/workspace`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
            body: params.toString()
        })
            .then(async response => {
                const payload = await response.json().catch(() => ({}));
                if (response.ok && payload.status === 'ok') {
                    showWorkspaceMessage('Workspace name saved.');
                    workspaceNameInput.value = payload.workspaceName || name;
                } else {
                    throw new Error(payload.message || 'Unable to save workspace name.');
                }
            })
            .catch(err => showWorkspaceMessage(`Error: ${err.message}`, true))
            .finally(() => { saveWorkspaceBtn.disabled = false; });
    }

    function showWorkspaceMessage(text, isError = false) {
        if (!workspaceMessageEl) return;
        workspaceMessageEl.textContent = text;
        workspaceMessageEl.style.color = isError ? '#b91c1c' : '#047857';
    }

    /* =========================
       Sync interval helpers
       ========================= */
    function loadSyncInterval() {
        fetch(`${contextPath}/admin/widgets/sync/timer`, { method: 'GET', headers: { 'Accept': 'application/json' } })
            .then(response => response.json())
            .then(payload => {
                if (payload.status === 'ok' && typeof payload.intervalSeconds === 'number') {
                    const minutes = Math.max(1, Math.round(payload.intervalSeconds / 60));
                    document.getElementById('syncInterval').value = minutes;
                    showSyncIntervalMessage(`Auto sync runs every ${minutes} minute(s). Last synced: ${formatHumanReadableTimestamp(payload.lastSynced)}`);
                }
            })
            .catch(() => showSyncIntervalMessage('Unable to load auto sync interval.', true));
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
            .catch(err => showSyncIntervalMessage(`Unable to save interval: ${err.message}`, true));
    }

    function showSyncIntervalMessage(text, isError = false) {
        const el = document.getElementById('syncIntervalMessage');
        if (el) {
            el.textContent = text;
            el.style.color = isError ? '#b91c1c' : '#047857';
        }
    }

    function formatHumanReadableTimestamp(value) {
        if (!value) return 'never';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return value;
        return date.toLocaleString(undefined, { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    }

    /* =========================
       Server connection helpers
       ========================= */
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
        if (apiKey) data.append('apiKey', apiKey);

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
            .catch(err => {
                resultEl.textContent = `Connection error: ${err.message}`;
                resultEl.style.color = '#b91c1c';
                lastTestSuccess = false;
                updateSaveButton();
            });
    }

    function updateSaveButton() {
        const saveBtn = document.getElementById('saveConfigBtn');
        if (saveBtn) saveBtn.disabled = !lastTestSuccess;
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
            .catch(err => {
                resultEl.textContent = `Save error: ${err.message}`;
                resultEl.style.color = '#b91c1c';
            });
    }

    /* =========================
       User management
       ========================= */
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
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ username, password, role })
        })
            .then(async response => {
                const payload = await response.json();
                if (response.ok) {
                    const createdRole = (payload.role || 'user').toLowerCase();
                    const createdUsername = payload.username || username;
                    showUserResult(`Created ${createdRole} ${createdUsername}.`);
                    document.getElementById('userCreateForm').reset();
                    loadUserList();
                } else {
                    throw new Error(payload.error || 'Unable to create user.');
                }
            })
            .catch(err => showUserResult(`Error: ${err.message}`, true));
    }

    function showUserResult(message, isError = false) {
        const resultEl = document.getElementById('userResult');
        if (resultEl) {
            resultEl.textContent = message;
            resultEl.style.color = isError ? '#b91c1c' : '#047857';
        }
    }

    function loadUserList() {
        fetch(`${contextPath}/admin/users`, { method: 'GET', headers: { 'Accept': 'application/json' } })
            .then(response => response.json())
            .then(payload => {
                if (payload.status !== 'ok') {
                    showUserResult(payload.message || 'Unable to load users.', true);
                    return;
                }
                renderUserTable(payload.users || []);
            })
            .catch(err => showUserResult(`Unable to load users: ${err.message}`, true));
    }

    function renderUserTable(users) {
        if (!userTableBody) return;
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

    window.deleteUser = function (id) {
        if (!confirm('Delete this user?')) return;
        fetch(`${contextPath}/admin/users?userId=${encodeURIComponent(id)}`, { method: 'DELETE', headers: { 'Accept': 'application/json' } })
            .then(async response => {
                const payload = await response.json().catch(() => ({}));
                if (response.ok) {
                    showUserResult('User deleted.');
                    loadUserList();
                } else {
                    throw new Error(payload.error || 'Unable to delete user.');
                }
            })
            .catch(err => showUserResult(`Error: ${err.message}`, true));
    };

    /* =========================
       Widget management
       ========================= */
    function renderWidgetTable() {
        const tbody = document.getElementById('widgetTableBody');
        if (!tbody) return;

        if (!widgetState.widgets.length) {
            tbody.innerHTML = '<tr><td colspan="4" class="empty-row">No widget entries available.</td></tr>';
        } else {
            tbody.innerHTML = widgetState.widgets.map(entry => `<tr>
                <td><input type="checkbox" class="widget-select" value="${entry.id}"></td>
                <td>${escapeHtml(entry.widgetId)}</td>
                <td>${escapeHtml(entry.displayName)}</td>
                <td class="actions">
                    <button type="button" class="ghost-btn" onclick="editWidgetEntry(${entry.id})">Edit</button>
                    <button type="button" class="ghost-btn" onclick="deleteWidgetEntry(${entry.id})">Delete</button>
                </td>
            </tr>`).join('');
        }

        if (widgetSelectAll) widgetSelectAll.checked = false;
        renderWidgetTableExplorer();
    }

    function renderWidgetTableExplorer() {
        if (!widgetTableExplorerBody) return;
        if (!widgetState.widgets.length) {
            widgetTableExplorerBody.innerHTML = '<tr><td colspan="4" class="empty-row">Widget IDs will appear here once the registry loads.</td></tr>';
            return;
        }

        widgetTableExplorerBody.innerHTML = widgetState.widgets.map(entry => {
            const status = widgetSyncStatuses[entry.widgetId];
            const tableStatus = status ? (status.tableExists ? 'Table ready' : 'Table missing') : 'Not checked';
            const syncStatus = status ? (status.synced ? `Synced (last ${formatHumanReadableTimestamp(status.lastSynced)})` : 'Pending sync') : 'Awaiting sync';
            const message = status?.message ? `<div class="small-note">${escapeHtml(status.message)}</div>` : '';
            return `<tr>
                <td>${escapeHtml(entry.widgetId)}</td>
                <td>${tableStatus}</td>
                <td>${syncStatus}</td>
                <td>${message}</td>
            </tr>`;
        }).join('');
    }

    function reloadWidgetList() {
        const filter = widgetSearchInput ? widgetSearchInput.value.trim() : '';
        const endpoint = `${contextPath}/admin/widgets${filter ? '?filter=' + encodeURIComponent(filter) : ''}`;
        fetch(endpoint, { method: 'GET', headers: { 'Accept': 'application/json' } })
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
            .catch(err => showWidgetMessage(`Could not load widgets: ${err.message}`, true));
    }

    window.editWidgetEntry = function (id) {
        const entry = widgetState.widgets.find(w => w.id === id);
        if (!entry) {
            showWidgetMessage('Widget not found.', true);
            return;
        }
        populateWidgetForm(entry);
    };

    window.deleteWidgetEntry = function (id) {
        if (!confirm('Delete this widget?')) return;
        fetch(`${contextPath}/admin/widgets?id=${encodeURIComponent(id)}`, { method: 'DELETE', headers: { 'Accept': 'application/json' } })
            .then(async response => {
                const payload = await response.json().catch(() => ({}));
                if (response.ok) {
                    showWidgetMessage('Widget deleted.');
                    reloadWidgetList();
                } else {
                    throw new Error(payload.message || 'Unable to delete widget.');
                }
            })
            .catch(err => showWidgetMessage(`Error: ${err.message}`, true));
    };

    function deleteSelectedWidgets() {
        const selected = Array.from(document.querySelectorAll('.widget-select:checked')).map(cb => cb.value);
        if (!selected.length) {
            showWidgetMessage('No widgets selected.', true);
            return;
        }
        if (!confirm(`Delete ${selected.length} widget(s)?`)) return;
        const params = new URLSearchParams();
        selected.forEach(id => params.append('id', id));
        fetch(`${contextPath}/admin/widgets/bulk-delete`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
            body: params.toString()
        })
            .then(async response => {
                const payload = await response.json().catch(() => ({}));
                if (response.ok && payload.status === 'ok') {
                    showWidgetMessage('Selected widgets deleted.');
                    reloadWidgetList();
                } else {
                    throw new Error(payload.message || 'Unable to delete selected widgets.');
                }
            })
            .catch(err => showWidgetMessage(`Error: ${err.message}`, true));
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
        if (widgetState.editingId) data.append('id', widgetState.editingId);

        fetch(`${contextPath}/admin/widgets`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
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
            .catch(err => showWidgetMessage(`Save failed: ${err.message}`, true));
    }

    function showWidgetMessage(text, isError = false) {
        if (!widgetMessageEl) return;
        widgetMessageEl.textContent = text;
        widgetMessageEl.style.color = isError ? '#b91c1c' : '#047857';
    }

    function showWidgetTableExplorerMessage(text, isError = false) {
        if (!widgetTableExplorerMessage) return;
        widgetTableExplorerMessage.textContent = text;
        widgetTableExplorerMessage.style.color = isError ? '#b91c1c' : '#047857';
    }

    /* =========================
       Term management and CSV helpers
       ========================= */
    // Normalize various incoming shapes
    function normalizeTermIncoming(term) {
        if (!term || typeof term !== 'object') return { id: null, name: '', description: '', matchPattern: '', matchType: 'WILDCARD', isSystem: false };
        const id = term.id ?? term.ID ?? null;
        const name = term.name ?? term.termName ?? term.label ?? '';
        const description = term.description ?? term.desc ?? '';
        const matchPattern = term.matchPattern ?? term.match_pattern ?? term.matchpattern ?? '';
        const matchType = term.matchType ?? term.match_type ?? term.matchtype ?? (term.type ?? 'WILDCARD');
        const isSystem = Boolean(term.isSystem ?? term.systemFlag ?? term.isSystemFlag ?? term.system_flag ?? false);
        return { id, name, description, matchPattern, matchType, isSystem };
    }

    function loadTermList(fallbackTerms = []) {
        if (!termTableBody) return;
        renderTermList(fallbackTerms);
        fetch(`${contextPath}/admin/terms`, { method: 'GET', headers: { 'Accept': 'application/json' } })
            .then(response => response.json())
            .then(payload => {
                if (payload.status !== 'ok') throw new Error(payload.message || 'Unable to load terms.');
                renderTermList(payload.terms || []);
            })
            .catch(err => showTermMessage(`Unable to load terms: ${err.message}`, true));
    }

    function renderTermList(terms) {
        if (!termTableBody) return;
        if (!terms.length) {
            termTableBody.innerHTML = '<tr><td colspan="5" class="empty-row">No terms defined yet.</td></tr>';
            return;
        }
        termTableBody.innerHTML = terms.map(term => {
            const normalized = normalizeTermIncoming(term);
            const canModify = !normalized.isSystem;
            const sanitizedTerm = {
                id: normalized.id,
                name: normalized.name || '',
                description: normalized.description || '',
                matchPattern: normalized.matchPattern || '',
                matchType: normalized.matchType || '',
                isSystem: Boolean(normalized.isSystem)
            };
            const payload = escapeHtml(JSON.stringify(sanitizedTerm));
            return `<tr data-term="${payload}">
                <td>${escapeHtml(sanitizedTerm.name)}</td>
                <td>${escapeHtml(sanitizedTerm.description)}</td>
                <td>${escapeHtml(sanitizedTerm.matchPattern || '')}</td>
                <td>${escapeHtml(sanitizedTerm.matchType || '')}</td>
                <td class="actions">
                    ${canModify ? `<button type="button" class="ghost-btn" onclick="startTermEditFromRow(this)">Edit</button>
                    <button type="button" class="ghost-btn" onclick="deleteTerm(${sanitizedTerm.id})">Delete</button>` : '<span class="small-note">System</span>'}
                </td>
            </tr>`;
        }).join('');
    }

    function submitTermForm() {
        const name = termNameInput.value.trim();
        const description = termDescriptionInput.value.trim();
        const matchPattern = termPatternInput.value.trim();
        const matchType = termTypeSelect?.value || 'WILDCARD';

        if (!name || !description) {
            showTermMessage('Name and description are required.', true);
            return;
        }

        const payload = { name, description, matchPattern, matchType };
        let url = `${contextPath}/admin/terms`;
        let method = 'POST';
        if (isEditingTerm) {
            payload.id = Number(termIdInput.value);
            method = 'PUT';
        }

        fetch(url, { method, headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }, body: JSON.stringify(payload) })
            .then(async response => {
                const data = await response.json();
                if (!response.ok) throw new Error(data.message || 'Unable to save term.');
                showTermMessage(`Term "${data.term?.name || payload.name}" saved.`);
                resetTermForm();
                loadTermList();
            })
            .catch(err => showTermMessage(`Error: ${err.message}`, true));
    }

    window.startTermEdit = function (id, name, description, pattern, type) {
        isEditingTerm = true;
        termIdInput.value = id;
        termNameInput.value = name;
        termDescriptionInput.value = description;
        termPatternInput.value = pattern || '';
        termTypeSelect.value = type || 'WILDCARD';
        if (saveTermBtn) saveTermBtn.textContent = 'Update Term';
        if (cancelTermEditBtn) cancelTermEditBtn.style.display = 'inline-block';
    };

    window.startTermEditFromRow = function (button) {
        if (!button) return;
        const row = button.closest('tr');
        if (!row) return;
        const payload = row.dataset.term;
        if (!payload) {
            showTermMessage('Unable to load term data.', true);
            return;
        }
        try {
            const term = JSON.parse(payload);
            startTermEdit(term.id, term.name, term.description, term.matchPattern, term.matchType);
        } catch (err) {
            showTermMessage('Unable to parse term data for editing.', true);
        }
    };

    function resetTermForm() {
        isEditingTerm = false;
        termIdInput.value = '';
        termForm?.reset();
        if (saveTermBtn) saveTermBtn.textContent = 'Save Term';
        if (cancelTermEditBtn) cancelTermEditBtn.style.display = 'none';
    }

    window.deleteTerm = function (id) {
        if (!confirm('Delete this term?')) return;
        fetch(`${contextPath}/admin/terms?id=${encodeURIComponent(id)}`, { method: 'DELETE', headers: { 'Accept': 'application/json' } })
            .then(async response => {
                const payload = await response.json();
                if (!response.ok) throw new Error(payload.message || 'Unable to delete term.');
                showTermMessage('Term deleted.');
                loadTermList();
            })
            .catch(err => showTermMessage(`Error: ${err.message}`, true));
    };

    function showTermMessage(message, isError = false) {
        if (!termMessageEl) return;
        termMessageEl.textContent = message;
        termMessageEl.style.color = isError ? '#b91c1c' : '#047857';
    }

    /* =========================
       Safe Markdown rendering helpers
       ========================= */
    function renderMarkdownSafe(markdown, containerId) {
        const el = document.getElementById(containerId);
        if (!el) return;
        const md = markdown == null ? '' : String(markdown);
        try {
            const rawHtml = (typeof marked === 'function') ? marked(md) : escapeHtml(md).replace(/\n/g, '<br/>');
            const safeHtml = (typeof DOMPurify !== 'undefined') ? DOMPurify.sanitize(rawHtml) : rawHtml;
            el.innerHTML = safeHtml;
        } catch (e) {
            el.textContent = markdown;
        }
    }

    function safeFetchAndRenderResponse(url, containerId, fetchOptions) {
        fetchOptions = fetchOptions || { method: 'GET', headers: { 'Accept': 'application/json' } };
        fetch(url, fetchOptions)
            .then(resp => resp.json())
            .then(payload => {
                if (payload == null) return;
                if (payload.response_html) {
                    const el = document.getElementById(containerId);
                    if (!el) return;
                    const safe = (typeof DOMPurify !== 'undefined') ? DOMPurify.sanitize(payload.response_html) : payload.response_html;
                    el.innerHTML = safe;
                    return;
                }
                const md = payload.response_text || payload.responseText || payload.text || '';
                renderMarkdownSafe(md, containerId);
            })
            .catch(err => console.warn('Failed to fetch/render response:', err));
    }

    /* =========================
       Placeholder: syncWidgetTables (server-side may implement)
       ========================= */
    async function syncWidgetTables() {
        try {
            const resp = await fetch(`${contextPath}/admin/widgets/sync`, { method: 'POST', headers: { 'Accept': 'application/json' } });
            const payload = await resp.json().catch(() => ({}));
            if (resp.ok && payload.status === 'ok') {
                showWidgetTableExplorerMessage('Sync started.');
                return payload;
            } else {
                throw new Error(payload.message || 'Failed to start sync.');
            }
        } catch (err) {
            showWidgetTableExplorerMessage(`Sync failed: ${err.message}`, true);
            return Promise.reject(err);
        }
    }

    /* =========================
       Utilities
       ========================= */
    function escapeHtml(value) {
        if (value == null) return '';
        return String(value).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    // Expose a couple of helpers globally (used by inline onclicks in markup)
    window.editWidgetEntry = window.editWidgetEntry || window.editWidgetEntry;
    window.deleteWidgetEntry = window.deleteWidgetEntry || window.deleteWidgetEntry;
    window.startTermEditFromRow = window.startTermEditFromRow || window.startTermEditFromRow;
    window.deleteTerm = window.deleteTerm || window.deleteTerm;
    window.deleteUser = window.deleteUser || window.deleteUser;

    // End IIFE
})();
