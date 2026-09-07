package com.example.backend.auth.service

import com.example.backend.auth.ImagePresignedRequest
import com.example.backend.auth.PresignedUrlResponse
import com.example.backend.auth.VideoPresignedRequest
import com.example.backend.auth.VideoPresignedUrlResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.UUID

@Service
class MediaService(
    private val s3Presigner: S3Presigner,
    private val s3Client: S3Client,

    @Value("\${cloudflare.r2.bucket-name}")
    private val bucketName: String,

    @Value("\${cloudflare.r2.public-base-url}")
    private val publicBaseUrl: String
) {
    private fun createPresignedPutUrl(folder: String, originalFileName: String, contentType: String): PresignedUrlResponse {
        val extension = originalFileName.substringAfterLast(".", "bin")
        val uniqueKey = "$folder/${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(uniqueKey)
            .contentType(contentType)
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(5))
            .putObjectRequest(putObjectRequest)
            .build()

        val presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString()
        val fileUrl = "$publicBaseUrl/$uniqueKey"

        return PresignedUrlResponse(
            uploadUrl = presignedUrl,
            fileUrl = fileUrl,
            key = uniqueKey
        )
    }

    fun generateImagePresignedUrl(request: ImagePresignedRequest): PresignedUrlResponse {
        require(request.fileName.isNotBlank()) { "파일명이 비어있습니다." }
        require(request.contentType.isNotBlank()) { "MIME 타입이 비어있습니다." }

        return createPresignedPutUrl("images", request.fileName, request.contentType)
    }

    fun generateVideoPresignedUrl(request: VideoPresignedRequest): VideoPresignedUrlResponse {
        require(request.videoFileName.isNotBlank()) { "동영상 파일명이 비어있습니다." }
        require(request.videoContentType.isNotBlank()) { "동영상 MIME 타입이 비어있습니다." }
        require(request.thumbFileName.isNotBlank()) { "썸네일 파일명이 비어있습니다." }
        require(request.thumbContentType.isNotBlank()) { "썸네일 MIME 타입이 비어있습니다." }

        val videoData = createPresignedPutUrl("videos", request.videoFileName, request.videoContentType)
        val thumbData = createPresignedPutUrl("thumbnails", request.thumbFileName, request.thumbContentType)

        return VideoPresignedUrlResponse(
            video = videoData,
            thumbnail = thumbData
        )
    }

    fun deleteMediaFromR2(fileUrls: List<String?>) {
        val keysToDelete = fileUrls
            .filterNotNull()
            .filter { it.isNotBlank() && it.startsWith(publicBaseUrl) }
            .map { it.removePrefix(publicBaseUrl).trimStart('/') }
            .map { ObjectIdentifier.builder().key(it).build() }

        if (keysToDelete.isEmpty()) return

        val deleteRequest = DeleteObjectsRequest.builder()
            .bucket(bucketName)
            .delete(Delete.builder().objects(keysToDelete).build())
            .build()

        runCatching {
            s3Client.deleteObjects(deleteRequest)
        }.onFailure {
            System.err.println("R2 다중 파일 삭제 실패: ${it.message}")
        }
    }
}