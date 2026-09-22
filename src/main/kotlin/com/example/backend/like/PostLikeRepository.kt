package com.example.backend.like

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostLikeRepository: JpaRepository<PostLike, Long> {

    fun existsByUserIdAndPostId(userId: Long, postId: Long): Boolean
    fun deleteByUserIdAndPostId(userId: Long, postId: Long)
    fun deleteAllByPostId(postId: Long)
}