#!/usr/bin/env python3
"""훅 5개가 실제로 막는지(exit 2) / 통과시키는지(exit 0) 확인하는 테스트.

실행: python3 ~/.claude/hooks/tests/test_hooks.py
훅은 잘못 만들어도 에러 없이 통과하는 경우가 많아서, 막혀야 하는 명령이 정말 막히는지 꼭 확인한다.
"""
import json
import os
import subprocess
import sys
import tempfile

HOOKS = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
failed = []
total = 0


def run(script, payload):
    r = subprocess.run(
        [sys.executable, os.path.join(HOOKS, script)],
        input=json.dumps(payload), capture_output=True, text=True, timeout=20,
    )
    return r.returncode, r.stderr.strip()


def check(script, payload, expected, label):
    global total
    total += 1
    code, err = run(script, payload)
    ok = code == expected
    mark = "PASS" if ok else "FAIL"
    print(f"[{mark}] {script:28} {label}  (exit {code}, 기대 {expected})")
    if not ok:
        failed.append(f"{script}: {label} -> exit {code}, stderr={err}")


def git(cwd, *args):
    subprocess.run(["git", "-C", cwd, *args], check=True, capture_output=True, text=True)


def make_repo(branch):
    d = tempfile.mkdtemp(prefix="hooktest-")
    subprocess.run(["git", "init", "-q", "-b", branch, d], check=True)
    git(d, "config", "user.email", "t@t")
    git(d, "config", "user.name", "t")
    return d


def bash(cmd, cwd="."):
    return {"tool_name": "Bash", "tool_input": {"command": cmd}, "cwd": cwd}


def tool(name, path, cwd="."):
    return {"tool_name": name, "tool_input": {"file_path": path}, "cwd": cwd}


# ---------------- block-dangerous-git ----------------
G = "block-dangerous-git.py"
feature_repo = make_repo("feat/7-harness")
develop_repo = make_repo("develop")
for cmd, exp, label in [
    ("git push --force origin feat/x", 2, "push --force"),
    ("git push -f", 2, "push -f"),
    ("git push --force-with-lease", 2, "push --force-with-lease"),
    ("git push origin +feat/x", 2, "+refspec 강제 푸시"),
    ("git push origin main", 2, "main 직접 push"),
    ("git push origin develop", 2, "develop 직접 push"),
    ("git push origin HEAD:main", 2, "HEAD:main"),
    ("git push origin HEAD:refs/heads/develop", 2, "refs/heads/develop"),
    ("git push --all", 2, "push --all"),
    ("git status && git push origin main", 2, "&& 뒤에 숨긴 main push"),
    ("git reset --hard HEAD~1", 2, "reset --hard"),
    ("git commit --no-verify -m 'x'", 2, "commit --no-verify"),
    ("git commit -n -m 'x'", 2, "commit -n"),
    ("git push --no-verify", 2, "push --no-verify"),
    ("git push origin feat/7-harness", 0, "작업 브랜치 push 허용"),
    ("git push -u origin feat/7-harness", 0, "push -u 허용"),
    ("git push origin feat/main-menu", 0, "이름에 main 이 들어간 브랜치 허용"),
    ("git reset --soft HEAD~1", 0, "reset --soft 허용"),
    ("git log --oneline", 0, "git log 허용"),
    ("echo hello", 0, "git 아닌 명령 허용"),
]:
    check(G, bash(cmd, feature_repo), exp, label)
check(G, bash("git push", feature_repo), 0, "작업 브랜치에서 인자 없는 push 허용")
check(G, bash("git push", develop_repo), 2, "develop 에서 인자 없는 push 차단")
check(G, bash("git push origin HEAD", develop_repo), 2, "develop 에서 push origin HEAD 차단")

# ---------------- block-destructive-shell ----------------
S = "block-destructive-shell.py"
for cmd, exp, label in [
    ("rm -rf /", 2, "rm -rf /"),
    ("rm -rf /*", 2, "rm -rf /*"),
    ("rm -rf ~", 2, "rm -rf ~"),
    ("rm -rf ~/", 2, "rm -rf ~/"),
    ("rm -rf $HOME", 2, "rm -rf $HOME"),
    ("sudo rm -rf /", 2, "sudo rm -rf /"),
    ("rm -fr /", 2, "rm -fr /"),
    ("rm -r -f /", 2, "rm -r -f /"),
    ("rm --recursive --force /", 2, "긴 옵션"),
    ("echo ok && rm -rf ~", 2, "&& 뒤에 숨긴 rm"),
    ("mysql -e 'DROP TABLE users'", 2, "DROP TABLE"),
    ('psql -c "drop database app"', 2, "drop database (소문자)"),
    ("rm -rf build", 0, "rm -rf build 허용"),
    ("rm -rf ./node_modules", 0, "rm -rf ./node_modules 허용"),
    ("rm -rf /tmp/foo", 0, "rm -rf /tmp/foo 허용"),
    ("rm notes.txt", 0, "rm 단일 파일 허용"),
    ("echo hello", 0, "일반 명령 허용"),
]:
    check(S, bash(cmd), exp, label)

# ---------------- protect-secrets ----------------
P = "protect-secrets.py"
for payload, exp, label in [
    (tool("Read", "/x/.env"), 2, "Read .env"),
    (tool("Read", "/x/.env.local"), 2, "Read .env.local"),
    (tool("Write", "/x/.env"), 2, "Write .env"),
    (tool("Edit", "/x/server.pem"), 2, "Edit *.pem"),
    (tool("Read", "/Users/a/.ssh/config"), 2, "~/.ssh/ 안의 파일"),
    (tool("Read", "/x/id_rsa"), 2, "id_rsa"),
    (tool("Read", "/x/.env.example"), 0, ".env.example 허용"),
    (tool("Read", "/x/id_rsa.pub"), 0, "공개키 허용"),
    (tool("Read", "/x/README.md"), 0, "일반 파일 허용"),
    (tool("Edit", "/x/application.yml"), 0, "application.yml 허용"),
    (bash("cat .env"), 2, "Bash cat .env"),
    (bash("source .env"), 2, "Bash source .env"),
    (bash("git add .env"), 2, "Bash git add .env"),
    (bash("cp .env /tmp/x"), 2, "Bash cp .env"),
    (bash("export $(cat .env)"), 2, "Bash export $(cat .env)"),
    (bash("cat ~/.ssh/id_rsa"), 2, "Bash cat ~/.ssh/id_rsa"),
    (bash("cat .env.example"), 0, "Bash cat .env.example 허용"),
    (bash("ls -la .env"), 0, "Bash ls .env 허용(내용을 읽지 않음)"),
    (bash("echo .env >> .gitignore"), 0, ".gitignore 에 추가 허용"),
    (bash("./gradlew test"), 0, "일반 Bash 허용"),
]:
    check(P, payload, exp, label)

# ---------------- protect-applied-files ----------------
A = "protect-applied-files.py"
repo = make_repo("develop")
mig = os.path.join(repo, "src/main/resources/db/migration")
os.makedirs(mig)
tracked = os.path.join(mig, "V1__init.sql")
open(tracked, "w").write("create table a(id int);")
git(repo, "add", "-A")
git(repo, "commit", "-q", "-m", "chore: init")
untracked = os.path.join(mig, "V3__draft.sql")
open(untracked, "w").write("-- draft")
for payload, exp, label in [
    (tool("Edit", tracked), 2, "커밋된 마이그레이션 수정 차단"),
    (tool("Write", tracked), 2, "커밋된 마이그레이션 덮어쓰기 차단"),
    (tool("Write", os.path.join(mig, "V2__add_col.sql")), 0, "새 마이그레이션 생성 허용"),
    (tool("Edit", untracked), 0, "아직 커밋 안 한 마이그레이션 수정 허용"),
    (tool("Edit", "/p/backend/build/libs/app.jar"), 2, "build/ 수정 차단"),
    (tool("Write", "/p/build/classes/A.class"), 2, "루트 build/ 수정 차단"),
    (tool("Edit", "/p/src/main/java/com/foo/build/Builder.java"), 0, "src 아래 build 패키지 허용"),
    (tool("Edit", "/p/README.md"), 0, "일반 파일 허용"),
]:
    check(A, payload, exp, label)

# ---------------- check-commit ----------------
C = "check-commit.py"
clean_repo = make_repo("develop")
secret_repo = make_repo("develop")
open(os.path.join(secret_repo, ".env"), "w").write("DB_PASSWORD=x")
git(secret_repo, "add", "-f", ".env")
HEREDOC_OK = "git commit -m \"$(cat <<'EOF'\nfeat: 로그인 추가\n\n본문\nEOF\n)\""
HEREDOC_BAD = "git commit -m \"$(cat <<'EOF'\n로그인 추가\n\n본문\nEOF\n)\""
for cmd, cwd, exp, label in [
    ('git commit -m "feat: add login"', clean_repo, 0, "feat: 형식 허용"),
    ('git commit -m "fix(auth): 토큰 만료 처리"', clean_repo, 0, "fix(범위): 형식 허용"),
    ('git commit -am "chore: update"', clean_repo, 0, "-am 형식 허용"),
    (HEREDOC_OK, clean_repo, 0, "heredoc 올바른 형식 허용"),
    ('git commit -m "add login"', clean_repo, 2, "접두사 없는 메시지 차단"),
    ('git commit -m "Feat: add login"', clean_repo, 2, "대문자 접두사 차단"),
    (HEREDOC_BAD, clean_repo, 2, "heredoc 접두사 없는 메시지 차단"),
    ("git commit", clean_repo, 0, "-m 없으면 검사 생략"),
    ('git commit -m "$MSG"', clean_repo, 0, "변수 메시지는 검사 생략"),
    ("git status", clean_repo, 0, "commit 아닌 명령 허용"),
    ('git commit -m "feat: add env"', secret_repo, 2, ".env 스테이징 차단"),
]:
    check(C, bash(cmd, cwd), exp, label)

print()
print(f"결과: {total - len(failed)}/{total} 통과")
if failed:
    print("실패한 항목:")
    for f in failed:
        print(" -", f)
    sys.exit(1)
