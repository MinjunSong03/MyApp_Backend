package com.example.backend.admin

import com.example.backend.comment.CommentRepository
import com.example.backend.comment.CommentStatus
import com.example.backend.post.PostRepository
import com.example.backend.post.PostStatus
import com.example.backend.report.ReportCommentRepository
import com.example.backend.report.ReportPostRepository
import com.example.backend.report.ReportStatus
import com.example.backend.report.ReportUserRepository
import com.example.backend.user.Role
import com.example.backend.user.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val reportPostRepository: ReportPostRepository,
    private val reportCommentRepository: ReportCommentRepository,
    private val reportUserRepository: ReportUserRepository
) {
    // 관리자 검증
    private fun validateAdmin(adminId: Long) {
        val admin = userRepository.findByIdOrNull(adminId)
            ?: throw IllegalArgumentException("유효하지 않은 계정입니다.")
        check(admin.role == Role.ADMIN) { "관리자 권한이 없습니다." }
    }

    // 관리자가 명시적으로 게시물의 상태를 지정
    @Transactional
    fun updatePostStatus(adminId: Long, postId: Long, newStatus: PostStatus) {
        validateAdmin(adminId)
        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("존재하지 않는 게시글입니다.")
        post.status = newStatus
    }

    // 게시물 신고 관리
    @Transactional(readOnly = true)
    fun getPostReports(adminId: Long, status: ReportStatus, pageable: Pageable): Page<AdminPostReportResponse> {
        validateAdmin(adminId)
        return reportPostRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map { report ->
            val media = (listOfNotNull(report.post.videoUrl, report.post.videoThumbnailUrl) + report.post.imageUrls)
            AdminPostReportResponse(
                reportId = report.id,
                reporterId = report.reporter.id,
                reporterNickname = report.reporter.nickname,
                reportedUserId = report.post.user.id,
                reportedUserNickname = report.post.user.nickname,
                postId = report.post.id,
                postTitle = report.post.title,
                mediaUrls = media,
                reason = report.reason,
                detail = report.detail,
                reportStatus = report.status,
                postStatus = report.post.status,
                createdAt = report.createdAt
            )
        }
    }

    @Transactional
    fun acceptPostReport(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportPostRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("존재하지 않는 신고입니다.")

        report.status = ReportStatus.ACCEPTED
        report.post.status = PostStatus.BLINDED
    }

    @Transactional
    fun rejectPostReport(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportPostRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("존재하지 않는 신고입니다.")

        report.status = ReportStatus.REJECTED
    }

    // 댓글 신고 관리
    @Transactional(readOnly = true)
    fun getCommentReports(adminId: Long, status: ReportStatus, pageable: Pageable): Page<AdminCommentReportResponse> {
        validateAdmin(adminId)
        return reportCommentRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map { report ->
            AdminCommentReportResponse(
                reportId = report.id,
                reporterId = report.reporter.id,
                reporterNickname = report.reporter.nickname,
                reportedUserId = report.comment.user.id,
                reportedUserNickname = report.comment.user.nickname,
                commentId = report.comment.id,
                commentContent = report.comment.content,
                reason = report.reason,
                detail = report.detail,
                reportStatus = report.status,
                commentStatus = report.comment.status,
                createdAt = report.createdAt
            )
        }
    }

    @Transactional
    fun acceptCommentReport(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportCommentRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("존재하지 않는 신고입니다.")

        report.status = ReportStatus.ACCEPTED
        report.comment.status = CommentStatus.BLINDED
    }

    @Transactional
    fun rejectCommentReport(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportCommentRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("존재하지 않는 신고입니다.")

        report.status = ReportStatus.REJECTED
    }

    // 유저 신고 관리
    @Transactional(readOnly = true)
    fun getUserReports(adminId: Long, status: ReportStatus, pageable: Pageable): Page<AdminUserReportResponse> {
        validateAdmin(adminId)
        return reportUserRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map { report ->
            AdminUserReportResponse(
                reportId = report.id,
                reporterId = report.reporter.id,
                reporterNickname = report.reporter.nickname,
                reportedUserId = report.reported.id,
                reportedUserNickname = report.reported.nickname,
                reportedUserReportCount = report.reported.reportCount,
                reportedUserStatus = report.reported.status,
                reason = report.reason,
                detail = report.detail,
                reportStatus = report.status,
                createdAt = report.createdAt
            )
        }
    }

    @Transactional
    fun acceptUserReport(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportUserRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("존재하지 않는 신고입니다.")

        report.status = ReportStatus.ACCEPTED
        report.reported.ban()
    }

    @Transactional
    fun rejectUserReport(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportUserRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("존재하지 않는 신고입니다.")

        report.status = ReportStatus.REJECTED
    }
}