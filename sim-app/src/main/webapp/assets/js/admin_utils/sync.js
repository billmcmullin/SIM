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
                    if (el) {
                        el.value = minutes;
                    }
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

        _formatElapsedSeconds(seconds) {
            const total = Math.max(0, Number(seconds) || 0);
            const hrs = Math.floor(total / 3600);
            const mins = Math.floor((total % 3600) / 60);
            const secs = total % 60;
            if (hrs > 0) {
                return `${hrs}h ${mins}m ${secs}s`;
            }
            if (mins > 0) {
                return `${mins}m ${secs}s`;
            }
            return `${secs}s`;
        },

        _formatPhaseLabel(phase) {
            const key = (phase || '').toLowerCase();
            switch (key) {
                case 'manual_sync':
                case 'scheduled_sync':
                case 'syncing_widgets':
                    return 'Syncing widgets';
                case 'summary_generation':
                    return 'Generating summary';
                case 'manual_summary_retry':
                    return 'Manual summary retry';
                case 'completed':
                    return 'Completed';
                case 'failed':
                    return 'Failed';
                default:
                    return 'Running';
            }
        },

        _buildProgressMessage(payload, watchingExistingSync) {
            if (!payload || typeof payload !== 'object') {
                return '';
            }

            const percent = Number.isFinite(payload.progressPercent) ? Math.max(0, Math.min(100, payload.progressPercent)) : null;
            const total = Number.isFinite(payload.widgetsTotal) ? Math.max(0, payload.widgetsTotal) : 0;
            const completed = Number.isFinite(payload.widgetsCompleted) ? Math.max(0, payload.widgetsCompleted) : 0;
            const failed = Number.isFinite(payload.widgetsFailed) ? Math.max(0, payload.widgetsFailed) : 0;
            const runningSeconds = Number.isFinite(payload.runningSeconds) ? Math.max(0, payload.runningSeconds) : 0;
            const phaseLabel = this._formatPhaseLabel(payload.syncPhase);
            const syncMessage = (payload.syncMessage || '').trim();

            const parts = [];
            if (percent !== null) {
                parts.push(`${percent}%`);
            }
            if (total > 0) {
                parts.push(`${Math.min(completed, total)}/${total} widgets`);
            }
            if (failed > 0) {
                parts.push(`${failed} failed`);
            }
            if (runningSeconds > 0) {
                parts.push(`${this._formatElapsedSeconds(runningSeconds)} elapsed`);
            }

            const prefix = watchingExistingSync
                ? 'Sync already running on server'
                : 'Sync in progress';

            let line = `${prefix}: ${phaseLabel}`;
            if (parts.length > 0) {
                line += ` (${parts.join(', ')})`;
            }
            if (syncMessage) {
                line += `. ${syncMessage}`;
            }
            return line;
        },

        /**
         * Start a sync job and poll the timer endpoint until server reports completion.
         * On completion, notifies Widgets module to refresh explorer details.
         */
        async syncWidgetTables(contextPath, options = {}) {
            const ctx = contextPath || this.contextPath || '';
            const pollIntervalMs = options.pollIntervalMs || 2000;
            const timeoutMs = options.timeoutMs || 60000; // default 60s
            const timerUrl = `${ctx}/admin/widgets/sync/timer`;
            const startUrl = `${ctx}/admin/widgets/sync`;
            const explorerMsgEl = document.getElementById('widgetTableExplorerMessage');
            let startPayload = null;
            let appliedFromSyncResponse = false;
            let watchingExistingSync = false;

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
                startPayload = await resp.json().catch(() => ({}));
                if (!(resp.ok && startPayload?.status === 'ok')) {
                    const msg = startPayload?.message || `Failed to start sync (status ${resp.status}).`;
                    const isAlreadyRunning = resp.status === 409 && typeof msg === 'string'
                        && msg.toLowerCase().indexOf('already in progress') !== -1;

                    if (!isAlreadyRunning) {
                        if (explorerMsgEl) { explorerMsgEl.textContent = msg; explorerMsgEl.style.color = '#b91c1c'; }
                        throw new Error(msg);
                    }

                    watchingExistingSync = true;
                    if (explorerMsgEl) {
                        explorerMsgEl.textContent = 'A sync is already running on the server. Waiting for it to finish...';
                        explorerMsgEl.style.color = '#047857';
                    }
                }

                if (!watchingExistingSync) {
                    try {
                        if (window.AdminPage && window.AdminPage.Widgets && typeof window.AdminPage.Widgets.applySyncStatuses === 'function') {
                            window.AdminPage.Widgets.applySyncStatuses(startPayload?.widgetStatus || []);
                            appliedFromSyncResponse = Array.isArray(startPayload?.widgetStatus) && startPayload.widgetStatus.length > 0;
                        }
                    } catch (e) {
                        console.warn('Unable to apply sync response statuses:', e);
                    }
                }

                if (explorerMsgEl) {
                    if (watchingExistingSync) {
                        explorerMsgEl.textContent = 'Sync already in progress. Waiting for completion...';
                    } else {
                        explorerMsgEl.textContent = appliedFromSyncResponse
                            ? 'Sync completed on server. Finalizing status...'
                            : 'Sync started. Waiting for completion...';
                    }
                    explorerMsgEl.style.color = '#047857';
                }

                const effectiveTimeoutMs = watchingExistingSync ? Math.max(timeoutMs, 300000) : timeoutMs;
                const startTime = Date.now();
                while (Date.now() - startTime < effectiveTimeoutMs) {
                    await this._sleep(pollIntervalMs);
                    try {
                        const poll = await Api.fetchJson(timerUrl, { method: 'GET' });
                        if (poll.ok && poll.payload && typeof poll.payload.lastSynced !== 'undefined') {
                            const payload = poll.payload;
                            const newLast = payload.lastSynced;
                            const hasSyncRunning = typeof payload.syncRunning === 'boolean';
                            const syncRunning = hasSyncRunning ? payload.syncRunning : null;
                            const phase = typeof payload.syncPhase === 'string' ? payload.syncPhase.toLowerCase() : '';
                            const progressText = this._buildProgressMessage(payload, watchingExistingSync);

                            if (explorerMsgEl && progressText) {
                                explorerMsgEl.textContent = progressText;
                                explorerMsgEl.style.color = '#047857';
                            }

                            const runFailed = hasSyncRunning && syncRunning === false && phase === 'failed';
                            if (runFailed) {
                                const failMsg = (payload.syncMessage || 'Sync failed on server.').trim();
                                if (explorerMsgEl) {
                                    explorerMsgEl.textContent = failMsg;
                                    explorerMsgEl.style.color = '#b91c1c';
                                }
                                return Promise.reject(new Error(failMsg));
                            }

                            const lastSyncedChanged = Boolean(newLast && newLast !== previousLastSynced);
                            const existingRunFinished = watchingExistingSync && hasSyncRunning && syncRunning === false;
                            const startedRunFinished = !watchingExistingSync && hasSyncRunning && syncRunning === false;

                            if (lastSyncedChanged || existingRunFinished || startedRunFinished) {
                                if (explorerMsgEl) {
                                    if (newLast) {
                                        explorerMsgEl.textContent = `Sync completed. Last synced: ${Utils.formatHumanReadableTimestamp(newLast)}`;
                                        explorerMsgEl.style.color = '#047857';
                                    } else {
                                        explorerMsgEl.textContent = 'Sync finished, but no lastSynced timestamp was returned. Check sync status below.';
                                        explorerMsgEl.style.color = '#b45309';
                                    }
                                }
                                // Notify Widgets to refresh statuses
                                try {
                                    if (window.AdminPage && window.AdminPage.Widgets && typeof window.AdminPage.Widgets.fetchWidgetStatuses === 'function') {
                                        // fetch newest statuses and pass lastSynced
                                        await window.AdminPage.Widgets.fetchWidgetStatuses();
                                        if (typeof window.AdminPage.Widgets.handleSyncCompletion === 'function') {
                                            await window.AdminPage.Widgets.handleSyncCompletion(newLast);
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

                if (appliedFromSyncResponse) {
                    const timeoutInfo = 'Sync completed, but timer confirmation was delayed. Statuses were updated from sync results.';
                    if (explorerMsgEl) { explorerMsgEl.textContent = timeoutInfo; explorerMsgEl.style.color = '#b45309'; }
                    try {
                        if (window.AdminPage && window.AdminPage.Widgets && typeof window.AdminPage.Widgets.fetchWidgetStatuses === 'function') {
                            await window.AdminPage.Widgets.fetchWidgetStatuses();
                        }
                    } catch (e) {
                        console.warn('Post-timeout status refresh failed:', e);
                    }
                    return startPayload || { status: 'ok' };
                }

                const timeoutMsg = watchingExistingSync
                    ? 'A sync is still running on the server. Please wait and check again shortly.'
                    : 'Sync did not complete in time. It may be running in the background. Refresh explorer later.';
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
