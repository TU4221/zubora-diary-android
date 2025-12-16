package com.websarva.wings.android.zuboradiary.ui.diary.search

import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent

/**
 * 単語検索画面における、UIイベント。
 */
sealed interface WordSearchUiEvent : UiEvent {

    /** ソフトウェアキーボードを表示することを示すイベント。 */
    data object ShowKeyboard : WordSearchUiEvent
}
