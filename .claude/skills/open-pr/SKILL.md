---
name: open-pr
description: 커밋된 작업을 push 하고 자세한 본문의 Pull Request 를 만든다. "PR 올려줘", "PR 만들어줘", 작업이 끝나 리뷰를 요청할 때 사용한다. 커밋하지 않은 변경이 있으면 먼저 commit-plan 을 제안한다.
argument-hint: "[이슈 번호, 없으면 브랜치 이름에서 추출]"
---

# PR 올리기

형식은 `~/.claude/rules/pr-conventions.md` 를 따른다.

## 절차
1. **상태 확인**
   - 브랜치, 미커밋 변경(있으면 `commit-plan` 제안), base 브랜치(프로젝트 `CLAUDE.md` 에 없으면 `develop`, 없으면 `main`)
   - 이슈 번호: 브랜치 이름(`feat/7-harness` → 7)에서 뽑는다. 없으면 묻는다.
2. **변경 이해**: `git log <base>..HEAD --oneline`, `git diff <base>...HEAD --stat`, 필요한 부분의 diff, 이슈 본문(`gh issue view <번호>`)을 읽는다.
3. **검증 결과 확인**: 이 작업에서 빌드·테스트를 돌렸는지 확인한다. 없으면 `verify` 를 먼저 제안한다. 본문의 테스트 항목에는 실제 결과만 쓴다.
4. **제목**: `타입: [이름] 내용 (closes #이슈번호)`
5. **본문**: 레포에 `.github/PULL_REQUEST_TEMPLATE.md` 가 있으면 그 섹션을 따라 채운다. 없으면 개요 / 작업 내용 / API 변경 / 테스트 / 스크린샷·영상 / 관련 이슈 / 리뷰 포인트.
   - **자세히 쓴다.** 어떤 클래스·파일을 왜 바꿨는지, 고려한 대안, 주의점, 알려진 한계를 적는다.
6. **제목, 본문, base, 리뷰어를 보여주고 확인받는다.** 승인 전에는 push 도 PR 도 하지 않는다.
7. 승인되면 보호 브랜치가 아닌지 확인하고 `git push -u origin <브랜치>` → 본문을 임시 파일로 저장한 뒤 `gh pr create --base <base> --title "…" --body-file <파일> [--reviewer …]`
8. PR URL 을 보고한다.

## 하지 않는 것
- 요청 없이 PR 을 만들지 않는다.
- 강제 push 를 하지 않는다. base 가 이슈의 기본 브랜치와 다르면 먼저 묻는다.
