package com.example.backend.like

import com.example.backend.post.Post
import com.example.backend.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "post_likes",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_like_post_user_post",
            columnNames = ["user_id", "post_id"]
        )
    ]
)
class PostLike(
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