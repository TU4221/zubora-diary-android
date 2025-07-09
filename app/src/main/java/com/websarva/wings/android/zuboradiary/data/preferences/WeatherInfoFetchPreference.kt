package com.websarva.wings.android.zuboradiary.data.preferences

internal class WeatherInfoFetchPreference : UserPreference {

    companion object {
        const val IS_CHECKED_DEFAULT_VALUE = false
    }

    val isChecked: Boolean

    constructor(isChecked: Boolean) {
        this.isChecked = isChecked
    }

    constructor(): this(IS_CHECKED_DEFAULT_VALUE)
}
