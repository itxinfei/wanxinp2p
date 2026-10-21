#!/bin/bash
# SCP helper: .joycode/ssh_scp.sh <local_file> <remote_path>
PASS_SCRIPT="$(cd "$(dirname "$0")" && pwd)/ssh_pass.sh"
chmod +x "$PASS_SCRIPT"

export SSH_ASKPASS="$PASS_SCRIPT"
export SSH_ASKPASS_REQUIRE=force
export DISPLAY=:0

HOST="xfwl@192.168.20.130"
scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null "$1" "$HOST:$2"