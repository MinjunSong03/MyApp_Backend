package com.example.backend.userblock

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserBlockRepository: JpaRepository<UserBlock, Long> {

    fun existsByBlockerIdAndBlockedId(blockerId: Long, blockedId: Long): Boolean

    @Query("SELECT ub.blocked.id FROM UserBlock ub WHERE ub.blocker.id = :blockerId")
    fun findBlockedIdsByBlockerId(@Param("blockerId") blockerId: Long): List<Long>

    fun deleteByBlockerIdAndBlockedId(blockerId: Long, blockedId: Long)
}