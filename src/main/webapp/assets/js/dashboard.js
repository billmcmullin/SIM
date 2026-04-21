(() => {
    'use strict';

    function parseSlices(input) {
        if (Array.isArray(input)) return input;
        if (typeof input !== 'string') return [];
        try {
            const parsed = JSON.parse(input);
            return Array.isArray(parsed) ? parsed : [];
        } catch {
            return [];
        }
    }

    function buildSeries(slices, palette) {
        const labels = new Array(slices.length);
        const values = new Array(slices.length);
        const colors = new Array(slices.length);

        for (let i = 0; i < slices.length; i++) {
            const slice = slices[i] || {};
            labels[i] = slice.label ?? '';
            values[i] = typeof slice.count === 'number' ? slice.count : 0;
            colors[i] = palette[i % palette.length];
        }

        return { labels, values, colors };
    }

    function esc(v) {
        if (v === null || typeof v === 'undefined') return '';
        const str = String(v);
        if (!/[&<>"']/.test(str)) return str;
        return str
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function computeDelta(today, yesterday) {
        const t = Number.isFinite(today) ? today : 0;
        const y = Number.isFinite(yesterday) ? yesterday : 0;
        const delta = t - y;
        const pct = y === 0 ? (t > 0 ? 100 : 0) : (delta * 100) / y;
        const direction = delta > 0 ? 'up' : delta < 0 ? 'down' : 'flat';
        return { today: t, yesterday: y, delta, pct, direction };
    }

    function formatSignedInt(n) {
        if (!Number.isFinite(n)) return '0';
        return `${n > 0 ? '+' : ''}${Math.trunc(n)}`;
    }

    function formatPct(n) {
        if (!Number.isFinite(n)) return '0.0';
        return n.toFixed(1);
    }

    function renderProgressPill(deltaObj, forcedDirection) {
        const direction = (forcedDirection || deltaObj.direction || 'flat').toLowerCase();
        const cls = direction === 'up'
            ? 'progression-up'
            : direction === 'down'
                ? 'progression-down'
                : 'progression-flat';

        const text = `${formatSignedInt(deltaObj.delta)} (${formatPct(deltaObj.pct)}%) vs yesterday`;
        return `<span class="progression ${cls}" data-direction="${esc(direction)}">${esc(text)}</span>`;
    }

    function parseIntFromText(text) {
        if (!text) return null;
        const m = String(text).match(/-?\d+/);
        if (!m) return null;
        const n = parseInt(m[0], 10);
        return Number.isFinite(n) ? n : null;
    }

    function parseChatSummaryValues() {
        const summary = document.querySelector('.chat-progression-summary');
        if (!summary) return { today: null, yesterday: null };

        const txt = summary.textContent || '';
        const todayMatch = txt.match(/Today:\s*([0-9]+)/i);
        const yMatch = txt.match(/Yesterday:\s*([0-9]+)/i);

        return {
            today: todayMatch ? parseInt(todayMatch[1], 10) : null,
            yesterday: yMatch ? parseInt(yMatch[1], 10) : null
        };
    }

    function parseNewUsersFromServerRenderedDom() {
        const t1 = document.getElementById('serverNewUsersToday');
        const y1 = document.getElementById('serverNewUsersYesterday');

        return {
            today: parseIntFromText(t1?.textContent),
            yesterday: parseIntFromText(y1?.textContent)
        };
    }

    function applyProgressionDirectionStyling() {
        const direction = (window.chatProgressionDirection || '').toLowerCase().trim();
        if (!direction) return;

        const el = document.querySelector('.chat-progression-summary .progression');
        if (!el) return;

        el.classList.remove('progression-up', 'progression-down', 'progression-flat');
        if (direction === 'up') el.classList.add('progression-up');
        else if (direction === 'down') el.classList.add('progression-down');
        else el.classList.add('progression-flat');

        el.setAttribute('data-direction', direction);
    }

    function toYmd(d) {
        if (!(d instanceof Date) || Number.isNaN(d.getTime())) return '';
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
    }

    function getTodayYesterday() {
        if (window.dashboardDates?.today && window.dashboardDates?.yesterday) {
            return { today: window.dashboardDates.today, yesterday: window.dashboardDates.yesterday };
        }
        const today = new Date();
        const yesterday = new Date();
        yesterday.setDate(today.getDate() - 1);
        return { today: toYmd(today), yesterday: toYmd(yesterday) };
    }

    async function buildWidgetReviewSelectionLink(dayToken, contextPath) {
        return `${contextPath}/dashboard/sessions/drilldown/date-review-relative?day=${encodeURIComponent(dayToken)}`;
    }

    function setConditionalMetricLink(el, value, hrefOrBuilder) {
        if (!el) return;
        const n = Number(value);
        if (!Number.isFinite(n)) {
            el.textContent = 'N/A';
            return;
        }

        if (n > 0 && hrefOrBuilder) {
            const href = typeof hrefOrBuilder === 'string' ? hrefOrBuilder : '#';
            el.innerHTML = `<a class="metric-link metric-dynamic-link" href="${esc(href)}">${esc(String(n))}</a>`;
            if (typeof hrefOrBuilder === 'function') {
                const a = el.querySelector('a.metric-dynamic-link');
                if (a) a.__buildHref = hrefOrBuilder;
            }
        } else {
            el.textContent = String(n);
        }
    }

    function wireDynamicLinks() {
        document.addEventListener('click', async (event) => {
            const a = event.target.closest('a.metric-dynamic-link');
            if (!a || !a.__buildHref) return;

            event.preventDefault();
            if (a.dataset.loading === '1') return;

            try {
                a.dataset.loading = '1';
                const href = await a.__buildHref();
                if (href) window.location.href = href;
            } catch (e) {
                console.warn('Unable to open review selection:', e);
                alert('Unable to open chat review for this metric right now.');
            } finally {
                a.dataset.loading = '0';
            }
        });
    }

    function applyDeltaClasses(scope = document) {
        const nodes = scope.querySelectorAll('.progression');
        nodes.forEach(node => {
            const txt = (node.textContent || '').trim();
            node.classList.remove('progression-up', 'progression-down', 'progression-flat');

            if (txt.startsWith('+')) node.classList.add('progression-up');
            else if (txt.startsWith('-')) node.classList.add('progression-down');
            else node.classList.add('progression-flat');
        });
    }

    function hydrateDailyProgressSection(contextPath) {
        const section = document.getElementById('dailyProgressSection');
        if (!section) return;

        const todayChatsEl = document.getElementById('dpTodayChats');
        const yesterdayChatsEl = document.getElementById('dpYesterdayChats');
        const chatDeltaEl = document.getElementById('dpChatDelta');

        const todayUsersEl = document.getElementById('dpTodayUsers');
        const yesterdayUsersEl = document.getElementById('dpYesterdayUsers');
        const usersDeltaEl = document.getElementById('dpUsersDelta');

        const chatVals = parseChatSummaryValues();
        if (Number.isFinite(chatVals.today) && Number.isFinite(chatVals.yesterday)) {
            const d = computeDelta(chatVals.today, chatVals.yesterday);

            setConditionalMetricLink(todayChatsEl, d.today, () => buildWidgetReviewSelectionLink('today', contextPath));
            setConditionalMetricLink(yesterdayChatsEl, d.yesterday, () => buildWidgetReviewSelectionLink('yesterday', contextPath));

            if (chatDeltaEl) {
                const forcedDir = (window.chatProgressionDirection || d.direction || 'flat').toLowerCase();
                chatDeltaEl.innerHTML = renderProgressPill(d, forcedDir);
            }
        } else {
            if (todayChatsEl) todayChatsEl.textContent = 'N/A';
            if (yesterdayChatsEl) yesterdayChatsEl.textContent = 'N/A';
            if (chatDeltaEl) chatDeltaEl.innerHTML = '<span class="progression progression-flat">N/A</span>';
        }

        const newUserVals = parseNewUsersFromServerRenderedDom();
        if (Number.isFinite(newUserVals.today) && Number.isFinite(newUserVals.yesterday)) {
            const d = computeDelta(newUserVals.today, newUserVals.yesterday);
            const dates = getTodayYesterday();

            setConditionalMetricLink(
                todayUsersEl,
                d.today,
                `${contextPath}/dashboard/new-users/day?day=${encodeURIComponent(dates.today)}`
            );
            setConditionalMetricLink(
                yesterdayUsersEl,
                d.yesterday,
                `${contextPath}/dashboard/new-users/day?day=${encodeURIComponent(dates.yesterday)}`
            );

            if (usersDeltaEl) {
                const forcedDir = (window.newUsersProgressionDirection || d.direction || 'flat').toLowerCase();
                usersDeltaEl.innerHTML = renderProgressPill(d, forcedDir);
            }
        } else {
            if (todayUsersEl) todayUsersEl.textContent = 'N/A';
            if (yesterdayUsersEl) yesterdayUsersEl.textContent = 'N/A';
            if (usersDeltaEl) usersDeltaEl.innerHTML = '<span class="progression progression-flat">N/A</span>';
        }

        const summaryToday = document.getElementById('summaryTodayChats');
        const summaryYesterday = document.getElementById('summaryYesterdayChats');

        if (Number.isFinite(chatVals.today)) {
            setConditionalMetricLink(summaryToday, chatVals.today, () => buildWidgetReviewSelectionLink('today', contextPath));
        }
        if (Number.isFinite(chatVals.yesterday)) {
            setConditionalMetricLink(summaryYesterday, chatVals.yesterday, () => buildWidgetReviewSelectionLink('yesterday', contextPath));
        }

        applyDeltaClasses(section);
    }

    function renderActiveUsersDelta(data) {
        const deltaEl = document.getElementById('activeUsersDelta');
        if (!deltaEl) return;

        const activeUsers = typeof data.activeUsers === 'number' ? data.activeUsers : null;
        const activeUsersYesterday = typeof data.activeUsersYesterday === 'number' ? data.activeUsersYesterday : null;

        let delta = typeof data.activeUsersDelta === 'number' ? data.activeUsersDelta : null;
        let pct = typeof data.activeUsersDeltaPct === 'number' ? data.activeUsersDeltaPct : null;
        let direction = (data.activeUsersDirection || '').toLowerCase();

        if (delta === null && activeUsers !== null && activeUsersYesterday !== null) {
            const d = computeDelta(activeUsers, activeUsersYesterday);
            delta = d.delta;
            pct = d.pct;
            direction = d.direction;
        }

        if (delta === null) {
            deltaEl.className = 'progression progression-flat';
            deltaEl.textContent = '—';
            deltaEl.setAttribute('data-direction', 'flat');
            return;
        }

        if (!Number.isFinite(pct)) {
            pct = 0;
        }

        if (!direction) {
            direction = delta > 0 ? 'up' : delta < 0 ? 'down' : 'flat';
        }

        deltaEl.classList.remove('progression-up', 'progression-down', 'progression-flat');
        deltaEl.classList.add(
            direction === 'up' ? 'progression-up' :
                direction === 'down' ? 'progression-down' :
                    'progression-flat'
        );
        deltaEl.setAttribute('data-direction', direction);
        deltaEl.textContent = `${formatSignedInt(delta)} (${formatPct(pct)}%) vs yesterday`;
    }

    const dashboardConfig = window.dashboardConfig || {};
    const contextPath = dashboardConfig.contextPath || '';

    const termSlices = parseSlices(window.termChartData || []);
    const widgetSlices = parseSlices(window.widgetPieChartData || []);

    const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b'];

    const termChartEl = document.getElementById('termChart');
    const widgetChartEl = document.getElementById('widgetOverviewPieChart');
    const ctx = termChartEl?.getContext('2d');
    const widgetPieCtx = widgetChartEl?.getContext('2d');

    function openTermReview(term) {
        if (!term) return;
        window.location.href = `${contextPath}/dashboard/term-review?term=${encodeURIComponent(term)}`;
    }

    if (ctx && termSlices.length) {
        const { labels, values, colors } = buildSeries(termSlices, palette);

        new Chart(ctx, {
            type: 'pie',
            data: {
                labels,
                datasets: [{ data: values, backgroundColor: colors }]
            },
            options: {
                plugins: {
                    tooltip: {
                        callbacks: {
                            title: contextRows => {
                                const i = contextRows?.[0]?.dataIndex;
                                return (i !== undefined ? termSlices[i]?.term : '') || '';
                            },
                            label: context => {
                                const slice = termSlices[context.dataIndex];
                                return slice ? `${slice.label}: ${slice.count}` : '';
                            }
                        }
                    },
                    legend: { display: false }
                },
                responsive: true,
                maintainAspectRatio: false,
                onClick: (_event, elements) => {
                    if (!elements?.length) return;
                    const slice = termSlices[elements[0].index];
                    if (!slice) return;
                    openTermReview(slice.term);
                }
            }
        });
    }

    if (widgetPieCtx && widgetSlices.length) {
        const { labels, values, colors } = buildSeries(widgetSlices, palette);

        new Chart(widgetPieCtx, {
            type: 'pie',
            data: {
                labels,
                datasets: [{ data: values, backgroundColor: colors }]
            },
            options: {
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: context => {
                                const slice = widgetSlices[context.dataIndex];
                                return slice ? `${slice.label}: ${slice.count}` : '';
                            }
                        }
                    },
                    legend: { position: 'bottom' }
                },
                responsive: true,
                maintainAspectRatio: false,
                onClick: (_event, elements) => {
                    if (!elements?.length) return;
                    const slice = widgetSlices[elements[0].index];
                    if (!slice) return;

                    const widgetId = slice.widgetId || slice.label;
                    if (!widgetId) return;

                    window.location.href = `${contextPath}/dashboard/widgets/view?widgetId=${encodeURIComponent(widgetId)}`;
                }
            }
        });
    }

    const legendEl = document.getElementById('termChartLegend');
    if (legendEl && termSlices.length) {
        const frag = document.createDocumentFragment();

        for (let i = 0; i < termSlices.length; i++) {
            const slice = termSlices[i] || {};
            const chip = document.createElement('button');
            chip.className = 'legend-chip';
            chip.style.background = palette[i % palette.length];
            chip.type = 'button';
            chip.textContent = `${slice.label ?? ''} (${typeof slice.count === 'number' ? slice.count : 0})`;
            chip.dataset.term = slice.term || '';
            frag.appendChild(chip);
        }

        legendEl.appendChild(frag);

        legendEl.addEventListener('click', event => {
            const chip = event.target.closest('.legend-chip');
            if (!chip || !legendEl.contains(chip)) return;
            openTermReview(chip.dataset.term);
        });

        legendEl.addEventListener('keydown', event => {
            const chip = event.target.closest('.legend-chip');
            if (!chip || !legendEl.contains(chip)) return;
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                openTermReview(chip.dataset.term);
            }
        });
    }

    (async function loadTopSessions() {
        const totalEl = document.getElementById('totalSessions');
        const listEl = document.getElementById('topSessionList');

        const activeDaysEl = document.getElementById('activeDaysLabel');
        const activeCountLink = document.getElementById('activeSessionsLink');
        const inactiveCountLink = document.getElementById('inactiveSessionsLink');

        if (!listEl || !totalEl) return;

        try {
            const url = `${contextPath}/dashboard/sessions.json?page=1&pageSize=10&sortBy=count&sortDir=desc`;
            const resp = await fetch(url, {
                credentials: 'same-origin',
                headers: { Accept: 'application/json' }
            });

            if (!resp.ok) {
                totalEl.textContent = 'N/A';
                if (activeCountLink) activeCountLink.textContent = 'N/A';
                if (inactiveCountLink) inactiveCountLink.textContent = 'N/A';
                renderActiveUsersDelta({});
                return;
            }

            const data = await resp.json();
            if (!data || data.status !== 'ok') {
                totalEl.textContent = 'N/A';
                if (activeCountLink) activeCountLink.textContent = 'N/A';
                if (inactiveCountLink) inactiveCountLink.textContent = 'N/A';
                renderActiveUsersDelta({});
                return;
            }

            const activeDays = Number.isInteger(data.activeDays) && data.activeDays > 0 ? data.activeDays : 7;
            const activeUsers = typeof data.activeUsers === 'number' ? data.activeUsers : null;
            const inactiveUsers = typeof data.inactiveUsers === 'number' ? data.inactiveUsers : null;

            totalEl.textContent = typeof data.total === 'number' ? String(data.total) : '—';
            if (activeDaysEl) activeDaysEl.textContent = String(activeDays);

            if (activeCountLink) {
                activeCountLink.textContent = activeUsers === null ? '—' : String(activeUsers);
                activeCountLink.href = `${contextPath}/dashboard/sessions?activity=active&activeDays=${encodeURIComponent(String(activeDays))}`;
            }

            if (inactiveCountLink) {
                inactiveCountLink.textContent = inactiveUsers === null ? '—' : String(inactiveUsers);
                inactiveCountLink.href = `${contextPath}/dashboard/inactive-users`;
            }

            // NEW: apply active-users day-over-day delta (green/red/flat)
            renderActiveUsersDelta(data);

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
                        ? `<a class="customer-profile-link" href="${contextPath}/customer-profile?sessionId=${encodeURIComponent(sessionId)}">${esc(label)}</a>`
                        : esc(label)}
                    </div>
                        ${label !== sessionId && sessionId
                        ? `<div class="session-id-muted"><a class="customer-profile-link" href="${contextPath}/customer-profile?sessionId=${encodeURIComponent(sessionId)}">${esc(sessionId)}</a></div>`
                        : ''}
                    </td>
                    <td>
                        <a class="session-count-link" href="${esc(reviewUrl)}">${count} chats</a>
                    </td>
                    <td>${esc(last)}</td>
                </tr>`;
            }

            listEl.innerHTML = html;
        } catch (e) {
            console.warn('Unable to load top sessions:', e);
            totalEl.textContent = 'N/A';
            if (activeCountLink) activeCountLink.textContent = 'N/A';
            if (inactiveCountLink) inactiveCountLink.textContent = 'N/A';
            renderActiveUsersDelta({});
        }
    })();

    wireDynamicLinks();
    applyProgressionDirectionStyling();
    hydrateDailyProgressSection(contextPath);
    applyDeltaClasses(document);
})();
