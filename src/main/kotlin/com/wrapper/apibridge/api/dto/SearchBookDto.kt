package com.wrapper.apibridge.api.dto

class SearchBookDto(

    //TODO(Add validation)
    val name: String?,
){

    fun getNameAsQueryParam() {
        val a = name
    }
}
