package com.example.backend.like

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserLikeRepository: JpaRepository<UserLike, Long> {
    fun existsByFromUserIdAndToUserId(fromUserId: Long, toUserId: Long): Boolean
    fun deleteByFromUserIdAndToUserId(fromUserId: Long, toUserId: Long)
}