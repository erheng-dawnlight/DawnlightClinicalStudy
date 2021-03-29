package com.example.dawnlightclinicalstudy.data

import com.example.dawnlightclinicalstudy.data_source.RetrofitService
import com.example.dawnlightclinicalstudy.data_source.request.OpenCloseSessionRequest
import com.example.dawnlightclinicalstudy.data_source.response.EmptyResponse
import com.example.dawnlightclinicalstudy.data_source.response.TestPlanResponse
import com.example.dawnlightclinicalstudy.domain.Session
import kotlinx.coroutines.Dispatchers

class UserSessionRepository(
    private val retrofitService: RetrofitService,
) {

    var subjectId = ""
    val deviceIds = mutableListOf<String>()
    var eachSessionSecond = 60
    val sessions = mutableListOf<Session>()

    suspend fun fetchTestPlan(): DataState<TestPlanResponse> {
        return safeApiCall(Dispatchers.IO) {
            retrofitService.testPlan()
        }
    }

    suspend fun openSession(
        openCloseSessionRequest: OpenCloseSessionRequest,
    ): DataState<EmptyResponse> {
        return safeApiCall(Dispatchers.IO) {
            retrofitService.openSession(openCloseSessionRequest)
        }
    }

    suspend fun closeSession(
        openCloseSessionRequest: OpenCloseSessionRequest,
    ): DataState<EmptyResponse> {
        return safeApiCall(Dispatchers.IO) {
            retrofitService.closeSession(openCloseSessionRequest)
        }
    }
}
