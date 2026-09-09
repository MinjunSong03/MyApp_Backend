package com.example.backend.auth.controller

import com.example.backend.auth.CreatePostRequest
import com.example.backend.auth.CreateReportRequest
import com.example.backend.auth.EditPostRequest
import com.example.backend.auth.PostResponse
import com.example.backend.auth.service.PostService
import com.example.backend.common.CurrentUserId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPost(
        @CurrentUserId userId: Long,
        @RequestBody request: CreatePostRequest
    ): PostResponse {
        return postService.createPost(userId, request)
    }

    @GetMapping
    fun getHomeFeed(
        @CurrentUserId userId: Long,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): Slice<PostResponse> {
        return postService.getHomeFeed(userId, pageable)
    }

    @GetMapping("/my_posts_act")
    fun getMyActPost(
        @CurrentUserId userId: Long,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): Slice<PostResponse> {
        return postService.getMyActPost(userId, pageable)
    }

    @GetMapping("/my_posts_hidden")
    fun getMyHiddenPost(
        @CurrentUserId userId: Long,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): Slice<PostResponse> {
        return postService.getMyHiddenPost(userId, pageable)
    }

    @GetMapping("/{postId}/get")
    fun getPostById(
        @CurrentUserId userId: Long,
        @PathVariable postId: Long
    ): PostResponse {
        return postService.getPostById(userId, postId)
    }

    @GetMapping("/{postId}/detail")
    fun getPostDetail(
        @CurrentUserId userId: Long,
        @PathVariable postId: Long
    ): PostResponse {
        return postService.getPostDetail(userId, postId)
    }

    @PatchMapping("/{postId}/edit")
    fun editPost(
        @CurrentUserId userId: Long,
        @PathVariable postId: Long,
        @RequestBody request: EditPostRequest
    ): PostResponse {
        return postService.editPost(userId, postId, request)
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePost(
        @CurrentUserId userId: Long,
        @PathVariable postId: Long
    ) {
        postService.deletePost(userId, postId)
    }

    @PostMapping("/{postId}/hide")
    fun hidePost(
        @CurrentUserId userId: Long,
        @PathVariable postId: Long
    ) {
        return postService.hidePost(userId, postId)
    }

    @DeleteMapping("/{postId}/unhide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unhidePost(
        @CurrentUserId userId: Long,
        @PathVariable postId: Long
    ) {
        postService.unhidePost(userId, postId)
    }

    @PostMapping("/{postId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    fun reportPost(
        @CurrentUserId userId: Long,
        @PathVariable postId: Long,
        @RequestBody request: CreateReportRequest
    ) {
        postService.reportPost(reporterId = userId, postId = postId, reason = request.reason, detail = request.detail)
    }

    @GetMapping("/user/{targetUserId}")
    fun getUserPosts(
        @CurrentUserId currentUserId: Long,
        @PathVariable targetUserId: Long,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): Slice<PostResponse> {
        return postService.getUserPosts(currentUserId = currentUserId, targetUserId = targetUserId, pageable = pageable)
    }
}