package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.Preferences

internal sealed class UserPreferencesFetchResult {
    data class Success(val preferences: Preferences) : UserPreferencesFetchResult()
    data class Failure(
        val exception: UserPreferencesAccessException,
        val fallbackPreferences: Preferences
    ) : UserPreferencesFetchResult()
}
