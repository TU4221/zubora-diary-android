package com.websarva.wings.android.zuboradiary.domain.model.settings

internal data class PasscodeLockSetting(
    val isChecked: Boolean,
    val passCode: String
) : UserSetting {

    init {
        require(checkLegalArgument(isChecked, passCode))
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
}
