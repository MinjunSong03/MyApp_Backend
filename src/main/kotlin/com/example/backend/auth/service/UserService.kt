package com.example.backend.auth.service

import com.example.backend.auth.BlockedUserResponse
import com.example.backend.auth.UserProfileResponse
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
    private val mediaService: MediaService
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

       // 수정 필요
       check(user.status == UserStatus.ACTIVE || user.status == UserStatus.DELETED) { "활성화된 사용자만 프로필을 변경할 수 있습니다." }

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

        val blocked = userRepository.findByIdOrNull(blockedId)
            ?: throw IllegalArgumentException("차단 대상 유저를 찾을 수 없습니다.")

        val userBlock = UserBlock(blocker = blocker, blocked = blocked)
        userBlockRepository.save(userBlock)
    }

    @Transactional
    fun unblockUser(blockerId: Long, blockedId: Long) {
        userBlockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId)
    }

    @Transactional(readOnly = true)
    fun getMyBlockedUser(userId: Long, pageable: Pageable): Slice<BlockedUserResponse> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        require(user.id == userId) { "본인 인증에 실패했습니다." }

        val users =  userBlockRepository.findBlockedIdsByBlockerId(userId, pageable)

        return users.map { BlockedUserResponse.from(user = it) }
    }

    @Transactional(readOnly = true)
    fun getUserProfile(currentUserId: Long, targetUserId: Long): UserProfileResponse {
        val targetUser = userRepository.findByIdOrNull(targetUserId)
            ?: throw IllegalArgumentException("Invalid user")

        if (userBlockRepository.existsByBlockerIdAndBlockedId(currentUserId, targetUserId)) {
            throw IllegalStateException("차단한 사용자의 프로필은 조회할 수 없습니다.")
        }

        return UserProfileResponse(
            id = targetUser.id,
            nickname = targetUser.nickname,
            profileImageUrl = targetUser.profileImageUrl,
            isDeleted = (targetUser.status == UserStatus.DELETED),
            isMine = (currentUserId == targetUser.id)
        )
    }

    @Transactional
    fun reportUser(reporterId: Long, targetId: Long, reason: ReportReason, detail: String) {
        require(reporterId != targetId) { "자기 자신을 신고할 수 없습니다." }

        val reporter = userRepository.findByIdOrNull(reporterId)
            ?: throw IllegalArgumentException("Invalid user")

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
}