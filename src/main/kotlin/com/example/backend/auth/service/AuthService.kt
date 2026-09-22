package com.example.backend.auth.service

import com.example.backend.auth.AuthResponse
import com.example.backend.auth.JwtTokenProvider
import com.example.backend.auth.KakaoClient
import com.example.backend.auth.TokenRefreshResponse
import com.example.backend.user.AuthProvider
import com.example.backend.user.User
import com.example.backend.user.UserRepository
import com.example.backend.userHiddenPost.UserHiddenPostRepository
import com.example.backend.userblock.UserBlockRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
@Service
class AuthService(
    private val kakaoClient: KakaoClient,
    private val userRepository: UserRepository,
    private val userBlockRepository: UserBlockRepository,
    private val userHiddenPostRepository: UserHiddenPostRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val mediaService: MediaService
) {
    @Transactional
    fun loginWithKakao(accessToken: String): AuthResponse {
        val kakaoUser = kakaoClient.getUserInfo(accessToken)
        val oauthId = kakaoUser.id.toString()

        val existingUser = userRepository.findByOauthId(oauthId)
        val isNewUser = (existingUser == null)

        val user = existingUser ?: userRepository.save(
            User(
                provider = AuthProvider.KAKAO,
                oauthId = oauthId,
                nickname = kakaoUser.kakaoAccount?.profile?.nickname ?: "User_${oauthId.take(4)}",
                profileImageUrl = kakaoUser.kakaoAccount?.profile?.profileImageUrl
            )
        )

        val serviceToken = jwtTokenProvider.createToken(user.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id)
        user.updateRefreshToken(refreshToken)

        return AuthResponse(
            token = serviceToken,
            refreshToken = refreshToken,
            userId = user.id,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            isNewUser = isNewUser
        )
    }

    @Transactional
    fun unlinkFromKakao(userId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        user.oauthId?.let { kakaoClient.unlink(it) }

        mediaService.deleteMediaFromR2(listOf(user.profileImageUrl))

        userBlockRepository.deleteAllByBlockerId(userId)
        userHiddenPostRepository.deleteAllByUserId(userId)

        user.withdraw()
    }

    @Transactional
    fun refreshToken(refreshToken: String): TokenRefreshResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("유효하지 않거나 만료된 Refresh Token입니다.")
        }

        val userId = jwtTokenProvider.getUserId(refreshToken)
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        if (user.refreshToken != refreshToken) {
            throw IllegalArgumentException("토큰 정보가 일치하지 않습니다.")
        }

        val newAccessToken = jwtTokenProvider.createToken(user.id)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(user.id)
        user.updateRefreshToken(newRefreshToken)

        return TokenRefreshResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }
}