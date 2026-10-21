#!/bin/bash
# RocketMQ 部署脚本 (仅RocketMQ, Apollo/DB/ES已完成)
# 服务器: 192.168.20.130

echo "=== 拉取RocketMQ镜像 ==="
docker pull apache/rocketmq:5.3.1
if [ $? -ne 0 ]; then
  echo "!!! 镜像拉取失败,重试一次"
  docker pull apache/rocketmq:5.3.1
fi

echo "=== 启动NameServer ==="
docker rm -f rmqnamesrv 2>/dev/null
docker run -d --name rmqnamesrv \
  --network xfjr-net --restart unless-stopped \
  -p 9876:9876 \
  apache/rocketmq:5.3.1 sh mqnamesrv

echo "=== 生成broker.conf ==="
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

echo "=== 启动Broker ==="
docker rm -f rmqbroker 2>/dev/null
docker run -d --name rmqbroker \
  --network xfjr-net --restart unless-stopped \
  -p 10911:10911 -p 10909:10909 \
  -v /tmp/broker.conf:/home/rocketmq/broker.conf \
  apache/rocketmq:5.3.1 sh mqbroker -c /home/rocketmq/broker.conf

echo "=== 等待启动(15秒) ==="
sleep 15
echo "=== 容器状态 ==="
docker ps --format "{{.Names}}|{{.Status}}" | grep -E "rmq|apollo|elastic"
echo "=== Broker注册检查 ==="
docker exec rmqbroker sh -c "cd /home/rocketmq/rocketmq-5.3.1/bin && sh mqadmin clusterList -n 192.168.20.130:9876 2>/dev/null" || echo "Broker尚未注册,需等待"
echo "=== RocketMQ部署完成 ==="