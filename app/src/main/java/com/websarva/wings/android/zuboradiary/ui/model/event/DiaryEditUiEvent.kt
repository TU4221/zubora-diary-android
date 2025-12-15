package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryEditFragment

/**
 * 日記編集画面([DiaryEditFragment])における、UIイベント。
 */
sealed interface DiaryEditUiEvent : UiEvent {

    /** ギャラリーから画像を選択する処理を開始することを示すイベント。 */
    data object ShowImageSelectionGallery : DiaryEditUiEvent

    /**
     * 日記項目を追加（表示）するアニメーションを開始することを示すイベント。
     * @property itemNumber 表示にする項目の番号。
     */
    data class startDiaryItemAdditionAnimation(val itemNumber: Int) : DiaryEditUiEvent

    /**
     * 日記項目を削除（非表示）にするアニメーションを開始することを示すイベント。
     * @property itemNumber 非表示にする項目の番号。
     */
    data class startDiaryItemDeleteAnimation(val itemNumber: Int) : DiaryEditUiEvent

    /** 天気情報を取得する前に、位置情報権限を確認することを示すイベント。 */
    data object CheckAccessLocationPermissionBeforeWeatherInfoFetch : DiaryEditUiEvent
}
