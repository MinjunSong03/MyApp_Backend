package com.example.backend.auth

import com.example.backend.post.MediaType
import com.example.backend.post.Post
import com.example.backend.report.ReportReason
import java.time.LocalDateTime

data class OAuthLoginRequest(
    val accessToken: String
)

data class UpdateNicknameRequest(
    val nickname: String
)

data class AuthResponse(
    val token: String,
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val isNewUser: Boolean
)

data class CreatePostRequest(
    val title: String,
    val description: String,
    val mediaType: MediaType,
    val thumbnailUrl: String,
    val mediaUrl: String
)

data class EditPostRequest(
    val title: String,
    val description: String,
    val mediaType: MediaType,
    val thumbnailUrl: String,
    val mediaUrl: String
)

data class PostResponse(
    val id: Long,
    val userId: Long,
    val authorNickname: String,
    val authorProfileImageUrl: String?,
    val title: String,
    val description: String,
    val mediaType: MediaType,
    val thumbnailUrl: String,
    val mediaUrl: String,
    val viewCount: Long,
    val createdAt: LocalDateTime,
    val isMine: Boolean
) {
    companion object {
        fun from(post: Post, currentUserId: Long): PostResponse = PostResponse(
            id = post.id,
            userId = post.user.id,
            authorNickname = post.user.nickname,
            authorProfileImageUrl = post.user.profileImageUrl,
            title = post.title,
            description = post.description,
            mediaType = post.mediaType,
            thumbnailUrl = post.thumbnailUrl,
            mediaUrl = post.mediaUrl,
            viewCount = post.viewCount,
            createdAt = post.createdAt,
            isMine = post.user.id == currentUserId
        )
    }
}

data class CreateReportRequest(
    val reason: ReportReason,
    val detail: String
)