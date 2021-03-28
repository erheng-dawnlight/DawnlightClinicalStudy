package com.example.dawnlightclinicalstudy.data

import com.example.dawnlightclinicalstudy.data_source.RetrofitService
import com.example.dawnlightclinicalstudy.data_source.request.OpenSessionRequest
import com.example.dawnlightclinicalstudy.data_source.response.EmptyResponse
import com.example.dawnlightclinicalstudy.data_source.response.TestPlanResponse
import com.example.dawnlightclinicalstudy.domain.Posture
import kotlinx.coroutines.Dispatchers

class UserSessionRepository(
    private val retrofitService: RetrofitService,
) {

    var posture: Posture = Posture.UNKNOWN
    var subjectId = ""
    val deviceIds = mutableListOf<String>()

    private var currentSessionRequest: OpenSessionRequest? = null

    suspend fun fetchTestPlan(): DataState<TestPlanResponse> {
        return safeApiCall(Dispatchers.IO) {
            retrofitService.testPlan()
        }
    }

    suspend fun openSession(
        openSessionRequest: OpenSessionRequest,
    ): DataState<EmptyResponse> {
        currentSessionRequest = openSessionRequest
        return safeApiCall(Dispatchers.IO) {
            retrofitService.openSession(openSessionRequest)
        }
    }

    suspend fun closeSession(): DataState<EmptyResponse> {
        return safeApiCall(Dispatchers.IO) {
            retrofitService.openSession(currentSessionRequest!!)
        }
    }
}
