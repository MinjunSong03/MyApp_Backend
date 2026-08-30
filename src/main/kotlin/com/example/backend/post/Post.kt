package com.example.backend.post

import com.example.backend.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

enum class MediaType { IMAGE, VIDEO }
enum class PostStatus { ACTIVE, BLINDED, DELETED }

@Entity
@Table(name = "posts",
    indexes = [
        Index(name = "idx_posts_status_created", columnList = "status, createdAt DESC")
    ])
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    var title: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var mediaType: MediaType = MediaType.IMAGE,

    @Column(nullable = false)
    var thumbnailUrl: String = "",

    @Column(nullable = false)
    var mediaUrl: String = "",

    @Column(nullable = false)
    var isHidden: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PostStatus = PostStatus.ACTIVE,

    @Column(nullable = false)
    var viewCount: Long = 0,

    @Column(nullable = false)
    var reportCount: Int = 0,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun edit(title: String, description: String, mediaType: MediaType, thumbnailUrl: String, mediaUrl: String) {
        this.title = title
        this.description = description
        this.mediaType = mediaType
        this.thumbnailUrl = thumbnailUrl
        this.mediaUrl = mediaUrl
        this.updatedAt = LocalDateTime.now()
    }

    fun incrementReportCount() {
        this.reportCount += 1
        if (this.reportCount >= 5) {
            this.status = PostStatus.BLINDED
        }
    }

    fun incrementViewCount() {
        this.viewCount += 1
    }

    fun hide() {
        this.isHidden = true
        this.updatedAt = LocalDateTime.now()
    }

    fun unhide() {
        this.isHidden = false
        this.updatedAt = LocalDateTime.now()
    }
}