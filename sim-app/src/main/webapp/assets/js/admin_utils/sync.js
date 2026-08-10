// sync.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};
    const Api = window.AdminPage.Api;
    const Utils = window.AdminPage.Utils;

    const Sync = {
        summaryAutoToggleInFlight: false,

        init(contextPath) {
            this.contextPath = contextPath || '';
            document.getElementById('saveSummarySettingsBtn')?.addEventListener('click', () => this.saveSummarySettings());
            document.getElementById('summaryAutoToggleBtn')?.addEventListener('click', () => this.toggleSummaryAutoEnabled());
            document.getElementById('adminSummaryRetryBtn')?.addEventListener('click', () => this.runManualSummaryRetry());
            this.loadSyncInterval();
        },

        async loadSyncInterval() {
            try {
                const { payload, ok } = await Api.fetchJson(`${this.contextPath}/admin/widgets/sync/timer`, { method: 'GET' });
                if (ok && payload?.status === 'ok') {
                    if (typeof payload.intervalSeconds === 'number') {
                        const minutes = Math.max(1, Math.round(payload.intervalSeconds / 60));
                        const el = document.getElementById('syncInterval');
                        if (el) {
                            el.value = minutes;
                        }
                        this.showSyncIntervalMessage(`Auto sync runs every ${minutes} minute(s). Last synced: ${Utils.formatHumanReadableTimestamp(payload.lastSynced)}`);
                    }
                    this.applySummarySettings(payload);
                    this.renderWidgetSyncProgress(payload, false);
                }
            } catch {
                this.showSyncIntervalMessage('Unable to load auto sync interval.', true);
                this.showSummarySettingsMessage('Unable to load summary settings.', true);
            }
        },

        applySummarySettings(payload) {
            if (!payload || typeof payload !== 'object') {
                return;
            }

            if (typeof payload.summaryIntervalSeconds === 'number') {
                const intervalMinutes = Math.max(1, Math.round(payload.summaryIntervalSeconds / 60));
                const summaryIntervalEl = document.getElementById('summaryInterval');
                if (summaryIntervalEl) {
                    summaryIntervalEl.value = intervalMinutes;
                }
            }

            if (typeof payload.summaryMaxRows === 'number') {
                const summaryMaxRowsEl = document.getElementById('summaryMaxRows');
                if (summaryMaxRowsEl) {
                    summaryMaxRowsEl.value = payload.summaryMaxRows;
                }
            }

            if (typeof payload.summaryMaxUpstreamEntries === 'number') {
                const summaryMaxUpstreamEntriesEl = document.getElementById('summaryMaxUpstreamEntries');
                if (summaryMaxUpstreamEntriesEl) {
                    summaryMaxUpstreamEntriesEl.value = payload.summaryMaxUpstreamEntries;
                }
            }

            if (typeof payload.summaryMaxMessageChars === 'number') {
                const summaryMaxMessageCharsEl = document.getElementById('summaryMaxMessageChars');
                if (summaryMaxMessageCharsEl) {
                    summaryMaxMessageCharsEl.value = payload.summaryMaxMessageChars;
                }
            }

            if (typeof payload.summaryMaxRequestBytes === 'number') {
                const summaryMaxRequestBytesEl = document.getElementById('summaryMaxRequestBytes');
                if (summaryMaxRequestBytesEl) {
                    summaryMaxRequestBytesEl.value = payload.summaryMaxRequestBytes;
                }
            }

            if (typeof payload.summaryPrompt === 'string') {
                const promptEl = document.getElementById('summaryPromptTemplate');
                if (promptEl) {
                    promptEl.value = payload.summaryPrompt;
                }
            }

            this.updateSummaryAutoToggleButton(payload.summaryAutoEnabled !== false);
            this.renderSummaryRuntimeStatus(payload);
        },

        updateSummaryAutoToggleButton(autoEnabled) {
            const toggleBtn = document.getElementById('summaryAutoToggleBtn');
            const enabled = autoEnabled !== false;

            if (toggleBtn) {
                toggleBtn.dataset.autoEnabled = enabled ? 'true' : 'false';
                toggleBtn.textContent = enabled ? 'Disable Automatic Summary' : 'Enable Automatic Summary';
                toggleBtn.disabled = this.summaryAutoToggleInFlight;
            }

            const badgeEl = document.getElementById('summaryAutoModeBadge');
            if (badgeEl) {
                badgeEl.textContent = enabled ? 'Auto Summary: Enabled' : 'Auto Summary: Disabled';
                badgeEl.style.color = enabled ? '#047857' : '#6b7280';
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

        showSummarySettingsMessage(text, isError = false) {
            const el = document.getElementById('summarySettingsMessage');
            if (el) {
                el.textContent = text;
                el.style.color = isError ? '#b91c1c' : '#047857';
            }
        },

        renderSummaryRuntimeStatus(payload) {
            const statusEl = document.getElementById('summaryRuntimeStatus');
            if (!statusEl || !payload) {
                return;
            }

            const autoEnabled = payload.summaryAutoEnabled !== false;
            const paused = Boolean(payload.summaryAutoPaused);
            const reason = (payload.summaryAutoPausedReason || '').trim();
            const lastRunAt = Utils.formatHumanReadableTimestamp(payload.summaryLastRunAt);
            const nextRunAt = Utils.formatHumanReadableTimestamp(payload.summaryNextRunAt);

            if (!autoEnabled) {
                statusEl.textContent = `Status: disabled. Automatic summary generation is off. Last run: ${lastRunAt}. Use Generate Summary Now for manual runs.`;
                statusEl.style.color = '#6b7280';
                return;
            }

            if (paused) {
                statusEl.textContent = `Status: paused until admin generates a summary. Last run: ${lastRunAt}. Reason: ${reason || 'manual summary generation required'}.`;
                statusEl.style.color = '#b45309';
                return;
            }

            statusEl.textContent = `Status: active. Last run: ${lastRunAt}. Next run: ${nextRunAt}.`;
            statusEl.style.color = '#047857';
        },

        async toggleSummaryAutoEnabled() {
            const toggleBtn = document.getElementById('summaryAutoToggleBtn');
            if (!toggleBtn || this.summaryAutoToggleInFlight) {
                return;
            }

            const currentlyEnabled = toggleBtn.dataset.autoEnabled !== 'false';
            const targetEnabled = !currentlyEnabled;
            const actionLabel = targetEnabled ? 'Enabling' : 'Disabling';

            this.summaryAutoToggleInFlight = true;
            this.updateSummaryAutoToggleButton(currentlyEnabled);
            this.showSummarySettingsMessage(`${actionLabel} automatic summary...`);

            const data = new URLSearchParams();
            data.append('summaryAutoEnabled', String(targetEnabled));

            try {
                const { payload, ok } = await Api.postUrlEncoded(`${this.contextPath}/admin/widgets/sync/timer`, data);
                if (ok && payload?.status === 'ok') {
                    this.applySummarySettings(payload);
                    this.showSummarySettingsMessage(
                        targetEnabled
                            ? 'Automatic summary generation enabled.'
                            : 'Automatic summary generation disabled.'
                    );
                    return;
                }

                this.showSummarySettingsMessage(payload?.message || 'Unable to update automatic summary setting.', true);
            } catch (err) {
                this.showSummarySettingsMessage(`Unable to update automatic summary setting: ${err.message}`, true);
            } finally {
                this.summaryAutoToggleInFlight = false;
                await this.loadSyncInterval();
            }
        },

        async saveSummarySettings() {
            const summaryIntervalEl = document.getElementById('summaryInterval');
            const summaryMaxRowsEl = document.getElementById('summaryMaxRows');
            const summaryMaxUpstreamEntriesEl = document.getElementById('summaryMaxUpstreamEntries');
            const summaryMaxMessageCharsEl = document.getElementById('summaryMaxMessageChars');
            const summaryMaxRequestBytesEl = document.getElementById('summaryMaxRequestBytes');
            const promptEl = document.getElementById('summaryPromptTemplate');

            const intervalMinutes = parseInt(summaryIntervalEl?.value, 10);
            const maxRows = parseInt(summaryMaxRowsEl?.value, 10);
            const maxUpstreamEntries = parseInt(summaryMaxUpstreamEntriesEl?.value, 10);
            const maxMessageChars = parseInt(summaryMaxMessageCharsEl?.value, 10);
            const maxRequestBytes = parseInt(summaryMaxRequestBytesEl?.value, 10);
            const prompt = (promptEl?.value || '').trim();

            if (isNaN(intervalMinutes) || intervalMinutes < 5) {
                this.showSummarySettingsMessage('Please provide a valid summary interval (minimum 5 minutes).', true);
                return;
            }

            if (isNaN(maxRows) || maxRows < 50) {
                this.showSummarySettingsMessage('Please provide a valid max chats value (minimum 50).', true);
                return;
            }

            if (isNaN(maxUpstreamEntries) || maxUpstreamEntries < 5) {
                this.showSummarySettingsMessage('Please provide valid max evidence entries (minimum 5).', true);
                return;
            }

            if (isNaN(maxMessageChars) || maxMessageChars < 600) {
                this.showSummarySettingsMessage('Please provide valid max summary message chars (minimum 600).', true);
                return;
            }

            if (isNaN(maxRequestBytes) || maxRequestBytes < 1024) {
                this.showSummarySettingsMessage('Please provide valid max summary request bytes (minimum 1024).', true);
                return;
            }

            const data = new URLSearchParams();
            data.append('summaryIntervalSeconds', String(intervalMinutes * 60));
            data.append('summaryMaxRows', String(maxRows));
            data.append('summaryMaxUpstreamEntries', String(maxUpstreamEntries));
            data.append('summaryMaxMessageChars', String(maxMessageChars));
            data.append('summaryMaxRequestBytes', String(maxRequestBytes));
            data.append('summaryPrompt', prompt);

            try {
                const { payload, ok } = await Api.postUrlEncoded(`${this.contextPath}/admin/widgets/sync/timer`, data);
                if (ok && payload?.status === 'ok') {
                    this.applySummarySettings(payload);
                    this.showSummarySettingsMessage('Summary settings saved successfully.');
                    return;
                }
                this.showSummarySettingsMessage(payload?.message || 'Unable to save summary settings.', true);
            } catch (err) {
                this.showSummarySettingsMessage(`Unable to save summary settings: ${err.message}`, true);
            }
        },

        async runManualSummaryRetry() {
            const button = document.getElementById('adminSummaryRetryBtn');
            if (button) {
                button.disabled = true;
            }
            this.showSummarySettingsMessage('Generating summary now...');

            try {
                const { payload, ok, status } = await Api.fetchJson(`${this.contextPath}/admin/widgets/summary/retry`, {
                    method: 'POST',
                    headers: { 'Accept': 'application/json' }
                });

                if (ok && payload?.status === 'ok') {
                    this.showSummarySettingsMessage(payload?.message || 'Manual summary generation completed successfully.');
                } else {
                    const message = payload?.message || `Manual summary generation failed (HTTP ${status}).`;
                    this.showSummarySettingsMessage(message, true);
                }
            } catch (err) {
                this.showSummarySettingsMessage(`Manual summary generation request failed: ${err.message}`, true);
            } finally {
                if (button) {
                    button.disabled = false;
                }
                await this.loadSyncInterval();
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
                    return 'Generating summary now';
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
            const currentWidgetId = typeof payload.currentWidgetId === 'string' ? payload.currentWidgetId.trim() : '';
            const currentWidgetIndex = Number.isFinite(payload.currentWidgetIndex) ? Math.max(0, payload.currentWidgetIndex) : 0;
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
            if (currentWidgetId) {
                const totalForCurrent = Math.max(1, total || currentWidgetIndex);
                parts.push(`current ${Math.max(1, currentWidgetIndex)}/${totalForCurrent}: ${currentWidgetId}`);
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

        renderWidgetSyncProgress(payload, watchingExistingSync) {
            const wrapEl = document.getElementById('widgetSyncProgressWrap');
            const barEl = document.getElementById('widgetSyncProgressBar');
            const textEl = document.getElementById('widgetSyncProgressText');
            const currentEl = document.getElementById('widgetSyncCurrentWidget');
            if (!wrapEl || !barEl || !textEl || !currentEl || !payload || typeof payload !== 'object') {
                return;
            }

            const running = payload.syncRunning === true;
            const phase = typeof payload.syncPhase === 'string' ? payload.syncPhase.toLowerCase() : '';
            const percent = Number.isFinite(payload.progressPercent)
                ? Math.max(0, Math.min(100, payload.progressPercent))
                : 0;
            const total = Number.isFinite(payload.widgetsTotal) ? Math.max(0, payload.widgetsTotal) : 0;
            const completed = Number.isFinite(payload.widgetsCompleted) ? Math.max(0, payload.widgetsCompleted) : 0;
            const currentWidgetId = typeof payload.currentWidgetId === 'string' ? payload.currentWidgetId.trim() : '';
            const currentWidgetIndex = Number.isFinite(payload.currentWidgetIndex) ? Math.max(0, payload.currentWidgetIndex) : 0;

            const shouldShow = running || phase === 'completed' || phase === 'failed';
            if (!shouldShow) {
                wrapEl.style.display = 'none';
                barEl.style.width = '0%';
                textEl.textContent = '0%';
                currentEl.textContent = '';
                return;
            }

            wrapEl.style.display = '';
            barEl.style.width = `${percent}%`;

            let progressText = `${percent}%`;
            if (total > 0) {
                progressText += ` • ${Math.min(completed, total)}/${total} widgets`;
            }
            if (running && watchingExistingSync) {
                progressText += ' • watching server sync';
            }
            textEl.textContent = progressText;

            if (running && currentWidgetId) {
                const totalForCurrent = Math.max(1, total || currentWidgetIndex);
                currentEl.textContent = `Current widget ${Math.max(1, currentWidgetIndex)}/${totalForCurrent}: ${currentWidgetId}`;
                currentEl.style.color = '#1f2937';
            } else if (phase === 'failed') {
                currentEl.textContent = (payload.syncMessage || 'Widget sync failed.').trim();
                currentEl.style.color = '#b91c1c';
            } else if (phase === 'completed') {
                currentEl.textContent = 'Widget sync completed.';
                currentEl.style.color = '#047857';
            } else {
                currentEl.textContent = '';
                currentEl.style.color = '#1f2937';
            }
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
                this.renderWidgetSyncProgress({
                    syncRunning: true,
                    syncPhase: 'syncing_widgets',
                    progressPercent: 1,
                    widgetsTotal: 0,
                    widgetsCompleted: 0,
                    currentWidgetId: '',
                    currentWidgetIndex: 0,
                    syncMessage: 'Sync started.'
                }, watchingExistingSync);

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
                            this.renderWidgetSyncProgress(payload, watchingExistingSync);

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
                this.renderWidgetSyncProgress({
                    syncRunning: false,
                    syncPhase: 'failed',
                    progressPercent: 0,
                    widgetsTotal: 0,
                    widgetsCompleted: 0,
                    currentWidgetId: '',
                    currentWidgetIndex: 0,
                    syncMessage: timeoutMsg
                }, watchingExistingSync);
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
                this.renderWidgetSyncProgress({
                    syncRunning: false,
                    syncPhase: 'failed',
                    progressPercent: 0,
                    widgetsTotal: 0,
                    widgetsCompleted: 0,
                    currentWidgetId: '',
                    currentWidgetIndex: 0,
                    syncMessage: `Sync failed: ${err.message}`
                }, watchingExistingSync);
                return Promise.reject(err);
            }
        }
    };

    window.AdminPage.Sync = Sync;
})();
