---
name: release
description: 마지막 태그 이후 변경을 모아 릴리스 노트 초안을 쓰고, 확인받은 뒤 태그와 GitHub 릴리스를 만든다. 배포 성격의 작업이라 /release 로 직접 불렀을 때만 실행한다.
disable-model-invocation: true
argument-hint: "[버전, 예: v1.2.0]"
---

# 릴리스

## 절차
1. **상태 확인**: 기본 브랜치에 있는지, 최신인지(`git fetch`, `git status -sb`), 미커밋 변경이 없는지 확인한다. 아니면 멈추고 알린다.
2. **마지막 릴리스 찾기**: `git describe --tags --abbrev=0`. 태그가 없으면 처음 릴리스라고 알린다.
3. **변경 모으기**: `git log <마지막태그>..HEAD --oneline` 과 머지된 PR(`gh pr list --state merged --search "merged:>날짜"`)을 읽는다.
4. **릴리스 노트 초안**: `feat`, `fix`, `chore`, 기타로 나눠 PR 링크와 함께 적는다. 깨지는 변경(breaking)은 따로 강조한다.
5. **버전 제안**: 변경 성격에 따라 semver 의 올릴 자리(major/minor/patch)를 제안하고 이유를 적는다.
6. **노트, 버전, 대상 커밋을 보여주고 확인받는다.**
7. 승인 뒤에 단계마다 다시 확인한다.
   - `git tag -a <버전> -m "<버전>"`
   - 태그 push (`git push origin <버전>`) — 확인 후
   - `gh release create <버전> --title "<버전>" --notes-file <파일>` — 확인 후
8. 릴리스 URL 을 보고한다.
