#!/bin/bash
# 修复剩余4个表名(含MySQL保留字release需反引号)
MYSQL_CMD="docker exec mysql8 mysql -uroot -p123456"

echo "=== 修复剩余表名 ==="
$MYSQL_CMD apolloconfigdb -e "RENAME TABLE \`release\` TO \`Release\`;" 2>/dev/null && echo "  release->Release ok" || echo "  release失败"
$MYSQL_CMD apolloconfigdb -e "RENAME TABLE releasehistory TO ReleaseHistory;" 2>/dev/null && echo "  releasehistory->ReleaseHistory ok" || echo "  releasehistory失败"
$MYSQL_CMD apolloconfigdb -e "RENAME TABLE releasemessage TO ReleaseMessage;" 2>/dev/null && echo "  releasemessage->ReleaseMessage ok" || echo "  releasemessage失败"
$MYSQL_CMD apolloconfigdb -e "RENAME TABLE serverconfig TO ServerConfig;" 2>/dev/null && echo "  serverconfig->ServerConfig ok" || echo "  serverconfig失败"

echo "=== 验证表名 ==="
$MYSQL_CMD apolloconfigdb -e "SHOW TABLES;" 2>/dev/null

echo "=== 重启Apollo ==="
docker restart apollo-configservice apollo-adminservice apollo-portal
echo "=== 等待120秒 ==="
sleep 120
echo "=== 容器状态 ==="
docker ps --format "{{.Names}}|{{.Status}}" | grep apollo
echo "=== HTTP探测 ==="
curl -s -o /dev/null -w "configservice:%{http_code}\n" --max-time 5 http://192.168.20.130:8080
curl -s -o /dev/null -w "adminservice:%{http_code}\n" --max-time 5 http://192.168.20.130:8092
curl -s -o /dev/null -w "portal:%{http_code}\n" --max-time 5 http://192.168.20.130:8070