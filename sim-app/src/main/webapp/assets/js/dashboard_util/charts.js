(() => {
    'use strict';

    const core = window.DashboardCore;
    const termReview = window.DashboardTermReview;

    function renderLastFiveDaysTrendChart(contextPath) {
        const el = document.getElementById('lastFiveDaysTrendChart');
        if (!el || typeof Chart === 'undefined') {
            return;
        }

        const trend = core.parseTrendData(window.lastFiveDaysTrendData || {});
        const labelsRaw = Array.isArray(trend.labels) ? trend.labels : [];
        const valuesRaw = Array.isArray(trend.values) ? trend.values : [];

        const labels = labelsRaw.map(v => {
            const s = String(v || '');
            const d = new Date(`${s}T00:00:00`);
            if (Number.isNaN(d.getTime())) {
                return s;
            }
            return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
        });

        const values = valuesRaw.map(v => {
            const n = Number(v);
            return Number.isFinite(n) ? n : 0;
        });

        if (!labels.length || !values.length || labels.length !== values.length) {
            return;
        }

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
                    if (!elements?.length) {
                        return;
                    }
                    const i = elements[0].index;
                    const rawDate = String(labelsRaw[i] || '').trim();
                    const value = Number(values[i]);
                    if (!rawDate) {
                        return;
                    }
                    if (!Number.isFinite(value) || value <= 0) {
                        return;
                    }
                    const href = `${contextPath}/dashboard/sessions/drilldown/date-review-relative?date=${encodeURIComponent(rawDate)}`;
                    if (window.DashboardInlineView && typeof window.DashboardInlineView.openOrNavigate === 'function') {
                        window.DashboardInlineView.openOrNavigate(href, 'Date Review');
                    } else {
                        window.location.href = href;
                    }
                }
            }
        });
    }

    function bootPieAndTermCharts(contextPath) {
        const dayTermSlices = core.parseSlices(window.termChartData || []);
        const totalTermSlices = core.parseSlices(window.termChartTotalData || []);
        const widgetSlices = core.parseSlices(window.widgetPieChartData || []);
        const termIncreaseMap = core.parseObject(window.termIncreaseMapJson || {});
        const termTotalMap = core.parseObject(window.termTotalMapJson || {});
        let legendMode = String(window.termLegendDefaultMode || 'increase').toLowerCase() === 'total' ? 'total' : 'increase';

        const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b'];

        const termChartEl = document.getElementById('termChart');
        const widgetChartEl = document.getElementById('widgetOverviewPieChart');
        const ctx = termChartEl?.getContext('2d');
        const widgetPieCtx = widgetChartEl?.getContext('2d');

        let termChartInstance = null;

        function getSlicesForMode(mode) {
            if (mode === 'total') {
                return totalTermSlices;
            }
            return dayTermSlices;
        }

        async function openTermReview(term) {
            if (!term) {
                return;
            }
            const increaseOnly = legendMode === 'increase';
            if (increaseOnly) {
                const inc = Number(termIncreaseMap[term] || 0);
                if (!(Number.isFinite(inc) && inc > 0)) {
                    return;
                }
            }
            try {
                const href = await termReview.buildTermReviewSelectionLink(contextPath, term, increaseOnly);
                if (href) {
                    if (window.DashboardInlineView && typeof window.DashboardInlineView.openOrNavigate === 'function') {
                        window.DashboardInlineView.openOrNavigate(href, 'Term Review');
                    } else {
                        window.location.href = href;
                    }
                }
            } catch (e) {
                console.warn('Unable to open term review:', e);
                console.warn(e?.message || 'Unable to open chat review for this term right now.');
            }
        }

        function getTermValueForMode(term, fallbackCount, mode) {
            if (mode === 'increase') {
                const v = Number(termIncreaseMap[term] || 0);
                return Number.isFinite(v) ? Math.max(0, v) : 0;
            }
            const t = Number(termTotalMap[term]);
            if (Number.isFinite(t)) {
                return Math.max(0, t);
            }
            return Number.isFinite(fallbackCount) ? Math.max(0, fallbackCount) : 0;
        }

        function buildTermChartValues(mode, slices) {
            const safeSlices = Array.isArray(slices) ? slices : [];
            const values = new Array(safeSlices.length);
            for (let i = 0; i < safeSlices.length; i++) {
                const s = safeSlices[i] || {};
                values[i] = getTermValueForMode(s.term || '', s.count, mode);
            }
            return values;
        }

        function updateTermModeSummary(mode) {
            const summaryEl = document.getElementById('termLegendModeSummary');
            if (!summaryEl) {
                return;
            }

            const activeSlices = getSlicesForMode(mode);
            let total = 0;
            for (let i = 0; i < activeSlices.length; i++) {
                const s = activeSlices[i] || {};
                total += getTermValueForMode(s.term || '', s.count, mode);
            }

            summaryEl.textContent = mode === 'increase'
                ? `Showing increases (today): ${total} total entries`
                : `Showing totals (all entries): ${total} total entries`;
        }

        function renderOrUpdateTermChart(mode) {
            const activeSlices = getSlicesForMode(mode);
            if (!ctx || !activeSlices.length) {
                return;
            }

            const labels = activeSlices.map(s => (s?.label ?? ''));
            const colors = activeSlices.map((_, i) => palette[i % palette.length]);
            const values = buildTermChartValues(mode, activeSlices);

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
                                        return (i !== undefined ? activeSlices[i]?.term : '') || '';
                                    },
                                    label: context => {
                                        const i = context.dataIndex;
                                        const slice = activeSlices[i];
                                        const term = slice?.term || '';
                                        const value = getTermValueForMode(term, slice?.count, mode);
                                        return `${slice?.label ?? ''}: ${value}`;
                                    }
                                }
                            },
                            legend: { display: false }
                        },
                        responsive: true,
                        maintainAspectRatio: false,
                        onClick: async (_event, elements) => {
                            if (!elements?.length) {
                                return;
                            }
                            const slice = activeSlices[elements[0].index];
                            if (!slice) {
                                return;
                            }
                            await openTermReview(slice.term);
                        }
                    }
                });
            } else {
                termChartInstance.data.labels = labels;
                termChartInstance.data.datasets[0].data = values;
                termChartInstance.data.datasets[0].backgroundColor = colors;
                termChartInstance.update();
            }

            updateTermModeSummary(mode);
        }

        if (widgetPieCtx && widgetSlices.length) {
            const { labels, values, colors } = core.buildSeries(widgetSlices, palette);

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
                        if (!elements?.length) {
                            return;
                        }
                        const slice = widgetSlices[elements[0].index];
                        if (!slice) {
                            return;
                        }
                        const widgetId = slice.widgetId || slice.label;
                        if (!widgetId) {
                            return;
                        }
                        const href = `${contextPath}/dashboard/widgets/view?widgetId=${encodeURIComponent(widgetId)}`;
                        if (window.DashboardInlineView && typeof window.DashboardInlineView.openOrNavigate === 'function') {
                            window.DashboardInlineView.openOrNavigate(href, 'Widget View');
                        } else {
                            window.location.href = href;
                        }
                    }
                }
            });
        }

        const legendEl = document.getElementById('termChartLegend');
        const legendToggleBtn = document.getElementById('termLegendValueToggleBtn');

        function makeTermDynamicLink(term, increaseOnly, cssClass, text, title) {
            const a = document.createElement('a');
            a.className = `${cssClass} metric-dynamic-link`;
            a.href = '#';
            a.dataset.term = term || '';
            a.textContent = text || '';
            if (title) {
                a.title = title;
            }
            a.__buildHref = async () => termReview.buildTermReviewSelectionLink(contextPath, term, increaseOnly);
            return a;
        }

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

            const nameLink = makeTermDynamicLink(
                term,
                legendMode === 'increase',
                'legend-chip-name-link',
                label,
                label
            );

            if (legendMode === 'increase' && !hasIncrease) {
                nameLink.removeAttribute('href');
                nameLink.setAttribute('aria-disabled', 'true');
                nameLink.classList.add('is-disabled');
                nameLink.title = `${label} (no increases today)`;
                nameLink.__buildHref = null;
            }

            chip.appendChild(nameLink);

            const valueWrap = document.createElement('span');
            valueWrap.className = 'legend-chip-value-wrap';

            if (legendMode === 'increase') {
                if (hasIncrease) {
                    const incLink = makeTermDynamicLink(
                        term,
                        true,
                        'legend-chip-increase-link',
                        `+${inc}`
                    );
                    incLink.dataset.increaseOnly = '1';
                    valueWrap.appendChild(incLink);
                } else {
                    const zero = document.createElement('span');
                    zero.className = 'legend-chip-value-muted';
                    zero.textContent = '+0';
                    valueWrap.appendChild(zero);
                }
            } else {
                const totalLink = makeTermDynamicLink(
                    term,
                    false,
                    'legend-chip-total-link',
                    String(Number.isFinite(total) ? total : 0)
                );
                valueWrap.appendChild(totalLink);
            }

            chip.appendChild(valueWrap);
            return chip;
        }

        function renderLegend() {
            if (!legendEl) {
                return;
            }
            legendEl.innerHTML = '';
            const activeSlices = getSlicesForMode(legendMode);
            if (!activeSlices.length) {
                return;
            }

            const frag = document.createDocumentFragment();
            for (let i = 0; i < activeSlices.length; i++) {
                frag.appendChild(buildLegendChip(activeSlices[i] || {}, i));
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

            legendEl.addEventListener('click', async event => {
                const a = event.target.closest('a');
                if (a && legendEl.contains(a) && a.__buildHref) {
                    return;
                }

                const chip = event.target.closest('.legend-chip');
                if (!chip || !legendEl.contains(chip)) {
                    return;
                }

                const term = chip.dataset.term || '';
                if (!term) {
                    return;
                }

                const mode = chip.dataset.mode || legendMode;
                if (mode === 'increase') {
                    const inc = Number(termIncreaseMap[term] || 0);
                    if (!(Number.isFinite(inc) && inc > 0)) {
                        return;
                    }
                }

                try {
                    const href = await termReview.buildTermReviewSelectionLink(contextPath, term, mode === 'increase');
                    if (href) {
                        if (window.DashboardInlineView && typeof window.DashboardInlineView.openOrNavigate === 'function') {
                            window.DashboardInlineView.openOrNavigate(href, 'Term Review');
                        } else {
                            window.location.href = href;
                        }
                    }
                } catch (e) {
                    console.warn('Unable to open term review:', e);
                    console.warn(e?.message || 'Unable to open chat review for this term right now.');
                }
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
    }

    window.DashboardCharts = {
        bootPieAndTermCharts,
        renderLastFiveDaysTrendChart
    };
})();
