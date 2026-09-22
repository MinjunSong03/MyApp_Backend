package com.example.backend.post

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostRepository: JpaRepository<Post, Long> {

    // 게시물 상태에 따른, 내가 차단한 유저를 제외한, 내가 숨김 처리한 게시물을 제외한 모든 게시물 조회.
    @Query("""
        SELECT p FROM Post p 
        WHERE p.status = :status 
          AND p.isHidden = false 
          AND p.user.id NOT IN (
          SELECT ub.blocked.id FROM UserBlock ub WHERE ub.blocker.id = :currentUserId
          )
          AND p.id NOT IN (
              SELECT hp.post.id FROM UserHiddenPost hp WHERE hp.user.id = :currentUserId
          )
        ORDER BY p.id DESC
    """)
    fun findFilteredFeed(
        @Param("status") status: PostStatus,
        @Param("currentUserId") currentUserId: Long,
        pageable: Pageable
    ): Slice<Post>

    // 내가 숨김 처리하지 않은 나의 모든 게시물 조회.
    fun findByStatusAndUserIdAndIsHiddenFalseOrderByCreatedAtDesc(
        status: PostStatus,
        userId: Long,
        pageable: Pageable
    ): Slice<Post>

    // 내가 숨김 처리한 나의 모든 게시물 조회.
    fun findByStatusAndUserIdAndIsHiddenTrueOrderByCreatedAtDesc(
        status: PostStatus,
        userId: Long,
        pageable: Pageable
    ): Slice<Post>

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    fun incrementViewCount(@Param("postId") postId: Long)

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    fun incrementLikeCount(@Param("postId") postId: Long)

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
    fun decrementLikeCount(@Param("postId") postId: Long)

    @Query("""
        SELECT pl.post FROM PostLike pl
        WHERE pl.user.id = :userId
          AND pl.post.status = :status
          AND pl.post.isHidden = false
          AND pl.post.user.id NOT IN (
              SELECT ub.blocked.id FROM UserBlock ub WHERE ub.blocker.id = :userId
          )
          AND pl.post.id NOT IN (
              SELECT hp.post.id FROM UserHiddenPost hp WHERE hp.user.id = :userId
          )
        ORDER BY pl.id DESC
    """)
    fun findLikedPosts(
        @Param("userId") userId: Long,
        @Param("status") status: PostStatus = PostStatus.ACTIVE,
        pageable: Pageable
    ): Slice<Post>
}