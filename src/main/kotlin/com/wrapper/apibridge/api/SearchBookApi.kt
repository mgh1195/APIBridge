package com.wrapper.apibridge.api

import com.wrapper.apibridge.api.dto.SearchBookDto
import com.wrapper.apibridge.service.SearchBookService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//TODO(Manage logs)
@RestController
@RequestMapping("/api/v1/search-books")
class SearchBookApi(
    private val searchBookService: SearchBookService
) {

    //TODO(Add metrics)
    @PostMapping
    fun search(
        //TODO(Add validation)
        @RequestBody dto: SearchBookDto,
        @PageableDefault(size = 5) pageable: Pageable,
    ) =
        searchBookService.searchBook(dto, pageable)

}
