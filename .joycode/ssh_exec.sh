#!/bin/bash
# SSH helper: .joycode/ssh_exec.sh "remote command"
PASS_SCRIPT="$(cd "$(dirname "$0")" && pwd)/ssh_pass.sh"
chmod +x "$PASS_SCRIPT"

export SSH_ASKPASS="$PASS_SCRIPT"
export SSH_ASKPASS_REQUIRE=force
export DISPLAY=:0

HOST="xfwl@192.168.20.130"
ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null "$HOST" "$1"