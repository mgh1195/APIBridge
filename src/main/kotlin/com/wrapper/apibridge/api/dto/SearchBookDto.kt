package com.wrapper.apibridge.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class SearchBookDto(

    @field:NotNull(message = "search_book.name_cannot_be_null")
    @field:NotBlank(message = "search_book.name_cannot_be_blank")
    val name: String?,
)
