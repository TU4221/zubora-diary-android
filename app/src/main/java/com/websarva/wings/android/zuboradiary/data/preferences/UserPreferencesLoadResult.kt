package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.Preferences

/**
 * ユーザー設定の読み込み結果を表すシールドクラス。
 *
 * 読み込みが成功した場合は[Success]、失敗した場合は[Failure]となる。
 */
internal sealed class UserPreferencesLoadResult {

    /**
     * ユーザー設定の読み込みが成功した場合のデータクラス。
     *
     * @property preferences 読み込まれた設定データ。
     */
    data class Success(val preferences: Preferences) : UserPreferencesLoadResult()

    /**
     * ユーザー設定の読み込みが失敗した場合のデータクラス。
     *
     * @property exception 発生した例外。
     */
    data class Failure(val exception: UserPreferencesException) : UserPreferencesLoadResult()
}
