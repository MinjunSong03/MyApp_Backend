package com.example.backend.admin

import com.example.backend.report.ReportStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/admin/reports")
class AdminViewController(
    private val adminService: AdminService
) {
    private val defaultAdminId: Long = 1L

    // 게시물 신고
    @GetMapping("/posts")
    fun postReportsPage(
        @RequestParam(defaultValue = "PENDING") status: ReportStatus,
        @PageableDefault(size = 15, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model
    ): String {
        val reports = adminService.getPostReports(defaultAdminId, status, pageable)
        model.addAttribute("reports", reports)
        model.addAttribute("currentStatus", status)
        return "admin/posts"
    }

    @PatchMapping("/posts/{reportId}/accept")
    fun acceptPostReport(
        @PathVariable reportId: Long,
        model: Model
    ): String {
        adminService.acceptPostReport(defaultAdminId, reportId)
        return "admin/posts :: report-action-done(status='ACCEPTED')"
    }

    @PatchMapping("/posts/{reportId}/reject")
    fun rejectPostReport(
        @PathVariable reportId: Long,
        model: Model
    ): String {
        adminService.rejectPostReport(defaultAdminId, reportId)
        return "admin/posts :: report-action-done(status='REJECTED')"
    }


    // 댓글 신고
    @GetMapping("/comments")
    fun commentReportsPage(
        @RequestParam(defaultValue = "PENDING") status: ReportStatus,
        @PageableDefault(size = 15, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model
    ): String {
        val reports = adminService.getCommentReports(defaultAdminId, status, pageable)
        model.addAttribute("reports", reports)
        model.addAttribute("currentStatus", status)
        return "admin/comments"
    }

    @PatchMapping("/comments/{reportId}/accept")
    fun acceptCommentReport(@PathVariable reportId: Long): String {
        adminService.acceptCommentReport(defaultAdminId, reportId)
        return "admin/comments :: report-action-done(status='ACCEPTED')"
    }

    @PatchMapping("/comments/{reportId}/reject")
    fun rejectCommentReport(@PathVariable reportId: Long): String {
        adminService.rejectCommentReport(defaultAdminId, reportId)
        return "admin/comments :: report-action-done(status='REJECTED')"
    }

    // 유저 신고
    @GetMapping("/users")
    fun userReportsPage(
        @RequestParam(defaultValue = "PENDING") status: ReportStatus,
        @PageableDefault(size = 15, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model
    ): String {
        val reports = adminService.getUserReports(defaultAdminId, status, pageable)
        model.addAttribute("reports", reports)
        model.addAttribute("currentStatus", status)
        return "admin/users"
    }

    @PatchMapping("/users/{reportId}/accept")
    fun acceptUserReport(@PathVariable reportId: Long): String {
        adminService.acceptUserReport(defaultAdminId, reportId)
        return "admin/users :: report-action-done(status='ACCEPTED')"
    }

    @PatchMapping("/users/{reportId}/reject")
    fun rejectUserReport(@PathVariable reportId: Long): String {
        adminService.rejectUserReport(defaultAdminId, reportId)
        return "admin/users :: report-action-done(status='REJECTED')"
    }
}