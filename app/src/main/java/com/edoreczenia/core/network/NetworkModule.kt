package com.edoreczenia.core.network

import com.edoreczenia.core.session.SessionManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

// BuildConfig jest generowany przez AGP podczas budowania projektu.
// Referencja będzie rozwiązana po pierwszym Gradle sync/build.
private const val BASE_URL_FALLBACK = "https://api.edoreczenia.pl/"

/**
 * Dostawca zależności sieciowych — bez frameworka DI.
 * Pobierz instancje przez [NetworkModule.retrofit] lub [NetworkModule.okHttpClient].
 *
 * Aby podmienić SessionManager (np. w testach), użyj [NetworkModule.create].
 */
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun create(sessionManager: SessionManager): NetworkModule.Holder = Holder(sessionManager)

    class Holder(private val sessionManager: SessionManager) {

        val okHttpClient: OkHttpClient by lazy {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .authenticator(TokenAuthenticator(sessionManager))
                .addInterceptor(loggingInterceptor)
                .build()
        }

        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL_FALLBACK)
                .client(okHttpClient)
                .addConverterFactory(
                    json.asConverterFactory("application/json".toMediaType())
                )
                .build()
        }

        inline fun <reified T> createService(): T = retrofit.create(T::class.java)
    }

    // Domyślna instancja korzystająca z singletona SessionManager
    val default: Holder by lazy { Holder(SessionManager.getInstance()) }
}



