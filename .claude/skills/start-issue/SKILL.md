---
name: start-issue
description: 작업 내용을 받아 GitHub 이슈를 만들고 작업 브랜치까지 만든다. "이슈 만들어줘", "이슈 파고 브랜치 따줘", "~~ 작업 시작할게"처럼 새 작업을 시작할 때 사용한다. 이미 이슈와 브랜치가 있거나, 커밋·PR만 요청한 경우에는 사용하지 않는다.
argument-hint: "[작업 설명]"
---

# 이슈 만들고 브랜치 열기

규칙은 `~/.claude/rules/git-conventions.md`, `pr-conventions.md` 를 따른다.

## 절차
1. **상태 확인**: `git status --short`, `git branch --show-current`, 기본 브랜치(`develop` 없으면 `main`)를 확인한다.
   - 커밋하지 않은 변경이 있으면 알리고 어떻게 할지 묻는다. (그대로 가져가기 / 먼저 정리하기)
2. **중복 확인**: `git fetch origin` 후 아래를 본다. 이미 해결됐거나 진행 중이면 알리고 멈춘다.
   - `git log origin/<기본브랜치> --oneline -20`
   - `gh issue list --state all --search "<키워드>"`, `gh pr list --state all --search "<키워드>"`
3. **이슈 초안을 쓴다.**
   - 타입 결정: `feat`, `fix`, `chore`, `docs`, `refactor`
   - 제목: `타입: [이름] 내용` (이름은 `~/.claude/CLAUDE.md` 의 작성자)
   - 본문: 레포의 `.github/ISSUE_TEMPLATE/*.md` 가 있으면 그 형식으로, 없으면 `Description / To-do / 기타`
   - 라벨: `gh label list` 에 있는 것 중 타입에 맞는 것만. 없으면 붙이지 않는다.
4. **초안(제목, 본문, 라벨, 브랜치 이름)을 보여주고 확인받는다.** 승인 전에는 만들지 않는다.
5. 승인되면 본문을 임시 파일로 저장하고 `gh issue create --title "…" --body-file <파일> [--label …]` 로 만든다. 출력의 URL 에서 이슈 번호를 얻는다.
6. **브랜치 생성**: `git switch -c <타입>/<번호>-<영문-kebab-설명> origin/<기본브랜치> --no-track`
   - push 는 하지 않는다. 첫 커밋 뒤에 사용자 확인을 받고 올린다.
7. 이슈 링크, 브랜치 이름, 다음 단계를 보고한다.

## 하지 않는 것
- 승인 없이 이슈를 만들지 않는다.
- 기본 브랜치에서 직접 작업을 시작하지 않는다.
