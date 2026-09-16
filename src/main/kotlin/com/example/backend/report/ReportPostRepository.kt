package com.example.backend.report

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ReportPostRepository: JpaRepository<ReportPost, Long> {

    fun existsByReporterIdAndPostId(reporterId: Long, postId: Long): Boolean
    fun deleteAllByPostId(postId: Long)

    fun findByStatusOrderByCreatedAtDesc(
        status: ReportStatus,
        pageable: Pageable
    ): Page<ReportPost>
}