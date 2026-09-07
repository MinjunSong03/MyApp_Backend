package com.example.backend.auth.service

import com.example.backend.auth.BlockedUserResponse
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
    private val mediaService: MediaService
) {
   @Transactional
    fun updateProfile(userId: Long,
                      newNickname: String,
                      profileImageUrl: String?,
                      deleteProfileImage: Boolean
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid User")

       check(user.status == UserStatus.ACTIVE) { "활성화된 사용자만 프로필을 변경할 수 있습니다." }

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
            ?: throw IllegalArgumentException("Invalid User")

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
            ?: throw IllegalArgumentException("Invalid User")

        require(user.id == userId) { "본인 인증에 실패했습니다." }

        val users =  userBlockRepository.findBlockedIdsByBlockerId(userId, pageable)

        return users.map { BlockedUserResponse.from(user = it) }
    }
}