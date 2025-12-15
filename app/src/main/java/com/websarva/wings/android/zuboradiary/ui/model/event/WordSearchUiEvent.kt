package com.websarva.wings.android.zuboradiary.ui.model.event

/**
 * 単語検索画面における、UIイベント。
 */
sealed interface WordSearchUiEvent : UiEvent {

    /** ソフトウェアキーボードを表示することを示すイベント。 */
    data object ShowKeyboard : WordSearchUiEvent
}
