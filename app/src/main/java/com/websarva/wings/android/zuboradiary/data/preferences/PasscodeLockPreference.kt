package com.websarva.wings.android.zuboradiary.data.preferences

/**
 * パスコードロック機能に関するユーザー設定を表すデータクラス。
 *
 * この設定は、アプリ起動時のパスコードロックが有効かどうか、
 * および設定されているパスコード自体を保持する。
 *
 * @property isEnabled パスコードロック機能が有効な場合はtrue、無効な場合はfalse。
 * @property passcode 設定されているパスコード文字列。パスコードが未設定または無効の場合は空文字列を代入。
 */
internal class PasscodeLockPreference(
    val isEnabled: Boolean,
    val passcode: String
) : UserPreference
