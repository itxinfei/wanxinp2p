#!/bin/bash
PASS_SCRIPT="$(cd "$(dirname "$0")" && pwd)/ssh_pass.sh"
export SSH_ASKPASS="$PASS_SCRIPT"
export SSH_ASKPASS_REQUIRE=force
export DISPLAY=:0

for i in 1 2 3 4 5 6 7 8; do
  echo "---尝试 $i ---"
  ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o ConnectTimeout=20 \
    xfwl@192.168.20.130 \
    "echo CONNECTED; echo 123456 | sudo -S docker stop apollo-configservice apollo-adminservice apollo-portal 2>/dev/null; echo APOLLO_STOPPED; free -h | head -2" 2>&1
  if [ $? -eq 0 ]; then
    echo "===成功连接并停止Apollo==="
    break
  fi
  echo "失败,等待30秒后重试"
  sleep 30
done