package com.example.backend.auth.service

import com.example.backend.auth.UserResponse
import com.example.backend.auth.LikeResponse
import com.example.backend.auth.UserProfileResponse
import com.example.backend.like.UserLike
import com.example.backend.like.UserLikeRepository
import com.example.backend.report.ReportReason
import com.example.backend.report.ReportUser
import com.example.backend.report.ReportUserRepository
import com.example.backend.user.UserRepository
import com.example.backend.user.UserStatus
import com.example.backend.userblock.UserBlock
import com.example.backend.userblock.UserBlockRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userBlockRepository: UserBlockRepository,
    private val reportUserRepository: ReportUserRepository,
    private val userLikeRepository: UserLikeRepository,
    private val mediaService: MediaService,
) {
   @Transactional
    fun updateProfile(
       userId: Long,
       newNickname: String,
       profileImageUrl: String?,
       deleteProfileImage: Boolean
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

       check(user.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

       mediaService.deleteMediaFromR2(listOf(user.profileImageUrl))

       user.updateProfile(
           newNickname = newNickname,
           newProfileImageUrl = profileImageUrl,
           deleteProfileImage = deleteProfileImage
       )
    }

    @Transactional
    fun blockUser(blockerId: Long, blockedId: Long) {
        require(blockerId != blockedId) { "자기 자신을 차단할 수 없습니다." }

        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw IllegalStateException("이미 차단된 사용자입니다.")
        }

        val blocker = userRepository.findByIdOrNull(blockerId)
            ?: throw IllegalArgumentException("Invalid user")

        check(blocker.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        val blocked = userRepository.findByIdOrNull(blockedId)
            ?: throw IllegalArgumentException("차단 대상 유저를 찾을 수 없습니다.")

        val userBlock = UserBlock(blocker = blocker, blocked = blocked)
        userBlockRepository.save(userBlock)
    }

    @Transactional
    fun unblockUser(blockerId: Long, blockedId: Long) {
        val blocker = userRepository.findByIdOrNull(blockerId)
            ?: throw IllegalArgumentException("Invalid user")

        check(blocker.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        userBlockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId)
    }

    @Transactional(readOnly = true)
    fun getMyBlockedUser(userId: Long, pageable: Pageable): Slice<UserResponse> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val users =  userBlockRepository.findBlockedIdsByBlockerId(userId, pageable)

        return users.map { UserResponse.from(user = it) }
    }

    @Transactional(readOnly = true)
    fun getUserProfile(currentUserId: Long, targetUserId: Long): UserProfileResponse {
        val targetUser = userRepository.findByIdOrNull(targetUserId)
            ?: throw IllegalArgumentException("Invalid user")

        if (userBlockRepository.existsByBlockerIdAndBlockedId(currentUserId, targetUserId)) {
            throw IllegalStateException("차단한 사용자의 프로필은 조회할 수 없습니다.")
        }

        val isLiked = userLikeRepository.existsByFromUserIdAndToUserId(
            fromUserId = currentUserId,
            toUserId = targetUserId
        )

        return UserProfileResponse(
            id = targetUser.id,
            nickname = targetUser.nickname,
            profileImageUrl = targetUser.profileImageUrl,
            isDeleted = (targetUser.status == UserStatus.DELETED),
            isMine = (currentUserId == targetUser.id),
            likeCount = targetUser.likeCount,
            isLiked = isLiked
        )
    }

    @Transactional
    fun reportUser(reporterId: Long, targetId: Long, reason: ReportReason, detail: String) {
        require(reporterId != targetId) { "자기 자신을 신고할 수 없습니다." }

        val reporter = userRepository.findByIdOrNull(reporterId)
            ?: throw IllegalArgumentException("Invalid user")

        check(reporter.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        val targetUser = userRepository.findByIdOrNull(targetId)
            ?: throw IllegalArgumentException("신고할 사용자를 찾을 수 없습니다.")

        if (reportUserRepository.existsByReporterIdAndReportedId(reporterId, targetId)) {
            throw IllegalStateException("이미 신고한 사용자입니다.")
        }

        val report = ReportUser(
            reporter = reporter,
            reported = targetUser,
            reason = reason,
            detail = detail
        )
        reportUserRepository.save(report)
        targetUser.incrementReportCount()
    }

    @Transactional(readOnly = true)
    fun getMyLikedUsers(userId: Long, pageable: Pageable): Slice<UserResponse> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val likedUsers = userRepository.findLikedUsers(
            userId = user.id,
            bannedStatus = UserStatus.BANNED,
            pageable = pageable
        )

        return likedUsers.map { UserResponse.from(user = it) }
    }

    @Transactional
    fun likeUser(fromUserId: Long, toUserId: Long): LikeResponse {
        val fromUser = userRepository.findByIdOrNull(fromUserId)
            ?: throw IllegalArgumentException("Invalid user")

        check(fromUser.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        val toUser = userRepository.findByIdOrNull(toUserId)
            ?: throw IllegalArgumentException("'좋아요'할 사용자를 찾을 수 없습니다.")

        if (userLikeRepository.existsByFromUserIdAndToUserId(fromUserId =  fromUserId, toUserId =  toUserId)) {
            throw IllegalStateException("이미 '좋아요'한 사용자입니다.")
        }

        val userLike = UserLike(fromUser = fromUser, toUser = toUser)
        userLikeRepository.save(userLike)
        userRepository.incrementLikeCount(toUserId)

        return LikeResponse(isLiked = true, likeCount = toUser.likeCount + 1)
    }

    @Transactional
    fun unlikeUser(fromUserId: Long, toUserId: Long): LikeResponse {
        if (!userLikeRepository.existsByFromUserIdAndToUserId(fromUserId = fromUserId, toUserId = toUserId)) {
            throw IllegalStateException("좋아요를 누르지 않은 사용자입니다.")
        }

        val toUser = userRepository.findByIdOrNull(toUserId)
            ?: throw IllegalArgumentException("대상 사용자를 찾을 수 없습니다.")

        userLikeRepository.deleteByFromUserIdAndToUserId(fromUserId = fromUserId, toUserId = toUserId)
        userRepository.decrementLikeCount(toUserId)

        val updatedCount = (toUser.likeCount - 1).coerceAtLeast(0)

        return LikeResponse(isLiked = false, likeCount = updatedCount)
    }
}