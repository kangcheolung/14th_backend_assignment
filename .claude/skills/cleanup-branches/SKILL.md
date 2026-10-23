---
name: cleanup-branches
description: 이미 머지된 브랜치 목록을 보여주고, 사용자가 고른 것만 삭제한다. 브랜치를 지우는 작업이라 /cleanup-branches 로 직접 불렀을 때만 실행한다.
disable-model-invocation: true
---

# 머지된 브랜치 정리

## 절차
1. `git fetch --prune` 으로 원격 상태를 맞춘다.
2. 기본 브랜치(`develop` 없으면 `main`)를 확인한다.
3. **머지된 브랜치 찾기**
   - 로컬: `git branch --merged <기본브랜치>` (기본 브랜치, `main`, `develop`, 현재 브랜치는 제외)
   - 원격: `git branch -r --merged origin/<기본브랜치>` (`origin/HEAD`, `origin/main`, `origin/develop` 제외)
4. 표로 보여준다: 브랜치, 마지막 커밋 날짜, 로컬/원격 여부
5. **사용자가 지정한 브랜치만** 삭제한다.
   - 로컬: `git branch -d <이름>` (`-D` 강제 삭제는 쓰지 않는다. 막히면 이유를 알리고 멈춘다)
   - 원격 삭제(`git push origin --delete <이름>`)는 로컬과 별도로 한 번 더 확인받는다.
6. 삭제한 목록과 남은 브랜치를 보고한다.

## 하지 않는 것
- 머지되지 않은 브랜치, 현재 브랜치, `main`, `develop` 은 지우지 않는다.
- "전부 지워"라고 해도 목록을 한 번 더 보여주고 확인받는다.
