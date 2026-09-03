package com.example.backend.auth.service

import com.example.backend.auth.CreatePostRequest
import com.example.backend.auth.EditPostRequest
import com.example.backend.auth.PostResponse
import com.example.backend.post.Post
import com.example.backend.post.PostRepository
import com.example.backend.post.PostStatus
import com.example.backend.report.Report
import com.example.backend.report.ReportReason
import com.example.backend.report.ReportRepository
import com.example.backend.user.UserRepository
import com.example.backend.userHiddenPost.UserHiddenPostRepository
import com.example.backend.userblock.UserBlockRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.example.backend.userHiddenPost.UserHiddenPost

@Service
class PostService (
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val userBlockRepository: UserBlockRepository,
    private val reportRepository: ReportRepository,
    private val userHiddenPostRepository: UserHiddenPostRepository,
    private val mediaService: MediaService
) {
    @Transactional
    fun createPost(userId: Long, request: CreatePostRequest): PostResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user.")

        val post = Post(
            user = user,
            title = request.title,
            description = request.description,
            videoUrl = request.videoUrl,
            videoThumbnailUrl = request.videoThumbnailUrl,
            imageUrls = request.imageUrls.toMutableList()
        )
        return PostResponse.from(postRepository.save(post), userId)
    }

    @Transactional(readOnly = true)
    fun getHomeFeed(userId: Long, pageable: Pageable): Slice<PostResponse> {

        val posts = postRepository.findFilteredFeed(PostStatus.ACTIVE, userId, pageable)

        return posts.map { PostResponse.from(post = it, currentUserId = userId) }
    }

    @Transactional(readOnly = true)
    fun getMyActPost(userId: Long, pageable: Pageable): Slice<PostResponse> {
        val posts = postRepository.findByStatusAndUserIdAndIsHiddenFalse(PostStatus.ACTIVE, userId, pageable)

        return posts.map { PostResponse.from(post = it, currentUserId = userId) }
    }

    @Transactional(readOnly = true)
    fun getMyHiddenPost(userId: Long, pageable: Pageable): Slice<PostResponse> {
        val posts = postRepository.findByStatusAndUserIdAndIsHiddenTrue(PostStatus.ACTIVE, userId, pageable)

        return posts.map { PostResponse.from(post = it, currentUserId = userId) }
    }

    @Transactional(readOnly = true)
    fun getPostById(userId: Long, postId: Long): PostResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user.")

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("존재하지 않는 게시글입니다.")

        require(post.user.id == user.id) { "나의 게시물이 아닙니다." }

        return PostResponse.from(post = post, currentUserId = userId)
    }

    @Transactional(readOnly = true)
    fun getPostDetail(userId: Long, postId: Long): PostResponse {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("Invalid post")

        if (post.status != PostStatus.ACTIVE) {
            throw IllegalStateException("삭제되었거나 블라인드 처리된 게시글입니다.")
        }

        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId = userId, blockedId = post.user.id)) {
            throw IllegalStateException("차단한 사용자의 게시글은 열람할 수 없습니다.")
        }

        post.incrementViewCount()
        return PostResponse.from(post, userId)
    }

    @Transactional
    fun editPost(userId: Long, postId: Long, request: EditPostRequest): PostResponse {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("존재하지 않는 게시글입니다.")

        require(post.user.id == userId) { "게시글 수정 권한이 없습니다." }

        post.edit(
            title = request.title,
            description = request.description,
            videoUrl = request.videoUrl,
            videoThumbnailUrl = request.videoThumbnailUrl,
            imageUrls = request.imageUrls
            )
        return PostResponse.from(post, userId)
    }

    @Transactional
    fun deletePost(userId: Long, postId: Long) {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("존재하지 않는 게시글입니다.")

        require(post.user.id == userId) { "게시글 삭제 권한이 없습니다." }

        if (post.status == PostStatus.BLINDED) {
            throw IllegalArgumentException("신고로 인해 검토중인 게시글입니다.")
        }

        post.videoUrl?.let { mediaService.deleteMediaFromR2(it) }
        post.videoThumbnailUrl?.let { thumb ->
            if (thumb != post.videoUrl) mediaService.deleteMediaFromR2(thumb)
        }
        post.imageUrls.forEach { imageUrl ->
            mediaService.deleteMediaFromR2(imageUrl)
        }

        userHiddenPostRepository.deleteAllByPostId(postId)
        reportRepository.deleteAllByPostId(postId)

        postRepository.delete(post)
    }

    @Transactional
    fun reportPost(reporterId: Long, postId: Long, reason: ReportReason, detail: String) {
        val reporter = userRepository.findByIdOrNull(reporterId)
            ?: throw IllegalArgumentException("신고자를 찾을 수 없습니다.")
        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("해당 게시글을 찾을 수 없습니다.")

        require(post.user.id != reporterId) { "자신의 게시글은 신고할 수 없습니다." }

        if (reportRepository.existsByReporterIdAndPostId(reporterId, postId)) {
            throw IllegalStateException("이미 신고한 게시글입니다.")
        }

        val report = Report(
            reporter = reporter,
            post = post,
            reason = reason,
            detail = detail
        )
        reportRepository.save(report)

        post.incrementReportCount()
    }

    @Transactional
    fun hidePost(userId: Long, postId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user.")

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("해당 게시글을 찾을 수 없습니다.")

        if (user.id == post.user.id) {
            post.hide()
        } else {
            if (userHiddenPostRepository.existsByUserIdAndPostId(userId, postId)) {
                throw IllegalArgumentException("이미 숨김 처리된 게시물입니다.")
            }

            val hiddenPost = UserHiddenPost(user = user, post = post)
            userHiddenPostRepository.save(hiddenPost)
        }
    }

    @Transactional
    fun unhidePost(userId: Long, postId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user.")

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("해당 게시글을 찾을 수 없습니다.")

        if (user.id == post.user.id) {
            post.unhide()
    } else {
            throw IllegalArgumentException("본인의 게시물만 숨김 해제 가능합니다.")
        }
    }
}



