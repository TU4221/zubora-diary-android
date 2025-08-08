package com.websarva.wings.android.zuboradiary.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.websarva.wings.android.zuboradiary.ui.model.state.MainActivityUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class MainActivityViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow<MainActivityUiState>(
            MainActivityUiState.ShowingBottomNavigation(false)
        )
    val uiState get() = _uiState.asStateFlow()

    private val _isProgressIndicatorVisible = MutableStateFlow(false)
    val isProgressIndicatorVisible get() = _isProgressIndicatorVisible.asStateFlow()

    fun onRequestBottomNavigationStateChange(isVisible: Boolean) {
        updateBottomNavigationUiState(isVisible)
    }

    fun onRequestBottomNavigationEnabledChange(isEnabled: Boolean) {
        updateBottomNavigationEnabledUiState(isEnabled)
    }

    fun onFragmentProgressVisibilityChanged(isVisible: Boolean) {
        updateIsProgressIndicatorVisible(isVisible)
    }

    private fun updateBottomNavigationUiState(isVisible: Boolean) {
        _uiState.update {
            if (isVisible) {
                MainActivityUiState.ShowingBottomNavigation(it.isBottomNavigationEnabled)
            } else {
                MainActivityUiState.HidingBottomNavigation(it.isBottomNavigationEnabled)
            }
        }
    }

    private fun updateBottomNavigationEnabledUiState(isEnabled: Boolean) {
        _uiState.update {
            when (it) {
                is MainActivityUiState.ShowingBottomNavigation -> {
                    it.copy(isEnabled)
                }
                is MainActivityUiState.HidingBottomNavigation -> {
                    it.copy(isEnabled)
                }
            }
        }
    }

    private fun updateIsProgressIndicatorVisible(isVisible: Boolean) {
        _isProgressIndicatorVisible.value = isVisible
    }
}
