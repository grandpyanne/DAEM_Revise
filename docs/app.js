// DAEM Revise, web version · crafted by Grandpyanne™
// Reads the same content.json as the Android app. Progress is kept on this phone (localStorage).
// Pages are addresses after the # (e.g. #/topic/mechtech/mt-4/quiz), so the browser's back
// gesture works and reopening the app returns to the same page, mid-quiz included.
"use strict";

const ACCENTS = ["#3D6FB6", "#7B5CC4", "#2F9E8F", "#D1793A", "#4F8A3C", "#B0464F", "#3A8FC4", "#8A6D3B"];
const SITTING = /(June\/July|Oct\/Nov|March\/April) \d{4}/g;
const QUIZ_PASS = 70;
const KEEP_EXAMS_MS = 7 * 24 * 3600 * 1000;

let DATA = null;      // content.json
let SKETCH = null;    // sketches.json: { S: topic sketches, FIG: note diagrams }
let EXAMS = [];       // exams.json
const app = document.getElementById("app");

// ---------------------------------------------------------------- storage
const store = (() => {
  const KEY = "daem-revise-v1";
  let s = {};
  try { s = JSON.parse(localStorage.getItem(KEY) || "{}") || {}; } catch (e) { s = {}; }
  const save = () => { try { localStorage.setItem(KEY, JSON.stringify(s)); } catch (e) { /* storage full or blocked */ } };
  return {
    get: (k, d) => (k in s ? s[k] : d),
    set: (k, v) => { s[k] = v; save(); },
    obj: (k) => { if (!s[k] || typeof s[k] !== "object") s[k] = {}; return s[k]; },
    save,
  };
})();

const today = () => { const d = new Date(); return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`; };
const dayNum = () => Math.floor((Date.now() + 3 * 3600e3) / 86400e3);

function markStudied() {
  const days = store.obj("days");
  days[today()] = (days[today()] || 0) + 1;
  store.save();
}
function streak() {
  const days = store.obj("days");
  let n = 0;
  const d = new Date();
  if (!days[today()]) d.setDate(d.getDate() - 1);
  for (;;) {
    const k = `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`;
    if (!days[k]) break;
    n++; d.setDate(d.getDate() - 1);
  }
  return n;
}

// Spaced repetition per question: missed → back today; right → 1, 3, 7, 14, 30 days.
const GAPS = [1, 3, 7, 14, 30];
function qState(tid, i) { return store.obj("srs")[`${tid}#${i}`]; }
function rate(tid, i, right) {
  const srs = store.obj("srs");
  const k = `${tid}#${i}`;
  const box = right ? Math.min(((srs[k] && srs[k].box) ?? -1) + 1, GAPS.length - 1) : -1;
  srs[k] = { box, due: dayNum() + (box < 0 ? 0 : GAPS[box]) };
  const a = store.obj("answers");
  a[today()] = (a[today()] || 0) + 1;
  store.save();
  markStudied();
}

// Stars, as in Kapi's Park: notes read, quiz ≥ 70%, at least half the cards known.
function topicStars(t) {
  const best = store.obj("quizBest")[t.id] || 0;
  const known = (store.obj("cardsKnown")[t.id] || []).length;
  return [
    !!store.obj("notesRead")[t.id] && t.notes.length > 0,
    t.mcqs.length > 0 && best >= QUIZ_PASS,
    t.cards.length > 0 && known >= Math.ceil(t.cards.length / 2),
  ];
}
const starCount = (t) => topicStars(t).filter(Boolean).length;

// ---------------------------------------------------------------- helpers
const esc = (s) => String(s ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const unitById = (id) => DATA.units.find((u) => u.id === id);
const accentOf = (u) => ACCENTS[DATA.units.indexOf(u) % ACCENTS.length];
const sittings = (t) => new Set(t.questions.flatMap((q) => q.src.match(SITTING) || [])).size;
const shuffle = (a) => { a = a.slice(); for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; } return a; };
const go = (h) => { location.hash = h; };
const starsHtml = (t) => `<span class="stars">${topicStars(t).map((on) => (on ? "★" : '<span class="off">★</span>')).join("")}</span>`;

function header(title, sub, back = true, extra = "") {
  return `<div class="top">${back ? '<button class="icon-btn" data-back aria-label="Back">‹</button>' : ""}
    <div class="grow"><h1>${esc(title)}</h1>${sub ? `<div class="sub">${esc(sub)}</div>` : ""}</div>${extra}</div>`;
}

function render(html, after) {
  app.innerHTML = html;
  app.querySelectorAll("[data-back]").forEach((b) => b.addEventListener("click", () => (history.length > 1 ? history.back() : go("#/"))));
  app.querySelectorAll("[data-go]").forEach((b) => b.addEventListener("click", () => go(b.dataset.go)));
  drawFigures();
  if (after) after();
}

// ---------------------------------------------------------------- figures
function drawFigures() {
  const css = getComputedStyle(document.documentElement);
  const ink = css.getPropertyValue("--ink").trim() || "#2A4F86";
  const textInk = css.getPropertyValue("--text").trim() || "#16202E";
  app.querySelectorAll("canvas[data-fig]").forEach((cv) => {
    const fig = SKETCH && SKETCH.FIG[cv.dataset.fig];
    if (!fig) return;
    const [h, , src] = fig;
    const w = Math.min(cv.parentElement.clientWidth - 24, 440);
    const dpr = window.devicePixelRatio || 1;
    cv.style.width = w + "px";
    cv.style.height = (w * h / 200) + "px";
    cv.width = Math.round(w * dpr);
    cv.height = Math.round(w * h / 200 * dpr);
    const c = cv.getContext("2d");
    c.setTransform(dpr, 0, 0, dpr, 0, 0);
    drawSketch(c, src, w / 200, ink, textInk);
  });
}
window.addEventListener("resize", () => drawFigures());

// ---------------------------------------------------------------- home
function examCard() {
  const now = Date.now();
  const papers = EXAMS.map((p) => ({ ...p, s: Date.parse(p.start), e: Date.parse(p.end) })).sort((a, b) => a.s - b.s);
  if (!papers.length || now > Math.max(...papers.map((p) => p.e)) + KEEP_EXAMS_MS) return "";
  const sitting = papers.find((p) => now >= p.s && now < p.e);
  const next = papers.find((p) => p.s > now);
  const when = (ms) => new Date(ms).toLocaleString("en-GB", { weekday: "short", day: "numeric", month: "short", hour: "numeric", minute: "2-digit", timeZone: "Africa/Nairobi" });
  const left = (ms) => {
    const m = Math.max(0, Math.floor(ms / 60000)), d = Math.floor(m / 1440), h = Math.floor(m / 60) % 24;
    return d > 0 ? `${d} ${d === 1 ? "day" : "days"} ${h} h` : h > 0 ? `${h} h ${m % 60} min` : `${m % 60} min`;
  };
  const head = sitting ? `<div class="next">Now: ${esc(sitting.title)}</div><div class="small muted">Good luck! You've got this 🍀</div>`
    : next ? `<div class="next">${esc(next.title)} in ${left(next.s - now)}</div><div class="small muted">${when(next.s)} · ${esc(next.venue)}</div>`
    : `<div class="next">All papers done 🎉</div><div class="small muted">Well done. Rest, then back at it next term.</div>`;
  const rows = papers.map((p) => {
    const done = now >= p.e, isNext = p === next && !sitting;
    return `<div class="paper ${done ? "done" : ""}" data-go="#/unit/${p.unit}"><span class="mark">${done ? "✅" : isNext ? "👉" : "•"}</span>
      <div><div class="t" style="font-weight:${isNext ? 700 : 400}">${esc(p.title)}</div><div class="small muted">${when(p.s)} · ${esc(p.venue)}</div></div></div>`;
  }).join("");
  return `<div class="card exam"><h2>📅 End-of-term exams</h2>${head}<div style="margin-top:8px">${rows}</div></div>`;
}

function installCard() {
  const standalone = window.matchMedia("(display-mode: standalone)").matches || navigator.standalone;
  if (standalone || store.get("hideInstall", false)) return "";
  const ios = /iphone|ipad|ipod/i.test(navigator.userAgent);
  const how = ios ? "In Safari, tap <b>Share</b> (the square with an arrow), then <b>Add to Home Screen</b>."
    : "In your browser menu (⋮), tap <b>Add to Home screen</b> or <b>Install app</b>.";
  return `<div class="card install"><h2>📲 Put it on your home screen</h2><div class="small">${how} It then opens like an app and works offline.</div>
    <button class="btn ghost" style="margin-top:10px" id="hideInstall">Got it</button></div>`;
}

function home() {
  const name = store.get("name", "");
  const hour = new Date().getHours();
  const hi = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
  const s = streak();
  const answered = store.obj("answers")[today()] || 0;
  const units = DATA.units.filter((u) => u.ready && u.topics.length);
  const last = store.get("lastTopic", null);
  const lastT = last && unitById(last.u) && unitById(last.u).topics.find((t) => t.id === last.t);
  render(`
    ${header("DAEM Revise", "Year 2 · Airframes & Engines", false, '<button class="icon-btn" data-go="#/search" aria-label="Search">🔍</button>')}
    <div class="hero"><img src="img/mascot_happy.svg" alt="Kapi">
      <div><h1>${hi}${name ? ", " + esc(name) : ""}</h1>
      <div class="muted small">${s ? `🔥 ${s}-day streak` : "Start a streak today"} · ${answered} question${answered === 1 ? "" : "s"} today</div></div></div>
    ${examCard()}
    ${installCard()}
    <div class="btns">
      <button class="btn gold" data-go="#/quick">⚡ Quick 5</button>
      ${lastT ? `<button class="btn ghost" data-go="#/topic/${last.u}/${last.t}/notes">↩ ${esc(lastT.title)}</button>` : '<button class="btn ghost" data-go="#/search">🔍 Search</button>'}
    </div>
    <div class="list">${units.map((u) => {
      const got = u.topics.reduce((n, t) => n + starCount(t), 0), all = u.topics.length * 3;
      return `<button class="item" data-go="#/unit/${u.id}"><span class="bar" style="background:${accentOf(u)}"></span>
        <div class="grow"><div class="title">${esc(u.name)}</div><div class="small muted">${u.topics.length} topics · ★ ${got}/${all}</div>
        <div class="progress" style="margin-top:6px"><div style="width:${Math.round(100 * got / all)}%;background:${accentOf(u)}"></div></div></div></button>`;
    }).join("")}</div>
    <div class="card"><div class="row"><div class="grow"><b>Your name</b><div class="small muted">For the greeting. Stays on this phone.</div></div>
      <input id="name" class="search" style="max-width:160px" value="${esc(name)}" placeholder="Name"></div></div>
    <p class="foot">Kapi's games and the PDF books are in the Android app.<br>DAEM Revise · Grandpyanne™ · content updated ${esc(DATA.updated)}</p>
  `, () => {
    const hide = document.getElementById("hideInstall");
    if (hide) hide.onclick = () => { store.set("hideInstall", true); home(); };
    document.getElementById("name").onchange = (e) => { store.set("name", e.target.value.trim().slice(0, 30)); home(); };
  });
}

// ---------------------------------------------------------------- unit
function unitPage(uid, partIdx) {
  const u = unitById(uid);
  if (!u) return go("#/");
  const parts = u.parts || [];
  const pi = parts.length ? Math.min(+(partIdx ?? store.obj("lastPart")[uid] ?? 0), parts.length - 1) : 0;
  if (parts.length) { store.obj("lastPart")[uid] = pi; store.save(); }
  const topics = parts.length ? u.topics.filter((t) => t.part === parts[pi]) : u.topics;
  render(`
    ${header(u.name, u.note)}
    ${parts.length ? `<div class="tabs">${parts.map((p, i) => `<button class="tab ${i === pi ? "on" : ""}" data-go="#/unit/${uid}/${i}">${esc(p)}</button>`).join("")}</div>` : ""}
    <div class="btns"><button class="btn" data-go="#/uquiz/${uid}">📝 Unit quiz (10)</button><button class="btn ghost" data-go="#/search">🔍 Search</button></div>
    <div class="list">${topics.map((t) => `
      <button class="item" data-go="#/topic/${uid}/${t.id}/notes"><span class="bar" style="background:${accentOf(u)}"></span>
        <div class="grow"><div class="title">${esc(t.title)}</div>
        <div class="small muted">${t.notes.length ? "Notes · " : ""}${t.questions.length} paper Qs · ${t.mcqs.length} MCQs · ${t.cards.length} cards</div></div>
        ${starsHtml(t)}</button>`).join("")}</div>
  `);
}

// ---------------------------------------------------------------- topic
function noteHtml(n) {
  switch (n.type) {
    case "h": return `<h3>${esc(n.text)}</h3>`;
    case "p": return `<p>${esc(n.text)}</p>`;
    case "bullets": return `<ul>${n.items.map((i) => `<li>${esc(i)}</li>`).join("")}</ul>`;
    // Several formulas on one line are separated by wide gaps: show each on its own line.
    case "formula": return `<div class="formula">${n.text.split(/\s{3,}/).map(esc).join("<br>")}</div>`;
    case "tip": return `<div class="tip">${esc(n.text)}</div>`;
    case "fig": {
      const f = SKETCH && SKETCH.FIG[n.id];
      return f ? `<div class="figure"><canvas data-fig="${esc(n.id)}"></canvas><div class="cap">${esc(f[1])}</div></div>` : "";
    }
    default: return "";
  }
}

function topicPage(uid, tid, tab) {
  const u = unitById(uid);
  const t = u && u.topics.find((x) => x.id === tid);
  if (!t) return go("#/");
  store.set("lastTopic", { u: uid, t: tid });
  const tabs = [["notes", "Notes"], ["papers", `Papers (${t.questions.length})`], ["cards", `Cards (${t.cards.length})`], ["quiz", `Quiz (${t.mcqs.length})`]];
  tab = tabs.some(([k]) => k === tab) ? tab : "notes";
  if (tab === "quiz") return quizPage(`t:${tid}`, t.title, [t], t.mcqs.length, `#/topic/${uid}/${tid}/quiz`);
  if (tab === "cards") return cardsPage(u, t);
  const n = sittings(t);
  const top = `${header(t.title, t.section || u.name)}
    <div class="row" style="flex-wrap:wrap;gap:6px">${n ? `<span class="tag">Asked ${n === 1 ? "once" : n + " times"}</span>` : ""}${starsHtml(t)}</div>
    <div class="tabs">${tabs.map(([k, l]) => `<button class="tab ${k === tab ? "on" : ""}" data-go="#/topic/${uid}/${tid}/${k}">${esc(l)}</button>`).join("")}</div>`;
  if (tab === "papers") {
    return render(`${top}<div class="card">${t.questions.length ? t.questions.map((q) => `
      <div class="q"><div>${esc(q.q)}</div><div class="source" style="margin-top:4px">${esc(q.src)}</div>
      ${(q.figs || []).map((f) => `<a href="figures/${esc(f)}" target="_blank"><img src="figures/${esc(f)}" alt="Figure" loading="lazy"></a>`).join("")}</div>`).join("")
      : '<p class="muted">No past-paper questions for this topic yet.</p>'}</div>`);
  }
  const read = !!store.obj("notesRead")[tid];
  const refs = [
    ...(t.refs || []).map((r) => `📖 ${esc((DATA.books || []).find((b) => b.id === r.book)?.title || r.book)} · ${esc(r.where)}${r.page ? ", p. " + r.page : ""}`),
    ...(t.pdfs || []).map((p) => `📄 ${esc(p.file)}${p.where ? " · " + esc(p.where) : ""}`),
  ];
  render(`${top}
    ${t.summary ? `<div class="tip">${esc(t.summary)}</div>` : ""}
    <div class="card notes">${t.notes.length ? t.notes.map(noteHtml).join("") : '<p class="muted">Notes for this topic are coming. The papers, cards and quiz are ready.</p>'}</div>
    ${t.formulas && t.formulas.length ? `<div class="card"><h2>Key formulas</h2>${t.formulas.map((f) => `<div class="formula"><b>${esc(f.name)}:</b> ${esc(f.expr)}</div>`).join("")}</div>` : ""}
    ${refs.length ? `<div class="card"><h2>Read more</h2>${refs.map((r) => `<div class="small" style="margin:4px 0">${r}</div>`).join("")}</div>` : ""}
    ${t.notes.length ? `<button class="btn block ${read ? "ghost" : ""}" id="read">${read ? "✓ Notes read" : "I've read these notes ★"}</button>` : ""}
  `, () => {
    const b = document.getElementById("read");
    if (b) b.onclick = () => { store.obj("notesRead")[tid] = !read; store.save(); if (!read) markStudied(); topicPage(uid, tid, "notes"); };
  });
}

// ---------------------------------------------------------------- flashcards
function cardsPage(u, t) {
  const st = store.obj("cardPos");
  let i = Math.min(st[t.id] || 0, Math.max(0, t.cards.length - 1));
  let flipped = false;
  const known = new Set(store.obj("cardsKnown")[t.id] || []);
  const draw = () => {
    const c = t.cards[i];
    render(`${header(t.title, `Card ${t.cards.length ? i + 1 : 0} of ${t.cards.length} · ${known.size} known`)}
      <div class="tabs">${[["notes", "Notes"], ["papers", `Papers (${t.questions.length})`], ["cards", `Cards (${t.cards.length})`], ["quiz", `Quiz (${t.mcqs.length})`]]
        .map(([k, l]) => `<button class="tab ${k === "cards" ? "on" : ""}" data-go="#/topic/${u.id}/${t.id}/${k}">${esc(l)}</button>`).join("")}</div>
      ${c ? `<div class="card flash" id="flip"><div class="side"><div class="lbl">${flipped ? "Answer" : "Question · tap to flip"}</div>${esc(flipped ? c.back : c.front)}</div></div>
      <div class="btns"><button class="btn ghost" id="again">↺ Again</button><button class="btn" id="know">✓ I know it</button></div>
      <div class="btns"><button class="btn ghost" id="prev">‹ Back</button><button class="btn ghost" id="next">Next ›</button></div>`
      : '<p class="muted">No flashcards for this topic yet.</p>'}`, () => {
      if (!c) return;
      const move = (d) => { i = (i + d + t.cards.length) % t.cards.length; flipped = false; st[t.id] = i; store.save(); draw(); };
      document.getElementById("flip").onclick = () => { flipped = !flipped; draw(); };
      document.getElementById("prev").onclick = () => move(-1);
      document.getElementById("next").onclick = () => move(1);
      document.getElementById("again").onclick = () => { known.delete(i); store.obj("cardsKnown")[t.id] = [...known]; store.save(); move(1); };
      document.getElementById("know").onclick = () => { known.add(i); store.obj("cardsKnown")[t.id] = [...known]; store.save(); markStudied(); move(1); };
    });
  };
  draw();
}

// ---------------------------------------------------------------- quiz
// Missed and due questions first, then new ones, then the rest. The quiz in progress is saved,
// so leaving the app (or the phone closing it) and coming back resumes where you were.
function buildQuiz(topics, limit) {
  const all = topics.flatMap((t) => t.mcqs.map((_, i) => ({ t, i, s: qState(t.id, i) })));
  const now = dayNum();
  const due = shuffle(all.filter((x) => x.s && x.s.due <= now)).sort((a, b) => (a.s.box < 0 ? 0 : 1) - (b.s.box < 0 ? 0 : 1) || a.s.due - b.s.due);
  const fresh = shuffle(all.filter((x) => !x.s));
  const later = shuffle(all.filter((x) => x.s && x.s.due > now)).sort((a, b) => a.s.due - b.s.due);
  return shuffle([...due, ...fresh, ...later].slice(0, limit)).map((x) => ({ tid: x.t.id, i: x.i, order: shuffle(x.t.mcqs[x.i].options.map((_, k) => k)) }));
}

function quizPage(key, title, topics, limit, here) {
  const saved = store.obj("quizzes");
  const findQ = (tid, i) => { const t = topics.find((x) => x.id === tid); return t && t.mcqs[i]; };
  let q = saved[key];
  if (!q || !q.items || !q.items.length || q.items.some((it) => !findQ(it.tid, it.i))) {
    q = saved[key] = { items: buildQuiz(topics, limit), index: 0, correct: 0, chosen: null, recorded: false };
    store.save();
  }
  const tabsHtml = () => {
    if (!key.startsWith("t:")) return "";
    const t = topics[0], u = DATA.units.find((x) => x.topics.includes(t));
    return `<div class="tabs">${[["notes", "Notes"], ["papers", `Papers (${t.questions.length})`], ["cards", `Cards (${t.cards.length})`], ["quiz", `Quiz (${t.mcqs.length})`]]
      .map(([k, l]) => `<button class="tab ${k === "quiz" ? "on" : ""}" data-go="#/topic/${u.id}/${t.id}/${k}">${esc(l)}</button>`).join("")}</div>`;
  };
  const restart = () => { delete saved[key]; store.save(); quizPage(key, title, topics, limit, here); };

  if (!q.items.length) return render(`${header(title)}${tabsHtml()}<p class="muted">No quiz questions here yet.</p>`);

  if (q.index >= q.items.length) {
    const pct = Math.round(100 * q.correct / q.items.length);
    const best = store.obj("quizBest");
    const bestKey = key.startsWith("t:") ? key.slice(2) : key;
    if (!q.recorded) {
      q.recorded = true;
      best[bestKey] = Math.max(best[bestKey] || 0, pct);
      store.save();
    }
    return render(`${header(title, "Quiz complete")}${tabsHtml()}
      <div class="card" style="text-align:center"><div class="score">${pct}%</div>
      <div>${q.correct} of ${q.items.length} right · best ${best[bestKey]}%</div>
      <div class="muted small" style="margin-top:6px">${pct >= QUIZ_PASS ? "Great work ★" : "Missed questions come back first next time."}</div></div>
      <div class="btns"><button class="btn" id="again">↺ Try again</button><button class="btn ghost" data-back>Done</button></div>`,
      () => { document.getElementById("again").onclick = restart; });
  }

  const it = q.items[q.index];
  const mcq = findQ(it.tid, it.i);
  const opts = it.order.map((k) => mcq.options[k]);
  const right = it.order.indexOf(mcq.answer);
  const topicTitle = topics.length > 1 ? (topics.find((t) => t.id === it.tid) || {}).title : "";
  render(`${header(title, `Question ${q.index + 1} of ${q.items.length}`)}${tabsHtml()}
    <div class="progress" style="margin:8px 0"><div style="width:${100 * q.index / q.items.length}%"></div></div>
    <div class="card">${topicTitle ? `<div class="small muted">${esc(topicTitle)}</div>` : ""}
      <div style="font-weight:600;font-size:17px;margin:4px 0 8px">${esc(mcq.q)}</div>
      ${opts.map((o, k) => `<button class="opt ${q.chosen != null ? (k === right ? "right" : k === q.chosen ? "wrong" : "") : ""}" data-opt="${k}" ${q.chosen != null ? "disabled" : ""}>${esc(o)}</button>`).join("")}
      ${q.chosen != null ? `<div class="explain"><b>${q.chosen === right ? "✅ Right." : "❌ Not quite."}</b> ${esc(mcq.explain || "")}</div>` : ""}
    </div>
    ${q.chosen != null ? `<button class="btn block" id="next">${q.index === q.items.length - 1 ? "See result" : "Next question ›"}</button>` : ""}
  `, () => {
    app.querySelectorAll("[data-opt]").forEach((b) => b.onclick = () => {
      if (q.chosen != null) return;
      q.chosen = +b.dataset.opt;
      const ok = q.chosen === right;
      if (ok) q.correct++;
      rate(it.tid, it.i, ok);
      store.save();
      quizPage(key, title, topics, limit, here);
    });
    const n = document.getElementById("next");
    if (n) n.onclick = () => { q.index++; q.chosen = null; store.save(); quizPage(key, title, topics, limit, here); window.scrollTo(0, 0); };
  });
}

// ---------------------------------------------------------------- search
function searchPage(term) {
  render(`${header("Search")}<input class="search" id="s" type="search" placeholder="Search notes, papers, cards…" value="${esc(term || "")}" autofocus>
    <div id="res"></div>`, () => {
    const box = document.getElementById("s");
    const run = () => {
      const w = box.value.trim().toLowerCase();
      history.replaceState(null, "", "#/search/" + encodeURIComponent(box.value));
      if (w.length < 2) { document.getElementById("res").innerHTML = '<p class="muted small">Type at least 2 letters.</p>'; return; }
      const hits = [];
      for (const u of DATA.units) for (const t of u.topics) {
        const text = [t.title, t.summary, ...t.notes.map((n) => n.text || (n.items || []).join(" ")), ...t.questions.map((q) => q.q), ...t.cards.map((c) => c.front + " " + c.back)].join(" \n ");
        const at = text.toLowerCase().indexOf(w);
        if (at >= 0) hits.push({ u, t, snip: text.slice(Math.max(0, at - 50), at + w.length + 70) });
      }
      const mark = (s) => esc(s).replace(new RegExp(w.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), "gi"), (m) => `<mark>${m}</mark>`);
      document.getElementById("res").innerHTML = hits.length ? `<div class="list">${hits.slice(0, 60).map((h) => `
        <button class="item" data-go="#/topic/${h.u.id}/${h.t.id}/notes"><span class="bar" style="background:${accentOf(h.u)}"></span>
        <div class="grow"><div class="title">${esc(h.t.title)}</div><div class="small muted">${esc(h.u.short || h.u.name)} · …${mark(h.snip)}…</div></div></button>`).join("")}</div>`
        : '<p class="muted">Nothing found.</p>';
      document.querySelectorAll("#res [data-go]").forEach((b) => b.addEventListener("click", () => go(b.dataset.go)));
    };
    box.addEventListener("input", run);
    run();
  });
}

// ---------------------------------------------------------------- router
function route() {
  const parts = location.hash.replace(/^#\/?/, "").split("/").map(decodeURIComponent);
  const [page, a, b, c] = parts;
  window.scrollTo(0, 0);
  switch (page) {
    case "unit": return unitPage(a, b);
    case "topic": return topicPage(a, b, c);
    case "uquiz": { const u = unitById(a); return u ? quizPage(`u:${a}`, `${u.short || u.name} quiz`, u.topics, 10) : go("#/"); }
    case "quick": return quizPage("quick5", "Quick 5", DATA.units.flatMap((u) => u.topics), 5);
    case "search": return searchPage(a);
    default: return home();
  }
}

async function start() {
  try {
    const [c, s, e] = await Promise.all(["data/content.json", "data/sketches.json", "data/exams.json"].map((f) => fetch(f).then((r) => r.json())));
    DATA = c; SKETCH = s; EXAMS = e;
  } catch (err) {
    app.innerHTML = '<p class="loading">Couldn\'t load the study content. Check your connection and reopen.</p>';
    return;
  }
  // A finished Quick 5 should start fresh next time.
  const qz = store.obj("quizzes");
  if (qz.quick5 && qz.quick5.index >= (qz.quick5.items || []).length) { delete qz.quick5; store.save(); }
  window.addEventListener("hashchange", route);
  route();
  if ("serviceWorker" in navigator) navigator.serviceWorker.register("sw.js").catch(() => {});
}
start();
