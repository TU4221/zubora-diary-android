package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate

sealed class WordSearchUiEvent : UiEvent {
    data class NavigateDiaryShowFragment(val id: String, val date: LocalDate) : WordSearchUiEvent()
    data object ShowKeyboard : WordSearchUiEvent()
}
