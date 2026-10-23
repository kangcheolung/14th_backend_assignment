package com.cotato.backend14th.domain.studypost.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cotato.backend14th.domain.studypost.entity.StudyPostComment;

public interface StudyPostCommentRepository extends JpaRepository<StudyPostComment, Long> {

	@EntityGraph(attributePaths = "user")
	Page<StudyPostComment> findByStudyPostId(Long studyPostId, Pageable pageable);

	@EntityGraph(attributePaths = "user")
	Optional<StudyPostComment> findWithUserById(Long id);

	// 목록의 글마다 댓글 수를 따로 세면 쿼리가 글 수만큼 늘어나서, 현재 페이지의 글 id 를 한 번에 묶어 센다
	@Query("select c.studyPost.id as postId, count(c) as commentCount "
		+ "from StudyPostComment c where c.studyPost.id in :postIds group by c.studyPost.id")
	List<CommentCount> countByStudyPostIds(@Param("postIds") Collection<Long> postIds);

	void deleteByStudyPostId(Long studyPostId);

	interface CommentCount {

		Long getPostId();

		long getCommentCount();
	}
}
