package com.example.backend.userHiddenPost

import org.springframework.data.jpa.repository.JpaRepository

interface UserHiddenPostRepository: JpaRepository<UserHiddenPost, Long> {
    fun existsByUserIdAndPostId(userId: Long, postId: Long): Boolean
    fun deleteAllByUserId(userId: Long)
    fun deleteAllByPostId(postId: Long)
}