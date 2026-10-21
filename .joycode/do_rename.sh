#!/bin/bash
set -e
cd "$(dirname "$0")/.."

echo "=== Step 0: Clean build outputs ==="
for mod in xfjr-*; do
  [ -d "$mod/target" ] && rm -rf "$mod/target" && echo "  cleaned $mod/target"
done

echo ""
echo "=== Step 1: Replace file contents xfjr -> xfjr ==="
find . -type f \
  \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" \
     -o -name "*.properties" -o -name "*.js" -o -name "*.vue" \
     -o -name "*.json" -o -name "*.sh" -o -name "*.sql" -o -name "*.md" \
     -o -name "*.txt" -o -name "*.bat" -o -name "*.html" -o -name "*.css" \
     -o -name "*.scss" -o -name "*.factories" -o -name "*.imports" \
     -o -name ".env*" -o -name ".gitignore" -o -name "Dockerfile" \) \
  -not -path "*/target/*" -not -path "*/node_modules/*" -not -path "*/.git/*" \
  -not -path "*/unpackage/*" -not -name "package-lock.json" \
  -print0 | xargs -0 sed -i 's/xfjr/xfjr/g' 2>/dev/null
echo "  content replacement done"

echo ""
echo "=== Step 1b: Replace hyphen variant xinfei-p2p -> xfjr ==="
find . -type f \
  \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.sql" \) \
  -not -path "*/target/*" -not -path "*/node_modules/*" -not -path "*/.git/*" \
  -print0 | xargs -0 sed -i 's/xinfei-p2p/xfjr/g' 2>/dev/null
echo "  hyphen variant done"

echo ""
echo "=== Step 2: Move Java package directories ==="
find . -depth -type d -name "xfjr" \
  -not -path "*/target/*" -not -path "*/node_modules/*" | while read d; do
  parent=$(dirname "$d")
  mv "$d" "$parent/xfjr"
  echo "  $d -> $parent/xfjr"
done

echo ""
echo "=== Step 3: Rename top-level module directories ==="
for dir in xfjr-*; do
  newdir="${dir/xfjr/xfjr}"
  mv "$dir" "$newdir"
  echo "  $dir -> $newdir"
done

echo ""
echo "=== Verify: remaining xfjr references ==="
remaining=$(grep -rl "xfjr" --include="*.java" --include="*.xml" --include="*.yml" --include="*.json" --include="*.sh" . 2>/dev/null | grep -v target | grep -v node_modules | grep -v package-lock | grep -v .joycode | wc -l)
echo "  Remaining files with xfjr: $remaining"

echo ""
echo "=== New directory structure ==="
ls -d xfjr-* 2>/dev/null