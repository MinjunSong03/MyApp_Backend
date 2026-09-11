package com.example.backend.report

import com.example.backend.comment.Comment
import com.example.backend.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "report_comments",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_reporter_comment",
            columnNames = ["reporter_id", "comment_id"]
        )
    ]
)
class ReportComment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    val reporter: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    val comment: Comment,

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