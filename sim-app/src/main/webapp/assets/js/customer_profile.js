// assets/js/customer_profile.js
(function () {
    'use strict';

    window.CustomerProfilePage = window.CustomerProfilePage || {};

    const config = window.customerProfileConfig || {};
    const contextPath = config.contextPath || '';
    const sessionId = config.sessionId || '';
    const friendlyName = config.friendlyName || '';
    const syncEndpoint = `${contextPath}/admin/sync-customer-profile`;

    document.addEventListener('DOMContentLoaded', () => {
        bindProfileLinks();
        bindSyncButton();
        hydrateHeader();
    });

    function hydrateHeader() {
        const sessionEl = document.getElementById('cpSessionId');
        const friendlyEl = document.getElementById('cpFriendlyName');

        if (sessionEl && sessionId) {
            sessionEl.textContent = sessionId;
        }
        if (friendlyEl && friendlyName) {
            friendlyEl.textContent = friendlyName;
        }

        // Normalize server-rendered timestamps to browser local time.
        setText('cpLastSyncedAt', document.getElementById('cpLastSyncedAt')?.textContent, true);
        normalizeLinkedSessionUpdatedAt();
    }

    function bindSyncButton() {
        const btn = document.getElementById('syncCustomerProfileBtn');
        if (!btn) {
            return;
        }

        btn.addEventListener('click', async () => {
            const sid = getSessionIdFromPage();
            if (!sid) {
                setStatus('Missing session id for sync.', false);
                return;
            }

            const original = btn.textContent;
            btn.disabled = true;
            btn.textContent = 'Syncing...';

            try {
                const payload = await postUrlEncoded(syncEndpoint, new URLSearchParams({ sessionId: sid }));

                if (payload.ok && payload.data?.status === 'ok') {
                    setStatus(payload.data.message || 'Customer profile synced successfully.', true);

                    // Update visible fields if returned by server
                    if (payload.data.profile) {
                        patchProfileFields(payload.data.profile);
                    }

                    // Optional full refresh if server indicates
                    if (payload.data.refresh === true) {
                        window.location.reload();
                    }
                } else {
                    setStatus(payload.data?.message || 'Sync failed.', false);
                }
            } catch (e) {
                setStatus(`Sync error: ${e.message}`, false);
            } finally {
                btn.disabled = false;
                btn.textContent = original || 'Sync from Salesforce';
            }
        });
    }

    function patchProfileFields(profile) {
        setText('cpFriendlyName', profile.friendlyName);
        setText('cpEmail', profile.email);
        setText('cpPhone', profile.phone);
        setText('cpTitle', profile.title);
        setText('cpDepartment', profile.department);
        setText('cpSalesforceContactId', profile.salesforceContactId);
        setText('cpSalesforceAccountId', profile.salesforceAccountId);
        setText('cpLastSyncedAt', profile.lastSyncedAt, true);
        normalizeLinkedSessionUpdatedAt();
    }

    function setText(id, value, isDateTime = false) {
        const el = document.getElementById(id);
        if (!el) {
            return;
        }
        if (value === null || value === undefined || value === '') {
            el.textContent = '—';
            return;
        }
        if (isDateTime) {
            el.textContent = formatDateTime(value, '—');
            return;
        }
        el.textContent = String(value);
    }

    function formatDateTime(value, fallback = '—') {
        if (value === null || value === undefined || value === '') {
            return fallback;
        }

        const raw = String(value).trim();
        if (!raw || raw === '—' || raw.toLowerCase() === 'never') {
            return raw || fallback;
        }

        const d = new Date(raw);
        if (Number.isNaN(d.getTime())) {
            return raw;
        }

        const formatted = d.toLocaleString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: 'numeric',
            minute: '2-digit',
            hour12: true
        });
        return formatted.endsWith('.') ? formatted : `${formatted}.`;
    }

    function normalizeLinkedSessionUpdatedAt() {
        const table = document.querySelector('.customer-profile-linked-table');
        if (!table) {
            return;
        }
        const rows = table.querySelectorAll('tbody tr td:nth-child(4)');
        rows.forEach((cell) => {
            if (!cell) {
                return;
            }
            cell.textContent = formatDateTime(cell.textContent, '—');
        });
    }

    function setStatus(message, success) {
        const el = document.getElementById('customerProfileSyncStatus');
        if (!el) {
            return;
        }
        el.textContent = message;
        el.style.color = success ? '#047857' : '#b91c1c';
    }

    function getSessionIdFromPage() {
        if (sessionId) {
            return sessionId;
        }
        const hidden = document.getElementById('cpSessionIdInput');
        if (hidden && hidden.value) {
            return hidden.value.trim();
        }
        const text = document.getElementById('cpSessionId')?.textContent || '';
        return text.trim() || '';
    }

    async function postUrlEncoded(url, params) {
        const res = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json'
            },
            credentials: 'same-origin',
            body: params.toString()
        });

        let data = null;
        try {
            data = await res.json();
        } catch {
            data = { status: 'error', message: `Unexpected response (${res.status})` };
        }

        return { ok: res.ok, data };
    }

    /**
     * Turns Session ID / Friendly Name elements into links to customer profile.
     *
     * Usage:
     * - add class "js-customer-profile-link"
     * - set one of:
     *   data-session-id="..."
     *   data-friendly-name="..."
     *
     * Optional:
     * - data-context-path="/chat-server"
     *
     * Example:
     * <span class="js-customer-profile-link" data-session-id="abc123">abc123</span>
     */
    function bindProfileLinks() {
        const nodes = document.querySelectorAll('.js-customer-profile-link');
        if (!nodes.length) {
            return;
        }

        nodes.forEach((node) => {
            // skip if already anchor
            if (node.tagName === 'A') {
                return;
            }

            const sid = (node.getAttribute('data-session-id') || '').trim();
            const fname = (node.getAttribute('data-friendly-name') || '').trim();
            const localContextPath = node.getAttribute('data-context-path') || contextPath || '';
            if (!sid && !fname) {
                return;
            }

            const a = document.createElement('a');
            const qs = new URLSearchParams();
            if (sid) {
                qs.set('sessionId', sid);
            }
            if (!sid && fname) {
                qs.set('friendlyName', fname); // fallback route support
            }

            a.href = `${localContextPath}/customer-profile?${qs.toString()}`;
            a.textContent = node.textContent && node.textContent.trim() ? node.textContent.trim() : (sid || fname);
            a.className = 'customer-profile-link';

            // preserve title/tooltip if present
            if (node.title) {
                a.title = node.title;
            }

            node.replaceWith(a);
        });
    }
})();
