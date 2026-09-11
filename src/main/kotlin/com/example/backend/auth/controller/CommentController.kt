package com.example.backend.auth.controller

import com.example.backend.auth.CommentResponse
import com.example.backend.auth.CreateCommentRequest
import com.example.backend.auth.CreateReportRequest
import com.example.backend.auth.EditCommentRequest
import com.example.backend.auth.service.CommentService
import com.example.backend.common.CurrentUserId
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Pageable

@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentService: CommentService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createComment(
        @CurrentUserId userId: Long,
        @RequestBody request: CreateCommentRequest
    ): CommentResponse {
        return commentService.createComment(userId, request)
    }

    @GetMapping
    fun getComments(
        @CurrentUserId userId: Long,
        @RequestParam postId: Long,
        @PageableDefault(
            size = 15,
            sort = ["createdAt"],
            direction = Sort.Direction.ASC
        ) pageable: Pageable
    ): Slice<CommentResponse> {
        return commentService.getComments(userId, postId, pageable)
    }

    @PatchMapping("/{commentId}")
    fun editComment(
        @CurrentUserId userId: Long,
        @PathVariable commentId: Long,
        @RequestBody request: EditCommentRequest
    ): CommentResponse {
        return commentService.editComment(userId, commentId, request)
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteComment(
        @CurrentUserId userId: Long,
        @PathVariable commentId: Long
    ) {
        commentService.deleteComment(userId, commentId)
    }

    @PostMapping("/{commentId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    fun reportComment(
        @CurrentUserId userId: Long,
        @PathVariable commentId: Long,
        @RequestBody request: CreateReportRequest
    ) {
        commentService.reportComment(
            reporterId = userId,
            commentId = commentId,
            reason = request.reason,
            detail = request.detail
        )
    }
}