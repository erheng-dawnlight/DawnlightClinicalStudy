package com.example.dawnlightclinicalstudy.data_source

import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitService {
    @POST("1/signal/{device_id}")
    fun uploadLifeSignal(
        @Path("device_id") deviceId: String,
    )
}
