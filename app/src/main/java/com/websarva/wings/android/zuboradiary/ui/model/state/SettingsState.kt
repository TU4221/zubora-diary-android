package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class SettingsState : UiState {
    data object Idle: SettingsState() // 初期状態
    data object LoadingAllSettings: SettingsState() // 全設定読込中
    data object DeletingAllData: SettingsState() // 全アプリデータ削除中
    data object DeletingAllDiaries: SettingsState() // 全日記削除中

    // MEMO:DataStoreへの個別の設定値書き込みは通常高速であり、
    //      毎回プログレスバーを表示するのは一般的ではないため、不要とする。
    // data object UpdateSetting: SettingsState() // 設定値更新
    // data object InitializingAllSettings: SettingsState() // 全設定値初期化中
}
