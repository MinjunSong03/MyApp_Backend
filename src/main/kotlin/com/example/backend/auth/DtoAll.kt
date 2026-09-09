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

data class UserProfileResponse(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String? = null,
    val isDeleted: Boolean = false,
    val isMine: Boolean = false
)

data class UpdateProfileRequest(
    val nickname: String,
    val profileImageUrl: String? = null,
    val deleteProfileImage: Boolean = false
)

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val isNewUser: Boolean
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

data class CreatePostRequest(
    val title: String,
    val description: String,
    val videoUrl: String? = null,
    val videoThumbnailUrl: String? = null,
    val imageUrls: List<String> = emptyList()
)

data class EditPostRequest(
    val title: String,
    val description: String,
    val videoUrl: String? = null,
    val videoThumbnailUrl: String? = null,
    val imageUrls: List<String> = emptyList()
)

data class PostResponse(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val userProfileImageUrl: String?,
    val title: String,
    val description: String,
    val videoUrl: String?,
    val videoThumbnailUrl: String?,
    val imageUrls: List<String>,
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
            videoUrl = post.videoUrl,
            videoThumbnailUrl = post.videoThumbnailUrl,
            imageUrls = post.imageUrls.toList(),
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