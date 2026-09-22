package com.example.backend.user

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository: JpaRepository<User, Long> {
    fun findByOauthId(oauthId: String): User?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.likeCount = u.likeCount + 1 WHERE u.id = :userId")
    fun incrementLikeCount(@Param("userId") userId: Long)

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.likeCount = u.likeCount - 1 WHERE u.id = :userId AND u.likeCount > 0")
    fun decrementLikeCount(@Param("userId") userId: Long)

    @Query("""
        SELECT ul.toUser FROM UserLike ul
        WHERE ul.fromUser.id = :userId
          AND ul.toUser.status = :bannedStatus
          AND ul.toUser.id NOT IN (
              SELECT ub.blocked.id FROM UserBlock ub WHERE ub.blocker.id = :userId
          )
        ORDER BY ul.id DESC
    """)
    fun findLikedUsers(
        @Param("userId") userId: Long,
        @Param("bannedStatus") bannedStatus: UserStatus = UserStatus.BANNED,
        pageable: Pageable
    ): Slice<User>
}