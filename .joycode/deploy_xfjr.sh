#!/bin/bash
# P2P 中间件部署脚本
# 服务器: 192.168.20.130
# 复用: MySQL8(3306/root:123456), Redis7(6379), MinIO(9005)
# 新部署: Apollo(8080/8090/8070), RocketMQ(9876/10911), ES(9200)
# 容器名使用默认名,不加前缀

SERVER_IP="192.168.20.130"
MYSQL_PASS="123456"
JDBC_PARAMS="characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"

echo "=== 0. 创建Docker网络 ==="
docker network create xfjr-net 2>/dev/null || echo "  网络已存在"

echo "=== 1. 导入数据库 ==="
sed -i 's/\r$//' /tmp/p2p-init.sql /tmp/depository-init.sql /tmp/p2p_apollo.sql

docker cp /tmp/p2p-init.sql mysql8:/tmp/p2p-init.sql
docker exec mysql8 sh -c "mysql -uroot -p$MYSQL_PASS < /tmp/p2p-init.sql" 2>/dev/null
echo "  P2P业务库导入完成"

docker cp /tmp/depository-init.sql mysql8:/tmp/depository-init.sql
docker exec mysql8 sh -c "mysql -uroot -p$MYSQL_PASS < /tmp/depository-init.sql" 2>/dev/null
echo "  存管库导入完成"

docker cp /tmp/p2p_apollo.sql mysql8:/tmp/p2p_apollo.sql
docker exec mysql8 sh -c "mysql -uroot -p$MYSQL_PASS < /tmp/p2p_apollo.sql" 2>/dev/null
echo "  Apollo库导入完成"

echo "  --- 数据库列表 ---"
docker exec mysql8 mysql -uroot -p$MYSQL_PASS -e "SHOW DATABASES;" 2>/dev/null | grep -E "p2p_|apollo"

echo ""
echo "=== 2. 部署Apollo (默认名) ==="
# 8090被xxl-job-admin占用,adminservice内部端口改为8092
docker pull apolloconfig/apollo-configservice:2.4.0 2>&1 | tail -1
docker run -d --name apollo-configservice \
  --network xfjr-net --restart unless-stopped \
  -p 8080:8080 \
  -e HOST_IP=$SERVER_IP \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://$SERVER_IP:3306/apolloconfigdb?$JDBC_PARAMS" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=$MYSQL_PASS \
  apolloconfig/apollo-configservice:2.4.0

docker pull apolloconfig/apollo-adminservice:2.4.0 2>&1 | tail -1
docker run -d --name apollo-adminservice \
  --network xfjr-net --restart unless-stopped \
  -p 8092:8092 \
  -e HOST_IP=$SERVER_IP \
  -e SERVER_PORT=8092 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://$SERVER_IP:3306/apolloconfigdb?$JDBC_PARAMS" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=$MYSQL_PASS \
  apolloconfig/apollo-adminservice:2.4.0

docker pull apolloconfig/apollo-portal:2.4.0 2>&1 | tail -1
docker run -d --name apollo-portal \
  --network xfjr-net --restart unless-stopped \
  -p 8070:8070 \
  -e HOST_IP=$SERVER_IP \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://$SERVER_IP:3306/apolloportaldb?$JDBC_PARAMS" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=$MYSQL_PASS \
  -e APOLLO_PORTAL_ENVS=dev \
  -e DEV_META=http://$SERVER_IP:8080 \
  apolloconfig/apollo-portal:2.4.0
echo "  Apollo部署完成 (config:8080, admin:8092, portal:8070)"

echo ""
echo "=== 3. 部署RocketMQ (默认名) ==="
docker pull apache/rocketmq:5.3.1 2>&1 | tail -1

docker run -d --name rmqnamesrv \
  --network xfjr-net --restart unless-stopped \
  -p 9876:9876 \
  apache/rocketmq:5.3.1 sh mqnamesrv

cat > /tmp/broker.conf << 'BROKEREOF'
brokerClusterName=DefaultCluster
brokerName=broker-a
brokerId=0
deleteWhen=04
fileReservedTime=48
brokerRole=ASYNC_MASTER
flushDiskType=ASYNC_FLUSH
brokerIP1=192.168.20.130
namesrvAddr=192.168.20.130:9876
autoCreateTopicEnable=true
autoCreateSubscriptionGroup=true
BROKEREOF

docker run -d --name rmqbroker \
  --network xfjr-net --restart unless-stopped \
  -p 10911:10911 -p 10909:10909 \
  -v /tmp/broker.conf:/home/rocketmq/broker.conf \
  apache/rocketmq:5.3.1 sh mqbroker -c /home/rocketmq/broker.conf
echo "  RocketMQ部署完成 (namesrv:9876, broker:10911)"

echo ""
echo "=== 4. 部署Elasticsearch (默认名) ==="
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.17.0 2>&1 | tail -1
docker run -d --name elasticsearch \
  --network xfjr-net --restart unless-stopped \
  -p 9200:9200 -p 9300:9300 \
  -e discovery.type=single-node \
  -e xpack.security.enabled=false \
  -e ES_JAVA_OPTS="-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.0
echo "  Elasticsearch部署完成 (9200)"

echo ""
echo "=== 部署完成! ==="
sleep 5
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "apollo|rmq|elastic|mysql8|redis"