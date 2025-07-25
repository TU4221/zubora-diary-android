package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate

sealed class WordSearchEvent : ViewModelEvent {
    internal data class NavigateDiaryShowFragment(val date: LocalDate) : WordSearchEvent()
    internal data object ShowKeyboard : WordSearchEvent()

    internal data class CommonEvent(val event: CommonViewModelEvent) : WordSearchEvent()
}
