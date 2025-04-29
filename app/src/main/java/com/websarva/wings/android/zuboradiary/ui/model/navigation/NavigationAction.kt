package com.websarva.wings.android.zuboradiary.ui.model.navigation

internal sealed class NavigationAction {
    data object None : NavigationAction()
    data object NavigatePreviousFragment : NavigationAction()
}
