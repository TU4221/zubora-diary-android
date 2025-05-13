package com.websarva.wings.android.zuboradiary.ui.model.action

import java.time.LocalDate

internal sealed class WordSearchFragmentAction : FragmentAction() {
    data class NavigateDiaryShowFragment(val date: LocalDate) : WordSearchFragmentAction()
}
