// workspace.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};
    const Utils = window.AdminPage.Utils;
    const Api = window.AdminPage.Api;

    const Workspace = {
        init(config) {
            this.workspaceNameInput = document.getElementById('workspaceNameInput');
            this.saveWorkspaceBtn = document.getElementById('saveWorkspaceBtn');
            this.workspaceMessageEl = document.getElementById('workspaceMessage');

            if (this.workspaceNameInput && typeof config.workspaceName === 'string') {
                this.workspaceNameInput.value = config.workspaceName;
            }
            if (this.saveWorkspaceBtn) {
                this.saveWorkspaceBtn.addEventListener('click', () => {
                    const name = this.workspaceNameInput.value.trim();
                    this.saveWorkspaceName(name);
                });
            }
        },

        async saveWorkspaceName(name) {
            if (!this.saveWorkspaceBtn) return;
            const params = new URLSearchParams();
            params.append('workspaceName', name);

            this.saveWorkspaceBtn.disabled = true;
            try {
                const { payload, ok } = await Api.postUrlEncoded(`${window.adminPageConfig?.contextPath || ''}/admin/workspace`, params);
                if (ok && payload?.status === 'ok') {
                    this.showWorkspaceMessage('Workspace name saved.');
                    this.workspaceNameInput.value = payload.workspaceName || name;
                } else {
                    throw new Error(payload?.message || 'Unable to save workspace name.');
                }
            } catch (err) {
                this.showWorkspaceMessage(`Error: ${err.message}`, true);
            } finally {
                this.saveWorkspaceBtn.disabled = false;
            }
        },

        showWorkspaceMessage(text, isError = false) {
            if (!this.workspaceMessageEl) return;
            this.workspaceMessageEl.textContent = text;
            this.workspaceMessageEl.style.color = isError ? '#b91c1c' : '#047857';
        }
    };

    window.AdminPage.Workspace = Workspace;
})();
