package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class DiaryEditState : UiState, Parcelable {
    data object Idle: DiaryEditState() // 初期状態
    data object Editing : DiaryEditState() // 編集中

    data object CheckingDiaryInfo : DiaryEditState() // 日記情報確認中
    data object Loading : DiaryEditState() // 読込中
    data object LoadError : DiaryEditState() // 読込中
    data object Saving : DiaryEditState() // 保存中
    data object Deleting : DiaryEditState() // 削除中

    data object AddingItem : DiaryEditState() // 日記項目追加中
    data object DeletingItem : DiaryEditState() // 日記項目削除中

    data object SelectingPicture : DiaryEditState() // 日記写真選択中

    data object CheckingWeatherAvailability : DiaryEditState() // 天気情報取得可否確認中
    data object FetchingWeatherInfo : DiaryEditState() // 天気情報取得中
}
