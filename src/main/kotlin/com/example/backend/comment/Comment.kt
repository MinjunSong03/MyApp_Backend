package com.example.backend.comment

import com.example.backend.post.Post
import com.example.backend.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

enum class CommentStatus { ACTIVE, BLINDED, DELETED }

@Entity
@Table(
    name = "comments",
    indexes = [
        Index(
            name = "idx_comments_post_created",
            columnList = "post_id, createdAt ASC"
        )
    ]
)
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CommentStatus = CommentStatus.ACTIVE,

    @Column(nullable = false)
    var reportCount: Int = 0,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var editedAt: LocalDateTime? = null,

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun edit(content: String) {
        require(content.isNotBlank()) { "댓글 내용을 입력해 주세요." }
        this.content = content
        this.editedAt = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
    }

    fun incrementReportCount() {
        this.reportCount += 1
        if (this.reportCount >= 5) {
            this.status = CommentStatus.BLINDED
        }
    }

    fun delete() {
        this.status = CommentStatus.DELETED
        this.updatedAt = LocalDateTime.now()
    }
}