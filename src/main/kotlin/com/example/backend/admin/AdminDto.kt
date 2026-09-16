package com.example.backend.admin

import com.example.backend.comment.CommentStatus
import com.example.backend.post.PostStatus
import com.example.backend.report.ReportReason
import com.example.backend.report.ReportStatus
import com.example.backend.user.UserStatus
import java.time.LocalDateTime

data class AdminPostReportResponse(
    val reportId: Long,
    val reporterId: Long,
    val reporterNickname: String,
    val reportedUserId: Long,
    val reportedUserNickname: String,
    val postId: Long,
    val postTitle: String,
    val mediaUrls: List<String>,
    val reason: ReportReason,
    val detail: String,
    val reportStatus: ReportStatus,
    val postStatus: PostStatus,
    val createdAt: LocalDateTime
)

data class AdminCommentReportResponse(
    val reportId: Long,
    val reporterId: Long,
    val reporterNickname: String,
    val reportedUserId: Long,
    val reportedUserNickname: String,
    val commentId: Long,
    val commentContent: String,
    val reason: ReportReason,
    val detail: String,
    val reportStatus: ReportStatus,
    val commentStatus: CommentStatus,
    val createdAt: LocalDateTime
)

data class AdminUserReportResponse(
    val reportId: Long,
    val reporterId: Long,
    val reporterNickname: String,
    val reportedUserId: Long,
    val reportedUserNickname: String,
    val reportedUserReportCount: Int,
    val reportedUserStatus: UserStatus,
    val reason: ReportReason,
    val detail: String,
    val reportStatus: ReportStatus,
    val createdAt: LocalDateTime
)