package com.example.backend.report

import org.springframework.data.jpa.repository.JpaRepository

interface ReportUserRepository: JpaRepository<ReportUser, Long> {
    fun existsByReporterIdAndReportedId(reporterId: Long, reportedId: Long): Boolean
    fun countByReportedId(reportedId: Long): Long
}