package com.example.backend.auth.controller

import com.example.backend.auth.AuthResponse
import com.example.backend.auth.service.AuthService
import com.example.backend.auth.OAuthLoginRequest
import com.example.backend.auth.RefreshTokenRequest
import com.example.backend.auth.TokenRefreshResponse
import com.example.backend.common.CurrentUserId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/kakao/login")
    fun loginWithKakao(@RequestBody request: OAuthLoginRequest): AuthResponse {
        return authService.loginWithKakao(request.accessToken)
    }

    @PostMapping("/kakao/unlink")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlinkKakaoAccount(@CurrentUserId userId: Long) {
        authService.unlinkFromKakao(userId)
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): TokenRefreshResponse {
        return authService.refreshToken(request.refreshToken)
    }
}