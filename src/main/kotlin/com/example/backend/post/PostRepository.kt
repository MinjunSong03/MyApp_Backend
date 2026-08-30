package com.example.backend.post

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
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
        ORDER BY p.createdAt DESC
    """)
    fun findFilteredFeed(
        @Param("status") status: PostStatus,
        @Param("currentUserId") currentUserId: Long,
        pageable: Pageable
    ): Slice<Post>

    // (게시물 상태에 따른) 나 혹은 타인의 게시물 조회.
    fun findByStatusAndUserIdOrderByCreatedAtDesc(
        status: PostStatus,
        userId: Long,
        pageable: Pageable
    ): Slice<Post>

    // 내가 숨김 처리하지 않은 나의 모든 게시물 조회.
    fun findByStatusAndUserIdAndIsHiddenFalse(
        status: PostStatus,
        userId: Long,
        pageable: Pageable
    ): Slice<Post>

    // 내가 숨김 처리한 나의 모든 게시물 조회.
    fun findByStatusAndUserIdAndIsHiddenTrue(
        status: PostStatus,
        userId: Long,
        pageable: Pageable
    ): Slice<Post>
}