// sync.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};
    const Api = window.AdminPage.Api;
    const Utils = window.AdminPage.Utils;

    const Sync = {
        init(contextPath) {
            this.contextPath = contextPath || '';
            this.loadSyncInterval();
        },

        async loadSyncInterval() {
            try {
                const { payload, ok } = await Api.fetchJson(`${this.contextPath}/admin/widgets/sync/timer`, { method: 'GET' });
                if (ok && payload?.status === 'ok' && typeof payload.intervalSeconds === 'number') {
                    const minutes = Math.max(1, Math.round(payload.intervalSeconds / 60));
                    const el = document.getElementById('syncInterval');
                    if (el) el.value = minutes;
                    this.showSyncIntervalMessage(`Auto sync runs every ${minutes} minute(s). Last synced: ${Utils.formatHumanReadableTimestamp(payload.lastSynced)}`);
                }
            } catch {
                this.showSyncIntervalMessage('Unable to load auto sync interval.', true);
            }
        },

        async saveSyncInterval() {
            const minutesInput = document.getElementById('syncInterval');
            const minutes = parseInt(minutesInput.value, 10);
            if (isNaN(minutes) || minutes < 1) {
                this.showSyncIntervalMessage('Please enter a valid interval (minimum 1 minute).', true);
                return;
            }
            const seconds = minutes * 60;
            const data = new URLSearchParams();
            data.append('intervalSeconds', seconds);

            try {
                const { payload, ok } = await Api.postUrlEncoded(`${this.contextPath}/admin/widgets/sync/timer`, data);
                if (ok && payload?.status === 'ok') {
                    this.showSyncIntervalMessage(`Auto sync interval set to ${minutes} minute(s). Last synced: ${Utils.formatHumanReadableTimestamp(payload.lastSynced)}`);
                } else {
                    this.showSyncIntervalMessage(payload?.message || 'Unable to update interval.', true);
                }
            } catch (err) {
                this.showSyncIntervalMessage(`Unable to save interval: ${err.message}`, true);
            }
        },

        showSyncIntervalMessage(text, isError = false) {
            const el = document.getElementById('syncIntervalMessage');
            if (el) {
                el.textContent = text;
                el.style.color = isError ? '#b91c1c' : '#047857';
            }
        },

        _sleep(ms) {
            return new Promise(resolve => setTimeout(resolve, ms));
        },

        /**
         * Start a sync job and poll the timer endpoint until lastSynced changes.
         * On completion, notifies Widgets module to refresh explorer details.
         */
        async syncWidgetTables(contextPath, options = {}) {
            const ctx = contextPath || this.contextPath || '';
            const pollIntervalMs = options.pollIntervalMs || 2000;
            const timeoutMs = options.timeoutMs || 60000; // default 60s
            const timerUrl = `${ctx}/admin/widgets/sync/timer`;
            const startUrl = `${ctx}/admin/widgets/sync`;
            const explorerMsgEl = document.getElementById('widgetTableExplorerMessage');

            // Read current lastSynced to detect changes
            let previousLastSynced = null;
            try {
                const timerResp = await Api.fetchJson(timerUrl, { method: 'GET' });
                previousLastSynced = timerResp.payload?.lastSynced ?? null;
            } catch (e) {
                console.warn('Unable to read current sync timer before starting sync:', e);
                previousLastSynced = null;
            }

            try {
                const resp = await fetch(startUrl, { method: 'POST', headers: { 'Accept': 'application/json' }, credentials: 'same-origin' });
                const payload = await resp.json().catch(() => ({}));
                if (!(resp.ok && payload?.status === 'ok')) {
                    const msg = payload?.message || `Failed to start sync (status ${resp.status}).`;
                    if (explorerMsgEl) { explorerMsgEl.textContent = msg; explorerMsgEl.style.color = '#b91c1c'; }
                    throw new Error(msg);
                }

                if (explorerMsgEl) {
                    explorerMsgEl.textContent = 'Sync started. Waiting for completion...';
                    explorerMsgEl.style.color = '#047857';
                }

                const startTime = Date.now();
                while (Date.now() - startTime < timeoutMs) {
                    await this._sleep(pollIntervalMs);
                    try {
                        const poll = await Api.fetchJson(timerUrl, { method: 'GET' });
                        if (poll.ok && poll.payload && typeof poll.payload.lastSynced !== 'undefined') {
                            const newLast = poll.payload.lastSynced;
                            if (newLast && newLast !== previousLastSynced) {
                                if (explorerMsgEl) {
                                    explorerMsgEl.textContent = `Sync completed. Last synced: ${Utils.formatHumanReadableTimestamp(newLast)}`;
                                    explorerMsgEl.style.color = '#047857';
                                }
                                // Notify Widgets to refresh statuses
                                try {
                                    if (window.AdminPage && window.AdminPage.Widgets && typeof window.AdminPage.Widgets.fetchWidgetStatuses === 'function') {
                                        // fetch newest statuses and pass lastSynced
                                        await window.AdminPage.Widgets.fetchWidgetStatuses();
                                        if (typeof window.AdminPage.Widgets.handleSyncCompletion === 'function') {
                                            window.AdminPage.Widgets.handleSyncCompletion(newLast);
                                        }
                                    }
                                } catch (e) {
                                    console.warn('Widgets post-sync update threw:', e);
                                }
                                return poll.payload;
                            }
                        }
                    } catch (e) {
                        console.warn('Error polling sync timer:', e);
                    }
                }

                const timeoutMsg = 'Sync did not complete in time. It may be running in the background. Refresh explorer later.';
                if (explorerMsgEl) { explorerMsgEl.textContent = timeoutMsg; explorerMsgEl.style.color = '#b91c1c'; }
                try {
                    if (window.AdminPage && window.AdminPage.Widgets && typeof window.AdminPage.Widgets.handleSyncTimeout === 'function') {
                        window.AdminPage.Widgets.handleSyncTimeout();
                    }
                } catch (e) {
                    console.warn('Widgets.handleSyncTimeout threw:', e);
                }
                return Promise.reject(new Error(timeoutMsg));
            } catch (err) {
                if (explorerMsgEl) { explorerMsgEl.textContent = `Sync failed: ${err.message}`; explorerMsgEl.style.color = '#b91c1c'; }
                return Promise.reject(err);
            }
        }
    };

    window.AdminPage.Sync = Sync;
})();
