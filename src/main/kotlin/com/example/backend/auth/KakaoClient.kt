package com.example.backend.auth

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClientResponseException

data class KakaoUserResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?
)

data class KakaoAccount(
    val profile: KakaoProfile?
)

data class KakaoProfile(
    val nickname: String?,
    @JsonProperty("profile_image_url")
    val profileImageUrl: String?
)

@Component
class KakaoClient(
    @Value("\${kakao.admin-key}")
    private val kakaoAdminKey: String
) {
    private val restClient = RestClient.builder()
        .baseUrl("https://kapi.kakao.com")
        .build()

    fun getUserInfo(accessToken: String): KakaoUserResponse {
        return try {
            restClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .retrieve()
                .body(KakaoUserResponse::class.java)
                ?: throw IllegalArgumentException("카카오 유저 정보를 불러오지 못했습니다.")
        } catch (e: RestClientResponseException) {
            throw IllegalArgumentException("Invalid Kakao Access Token: ${e.statusCode}")
        }
    }

    fun unlink(oauthId: String) {
        try {
            restClient.post()
                .uri("/v1/user/unlink")
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK $kakaoAdminKey")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("target_id_type=user_id&target_id=$oauthId")
                .retrieve()
                .toBodilessEntity()
        } catch (e: RestClientResponseException) {
            System.err.println("Kakao Admin Unlink Response: ${e.message}")
        }
    }
}