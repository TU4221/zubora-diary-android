package com.websarva.wings.android.zuboradiary.data.preferences

/**
 * ユーザー設定オブジェクトのFlowストリームの結果を表すsealed class。
 *
 * Flowからの放出が成功し、かつ設定データが期待通りに存在する場合は[Success]となる。
 * Flowからの放出自体に失敗した場合、または設定データが存在しないなどの理由で
 * ユーザー設定オブジェクトへの変換に失敗した場合は[Failure]となる。
 *
 * @param T [UserPreference]を継承する、Flowで監視したい具体的なユーザー設定の型。
 */
internal sealed class UserPreferenceFlowResult<out T : UserPreference> {

    /**
     * ユーザー設定のFlowからの放出が成功し、有効なユーザー設定オブジェクトが取得できた場合のデータクラス。
     *
     * @param T [UserPreference]を継承するユーザー設定の型。
     * @property preference 取得したユーザー設定。
     */
    data class Success<out T : UserPreference>(val preference: T) : UserPreferenceFlowResult<T>()

    /**
     * ユーザー設定のFlowからの放出が失敗した場合のデータクラス。
     *
     * DataStoreからの読み込みエラーや、期待する設定データが存在しない場合などが含まれる。
     *
     * @property exception 発生した例外。
     */
    data class Failure(
        val exception: UserPreferencesException
    ) : UserPreferenceFlowResult<Nothing>()
}
