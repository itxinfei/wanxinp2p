#!/bin/bash
# 用正确URL下载Apollo 2.4.0官方SQL并重新导入
BASE="https://raw.githubusercontent.com/apolloconfig/apollo/v2.4.0/scripts/sql/src"

echo "=== 1. 下载官方SQL(正确路径) ==="
curl -sL --max-time 120 -o /tmp/apolloconfigdb.sql "$BASE/apolloconfigdb.sql"
curl -sL --max-time 120 -o /tmp/apolloportaldb.sql "$BASE/apolloportaldb.sql"
echo "configdb: $(wc -c < /tmp/apolloconfigdb.sql 2>/dev/null || echo 0) bytes"
echo "portaldb: $(wc -c < /tmp/apolloportaldb.sql 2>/dev/null || echo 0) bytes"

if [ "$(wc -c < /tmp/apolloconfigdb.sql 2>/dev/null || echo 0)" -lt 5000 ]; then
  echo "下载失败!"
  exit 1
fi

echo "=== 2. 删库重新导入 ==="
docker exec mysql8 mysql -uroot -p123456 -e "DROP DATABASE IF EXISTS apolloconfigdb; DROP DATABASE IF EXISTS apolloportaldb;" 2>/dev/null
docker cp /tmp/apolloconfigdb.sql mysql8:/tmp/apolloconfigdb.sql
docker cp /tmp/apolloportaldb.sql mysql8:/tmp/apolloportaldb.sql
docker exec mysql8 sh -c "mysql -uroot -p123456 < /tmp/apolloconfigdb.sql" 2>/dev/null
docker exec mysql8 sh -c "mysql -uroot -p123456 < /tmp/apolloportaldb.sql" 2>/dev/null
echo "导入完成"

echo "=== 3. 验证 ==="
docker exec mysql8 mysql -uroot -p123456 apolloconfigdb -e "SHOW TABLES;" 2>/dev/null
docker exec mysql8 mysql -uroot -p123456 apolloportaldb -e "SHOW TABLES;" 2>/dev/null

echo "=== 4. 重启Apollo ==="
docker restart apollo-configservice apollo-adminservice apollo-portal
echo "等待120秒..."
sleep 120
echo "=== 容器状态 ==="
docker ps --format "{{.Names}}|{{.Status}}" | grep apollo
echo "=== HTTP探测 ==="
curl -s -o /dev/null -w "configservice:%{http_code}\n" --max-time 5 http://192.168.20.130:8080
curl -s -o /dev/null -w "adminservice:%{http_code}\n" --max-time 5 http://192.168.20.130:8092
curl -s -o /dev/null -w "portal:%{http_code}\n" --max-time 5 http://192.168.20.130:8070