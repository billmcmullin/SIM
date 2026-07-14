// terms.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};
    const Api = window.AdminPage.Api;
    const Utils = window.AdminPage.Utils;

    const Terms = {
        init(config) {
            this.contextPath = config.contextPath || '';
            this.termTableBody = document.getElementById('termTableBody');
            this.termMessageEl = document.getElementById('termMessage');
            this.termForm = document.getElementById('termCreateForm');
            this.termIdInput = document.getElementById('termId');
            this.termNameInput = document.getElementById('termName');
            this.termDescriptionInput = document.getElementById('termDescription');
            this.termPatternInput = document.getElementById('termPattern');
            this.termTypeSelect = document.getElementById('termType');
            this.saveTermBtn = document.getElementById('saveTermBtn');
            this.cancelTermEditBtn = document.getElementById('cancelTermEditBtn');
            this.exportTermsBtn = document.getElementById('exportTermsBtn');
            this.termsImportForm = document.getElementById('termsImportForm');
            this.termsCsvFileInput = document.getElementById('termsCsvFile');
            this.importTermsBtn = document.getElementById('importTermsBtn');

            this.isEditingTerm = false;

            window.startTermEditFromRow = this.startTermEditFromRow.bind(this);
            window.deleteTerm = this.deleteTerm.bind(this);

            this.termForm?.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitTermForm();
            });

            this.cancelTermEditBtn?.addEventListener('click', () => this.resetTermForm());
            this.exportTermsBtn?.addEventListener('click', () => this.exportTerms());

            if (this.importTermsBtn && this.termsCsvFileInput) {
                this.importTermsBtn.addEventListener('click', () => this.termsCsvFileInput.click());
                this.termsCsvFileInput.tabIndex = -1;
                this.termsCsvFileInput.addEventListener('change', () => {
                    if (!this.termsCsvFileInput.files || this.termsCsvFileInput.files.length === 0) return;
                    if (typeof this.termsImportForm.requestSubmit === 'function') {
                        this.termsImportForm.requestSubmit();
                    } else {
                        this.termsImportForm.submit();
                    }
                });

                this.termsImportForm?.addEventListener('submit', async (event) => {
                    event.preventDefault();
                    const f = this.termsCsvFileInput;
                    if (!f || !f.files || f.files.length === 0) {
                        alert('Please choose a CSV file to import.');
                        return;
                    }
                    if (!confirm('Import CSV will create or update terms. Continue?')) {
                        return;
                    }
                    const formData = new FormData(this.termsImportForm);
                    try {
                        const resp = await Api.postFormData(this.termsImportForm.action, formData);
                        if (resp && resp.url) {
                            this.processImportResultUrl(resp.url);
                        } else {
                            this.loadTermList();
                            this.showTermMessage('Import completed.');
                        }
                    } catch (err) {
                        console.error('CSV import failed via AJAX, falling back to normal form submit', err);
                        try {
                            this.termsImportForm.submit();
                        } catch (e) {
                            alert('Import failed and fallback submit also failed: ' + e.message);
                        }
                    }
                });
            }

            this.handleImportResultFromQuery();
            this.loadTermList(config.initialTerms || []);
        },

        normalizeTermIncoming(term) {
            if (!term || typeof term !== 'object') return { id: null, name: '', description: '', matchPattern: '', matchType: 'WILDCARD', isSystem: false };
            const id = term.id ?? term.ID ?? null;
            const name = term.name ?? term.termName ?? term.label ?? '';
            const description = term.description ?? term.desc ?? '';
            const matchPattern = term.matchPattern ?? term.match_pattern ?? term.matchpattern ?? '';
            const matchType = term.matchType ?? term.match_type ?? term.matchtype ?? (term.type ?? 'WILDCARD');
            const isSystem = Boolean(term.isSystem ?? term.systemFlag ?? term.isSystemFlag ?? term.system_flag ?? false);
            return { id, name, description, matchPattern, matchType, isSystem };
        },

        async loadTermList(fallbackTerms = []) {
            if (!this.termTableBody) return;
            this.renderTermList(fallbackTerms);
            try {
                const { payload, ok } = await Api.fetchJson(`${this.contextPath}/admin/terms`, { method: 'GET' });
                if (!ok || payload?.status !== 'ok') throw new Error(payload?.message || 'Unable to load terms.');
                this.renderTermList(payload.terms || []);
            } catch (err) {
                this.showTermMessage(`Unable to load terms: ${err.message}`, true);
            }
        },

        renderTermList(terms) {
            if (!this.termTableBody) return;
            if (!terms.length) {
                this.termTableBody.innerHTML = '<tr><td colspan="5" class="empty-row">No terms defined yet.</td></tr>';
                return;
            }
            this.termTableBody.innerHTML = terms.map(term => {
                const normalized = this.normalizeTermIncoming(term);
                const canModify = !normalized.isSystem;
                const sanitizedTerm = {
                    id: normalized.id,
                    name: normalized.name || '',
                    description: normalized.description || '',
                    matchPattern: normalized.matchPattern || '',
                    matchType: normalized.matchType || '',
                    isSystem: Boolean(normalized.isSystem)
                };
                const payload = Utils.escapeHtml(JSON.stringify(sanitizedTerm));
                return `<tr data-term="${payload}">
                    <td>${Utils.escapeHtml(sanitizedTerm.name)}</td>
                    <td>${Utils.escapeHtml(sanitizedTerm.description)}</td>
                    <td>${Utils.escapeHtml(sanitizedTerm.matchPattern || '')}</td>
                    <td>${Utils.escapeHtml(sanitizedTerm.matchType || '')}</td>
                    <td class="actions">
                        ${canModify ? `<button type="button" class="ghost-btn" onclick="startTermEditFromRow(this)">Edit</button>
                        <button type="button" class="ghost-btn" onclick="deleteTerm(${sanitizedTerm.id})">Delete</button>` : '<span class="small-note">System</span>'}
                    </td>
                </tr>`;
            }).join('');
        },

        async submitTermForm() {
            const name = this.termNameInput.value.trim();
            const description = this.termDescriptionInput.value.trim();
            const matchPattern = this.termPatternInput.value.trim();
            const matchType = this.termTypeSelect?.value || 'WILDCARD';

            if (!name || !description) {
                this.showTermMessage('Name and description are required.', true);
                return;
            }

            const payload = { name, description, matchPattern, matchType };
            const url = `${this.contextPath}/admin/terms`;
            let method = 'POST';
            if (this.isEditingTerm) {
                payload.id = Number(this.termIdInput.value);
                method = 'PUT';
            }

            try {
                const result = (method === 'POST') ? await Api.postJson(url, payload) : await Api.putJson(url, payload);
                if (!result.ok) throw new Error(result.payload?.message || 'Unable to save term.');
                this.showTermMessage(`Term "${result.payload?.term?.name || payload.name}" saved.`);
                this.resetTermForm();
                this.loadTermList();
            } catch (err) {
                this.showTermMessage(`Error: ${err.message}`, true);
            }
        },

        startTermEdit(id, name, description, pattern, type) {
            this.isEditingTerm = true;
            this.termIdInput.value = id;
            this.termNameInput.value = name;
            this.termDescriptionInput.value = description;
            this.termPatternInput.value = pattern || '';
            this.termTypeSelect.value = type || 'WILDCARD';
            if (this.saveTermBtn) this.saveTermBtn.textContent = 'Update Term';
            if (this.cancelTermEditBtn) this.cancelTermEditBtn.style.display = 'inline-block';
        },

        startTermEditFromRow(button) {
            if (!button) return;
            const row = button.closest('tr');
            if (!row) return;
            const payload = row.dataset.term;
            if (!payload) {
                this.showTermMessage('Unable to load term data.', true);
                return;
            }
            try {
                const term = JSON.parse(payload);
                this.startTermEdit(term.id, term.name, term.description, term.matchPattern, term.matchType);
            } catch {
                this.showTermMessage('Unable to parse term data for editing.', true);
            }
        },

        resetTermForm() {
            this.isEditingTerm = false;
            this.termIdInput.value = '';
            this.termForm?.reset();
            if (this.saveTermBtn) this.saveTermBtn.textContent = 'Save Term';
            if (this.cancelTermEditBtn) this.cancelTermEditBtn.style.display = 'none';
        },

        async deleteTerm(id) {
            if (!confirm('Delete this term?')) return;
            try {
                const { ok, payload } = await Api.delete(`${this.contextPath}/admin/terms?id=${encodeURIComponent(id)}`);
                if (!ok) throw new Error(payload?.message || 'Unable to delete term.');
                this.showTermMessage('Term deleted.');
                this.loadTermList();
            } catch (err) {
                this.showTermMessage(`Error: ${err.message}`, true);
            }
        },

        showTermMessage(message, isError = false) {
            if (!this.termMessageEl) return;
            this.termMessageEl.textContent = message;
            this.termMessageEl.style.color = isError ? '#b91c1c' : '#047857';
        },

        async exportTerms() {
            const exportUrl = `${this.contextPath}/admin/terms/export`;
            try {
                const resp = await fetch(exportUrl, {
                    method: 'GET',
                    credentials: 'same-origin',
                    headers: { 'Accept': 'text/csv,application/octet-stream;q=0.9,*/*;q=0.8' }
                });

                if (!resp.ok) {
                    window.location.href = exportUrl;
                    return;
                }

                const blob = await resp.blob();
                let suggestedName = 'terms.csv';
                const cd = resp.headers.get('content-disposition') || '';
                const parsed = Utils.parseContentDispositionFilename(cd);
                if (parsed) suggestedName = parsed;

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
                        this.showTermMessage(`Export saved as "${handle.name}".`);
                        return;
                    } catch (err) {
                        console.warn('Save file picker failed or cancelled:', err);
                    }
                }

                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = suggestedName;
                document.body.appendChild(a);
                a.click();
                a.remove();
                URL.revokeObjectURL(url);
                this.showTermMessage(`CSV download started (${suggestedName}).`);
            } catch (err) {
                console.error('Export failed, falling back to direct download:', err);
                window.location.href = `${this.contextPath}/admin/terms/export`;
            }
        },

        processImportResultUrl(urlString) {
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
                    this.showTermMessage(parts.join(' — '), errors !== null && errors !== undefined);
                } else {
                    this.showTermMessage('Import completed.');
                }
                this.loadTermList();
            } catch (e) {
                console.warn('Unable to parse import redirect URL:', e);
                this.showTermMessage('Import completed.');
                this.loadTermList();
            }
        },

        handleImportResultFromQuery() {
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
                    this.showTermMessage(parts.join(' — '), errors !== null && errors !== undefined);
                    const cleanUrl = window.location.pathname + window.location.hash;
                    history.replaceState(null, '', cleanUrl);
                    this.loadTermList();
                }
            } catch {
                // ignore parsing issues
            }
        }
    };

    window.AdminPage.Terms = Terms;
})();
