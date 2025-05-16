package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class DiaryEditState {
    data object Idle : DiaryEditState()
    data object Loading : DiaryEditState() // 読込中
    data object Saving : DiaryEditState() // 保存中
    data object Deleting : DiaryEditState() // 削除中

    data object ItemAdding : DiaryEditState() // 日記項目追加中
    data object ItemDeleting : DiaryEditState() // 日記項目削除中

    data object PictureSelecting : DiaryEditState() // 日記写真選択中
    data object PictureDeleting : DiaryEditState() // 日記写真削除中

    data object WeatherFetching : DiaryEditState() // 日記リスト更新中
}
