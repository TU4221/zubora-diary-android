package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate

sealed class WordSearchEvent : UiEvent {
    internal data class NavigateDiaryShowFragment(val id: String, val date: LocalDate) : WordSearchEvent()
    internal data object ShowKeyboard : WordSearchEvent()
}
