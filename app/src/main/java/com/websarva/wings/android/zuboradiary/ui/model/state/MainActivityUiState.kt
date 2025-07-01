package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class MainActivityUiState(
    open val isBottomNavigationEnabled: Boolean
) {

    data class ShowingBottomNavigation(
        override val isBottomNavigationEnabled: Boolean
    ) : MainActivityUiState(isBottomNavigationEnabled)

    data class HidingBottomNavigation(
        override val isBottomNavigationEnabled: Boolean
    ) : MainActivityUiState(isBottomNavigationEnabled)
}
