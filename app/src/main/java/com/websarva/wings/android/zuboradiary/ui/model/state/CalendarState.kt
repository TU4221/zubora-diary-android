package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class CalendarState : UiState {
    data object Idle: CalendarState() // 初期状態

    data object LoadingDiaryInfo : CalendarState() //日記読込中
    data object LoadingDiary : CalendarState() //日記読込中
    data object LoadDiarySuccess : CalendarState() //日記読込中
    data object LoadError : CalendarState()

    data object NoDiary : CalendarState() // 日記非表示
}
