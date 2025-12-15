package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen.DiaryItemTitleEditDialog

/**
 * 日記項目タイトル編集ダイアログ([DiaryItemTitleEditDialog])における、UIイベント。
 */
sealed interface DiaryItemTitleEditUiEvent : UiEvent {

    /** スワイプされた選択履歴の表示を元に戻すことを示すイベント。 */
    data object CloseSwipedTitleSelectionHistory : DiaryItemTitleEditUiEvent
}
