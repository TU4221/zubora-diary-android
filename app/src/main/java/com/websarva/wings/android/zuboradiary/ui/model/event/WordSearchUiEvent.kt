package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.WordSearchFragment
import java.time.LocalDate

/**
 * 単語検索画面([WordSearchFragment])における、UIイベントを表すsealed class。
 */
sealed class WordSearchUiEvent : UiEvent {

    /**
     * 日記表示画面へ遷移することを示すイベント。
     * @property id 表示対象の日記ID。
     * @property date 対象の日記の日付。
     */
    data class NavigateDiaryShowScreen(val id: String, val date: LocalDate) : WordSearchUiEvent()

    /** ソフトウェアキーボードを表示することを示すイベント。 */
    data object ShowKeyboard : WordSearchUiEvent()
}
