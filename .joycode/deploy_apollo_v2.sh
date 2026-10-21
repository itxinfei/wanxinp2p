#!/bin/bash
# 部署Apollo 2.4.0官方SQL: 上传3个文件到服务器, sudo执行导入脚本
cd "$(dirname "$0")"
PASS_SCRIPT="$(pwd)/ssh_pass.sh"
chmod +x "$PASS_SCRIPT"
export SSH_ASKPASS="$PASS_SCRIPT"
export SSH_ASKPASS_REQUIRE=force
export DISPLAY=:0
HOST="xfwl@192.168.20.130"
SSH_OPTS="-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"

echo "=== 1. 上传SQL和导入脚本到服务器 ==="
scp $SSH_OPTS apolloconfigdb-v2.4.0.sql apolloportaldb-v2.4.0.sql remote_import_apollo.sh "$HOST:/tmp/"
if [ $? -ne 0 ]; then echo "SCP_FAILED"; exit 1; fi
echo "SCP_OK"

echo "=== 2. 远程执行导入+重启 ==="
ssh $SSH_OPTS "$HOST" "echo 123456 | sudo -S bash /tmp/remote_import_apollo.sh" 2>&1

echo "=== ALL_DONE ==="