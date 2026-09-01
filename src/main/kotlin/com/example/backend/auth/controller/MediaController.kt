package com.example.backend.auth.controller

import com.example.backend.auth.ImagePresignedRequest
import com.example.backend.auth.PresignedUrlResponse
import com.example.backend.auth.VideoPresignedRequest
import com.example.backend.auth.VideoPresignedUrlResponse
import com.example.backend.auth.service.MediaService
import com.example.backend.common.CurrentUserId
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/media")
class MediaController(
    private val mediaService: MediaService
) {
    @PostMapping("/image-presigned")
    fun getImagePresignedUrl(
        @CurrentUserId userId: Long,
        @RequestBody request: ImagePresignedRequest
    ): PresignedUrlResponse {
        return mediaService.generateImagePresignedUrl(request)
    }

    @PostMapping("/video-presigned")
    fun getVideoPresignedUrl(
        @CurrentUserId userId: Long,
        @RequestBody request: VideoPresignedRequest
    ): VideoPresignedUrlResponse {
        return mediaService.generateVideoPresignedUrl(request)
    }
}