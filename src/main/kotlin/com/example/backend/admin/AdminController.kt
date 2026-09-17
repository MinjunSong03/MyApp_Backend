package com.example.backend.admin

import com.example.backend.common.CurrentUserId
import com.example.backend.post.PostStatus
import com.example.backend.report.ReportStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/reports")
class AdminController(
    private val adminService: AdminService
) {
    // 게시물 신고 관리
    @GetMapping("/posts")
    fun getPostReports(
        @CurrentUserId adminId: Long,
        @RequestParam(defaultValue = "PENDING") status: ReportStatus,
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ResponseEntity<Page<AdminPostReportResponse>> {
        return ResponseEntity.ok(adminService.getPostReports(adminId, status, pageable))
    }

    @PatchMapping("/posts/{reportId}/accept")
    fun acceptPostReport(
        @CurrentUserId adminId: Long,
        @PathVariable reportId: Long
    ): ResponseEntity<Unit> {
        adminService.acceptPostReport(adminId, reportId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/posts/{reportId}/reject")
    fun rejectPostReport(
        @CurrentUserId adminId: Long,
        @PathVariable reportId: Long
    ): ResponseEntity<Unit> {
        adminService.rejectPostReport(adminId, reportId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/posts/{reportId}/restore")
    fun restorePostReport(
        @CurrentUserId adminId: Long,
        @PathVariable reportId: Long
    ): ResponseEntity<Unit> {
        adminService.restorePost(adminId, reportId)
        return ResponseEntity.noContent().build()
    }

    // 댓글 신고 관리
    @GetMapping("/comments")
    fun getCommentReports(
        @CurrentUserId adminId: Long,
        @RequestParam(defaultValue = "PENDING") status: ReportStatus,
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ResponseEntity<Page<AdminCommentReportResponse>> {
        return ResponseEntity.ok(adminService.getCommentReports(adminId, status, pageable))
    }

    @PatchMapping("/comments/{reportId}/accept")
    fun acceptCommentReport(
        @CurrentUserId adminId: Long,
        @PathVariable reportId: Long
    ): ResponseEntity<Unit> {
        adminService.acceptCommentReport(adminId, reportId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/comments/{reportId}/reject")
    fun rejectCommentReport(
        @CurrentUserId adminId: Long,
        @PathVariable reportId: Long
    ): ResponseEntity<Unit> {
        adminService.rejectCommentReport(adminId, reportId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/comments/{reportId}/restore")
    fun restoreCommentReport(
        @CurrentUserId adminId: Long,
        @PathVariable reportId: Long
    ): ResponseEntity<Unit> {
        adminService.restoreComment(adminId, reportId)
        return ResponseEntity.noContent().build()
    }

    // 유저 신고 관리
    @GetMapping("/users")
    fun getUserReports(
        @CurrentUserId adminId: Long,
        @RequestParam(defaultValue = "PENDING") status: ReportStatus,
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ResponseEntity<Page<AdminUserReportResponse>> {
        return ResponseEntity.ok(adminService.getUserReports(adminId, status, pageable))
    }

    @PatchMapping("/users/{reportId}/accept")
    fun acceptUserReport(
        @CurrentUserId adminId: Long,
        @PathVariable reportId: Long
    ): ResponseEntity<Unit> {
        adminService.acceptUserReport(adminId, reportId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/users/{reportId}/reject")
    fun rejectUserReport(
        @CurrentUserId adminId: Long,
        @PathVariable reportId: Long
    ): ResponseEntity<Unit> {
        adminService.rejectUserReport(adminId, reportId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/users/{reportId}/restore")
    fun unbanUserReport(
        @CurrentUserId adminId: Long,
        @PathVariable reportId: Long
    ): ResponseEntity<Unit> {
        adminService.unbanUser(adminId, reportId)
        return ResponseEntity.noContent().build()
    }
}