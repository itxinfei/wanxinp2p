#!/bin/bash
cd "$(dirname "$0")/.."
echo "=== remaining xinfeip2p dirs ==="
find . -type d -name "xinfeip2p" -not -path "*/target/*" -not -path "*/node_modules/*" -not -path "*/.git/*" 2>/dev/null
echo "=== cn/itcast dirs ==="
find . -type d -path "*cn/itcast*" -not -path "*/target/*" -not -path "*/node_modules/*" -not -path "*/.git/*" -maxdepth 8 2>/dev/null | head -20
echo "=== done ==="