package com.cotato.backend14th.domain.studypost.entity;

/**
 * 스터디 모집 상태. 두 상태 사이를 자유롭게 오갈 수 있다.
 */
public enum StudyStatus {
	RECRUITING, // 모집 중
	CLOSED      // 모집 마감. 마감된 글에는 댓글(신청)을 달 수 없다
}
