package com.cotato.backend14th.domain.studypost.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import com.cotato.backend14th.domain.studypost.entity.StudyPost;
import com.cotato.backend14th.domain.studypost.entity.StudyPostComment;
import com.cotato.backend14th.domain.studypost.entity.StudyStatus;
import com.cotato.backend14th.domain.user.entity.Part;
import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.domain.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

/**
 * 목록 검색(Specification)과 댓글 집계 쿼리를 실제 DB 로 검증한다. 메모리 DB(H2)를 쓰므로 MySQL 이 필요 없다.
 * local 프로파일은 DB 접속 정보를 환경 파일에서 받기 때문에 이 테스트에서는 test 프로파일로 바꾼다.
 */
@DataJpaTest
@ActiveProfiles("test")
class StudyPostRepositoryTest {

	private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	@Autowired
	private StudyPostRepository studyPostRepository;

	@Autowired
	private StudyPostCommentRepository commentRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager entityManager;

	@PersistenceUnit
	private EntityManagerFactory entityManagerFactory;

	private User backendUser;
	private User frontendUser;
	private StudyPost algorithmPost;
	private StudyPost reactPost;
	private StudyPost closedBackendPost;

	@BeforeEach
	void setUp() {
		backendUser = userRepository.save(user("be@gmail.com", "백엔드", Part.BE));
		frontendUser = userRepository.save(user("fe@gmail.com", "프론트", Part.FE));

		algorithmPost = studyPostRepository.save(StudyPost.create(backendUser, "알고리즘 스터디", "주 2회 백준 풀이"));
		reactPost = studyPostRepository.save(StudyPost.create(frontendUser, "리액트 스터디", "알고리즘 말고 리액트 공부"));
		closedBackendPost = studyPostRepository.save(StudyPost.create(backendUser, "스프링 스터디", "JPA 공부"));
		closedBackendPost.edit(null, null, StudyStatus.CLOSED);
		flushAndClear();
	}

	@Test
	@DisplayName("조건이 없으면 모든 글이 최신순으로 나온다")
	void findAllWithoutCondition() {
		Page<StudyPost> page = search(null, null, null, 0, 10);

		assertThat(page.getContent()).extracting(StudyPost::getId)
			.containsExactly(closedBackendPost.getId(), reactPost.getId(), algorithmPost.getId());
		assertThat(page.getTotalElements()).isEqualTo(3);
	}

	@Test
	@DisplayName("키워드는 제목과 내용 모두에서 찾는다")
	void keywordMatchesTitleOrContent() {
		Page<StudyPost> page = search("알고리즘", null, null, 0, 10);

		// 제목에 있는 글과 내용에만 있는 글이 모두 나온다
		assertThat(page.getContent()).extracting(StudyPost::getId)
			.containsExactlyInAnyOrder(algorithmPost.getId(), reactPost.getId());
	}

	@Test
	@DisplayName("모집 상태로 거른다")
	void filterByStatus() {
		assertThat(search(null, StudyStatus.CLOSED, null, 0, 10).getContent())
			.extracting(StudyPost::getId).containsExactly(closedBackendPost.getId());
		assertThat(search(null, StudyStatus.RECRUITING, null, 0, 10).getContent())
			.extracting(StudyPost::getId).containsExactly(reactPost.getId(), algorithmPost.getId());
	}

	@Test
	@DisplayName("파트는 글 작성자의 파트로 거른다")
	void filterByAuthorPart() {
		assertThat(search(null, null, Part.FE, 0, 10).getContent())
			.extracting(StudyPost::getId).containsExactly(reactPost.getId());
		assertThat(search(null, null, Part.PM, 0, 10).getContent()).isEmpty();
	}

	@Test
	@DisplayName("키워드, 상태, 파트를 함께 주면 모두 만족하는 글만 나온다")
	void combinedConditions() {
		Page<StudyPost> page = search("스터디", StudyStatus.RECRUITING, Part.BE, 0, 10);

		assertThat(page.getContent()).extracting(StudyPost::getId).containsExactly(algorithmPost.getId());
		assertThat(page.getTotalElements()).isEqualTo(1);
	}

	@Test
	@DisplayName("페이지를 나누면 전체 개수는 유지하고 마지막 페이지에는 남은 글만 있다")
	void paging() {
		Page<StudyPost> lastPage = search(null, null, null, 1, 2);

		assertThat(lastPage.getTotalElements()).isEqualTo(3);
		assertThat(lastPage.getTotalPages()).isEqualTo(2);
		assertThat(lastPage.getContent()).extracting(StudyPost::getId).containsExactly(algorithmPost.getId());
	}

	@Test
	@DisplayName("목록으로 가져온 글의 작성자는 추가 쿼리 없이 읽을 수 있다")
	void authorIsFetchedTogether() {
		Page<StudyPost> page = search(null, null, null, 0, 10);

		assertThat(page.getContent())
			.allSatisfy(post -> assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(post, "user")).isTrue());
	}

	@Test
	@DisplayName("댓글 수는 글 id 별로 묶어서 센다")
	void countCommentsByPost() {
		commentRepository.save(StudyPostComment.create(algorithmPost, frontendUser, "신청1"));
		commentRepository.save(StudyPostComment.create(algorithmPost, backendUser, "신청2"));
		commentRepository.save(StudyPostComment.create(reactPost, backendUser, "신청3"));
		flushAndClear();

		List<StudyPostCommentRepository.CommentCount> counts = commentRepository.countByStudyPostIds(
			List.of(algorithmPost.getId(), reactPost.getId(), closedBackendPost.getId()));

		// 댓글이 없는 글은 결과에 나오지 않는다 (서비스가 0 으로 채운다)
		assertThat(counts).hasSize(2);
		assertThat(counts).filteredOn(c -> c.getPostId().equals(algorithmPost.getId()))
			.singleElement().extracting(StudyPostCommentRepository.CommentCount::getCommentCount).isEqualTo(2L);
		assertThat(counts).filteredOn(c -> c.getPostId().equals(reactPost.getId()))
			.singleElement().extracting(StudyPostCommentRepository.CommentCount::getCommentCount).isEqualTo(1L);
	}

	@Test
	@DisplayName("댓글 목록은 오래된 순으로 나오고 작성자를 함께 가져온다")
	void commentsOldestFirst() {
		StudyPostComment first = commentRepository.save(StudyPostComment.create(algorithmPost, frontendUser, "먼저"));
		StudyPostComment second = commentRepository.save(StudyPostComment.create(algorithmPost, backendUser, "나중"));
		commentRepository.save(StudyPostComment.create(reactPost, backendUser, "다른 글 댓글"));
		flushAndClear();

		Page<StudyPostComment> page = commentRepository.findByStudyPostId(
			algorithmPost.getId(), PageRequest.of(0, 20, Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"))));

		assertThat(page.getContent()).extracting(StudyPostComment::getId).containsExactly(first.getId(), second.getId());
		assertThat(page.getContent())
			.allSatisfy(c -> assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(c, "user")).isTrue());
	}

	@Test
	@DisplayName("글의 댓글을 지우면 그 글의 댓글만 사라진다")
	void deleteCommentsOfPost() {
		commentRepository.save(StudyPostComment.create(algorithmPost, frontendUser, "지워질 댓글"));
		StudyPostComment kept = commentRepository.save(StudyPostComment.create(reactPost, backendUser, "남을 댓글"));
		flushAndClear();

		commentRepository.deleteByStudyPostId(algorithmPost.getId());
		flushAndClear();

		assertThat(commentRepository.findAll()).extracting(StudyPostComment::getId).containsExactly(kept.getId());
	}

	private Page<StudyPost> search(String keyword, StudyStatus status, Part part, int page, int size) {
		Specification<StudyPost> spec = Specification.allOf(
			StudyPostSpecifications.keywordContains(keyword),
			StudyPostSpecifications.statusEquals(status),
			StudyPostSpecifications.authorPartEquals(part)
		);
		return studyPostRepository.findAll(spec, PageRequest.of(page, size, NEWEST_FIRST));
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

	private static User user(String email, String nickname, Part part) {
		return User.builder()
			.email(email)
			.password("encoded")
			.name("이름")
			.nickname(nickname)
			.generation(14)
			.part(part)
			.build();
	}
}
