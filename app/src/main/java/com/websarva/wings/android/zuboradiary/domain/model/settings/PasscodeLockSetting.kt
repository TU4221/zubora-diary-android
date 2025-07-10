package com.websarva.wings.android.zuboradiary.domain.model.settings

// MEMO:パスコードは有効の時のみ必要になる為、下記シールドクラスで対応。
internal sealed class PasscodeLockSetting(
    val isChecked: Boolean,
) : UserSetting {

    data class Enabled(val passcode: String) : PasscodeLockSetting(true) {
        init {
            require(checkLegalArgument(passcode))
        }

        private fun checkLegalArgument( passCode: String): Boolean {
            return passCode.matches("\\d{4}".toRegex())
        }
    }

    data object Disabled : PasscodeLockSetting(false)
}
