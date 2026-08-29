package com.example.backend.report

import com.example.backend.post.Post
import com.example.backend.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

enum class ReportStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

enum class ReportReason {
    SPAM,
    INAPPROPRIATE,
    VIOLENCE,
    COPYRIGHT,
    OTHER
}

@Entity
@Table(name = "reports",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_reporter_post", columnNames = ["reporter_id", "post_id"])
    ])
class Report (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    val reporter: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val reason: ReportReason,

    @Column(nullable = false, columnDefinition = "TEXT")
    val detail: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReportStatus = ReportStatus.PENDING,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)