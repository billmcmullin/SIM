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

    function parseObject(input) {
        if (input && typeof input === 'object' && !Array.isArray(input)) return input;
        if (typeof input !== 'string') return {};
        try {
            const parsed = JSON.parse(input);
            return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
        } catch {
            return {};
        }
    }

    function parseTrendData(input) {
        if (input && typeof input === 'object' && !Array.isArray(input)) return input;
        if (typeof input !== 'string') return { labels: [], values: [], days: 5 };
        try {
            const parsed = JSON.parse(input);
            return (parsed && typeof parsed === 'object' && !Array.isArray(parsed))
                ? parsed
                : { labels: [], values: [], days: 5 };
        } catch {
            return { labels: [], values: [], days: 5 };
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
        const cls = direction === 'up' ? 'progression-up' : direction === 'down' ? 'progression-down' : 'progression-flat';
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
        return {
            today: parseIntFromText(document.getElementById('serverNewUsersToday')?.textContent),
            yesterday: parseIntFromText(document.getElementById('serverNewUsersYesterday')?.textContent)
        };
    }

    function parseTermsFromServerRenderedDom() {
        return {
            today: parseIntFromText(document.getElementById('serverTermsToday')?.textContent),
            yesterday: parseIntFromText(document.getElementById('serverTermsYesterday')?.textContent)
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

    function buildTermReviewUrl(contextPath, term, increaseOnly) {
        const qp = new URLSearchParams();
        qp.set('term', term || '');
        if (increaseOnly) qp.set('mode', 'increaseOnly');
        return `${contextPath}/dashboard/term-review?${qp.toString()}`;
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

    function ensureSummaryProgressUi() {
        const bodyEl = document.getElementById('dailySummaryBody');
        if (!bodyEl) return;
        if (document.getElementById('dailySummaryProgressWrap')) return;

        const wrap = document.createElement('div');
        wrap.id = 'dailySummaryProgressWrap';
        wrap.style.marginBottom = '10px';

        wrap.innerHTML = `
            <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
                <div style="flex:1 1 260px; min-width:220px; background:#e5e7eb; border-radius:999px; height:10px; overflow:hidden;">
                    <div id="dailySummaryProgressBar" style="width:0%; height:100%; background:#2563eb; transition:width .25s ease;"></div>
                </div>
                <span id="dailySummaryProgressText" class="helper-note" style="font-size:12px;">0%</span>
            </div>
        `;
        bodyEl.parentNode.insertBefore(wrap, bodyEl);
    }

    function setSummaryProgress(pct, message) {
        const bar = document.getElementById('dailySummaryProgressBar');
        const txt = document.getElementById('dailySummaryProgressText');
        const p = Math.max(0, Math.min(100, Number(pct) || 0));
        if (bar) bar.style.width = `${p}%`;
        if (txt) txt.textContent = `${p}%${message ? ` • ${message}` : ''}`;
    }

    function hideSummaryProgressIfDone(inProgress) {
        const wrap = document.getElementById('dailySummaryProgressWrap');
        if (!wrap) return;
        wrap.style.display = inProgress ? '' : 'none';
    }

    async function loadDailySummary(contextPath) {
        const bodyEl = document.getElementById('dailySummaryBody');
        const metaEl = document.getElementById('dailySummaryMeta');
        if (!bodyEl) return;

        ensureSummaryProgressUi();
        bodyEl.innerHTML = '<p style="margin:0;">Loading latest summary…</p>';
        if (metaEl) metaEl.textContent = 'Loading latest daily analysis…';
        setSummaryProgress(5, 'loading');

        let pollCount = 0;
        const maxPolls = 30;

        while (pollCount < maxPolls) {
            pollCount++;
            let data = null;

            try {
                const resp = await fetch(`${contextPath}/dashboard/daily-summary.json`, {
                    credentials: 'same-origin',
                    headers: { Accept: 'application/json' }
                });

                if (!resp.ok) {
                    bodyEl.innerHTML = '<p style="margin:0;">Unable to load summary right now.</p>';
                    if (metaEl) metaEl.textContent = `Status: ${resp.status}`;
                    setSummaryProgress(0, 'error');
                    hideSummaryProgressIfDone(false);
                    return;
                }

                data = await resp.json();
            } catch (e) {
                console.warn('Unable to load daily summary:', e);
                bodyEl.innerHTML = '<p style="margin:0;">Unable to load summary right now.</p>';
                if (metaEl) metaEl.textContent = 'Request failed.';
                setSummaryProgress(0, 'request failed');
                hideSummaryProgressIfDone(false);
                return;
            }

            if (!data || data.status !== 'ok' || !data.summary) {
                bodyEl.innerHTML = '<p style="margin:0;">Summary is not available yet.</p>';
                if (metaEl) metaEl.textContent = 'No summary returned.';
                setSummaryProgress(0, 'not ready');
                hideSummaryProgressIfDone(false);
                return;
            }

            const s = data.summary || {};
            const m = data.meta || {};
            const inProgress = !!m.inProgress;
            const pct = Number.isFinite(Number(m.progressPct)) ? Number(m.progressPct) : (inProgress ? 30 : 100);

            setSummaryProgress(pct, m.message || (inProgress ? 'generating' : 'complete'));

            bodyEl.innerHTML = `
                <div style="display:grid; grid-template-columns:repeat(auto-fit,minmax(220px,1fr)); gap:12px;">
                    <div>
                        <h4 style="margin:0 0 6px 0;">Overall</h4>
                        <p style="margin:0; white-space:pre-wrap;">${esc(s.overall || '—')}</p>
                    </div>
                    <div>
                        <h4 style="margin:0 0 6px 0;">Quality</h4>
                        <p style="margin:0; white-space:pre-wrap;">${esc(s.quality || '—')}</p>
                    </div>
                    <div>
                        <h4 style="margin:0 0 6px 0;">Response</h4>
                        <p style="margin:0; white-space:pre-wrap;">${esc(s.response || '—')}</p>
                    </div>
                    <div>
                        <h4 style="margin:0 0 6px 0;">Usage</h4>
                        <p style="margin:0; white-space:pre-wrap;">${esc(s.usage || '—')}</p>
                    </div>
                </div>
            `;

            const entryCount = Number.isFinite(Number(s.entryCount)) ? Number(s.entryCount) : 0;
            const generatedAt = m.generatedAt ? String(m.generatedAt) : '';
            const slot = Number.isFinite(Number(m.slot)) ? Number(m.slot) : 0;
            if (metaEl) {
                metaEl.textContent = `Entries analyzed: ${entryCount} • Slot: ${slot} • Generated: ${generatedAt || '—'}${inProgress ? ' • updating…' : ''}`;
            }

            hideSummaryProgressIfDone(inProgress);

            if (!inProgress) return;
            await new Promise(r => setTimeout(r, 2000));
        }

        if (metaEl) metaEl.textContent = 'Summary is still generating. Please refresh shortly.';
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

        const todayTermsEl = document.getElementById('dpTodayTerms');
        const yesterdayTermsEl = document.getElementById('dpYesterdayTerms');
        const termsDeltaEl = document.getElementById('dpTermsDelta');

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

            setConditionalMetricLink(todayUsersEl, d.today, `${contextPath}/dashboard/new-users/drilldown?day=${encodeURIComponent(dates.today)}`);
            setConditionalMetricLink(yesterdayUsersEl, d.yesterday, `${contextPath}/dashboard/new-users/drilldown?day=${encodeURIComponent(dates.yesterday)}`);

            if (usersDeltaEl) {
                const forcedDir = (window.newUsersProgressionDirection || d.direction || 'flat').toLowerCase();
                usersDeltaEl.innerHTML = renderProgressPill(d, forcedDir);
            }
        } else {
            if (todayUsersEl) todayUsersEl.textContent = 'N/A';
            if (yesterdayUsersEl) yesterdayUsersEl.textContent = 'N/A';
            if (usersDeltaEl) usersDeltaEl.innerHTML = '<span class="progression progression-flat">N/A</span>';
        }

        const termVals = parseTermsFromServerRenderedDom();
        if (Number.isFinite(termVals.today) && Number.isFinite(termVals.yesterday)) {
            const d = computeDelta(termVals.today, termVals.yesterday);
            const dates = getTodayYesterday();

            setConditionalMetricLink(todayTermsEl, d.today, `${contextPath}/dashboard/sessions/drilldown/date-review?date=${encodeURIComponent(dates.today)}`);
            setConditionalMetricLink(yesterdayTermsEl, d.yesterday, `${contextPath}/dashboard/sessions/drilldown/date-review?date=${encodeURIComponent(dates.yesterday)}`);

            if (termsDeltaEl) {
                const forcedDir = (window.termsProgressionDirection || d.direction || 'flat').toLowerCase();
                termsDeltaEl.innerHTML = renderProgressPill(d, forcedDir);
            }
        } else {
            if (todayTermsEl) todayTermsEl.textContent = 'N/A';
            if (yesterdayTermsEl) yesterdayTermsEl.textContent = 'N/A';
            if (termsDeltaEl) termsDeltaEl.innerHTML = '<span class="progression progression-flat">N/A</span>';
        }

        const summaryToday = document.getElementById('summaryTodayChats');
        const summaryYesterday = document.getElementById('summaryYesterdayChats');
        if (Number.isFinite(chatVals.today)) setConditionalMetricLink(summaryToday, chatVals.today, () => buildWidgetReviewSelectionLink('today', contextPath));
        if (Number.isFinite(chatVals.yesterday)) setConditionalMetricLink(summaryYesterday, chatVals.yesterday, () => buildWidgetReviewSelectionLink('yesterday', contextPath));

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

        if (!Number.isFinite(pct)) pct = 0;
        if (!direction) direction = delta > 0 ? 'up' : delta < 0 ? 'down' : 'flat';

        deltaEl.classList.remove('progression-up', 'progression-down', 'progression-flat');
        deltaEl.classList.add(direction === 'up' ? 'progression-up' : direction === 'down' ? 'progression-down' : 'progression-flat');
        deltaEl.setAttribute('data-direction', direction);
        deltaEl.textContent = `${formatSignedInt(delta)} (${formatPct(pct)}%) vs yesterday`;
    }

    function renderLastFiveDaysTrendChart() {
        const el = document.getElementById('lastFiveDaysTrendChart');
        if (!el || typeof Chart === 'undefined') return;

        const trend = parseTrendData(window.lastFiveDaysTrendData || {});
        const labelsRaw = Array.isArray(trend.labels) ? trend.labels : [];
        const valuesRaw = Array.isArray(trend.values) ? trend.values : [];

        const labels = labelsRaw.map(v => {
            const s = String(v || '');
            const d = new Date(`${s}T00:00:00`);
            if (Number.isNaN(d.getTime())) return s;
            return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
        });

        const values = valuesRaw.map(v => {
            const n = Number(v);
            return Number.isFinite(n) ? n : 0;
        });

        if (!labels.length || !values.length || labels.length !== values.length) return;

        const ctx = el.getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Total Entries',
                    data: values,
                    borderColor: '#1d4ed8',
                    backgroundColor: 'rgba(29, 78, 216, 0.12)',
                    fill: true,
                    tension: 0.28,
                    pointRadius: 3,
                    pointHoverRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            title: items => {
                                const i = items?.[0]?.dataIndex ?? -1;
                                return i >= 0 ? String(labelsRaw[i] || labels[i] || '') : '';
                            },
                            label: item => `Total entries: ${item.parsed?.y ?? 0}`
                        }
                    }
                },
                scales: {
                    y: { beginAtZero: true, ticks: { precision: 0 } }
                },
                onClick: (_event, elements) => {
                    if (!elements?.length) return;
                    const i = elements[0].index;
                    const rawDate = String(labelsRaw[i] || '').trim();
                    const value = Number(values[i]);
                    if (!rawDate) return;
                    if (!Number.isFinite(value) || value <= 0) return;
                    window.location.href = `${contextPath}/dashboard/sessions/drilldown/date-review-relative?date=${encodeURIComponent(rawDate)}`;
                }
            }
        });
    }

    const dashboardConfig = window.dashboardConfig || {};
    const contextPath = dashboardConfig.contextPath || '';

    const termSlices = parseSlices(window.termChartData || []);
    const widgetSlices = parseSlices(window.widgetPieChartData || []);
    const termIncreaseMap = parseObject(window.termIncreaseMapJson || {});
    const termTotalMap = parseObject(window.termTotalMapJson || {});
    let legendMode = String(window.termLegendDefaultMode || 'increase').toLowerCase() === 'total' ? 'total' : 'increase';

    const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b'];

    const termChartEl = document.getElementById('termChart');
    const widgetChartEl = document.getElementById('widgetOverviewPieChart');
    const ctx = termChartEl?.getContext('2d');
    const widgetPieCtx = widgetChartEl?.getContext('2d');

    let termChartInstance = null;

    function openTermReview(term) {
        if (!term) return;
        const increaseOnly = legendMode === 'increase';
        if (increaseOnly) {
            const inc = Number(termIncreaseMap[term] || 0);
            if (!(Number.isFinite(inc) && inc > 0)) return;
        }
        window.location.href = buildTermReviewUrl(contextPath, term, increaseOnly);
    }

    function getTermValueForMode(term, fallbackCount, mode) {
        if (mode === 'increase') {
            const v = Number(termIncreaseMap[term] || 0);
            return Number.isFinite(v) ? Math.max(0, v) : 0;
        }
        const t = Number(termTotalMap[term]);
        if (Number.isFinite(t)) return Math.max(0, t);
        return Number.isFinite(fallbackCount) ? Math.max(0, fallbackCount) : 0;
    }

    function buildTermChartValues(mode) {
        const values = new Array(termSlices.length);
        for (let i = 0; i < termSlices.length; i++) {
            const s = termSlices[i] || {};
            values[i] = getTermValueForMode(s.term || '', s.count, mode);
        }
        return values;
    }

    function updateTermModeSummary(mode) {
        const summaryEl = document.getElementById('termLegendModeSummary');
        if (!summaryEl) return;
        let total = 0;
        for (let i = 0; i < termSlices.length; i++) {
            const s = termSlices[i] || {};
            total += getTermValueForMode(s.term || '', s.count, mode);
        }
        summaryEl.textContent = mode === 'increase'
            ? `Showing increases (today): ${total} total entries`
            : `Showing totals (all entries): ${total} total entries`;
    }

    function renderOrUpdateTermChart(mode) {
        if (!ctx || !termSlices.length) return;

        const labels = termSlices.map(s => (s?.label ?? ''));
        const colors = termSlices.map((_, i) => palette[i % palette.length]);
        const values = buildTermChartValues(mode);

        if (!termChartInstance) {
            termChartInstance = new Chart(ctx, {
                type: 'pie',
                data: { labels, datasets: [{ data: values, backgroundColor: colors }] },
                options: {
                    plugins: {
                        tooltip: {
                            callbacks: {
                                title: contextRows => {
                                    const i = contextRows?.[0]?.dataIndex;
                                    return (i !== undefined ? termSlices[i]?.term : '') || '';
                                },
                                label: context => {
                                    const i = context.dataIndex;
                                    const slice = termSlices[i];
                                    const term = slice?.term || '';
                                    const value = getTermValueForMode(term, slice?.count, legendMode);
                                    return `${slice?.label ?? ''}: ${value}`;
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
        } else {
            termChartInstance.data.datasets[0].data = values;
            termChartInstance.update();
        }

        updateTermModeSummary(mode);
    }

    if (widgetPieCtx && widgetSlices.length) {
        const { labels, values, colors } = buildSeries(widgetSlices, palette);

        new Chart(widgetPieCtx, {
            type: 'pie',
            data: { labels, datasets: [{ data: values, backgroundColor: colors }] },
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
    const legendToggleBtn = document.getElementById('termLegendValueToggleBtn');

    function buildLegendChip(slice, index) {
        const term = slice.term || '';
        const label = slice.label ?? '';
        const rangeCount = typeof slice.count === 'number' ? slice.count : 0;
        const inc = Number(termIncreaseMap[term] || 0);
        const total = Number(termTotalMap[term] || rangeCount || 0);
        const hasIncrease = Number.isFinite(inc) && inc > 0;

        const chip = document.createElement('div');
        chip.className = 'legend-chip';
        chip.style.setProperty('--legend-dot-color', palette[index % palette.length]);
        chip.dataset.term = term;
        chip.dataset.mode = legendMode;

        const nameLink = document.createElement('a');
        nameLink.className = 'legend-chip-name-link';
        nameLink.dataset.term = term;
        nameLink.title = label;
        nameLink.textContent = label;

        if (legendMode === 'total') nameLink.href = buildTermReviewUrl(contextPath, term, false);
        else if (hasIncrease) nameLink.href = buildTermReviewUrl(contextPath, term, true);
        else {
            nameLink.removeAttribute('href');
            nameLink.setAttribute('aria-disabled', 'true');
            nameLink.classList.add('is-disabled');
            nameLink.title = `${label} (no increases today)`;
        }

        chip.appendChild(nameLink);

        const valueWrap = document.createElement('span');
        valueWrap.className = 'legend-chip-value-wrap';

        if (legendMode === 'increase') {
            if (hasIncrease) {
                const incLink = document.createElement('a');
                incLink.className = 'legend-chip-increase-link';
                incLink.href = buildTermReviewUrl(contextPath, term, true);
                incLink.dataset.term = term;
                incLink.dataset.increaseOnly = '1';
                incLink.textContent = `+${inc}`;
                valueWrap.appendChild(incLink);
            } else {
                const zero = document.createElement('span');
                zero.className = 'legend-chip-value-muted';
                zero.textContent = '+0';
                valueWrap.appendChild(zero);
            }
        } else {
            const totalLink = document.createElement('a');
            totalLink.className = 'legend-chip-total-link';
            totalLink.href = buildTermReviewUrl(contextPath, term, false);
            totalLink.dataset.term = term;
            totalLink.textContent = String(Number.isFinite(total) ? total : 0);
            valueWrap.appendChild(totalLink);
        }

        chip.appendChild(valueWrap);
        return chip;
    }

    function renderLegend() {
        if (!legendEl) return;
        legendEl.innerHTML = '';
        if (!termSlices.length) return;

        const frag = document.createDocumentFragment();
        for (let i = 0; i < termSlices.length; i++) {
            frag.appendChild(buildLegendChip(termSlices[i] || {}, i));
        }
        legendEl.appendChild(frag);

        if (legendToggleBtn) {
            if (legendMode === 'increase') {
                legendToggleBtn.textContent = 'Show Totals';
                legendToggleBtn.setAttribute('aria-pressed', 'false');
            } else {
                legendToggleBtn.textContent = 'Show Increases';
                legendToggleBtn.setAttribute('aria-pressed', 'true');
            }
        }
    }

    if (legendEl) {
        renderLegend();

        legendEl.addEventListener('click', event => {
            const a = event.target.closest('a');
            if (a && legendEl.contains(a)) return;

            const chip = event.target.closest('.legend-chip');
            if (!chip || !legendEl.contains(chip)) return;

            const term = chip.dataset.term || '';
            if (!term) return;

            const mode = chip.dataset.mode || legendMode;
            if (mode === 'increase') {
                const inc = Number(termIncreaseMap[term] || 0);
                if (!(Number.isFinite(inc) && inc > 0)) return;
            }

            window.location.href = buildTermReviewUrl(contextPath, term, mode === 'increase');
        });
    }

    if (legendToggleBtn) {
        legendToggleBtn.addEventListener('click', () => {
            legendMode = legendMode === 'increase' ? 'total' : 'increase';
            renderLegend();
            renderOrUpdateTermChart(legendMode);
        });
    }

    renderOrUpdateTermChart(legendMode);
    renderLastFiveDaysTrendChart();
    loadDailySummary(contextPath);

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
                    <td><a class="session-count-link" href="${esc(reviewUrl)}">${count} chats</a></td>
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
