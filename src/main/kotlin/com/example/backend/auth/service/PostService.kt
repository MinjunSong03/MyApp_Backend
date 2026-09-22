package com.example.backend.auth.service

import com.example.backend.auth.CreatePostRequest
import com.example.backend.auth.EditPostRequest
import com.example.backend.auth.LikeResponse
import com.example.backend.auth.PostResponse
import com.example.backend.comment.CommentRepository
import com.example.backend.like.PostLike
import com.example.backend.like.PostLikeRepository
import com.example.backend.post.Post
import com.example.backend.post.PostRepository
import com.example.backend.post.PostStatus
import com.example.backend.report.ReportPost
import com.example.backend.report.ReportReason
import com.example.backend.report.ReportPostRepository
import com.example.backend.user.UserRepository
import com.example.backend.user.UserStatus
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
    private val reportPostRepository: ReportPostRepository,
    private val commentRepository: CommentRepository,
    private val userHiddenPostRepository: UserHiddenPostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val mediaService: MediaService
) {
    private fun toPostResponseSlice(posts: Slice<Post>, currentUserId: Long): Slice<PostResponse> {
        if (posts.isEmpty) {
            return posts.map { PostResponse.from(it, currentUserId, false) }
        }

        val postIds = posts.content.map { it.id }

        val likedPostIdSet = postLikeRepository.findLikedPostIdsByUserIdAndPostIdIn(currentUserId, postIds).toSet()

        return posts.map { post ->
            PostResponse.from(
                post = post,
                currentUserId = currentUserId,
                isLiked = post.id in likedPostIdSet
            )
        }
    }

    @Transactional
    fun createPost(userId: Long, request: CreatePostRequest): PostResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        check(user.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

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
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val posts = postRepository.findFilteredFeed(PostStatus.ACTIVE, userId, pageable)

        return toPostResponseSlice(posts, user.id)
    }

    @Transactional(readOnly = true)
    fun getMyActPost(userId: Long, pageable: Pageable): Slice<PostResponse> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val posts = postRepository.findByStatusAndUserIdAndIsHiddenFalseOrderByCreatedAtDesc(PostStatus.ACTIVE, userId, pageable)

        return toPostResponseSlice(posts, user.id)
    }

    @Transactional(readOnly = true)
    fun getMyHiddenPost(userId: Long, pageable: Pageable): Slice<PostResponse> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val posts = postRepository.findByStatusAndUserIdAndIsHiddenTrueOrderByCreatedAtDesc(PostStatus.ACTIVE, userId, pageable)

        return toPostResponseSlice(posts, user.id)
    }

    @Transactional(readOnly = true)
    fun getPostById(userId: Long, postId: Long): PostResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("존재하지 않는 게시물입니다.")

        require(post.user.id == user.id) { "나의 게시물이 아닙니다." }

        val isLiked = postLikeRepository.existsByUserIdAndPostId(userId = user.id, postId = post.id)
        return PostResponse.from(post = post, currentUserId = user.id, isLiked = isLiked)
    }

    @Transactional
    fun getPostDetail(userId: Long, postId: Long): PostResponse {
        postRepository.incrementViewCount(postId)

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("Invalid post")

        if (post.status != PostStatus.ACTIVE) {
            throw IllegalStateException("삭제되었거나 블라인드 처리된 게시물입니다.")
        }

        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId = userId, blockedId = post.user.id)) {
            throw IllegalStateException("차단한 사용자의 게시물은 열람할 수 없습니다.")
        }

        val isLiked = postLikeRepository.existsByUserIdAndPostId(userId = userId, postId = postId)

        return PostResponse.from(post =  post, currentUserId = userId, isLiked =  isLiked)
    }

    @Transactional
    fun editPost(userId: Long, postId: Long, request: EditPostRequest): PostResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        check(user.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("존재하지 않는 게시물입니다.")

        require(post.user.id == user.id) { "게시물 수정 권한이 없습니다." }

        post.edit(
            title = request.title,
            description = request.description,
            videoUrl = request.videoUrl,
            videoThumbnailUrl = request.videoThumbnailUrl,
            imageUrls = request.imageUrls
            )

        val isLiked = postLikeRepository.existsByUserIdAndPostId(userId = user.id, postId = post.id)
        return PostResponse.from(post = post, currentUserId = user.id, isLiked = isLiked)
    }

    @Transactional
    fun deletePost(userId: Long, postId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        check(user.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("존재하지 않는 게시물입니다.")

        require(post.user.id == user.id) { "게시물 삭제 권한이 없습니다." }

        if (post.status == PostStatus.BLINDED) {
            throw IllegalArgumentException("신고로 인해 검토중인 게시물입니다.")
        }

        val mediaUrlsToDelete = (listOf(post.videoUrl, post.videoThumbnailUrl) + post.imageUrls).distinct()

        postLikeRepository.deleteAllByPostId(postId)
        mediaService.deleteMediaFromR2(mediaUrlsToDelete)
        commentRepository.deleteAllByPostId(postId)
        userHiddenPostRepository.deleteAllByPostId(postId)
        reportPostRepository.deleteAllByPostId(postId)
        postRepository.delete(post)
    }

    @Transactional
    fun reportPost(reporterId: Long, postId: Long, reason: ReportReason, detail: String) {
        val reporter = userRepository.findByIdOrNull(reporterId)
            ?: throw IllegalArgumentException("신고자를 찾을 수 없습니다.")

        check(reporter.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("해당 게시물을 찾을 수 없습니다.")

        require(post.user.id != reporterId) { "자신의 게시물은 신고할 수 없습니다." }

        if (reportPostRepository.existsByReporterIdAndPostId(reporterId, postId)) {
            throw IllegalStateException("이미 신고한 게시물입니다.")
        }

        val report = ReportPost(
            reporter = reporter,
            post = post,
            reason = reason,
            detail = detail
        )
        reportPostRepository.save(report)

        post.incrementReportCount()
    }

    @Transactional
    fun hidePost(userId: Long, postId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        check(user.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("해당 게시물을 찾을 수 없습니다.")

        if (user.id == post.user.id) {
            post.hide()
        } else {
            if (userHiddenPostRepository.existsByUserIdAndPostId(user.id, post.id)) {
                throw IllegalArgumentException("이미 숨김 처리된 게시물입니다.")
            }

            val hiddenPost = UserHiddenPost(user = user, post = post)
            userHiddenPostRepository.save(hiddenPost)
        }
    }

    @Transactional
    fun unhidePost(userId: Long, postId: Long): PostResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        check(user.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("해당 게시물을 찾을 수 없습니다.")

        if (user.id == post.user.id) {
            post.unhide()
            val isLiked = postLikeRepository.existsByUserIdAndPostId(userId = user.id, postId = post.id)
            return PostResponse.from(post = post, currentUserId = userId, isLiked = isLiked)
        } else {
            throw IllegalArgumentException("본인의 게시물만 숨김 해제 가능합니다.")
        }
    }

    @Transactional(readOnly = true)
    fun getUserPosts(currentUserId: Long, targetUserId: Long, pageable: Pageable): Slice<PostResponse> {
        val targetUser = userRepository.findByIdOrNull(targetUserId)
            ?: throw IllegalArgumentException("존재하지 않는 사용자입니다.")

        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId = currentUserId, blockedId = targetUserId)) {
            throw IllegalStateException("차단한 사용자의 게시물은 열람할 수 없습니다.")
        }

        val posts = postRepository.findByStatusAndUserIdAndIsHiddenFalseOrderByCreatedAtDesc(
            status = PostStatus.ACTIVE,
            userId = targetUser.id,
            pageable = pageable
        )

        return toPostResponseSlice(posts, currentUserId)
    }

    @Transactional(readOnly = true)
    fun getMyLikedPosts(userId: Long, pageable: Pageable): Slice<PostResponse> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        val posts = postRepository.findLikedPosts(
            userId = user.id,
            status = PostStatus.ACTIVE,
            pageable = pageable
        )

        return posts.map { PostResponse.from(post = it, currentUserId = user.id, isLiked = true) }
    }

    @Transactional
    fun likePost(userId: Long, postId: Long): LikeResponse {
        if (postLikeRepository.existsByUserIdAndPostId(userId =  userId, postId =  postId)) {
            throw IllegalStateException("이미 '좋아요'한 게시물입니다.")
        }
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("Invalid user")

        check(user.status == UserStatus.ACTIVE) { "이용이 정지된 계정입니다." }

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("해당 게시물을 찾을 수 없습니다.")

        val postLike = PostLike(user = user, post = post)
        postLikeRepository.save(postLike)
        postRepository.incrementLikeCount(postId)

        return LikeResponse(isLiked = true, likeCount = post.likeCount + 1)
    }

    @Transactional
    fun unlikePost(userId: Long, postId: Long): LikeResponse {
        if (!postLikeRepository.existsByUserIdAndPostId(userId = userId, postId = postId)) {
            throw IllegalStateException("좋아요를 누르지 않은 게시물입니다.")
        }

        val post = postRepository.findByIdOrNull(postId)
            ?: throw IllegalArgumentException("해당 게시물을 찾을 수 없습니다.")

        postLikeRepository.deleteByUserIdAndPostId(userId = userId, postId = postId)
        postRepository.decrementLikeCount(postId)

        val updatedCount = (post.likeCount - 1).coerceAtLeast(0)
        return LikeResponse(isLiked = false, likeCount = updatedCount)
    }
}



