// widget_review_util/render_util.js

export function renderLoading(el, text = "Loading...") {
    if (!el) {
        return;
    }
    el.innerHTML = `<div class="wr-loading">${escapeHtml(text)}</div>`;
}

export function renderError(el, message, requestId = "") {
    if (!el) {
        return;
    }
    const rid = requestId ? `<div class="wr-request-id">Request ID: ${escapeHtml(requestId)}</div>` : "";
    el.innerHTML = `
    <div class="wr-error">
      <div class="wr-error-title">Something went wrong</div>
      <div class="wr-error-message">${escapeHtml(message || "Unexpected error")}</div>
      ${rid}
    </div>
  `;
}

/**
 * Hardened markdown rendering:
 * 1) marked.parse(md)
 * 2) DOMPurify.sanitize(html)
 * 3) el.innerHTML = sanitized
 */
export function renderMarkdown(el, md) {
    if (!el) {
        return;
    }

    const markdown = (md ?? "").toString();

    try {
        const markedApi = globalThis.marked;
        const purifier = globalThis.DOMPurify;

        // Safe fallback if libraries are unavailable
        if (!markedApi || typeof markedApi.parse !== "function" || !purifier || typeof purifier.sanitize !== "function") {
            el.innerHTML = `<pre class="wr-markdown">${escapeHtml(markdown)}</pre>`;
            return;
        }

        if (typeof markedApi.setOptions === "function") {
            markedApi.setOptions({
                gfm: true,
                breaks: true,
                mangle: false,
                headerIds: false
            });
        }

        const html = markedApi.parse(markdown || "");
        const sanitized = purifier.sanitize(html, {
            USE_PROFILES: { html: true },
            FORBID_TAGS: ["style", "script", "iframe", "object", "embed", "form"],
            FORBID_ATTR: ["onerror", "onload", "onclick", "onmouseover"]
        });

        el.innerHTML = sanitized || `<pre class="wr-markdown"></pre>`;
    } catch {
        // Final safe fallback
        el.innerHTML = `<pre class="wr-markdown">${escapeHtml(markdown)}</pre>`;
    }
}

export function renderStatusPill(el, text, tone = "neutral") {
    if (!el) {
        return;
    }
    el.innerHTML = `<span class="wr-pill wr-pill-${escapeHtml(tone)}">${escapeHtml(text || "")}</span>`;
}

function escapeHtml(s) {
    return (s ?? "")
        .toString()
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
