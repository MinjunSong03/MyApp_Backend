package com.example.backend.common

import com.example.backend.auth.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class CurrentUserIdArgumentResolver(
    private val jwtTokenProvider: JwtTokenProvider
): HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUserId::class.java) &&
                (parameter.parameterType == Long::class.java || parameter.parameterType == Long::class.javaPrimitiveType)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Long {
        val request = webRequest.nativeRequest as HttpServletRequest
        val bearerToken = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION)
            ?: throw IllegalArgumentException("인증 토큰(Authorization 헤더)이 필요합니다.")

        if (!bearerToken.startsWith("Bearer ", ignoreCase = true)) {
            throw IllegalArgumentException("올바른 Authorization Bearer 헤더 형식이 아닙니다.")
        }

        val token = bearerToken.substring(7).trim()

        if (!jwtTokenProvider.validateToken(token)) {
            throw IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.")
        }

        return jwtTokenProvider.getUserId(token)
    }
}