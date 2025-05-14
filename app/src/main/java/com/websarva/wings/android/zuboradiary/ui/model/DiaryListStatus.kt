package com.websarva.wings.android.zuboradiary.ui.model

internal sealed class DiaryListStatus {
    data object Idle : DiaryListStatus() // 日記読込前
    data object NewLoading : DiaryListStatus() // 読込中
    data object AdditionLoading : DiaryListStatus() // 読込中
    data object Updating : DiaryListStatus() // 日記リスト更新中
    data object Results : DiaryListStatus() // 日記リスト表示
    data object NoResults : DiaryListStatus() // 日記なし
}
