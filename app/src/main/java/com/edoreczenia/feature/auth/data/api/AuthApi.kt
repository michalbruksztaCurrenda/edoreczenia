package com.edoreczenia.feature.auth.data.api

import com.edoreczenia.feature.auth.data.dto.request.LoginRequestDto
import com.edoreczenia.feature.auth.data.dto.request.RefreshTokenRequestDto
import com.edoreczenia.feature.auth.data.dto.response.LoginResponseDto
import com.edoreczenia.feature.auth.data.dto.response.RefreshTokenResponseDto
import com.edoreczenia.feature.auth.data.dto.response.SessionCheckResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Interfejs Retrofit dla endpointów auth.
 * Na tym etapie zawiera wyłącznie endpointy potrzebne do logowania.
 * Rejestracja i weryfikacja e-mail zostaną dodane w kolejnych user stories.
 */
interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body body: RefreshTokenRequestDto): RefreshTokenResponseDto

    @GET("api/v1/auth/session")
    suspend fun checkSession(): SessionCheckResponseDto
}

