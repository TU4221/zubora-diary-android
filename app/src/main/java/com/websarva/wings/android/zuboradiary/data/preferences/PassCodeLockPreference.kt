package com.websarva.wings.android.zuboradiary.data.preferences

import android.util.Log
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

class PassCodeLockPreference @JvmOverloads constructor(
    isChecked: Boolean = false,
    passCode: String = ""
) {

    companion object {
        @JvmField
        val PREFERENCES_KEY_IS_CHECKED: Preferences.Key<Boolean> =
            booleanPreferencesKey("is_checked_passcode_lock")
        @JvmField
        val PREFERENCES_KEY_PASSCODE: Preferences.Key<String> =
            stringPreferencesKey("passcode")
    }

    val isChecked: Boolean
    private var passCode: String

    init {
        if (isChecked) {
            require(passCode.matches("\\d{4}".toRegex()))
        } else {
            require(passCode.matches("".toRegex()))
        }

        this.isChecked = isChecked
        this.passCode = passCode
    }

    fun setUpPreferences(mutablePreferences: MutablePreferences) {
        mutablePreferences[PREFERENCES_KEY_IS_CHECKED] = isChecked
        mutablePreferences[PREFERENCES_KEY_PASSCODE] = passCode
    }
}
