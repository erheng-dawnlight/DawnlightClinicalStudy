package com.example.dawnlightclinicalstudy.domain

import android.content.Context
import java.io.Serializable

sealed class StringWrapper : Serializable {

    class Res(
        @androidx.annotation.StringRes val resId: Int,
        vararg val formatArgs: Serializable?
    ) : StringWrapper(), Serializable {

        fun resolve(context: Context): String {
            return context.getString(resId, *formatArgs)
        }

        override fun equals(other: Any?): Boolean {
            return other is Res
                    && other.resId == resId
                    && other.formatArgs.contentEquals(formatArgs)
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }

        override fun toString(): String {
            return Res::class.java.simpleName +
                    "(" +
                    "resId=$resId, " +
                    "formatArgs=${formatArgs.map { it.toString() }}" +
                    ")"
        }
    }

    class Plurals(
        @androidx.annotation.PluralsRes val resId: Int,
        val quantity: Int,
        private vararg val formatArgs: Serializable?
    ) : StringWrapper(), Serializable {

        fun resolve(context: Context): String {
            return context.resources.getQuantityString(resId, quantity, *formatArgs)
        }

        override fun equals(other: Any?): Boolean {
            return other is Res
                    && other.resId == resId
                    && other.formatArgs.contentEquals(formatArgs)
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + quantity.hashCode()
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }

        override fun toString(): String {
            return Res::class.java.simpleName +
                    "(" +
                    "resId=$resId, " +
                    "quantity=$quantity, " +
                    "formatArgs=${formatArgs.map { it.toString() }}" +
                    ")"
        }
    }

    data class Text(val text: String) : StringWrapper(), Serializable

    fun getText(context: Context) = when (this@StringWrapper) {
        is Res -> resolve(context)
        is Plurals -> resolve(context)
        is Text -> text
    }
}