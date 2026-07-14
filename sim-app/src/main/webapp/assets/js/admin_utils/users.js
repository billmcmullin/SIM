// users.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};
    const Api = window.AdminPage.Api;
    const Utils = window.AdminPage.Utils;

    const Users = {
        init(contextPath) {
            this.contextPath = contextPath || '';
            this.userTableBody = document.getElementById('userTableBody');
            window.deleteUser = this.deleteUser.bind(this);
            const userForm = document.getElementById('userCreateForm');
            if (userForm) {
                userForm.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.createUser();
                });
            }
            this.loadUserList();
        },

        async createUser() {
            const username = document.getElementById('newUsername').value.trim();
            const password = document.getElementById('newPassword').value.trim();
            const role = document.getElementById('roleSelect').value;

            if (!username || !password) {
                this.showUserResult('Username and password are required.', true);
                return;
            }

            try {
                const { ok, payload } = await Api.postJson(`${this.contextPath}/admin/users`, { username, password, role });
                if (ok) {
                    const createdRole = (payload.role || 'user').toLowerCase();
                    const createdUsername = payload.username || username;
                    this.showUserResult(`Created ${createdRole} ${createdUsername}.`);
                    document.getElementById('userCreateForm').reset();
                    this.loadUserList();
                } else {
                    throw new Error(payload?.error || 'Unable to create user.');
                }
            } catch (err) {
                this.showUserResult(`Error: ${err.message}`, true);
            }
        },

        showUserResult(message, isError = false) {
            const resultEl = document.getElementById('userResult');
            if (resultEl) {
                resultEl.textContent = message;
                resultEl.style.color = isError ? '#b91c1c' : '#047857';
            }
        },

        async loadUserList() {
            try {
                const { payload, ok } = await Api.fetchJson(`${this.contextPath}/admin/users`, { method: 'GET' });
                if (!ok || payload?.status !== 'ok') {
                    this.showUserResult(payload?.message || 'Unable to load users.', true);
                    return;
                }
                this.renderUserTable(payload.users || []);
            } catch (err) {
                this.showUserResult(`Unable to load users: ${err.message}`, true);
            }
        },

        renderUserTable(users) {
            if (!this.userTableBody) return;
            if (!users.length) {
                this.userTableBody.innerHTML = '<tr><td colspan="3" class="empty-row">No users found.</td></tr>';
                return;
            }
            this.userTableBody.innerHTML = users.map(user => `<tr>
                <td>${Utils.escapeHtml(user.username)}</td>
                <td>${Utils.escapeHtml(user.role)}</td>
                <td><button type="button" class="ghost-btn" onclick="deleteUser('${user.id}')">Delete</button></td>
            </tr>`).join('');
        },

        async deleteUser(id) {
            if (!confirm('Delete this user?')) return;
            try {
                const { ok, payload } = await Api.delete(`${this.contextPath}/admin/users?userId=${encodeURIComponent(id)}`);
                if (ok) {
                    this.showUserResult('User deleted.');
                    this.loadUserList();
                } else {
                    throw new Error(payload?.error || 'Unable to delete user.');
                }
            } catch (err) {
                this.showUserResult(`Error: ${err.message}`, true);
            }
        }
    };

    window.AdminPage.Users = Users;
})();
