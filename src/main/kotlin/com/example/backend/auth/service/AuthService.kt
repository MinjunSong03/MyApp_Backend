package com.example.backend.auth.service

import com.example.backend.auth.AuthResponse
import com.example.backend.auth.JwtTokenProvider
import com.example.backend.auth.KakaoClient
import com.example.backend.user.AuthProvider
import com.example.backend.user.User
import com.example.backend.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
@Service
class AuthService(
    private val kakaoClient: KakaoClient,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
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

        return AuthResponse(
            token = serviceToken,
            userId = user.id,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            isNewUser = isNewUser
        )
    }

    @Transactional
    fun unlinkFromKakao(userId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid User.")

        kakaoClient.unlink(user.oauthId ?: "")
        userRepository.delete(user)
    }
}