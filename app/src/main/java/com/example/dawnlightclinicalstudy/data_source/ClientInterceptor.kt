package com.example.dawnlightclinicalstudy.data_source

import okhttp3.Interceptor

class ClientInterceptor {

    fun interceptor(token: String): Interceptor {
        return Interceptor {
            val ongoing = it.request().newBuilder()
            ongoing.addHeader("Authorization", token)
            it.proceed(ongoing.build())
        }
    }

}