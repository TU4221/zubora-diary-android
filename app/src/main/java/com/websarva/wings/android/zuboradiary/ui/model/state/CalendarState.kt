package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class CalendarState : UiState {
    data object Idle: CalendarState() // 初期状態
    data object ShowingDiary : CalendarState() // 日記表示
    data object HidingDiary : CalendarState() // 日記非表示
}
