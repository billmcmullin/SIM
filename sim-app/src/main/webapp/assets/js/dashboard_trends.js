(function () {
    const cfg = window.dashboardConfig || {};
    const contextPath = cfg.contextPath || '';
    const SELECT_BY_DAY_URL = `${contextPath}/dashboard/trends/select`;

    let trendData = window.trendData || '{}';
    if (typeof trendData === 'string') {
        try {
            trendData = JSON.parse(trendData);
        } catch {
            trendData = { labels: [], values: [], widgetSeries: [], averagePostsPerDay: 0, totalPosts: 0 };
        }
    }

    const selectedDays = Number(window.trendDaysSelected || 30);

    const labels = Array.isArray(trendData.labels) ? trendData.labels : [];
    const totalValues = Array.isArray(trendData.values) ? trendData.values : [];
    const widgetSeries = Array.isArray(trendData.widgetSeries) ? trendData.widgetSeries : [];
    const averagePostsPerDay = Number(trendData.averagePostsPerDay || 0);
    const totalPosts = Number(trendData.totalPosts || 0);

    const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b', '#0ea5e9'];

    const daySelect = document.getElementById('trendDaysSelect');
    if (daySelect) {
        daySelect.value = String(selectedDays);
        daySelect.addEventListener('change', () => {
            const days = daySelect.value || '30';
            window.location.href = `${contextPath}/dashboard/trends?days=${encodeURIComponent(days)}`;
        });
    }

    // Summary stats
    const avgEl = document.getElementById('avgPostsPerDay');
    if (avgEl) {
        avgEl.textContent = Number.isFinite(averagePostsPerDay) ? averagePostsPerDay.toFixed(2) : '0.00';
    }

    const totalEl = document.getElementById('totalPosts');
    if (totalEl) {
        totalEl.textContent = Number.isFinite(totalPosts) ? String(totalPosts) : '0';
    }

    async function openReviewForDay(dayLabel, widgetId) {
        if (!dayLabel) {
            return;
        }
        try {
            const payload = { day: dayLabel };
            if (widgetId) {
                payload.widgetId = widgetId;
            }

            const res = await fetch(SELECT_BY_DAY_URL, {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const text = await res.text().catch(() => '');
            let json = {};
            try { json = JSON.parse(text); } catch { json = { message: text }; }

            if (!res.ok) {
                throw new Error(json.message || `HTTP ${res.status}`);
            }
            if (json.status === 'ok' && json.selectionId) {
                window.location.href = `${contextPath}/dashboard/widgets/drilldown/review?selectionId=${encodeURIComponent(json.selectionId)}`;
            } else {
                throw new Error(json.message || 'Unable to create review selection');
            }
        } catch (err) {
            console.error(err);
            alert(`Unable to open review for selected day: ${err.message}`);
        }
    }

    function trendChartOptions(widgetId) {
        return {
            responsive: true,
            maintainAspectRatio: false,
            scales: { y: { beginAtZero: true } },
            interaction: { mode: 'nearest', intersect: true },
            onHover: (event, elements, chart) => {
                if (chart && chart.canvas) {
                    chart.canvas.style.cursor = (elements && elements.length) ? 'pointer' : 'default';
                }
            },
            onClick: async (event, elements, chart) => {
                if (!elements || !elements.length) {
                    return;
                }
                const idx = elements[0].index;
                const dayLabel = chart?.data?.labels?.[idx];
                if (!dayLabel) {
                    return;
                }
                await openReviewForDay(dayLabel, widgetId || null);
            }
        };
    }

    const totalCtx = document.getElementById('trendChart')?.getContext('2d');
    if (totalCtx) {
        new Chart(totalCtx, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Total Entries Per Day',
                    data: totalValues,
                    borderColor: '#1d4ed8',
                    backgroundColor: 'rgba(29, 78, 216, 0.18)',
                    fill: true,
                    tension: 0.25,
                    borderWidth: 2,
                    pointRadius: 3,
                    pointHoverRadius: 5
                }]
            },
            options: trendChartOptions(null)
        });
    }

    const widgetContainer = document.getElementById('widgetTrendCharts');
    if (!widgetContainer) {
        return;
    }

    if (!widgetSeries.length) {
        widgetContainer.innerHTML = '<p class="helper-note">No widget trend data available for this period.</p>';
        return;
    }

    widgetSeries.forEach((series, index) => {
        const card = document.createElement('section');
        card.className = 'chart-wrapper';
        card.style.marginTop = '20px';

        const title = document.createElement('h3');
        const name = series.name || `Widget ${index + 1}`;
        const widgetAvg = Number(series.avgPerDay || 0);
        const widgetTotal = Number(series.total || 0);
        title.textContent = `${name} — Entries per day (Avg: ${widgetAvg.toFixed(2)}, Total: ${widgetTotal})`;
        title.style.marginTop = '0';
        card.appendChild(title);

        const chartArea = document.createElement('div');
        chartArea.className = 'chart-area';
        chartArea.style.maxWidth = '100%';
        chartArea.style.height = '320px';

        const canvas = document.createElement('canvas');
        chartArea.appendChild(canvas);
        card.appendChild(chartArea);
        widgetContainer.appendChild(card);

        new Chart(canvas.getContext('2d'), {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: name,
                    data: Array.isArray(series.values) ? series.values : [],
                    borderColor: palette[index % palette.length],
                    backgroundColor: 'rgba(99, 102, 241, 0.10)',
                    fill: true,
                    tension: 0.25,
                    borderWidth: 2,
                    pointRadius: 2,
                    pointHoverRadius: 5
                }]
            },
            options: trendChartOptions(series.widgetId || null)
        });
    });
})();
