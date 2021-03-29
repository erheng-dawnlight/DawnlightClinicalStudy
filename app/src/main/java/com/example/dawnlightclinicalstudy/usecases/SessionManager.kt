package com.example.dawnlightclinicalstudy.usecases

import com.example.dawnlightclinicalstudy.data.UserSessionRepository
import com.example.dawnlightclinicalstudy.domain.Session

class SessionManager(
    private val userSessionRepository: UserSessionRepository,
) {

    var currentSessionIndex = 0
    val totalSessions: Int
        get() = userSessionRepository.sessions.size

    fun nextSession(): Session {
        return userSessionRepository.sessions[++currentSessionIndex]
    }

    fun currentSession(): Session {
        return userSessionRepository.sessions[currentSessionIndex]
    }

    fun hasNextSession(): Boolean {
        return currentSessionIndex + 1 < userSessionRepository.sessions.size
    }
}