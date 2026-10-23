---
name: commit-plan
description: 변경사항을 논리 단위로 나눠 커밋 계획을 제시하고, 확인받은 뒤 단계별로 커밋한다. "커밋해줘", "커밋 계획 세워줘", "변경사항 커밋"이라고 요청할 때 사용한다. push 와 PR 은 하지 않는다(그건 open-pr).
---

# 커밋 계획과 커밋

규칙은 `~/.claude/rules/git-conventions.md` 를 따른다.

## 절차
1. **현재 상태 확인**: `git branch --show-current`, `git status --short`, `git diff`, `git diff --cached`
   - 현재 브랜치가 `main`/`develop` 이면 멈추고 작업 브랜치를 먼저 만들자고 제안한다.
2. **제외 대상 점검**: `.env`, 키 파일, `build/`, 로그, 임시 파일은 커밋 대상에서 뺀다. 의심스러운 파일은 묻는다.
3. **논리 단위로 나눈다.** 프로덕션 코드와 테스트 코드는 별도 커밋이다.
   - 권장 순서: 엔티티·Repository → Service → Controller → 테스트 → 문서
4. **계획표를 보여준다.** 순서, 포함 파일, 메시지(`타입: 요약`)를 표로 적는다.
5. **확인받는다.** 사용자가 "커밋해도 된다"고 한 뒤에만 실행한다.
6. **단계별 실행**: 각 커밋마다 `git add <파일을 하나씩 지정>` → `git commit -m "<메시지>"`. 훅이 막으면 우회하지 않고 이유를 고친다.
7. `git log --oneline -<개수>` 로 결과를 보여주고, 남은 변경이 없는지 `git status` 로 확인한다.

## 하지 않는 것
- `git add -A`, `git add .`, `--amend`, `--no-verify`
- push (사용자가 따로 요청할 때 `open-pr` 이 처리한다)
