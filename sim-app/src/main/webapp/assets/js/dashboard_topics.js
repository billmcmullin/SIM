(function () {
    const cfg = window.dashboardTopicsConfig || {};
    const contextPath = cfg.contextPath || '';
    const API_URL = `${contextPath}/dashboard/topics/data`;
    const REVIEW_START_URL = `${contextPath}/dashboard/topics/select`;

    const refreshBtn = document.getElementById('topicRefreshBtn');

    const dayInput = document.getElementById('topicDayInput');
    const startInput = document.getElementById('topicStartDateInput');
    const endInput = document.getElementById('topicEndDateInput');
    const useRangeCheckbox = document.getElementById('topicUseRange');

    const includeOtherBtn = document.getElementById('topicIncludeOtherBtn');
    let includeOtherEnabled = false;

    const summaryDayEl = document.getElementById('topicsSummaryDay');
    const summaryRangeEl = document.getElementById('topicsSummaryRange');
    const summaryMentionsEl = document.getElementById('topicsSummaryMentions');
    const summaryChatsEl = document.getElementById('topicsSummaryUniqueChats');

    const globalBody = document.getElementById('globalTopicsBody');
    const widgetContainer = document.getElementById('perWidgetTopicsContainer');
    const globalPieCanvas = document.getElementById('globalTopicsPieChart');

    const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b', '#0ea5e9', '#16a34a', '#be185d'];

    let globalPieChart = null;
    const widgetPieCharts = new Map();

    function esc(v) {
        if (v === null || v === undefined) return '';
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

    function toYmd(d) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
    }

    function ensureDefaultDateInputs() {
        const today = toYmd(new Date());
        if (dayInput && !dayInput.value) dayInput.value = today;
        if (startInput && !startInput.value) startInput.value = today;
        if (endInput && !endInput.value) endInput.value = today;
    }

    function applyIncludeOtherBtnState() {
        if (!includeOtherBtn) return;
        includeOtherBtn.textContent = includeOtherEnabled
            ? 'Including "Other Parasoft Match"'
            : 'Include "Other Parasoft Match"';
        includeOtherBtn.classList.toggle('is-active', includeOtherEnabled);
        includeOtherBtn.setAttribute('aria-pressed', includeOtherEnabled ? 'true' : 'false');
    }

    function hydrateControlsFromUrl() {
        const qp = new URLSearchParams(window.location.search || '');
        const day = (qp.get('day') || '').trim();
        const start = (qp.get('start') || '').trim();
        const end = (qp.get('end') || '').trim();
        const includeOther = (qp.get('includeOther') || '').trim().toLowerCase();

        if (dayInput && day) dayInput.value = day;
        if (startInput && start) startInput.value = start;
        if (endInput && end) endInput.value = end;

        if (useRangeCheckbox) {
            useRangeCheckbox.checked = !!(start && end);
        }

        includeOtherEnabled = includeOther === '1' || includeOther === 'true' || includeOther === 'yes' || includeOther === 'on';
        applyIncludeOtherBtnState();
    }

    function updateUrlFromControls() {
        const params = new URLSearchParams();
        const day = (dayInput?.value || '').trim();
        const start = (startInput?.value || '').trim();
        const end = (endInput?.value || '').trim();
        const useRange = !!(useRangeCheckbox && useRangeCheckbox.checked);

        // default always all topics
        params.set('limit', 'all');

        if (includeOtherEnabled) params.set('includeOther', 'true');

        if (useRange) {
            if (start) params.set('start', start);
            if (end) params.set('end', end);
            if (!start && !end && day) params.set('day', day);
        } else {
            if (day) params.set('day', day);
            else {
                if (start) params.set('start', start);
                if (end) params.set('end', end);
            }
        }

        const qs = params.toString();
        const newUrl = `${window.location.pathname}${qs ? `?${qs}` : ''}${window.location.hash || ''}`;
        window.history.replaceState({}, '', newUrl);
    }

    function getDateParams() {
        const params = new URLSearchParams();

        const useRange = !!(useRangeCheckbox && useRangeCheckbox.checked);
        const day = (dayInput?.value || '').trim();
        const start = (startInput?.value || '').trim();
        const end = (endInput?.value || '').trim();

        if (useRange) {
            if (start) params.set('start', start);
            if (end) params.set('end', end);
            if (!start && !end && day) params.set('day', day);
        } else {
            if (day) params.set('day', day);
            else if (start || end) {
                if (start) params.set('start', start);
                if (end) params.set('end', end);
            }
        }

        return params;
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
        if (globalPieChart) { globalPieChart.destroy(); globalPieChart = null; }
        widgetPieCharts.forEach(chart => chart.destroy());
        widgetPieCharts.clear();
    }

    function renderGlobalPie(globalRows) {
        if (!globalPieCanvas || typeof Chart === 'undefined') return;

        const rows = Array.isArray(globalRows) ? globalRows : [];
        const labels = rows.map(r => r.topic || 'Topic');
        const values = rows.map(r => Number(r.mentions || 0));

        if (globalPieChart) { globalPieChart.destroy(); globalPieChart = null; }
        if (!labels.length) return;

        globalPieChart = new Chart(globalPieCanvas.getContext('2d'), {
            type: 'pie',
            data: { labels, datasets: [{ data: values, backgroundColor: labels.map((_, i) => palette[i % palette.length]) }] },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } },
                onClick: (_evt, elements) => {
                    if (!elements?.length) return;
                    const row = rows[elements[0].index];
                    if (row) openReviewForChatIds(row.selectedChatIds || []);
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
                data: { labels, datasets: [{ data: values, backgroundColor: labels.map((_, i) => palette[i % palette.length]) }] },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom' } },
                    onClick: (_evt, elements) => {
                        if (!elements?.length) return;
                        const topic = topics[elements[0].index];
                        if (topic) openReviewForChatIds(topic.selectedChatIds || []);
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

    function updateSummary(data) {
        if (!data || typeof data !== 'object') return;
        if (summaryDayEl) summaryDayEl.textContent = data.day || '—';
        if (summaryRangeEl) summaryRangeEl.textContent = `${data.rangeStart || '—'} to ${data.rangeEnd || '—'}`;
        if (summaryMentionsEl) summaryMentionsEl.textContent = String(Number.isFinite(Number(data.termsTotal)) ? Number(data.termsTotal) : 0);
        if (summaryChatsEl) summaryChatsEl.textContent = String(Number.isFinite(Number(data.uniqueChatsTotal)) ? Number(data.uniqueChatsTotal) : 0);
    }

    async function loadTopics() {
        setLoading();
        destroyCharts();
        updateUrlFromControls();

        const params = getDateParams();
        params.set('limit', 'all'); // default always all topics
        if (includeOtherEnabled) params.set('includeOther', 'true');

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
            updateSummary(data);

            renderGlobalPie(globalTopics);
            renderWidgetPies(widgets);
        } catch (err) {
            console.error(err);
            if (globalBody) globalBody.innerHTML = `<tr><td colspan="3" class="empty-row">Unable to load topics: ${esc(err.message)}</td></tr>`;
            if (widgetContainer) widgetContainer.innerHTML = `<p class="helper-note">Unable to load per-widget topics: ${esc(err.message)}</p>`;
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        hydrateControlsFromUrl();
        ensureDefaultDateInputs();

        includeOtherBtn?.addEventListener('click', () => {
            includeOtherEnabled = !includeOtherEnabled;
            applyIncludeOtherBtnState();
            loadTopics();
        });

        dayInput?.addEventListener('change', () => {
            if (useRangeCheckbox) useRangeCheckbox.checked = false;
            loadTopics();
        });

        startInput?.addEventListener('change', () => {
            if (useRangeCheckbox) useRangeCheckbox.checked = true;
            loadTopics();
        });

        endInput?.addEventListener('change', () => {
            if (useRangeCheckbox) useRangeCheckbox.checked = true;
            loadTopics();
        });

        useRangeCheckbox?.addEventListener('change', loadTopics);

        refreshBtn?.addEventListener('click', () => {
            const today = toYmd(new Date());
            if (dayInput) dayInput.value = today;
            if (startInput) startInput.value = today;
            if (endInput) endInput.value = today;
            if (useRangeCheckbox) useRangeCheckbox.checked = false;

            includeOtherEnabled = false;
            applyIncludeOtherBtnState();

            loadTopics();
        });

        applyIncludeOtherBtnState();
        loadTopics();
    });
})();
