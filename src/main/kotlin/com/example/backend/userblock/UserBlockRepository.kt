package com.example.backend.userblock

import com.example.backend.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserBlockRepository: JpaRepository<UserBlock, Long> {

    fun existsByBlockerIdAndBlockedId(blockerId: Long, blockedId: Long): Boolean

    @Query("""
    SELECT ub.blocked 
    FROM UserBlock ub 
    WHERE ub.blocker.id = :blockerId 
    ORDER BY ub.createdAt DESC
    """)
    fun findBlockedIdsByBlockerId(
        @Param("blockerId") blockerId: Long,
        pageable: Pageable
    ): Slice<User>

    fun deleteByBlockerIdAndBlockedId(blockerId: Long, blockedId: Long)

    fun deleteAllByBlockerId(blockerId: Long)

    fun deleteAllByBlockedId(blockedId: Long)
}