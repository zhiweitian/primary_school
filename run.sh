#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

PORT="${PORT:-8765}"
URL="http://localhost:${PORT}/knowledge-tree/index.html"

if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "端口 $PORT 已被占用，直接打开浏览器…"
  open "$URL" 2>/dev/null || xdg-open "$URL" 2>/dev/null || true
  exit 0
fi

echo "启动服务：$URL"
open "$URL" 2>/dev/null || xdg-open "$URL" 2>/dev/null || true
exec python3 serve.py
