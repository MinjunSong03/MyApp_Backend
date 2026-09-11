package com.example.backend.auth.service

import com.example.backend.auth.CommentResponse
import com.example.backend.auth.CreateCommentRequest
import com.example.backend.auth.EditCommentRequest
import com.example.backend.comment.Comment
import com.example.backend.comment.CommentRepository
import com.example.backend.comment.CommentStatus
import com.example.backend.post.PostRepository
import com.example.backend.post.PostStatus
import com.example.backend.report.ReportComment
import com.example.backend.report.ReportCommentRepository
import com.example.backend.report.ReportReason
import com.example.backend.user.UserRepository
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Pageable

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val reportCommentRepository: ReportCommentRepository
) {
    @Transactional
    fun createComment(userId: Long,request: CreateCommentRequest): CommentResponse {
        require(request.content.isNotBlank()) { "댓글 내용을 입력해 주세요." }

        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val post = postRepository.findByIdOrNull(request.postId)
            ?: throw IllegalArgumentException("존재하지 않는 게시글입니다.")

        if (post.status != PostStatus.ACTIVE) {
            throw IllegalStateException("활성화 게시물에만 댓글을 작성할 수 있습니다.")
        }

        val comment = Comment(
            post = post,
            user = user,
            content = request.content
        )

        return CommentResponse.from(commentRepository.save(comment), userId)
    }

    @Transactional(readOnly = true)
    fun getComments(userId: Long, postId: Long, pageable: Pageable): Slice<CommentResponse> {
        userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val comments = commentRepository.findActiveComments(
            postId = postId,
            status = CommentStatus.ACTIVE,
            pageable = pageable
        )

        return comments.map { CommentResponse.from(it, userId) }
    }

    @Transactional
    fun editComment(userId: Long, commentId: Long, request: EditCommentRequest): CommentResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw IllegalArgumentException("존재하지 않는 댓글입니다.")

        require(comment.user.id == user.id) { "댓글 수정 권한이 없습니다." }

        if (comment.status != CommentStatus.ACTIVE) {
            throw IllegalStateException("수정할 수 없는 댓글입니다.")
        }

        comment.edit(request.content)
        return CommentResponse.from(comment, userId)
    }

    @Transactional
    fun deleteComment(userId: Long, commentId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw IllegalArgumentException("존재하지 않는 댓글입니다.")

        require(comment.user.id == user.id) { "댓글 삭제 권한이 없습니다." }

        comment.delete()
        reportCommentRepository.deleteAllByCommentId(commentId)
    }


    @Transactional
    fun reportComment(reporterId: Long, commentId: Long, reason: ReportReason, detail: String) {
        val reporter = userRepository.findByIdOrNull(reporterId)
            ?: throw IllegalArgumentException("신고자를 찾을 수 없습니다.")

        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw IllegalArgumentException("해당 댓글을 찾을 수 없습니다.")

        require(comment.user.id != reporterId) { "자신의 댓글은 신고할 수 없습니다." }

        if (reportCommentRepository.existsByReporterIdAndCommentId(reporterId, commentId)) {
            throw IllegalStateException("이미 신고한 댓글입니다.")
        }

        val report = ReportComment(
            reporter = reporter,
            comment = comment,
            reason = reason,
            detail = detail
        )
        reportCommentRepository.save(report)

        comment.incrementReportCount()
    }
}