#!/usr/bin/env python3
import argparse
import csv
import json
import pathlib
import re
from collections import Counter, defaultdict
from datetime import datetime, timezone
from html import escape
from typing import Iterable, List, Dict, Any, Tuple

DEFAULT_TERM_NAME = "No Match"
MULTI_TERM_NAME = "Multi"

HTML_TEMPLATE = """
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Chat Report</title>
<style>
  body {{ font-family: "Segoe UI", sans-serif; margin:0; padding:0; background:#f7fafc; color:#111; }}
  header {{ background:#111827; color:#fff; padding:24px; }}
  header h1 {{ margin:0 0 8px; }}
  header p {{ margin:0; opacity:.8; }}
  main {{ padding:24px; }}
  .section {{ background:#fff; border-radius:12px; padding:20px; margin-bottom:20px; box-shadow:0 10px 40px rgba(15,23,42,.08); }}
  .section h2 {{ margin-top:0; }}
  .section h3 {{ margin-top:16px; margin-bottom:8px; font-size:1.1rem; }}
  .metrics-grid {{ display:grid; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); gap:16px; }}
  .metric-card {{ background:#eef2ff; padding:14px; border-radius:10px; }}
  .metric-card strong {{ display:block; font-size:1.5rem; }}
  .filters {{ display:flex; flex-wrap:wrap; gap:12px; margin-bottom:12px; }}
  .filters input, .filters button, .pagination-controls button {{ padding:10px 14px; border-radius:8px; border:1px solid #cbd5f5; font-size:1rem; background:#fff; cursor:pointer; }}
  .filters button:disabled, .pagination-controls button:disabled {{ opacity:.6; cursor:not-allowed; }}
  .pagination-controls {{ display:flex; align-items:center; gap:10px; margin-bottom:16px; }}
  .pagination-controls input {{ width:70px; }}
  .chat-meta {{ display:flex; flex-wrap:wrap; gap:12px; margin-bottom:16px; font-size:.95rem; color:#475569; }}
  #searchInput {{ flex:1; min-width:240px; }}
  #chatContainer {{ display:grid; grid-template-columns:repeat(auto-fit,minmax(320px,1fr)); gap:16px; }}
  .chat-card {{ background:#fff; padding:16px; border-radius:12px; border:1px solid #e5e7eb; display:none; }}
  .chat-card h3 {{ margin:0 0 10px; font-size:1.2rem; }}
  .chat-card p {{ margin:6px 0; line-height:1.4; }}
  .source-list {{ list-style:none; padding:0; margin:0; }}
  .source-list li {{ margin-bottom:12px; font-size:.95rem; }}
  .badge {{ display:inline-block; padding:3px 10px; background:#4338ca; color:#fff; border-radius:999px; font-size:.75rem; margin:4px 6px 4px 0; }}
  footer {{ text-align:center; padding:24px; font-size:.85rem; color:#475569; }}
  .chat-card .source-details {{ font-size:.85rem; color:#475569; margin-top:4px; display:block; }}
  .search-hit {{ background:#fef3c7; }}
  #noResults {{ color:#dc2626; font-weight:600; text-align:center; margin-top:12px; display:none; }}
  .widget-grid {{ display:grid; grid-template-columns:repeat(auto-fit,minmax(240px,1fr)); gap:16px; }}
  .widget-card {{ background:#fff; padding:16px; border-radius:12px; border:1px solid #e5e7eb; position:relative; transition:box-shadow .2s ease, border-color .2s ease; }}
  .widget-card.highlight {{ box-shadow:0 0 0 3px rgba(59,130,246,.5); border-color:#4338ca; }}
  .widget-card h3 {{ margin:0 0 6px; font-size:1.05rem; }}
  .widget-badges {{ display:flex; flex-wrap:wrap; gap:6px; margin-top:8px; }}
  .widget-badge-tooltip {{ margin-top:6px; font-size:.8rem; color:#334155; min-height:1.2em; }}
  .pie-container {{ text-align:center; margin-top:12px; display:flex; flex-direction:column; align-items:center; gap:8px; }}
  #topicPieChart {{ max-width:none; width:460px; height:300px; }}
  .pie-legend {{ display:flex; flex-wrap:wrap; gap:6px; justify-content:center; margin-top:6px; }}
  .pie-tooltip {{ margin-top:8px; font-weight:600; color:#202733; min-height:1.2em; }}
  .session-list {{ padding-left:1.25rem; margin:0; }}
  .session-list li {{ margin-bottom:6px; font-size:.95rem; }}
  .widget-pie-chart {{ width:200px; height:200px; max-width:100%; margin-top:10px; }}
</style>
</head>
<body>
<header>
  <h1>Chat Report</h1>
  <p>Generated on {timestamp} — {chat_count} chat(s).</p>
</header>
<main>
  <section class="section" id="overview">
    <h2>Keyword Metrics</h2>
    <div class="metrics-grid">
      <div class="metric-card">
        <strong>{chat_count}</strong>
        Chats processed
      </div>
      <div class="metric-card">
        <strong>{term_total}</strong>
        Keyword mentions
      </div>
    </div>
    <h3>Topics</h3>
    <div class="metrics-grid">
      {first_topic_section}
    </div>
    <div class="pie-container">
      <canvas id="topicPieChart" width="460" height="300"></canvas>
      <div id="pieTooltip" class="pie-tooltip"></div>
      <div id="topicPieLegend" class="pie-legend"></div>
    </div>
  </section>

  <section class="section" id="widgets">
    <h2>Widgets</h2>
    <div class="widget-grid">
      {widget_cards}
    </div>
  </section>

  <section class="section" id="sessions">
    <h2>Sessions</h2>
    <div class="metrics-grid">
      <div class="metric-card">
        <strong>{unique_sessions}</strong>
        Unique user sessions
      </div>
    </div>
    <h3>Top 10 Sessions</h3>
    <ol class="session-list">
      {top_sessions}
    </ol>
  </section>

  <div class="filters">
    <input type="text" id="searchInput" placeholder="Search chats by text, prompt, session, or source..." />
    <button onclick="resetSearch()">Clear</button>
  </div>

  <section class="section" id="chats">
    <h2>Chats</h2>
    <div class="chat-meta">
      <span>Showing <strong id="displayedCount">{chat_count}</strong> of <strong id="totalChats">{chat_count}</strong> chats.</span>
      <span id="searchSummary" style="display:none;">Matches for "<span id="activeSearch"></span>": <strong id="searchMatchesValue">0</strong></span>
    </div>
    <div class="pagination-controls">
      <button id="prevPage" onclick="changePage(-1)">Previous</button>
      <span id="pageInfo">0 / 0</span>
      <button id="nextPage" onclick="changePage(1)">Next</button>
      <div>
        <input type="number" id="pageInput" min="1" placeholder="Page" />
        <button onclick="goToPage()">Go</button>
      </div>
    </div>
    <div id="noResults">No chats match your search criteria.</div>
    <div id="chatContainer">
      {chat_cards}
    </div>
  </section>
</main>
<footer>
  <p>Report generated by chat_report.py</p>
</footer>

<script>
const searchInput = document.getElementById("searchInput");
const cards = Array.from(document.querySelectorAll(".chat-card"));
const pageSize = {page_size};
const displayedCountEl = document.getElementById("displayedCount");
const totalChatsEl = document.getElementById("totalChats");
const searchSummaryEl = document.getElementById("searchSummary");
const searchMatchesValueEl = document.getElementById("searchMatchesValue");
const activeSearchEl = document.getElementById("activeSearch");
const pageInput = document.getElementById("pageInput");
const canvas = document.getElementById("topicPieChart");
const legend = document.getElementById("topicPieLegend");
const tooltipEl = document.getElementById("pieTooltip");
const topicColors = ["#4338ca", "#6366f1", "#10b981", "#f97316", "#ef4444", "#0ea5e9", "#8b5cf6", "#f59e0b"];
let filteredCards = cards.slice();
let currentPage = Math.max(1, {initial_page});
let topicPieSegments = [];
let pieRadius = 0;
let pieCenter = {{ x: 0, y: 0 }};
let currentHighlightIndex = -1;

function updateChatSummary() {{
  displayedCountEl.textContent = filteredCards.length;
  totalChatsEl.textContent = cards.length;
  const term = searchInput.value.trim();
  if (term) {{
    activeSearchEl.textContent = term;
    searchMatchesValueEl.textContent = filteredCards.length;
    searchSummaryEl.style.display = "inline-block";
  }} else {{
    searchSummaryEl.style.display = "none";
  }}
}}

function renderPage() {{
  const totalPages = Math.max(1, Math.ceil(filteredCards.length / pageSize));
  if (currentPage > totalPages) {{
    currentPage = totalPages;
  }}
  if (currentPage < 1) {{
    currentPage = 1;
  }}
  cards.forEach(card => card.style.display = "none");
  const start = (currentPage - 1) * pageSize;
  const end = start + pageSize;
  filteredCards.slice(start, end).forEach(card => card.style.display = "block");
  document.getElementById("pageInfo").textContent = `${{currentPage}} / ${{totalPages}}`;
  document.getElementById("prevPage").disabled = currentPage === 1;
  document.getElementById("nextPage").disabled = currentPage === totalPages;
  if (pageInput) {{
    pageInput.value = currentPage;
    pageInput.max = totalPages;
  }}
  document.getElementById("noResults").style.display = filteredCards.length === 0 ? "block" : "none";
  updateChatSummary();
}}

function filterChats(query) {{
  const value = query.trim().toLowerCase();
  filteredCards = cards.filter(card => card.getAttribute("data-search").includes(value));
  currentPage = 1;
  applySearchHighlights(value);
  renderPage();
}}

function applySearchHighlights(value) {{
  cards.forEach(card => {{
    card.classList.toggle("search-hit", value && card.getAttribute("data-search").includes(value));
  }});
}}

function resetSearch() {{
  searchInput.value = "";
  filterChats("");
}}

function changePage(delta) {{
  const totalPages = Math.max(1, Math.ceil(filteredCards.length / pageSize));
  currentPage = Math.min(Math.max(1, currentPage + delta), totalPages);
  renderPage();
}}

function goToPage() {{
  if (!pageInput) {{
    return;
  }}
  let target = parseInt(pageInput.value, 10);
  if (Number.isNaN(target)) {{
    return;
  }}
  currentPage = Math.max(1, target);
  renderPage();
}}

const topicPieData = {topic_pie_data};
const widgetPieConfigs = {widget_pie_configs};

function setPieTooltip(index) {{
  if (!tooltipEl) {{
    return;
  }}
  if (index >= 0 && topicPieData[index]) {{
    tooltipEl.textContent = topicPieData[index].label;
  }} else {{
    tooltipEl.textContent = "";
  }}
}}

function drawTopicPieChart(highlightIndex = -1) {{
  if (!canvas || !topicPieData.length) {{
    if (canvas) canvas.style.display = "none";
    if (legend) legend.style.display = "none";
    if (tooltipEl) tooltipEl.textContent = "";
    return;
  }}
  const ctx = canvas.getContext("2d");
  const width = canvas.width;
  const height = canvas.height;
  ctx.clearRect(0, 0, width, height);
  const total = topicPieData.reduce((sum, item) => sum + item.value, 0);
  if (!total) {{
    canvas.style.display = "none";
    if (legend) legend.style.display = "none";
    if (tooltipEl) tooltipEl.textContent = "";
    return;
  }}
  canvas.style.display = "block";
  if (legend) legend.style.display = "flex";
  const radiusBase = Math.min(width, height) / 2 - 12;
  pieRadius = radiusBase + 12;
  pieCenter = {{ x: width / 2, y: height / 2 }};
  let startAngle = 0;
  topicPieSegments = [];
  topicPieData.forEach((segment, index) => {{
    const slice = (segment.value / total) * Math.PI * 2;
    const endAngle = startAngle + slice;
    const isHighlighted = highlightIndex === index;
    const radius = radiusBase + (isHighlighted ? 10 : 0);
    ctx.beginPath();
    ctx.moveTo(pieCenter.x, pieCenter.y);
    ctx.arc(pieCenter.x, pieCenter.y, radius, startAngle, endAngle);
    ctx.closePath();
    ctx.fillStyle = topicColors[index % topicColors.length];
    ctx.fill();
    if (isHighlighted) {{
      ctx.lineWidth = 3;
      ctx.strokeStyle = "#fff";
      ctx.stroke();
    }}
    topicPieSegments.push({{ start: startAngle, end: endAngle }});
    startAngle = endAngle;
  }});
  if (legend) {{
    legend.innerHTML = topicPieData
      .map((segment, index) => `<span class="badge" style="background:${{topicColors[index % topicColors.length]}}">${{segment.label}}: ${{segment.value}}</span>`)
      .join("");
  }}
  setPieTooltip(highlightIndex);
}}

function handlePieHover(event) {{
  if (!topicPieSegments.length) {{
    return;
  }}
  const rect = canvas.getBoundingClientRect();
  const x = event.clientX - rect.left - pieCenter.x;
  const y = event.clientY - rect.top - pieCenter.y;
  const dist = Math.sqrt(x * x + y * y);
  if (dist > pieRadius) {{
    if (currentHighlightIndex !== -1) {{
      currentHighlightIndex = -1;
      drawTopicPieChart();
    }}
    setPieTooltip(-1);
    return;
  }}
  let angle = Math.atan2(y, x);
  if (angle < 0) {{
    angle += Math.PI * 2;
  }}
  const hitIndex = topicPieSegments.findIndex(segment => angle >= segment.start && angle < segment.end);
  if (hitIndex !== currentHighlightIndex) {{
    currentHighlightIndex = hitIndex;
    drawTopicPieChart(currentHighlightIndex);
  }}
}}

function drawWidgetPieChart(canvas, data, config) {{
  if (!canvas || !data.length) {{
    return;
  }}
  const ctx = canvas.getContext("2d");
  const width = canvas.width;
  const height = canvas.height;
  ctx.clearRect(0, 0, width, height);
  const total = data.reduce((sum, item) => sum + item.value, 0);
  if (!total) {{
    config.segments = [];
    return;
  }}
  const radius = Math.min(width, height) / 2 - 5;
  const centerX = width / 2;
  const centerY = height / 2;
  let startAngle = 0;
  const segments = [];
  data.forEach((segment, index) => {{
    const slice = (segment.value / total) * Math.PI * 2;
    const endAngle = startAngle + slice;
    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.arc(centerX, centerY, radius, startAngle, endAngle);
    ctx.closePath();
    ctx.fillStyle = topicColors[index % topicColors.length];
    ctx.fill();
    segments.push({{ start: startAngle, end: endAngle, term: segment.label }});
    startAngle = endAngle;
  }});
  config.segments = segments;
}}

function updateWidgetHighlight(config, index) {{
  const canvas = document.getElementById(config.id);
  if (!canvas) {{
    return;
  }}
  const card = canvas.closest(".widget-card");
  const tooltip = document.getElementById(config.tooltipId);
  if (index >= 0 && config.segments[index]) {{
    card?.classList.add("highlight");
    const term = config.segments[index].term;
    if (tooltip) {{
      tooltip.textContent = term;
    }}
  }} else {{
    card?.classList.remove("highlight");
    if (tooltip) tooltip.textContent = "";
  }}
}}

function handleWidgetPieHover(event, config) {{
  const canvas = document.getElementById(config.id);
  if (!canvas || !config.segments) {{
    return;
  }}
  const rect = canvas.getBoundingClientRect();
  const x = event.clientX - rect.left - rect.width / 2;
  const y = event.clientY - rect.top - rect.height / 2;
  const dist = Math.sqrt(x * x + y * y);
  const radius = Math.min(rect.width, rect.height) / 2 - 5;
  if (dist > radius) {{
    updateWidgetHighlight(config, -1);
    return;
  }}
  let angle = Math.atan2(y, x);
  if (angle < 0) {{
    angle += Math.PI * 2;
  }}
  const segmentIndex = config.segments.findIndex(segment => angle >= segment.start && angle < segment.end);
  if (segmentIndex !== config.activeSegment) {{
    config.activeSegment = segmentIndex;
    updateWidgetHighlight(config, segmentIndex);
  }}
}}

function resetWidgetHighlight(config) {{
  config.activeSegment = -1;
  updateWidgetHighlight(config, -1);
}}

widgetPieConfigs.forEach(config => {{
  const canvasEl = document.getElementById(config.id);
  if (!canvasEl) {{
    return;
  }}
  drawWidgetPieChart(canvasEl, config.data, config);
  canvasEl.addEventListener("mousemove", event => handleWidgetPieHover(event, config));
  canvasEl.addEventListener("mouseout", () => resetWidgetHighlight(config));
}});

if (canvas) {{
  canvas.addEventListener("mousemove", handlePieHover);
  canvas.addEventListener("mouseout", () => {{
    if (currentHighlightIndex !== -1) {{
      currentHighlightIndex = -1;
      drawTopicPieChart();
    }}
    setPieTooltip(-1);
  }});
}}

if (pageInput) {{
  pageInput.addEventListener("keydown", event => {{
    if (event.key === "Enter") {{
      event.preventDefault();
      goToPage();
    }}
  }});
}}

searchInput.addEventListener("input", () => filterChats(searchInput.value));
renderPage();
drawTopicPieChart();
</script>
</body>
</html>
"""

CHAT_CARD_TEMPLATE = """
<div class="chat-card" data-search="{searchable}">
  <h3>{prompt}</h3>
  <p><strong>Source:</strong> {source_embed}</p>
  <p><strong>Session ID:</strong> {session_id}</p>
  <p><strong>Answer:</strong><br>{response}</p>
  <p><small>Created at: {created_at}</small></p>
  <p><strong>Sources:</strong></p>
  <ul class="source-list">
    {source_items}
  </ul>
</div>
"""

TERMS_CARD_TEMPLATE = """
<div class="metric-card">
  <strong>{count}</strong>
  {term}
</div>
"""

def parse_json_file(path: pathlib.Path) -> Iterable[Dict[str, Any]]:
    text = path.read_text(encoding="utf-8")
    try:
        data = json.loads(text)
        if isinstance(data, list):
            return data
        return [data]
    except json.JSONDecodeError:
        records = []
        for line in text.splitlines():
            line = line.strip()
            if not line:
                continue
            records.append(json.loads(line))
        return records

def parse_jsonl_file(path: pathlib.Path) -> Iterable[Dict[str, Any]]:
    with path.open(encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            yield json.loads(line)

def parse_csv_file(path: pathlib.Path) -> Iterable[Dict[str, Any]]:
    with path.open(encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            full_message = row.get("full_message", "") or ""
            prompt = full_message
            response_data = {}
            json_start = full_message.find('{"text"')
            if json_start != -1:
                prompt = full_message[:json_start].strip()
                try:
                    response_data = json.loads(full_message[json_start:])
                except json.JSONDecodeError:
                    response_data = {}
            record = {
                "chat_id": row.get("chat_id", ""),
                "session_id": row.get("session_id", ""),
                "prompt": prompt,
                "created_at": row.get("created_at"),
                "text": json.dumps(response_data) if response_data else "",
                "raw_chat": {"response": response_data},
                "connection_information": row.get("widget", ""),
                "full_message": full_message,
                "parsed_question": prompt,
                "parsed_answer": response_data.get("text", ""),
                "metrics": {},
            }
            yield record

def normalize_text(value: Any) -> str:
    if isinstance(value, dict):
        return json.dumps(value, ensure_ascii=False)
    return str(value) if value is not None else ""

def build_searchable_text(record: Dict[str, Any]) -> str:
    parts = [
        record.get("prompt", ""),
        record.get("parsed_question", ""),
        normalize_text(record.get("parsed_answer")),
        normalize_text(record.get("text")),
        normalize_text(record.get("full_message")),
        normalize_text(record.get("translated_text")),
        normalize_text(record.get("raw_chat")),
        record.get("session_id", ""),
        ",".join(str(record.get("connection_information", "")).split()),
    ]
    return " ".join(parts).lower()

def combine_text_for_metrics(record: Dict[str, Any]) -> str:
    parts = [
        record.get("prompt", ""),
        record.get("parsed_question", ""),
        normalize_text(record.get("parsed_answer")),
        normalize_text(record.get("text")),
        normalize_text(record.get("full_message")),
        normalize_text(record.get("translated_text")),
        normalize_text(record.get("raw_chat")),
        record.get("session_id", ""),
        ",".join(str(record.get("connection_information", "")).split()),
    ]
    return " ".join(parts)

def build_source_items(raw_chat: Dict[str, Any]) -> str:
    sources = raw_chat.get("response", {})
    if isinstance(sources, str):
        try:
            sources = json.loads(sources).get("sources", [])
        except json.JSONDecodeError:
            sources = []
    else:
        sources = sources.get("sources", [])
    items = []
    for src in sources:
        title = escape(src.get("title", src.get("url", "source")))
        items.append(f"<li>{title}</li>")
    return "\n".join(items) if items else "<li>No sources recorded.</li>"

def resolve_embed_name(record: Dict[str, Any]) -> str:
    def get_from_path(obj: Dict[str, Any], path: List[str]) -> Any:
        current = obj
        for key in path:
            if not isinstance(current, dict):
                return None
            current = current.get(key)
        return current

    raw_chat = record.get("raw_chat") or {}
    response = raw_chat.get("response") or {}

    source_embed = get_from_path(record, ["source", "embed_name"])
    if source_embed:
        return str(source_embed)

    source_embed = get_from_path(raw_chat, ["source", "embed_name"])
    if source_embed:
        return str(source_embed)

    source_embed = get_from_path(response, ["source", "embed_name"])
    if source_embed:
        return str(source_embed)

    sources_list = response.get("sources", [])
    if isinstance(sources_list, list):
        for src in sources_list:
            if isinstance(src, dict):
                embed = src.get("embed_name")
                if embed:
                    return str(embed)
                inner_source = src.get("source") or {}
                if isinstance(inner_source, dict):
                    embed = inner_source.get("embed_name")
                    if embed:
                        return str(embed)
    for candidate in (
        record.get("embed_name"),
        record.get("widget"),
        record.get("connection_information"),
        raw_chat.get("widget"),
        raw_chat.get("embed_name"),
        raw_chat.get("connection_information"),
    ):
        if candidate:
            return str(candidate)
    return "Unknown Widget"

def load_terms(terms_path: pathlib.Path) -> List[Dict[str, Any]]:
    terms: List[Dict[str, Any]] = []
    if not terms_path or not terms_path.exists():
        return terms
    for line in terms_path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if "=" in line:
            name, pattern = line.split("=", 1)
            name = name.strip()
            pattern = pattern.strip()
        else:
            name = line
            pattern = re.escape(line)
        if not name:
            name = pattern
        if not pattern:
            pattern = re.escape(name)
        try:
            compiled = re.compile(pattern)
        except re.error as exc:
            raise ValueError(f"Invalid regex for term '{name}': {exc}") from exc
        terms.append({"name": name, "pattern": compiled})
    return terms

def parse_datetime(value: Any) -> datetime:
    if isinstance(value, datetime):
        return value
    if not value:
        return datetime.min.replace(tzinfo=timezone.utc)
    text = str(value).strip()
    if not text:
        return datetime.min.replace(tzinfo=timezone.utc)
    try:
        if text.endswith("Z"):
            text = text[:-1] + "+00:00"
        return datetime.fromisoformat(text)
    except ValueError:
        for fmt in ("%Y-%m-%d %H:%M:%S", "%Y-%m-%dT%H:%M:%S", "%Y-%m-%d"):
            try:
                return datetime.strptime(text, fmt).replace(tzinfo=timezone.utc)
            except ValueError:
                continue
    return datetime.min.replace(tzinfo=timezone.utc)

def format_datetime_mmddyyyy(value: Any) -> str:
    dt = parse_datetime(value)
    return dt.strftime("%m-%d-%Y")

def calculate_term_metrics(records: List[Dict[str, Any]], terms: List[Dict[str, Any]]) -> (Counter, Counter, Counter, int, int, Dict[str, Counter]):
    counter = Counter()
    first_counter = Counter()
    unique_match_counts = Counter()
    widget_stats: Dict[str, Counter] = defaultdict(Counter)
    no_match_count = 0
    multi_count = 0

    for record in records:
        text = combine_text_for_metrics(record)
        first_match = None
        first_pos = None
        matched_terms = set()
        for term in terms:
            term_name = term["name"]
            pattern = term["pattern"]
            for match in pattern.finditer(text):
                counter[term_name] += 1
                matched_terms.add(term_name)
                start = match.start()
                if first_pos is None or start < first_pos:
                    first_pos = start
                    first_match = term_name
        widget_name = resolve_embed_name(record)
        if matched_terms:
            if len(matched_terms) == 1:
                topic_term = first_match or next(iter(matched_terms))
                unique_match_counts[topic_term] += 1
                widget_stats[widget_name][topic_term] += 1
            else:
                multi_count += 1
                widget_stats[widget_name][MULTI_TERM_NAME] += 1
            if first_match:
                first_counter[first_match] += 1
        else:
            no_match_count += 1
            widget_stats[widget_name][DEFAULT_TERM_NAME] += 1

    return counter, first_counter, unique_match_counts, multi_count, no_match_count, widget_stats

def collect_records_from_path(path: pathlib.Path) -> List[Dict[str, Any]]:
    records = []
    if path.is_dir():
        for candidate in sorted(path.iterdir()):
            records.extend(_parse_candidate(candidate))
        return records
    if path.is_file():
        records.extend(_parse_candidate(path))
        return records
    raise FileNotFoundError(f"Path {path} is not a file or directory with supported extensions.")

def _parse_candidate(path: pathlib.Path) -> List[Dict[str, Any]]:
    if path.suffix.lower() == ".jsonl":
        return list(parse_jsonl_file(path))
    if path.suffix.lower() == ".json":
        return list(parse_json_file(path))
    if path.suffix.lower() == ".csv":
        return list(parse_csv_file(path))
    return []

def build_widget_cards(
    widget_stats: Dict[str, Counter],
) -> Tuple[str, List[Dict[str, Any]]]:
    if not widget_stats:
        return "<div class='metric-card'><strong>0</strong>No widgets recorded.</div>", []
    cards = []
    pie_configs = []
    for idx, (widget_name, stats) in enumerate(sorted(widget_stats.items(), key=lambda item: item[0].lower())):
        stats_with_defaults = {
            **{DEFAULT_TERM_NAME: 0, MULTI_TERM_NAME: 0},
            **stats,
        }
        total = sum(stats.values())
        badges = [
            f'<span class="badge">{escape(term)}: {count}</span>'
            for term, count in sorted(stats_with_defaults.items(), key=lambda item: (-item[1], item[0]))
            if count > 0
        ]
        badges_html = f'<div class="widget-badges">{"".join(badges)}</div>' if badges else "<p>No term matches recorded.</p>"
        pie_data = [
            {"label": term, "value": count}
            for term, count in sorted(stats_with_defaults.items(), key=lambda item: (-item[1], item[0]))
            if count > 0
        ]
        pie_configs.append(
            {
                "id": f"widgetPie-{idx}",
                "data": pie_data,
                "tooltipId": f"widgetPieTooltip-{idx}",
                "activeSegment": -1,
            }
        )
        cards.append(
            f"""
<div class='widget-card'>
  <h3>{escape(widget_name)}</h3>
  <p>{total} match(es)</p>
  {badges_html}
  <canvas class='widget-pie-chart' id='widgetPie-{idx}' width='200' height='200'></canvas>
  <div class='widget-badge-tooltip' id='widgetPieTooltip-{idx}'></div>
</div>
"""
        )
    return "\n".join(cards), pie_configs

def main():
    parser = argparse.ArgumentParser(description="Generate HTML report from chat JSON/CSV exports.")
    parser.add_argument("input_path", type=pathlib.Path, help="JSON/CSV file or directory containing exports.")
    parser.add_argument("--terms", type=pathlib.Path, default=None, help="Optional keywords file (name=regex per line).")
    parser.add_argument("--report", type=pathlib.Path, default=pathlib.Path("report"), help="Directory where HTML reports are written.")
    parser.add_argument("--page-size", type=int, default=10, help="Number of chats to show per page.")
    parser.add_argument("--page", type=int, default=1, help="Initial page number to display (1-based).")
    args = parser.parse_args()

    if not args.input_path.exists():
        parser.error(f"Path {args.input_path} does not exist.")
    if args.page_size < 1:
        parser.error("page-size must be at least 1.")
    if args.page < 1:
        parser.error("page must be at least 1.")

    records = collect_records_from_path(args.input_path)
    session_counts = Counter(escape(record.get("session_id", "") or "Unknown") for record in records)
    unique_sessions = len(session_counts)
    top_sessions = session_counts.most_common(10)
    top_sessions_html = "\n".join(
        f"<li><strong>{session}</strong>: {count} chat(s)</li>"
        for session, count in top_sessions
    ) or "<li>No sessions recorded.</li>"

    terms = load_terms(args.terms) if args.terms else []
    term_counts, first_term_counts, unique_match_counts, multi_count, no_match_count, widget_stats = calculate_term_metrics(records, terms)

    topic_entries = []
    if terms:
        topic_entries.append(TERMS_CARD_TEMPLATE.format(term=escape(MULTI_TERM_NAME), count=multi_count))
    topic_entries.append(TERMS_CARD_TEMPLATE.format(term=escape(DEFAULT_TERM_NAME), count=no_match_count))
    if first_term_counts:
        topic_entries.extend(
            TERMS_CARD_TEMPLATE.format(term=escape(name), count=count)
            for name, count in sorted(first_term_counts.items(), key=lambda item: item[1], reverse=True)
        )
    if not topic_entries:
        topic_entries = ["<div class='metric-card'><strong>0</strong>No topic data yet</div>"]
    first_topic_entries = "\n".join(topic_entries)

    topic_pie_data = []
    if terms:
        topic_pie_data.append({"label": MULTI_TERM_NAME, "value": multi_count})
    topic_pie_data.append({"label": DEFAULT_TERM_NAME, "value": no_match_count})
    topic_pie_data.extend(
        {"label": name, "value": count}
        for name, count in sorted(first_term_counts.items(), key=lambda item: item[1], reverse=True)
        if count > 0
    )

    widget_cards_html, widget_pie_configs = build_widget_cards(widget_stats)

    records_sorted = sorted(
        records,
        key=lambda rec: parse_datetime(rec.get("created_at") or rec.get("prompt_date") or ""),
        reverse=True,
    )

    chat_cards = []
    for record in records_sorted:
        searchable = escape(build_searchable_text(record))
        prompt = escape(record.get("parsed_question") or record.get("prompt", "") or "")
        response_value = record.get("parsed_answer")
        if not response_value and record.get("text"):
            try:
                response_value = json.loads(record.get("text")).get("text", "")
            except json.JSONDecodeError:
                response_value = record.get("text", "")
        response = escape(response_value or "").replace("\n", "<br>")
        session_id = escape(record.get("session_id", ""))
        raw_chat = record.get("raw_chat", {}) or {}
        sources_section = build_source_items(raw_chat)
        source_embed = escape(resolve_embed_name(record))
        created_at_display = format_datetime_mmddyyyy(record.get("created_at") or record.get("prompt_date") or "")
        card = CHAT_CARD_TEMPLATE.format(
            searchable=searchable,
            prompt=prompt,
            source_embed=source_embed,
            session_id=session_id,
            response=response[:800] + ("…" if len(response) > 800 else ""),
            source_items=sources_section,
            created_at=created_at_display,
        )
        chat_cards.append(card)

    html = HTML_TEMPLATE.format(
        timestamp=datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC"),
        chat_count=len(records),
        term_total=sum(term_counts.values()),
        first_topic_section=first_topic_entries,
        chat_cards="\n".join(chat_cards),
        page_size=args.page_size,
        topic_pie_data=json.dumps(topic_pie_data),
        widget_cards=widget_cards_html,
        widget_pie_configs=json.dumps(widget_pie_configs),
        unique_sessions=unique_sessions,
        top_sessions=top_sessions_html,
        initial_page=args.page,
    )

    report_dir = args.report
    report_dir.mkdir(parents=True, exist_ok=True)
    output_file = report_dir / "index.html"
    output_file.write_text(html, encoding="utf-8")
    print(f"Report written to {output_file.resolve()}")

if __name__ == "__main__":
    main()
