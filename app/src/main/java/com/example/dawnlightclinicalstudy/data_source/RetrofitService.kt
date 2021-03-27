package com.example.dawnlightclinicalstudy.data_source

import com.example.dawnlightclinicalstudy.data_source.response.TestPlanResponse
import retrofit2.http.GET

interface RetrofitService {
    @GET("api/1/testPlan")
    suspend fun testPlan(): TestPlanResponse
}
