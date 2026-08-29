package com.example.backend.userHiddenPost

import com.example.backend.post.Post
import com.example.backend.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_hidden_posts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_post_hide",
            columnNames = ["user_id", "post_id"]
        )
    ],
    indexes = [
        Index(name = "idx_user_hidden_posts_user", columnList = "user_id")
    ]
)
class UserHiddenPost (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)