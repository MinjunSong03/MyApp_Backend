package com.example.backend.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {
    fun findByOauthId(oauthId: String): User?
    fun existsByNickname(nickname: String): Boolean
}