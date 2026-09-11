package com.example.backend.user

import jakarta.persistence.*
import java.time.LocalDateTime

enum class UserStatus { ACTIVE, BANNED, DELETED }
enum class AuthProvider { LOCAL, KAKAO }

@Entity
@Table(name = "users")
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    var provider: AuthProvider? = AuthProvider.LOCAL,

    var refreshToken: String? = null,

    @Column(unique = true)
    var oauthId: String? = null,

    var nickname: String = "",

    var profileImageUrl: String? = null,

    @Column(nullable = false)
    var reportCount: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus = UserStatus.ACTIVE,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var deletedAt: LocalDateTime? = null

) {
    companion object {
        private val RESERVED_NICKNAMES = setOf("탈퇴한 사용자", "알 수 없음", "관리자", "admin")
    }

    fun updateProfile(newNickname: String, newProfileImageUrl: String?, deleteProfileImage: Boolean) {
        require(newNickname !in RESERVED_NICKNAMES) { "사용할 수 없는 닉네임입니다." }
        require(newNickname.length in 2..10) { "닉네임은 2자 이상 10자 이하이어야 합니다." }

        if (deleteProfileImage) {
            this.profileImageUrl = null
        } else if (newProfileImageUrl != null) {
            this.profileImageUrl = newProfileImageUrl
        }

        this.nickname = newNickname
        this.updatedAt = LocalDateTime.now()
    }

    fun ban() {
        this.status = UserStatus.BANNED
        this.updatedAt = LocalDateTime.now()
    }

    fun incrementReportCount() {
        this.reportCount += 1
        if (this.reportCount >= 5) {
            this.status = UserStatus.BANNED
            this.updatedAt = LocalDateTime.now()
        }
    }

    fun withdraw() {
        this.provider = null
        this.oauthId = null
        this.nickname = "탈퇴한 사용자"
        this.profileImageUrl = null
        this.status = UserStatus.DELETED
        this.updatedAt = LocalDateTime.now()
        this.deletedAt = LocalDateTime.now()
    }
    fun updateRefreshToken(token: String) {
        this.refreshToken = token
    }

}