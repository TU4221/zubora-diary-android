package com.websarva.wings.android.zuboradiary.data.preferences

internal sealed class UserPreferencesLoadingResult {
    data class Success(val preferences: AllPreferences) : UserPreferencesLoadingResult()
    data class Failure(
        val exception: UserPreferencesAccessException,
        val fallbackPreferences: AllPreferences
    ) : UserPreferencesLoadingResult()
}
