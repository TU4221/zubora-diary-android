package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class DiaryEditState : ViewModelState {
    data object Idle: DiaryEditState() // 初期状態
    data object Editing : DiaryEditState() // 編集中

    data object CheckingDiaryInfo : DiaryEditState() // 日記情報確認中
    data object Loading : DiaryEditState() // 読込中
    data object Saving : DiaryEditState() // 保存中
    data object Deleting : DiaryEditState() // 削除中

    data object ItemAdding : DiaryEditState() // 日記項目追加中
    data object ItemDeleting : DiaryEditState() // 日記項目削除中

    data object PictureSelecting : DiaryEditState() // 日記写真選択中

    data object CheckingWeatherAvailability : DiaryEditState() // 天気情報取得可否確認中
    data object FetchingWeatherInfo : DiaryEditState() // 天気情報取得中
}
