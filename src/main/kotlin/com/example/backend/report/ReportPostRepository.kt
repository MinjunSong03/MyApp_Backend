package com.example.backend.report

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ReportPostRepository: JpaRepository<ReportPost, Long> {

    fun existsByReporterIdAndPostId(reporterId: Long, postId: Long): Boolean

    fun countByPostId(postId: Long): Long

    fun findByStatusOrderByCreatedAtAsc(
        status: ReportStatus,
        pageable: Pageable
    ): Page<ReportPost>

    fun deleteAllByPostId(postId: Long)
}