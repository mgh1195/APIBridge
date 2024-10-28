package com.wrapper.apibridge.service

import com.wrapper.apibridge.api.dto.SearchBookResultDto
import com.wrapper.apibridge.api.dto.BookItem
import com.wrapper.apibridge.api.dto.SearchBookDto
import com.wrapper.apibridge.configuration.GoogleSearchBook
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


@Service
class SearchBookService(
    private val googleSearchBook: GoogleSearchBook,
) {

    @Cacheable(value = ["items"], key = "{#dto.name,#pageable.offset,#pageable.pageSize}")
    fun searchBook(dto: SearchBookDto, pageable: Pageable): List<BookItem> {
        return callApi(dto.name!!, pageable.offset, pageable.pageSize).items ?: listOf()
    }

    private fun callApi(name: String, offset: Long, pageSize: Int): SearchBookResultDto {

        val callSync = googleSearchBook.searchBook(name, offset, pageSize)

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

}
