package com.wrapper.apibridge.api.dto

import java.io.Serializable

data class SearchBookResultDto(
    val kind: String,
    val totalItems: Int,
    val items: List<BookItem>? = listOf()
) : Serializable

fun List<BookItem>.sortBookItems() =
    this.sortedBy { it.id }
