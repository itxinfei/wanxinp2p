#!/bin/bash
# 评估 xfjr 改名范围
cd "$(dirname "$0")/.."

echo "=== 1. 顶层目录 ==="
ls -d xfjr-* 2>/dev/null

echo ""
echo "=== 2. Java包目录结构 ==="
find . -type d -name "xfjr" -not -path "*/target/*" -not -path "*/node_modules/*" 2>/dev/null
find . -type d -path "*com/xfjr*" -not -path "*/target/*" 2>/dev/null | head -5
find . -type d -path "*cn/itcast/xfjr*" -not -path "*/target/*" 2>/dev/null | head -5

echo ""
echo "=== 3. 包含xfjr的文件数(按类型) ==="
for ext in java xml yml properties js vue json sh; do
  cnt=$(find . -type f -name "*.$ext" -not -path "*/target/*" -not -path "*/node_modules/*" -not -name "package-lock.json" 2>/dev/null | xargs grep -l "xfjr" 2>/dev/null | wc -l)
  echo "  .$ext: $cnt"
done

echo ""
echo "=== 4. 大写变体检查 ==="
grep -rl "Xinfeip2p\|XINFEIP2P" --include="*.java" --include="*.xml" . 2>/dev/null | grep -v target | head -10

echo ""
echo "=== 5. xinfei-p2p 连字符变体 ==="
grep -rl "xinfei-p2p" --include="*.java" --include="*.xml" --include="*.yml" --include="*.sql" . 2>/dev/null | grep -v target | head -10

echo ""
echo "=== 6. pom.xml中的artifactId ==="
grep -h "xfjr" pom.xml */pom.xml 2>/dev/null | sort -u | head -20