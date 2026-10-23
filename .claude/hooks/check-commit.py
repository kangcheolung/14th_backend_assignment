#!/usr/bin/env python3
"""PreToolUse(Bash): git commit 직전 검사.

1. 스테이징된 파일 중에 비밀 파일(.env, 키 파일 등)이 있으면 차단
2. -m 으로 준 커밋 메시지의 첫 줄이 `타입: 요약` 또는 `타입(범위): 요약` 형식이 아니면 차단
   (타입: feat, fix, chore, refactor, docs, test, style, perf, build, ci, revert)
   -m 이 없거나(편집기 사용) 변수로 넘기는 경우는 검사하지 않는다.
"""
import os
import re
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _lib import block, command_of, is_secret_path, load  # noqa: E402

TYPES = "feat|fix|chore|refactor|docs|test|style|perf|build|ci|revert"

data = load()
command = command_of(data)
if not re.search(r"\bgit\s+(?:-C\s+\S+\s+)?commit\b", command):
    sys.exit(0)
cwd = data.get("cwd") or "."

# 1. 스테이징된 비밀 파일
try:
    staged = subprocess.run(
        ["git", "-C", cwd, "diff", "--cached", "--name-only"],
        capture_output=True, text=True, timeout=10,
    ).stdout.splitlines()
except Exception:
    staged = []
leaked = [f for f in staged if is_secret_path(f)]
if leaked:
    block("비밀 파일이 스테이징돼 있어요: " + ", ".join(leaked) + " — `git restore --staged <파일>` 로 빼고 다시 커밋해요.")

# 2. 커밋 메시지 형식
m = re.search(r"(?:^|\s)(?:-[a-zA-Z]*m|--message)(?:\s+|=)(.*)", command, re.S)
if m:
    rest = m.group(1).lstrip()
    # git commit -m "$(cat <<'EOF' ... 형태에서 heredoc 껍데기를 벗긴다.
    rest = re.sub(r"""^["']?\$\(\s*cat\s*<<-?\s*['"]?\w+['"]?\s*\n?""", "", rest)
    body = rest.lstrip("\"' \n")
    if not body.startswith("$"):  # 변수나 명령 치환으로 넘긴 메시지는 검사할 수 없어서 건너뛴다.
        first = body.split("\n", 1)[0]
        if not re.match(rf"({TYPES})(\([^)]+\))?!?: \S", first):
            block(
                "커밋 메시지는 `타입: 요약` 형식이어야 해요 "
                "(feat, fix, chore, refactor, docs, test, style, perf, build, ci, revert). "
                f"현재 첫 줄: {first[:60]!r}"
            )

sys.exit(0)
