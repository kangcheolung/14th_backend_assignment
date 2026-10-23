#!/usr/bin/env python3
"""PreToolUse(Bash): 위험한 git 명령 차단.

차단 대상
  - git push --force / -f / --force-with-lease / +refspec
  - main, develop 으로 직접 push (현재 브랜치가 main/develop 일 때의 인자 없는 push 포함)
  - git push --all / --mirror (보호 브랜치를 함께 밀어버릴 수 있음)
  - git reset --hard
  - --no-verify (git commit -n 포함)
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _lib import block, command_of, current_branch, git_parts, load, segments  # noqa: E402

PROTECTED = ("main", "develop")

data = load()
command = command_of(data)
if "git" not in command:
    sys.exit(0)
cwd = data.get("cwd") or ""

for seg in segments(command):
    sub, args = git_parts(seg)
    if sub is None:
        continue

    # 훅 우회
    if "--no-verify" in args or (sub == "commit" and "-n" in args):
        block("--no-verify 는 금지예요. 훅과 검사를 우회하지 말고, 실패한 이유를 먼저 고쳐요.")

    if sub == "reset" and "--hard" in args:
        block("git reset --hard 는 금지예요. 작업 내용이 사라질 수 있어요. 필요하면 사용자에게 먼저 확인받아요.")

    if sub == "push":
        flags = [a for a in args if a.startswith("-")]
        positional = [a for a in args if not a.startswith("-")]

        for f in flags:
            if f in ("--force", "--force-if-includes") or f.startswith("--force-with-lease"):
                block("git push --force 는 금지예요. 새 커밋으로 해결해요.")
            if not f.startswith("--") and "f" in f[1:]:
                block("git push -f 는 금지예요. 새 커밋으로 해결해요.")
            if f in ("--all", "--mirror"):
                block("git push --all / --mirror 는 금지예요. 푸시할 브랜치를 직접 지정해요.")
        if any(p.startswith("+") for p in positional):
            block("+refspec 푸시는 강제 푸시예요. 금지돼 있어요.")

        # positional[0] 은 remote, 나머지는 refspec
        refspecs = positional[1:]
        if not refspecs or refspecs == ["HEAD"]:
            branch = current_branch(cwd)
            if branch in PROTECTED:
                block(f"현재 브랜치가 {branch} 예요. {branch} 로 직접 push 하지 말고, 작업 브랜치에서 PR 로 올려요.")
        for spec in refspecs:
            dst = spec.split(":", 1)[1] if ":" in spec else spec
            dst = dst.lstrip("+")
            if dst.startswith("refs/heads/"):
                dst = dst[len("refs/heads/"):]
            if dst in PROTECTED:
                block(f"{dst} 로 직접 push 는 금지예요. 작업 브랜치에서 PR 을 만들어 merge 해요.")

sys.exit(0)
