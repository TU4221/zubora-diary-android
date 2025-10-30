package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MainActivityUiState(
    // UiData
    val themeColor: ThemeColorUi? = null,

    // UiState
    val isBottomNavigationVisible: Boolean = false,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false
) : UiState, Parcelable
