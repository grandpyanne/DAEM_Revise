"""Refreshes the web version of DAEM Revise (the docs/ folder, served by GitHub Pages).

Run it after tools/build_content.py, from the project folder:

    python tools/build_web.py

It copies the same content the Android app uses (content.json and the question figures),
the note diagrams from tools/sketches.py, the exam timetable from data/Exams.kt and the
Kapi drawings, then gives the offline cache a new version so phones pick up the update.
Nothing in docs/data, docs/figures or docs/img should be edited by hand.
"""
import hashlib
import json
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
APP = ROOT / "app" / "src" / "main"
WEB = ROOT / "docs"
ANDROID = "{http://schemas.android.com/apk/res/android}"

# Grandpyanne™


def vector_to_svg(xml_path: Path) -> str:
    """Turns an Android vector drawable (paths and groups) into an SVG."""
    root = ET.parse(xml_path).getroot()
    vw = root.get(ANDROID + "viewportWidth")
    vh = root.get(ANDROID + "viewportHeight")

    def color(c):
        if not c:
            return None, 1.0
        c = c.strip()
        if c.startswith("#") and len(c) == 9:  # #AARRGGBB
            return "#" + c[3:], int(c[1:3], 16) / 255
        return c, 1.0

    def walk(node):
        out = []
        for child in node:
            tag = child.tag
            if tag == "path":
                attrs = [f'd="{child.get(ANDROID + "pathData")}"']
                fill, fa = color(child.get(ANDROID + "fillColor"))
                attrs.append(f'fill="{fill}"' if fill else 'fill="none"')
                if fill and fa < 1:
                    attrs.append(f'fill-opacity="{fa:.3f}"')
                if child.get(ANDROID + "fillAlpha"):
                    attrs.append(f'fill-opacity="{child.get(ANDROID + "fillAlpha")}"')
                stroke, sa = color(child.get(ANDROID + "strokeColor"))
                if stroke:
                    attrs.append(f'stroke="{stroke}"')
                    if sa < 1:
                        attrs.append(f'stroke-opacity="{sa:.3f}"')
                    attrs.append(f'stroke-width="{child.get(ANDROID + "strokeWidth") or 1}"')
                    if child.get(ANDROID + "strokeLineCap"):
                        attrs.append(f'stroke-linecap="{child.get(ANDROID + "strokeLineCap")}"')
                    if child.get(ANDROID + "strokeLineJoin"):
                        attrs.append(f'stroke-linejoin="{child.get(ANDROID + "strokeLineJoin")}"')
                out.append(f"<path {' '.join(attrs)}/>")
            elif tag == "group":
                tx = child.get(ANDROID + "translateX") or "0"
                ty = child.get(ANDROID + "translateY") or "0"
                sx = child.get(ANDROID + "scaleX") or "1"
                sy = child.get(ANDROID + "scaleY") or "1"
                rot = child.get(ANDROID + "rotation") or "0"
                px = child.get(ANDROID + "pivotX") or "0"
                py = child.get(ANDROID + "pivotY") or "0"
                tr = f"translate({tx} {ty}) translate({px} {py}) rotate({rot}) scale({sx} {sy}) translate(-{px} -{py})"
                out.append(f'<g transform="{tr}">{"".join(walk(child))}</g>')
        return out

    body = "".join(walk(root))
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {vw} {vh}">{body}</svg>'


def exams() -> list:
    """The exam timetable, read from data/Exams.kt so there is only one place to edit it."""
    src = (APP / "java/com/grandpyanne/daemrevise/data/Exams.kt").read_text(encoding="utf-8")
    ym = re.search(r"startMillis = at\((\d{4}), (\d{1,2}), day", src)
    year, month = int(ym.group(1)), int(ym.group(2))
    out = []
    for m in re.finditer(r'paper\("([^"]+)", "([^"]+)", (\d+), morning = (true|false), venue = "([^"]*)"\)', src):
        title, unit, day, morning, venue = m.groups()
        start = 9 if morning == "true" else 14
        out.append({
            "title": title, "unit": unit, "venue": venue,
            # Kenya time (UTC+3), written as ISO strings with the offset.
            "start": f"{year:04d}-{month:02d}-{int(day):02d}T{start:02d}:00:00+03:00",
            "end": f"{year:04d}-{month:02d}-{int(day):02d}T{start + 3:02d}:00:00+03:00",
        })
    return out


def main():
    if not WEB.exists():
        sys.exit("docs/ is missing: it holds the web app (index.html, app.js, style.css, sw.js).")
    data = WEB / "data"
    data.mkdir(exist_ok=True)

    # 1. Study content: the very same file the Android app reads.
    shutil.copyfile(APP / "assets" / "content.json", data / "content.json")

    # 2. Note diagrams and topic sketches, from tools/sketches.py.
    sk = data / "sketches.json"
    subprocess.run([sys.executable, str(ROOT / "tools" / "sketches.py"), str(sk)], check=True, stdout=subprocess.DEVNULL)

    # 3. Exam timetable.
    (data / "exams.json").write_text(json.dumps(exams(), ensure_ascii=False, indent=1), encoding="utf-8")

    # 4. Question figures.
    figs = WEB / "figures"
    figs.mkdir(exist_ok=True)
    for png in (APP / "assets" / "figures").glob("*.png"):
        shutil.copyfile(png, figs / png.name)

    # 5. Kapi and the icon, as SVG.
    img = WEB / "img"
    img.mkdir(exist_ok=True)
    for name in ("mascot_happy", "mascot_sleepy", "ic_launcher", "hot_spring"):
        (img / f"{name}.svg").write_text(vector_to_svg(APP / "res" / "drawable" / f"{name}.xml"), encoding="utf-8")

    # 6. A new cache version whenever anything changed, so phones update on their next visit.
    h = hashlib.sha256()
    files = sorted(p for p in WEB.rglob("*") if p.is_file() and p.name != "sw.js")
    for p in files:
        h.update(p.relative_to(WEB).as_posix().encode())
        h.update(p.read_bytes())
    version = h.hexdigest()[:10]
    listing = [p.relative_to(WEB).as_posix() for p in files if p.name not in ("CNAME", ".nojekyll")]
    sw = (WEB / "sw.js").read_text(encoding="utf-8")
    sw = re.sub(r'const VERSION = "[^"]*";', f'const VERSION = "{version}";', sw)
    sw = re.sub(r"const FILES = \[[^\]]*\];", "const FILES = " + json.dumps(["./"] + listing) + ";", sw, flags=re.S)
    (WEB / "sw.js").write_text(sw, encoding="utf-8")
    print(f"Web app refreshed: {len(listing)} files, cache version {version}")


if __name__ == "__main__":
    main()
