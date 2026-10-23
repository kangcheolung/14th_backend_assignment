#!/usr/bin/env python3
"""PreToolUse(Edit|MultiEdit|Write): 이미 적용된 파일과 빌드 산출물 수정 차단.

차단 대상
  - db/migration/, migrations/ 안에서 Git 이 이미 추적 중인 파일 (적용된 마이그레이션으로 본다)
    -> 새 파일을 만드는 것, 아직 커밋 안 한 새 마이그레이션을 고치는 것은 허용
  - build/ 폴더 (Gradle 산출물). 소스 경로(src/ 아래)의 build 패키지는 제외
"""
import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _lib import block, file_path_of, load  # noqa: E402


def is_tracked(path):
    try:
        r = subprocess.run(
            ["git", "-C", os.path.dirname(path) or ".", "ls-files", "--error-unmatch", os.path.basename(path)],
            capture_output=True, text=True, timeout=5,
        )
        return r.returncode == 0
    except Exception:
        return False


data = load()
path = file_path_of(data)
if not path:
    sys.exit(0)

norm = path.replace("\\", "/")
parts = [p for p in norm.split("/") if p]

# 1. 마이그레이션: 이미 추적 중인 파일만 막는다.
in_migration = ("migration" in parts and "db" in parts) or "migrations" in parts
if in_migration and os.path.exists(path) and is_tracked(path):
    block(
        f"'{os.path.basename(path)}' 은 이미 적용된 마이그레이션이라 수정할 수 없어요. "
        "스키마를 바꾸려면 다음 버전의 새 마이그레이션 파일을 추가해요."
    )

# 2. build/ 산출물: src/ 아래의 build 패키지는 소스이므로 제외
if "build" in parts:
    idx = parts.index("build")
    if "src" not in parts[:idx]:
        block("build/ 는 빌드가 만드는 산출물이라 직접 수정하지 않아요. 소스를 고치고 다시 빌드해요.")

sys.exit(0)
