package com.websarva.wings.android.zuboradiary.data.preferences

internal sealed class UserPreferenceFlowResult<out T : UserPreference> {
    data class Success<out T : UserPreference>(val preference: T) : UserPreferenceFlowResult<T>()
    data class Failure<out T : UserPreference>(
        val exception: UserPreferencesAccessException,
        val fallbackPreference: T
    ) : UserPreferenceFlowResult<T>()
}
