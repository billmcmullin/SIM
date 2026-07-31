// widgets.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};
    const Api = window.AdminPage.Api;
    const Utils = window.AdminPage.Utils;

    const Widgets = {
        init(config) {
            this.contextPath = config.contextPath || '';
            this.widgetState = { editingId: null, widgets: Array.isArray(config.widgetList) ? config.widgetList : [] };
            this.widgetSyncStatuses = {}; // keyed by widgetId: { tableExists, count, synced, lastSynced, message }
            this.widgetMessageEl = document.getElementById('widgetMessage');
            this.widgetSearchInput = document.getElementById('widgetSearch');
            this.widgetSelectAll = document.getElementById('widgetSelectAll');
            this.widgetTableExplorerBody = document.getElementById('widgetTableExplorerBody');
            this.widgetTableExplorerMessage = document.getElementById('widgetTableExplorerMessage');

            // expose handlers for inline onclicks
            window.editWidgetEntry = this.editWidgetEntry.bind(this);
            window.deleteWidgetEntry = this.deleteWidgetEntry.bind(this);

            // Wire UI buttons
            document.getElementById('saveWidgetEntryBtn')?.addEventListener('click', () => this.saveWidgetEntry());
            document.getElementById('clearWidgetFormBtn')?.addEventListener('click', () => this.clearWidgetForm());
            document.getElementById('deleteSelectedWidgetsBtn')?.addEventListener('click', () => this.deleteSelectedWidgets());
            document.getElementById('saveSyncIntervalBtn')?.addEventListener('click', () => window.AdminPage.Sync.saveSyncInterval());

            document.getElementById('searchWidgetsBtn')?.addEventListener('click', () => this.reloadWidgetList());
            document.getElementById('clearWidgetSearchBtn')?.addEventListener('click', () => {
                if (this.widgetSearchInput) {
                    this.widgetSearchInput.value = '';
                }
                this.reloadWidgetList();
            });

            this.widgetSearchInput?.addEventListener('keydown', (event) => {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    this.reloadWidgetList();
                }
            });

            const syncBtn = document.getElementById('syncWidgetTablesBtn');
            if (syncBtn) {
                syncBtn.addEventListener('click', () => {
                    syncBtn.disabled = true;
                    this.markSyncInProgress();
                    const syncModule = window.AdminPage?.Sync;
                    if (!syncModule || typeof syncModule.syncWidgetTables !== 'function') {
                        console.warn('Sync module not available');
                        syncBtn.disabled = false;
                        return;
                    }
                    syncModule.syncWidgetTables(this.contextPath, { pollIntervalMs: 2000, timeoutMs: 60000 })
                        .then(async (timerPayload) => {
                            await this.reloadWidgetList();
                            await this.fetchWidgetStatuses().catch(() => { });
                            const last = timerPayload?.lastSynced ?? null;
                            if (last && this.widgetTableExplorerMessage) {
                                this.widgetTableExplorerMessage.textContent = `Sync completed. Last synced: ${Utils.formatHumanReadableTimestamp(last)}`;
                                this.widgetTableExplorerMessage.style.color = '#047857';
                            }
                        })
                        .catch((err) => {
                            console.warn('Sync error or timeout:', err);
                            this.fetchWidgetStatuses().catch(() => { });
                        })
                        .finally(() => {
                            syncBtn.disabled = false;
                        });
                });
            }

            if (this.widgetSelectAll) {
                this.widgetSelectAll.addEventListener('change', event => {
                    const checked = event.target.checked;
                    document.querySelectorAll('.widget-select').forEach(cb => (cb.checked = checked));
                });
            }

            // Initial render & load
            this.renderWidgetTable();
            this.renderWidgetTableExplorer();

            // Load widget list and then check table statuses (gracefully handle missing endpoint)
            this.reloadWidgetList().then(() => {
                this.fetchWidgetStatuses().catch(err => {
                    console.warn('fetchWidgetStatuses error:', err);
                });
            });
        },

        // Fetch statuses via existing server servlet: /admin/widgets/table-check
        // Try bulk: ?ids=id1,id2,...; fall back to per-widget ?widgetId=...
        async fetchWidgetStatuses() {
            const widgets = this.widgetState.widgets || [];
            if (!widgets.length) {
                return;
            }

            const widgetIds = widgets.map(w => w.widgetId).filter(Boolean);
            if (!widgetIds.length) {
                return;
            }

            const bulkUrl = `${this.contextPath}/admin/widgets/table-check?ids=${encodeURIComponent(widgetIds.join(','))}`;
            try {
                const { payload, ok, status } = await Api.fetchJson(bulkUrl, { method: 'GET' });
                if (ok && payload?.status === 'ok' && Array.isArray(payload.statuses)) {
                    payload.statuses.forEach(s => {
                        const id = s.widgetId;
                        const prev = this.widgetSyncStatuses[id] || {};
                        this.widgetSyncStatuses[id] = {
                            tableExists: Boolean(s.tableExists),
                            count: (typeof s.count === 'number') ? s.count : (s.tableExists ? 0 : null),
                            synced: (typeof s.synced === 'boolean') ? s.synced : Boolean(prev.synced),
                            lastSynced: s.lastSynced || prev.lastSynced || null,
                            message: s.message || ''
                        };
                    });
                    this.widgetTableExplorerMessage && (this.widgetTableExplorerMessage.textContent = '');
                    this.renderWidgetTableExplorer();
                    return;
                } else if (status === 404) {
                    throw new Error('Bulk status endpoint not found (404)');
                } else if (payload && payload.status === 'error') {
                    // server-side error; fall back to per-widget if possible
                    console.warn('Bulk status returned unexpected payload, falling back to per-widget ', payload);
                }
            } catch (err) {
                console.warn('Bulk status fetch failed or unexpected; falling back to per-widget', err);
            }

            // Per-widget fallback
            let anyStatusFetched = false;
            for (const wid of widgetIds) {
                const url = `${this.contextPath}/admin/widgets/table-check?widgetId=${encodeURIComponent(wid)}`;
                try {
                    const { payload, ok, status } = await Api.fetchJson(url, { method: 'GET' });
                    if (ok && payload?.status === 'ok') {
                        // payload: {status:"ok", widgetId, tableName, tableExists, count, ...}
                        const s = payload;
                        const prev = this.widgetSyncStatuses[wid] || {};
                        this.widgetSyncStatuses[wid] = {
                            tableExists: Boolean(s.tableExists),
                            count: (typeof s.count === 'number') ? s.count : (s.tableExists ? 0 : null),
                            synced: (typeof s.synced === 'boolean') ? s.synced : Boolean(prev.synced),
                            lastSynced: s.lastSynced || prev.lastSynced || null,
                            message: s.message || ''
                        };
                        anyStatusFetched = true;
                    } else {
                        // Keep previous status on endpoint errors to avoid false negatives.
                        const prev = this.widgetSyncStatuses[wid] || {};
                        this.widgetSyncStatuses[wid] = {
                            tableExists: prev.tableExists,
                            count: prev.count ?? null,
                            synced: Boolean(prev.synced),
                            lastSynced: prev.lastSynced || null,
                            message: prev.message || ''
                        };
                        if (status === 404) {
                            console.warn('Per-widget status endpoint returned 404 for', wid);
                        }
                    }
                } catch (err) {
                    console.warn('Per-widget status fetch failed for', wid, err);
                    const prev = this.widgetSyncStatuses[wid] || {};
                    this.widgetSyncStatuses[wid] = {
                        tableExists: prev.tableExists,
                        count: prev.count ?? null,
                        synced: Boolean(prev.synced),
                        lastSynced: prev.lastSynced || null,
                        message: prev.message || ''
                    };
                }
            }

            if (!anyStatusFetched) {
                // No status data available: show explanatory message and let admin run sync
                const msgEl = this.widgetTableExplorerMessage;
                if (msgEl) {
                    msgEl.innerHTML = `Table check endpoint not available or returned errors. You can still run Sync to attempt to create/check tables.`;
                    msgEl.style.color = '#b91c1c';
                }
            } else {
                if (this.widgetTableExplorerMessage) {
                    this.widgetTableExplorerMessage.textContent = '';
                }
            }

            this.renderWidgetTableExplorer();
        },

        markSyncInProgress() {
            if (this.widgetTableExplorerMessage) {
                this.widgetTableExplorerMessage.textContent = 'Syncing widget tables…';
                this.widgetTableExplorerMessage.style.color = '#047857';
            }
            (this.widgetState.widgets || []).forEach(entry => {
                const prev = this.widgetSyncStatuses[entry.widgetId] || {};
                this.widgetSyncStatuses[entry.widgetId] = {
                    tableExists: (typeof prev.tableExists === 'boolean') ? prev.tableExists : undefined,
                    count: prev.count ?? null,
                    synced: false,
                    lastSynced: prev.lastSynced || null,
                    message: 'Syncing…'
                };
            });
            this.renderWidgetTableExplorer();
        },

        applySyncStatuses(syncStatuses, options = {}) {
            if (!Array.isArray(syncStatuses) || !syncStatuses.length) {
                return;
            }

            const lastSynced = options.lastSynced || null;
            syncStatuses.forEach((status) => {
                const wid = status?.widgetId;
                if (!wid) {
                    return;
                }
                const prev = this.widgetSyncStatuses[wid] || {};
                const tableExists = (typeof status.tableExists === 'boolean') ? status.tableExists : prev.tableExists;
                const message = status.message || '';
                const synced = (typeof status.synced === 'boolean')
                    ? status.synced
                    : (tableExists === true && message.toLowerCase().indexOf('failed') === -1);

                this.widgetSyncStatuses[wid] = {
                    tableExists,
                    count: prev.count ?? null,
                    synced,
                    lastSynced: status.lastSynced || lastSynced || prev.lastSynced || null,
                    message
                };
            });

            this.renderWidgetTableExplorer();
        },

        async handleSyncCompletion(lastSynced) {
            try {
                await this.fetchWidgetStatuses();
            } catch (e) {
                console.warn('fetchWidgetStatuses after sync failed:', e);
            }

            if (this.widgetTableExplorerMessage) {
                const msg = lastSynced ? `Sync completed. Last synced: ${Utils.formatHumanReadableTimestamp(lastSynced)}` : 'Sync completed.';
                this.widgetTableExplorerMessage.textContent = msg;
                this.widgetTableExplorerMessage.style.color = '#047857';
            }
            (this.widgetState.widgets || []).forEach(entry => {
                const s = this.widgetSyncStatuses[entry.widgetId] || {};
                if (typeof s.synced !== 'boolean') {
                    s.synced = Boolean(s.tableExists);
                }
                s.lastSynced = lastSynced || s.lastSynced || null;
                if (s.message === 'Syncing…') {
                    s.message = '';
                }
                this.widgetSyncStatuses[entry.widgetId] = s;
            });
            this.renderWidgetTableExplorer();
        },

        handleSyncTimeout() {
            if (this.widgetTableExplorerMessage) {
                this.widgetTableExplorerMessage.textContent = 'Sync timed out or failed. Check server logs or try again.';
                this.widgetTableExplorerMessage.style.color = '#b91c1c';
            }
            (this.widgetState.widgets || []).forEach(entry => {
                const s = this.widgetSyncStatuses[entry.widgetId] || {};
                if (s.message === 'Syncing…') {
                    s.message = '';
                }
                this.widgetSyncStatuses[entry.widgetId] = s;
            });
            this.renderWidgetTableExplorer();
        },

        renderWidgetTable() {
            const tbody = document.getElementById('widgetTableBody');
            if (!tbody) {
                return;
            }

            if (!this.widgetState.widgets.length) {
                tbody.innerHTML = '<tr><td colspan="4" class="empty-row">No widget entries available.</td></tr>';
            } else {
                tbody.innerHTML = this.widgetState.widgets.map(entry => `<tr>
                    <td><input type="checkbox" class="widget-select" value="${entry.id}"></td>
                    <td>${Utils.escapeHtml(entry.widgetId)}</td>
                    <td>${Utils.escapeHtml(entry.displayName)}</td>
                    <td class="actions">
                        <button type="button" class="ghost-btn" onclick="editWidgetEntry(${entry.id})">Edit</button>
                        <button type="button" class="ghost-btn" onclick="deleteWidgetEntry(${entry.id})">Delete</button>
                    </td>
                </tr>`).join('');
            }

            if (this.widgetSelectAll) {
                this.widgetSelectAll.checked = false;
            }
            this.renderWidgetTableExplorer();
        },

        renderWidgetTableExplorer() {
            if (!this.widgetTableExplorerBody) {
                return;
            }
            if (!this.widgetState.widgets.length) {
                this.widgetTableExplorerBody.innerHTML = '<tr><td colspan="4" class="empty-row">Widget IDs will appear here once the registry loads.</td></tr>';
                return;
            }

            this.widgetTableExplorerBody.innerHTML = this.widgetState.widgets.map(entry => {
                const status = this.widgetSyncStatuses[entry.widgetId];
                const tableStatus = status
                    ? (typeof status.tableExists === 'boolean' ? (status.tableExists ? 'Tables Ready' : 'Tables Missing') : 'Not checked')
                    : 'Not checked';
                const syncStatus = status ? (status.synced ? `Synced${status.lastSynced ? ' (last ' + Utils.formatHumanReadableTimestamp(status.lastSynced) + ')' : ''}` : (status.message || 'Pending sync')) : 'Awaiting sync';
                let details;
                if (!status) {
                    details = 'N/A';
                } else if (!status.tableExists) {
                    details = 'N/A';
                } else {
                    const cnt = (typeof status.count === 'number') ? status.count : 0;
                    details = `${cnt}`;
                }
                const message = status?.message ? `<div class="small-note">${Utils.escapeHtml(status.message)}</div>` : '';
                return `<tr>
                    <td>${Utils.escapeHtml(entry.widgetId)}</td>
                    <td>${tableStatus}</td>
                    <td>${syncStatus}</td>
                    <td>${details}${message}</td>
                </tr>`;
            }).join('');
        },

        async reloadWidgetList() {
            const filter = this.widgetSearchInput ? this.widgetSearchInput.value.trim() : '';
            const endpoint = `${this.contextPath}/admin/widgets${filter ? '?filter=' + encodeURIComponent(filter) : ''}`;
            try {
                const { payload, ok } = await Api.fetchJson(endpoint, { method: 'GET' });
                if (!ok || payload?.status !== 'ok') {
                    this.showWidgetMessage(payload?.message || 'Unable to load widgets.', true);
                    return Promise.reject(new Error(payload?.message || 'Unable to load widgets.'));
                }
                this.widgetState.widgets = payload.widgets || [];
                (this.widgetState.widgets || []).forEach(entry => {
                    this.widgetSyncStatuses[entry.widgetId] = this.widgetSyncStatuses[entry.widgetId] || { tableExists: undefined, count: null, synced: false, message: '' };
                });
                this.renderWidgetTable();
                this.showWidgetMessage(`Loaded ${this.widgetState.widgets.length} widget(s).`);
                return Promise.resolve();
            } catch (err) {
                this.showWidgetMessage(`Could not load widgets: ${err.message}`, true);
                return Promise.reject(err);
            }
        },

        editWidgetEntry(id) {
            const entry = this.widgetState.widgets.find(w => w.id === id);
            if (!entry) {
                this.showWidgetMessage('Widget not found.', true);
                return;
            }
            this.populateWidgetForm(entry);
        },

        async deleteWidgetEntry(id) {
            if (!confirm('Delete this widget?')) {
                return;
            }
            try {
                const url = `${this.contextPath}/admin/widgets?ids=${encodeURIComponent(String(id))}`;
                const { ok, payload, status } = await Api.delete(url);

                if (ok && payload?.status === 'ok') {
                    this.showWidgetMessage('Widget deleted.');
                    await this.reloadWidgetList();
                    await this.fetchWidgetStatuses().catch(() => { });
                } else {
                    throw new Error(payload?.message || `Unable to delete widget (status ${status}).`);
                }
            } catch (err) {
                this.showWidgetMessage(`Error: ${err.message}`, true);
            }
        },

        async deleteSelectedWidgets() {
            const selected = Array.from(document.querySelectorAll('.widget-select:checked')).map(cb => cb.value);
            if (!selected.length) {
                this.showWidgetMessage('No widgets selected.', true);
                return;
            }
            if (!confirm(`Delete ${selected.length} widget(s)?`)) {
                return;
            }

            try {
                const idsParam = encodeURIComponent(selected.join(','));
                const url = `${this.contextPath}/admin/widgets?ids=${idsParam}`;
                const { ok, payload, status } = await Api.delete(url);
                if (ok && payload?.status === 'ok') {
                    this.showWidgetMessage('Selected widgets deleted.');
                    await this.reloadWidgetList();
                    await this.fetchWidgetStatuses().catch(() => { });
                } else {
                    throw new Error(payload?.message || `Unable to delete selected widgets (status ${status}).`);
                }
            } catch (err) {
                this.showWidgetMessage(`Error: ${err.message}`, true);
            }
        },

        clearWidgetForm() {
            this.widgetState.editingId = null;
            document.getElementById('widgetIdInput').value = '';
            document.getElementById('widgetNameInput').value = '';
            this.showWidgetMessage('Ready to add a new widget.');
        },

        populateWidgetForm(entry) {
            this.widgetState.editingId = entry ? entry.id : null;
            document.getElementById('widgetIdInput').value = entry ? entry.widgetId : '';
            document.getElementById('widgetNameInput').value = entry ? entry.displayName : '';
            this.showWidgetMessage(entry ? `Editing widget ${entry.widgetId}.` : 'Ready to add a new widget.');
        },

        async saveWidgetEntry() {
            const widgetIdInput = document.getElementById('widgetIdInput');
            const widgetNameInput = document.getElementById('widgetNameInput');
            const widgetIdValue = (widgetIdInput?.value || '').trim();
            const widgetNameValue = (widgetNameInput?.value || '').trim();

            if (!widgetIdValue || !widgetNameValue) {
                this.showWidgetMessage('Widget ID and name are required.', true);
                return;
            }

            const data = new URLSearchParams();
            data.append('widgetId', widgetIdValue);
            data.append('displayName', widgetNameValue);
            if (this.widgetState.editingId) {
                data.append('id', this.widgetState.editingId);
            }

            try {
                const { status, payload } = await Api.fetchJson(`${this.contextPath}/admin/widgets`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
                    body: data.toString()
                });
                if (payload?.status === 'ok') {
                    this.showWidgetMessage(this.widgetState.editingId ? 'Widget updated.' : 'Widget added.');
                    this.widgetState.editingId = null;
                    await this.reloadWidgetList();
                    this.clearWidgetForm();
                    await this.fetchWidgetStatuses().catch(() => { });
                } else {
                    this.showWidgetMessage(payload?.message || `Unable to save widget (status ${status}).`, true);
                }
            } catch (err) {
                this.showWidgetMessage(`Save failed: ${err.message}`, true);
            }
        },

        showWidgetMessage(text, isError = false) {
            if (!this.widgetMessageEl) {
                return;
            }
            this.widgetMessageEl.textContent = text;
            this.widgetMessageEl.style.color = isError ? '#b91c1c' : '#047857';
        }
    };

    window.AdminPage.Widgets = Widgets;
})();
