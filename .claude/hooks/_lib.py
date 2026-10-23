"""훅 스크립트 공통 함수.

Claude Code는 훅을 실행할 때 stdin으로 JSON을 넘긴다.
  - Bash 명령: tool_input.command
  - 파일 도구(Read/Edit/Write): tool_input.file_path
  - 현재 작업 폴더: cwd
exit 2 로 끝내면 도구 실행이 차단되고, stderr 내용이 Claude에게 전달된다.
"""
import json
import os
import re
import shlex
import subprocess
import sys


def load():
    """stdin JSON을 읽는다. 읽을 수 없으면 빈 dict (훅이 깨져도 작업을 막지 않는다)."""
    try:
        return json.load(sys.stdin)
    except Exception:
        return {}


def block(message):
    """차단한다. stderr 메시지가 Claude에게 전달돼서 다른 방법을 찾게 된다."""
    sys.stderr.write("🚫 " + message + "\n")
    sys.exit(2)


def command_of(data):
    return (data.get("tool_input") or {}).get("command", "") or ""


def file_path_of(data):
    ti = data.get("tool_input") or {}
    return ti.get("file_path") or ti.get("path") or ti.get("notebook_path") or ""


def segments(command):
    """셸 연산자(&&, ||, ;, |)로 나눈 명령 조각을 토큰 리스트로 돌려준다."""
    try:
        lex = shlex.shlex(command, posix=True, punctuation_chars=True)
        lex.whitespace_split = True
        toks = list(lex)
    except ValueError:
        # 따옴표가 안 닫힌 경우(heredoc 등)는 공백 기준으로만 나눈다.
        toks = command.split()
    segs, cur = [], []
    for t in toks:
        if t and set(t) <= set("&|;()"):
            if cur:
                segs.append(cur)
            cur = []
        else:
            cur.append(t)
    if cur:
        segs.append(cur)
    return segs


def git_parts(seg):
    """['git', '-C', 'x', 'push', ...] -> ('push', [인자...]). git 명령이 아니면 (None, [])."""
    if not seg or os.path.basename(seg[0]) != "git":
        return None, []
    i = 1
    while i < len(seg) and seg[i].startswith("-"):
        i += 2 if seg[i] in ("-C", "-c") else 1
    if i >= len(seg):
        return None, []
    return seg[i], seg[i + 1:]


def current_branch(cwd):
    try:
        out = subprocess.run(
            ["git", "-C", cwd or ".", "branch", "--show-current"],
            capture_output=True, text=True, timeout=5,
        )
        return out.stdout.strip()
    except Exception:
        return ""


# 비밀 파일 판별 (.env.example 같은 견본 파일은 허용)
SECRET_NAME = re.compile(
    r"^(\.env(\..+)?|.+\.pem|.+\.key|.+\.p12|.+\.pfx|.+\.jks|.+\.keystore"
    r"|id_rsa.*|id_ed25519.*|id_ecdsa.*|id_dsa.*|credentials(\.json)?|\.netrc)$"
)
SAFE_SUFFIX = re.compile(r"\.(example|sample|template|dist|pub)$")


def is_secret_path(path):
    if not path:
        return False
    norm = path.replace("\\", "/")
    base = os.path.basename(norm.rstrip("/"))
    if SAFE_SUFFIX.search(base):
        return False
    if "/.ssh/" in norm or norm.endswith("/.aws/credentials"):
        return True
    return bool(SECRET_NAME.match(base))
