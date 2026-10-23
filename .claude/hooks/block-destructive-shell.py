#!/usr/bin/env python3
"""PreToolUse(Bash): 되돌릴 수 없는 삭제 명령 차단.

차단 대상
  - rm -r 계열로 /, /*, ~, ~/*, $HOME 같은 최상위/홈 경로를 지우는 명령
  - rm --no-preserve-root
  - DROP TABLE / DROP DATABASE (mysql -e, psql -c 등에 담긴 SQL 포함)
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _lib import block, command_of, load, segments  # noqa: E402

data = load()
command = command_of(data)
if not command:
    sys.exit(0)

HOME = os.path.expanduser("~").rstrip("/")
DANGEROUS_TARGETS = {
    "/", "/*", "~", "~/", "~/*", "~/.*",
    "$HOME", "$HOME/", "$HOME/*", "${HOME}", "${HOME}/", "${HOME}/*",
    HOME, HOME + "/", HOME + "/*",
}

for seg in segments(command):
    # sudo rm, command rm, env X=1 rm 같은 앞붙이를 건너뛴다.
    idx = None
    for i, tok in enumerate(seg[:4]):
        if os.path.basename(tok) == "rm":
            idx = i
            break
    if idx is None:
        continue
    args = seg[idx + 1:]
    if "--no-preserve-root" in args:
        block("rm --no-preserve-root 는 금지예요.")
    recursive = any(
        a == "--recursive" or (a.startswith("-") and not a.startswith("--") and ("r" in a[1:] or "R" in a[1:]))
        for a in args
    )
    if not recursive:
        continue
    targets = [a for a in args if not a.startswith("-")]
    for t in targets:
        if t in DANGEROUS_TARGETS:
            block(f"rm -r 로 '{t}' 를 지우는 건 금지예요. 지울 파일이나 폴더를 구체적으로 지정해요.")

if re.search(r"\bdrop\s+(table|database)\b", command, re.IGNORECASE):
    block("DROP TABLE / DROP DATABASE 는 금지예요. 필요하면 사용자에게 먼저 확인받고, 마이그레이션으로 처리해요.")

sys.exit(0)
