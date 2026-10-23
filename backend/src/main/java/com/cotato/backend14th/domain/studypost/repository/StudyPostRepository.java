package com.cotato.backend14th.domain.studypost.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cotato.backend14th.domain.studypost.entity.StudyPost;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long>, JpaSpecificationExecutor<StudyPost> {

	// 목록·상세는 작성자를 같이 보여주므로 한 번에 가져와 N+1 을 막는다 (ManyToOne 이라 페이징과 함께 써도 안전하다)
	@Override
	@EntityGraph(attributePaths = "user")
	Page<StudyPost> findAll(Specification<StudyPost> spec, Pageable pageable);

	@EntityGraph(attributePaths = "user")
	Optional<StudyPost> findWithUserById(Long id);
}
