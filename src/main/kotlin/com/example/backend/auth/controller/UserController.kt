package com.example.backend.auth.controller

import com.example.backend.auth.BlockedUserResponse
import com.example.backend.auth.UpdateProfileRequest
import com.example.backend.auth.service.UserService
import com.example.backend.common.CurrentUserId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    @PatchMapping("/update_profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateProfile(
        @CurrentUserId userId: Long,
        @RequestBody request: UpdateProfileRequest
    ) {
        userService.updateProfile(
            userId = userId,
            newNickname = request.nickname,
            profileImageUrl = request.profileImageUrl,
            deleteProfileImage = request.deleteProfileImage
        )
    }

    @PostMapping("/{targetUserId}/block")
    @ResponseStatus(HttpStatus.CREATED)
    fun blockUser(
        @CurrentUserId userId: Long,
        @PathVariable targetUserId: Long
    ) {
        userService.blockUser(blockerId = userId, blockedId = targetUserId)
    }

    @DeleteMapping("/{targetUserId}/unblock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unblockUser(
        @CurrentUserId userId: Long,
        @PathVariable targetUserId: Long
    ) {
        userService.unblockUser(blockerId = userId, blockedId = targetUserId)
    }

    @GetMapping("/my_blocked_user")
    fun getMyBlockedUser(@CurrentUserId userId: Long,
                      @PageableDefault(
                          size = 10,
                          sort = ["createdAt"],
                          direction = Sort.Direction.DESC
                      ) pageable: Pageable
    ): Slice<BlockedUserResponse> {
        return userService.getMyBlockedUser(userId, pageable)
    }
}