package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.Preferences

internal sealed class UserPreferencesLoadResult {
    data class Success(val preferences: Preferences) : UserPreferencesLoadResult()
    data class Failure(val exception: UserPreferencesException) : UserPreferencesLoadResult()
}
