package com.websarva.wings.android.zuboradiary.data.preferences

internal class PasscodeLockPreference(
    val isEnabled: Boolean = false,
    val passcode: String = ""
) : UserPreference
