(function () {
    'use strict';

    window.AdminPage = window.AdminPage || {};

    const AutoEmailAlerts = {
        init(config) {
            this.contextPath = config?.contextPath || '';
            this.initialTerms = Array.isArray(config?.initialTermList) ? config.initialTermList : [];

            this.loadBtn = document.getElementById('loadAutoEmailAlertsBtn');
            this.saveBtn = document.getElementById('saveAutoEmailAlertsBtn');
            this.sendHealthTestBtn = document.getElementById('sendAutoEmailHealthTestBtn');
            this.resultEl = document.getElementById('autoEmailAlertsResult');
            this.termSuggestionsEl = document.getElementById('aeTermNameSuggestions');

            if (!this.loadBtn || !this.saveBtn || !this.sendHealthTestBtn) {
                return;
            }

            this.bindEvents();
            this.renderTermSuggestions();
            this.loadConfig();
        },

        bindEvents() {
            this.loadBtn.addEventListener('click', () => this.loadConfig());
            this.saveBtn.addEventListener('click', () => this.saveConfig());
            this.sendHealthTestBtn.addEventListener('click', () => this.sendHealthTestEmail());
        },

        renderTermSuggestions() {
            if (!this.termSuggestionsEl) {
                return;
            }
            const names = new Set();
            for (const term of this.initialTerms) {
                const name = (term?.name || '').trim();
                if (name) {
                    names.add(name);
                }
            }
            this.termSuggestionsEl.innerHTML = Array.from(names)
                .sort((a, b) => a.localeCompare(b))
                .map((name) => `<option value="${escapeHtml(name)}"></option>`)
                .join('');
        },

        async loadConfig() {
            this.setMessage('Loading automatic email alerts...', false);
            try {
                const result = await window.AdminPage.Api.fetchJson(`${this.contextPath}/admin/email/alerts`, {
                    method: 'GET',
                    headers: { 'Accept': 'application/json' }
                });

                if (!result.ok || result.payload?.status !== 'ok') {
                    throw new Error(result.payload?.message || `Request failed (${result.status}).`);
                }

                this.fillForm(result.payload);
                this.setMessage('Automatic email alerts loaded.', false);
            } catch (err) {
                this.setMessage(`Failed to load automatic email alerts: ${err.message}`, true);
            }
        },

        async saveConfig() {
            const payload = this.readForm();
            this.setMessage('Saving automatic email alerts...', false);
            try {
                const result = await window.AdminPage.Api.postJson(`${this.contextPath}/admin/email/alerts`, payload);
                if (!result.ok || result.payload?.status !== 'ok') {
                    throw new Error(result.payload?.message || `Request failed (${result.status}).`);
                }

                this.fillForm(result.payload);
                this.setMessage('Automatic email alerts saved.', false);
            } catch (err) {
                this.setMessage(`Failed to save automatic email alerts: ${err.message}`, true);
            }
        },

        async sendHealthTestEmail() {
            const payload = {
                ...this.readForm(),
                sendTestEmail: true
            };

            this.setMessage('Sending health test email...', false);
            try {
                const result = await window.AdminPage.Api.postJson(`${this.contextPath}/admin/email/alerts`, payload);
                const msg = result.payload?.message || `Request failed (${result.status}).`;
                if (!result.ok || result.payload?.status !== 'ok') {
                    throw new Error(msg);
                }
                this.setMessage(msg, false);
            } catch (err) {
                this.setMessage(`Failed to send health test email: ${err.message}`, true);
            }
        },

        readForm() {
            return {
                healthEnabled: checked('aeHealthEnabled'),
                healthCheckIntervalMinutes: intVal('aeHealthCheckIntervalMinutes', 5),
                healthOfflineDelayMinutes: intVal('aeHealthOfflineDelayMinutes', 5),
                healthResendIntervalMinutes: intVal('aeHealthResendIntervalMinutes', 30),
                healthRecipients: value('aeHealthRecipients'),
                healthSubject: value('aeHealthSubject'),
                healthMessage: value('aeHealthMessage'),
                healthRunbookUrl: value('aeHealthRunbookUrl'),
                healthRunbookAttachmentPath: value('aeHealthRunbookAttachmentPath'),

                termEnabled: checked('aeTermEnabled'),
                termCheckIntervalMinutes: intVal('aeTermCheckIntervalMinutes', 10),
                termName: value('aeTermName'),
                termRecipients: value('aeTermRecipients'),
                termSubject: value('aeTermSubject'),
                termMessage: value('aeTermMessage')
            };
        },

        fillForm(data) {
            setChecked('aeHealthEnabled', !!data.healthEnabled);
            setValue('aeHealthCheckIntervalMinutes', safeNumber(data.healthCheckIntervalMinutes, 5));
            setValue('aeHealthOfflineDelayMinutes', safeNumber(data.healthOfflineDelayMinutes, 5));
            setValue('aeHealthResendIntervalMinutes', safeNumber(data.healthResendIntervalMinutes, 30));
            setValue('aeHealthRecipients', data.healthRecipients || '');
            setValue('aeHealthSubject', data.healthSubject || '');
            setValue('aeHealthMessage', data.healthMessage || '');
            setValue('aeHealthRunbookUrl', data.healthRunbookUrl || '');
            setValue('aeHealthRunbookAttachmentPath', data.healthRunbookAttachmentPath || '');

            setChecked('aeTermEnabled', !!data.termEnabled);
            setValue('aeTermCheckIntervalMinutes', safeNumber(data.termCheckIntervalMinutes, 10));
            setValue('aeTermName', data.termName || '');
            setValue('aeTermRecipients', data.termRecipients || '');
            setValue('aeTermSubject', data.termSubject || '');
            setValue('aeTermMessage', data.termMessage || '');

            setText('aeHealthLastStatus', data.healthLastStatus || 'UNKNOWN');
            setText('aeHealthLastCheckedAt', formatDateLabel(data.healthLastCheckedAt, 'Never'));
            setText('aeHealthOfflineSince', formatDateLabel(data.healthOfflineSince, 'N/A'));
            setText('aeHealthLastAlertAt', formatDateLabel(data.healthLastAlertAt, 'N/A'));

            setText('aeTermLastCheckedAt', formatDateLabel(data.termLastCheckedAt, 'Never'));
            setText('aeTermLastCount', String(safeNumber(data.termLastCount, 0)));
            setText('aeTermLastAlertAt', formatDateLabel(data.termLastAlertAt, 'N/A'));
        },

        setMessage(text, isError) {
            if (!this.resultEl) {
                return;
            }
            this.resultEl.textContent = text || '';
            this.resultEl.style.color = isError ? '#b91c1c' : '#047857';
        }
    };

    function value(id) {
        const el = document.getElementById(id);
        return el ? String(el.value || '').trim() : '';
    }

    function setValue(id, val) {
        const el = document.getElementById(id);
        if (el) {
            el.value = val == null ? '' : String(val);
        }
    }

    function checked(id) {
        const el = document.getElementById(id);
        return !!(el && el.checked);
    }

    function setChecked(id, val) {
        const el = document.getElementById(id);
        if (el) {
            el.checked = !!val;
        }
    }

    function setText(id, text) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = text == null ? '' : String(text);
        }
    }

    function intVal(id, fallback) {
        const raw = value(id);
        if (!raw) {
            return fallback;
        }
        const n = Number.parseInt(raw, 10);
        if (!Number.isFinite(n)) {
            return fallback;
        }
        return Math.max(0, n);
    }

    function safeNumber(value, fallback) {
        const n = Number(value);
        if (!Number.isFinite(n)) {
            return fallback;
        }
        return n;
    }

    function formatDateLabel(value, fallback) {
        if (!value) {
            return fallback;
        }
        const d = new Date(value);
        if (Number.isNaN(d.getTime())) {
            return fallback;
        }
        return d.toLocaleString();
    }

    function escapeHtml(input) {
        return String(input || '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    window.AdminPage.AutoEmailAlerts = AutoEmailAlerts;
})();
