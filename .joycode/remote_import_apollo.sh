#!/bin/bash
# 服务器端执行: 删库重建+导入Apollo 2.4.0官方SQL+重启Apollo
MYSQL_EXEC="docker exec mysql8 mysql -uroot -p123456"
MYSQL_IMPORT="docker exec -i mysql8 mysql -uroot -p123456 --default-character-set=utf8mb4"

echo "--- 删除并重建 apolloconfigdb ---"
$MYSQL_EXEC -e "DROP DATABASE IF EXISTS apolloconfigdb; CREATE DATABASE apolloconfigdb DEFAULT CHARACTER SET utf8mb4;"
echo "--- 导入 apolloconfigdb-v2.4.0.sql ---"
$MYSQL_IMPORT apolloconfigdb < /tmp/apolloconfigdb-v2.4.0.sql
echo "--- 删除并重建 apolloportaldb ---"
$MYSQL_EXEC -e "DROP DATABASE IF EXISTS apolloportaldb; CREATE DATABASE apolloportaldb DEFAULT CHARACTER SET utf8mb4;"
echo "--- 导入 apolloportaldb-v2.4.0.sql ---"
$MYSQL_IMPORT apolloportaldb < /tmp/apolloportaldb-v2.4.0.sql
echo "--- 验证表数量 ---"
$MYSQL_EXEC -e "SELECT COUNT(*) AS cfg_tables FROM information_schema.tables WHERE table_schema='apolloconfigdb'; SELECT COUNT(*) AS portal_tables FROM information_schema.tables WHERE table_schema='apolloportaldb';"
echo "--- 重启Apollo三件套 ---"
docker restart apollo-configservice apollo-adminservice apollo-portal
echo "REMOTE_DONE"