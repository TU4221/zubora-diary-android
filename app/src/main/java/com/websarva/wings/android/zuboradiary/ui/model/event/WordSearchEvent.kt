package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate

internal sealed class WordSearchEvent : ViewModelEvent() {
    data class NavigateDiaryShowFragment(val date: LocalDate) : WordSearchEvent()
    data object ShowKeyboard : WordSearchEvent()
}
