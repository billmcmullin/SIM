// File: src/main/webapp/assets/js/admin_utils/db_import.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    const DbImport = {
        init({ contextPath }) {
            this.contextPath = contextPath || '';
            this.precheckPassed = false;

            this.fileInput = document.getElementById('dbImportFile');
            this.precheckBtn = document.getElementById('precheckImportBtn');
            this.runBtn = document.getElementById('runImportBtn');
            this.msg = document.getElementById('dbImportMessage');
            this.summaryWrap = document.getElementById('dbImportSummary');
            this.summaryBody = document.getElementById('dbImportSummaryBody');
            this.widgetPreviewBody = document.getElementById('widgetImportPreviewBody');

            this.precheckBtn?.addEventListener('click', () => this.precheckImport());
            this.runBtn?.addEventListener('click', () => this.runImport());
            this.fileInput?.addEventListener('change', () => this.onFileChanged());

            if (this.runBtn) {
                this.runBtn.disabled = true;
            }
        },

        onFileChanged() {
            this.precheckPassed = false;
            if (this.runBtn) {
                this.runBtn.disabled = true;
            }
            this.setMessage('File selected. Run precheck before importing.', '#374151');
        },

        getSelectedFile() {
            return this.fileInput?.files?.[0] || null;
        },

        setMessage(text, color = '#374151') {
            if (!this.msg) {
                return;
            }
            this.msg.textContent = text || '';
            this.msg.style.color = color;
        },

        async precheckImport() {
            const file = this.getSelectedFile();
            this.precheckPassed = false;
            if (this.runBtn) {
                this.runBtn.disabled = true;
            }

            if (!file) {
                this.setMessage('Please choose a backup ZIP file first.', '#b91c1c');
                return;
            }

            this.setMessage('Running precheck...', '#374151');

            const fd = new FormData();
            fd.append('action', 'precheck');
            fd.append('file', file);

            try {
                const result = await postFormDataJson(`${this.contextPath}/admin/db/import`, fd);
                const payload = result.payload;

                if (!result.ok || payload?.status !== 'ok' || payload?.readyForImport !== true) {
                    this.setMessage(payload?.message || `Precheck failed (${result.status}).`, '#b91c1c');
                    this.renderSummary(null);
                    this.renderWidgetPreview([]);
                    return;
                }

                this.precheckPassed = true;
                if (this.runBtn) {
                    this.runBtn.disabled = false;
                }

                this.setMessage(payload?.message || 'Precheck successful. Ready to import.', '#047857');
                this.renderSummary(payload);
                this.renderWidgetPreview(Array.isArray(payload.widgetTables) ? payload.widgetTables : []);
            } catch (err) {
                this.setMessage(`Precheck error: ${err.message}`, '#b91c1c');
            }
        },

        async runImport() {
            if (!this.precheckPassed) {
                this.setMessage('Please run a successful precheck before import.', '#b91c1c');
                return;
            }

            const file = this.getSelectedFile();
            if (!file) {
                this.setMessage('Please choose a backup ZIP file first.', '#b91c1c');
                return;
            }

            if (!confirmAction('This will replace current table data with backup data. Continue?')) {
                return;
            }

            this.setMessage('Import in progress...', '#374151');

            const fd = new FormData();
            fd.append('action', 'run');
            fd.append('file', file);

            try {
                const result = await postFormDataJson(`${this.contextPath}/admin/db/import`, fd);
                const payload = result.payload;

                if (!result.ok || payload?.status !== 'ok') {
                    this.setMessage(payload?.message || `Import failed (${result.status}).`, '#b91c1c');
                    return;
                }

                const imported = payload.importedTables || {};
                const parts = Object.keys(imported).map((t) => `${t}: ${imported[t]}`);
                this.setMessage(
                    `Import completed.${parts.length ? ` Rows imported -> ${parts.join(', ')}` : ''}`,
                    '#047857'
                );
            } catch (err) {
                this.setMessage(`Import error: ${err.message}`, '#b91c1c');
            }
        },

        renderSummary(payload) {
            if (!this.summaryWrap || !this.summaryBody) {
                return;
            }

            if (!payload) {
                this.summaryWrap.style.display = 'none';
                this.summaryBody.innerHTML = '';
                return;
            }

            this.summaryWrap.style.display = 'block';
            const createdBaseline = Array.isArray(payload.createdBaselineTables) ? payload.createdBaselineTables : [];
            const createdWidget = Array.isArray(payload.createdWidgetTables) ? payload.createdWidgetTables : [];

            this.summaryBody.innerHTML = `
                <p><strong>Connection:</strong> OK</p>
                <p><strong>ZIP tables found:</strong> ${Number(payload.zipTableCount || 0)}</p>
                <p><strong>Created baseline tables:</strong> ${createdBaseline.length ? createdBaseline.map(escapeHtml).join(', ') : 'None'}</p>
                <p><strong>Created widget tables:</strong> ${createdWidget.length ? createdWidget.map(escapeHtml).join(', ') : 'None'}</p>
            `;
        },

        renderWidgetPreview(widgetTables) {
            if (!this.widgetPreviewBody) {
                return;
            }

            if (!widgetTables.length) {
                this.widgetPreviewBody.innerHTML = '<tr><td colspan="2" class="empty-row">No widget tables detected in backup ZIP.</td></tr>';
                return;
            }

            this.widgetPreviewBody.innerHTML = widgetTables.map((w) => `
                <tr>
                    <td>${escapeHtml(w.table || '')}</td>
                    <td>${Number(w.rowsToImport || 0)}</td>
                </tr>
            `).join('');
        }
    };

    function escapeHtml(v) {
        if (window.AdminPage?.Utils?.escapeHtml) {
            return window.AdminPage.Utils.escapeHtml(v);
        }
        return String(v ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function confirmAction(message) {
        const confirmFn = window.confirm?.bind(window);
        return typeof confirmFn === 'function' ? confirmFn(message) : false;
    }

    async function postFormDataJson(url, formData) {
        if (window.AdminPage?.Api?.postFormDataJson) {
            return window.AdminPage.Api.postFormDataJson(url, formData);
        }
        const resp = await fetch(url, {
            method: 'POST',
            credentials: 'same-origin',
            body: formData,
            redirect: 'follow'
        });
        const payload = await resp.json().catch(() => null);
        return { status: resp.status, ok: resp.ok, payload, response: resp };
    }

    window.AdminPage.DbImport = DbImport;
})();
