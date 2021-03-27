package com.example.dawnlightclinicalstudy.data

import retrofit2.Response

sealed class DataState<out T> {

    data class Success<out T>(val value: T) : DataState<T>()

    data class HttpError(val responseError: Response<*>) : DataState<Nothing>()

    data class GenericError(val throwable: Throwable) : DataState<Nothing>()
}
