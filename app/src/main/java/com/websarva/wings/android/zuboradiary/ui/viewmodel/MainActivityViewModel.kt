package com.websarva.wings.android.zuboradiary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.ui.model.event.MainActivityEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.MainActivityUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MainActivityViewModel : ViewModel() {

    private val _uiEvent = MutableSharedFlow<ConsumableEvent<MainActivityEvent>>(replay = 1)
    val uiEvent get() = _uiEvent.asSharedFlow()

    private val _uiState =
        MutableStateFlow<MainActivityUiState>(
            MainActivityUiState.ShowingBottomNavigation(false)
        )
    val uiState get() = _uiState.asStateFlow()

    private val _isProgressIndicatorVisible = MutableStateFlow(false)
    val isProgressIndicatorVisible get() = _isProgressIndicatorVisible.asStateFlow()

    private val _wasSelectedTab = MutableStateFlow(false)
    val wasSelectedTab get() = _wasSelectedTab.asStateFlow()

    fun onRequestBottomNavigationStateChange(isVisible: Boolean) {
        updateBottomNavigationUiState(isVisible)
    }

    fun onRequestBottomNavigationEnabledChange(isEnabled: Boolean) {
        updateBottomNavigationEnabledUiState(isEnabled)
    }

    fun onFragmentProgressVisibilityChanged(isVisible: Boolean) {
        updateIsProgressIndicatorVisible(isVisible)
    }

    fun onBottomNavigationItemSelect() {
        updateWasSelectedTab(true)
    }

    fun onFragmentTransitionSetupCompleted() {
        updateWasSelectedTab(false)
    }

    fun onNavigateBackFromBottomNavigationTab() {
        viewModelScope.launch {
            updateUiEvent(MainActivityEvent.NavigateStartTabFragment)
        }
    }

    private suspend fun updateUiEvent(event: MainActivityEvent) {
        _uiEvent.emit(
            ConsumableEvent(event)
        )
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

    private fun updateWasSelectedTab(wasSelected: Boolean) {
        _wasSelectedTab.value = wasSelected
    }
}
