package com.example.dawnlightclinicalstudy.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

/**
 * Reference: https://medium.com/@douglas.iacovelli/how-to-handle-errors-with-retrofit-and-coroutines-33e7492a912
 */
suspend fun <T : Any> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T
): DataState<T> {
    return withContext(dispatcher) {
        try {
            DataState.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            if (throwable is HttpException) {
                DataState.HttpError(throwable.response() as Response<*>)
            } else {
                DataState.GenericError(throwable)
            }
        }
    }
}