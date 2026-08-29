package com.example.backend.user

import jakarta.persistence.*
import java.time.LocalDateTime

enum class UserStatus { ACTIVE, BANNED }
enum class AuthProvider { LOCAL, KAKAO }

@Entity
@Table(name = "users")
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthProvider = AuthProvider.LOCAL,

    @Column(unique = true, nullable = true)
    var username: String? = null,

    @Column(nullable = true)
    var password: String? = null,

    @Column(unique = true, nullable = true)
    val oauthId: String? = null,

    @Column(nullable = false)
    var nickname: String = "",

    var profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus = UserStatus.ACTIVE,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()

) {
    fun updateNickname(newNickname: String) {
        require(newNickname.length in 2..10) { "닉네임은 2자 이상 10자 이하이어야 합니다." }

        this.nickname = newNickname
        this.updatedAt = LocalDateTime.now()
    }

    fun ban() {
        this.status = UserStatus.BANNED
        this.updatedAt = LocalDateTime.now()
    }
}