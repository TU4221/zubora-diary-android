package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class DiaryListState : ViewModelState {
    data object Idle: DiaryListState() // 初期状態
    data object NewLoading : DiaryListState() // 読込中
    data object AdditionLoading : DiaryListState() // 読込中
    data object Updating : DiaryListState() // 日記リスト更新中
    data object Results : DiaryListState() // 日記リスト表示
    data object NoResults : DiaryListState() // 日記なし
}
