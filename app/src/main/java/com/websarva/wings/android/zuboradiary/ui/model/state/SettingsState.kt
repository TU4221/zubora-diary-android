package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class SettingsState : ViewModelState {
    data object Idle: SettingsState() // 初期状態
}
