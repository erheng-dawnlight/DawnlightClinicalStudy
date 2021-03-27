package com.example.dawnlightclinicalstudy.data

import com.example.dawnlightclinicalstudy.data_source.RetrofitService
import com.example.dawnlightclinicalstudy.data_source.response.TestPlanResponse
import kotlinx.coroutines.Dispatchers

class UserSessionRepository(
    private val retrofitService: RetrofitService,
) {

    suspend fun fetchTestPlan(): DataState<TestPlanResponse> {
        return safeApiCall(Dispatchers.IO) {
            retrofitService.testPlan()
        }
    }
}
