package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class DiaryListState : UiState, Parcelable {
    data object Idle: DiaryListState() // 初期状態

    data object LoadingDiaryInfo : DiaryListState() // 日記情報読込中
    data object LoadingNewDiaryList : DiaryListState() // 日記リスト新規読込中
    data object LoadingAdditionDiaryList : DiaryListState() // 日記リスト追加読込中
    data object UpdatingDiaryList : DiaryListState() // 日記リスト更新中

    data object DeletingDiary : DiaryListState() // 日記削除中

    data object ShowingDiaryList : DiaryListState() // 日記リスト表示
    data object NoDiaries : DiaryListState() // 日記なし
}
