package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.WordSearchFragment

/**
 * 単語検索画面([WordSearchFragment])における、UIイベント。
 */
sealed interface WordSearchUiEvent : UiEvent {

    /** ソフトウェアキーボードを表示することを示すイベント。 */
    data object ShowKeyboard : WordSearchUiEvent
}
