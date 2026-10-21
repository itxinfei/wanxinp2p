#!/bin/bash
# Apollo configservice端口8080->28080, portal DEV_META同步更新
JDBC_PARAMS="characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"

echo "=== 1. 停止并删除 apollo-configservice ==="
docker stop apollo-configservice && docker rm apollo-configservice

echo "=== 2. 重建 apollo-configservice (28080:8080) ==="
docker run -d --name apollo-configservice \
  --network xfjr-net --restart unless-stopped \
  -p 28080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql8:3306/apolloconfigdb?$JDBC_PARAMS" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=123456 \
  -e HOST_IP=192.168.20.130 \
  apolloconfig/apollo-configservice:2.4.0

echo "=== 3. 停止并删除 apollo-portal ==="
docker stop apollo-portal && docker rm apollo-portal

echo "=== 4. 重建 apollo-portal (DEV_META=28080) ==="
docker run -d --name apollo-portal \
  --network xfjr-net --restart unless-stopped \
  -p 8070:8070 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql8:3306/apolloportaldb?$JDBC_PARAMS" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=123456 \
  -e APOLLO_PORTAL_ENVS=dev \
  -e DEV_META=http://192.168.20.130:28080 \
  -e HOST_IP=192.168.20.130 \
  apolloconfig/apollo-portal:2.4.0

echo "=== 5. 重启 adminservice (重新发现configservice) ==="
docker restart apollo-adminservice

echo "PORT_CHANGE_DONE"