package com.websarva.wings.android.zuboradiary.domain.model.settings

import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsLoadException

/**
 * ユーザー設定の取得処理における最終的な結果を表す基底クラス。
 *
 * データソース（例: データベースやファイル）から取得したユーザー設定情報 ([UserSettingDataSourceResult]) に、
 * アプリケーションのルール（ビジネスルール）を適用した後の状態を示す。
 *
 * このクラスは以下のいずれかの状態を表す。
 * - 設定値が正常に利用できる状態（成功）
 * - 何らかのエラーが発生したが、代わりに使える代替設定値がある状態
 *
 * @param T [UserSetting] を実装するユーザー設定の型。
 */
internal sealed class UserSettingResult<out T : UserSetting> {

    /**
     * ユーザー設定が正常に利用できる状態を表す。
     *
     * この状態になるのは、以下のいずれかの場合である。
     * - データソースから設定値が問題なく取得できた。
     * - データソースに設定値が見つからなかった（例: アプリの初回起動時など）が、
     *   アプリケーションのルールに基づいて、あらかじめ決められた初期設定値（デフォルト値）が適用された。
     *
     * @param T [UserSetting] を実装するユーザー設定の型。
     * @property setting 利用できるユーザー設定の値。
     */
    data class Success<out T : UserSetting>(val setting: T) : UserSettingResult<T>()

    /**
     * ユーザー設定の取得中にエラーが発生したものの、代替の設定値（フォールバック値）が利用できる状態を表す。
     *
     * 例えば、設定ファイルの読み込みに失敗した場合でも、
     * アプリケーションが動作し続けられるように、あらかじめ定義された安全な設定値が提供される。
     *
     * @param T [UserSetting] を実装するユーザー設定の型。
     * @property exception 設定値の取得に失敗した際に発生した例外。
     * @property fallbackSetting エラー発生時に代わりに利用するユーザー設定の値。
     */
    data class Failure<out T : UserSetting>(
        val exception: UserSettingsLoadException,
        val fallbackSetting: T
    ) : UserSettingResult<T>()
}
