"""Builds demo/index.html: the real UI plus the in-browser API stand-in (for the published demo only)."""
import pathlib
root = pathlib.Path(__file__).resolve().parent.parent
html = (root / "src/main/resources/static/index.html").read_text(encoding="utf-8")
mock = (root / "demo/mock-api.js").read_text(encoding="utf-8")
(root / "demo/index.html").write_text(html.replace("<script>\n", "<script>\n" + mock + "\n</script>\n<script>\n", 1), encoding="utf-8")
print("demo/index.html written")
