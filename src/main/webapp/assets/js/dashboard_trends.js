(function () {
    const cfg = window.dashboardConfig || {};
    const contextPath = cfg.contextPath || '';

    let trendData = window.trendData || '{}';
    if (typeof trendData === 'string') {
        try {
            trendData = JSON.parse(trendData);
        } catch (e) {
            trendData = { labels: [], values: [], widgetSeries: [] };
        }
    }

    const selectedDays = Number(window.trendDaysSelected || 30);

    const labels = Array.isArray(trendData.labels) ? trendData.labels : [];
    const totalValues = Array.isArray(trendData.values) ? trendData.values : [];
    const widgetSeries = Array.isArray(trendData.widgetSeries) ? trendData.widgetSeries : [];

    const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b', '#0ea5e9'];

    const daySelect = document.getElementById('trendDaysSelect');
    if (daySelect) {
        daySelect.value = String(selectedDays);
        daySelect.addEventListener('change', () => {
            const days = daySelect.value || '30';
            window.location.href = `${contextPath}/dashboard/trends?days=${encodeURIComponent(days)}`;
        });
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
                    pointRadius: 3
                }]
            },
            options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true } } }
        });
    }

    const widgetContainer = document.getElementById('widgetTrendCharts');
    if (!widgetContainer) return;

    if (!widgetSeries.length) {
        widgetContainer.innerHTML = '<p class="helper-note">No widget trend data available for this period.</p>';
        return;
    }

    widgetSeries.forEach((series, index) => {
        const card = document.createElement('section');
        card.className = 'chart-wrapper';
        card.style.marginTop = '20px';

        const title = document.createElement('h3');
        title.textContent = `${series.name || `Widget ${index + 1}`} — Entries per day`;
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
                    label: series.name || `Widget ${index + 1}`,
                    data: Array.isArray(series.values) ? series.values : [],
                    borderColor: palette[index % palette.length],
                    backgroundColor: 'rgba(99, 102, 241, 0.10)',
                    fill: true,
                    tension: 0.25,
                    borderWidth: 2,
                    pointRadius: 2
                }]
            },
            options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true } } }
        });
    });
})();
