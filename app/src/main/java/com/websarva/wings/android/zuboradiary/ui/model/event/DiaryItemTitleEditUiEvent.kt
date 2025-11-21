package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen.DiaryItemTitleEditDialog
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi

/**
 * 日記項目タイトル編集ダイアログ([DiaryItemTitleEditDialog])における、UIイベントを表すsealed class。
 */
sealed class DiaryItemTitleEditUiEvent : UiEvent {

    /**
     * 編集を完了し、結果を呼び出し元に返すことを示すイベント。
     * @property diaryItemTitleSelection 編集/選択されたタイトル情報。
     */
    data class CompleteEdit(
        val diaryItemTitleSelection: DiaryItemTitleSelectionUi
    ) : DiaryItemTitleEditUiEvent()

    /**
     * 履歴アイテム削除確認ダイアログへ遷移することを示すイベント。
     * @property itemTitle 削除対象の項目タイトル。
     */
    data class NavigateSelectionHistoryItemDeleteDialog(
        val itemTitle: String
    ) : DiaryItemTitleEditUiEvent()

    /** スワイプされた履歴アイテムの表示を元に戻すことを示すイベント。 */
    data object CloseSwipedItem : DiaryItemTitleEditUiEvent()
}
