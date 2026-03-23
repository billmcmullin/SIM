(function () {
    const cfg = window.dashboardTopicsConfig || {};
    const contextPath = cfg.contextPath || '';
    const API_URL = `${contextPath}/dashboard/topics/data`;
    const REVIEW_START_URL = `${contextPath}/dashboard/topics/select`;

    const searchInput = document.getElementById('topicSearchInput');
    const limitSelect = document.getElementById('topicLimitSelect');
    const refreshBtn = document.getElementById('topicRefreshBtn');

    const globalBody = document.getElementById('globalTopicsBody');
    const widgetContainer = document.getElementById('perWidgetTopicsContainer');
    const globalPieCanvas = document.getElementById('globalTopicsPieChart');

    const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b', '#0ea5e9', '#16a34a', '#be185d'];

    let globalPieChart = null;
    const widgetPieCharts = new Map();

    function esc(v) {
        if (v == null) return '';
        return String(v)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function makeWidgetKey(name) {
        return String(name || 'widget').replaceAll(/[^a-zA-Z0-9_-]/g, '_');
    }

    async function openReviewForChatIds(chatIds) {
        if (!Array.isArray(chatIds) || !chatIds.length) return;
        try {
            const res = await fetch(REVIEW_START_URL, {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ selectedChatIds: chatIds })
            });

            const text = await res.text();
            let data = {};
            try { data = JSON.parse(text); } catch { data = { message: text }; }

            if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`);
            if (!data.selectionId) throw new Error(data.message || 'No selectionId returned');

            window.location.href = `${contextPath}/dashboard/widgets/drilldown/review?selectionId=${encodeURIComponent(data.selectionId)}`;
        } catch (err) {
            console.error(err);
            alert(`Unable to open review: ${err.message}`);
        }
    }

    function setLoading() {
        if (globalBody) globalBody.innerHTML = '<tr><td colspan="3" class="empty-row">Loading topics…</td></tr>';
        if (widgetContainer) widgetContainer.innerHTML = '<p class="helper-note">Loading per-widget topics…</p>';
    }

    function mentionButton(mentions, chatIds) {
        const idsJson = esc(JSON.stringify(Array.isArray(chatIds) ? chatIds : []));
        return `<button type="button" class="ghost-btn small topic-mentions-btn" data-chat-ids='${idsJson}'>${mentions ?? 0}</button>`;
    }

    function renderGlobal(rows) {
        if (!globalBody) return;
        if (!Array.isArray(rows) || !rows.length) {
            globalBody.innerHTML = '<tr><td colspan="3" class="empty-row">No topics found.</td></tr>';
            return;
        }
        globalBody.innerHTML = rows.map(r => `
            <tr>
                <td>${r.rank ?? ''}</td>
                <td>${esc(r.topic)}</td>
                <td>${mentionButton(r.mentions, r.selectedChatIds)}</td>
            </tr>
        `).join('');
    }

    function renderWidgets(widgets) {
        if (!widgetContainer) return;
        if (!Array.isArray(widgets) || !widgets.length) {
            widgetContainer.innerHTML = '<p class="helper-note">No widget topic data found.</p>';
            return;
        }

        widgetContainer.innerHTML = widgets.map(w => {
            const widgetName = w.widgetName || 'Widget';
            const key = makeWidgetKey(widgetName);
            const topics = Array.isArray(w.topics) ? w.topics : [];

            const rows = topics.length ? topics.map(t => `
                <tr>
                    <td>${t.rank ?? ''}</td>
                    <td>${esc(t.topic)}</td>
                    <td>${mentionButton(t.mentions, t.selectedChatIds)}</td>
                </tr>
            `).join('') : '<tr><td colspan="3" class="empty-row">No topics found.</td></tr>';

            return `
                <section class="section">
                    <h3>${esc(widgetName)}</h3>
                    <div style="display:flex; gap:20px; align-items:flex-start; flex-wrap:wrap;">
                        <div style="flex:1 1 520px; min-width:420px;">
                            <div class="table-scroll">
                                <table class="widget-table">
                                    <thead><tr><th>Rank</th><th>Topic</th><th>Mentions</th></tr></thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            </div>
                        </div>
                        <div style="flex:0 0 320px; width:320px; max-width:100%;">
                            <div class="chart-area" style="height:280px; margin:0;">
                                <canvas id="widgetTopicsPieChart_${key}"></canvas>
                            </div>
                        </div>
                    </div>
                </section>
            `;
        }).join('');
    }

    function destroyCharts() {
        if (globalPieChart) {
            globalPieChart.destroy();
            globalPieChart = null;
        }
        widgetPieCharts.forEach(chart => chart.destroy());
        widgetPieCharts.clear();
    }

    function renderGlobalPie(globalRows) {
        if (!globalPieCanvas || typeof Chart === 'undefined') return;

        const rows = Array.isArray(globalRows) ? globalRows : [];
        const labels = rows.map(r => r.topic || 'Topic');
        const values = rows.map(r => Number(r.mentions || 0));

        if (globalPieChart) {
            globalPieChart.destroy();
            globalPieChart = null;
        }

        if (!labels.length) return;

        globalPieChart = new Chart(globalPieCanvas.getContext('2d'), {
            type: 'pie',
            data: {
                labels,
                datasets: [{
                    data: values,
                    backgroundColor: labels.map((_, i) => palette[i % palette.length])
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } },
                onClick: (evt, elements) => {
                    if (!elements || !elements.length) return;
                    const idx = elements[0].index;
                    const row = rows[idx];
                    if (!row) return;
                    openReviewForChatIds(row.selectedChatIds || []);
                }
            }
        });
    }

    function renderWidgetPies(widgets) {
        if (typeof Chart === 'undefined') return;
        const list = Array.isArray(widgets) ? widgets : [];

        list.forEach(w => {
            const widgetName = w.widgetName || 'Widget';
            const key = makeWidgetKey(widgetName);
            const canvas = document.getElementById(`widgetTopicsPieChart_${key}`);
            if (!canvas) return;

            const topics = Array.isArray(w.topics) ? w.topics : [];
            const labels = topics.map(t => t.topic || 'Topic');
            const values = topics.map(t => Number(t.mentions || 0));

            if (!labels.length) return;

            const chart = new Chart(canvas.getContext('2d'), {
                type: 'pie',
                data: {
                    labels,
                    datasets: [{
                        data: values,
                        backgroundColor: labels.map((_, i) => palette[i % palette.length])
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom' } },
                    onClick: (evt, elements) => {
                        if (!elements || !elements.length) return;
                        const idx = elements[0].index;
                        const topic = topics[idx];
                        if (!topic) return;
                        openReviewForChatIds(topic.selectedChatIds || []);
                    }
                }
            });

            widgetPieCharts.set(key, chart);
        });
    }

    function wireMentionButtons() {
        document.querySelectorAll('.topic-mentions-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                let ids = [];
                try { ids = JSON.parse(btn.dataset.chatIds || '[]'); } catch { ids = []; }
                openReviewForChatIds(ids);
            });
        });
    }

    async function loadTopics() {
        setLoading();
        destroyCharts();

        const params = new URLSearchParams();
        const q = (searchInput?.value || '').trim();
        const selectedLimit = (limitSelect?.value || '5').trim();

        if (q) params.set('q', q);
        if (selectedLimit) params.set('limit', selectedLimit);

        try {
            const res = await fetch(`${API_URL}?${params.toString()}`, {
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            if (!data || data.status !== 'ok') throw new Error(data?.message || 'Unexpected response');

            const globalTopics = Array.isArray(data.globalTopics) ? data.globalTopics : [];
            const widgets = Array.isArray(data.widgets) ? data.widgets : [];

            renderGlobal(globalTopics);
            renderWidgets(widgets);
            wireMentionButtons();

            // pie charts use same filtered/limited data as tables
            renderGlobalPie(globalTopics);
            renderWidgetPies(widgets);
        } catch (err) {
            console.error(err);
            if (globalBody) globalBody.innerHTML = `<tr><td colspan="3" class="empty-row">Unable to load topics: ${esc(err.message)}</td></tr>`;
            if (widgetContainer) widgetContainer.innerHTML = `<p class="helper-note">Unable to load per-widget topics: ${esc(err.message)}</p>`;
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        if (limitSelect && !limitSelect.value) limitSelect.value = '5';

        searchInput?.addEventListener('input', loadTopics);
        searchInput?.addEventListener('keydown', e => {
            if (e.key === 'Enter') { e.preventDefault(); loadTopics(); }
        });
        limitSelect?.addEventListener('change', loadTopics);
        refreshBtn?.addEventListener('click', () => {
            if (searchInput) searchInput.value = '';
            if (limitSelect) limitSelect.value = '5';
            loadTopics();
        });

        loadTopics();
    });
})();
