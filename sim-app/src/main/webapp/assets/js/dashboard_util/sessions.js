(() => {
    'use strict';

    const core = window.DashboardCore;
    const metrics = window.DashboardMetrics;

    async function loadTopSessions(contextPath) {
        const totalEl = document.getElementById('totalSessions');
        const listEl = document.getElementById('topSessionList');

        const activeDaysEl = document.getElementById('activeDaysLabel');
        const activeCountLink = document.getElementById('activeSessionsLink');
        const inactiveCountLink = document.getElementById('inactiveSessionsLink');

        if (!listEl || !totalEl) {
            return;
        }

        try {
            const url = `${contextPath}/dashboard/sessions.json?page=1&pageSize=10&sortBy=count&sortDir=desc`;
            const resp = await fetch(url, {
                credentials: 'same-origin',
                headers: { Accept: 'application/json' }
            });

            if (!resp.ok) {
                totalEl.textContent = 'N/A';
                if (activeCountLink) {
                    activeCountLink.textContent = 'N/A';
                }
                if (inactiveCountLink) {
                    inactiveCountLink.textContent = 'N/A';
                }
                metrics.renderActiveUsersDelta({});
                return;
            }

            const data = await resp.json();
            if (!data || data.status !== 'ok') {
                totalEl.textContent = 'N/A';
                if (activeCountLink) {
                    activeCountLink.textContent = 'N/A';
                }
                if (inactiveCountLink) {
                    inactiveCountLink.textContent = 'N/A';
                }
                metrics.renderActiveUsersDelta({});
                return;
            }

            const activeDays = Number.isInteger(data.activeDays) && data.activeDays > 0 ? data.activeDays : 7;
            const activeUsers = typeof data.activeUsers === 'number' ? data.activeUsers : null;
            const inactiveUsers = typeof data.inactiveUsers === 'number' ? data.inactiveUsers : null;

            totalEl.textContent = typeof data.total === 'number' ? String(data.total) : '—';
            if (activeDaysEl) {
                activeDaysEl.textContent = String(activeDays);
            }

            if (activeCountLink) {
                activeCountLink.textContent = activeUsers === null ? '—' : String(activeUsers);
                activeCountLink.href = `${contextPath}/dashboard/sessions?activity=active&activeDays=${encodeURIComponent(String(activeDays))}`;
            }

            if (inactiveCountLink) {
                inactiveCountLink.textContent = inactiveUsers === null ? '—' : String(inactiveUsers);
                inactiveCountLink.href = `${contextPath}/dashboard/inactive-users`;
            }

            metrics.renderActiveUsersDelta(data);

            const sessions = Array.isArray(data.sessions) ? data.sessions : [];
            if (!sessions.length) {
                listEl.innerHTML = '<tr><td colspan="4" class="empty-row">No sessions found.</td></tr>';
                return;
            }

            let html = '';
            for (let idx = 0; idx < sessions.length; idx++) {
                const s = sessions[idx] || {};
                const rank = idx + 1;
                const sessionId = s.sessionId || '';
                const label = s.displayLabel || sessionId;
                const count = typeof s.count === 'number' ? s.count : 0;
                const last = s.last || '—';
                const reviewUrl = s.reviewUrl || `${contextPath}/dashboard/sessions/drilldown/session-review?sessionId=${encodeURIComponent(sessionId)}`;

                html += `<tr>
                    <td>${rank}</td>
                    <td>
                        <div>
                            ${sessionId
                        ? `<a class="customer-profile-link" href="${contextPath}/customer-profile?sessionId=${encodeURIComponent(sessionId)}">${core.esc(label)}</a>`
                        : core.esc(label)}
                        </div>
                        ${label !== sessionId && sessionId
                        ? `<div class="session-id-muted"><a class="customer-profile-link" href="${contextPath}/customer-profile?sessionId=${encodeURIComponent(sessionId)}">${core.esc(sessionId)}</a></div>`
                        : ''}
                    </td>
                    <td><a class="session-count-link" href="${core.esc(reviewUrl)}">${count} chats</a></td>
                    <td>${core.esc(last)}</td>
                </tr>`;
            }

            listEl.innerHTML = html;
        } catch (e) {
            console.warn('Unable to load top sessions:', e);
            totalEl.textContent = 'N/A';
            if (activeCountLink) {
                activeCountLink.textContent = 'N/A';
            }
            if (inactiveCountLink) {
                inactiveCountLink.textContent = 'N/A';
            }
            metrics.renderActiveUsersDelta({});
        }
    }

    window.DashboardSessions = {
        loadTopSessions
    };
})();
