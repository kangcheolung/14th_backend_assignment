#!/usr/bin/env python3
"""PreToolUse(Read|Edit|MultiEdit|Write|Bash): 비밀 파일 접근 차단.

차단 대상: .env, .env.* (견본 .env.example 등은 허용), *.pem, *.key, id_rsa 류, credentials.json, ~/.ssh/ 등
  - 파일 도구: tool_input.file_path 로 판단
  - Bash: 명령에 비밀 파일 경로가 들어 있으면 차단 (ls, stat 처럼 내용을 읽지 않는 명령은 허용)
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _lib import block, command_of, file_path_of, is_secret_path, load, segments  # noqa: E402

READ_ONLY_META = {"ls", "stat", "file", "test", "[", "[[", "wc", "du"}

data = load()
tool = data.get("tool_name", "")

if tool == "Bash":
    command = command_of(data)
    # .gitignore 에 .env 를 적어 넣는 작업이나 ignore 여부 확인은 허용
    if ".gitignore" in command or "check-ignore" in command:
        sys.exit(0)
    for seg in segments(command):
        if not seg or os.path.basename(seg[0]) in READ_ONLY_META:
            continue
        for tok in seg:
            cleaned = tok.strip("\"'`;()<>$")
            cleaned = re.sub(r"^[A-Za-z_]+=", "", cleaned)  # VAR=.env 형태
            if is_secret_path(cleaned):
                block(f"비밀 파일 '{cleaned}' 은 접근할 수 없어요. 값이 필요하면 사용자에게 직접 알려 달라고 해요.")
else:
    path = file_path_of(data)
    if is_secret_path(path):
        block(f"비밀 파일 '{os.path.basename(path)}' 은 읽거나 수정할 수 없어요. 값이 필요하면 사용자에게 직접 알려 달라고 해요.")

sys.exit(0)
