package com.example.dawnlightclinicalstudy.data_source

import com.example.dawnlightclinicalstudy.data_source.request.LifeSignalRequest
import com.example.dawnlightclinicalstudy.data_source.request.OpenCloseSessionRequest
import com.example.dawnlightclinicalstudy.data_source.response.EmptyResponse
import com.example.dawnlightclinicalstudy.data_source.response.TestPlanResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitService {
    @GET("api/1/testPlan")
    suspend fun testPlan(): TestPlanResponse

    @POST("api/1/session/open")
    suspend fun openSession(
        @Body values: OpenCloseSessionRequest,
    ): EmptyResponse

    @POST("api/1/session/close")
    suspend fun closeSession(
        @Body values: OpenCloseSessionRequest,
    ): EmptyResponse

    @POST("api/1/signal/{deviceId}")
    suspend fun uploadSignal(
        @Path("deviceId") deviceId: String,
        @Body values: List<LifeSignalRequest>,
    ): EmptyResponse
}
