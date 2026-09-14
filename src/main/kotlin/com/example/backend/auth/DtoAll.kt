package com.example.backend.auth

import com.example.backend.comment.Comment
import com.example.backend.post.MediaType
import com.example.backend.post.Post
import com.example.backend.report.ReportReason
import com.example.backend.user.User
import com.example.backend.user.UserStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
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
    @field:NotBlank(message = "닉네임을 입력해 주세요.")
    @field:Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해 주세요.")
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
    @field:NotBlank(message = "제목을 입력해 주세요.")
    @field:Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
    val title: String,
    @field:NotBlank(message = "내용을 입력해 주세요.")
    @field:Size(max = 3000, message = "내용은 최대 3,000자까지 입력 가능합니다.")
    val description: String,
    val videoUrl: String? = null,
    val videoThumbnailUrl: String? = null,
    val imageUrls: List<String> = emptyList()
)

data class EditPostRequest(
    @field:NotBlank(message = "제목을 입력해 주세요.")
    @field:Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
    val title: String,
    @field:NotBlank(message = "내용을 입력해 주세요.")
    @field:Size(max = 3000, message = "내용은 최대 3,000자까지 입력 가능합니다.")
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
    val editedAt: LocalDateTime?,
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
            editedAt = post.editedAt,
            isMine = post.user.id == currentUserId,
            isHidden = post.isHidden,
            isUserDeleted = (post.user.status == UserStatus.DELETED)
        )
    }
}

data class CreateCommentRequest(
    val postId: Long,
    @field:NotBlank(message = "댓글을 입력해 주세요.")
    @field:Size(max = 500, message = "댓글은 최대 500자까지 입력 가능합니다.")
    val content: String
)

data class EditCommentRequest(
    @field:NotBlank(message = "댓글을 입력해 주세요.")
    @field:Size(max = 500, message = "댓글은 최대 500자까지 입력 가능합니다.")
    val content: String
)

data class CommentResponse(
    val id: Long,
    val postId: Long,
    val userId: Long,
    val userNickname: String,
    val userProfileImageUrl: String?,
    val content: String,
    val createdAt: LocalDateTime,
    val editedAt: LocalDateTime?,
    val isMine: Boolean,
    val isUserDeleted: Boolean
) {
    companion object {
        fun from(comment: Comment, currentUserId: Long): CommentResponse = CommentResponse(
            id = comment.id,
            postId = comment.post.id,
            userId = comment.user.id,
            userNickname = comment.user.nickname,
            userProfileImageUrl = comment.user.profileImageUrl,
            content = comment.content,
            createdAt = comment.createdAt,
            editedAt = comment.editedAt,
            isMine = comment.user.id == currentUserId,
            isUserDeleted = (comment.user.status == UserStatus.DELETED)
        )
    }
}

data class CreateReportRequest(
    val reason: ReportReason,
    @field:Size(max = 500, message = "신고 상세 사유는 최대 500자까지 입력 가능합니다.")
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