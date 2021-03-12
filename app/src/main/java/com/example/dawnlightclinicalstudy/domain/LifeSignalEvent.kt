package com.example.dawnlightclinicalstudy.domain

enum class LifeSignalEvent(val string: String) {
    ON_DISCOVERY("onDiscovery"),
    ON_DATA("onData"),
    ON_ORDERED_DATA("onOrderedData"),
    ON_FILTERED_DATA("onFilteredData"),
    ON_STATUS("onStatus"),
    UNKNOWN(""),
    ;

    companion object {
        fun fromString(string: String) =
            values().firstOrNull { string == it.string } ?: UNKNOWN
    }
}