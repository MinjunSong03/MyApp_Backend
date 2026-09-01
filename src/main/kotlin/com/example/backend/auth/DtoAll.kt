package com.example.backend.auth

import com.example.backend.post.MediaType
import com.example.backend.post.Post
import com.example.backend.report.ReportReason
import com.example.backend.user.User
import com.example.backend.user.UserStatus
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
    val userNickname: String,
    val userProfileImageUrl: String?,
    val title: String,
    val description: String,
    val mediaType: MediaType,
    val thumbnailUrl: String,
    val mediaUrl: String,
    val viewCount: Long,
    val createdAt: LocalDateTime,
    val isMine: Boolean,
    val isHidden: Boolean,
    val isUserDeleted: Boolean
) {
    companion object {
        fun from(post: Post, currentUserId: Long): PostResponse = PostResponse(
            id = post.id,
            userId = post.user.id,
            userNickname = post.user.nickname,
            userProfileImageUrl = post.user.profileImageUrl,
            title = post.title,
            description = post.description,
            mediaType = post.mediaType,
            thumbnailUrl = post.thumbnailUrl,
            mediaUrl = post.mediaUrl,
            viewCount = post.viewCount,
            createdAt = post.createdAt,
            isMine = post.user.id == currentUserId,
            isHidden = post.isHidden,
            isUserDeleted = (post.user.status == UserStatus.DELETED)
        )
    }
}

data class CreateReportRequest(
    val reason: ReportReason,
    val detail: String
)

data class BlockedUserResponse(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val status: UserStatus
) {
    companion object {
        fun from(user: User): BlockedUserResponse = BlockedUserResponse(
            id = user.id,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            status = user.status
        )
    }
}

data class ImagePresignedRequest(
    val fileName: String,
    val contentType: String
)

data class VideoPresignedRequest(
    val videoFileName: String,
    val videoContentType: String,
    val thumbFileName: String,
    val thumbContentType: String
)

data class PresignedUrlResponse(
    val uploadUrl: String,
    val fileUrl: String,
    val key: String
)

data class VideoPresignedUrlResponse(
    val video: PresignedUrlResponse,
    val thumbnail: PresignedUrlResponse
)