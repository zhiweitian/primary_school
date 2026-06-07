#!/usr/bin/env python3
import json
import os
from http.server import HTTPServer, SimpleHTTPRequestHandler
from urllib.parse import urlparse

ROOT = os.path.dirname(os.path.abspath(__file__))
STATS_PATH = os.path.join(ROOT, "data", "practice-stats.json")


class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=ROOT, **kwargs)

    def do_POST(self):
        if urlparse(self.path).path != "/api/practice-stats":
            self.send_error(404)
            return
        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length)
        try:
            data = json.loads(body.decode("utf-8"))
        except json.JSONDecodeError:
            self.send_error(400)
            return
        os.makedirs(os.path.dirname(STATS_PATH), exist_ok=True)
        with open(STATS_PATH, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            f.write("\n")
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(b'{"ok":true}')

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        super().end_headers()


if __name__ == "__main__":
    import sys

    port = int(os.environ.get("PORT", "8765"))
    try:
        server = HTTPServer(("localhost", port), Handler)
    except OSError as e:
        if e.errno == 48:
            print(f"端口 {port} 已被占用。查看占用：lsof -nP -iTCP:{port} -sTCP:LISTEN", file=sys.stderr)
            print(f"结束占用：kill $(lsof -t -iTCP:{port} -sTCP:LISTEN)", file=sys.stderr)
        raise SystemExit(1) from e
    base = f"http://localhost:{port}"
    links = [
        ("知识树", f"{base}/knowledge-tree/index.html"),
        ("认识近似数", f"{base}/apps/grade2/approx-number-basics.html"),
        ("万以内比大小", f"{base}/apps/grade2/compare-within-10000.html"),
    ]
    print(f"Serving {ROOT}", flush=True)
    print(f"  根目录  {base}/", flush=True)
    for name, url in links:
        print(f"  {name}  {url}", flush=True)
    server.serve_forever()
