// markdown.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    const Markdown = {
        renderMarkdownSafe(markdown, containerId) {
            const el = document.getElementById(containerId);
            if (!el) return;
            const md = markdown == null ? '' : String(markdown);
            try {
                const rawHtml = (typeof marked === 'function') ? marked(md) : window.AdminPage.Utils.escapeHtml(md).replace(/\n/g, '<br/>');
                const safeHtml = (typeof DOMPurify !== 'undefined') ? DOMPurify.sanitize(rawHtml) : rawHtml;
                el.innerHTML = safeHtml;
            } catch (e) {
                el.textContent = markdown;
            }
        },

        safeFetchAndRenderResponse(url, containerId, fetchOptions) {
            fetchOptions = fetchOptions || { method: 'GET', headers: { 'Accept': 'application/json' } };
            fetch(url, fetchOptions)
                .then(resp => resp.json())
                .then(payload => {
                    if (!payload) return;
                    if (payload.response_html) {
                        const el = document.getElementById(containerId);
                        if (!el) return;
                        const safe = (typeof DOMPurify !== 'undefined') ? DOMPurify.sanitize(payload.response_html) : payload.response_html;
                        el.innerHTML = safe;
                        return;
                    }
                    const md = payload.response_text || payload.responseText || payload.text || '';
                    Markdown.renderMarkdownSafe(md, containerId);
                })
                .catch(err => console.warn('Failed to fetch/render response:', err));
        }
    };

    window.AdminPage.Markdown = Markdown;
})();
