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
import com.example.backend.user.UserStatus
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
                reportedId = report.post.user.id,
                reportedNickname = report.post.user.nickname,
                postId = report.post.id,
                postTitle = report.post.title,
                postDescription = report.post.description,
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

        val post = postRepository.findByIdOrNull(report.post.id)
            ?: throw IllegalArgumentException("게시물을 찾을 수 없습니다.")

        post.blind()
        report.accept()
        postRepository.save(post)
        reportPostRepository.save(report)
    }

    @Transactional
    fun rejectPostReport(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportPostRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("존재하지 않는 신고입니다.")

        report.reject()
        reportPostRepository.save(report)
        reportPostRepository.deleteAllByPostId(report.post.id)
    }

    @Transactional
    fun restorePost(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportPostRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("신고 내역을 찾을 수 없습니다.")

        val post = postRepository.findByIdOrNull(report.post.id)
            ?: throw IllegalArgumentException("게시물을 찾을 수 없습니다.")

        post.unblind()
        report.restore()
        postRepository.save(post)
        reportPostRepository.save(report)
        reportPostRepository.deleteAllByPostId(report.post.id)
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
                reportedId = report.comment.user.id,
                reportedNickname = report.comment.user.nickname,
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

        val comment = commentRepository.findByIdOrNull(report.comment.id)
            ?: throw IllegalArgumentException("댓글을 찾을 수 없습니다.")

        comment.blind()
        report.accept()
        commentRepository.save(comment)
        reportCommentRepository.save(report)
    }

    @Transactional
    fun rejectCommentReport(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportCommentRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("존재하지 않는 신고입니다.")

        report.reject()
        reportCommentRepository.save(report)
        reportCommentRepository.deleteAllByCommentId(report.comment.id)
    }

    @Transactional
    fun restoreComment(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportCommentRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("신고 내역을 찾을 수 없습니다.")

        val comment = commentRepository.findByIdOrNull(report.comment.id)
            ?: throw IllegalArgumentException("댓글을 찾을 수 없습니다.")

        comment.unblind()
        report.restore()
        commentRepository.save(comment)
        reportCommentRepository.save(report)
        reportCommentRepository.deleteAllByCommentId(report.comment.id)
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
                reportedId = report.reported.id,
                reportedNickname = report.reported.nickname,
                reportedReportCount = report.reported.reportCount,
                reportedStatus = report.reported.status,
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

        val user = userRepository.findByIdOrNull(report.reported.id)
            ?: throw IllegalArgumentException("유저를 찾을 수 없습니다.")

        user.ban()
        report.accept()
        userRepository.save(user)
        reportUserRepository.save(report)
    }

    @Transactional
    fun rejectUserReport(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportUserRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("존재하지 않는 신고입니다.")

        report.reject()
        reportUserRepository.save(report)
        reportUserRepository.deleteAllByReportedId(report.reported.id)
    }

    @Transactional
    fun unbanUser(adminId: Long, reportId: Long) {
        validateAdmin(adminId)
        val report = reportUserRepository.findByIdOrNull(reportId)
            ?: throw IllegalArgumentException("신고 내역을 찾을 수 없습니다.")

        val user = userRepository.findByIdOrNull(report.reported.id)
            ?: throw IllegalArgumentException("유저를 찾을 수 없습니다.")

        user.unban()
        report.restore()
        userRepository.save(user)
        reportUserRepository.save(report)
        reportUserRepository.deleteAllByReportedId(report.reported.id)
    }
}