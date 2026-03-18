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

    function esc(v) {
        if (v == null) return '';
        return String(v)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
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
                    <div class="table-scroll">
                        <table class="widget-table">
                            <thead><tr><th>Rank</th><th>Topic</th><th>Mentions</th></tr></thead>
                            <tbody>${rows}</tbody>
                        </table>
                    </div>
                </section>
            `;
        }).join('');
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

        const params = new URLSearchParams();
        const q = (searchInput?.value || '').trim();
        const selectedLimit = (limitSelect?.value || '5').trim();

        if (q) params.set('q', q);
        if (selectedLimit) params.set('limit', selectedLimit); // can be 5/10/20/all

        try {
            const res = await fetch(`${API_URL}?${params.toString()}`, {
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            if (!data || data.status !== 'ok') throw new Error(data?.message || 'Unexpected response');

            renderGlobal(data.globalTopics || []);
            renderWidgets(data.widgets || []);
            wireMentionButtons();
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
