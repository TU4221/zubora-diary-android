package com.websarva.wings.android.zuboradiary.data.preferences

internal class PassCodeLockPreference : UserPreference {

    companion object {
        const val IS_CHECKED_DEFAULT_VALUE = false
        const val PASS_CODE_DEFAULT_VALUE = ""
    }

    val isChecked: Boolean
    val passCode: String

    constructor(isChecked: Boolean, passCode: String) {
        require(checkLegalArgument(isChecked, passCode))

        this.isChecked = isChecked
        this.passCode = passCode
    }

    constructor(): this(IS_CHECKED_DEFAULT_VALUE, PASS_CODE_DEFAULT_VALUE)

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
}
