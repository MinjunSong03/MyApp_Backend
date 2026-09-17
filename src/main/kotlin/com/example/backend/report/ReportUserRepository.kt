package com.example.backend.report

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ReportUserRepository: JpaRepository<ReportUser, Long> {
    fun existsByReporterIdAndReportedId(reporterId: Long, reportedId: Long): Boolean
    fun findByStatusOrderByCreatedAtDesc(
        status: ReportStatus,
        pageable: Pageable
    ): Page<ReportUser>

    fun deleteAllByReportedId(reportedId: Long)
}