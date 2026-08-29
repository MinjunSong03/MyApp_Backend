package com.example.backend.userblock

import com.example.backend.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_blocks",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_blocker_blocked", columnNames = ["blocker_id", "blocked_id"])
    ],
    indexes = [
        Index(name = "idx_user_blocks_blocker", columnList = "blocker_id")
    ]
)
class UserBlock (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    val blocker: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    val blocked: User,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)