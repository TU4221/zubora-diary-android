package com.websarva.wings.android.zuboradiary.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.event.MainActivityUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.FragmentUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.MainActivityUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MainActivityViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val loadThemeColorSettingUseCase: LoadThemeColorSettingUseCase,
) : ViewModel() {

    companion object {
        private const val SAVED_UI_STATE_KEY = "uiState"
    }

    private val _activityUiEvent = MutableSharedFlow<ConsumableEvent<MainActivityUiEvent>>(replay = 1)
    val activityUiEvent get() = _activityUiEvent.asSharedFlow()

    private val _fragmentUiEvent = MutableSharedFlow<ConsumableEvent<FragmentUiEvent>>(replay = 1)
    val fragmentUiEvent get() = _fragmentUiEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(
        handle.get<MainActivityUiState>(SAVED_UI_STATE_KEY)?.let { savedUiState ->
            MainActivityUiState().copy(
                themeColor = savedUiState.themeColor,
                isBottomNavigationVisible = savedUiState.isBottomNavigationVisible
            )
        } ?: MainActivityUiState()
    )
    val uiState get() = _uiState.asStateFlow()

    init {
        observeDerivedUiStateChanges(handle)
    }

    private fun observeDerivedUiStateChanges(handle: SavedStateHandle) {
        uiState.onEach {
            handle[SAVED_UI_STATE_KEY] = it
        }.launchIn(viewModelScope)

        loadThemeColorSettingUseCase()
            .map {
                when (it) {
                    is UseCaseResult.Success -> it.value.themeColor.toUiModel()
                    is UseCaseResult.Failure -> {
                        it.exception.fallbackSetting.themeColor.toUiModel()
                    }
                }
            }.distinctUntilChanged().onEach { themeColor ->
                _uiState.update {
                    it.copy(
                        themeColor = themeColor
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun onRequestBottomNavigationVisibleChange(isVisible: Boolean) {
        if (isVisible) {
            updateToBottomNavigationVisibleState()
        } else {
            updateToBottomNavigationInvisibleState()
        }
    }

    fun onRequestBottomNavigationEnabledChange(isEnabled: Boolean) {
        updateIsInputDisabled(!isEnabled)
    }

    fun onFragmentProgressStateChanged(isProcessing: Boolean) {
        if (isProcessing) {
            updateToProcessingState()
        } else {
            updateToIdleState()
        }
    }

    fun onBottomNavigationItemSelect() {
        updateWasSelectedTab(true)
    }

    fun onFragmentTransitionSetupCompleted() {
        updateWasSelectedTab(false)
    }

    fun onNavigateBackFromBottomNavigationTab() {
        viewModelScope.launch {
            updateActivityUiEvent(MainActivityUiEvent.NavigateStartTabFragment)
        }
    }

    fun onBottomNavigationItemReselect() {
        viewModelScope.launch {
            updateFragmentUiEvent(FragmentUiEvent.ProcessOnBottomNavigationItemReselect)
        }
    }

    private suspend fun updateActivityUiEvent(event: MainActivityUiEvent) {
        _activityUiEvent.emit(
            ConsumableEvent(event)
        )
    }

    private suspend fun updateFragmentUiEvent(event: FragmentUiEvent) {
        _fragmentUiEvent.emit(
            ConsumableEvent(event)
        )
    }

    private fun updateIsInputDisabled(isDisabled: Boolean) {
        _uiState.update {
            it.copy(
                isInputDisabled = isDisabled
            )
        }
    }

    private fun updateWasSelectedTab(wasSelected: Boolean) {
        _uiState.update {
            it.copy(
                wasSelectedTab = wasSelected
            )
        }
    }

    private fun updateToIdleState() {
        _uiState.update {
            it.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updateToProcessingState() {
        _uiState.update {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    private fun updateToBottomNavigationVisibleState() {
        _uiState.update {
            it.copy(
                isBottomNavigationVisible = true,
                isInputDisabled = false
            )
        }
    }

    private fun updateToBottomNavigationInvisibleState() {
        _uiState.update {
            it.copy(
                isBottomNavigationVisible = false,
                isInputDisabled = true
            )
        }
    }
}
