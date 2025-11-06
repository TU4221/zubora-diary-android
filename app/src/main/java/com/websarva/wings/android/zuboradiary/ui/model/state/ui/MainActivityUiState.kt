package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class MainActivityUiState(
    // UiData
    val themeColor: ThemeColorUi? = null,

    // UiState
    val isBottomNavigationVisible: Boolean = false,
    // MEMO:タブ選択で下記の様な画面遷移を行う時、Bを表示処理中にタブ選択でAを表示させようとすると、
    //      BのFragmentが消えた後、AのFragmentが表示されない不具合が生じる。
    //      (何も表示されない状態)
    //      これを回避するために、遷移先のFragmentが表示しきるまで、タブ選択できないようにする。
    //      Fragment A → B → A
    val isBottomNavigationEnabled: Boolean = false,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false,
    val isNavigating: Boolean = false
) : UiState, Parcelable {

    companion object {
        fun fromSavedState(savedUiState: MainActivityUiState): MainActivityUiState {
            return MainActivityUiState(
                themeColor = savedUiState.themeColor,
                isBottomNavigationVisible = savedUiState.isBottomNavigationVisible
            )
        }
    }
}
