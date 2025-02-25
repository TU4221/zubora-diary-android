package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

class PassCodeLockPreference {

    companion object {
        private const val IS_CHECKED_DEFAULT_VALUE = false
        private const val PASS_CODE_DEFAULT_VALUE = ""
    }

    private val isCheckedPreferenceKey = booleanPreferencesKey("is_checked_passcode_lock")
    private val passcodePreferenceKey = stringPreferencesKey("passcode")

    val isChecked: Boolean
    val passCode: String

    constructor(preferences: Preferences) {
        var isChecked = preferences[isCheckedPreferenceKey]
        var passCode = preferences[passcodePreferenceKey]
        if (isChecked == null || passCode == null) {
            isChecked = IS_CHECKED_DEFAULT_VALUE
            passCode = PASS_CODE_DEFAULT_VALUE
        }
        require(checkLegalArgument(isChecked, passCode))

        this.isChecked = isChecked
        this.passCode = passCode
    }

    @JvmOverloads
    constructor(
        isChecked: Boolean = IS_CHECKED_DEFAULT_VALUE,
        passCode: String = PASS_CODE_DEFAULT_VALUE
    ) {
        require(checkLegalArgument(isChecked, passCode))

        this.isChecked = isChecked
        this.passCode = passCode
    }

    private fun checkLegalArgument(
        isChecked: Boolean,
        passCode: String
    ): Boolean {
        return if (isChecked) {
            passCode.matches("\\d{4}".toRegex())
        } else {
            passCode.matches("".toRegex())
        }
    }

    fun setUpPreferences(mutablePreferences: MutablePreferences) {
        mutablePreferences[isCheckedPreferenceKey] = isChecked
        mutablePreferences[passcodePreferenceKey] = passCode
    }
}
