package com.example.dawnlightclinicalstudy.domain

class SingleEvent<out T>(private val data: T) {
    private var consumed: Boolean = false

    /**
     * Consume the data and prevents its use again.
     */
    fun maybeConsume(consume: (T) -> Unit) {
        if (!consumed) {
            consumed = true
            consume(data)
        }
    }

    /**
     * Returns the data, even if it's already been handled.
     */
    fun peek(): T = data

    fun hasConsumed() = consumed
}
