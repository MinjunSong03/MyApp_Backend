package com.example.backend.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                message = e.message ?: "잘못된 요청입니다.",
                code = "INVALID_ARGUMENT"
            ))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                message = e.message ?: "요청을 처리할 수 없는 상태입니다.",
                code = "INVALID_STATE"
            ))
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientError(e: HttpClientErrorException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(e.statusCode)
            .body(ErrorResponse(
                message = "외부 API 호출 에러: ${e.responseBodyAsString.ifBlank { e.message }}",
                code = "EXTERNAL_API_ERROR"
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                message = e.message ?: "서버 내부 오류가 발생했습니다.",
                code = "INTERNAL_SERVER_ERROR"
            ))
    }
}

data class ErrorResponse(
    val message: String,
    val code: String
)
