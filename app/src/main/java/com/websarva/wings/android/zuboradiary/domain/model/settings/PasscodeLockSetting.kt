package com.websarva.wings.android.zuboradiary.domain.model.settings

import kotlinx.serialization.Serializable

/**
 * パスコードロック設定の状態を表す基底クララス。
 *
 * このクラスは、パスコードロックが有効か無効か、そして有効な場合はそのパスコードを保持する。
 * パスコードは4桁の文字列数字である必要がある。
 *
 * @property isEnabled パスコードロックが有効な場合は `true`、無効な場合は `false`。
 */
@Serializable
internal sealed class PasscodeLockSetting(
    val isEnabled: Boolean,
) : UserSetting {

    /**
     * パスコードロックが有効な状態を表すデータクラス。
     *
     * @property passcode 設定されている4桁の文字列数字のパスコード。
     * @throws IllegalArgumentException [passcode] が4桁の数字でない場合。
     */
    @Serializable
    data class Enabled(val passcode: String) : PasscodeLockSetting(true) {
        init {
            require(checkLegalArgument(passcode))
        }

        /**
         * 指定されたパスコードが4桁の数字であるか検証する。
         *
         * @param passCode 検証するパスコード文字列。
         * @return パスコードが4桁の数字であれば `true`、そうでなければ `false`。
         */
        private fun checkLegalArgument( passCode: String): Boolean {
            return passCode.matches("\\d{4}".toRegex())
        }
    }

    /**
     * パスコードロックが無効な状態を表すデータオブジェクト。
     */
    @Serializable
    data object Disabled : PasscodeLockSetting(false)

    companion object {
        /**
         * デフォルトのパスコードロック設定（無効）を返す。
         *
         * @return デフォルトのパスコードロック設定。
         */
        fun default(): PasscodeLockSetting {
            return Disabled
        }
    }
}
