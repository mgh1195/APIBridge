package com.wrapper.apibridge.service

import com.wrapper.apibridge.api.dto.SearchBookResultDto
import com.wrapper.apibridge.api.dto.BookItem
import com.wrapper.apibridge.api.dto.SearchBookDto
import okhttp3.OkHttpClient
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


@Service
class SearchBookService {

    @Cacheable(value = ["items"], key = "{#dto.name,#pageable.offset,#pageable.pageSize}")
    fun searchBook(dto: SearchBookDto, pageable: Pageable): List<BookItem> {
        return callApi(dto.name!!, pageable.offset, pageable.pageSize).items ?: listOf()
    }

    private fun callApi(name: String, offset: Long, pageSize: Int): SearchBookResultDto {

        val retrofit = createRetrofit()
        val service = retrofit.create(GoogleSearchBook::class.java)
        val callSync = service.searchBook(bookName = name, offset = offset, pageSize = pageSize)

//        try {
//
//        val response = callSync.execute()
//        }catch (e:Exception){
//            val a=0
//        }
        val response = callSync.execute()
        //TODO(Throw custom exception)
        if (!response.isSuccessful)
            throw Exception()

        //Body must have value, If not found any book, api return object with zero total.
        //TODO(Customize exception)
        return response.body()?.copy(
            //Sort items when receive it.
            items = response.body()!!.items?.sortedBy { it.volumeInfo.title } ?: listOf()
        ) ?: throw Exception()
    }

    private fun createRetrofit(url: String = "https://www.googleapis.com/books/v1/"): Retrofit {
        val httpClient = OkHttpClient.Builder()
        return Retrofit.Builder()
            //TODO(Move url to configuration)
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()
    }

}

//https://www.googleapis.com/books/v1/volumes?q=harry+potter:keyes&key=%7BKEY%7D
interface GoogleSearchBook {
    @GET("volumes")
    fun searchBook(
        //TODO(Ask about filter in end of q, like :keyes in sample search)
        @Query("q") bookName: String,
        //TODO(Move key to configuration)
        @Query("key") key: String = "",
        @Query("startIndex") offset: Long,
        @Query("maxResults") pageSize: Int,
    ): Call<SearchBookResultDto>

}
