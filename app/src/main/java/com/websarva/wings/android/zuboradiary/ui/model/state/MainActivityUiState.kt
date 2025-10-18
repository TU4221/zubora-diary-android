package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class MainActivityUiState(
    open val isBottomNavigationEnabled: Boolean
) : UiState, Parcelable {

    data class ShowingBottomNavigation(
        override val isBottomNavigationEnabled: Boolean
    ) : MainActivityUiState(isBottomNavigationEnabled)

    data class HidingBottomNavigation(
        override val isBottomNavigationEnabled: Boolean
    ) : MainActivityUiState(isBottomNavigationEnabled)
}
