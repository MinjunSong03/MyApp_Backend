package com.example.backend.report

import org.springframework.data.jpa.repository.JpaRepository

interface ReportCommentRepository: JpaRepository<ReportComment, Long> {
    fun existsByReporterIdAndCommentId(reporterId: Long, commentId: Long): Boolean
    fun deleteAllByCommentId(commentId: Long)
}