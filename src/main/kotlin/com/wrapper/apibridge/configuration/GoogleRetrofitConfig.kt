package com.wrapper.apibridge.configuration

import com.wrapper.apibridge.api.dto.SearchBookResultDto
import okhttp3.OkHttpClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@Configuration
class GoogleRetrofitConfig(
    private val googleApiProperties: GoogleApiProperties,
) {

    @Bean
    fun retrofit(): Retrofit {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val url = original.url.newBuilder()
                    .addQueryParameter("key", googleApiProperties.apiKey)
                    .build()
                val request = original.newBuilder().url(url).build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(googleApiProperties.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    @Bean
    fun googleSearchBook(retrofit: Retrofit): GoogleSearchBook {
        return retrofit.create(GoogleSearchBook::class.java)
    }

}

interface GoogleSearchBook {
    @GET("volumes")
    fun searchBook(
        //TODO(Ask about filter in end of q, like :keyes in sample search)
        @Query("q") bookName: String,
        @Query("startIndex") offset: Long,
        @Query("maxResults") pageSize: Int,
    ): Call<SearchBookResultDto>

}

@Configuration
@ConfigurationProperties(prefix = "app.google")
data class GoogleApiProperties(
    var apiKey: String = "",
    var baseUrl: String = "",
    var maxResults: Int = 5
)
