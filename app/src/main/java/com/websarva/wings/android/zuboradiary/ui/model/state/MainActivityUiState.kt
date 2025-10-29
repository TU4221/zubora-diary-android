package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class MainActivityUiState(
    open val isBottomNavigationEnabled: Boolean
) : UiState, Parcelable {

    @IgnoredOnParcel
    override val isProcessing: Boolean = false
    @IgnoredOnParcel
    override val isInputDisabled: Boolean = false

    data class ShowingBottomNavigation(
        override val isBottomNavigationEnabled: Boolean
    ) : MainActivityUiState(isBottomNavigationEnabled)

    data class HidingBottomNavigation(
        override val isBottomNavigationEnabled: Boolean
    ) : MainActivityUiState(isBottomNavigationEnabled)
}
