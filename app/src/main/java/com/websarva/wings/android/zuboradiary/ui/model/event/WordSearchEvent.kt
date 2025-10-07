package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.DiaryIdUi
import java.time.LocalDate

sealed class WordSearchEvent : UiEvent {
    internal data class NavigateDiaryShowFragment(val id: DiaryIdUi, val date: LocalDate) : WordSearchEvent()
    internal data object ShowKeyboard : WordSearchEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : WordSearchEvent()
}
