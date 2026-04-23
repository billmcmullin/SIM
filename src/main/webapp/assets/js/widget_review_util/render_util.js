// widget_review_util/render_util.js

export function renderLoading(el, text = "Loading...") {
    if (!el) return;
    el.innerHTML = `<div class="wr-loading">${escapeHtml(text)}</div>`;
}

export function renderError(el, message, requestId = "") {
    if (!el) return;
    const rid = requestId ? `<div class="wr-request-id">Request ID: ${escapeHtml(requestId)}</div>` : "";
    el.innerHTML = `
    <div class="wr-error">
      <div class="wr-error-title">Something went wrong</div>
      <div class="wr-error-message">${escapeHtml(message || "Unexpected error")}</div>
      ${rid}
    </div>
  `;
}

export function renderMarkdown(el, md) {
    if (!el) return;
    // If you use a markdown lib, plug it in here. For now plain preformatted fallback:
    el.innerHTML = `<pre class="wr-markdown">${escapeHtml(md || "")}</pre>`;
}

export function renderStatusPill(el, text, tone = "neutral") {
    if (!el) return;
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
