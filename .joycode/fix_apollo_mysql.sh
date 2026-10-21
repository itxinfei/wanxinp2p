#!/bin/bash
# 修复Apollo连接MySQL: mysql8加入xfjr-net, Apollo用容器名连接
SERVER_IP="192.168.20.130"
MYSQL_PASS="123456"
JDBC_PARAMS="characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"

echo "=== 1. mysql8加入xfjr-net ==="
docker network connect xfjr-net mysql8 2>/dev/null || echo "  已在网络中"
docker network inspect xfjr-net --format '{{range .Containers}}{{.Name}} {{end}}'

echo "=== 2. 测试连通性(从apollo容器) ==="
docker exec apollo-configservice sh -c "timeout 3 bash -c 'echo > /dev/tcp/mysql8/3306' 2>/dev/null && echo 'TCP mysql8:3306 连通' || echo 'TCP 不通'"

echo "=== 3. 重建Apollo三件套(容器名连MySQL) ==="
docker rm -f apollo-configservice apollo-adminservice apollo-portal 2>/dev/null

docker run -d --name apollo-configservice \
  --network xfjr-net --restart unless-stopped \
  -p 8080:8080 \
  -e HOST_IP=$SERVER_IP \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql8:3306/apolloconfigdb?$JDBC_PARAMS" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=$MYSQL_PASS \
  apolloconfig/apollo-configservice:2.4.0

docker run -d --name apollo-adminservice \
  --network xfjr-net --restart unless-stopped \
  -p 8092:8092 \
  -e HOST_IP=$SERVER_IP \
  -e SERVER_PORT=8092 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql8:3306/apolloconfigdb?$JDBC_PARAMS" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=$MYSQL_PASS \
  apolloconfig/apollo-adminservice:2.4.0

docker run -d --name apollo-portal \
  --network xfjr-net --restart unless-stopped \
  -p 8070:8070 \
  -e HOST_IP=$SERVER_IP \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql8:3306/apolloportaldb?$JDBC_PARAMS" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=$MYSQL_PASS \
  -e APOLLO_PORTAL_ENVS=dev \
  -e DEV_META=http://$SERVER_IP:8080 \
  apolloconfig/apollo-portal:2.4.0

echo "=== Apollo重建完成,等待90秒启动 ==="
sleep 90
echo "=== 容器状态 ==="
docker ps --format "{{.Names}}|{{.Status}}" | grep -E "apollo"
echo "=== HTTP探测 ==="
curl -s -o /dev/null -w "configservice:%{http_code}\n" --max-time 5 http://$SERVER_IP:8080 || echo "configservice:不通"
curl -s -o /dev/null -w "adminservice:%{http_code}\n" --max-time 5 http://$SERVER_IP:8092 || echo "adminservice:不通"
curl -s -o /dev/null -w "portal:%{http_code}\n" --max-time 5 http://$SERVER_IP:8070 || echo "portal:不通"