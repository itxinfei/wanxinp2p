#!/bin/bash
# 服务器端全量健康验证: Apollo+RocketMQ+ES+MySQL
echo "=== 1. Apollo HTTP ==="
curl -s -o /dev/null -w 'configservice(8080): %{http_code}\n' http://localhost:8080/
curl -s -o /dev/null -w 'adminservice(8092): %{http_code}\n' http://localhost:8092/
curl -s -o /dev/null -w 'portal(8070): %{http_code}\n' http://localhost:8070/

echo ""
echo "=== 2. Apollo Eureka服务注册 ==="
curl -s http://localhost:8080/eureka/apps | grep -oE '<app>[^<]+</app>|<status>[^<]+</status>|<instanceId>[^<]+</instanceId>' | head -6

echo ""
echo "=== 3. RocketMQ集群状态 ==="
docker exec rmqbroker /home/rocketmq/rocketmq-5.3.1/bin/mqadmin clusterList -n 192.168.20.130:9876 2>/dev/null | grep -E 'broker-a|DefaultCluster|#BID'

echo ""
echo "=== 4. ES集群健康 ==="
curl -s http://localhost:9200/_cluster/health
echo ""

echo ""
echo "=== 5. MySQL数据库列表 ==="
docker exec mysql8 mysql -uroot -p123456 -e "SHOW DATABASES" 2>/dev/null

echo "=== 6. Apollo ServerConfig抽样 ==="
docker exec mysql8 mysql -uroot -p123456 apolloconfigdb -e "SELECT \`Key\`,\`Value\` FROM \`ServerConfig\` LIMIT 3" 2>/dev/null

echo ""
echo "VERIFY_ALL_DONE"