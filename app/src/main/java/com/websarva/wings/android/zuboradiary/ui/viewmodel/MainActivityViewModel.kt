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

    fun showBottomNavigation() {
        _uiState.update {
            MainActivityUiState.ShowingBottomNavigation(it.isBottomNavigationEnabled)
        }
    }

    fun hideBottomNavigation() {
        _uiState.update {
            MainActivityUiState.HidingBottomNavigation(it.isBottomNavigationEnabled)
        }
    }

    fun switchBottomNavigationEnabled(isEnabled: Boolean) {
        _uiState.value  =
            when (val value = _uiState.value) {
                is MainActivityUiState.ShowingBottomNavigation -> {
                    value.copy(isEnabled)
                }
                is MainActivityUiState.HidingBottomNavigation -> {
                    value.copy(isEnabled)
                }
            }
    }

    fun onRequestShowProgressIndicator() {
        _isProgressIndicatorVisible.value = true
    }

    fun onRequestHideProgressIndicator() {
        _isProgressIndicatorVisible.value = false
    }
}
