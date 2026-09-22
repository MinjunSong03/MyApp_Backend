package com.example.backend.like

import com.example.backend.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_likes",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_like_from_to",
            columnNames = ["from_user_id", "to_user_id"]
        )
    ]
)
class UserLike(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    val fromUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    val toUser: User,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)