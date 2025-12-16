package com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle

import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent

/**
 * 日記項目タイトル編集ダイアログにおける、UIイベント。
 */
sealed interface DiaryItemTitleEditUiEvent : UiEvent {

    /** スワイプされた選択履歴の表示を元に戻すことを示すイベント。 */
    data object CloseSwipedTitleSelectionHistory : DiaryItemTitleEditUiEvent
}
