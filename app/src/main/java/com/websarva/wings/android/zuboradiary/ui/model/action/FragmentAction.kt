package com.websarva.wings.android.zuboradiary.ui.model.action

internal sealed class FragmentAction {
    data object None : FragmentAction()
    data object NavigatePreviousFragment : FragmentAction()
}
