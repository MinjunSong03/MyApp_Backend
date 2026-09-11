package com.example.backend.comment

import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.domain.Pageable

interface CommentRepository: JpaRepository<Comment, Long> {
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.user
        WHERE c.post.id = :postId
          AND c.status = :status
        ORDER BY c.createdAt ASC
    """)
    fun findActiveComments(
        @Param("postId") postId: Long,
        @Param("status") status: CommentStatus,
        pageable: Pageable
    ): Slice<Comment>

    fun deleteAllByPostId(postId: Long)
}