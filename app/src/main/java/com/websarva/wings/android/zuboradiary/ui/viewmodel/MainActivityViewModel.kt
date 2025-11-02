package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ThemeColorSettingLoadException
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.event.MainActivityUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.message.CommonAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.message.MainActivityAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.MainActivityUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _activityCallbackUiEvent =
        MutableSharedFlow<ConsumableEvent<ActivityCallbackUiEvent>>(replay = 1)
    val activityCallbackUiEvent get() = _activityCallbackUiEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(
        handle.get<MainActivityUiState>(SAVED_UI_STATE_KEY)?.let { savedUiState ->
            MainActivityUiState().copy(
                themeColor = savedUiState.themeColor,
                isBottomNavigationVisible = savedUiState.isBottomNavigationVisible
            )
        } ?: MainActivityUiState()
    )
    val uiState get() = _uiState.asStateFlow()

    private val _wasBottomNavigationTabSelected = MutableStateFlow(false)
    val wasSelectedTab get() = _wasBottomNavigationTabSelected.asStateFlow()

    private val _wasVisibleFragmentTransitionSetupCompleted = MutableStateFlow(false)

    private val _wasInvisibleFragmentTransitionSetupCompleted = MutableStateFlow(false)

    private val wasFragmentTransitionSetupCompletedFlow =
        combine(
            _wasVisibleFragmentTransitionSetupCompleted,
            _wasInvisibleFragmentTransitionSetupCompleted
        ) { wasVisibleFragmentCompleted, wasInvisibleFragmentCompleted ->
            wasVisibleFragmentCompleted && wasInvisibleFragmentCompleted
        }

    init {
        observeDerivedUiStateChanges(handle)
    }

    private fun observeDerivedUiStateChanges(handle: SavedStateHandle) {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_UI_STATE_KEY] = it
        }.launchIn(viewModelScope)

        loadThemeColorSettingUseCase()
            .onEach {
                when (it) {
                    is UseCaseResult.Success -> { /*処理なし*/ }
                    is UseCaseResult.Failure -> {
                        val appMessage = when (it.exception) {
                            is ThemeColorSettingLoadException.LoadFailure -> {
                                MainActivityAppMessage.SettingsLoadFailure
                            }
                            is ThemeColorSettingLoadException.Unknown -> {
                                CommonAppMessage.Unexpected(it.exception)
                            }
                        }
                        emitActivityUiEvent(
                            MainActivityUiEvent.NavigateAppMessage(appMessage)
                        )
                    }
                }
            }.map {
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

        wasFragmentTransitionSetupCompletedFlow.onEach {
            if (it) {
                updateWasBottomNavigationTabSelected(false)
                _wasVisibleFragmentTransitionSetupCompleted.update { false }
                _wasInvisibleFragmentTransitionSetupCompleted.update { false }
            }
        }.launchIn(viewModelScope)

        combine(
            _uiState.map { it.isInputDisabled },
            _uiState.map { it.isNavigating }
        ) { isInputDisabled, isNavigating ->
            !isInputDisabled && !isNavigating
        }.distinctUntilChanged().onEach { isBottomNavigationEnabled ->
            _uiState.update {
                it.copy(
                    isBottomNavigationEnabled = isBottomNavigationEnabled
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onFragmentViewReady(needsBottomNavigation: Boolean) {
        if (needsBottomNavigation) {
            updateToBottomNavigationVisibleState()
        } else {
            updateToBottomNavigationInvisibleState()
        }
    }

    fun onFragmentViewResumed() {
        updateIsNavigating(false)
    }

    fun onFragmentViewPause() {
        if (!_wasBottomNavigationTabSelected.value) return
        updateIsNavigating(true)
    }

    fun onFragmentProcessingStateChanged(isProcessing: Boolean) {
        if (isProcessing) {
            updateToProcessingState()
        } else {
            updateToIdleState()
        }
    }

    fun onBottomNavigationItemSelect() {
        updateWasBottomNavigationTabSelected(true)
    }

    fun onVisibleFragmentTransitionSetupCompleted() {
        markVisibleFragmentTransitionSetupCompleted()
    }

    fun onInvisibleFragmentTransitionSetupCompleted() {
        markInvisibleFragmentTransitionSetupCompleted()
    }

    fun onNavigateBackFromBottomNavigationTab() {
        viewModelScope.launch {
            emitActivityUiEvent(MainActivityUiEvent.NavigateStartTabFragment)
        }
    }

    fun onBottomNavigationItemReselect() {
        viewModelScope.launch {
            emitActivityCallbackUiEvent(ActivityCallbackUiEvent.ProcessOnBottomNavigationItemReselect)
        }
    }

    private suspend fun emitActivityUiEvent(event: MainActivityUiEvent) {
        _activityUiEvent.emit(
            ConsumableEvent(event)
        )
    }

    private suspend fun emitActivityCallbackUiEvent(event: ActivityCallbackUiEvent) {
        _activityCallbackUiEvent.emit(
            ConsumableEvent(event)
        )
    }

    private fun updateIsNavigating(isNavigating: Boolean) {
        _uiState.update {
            it.copy(
                isNavigating = isNavigating
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

    private fun updateWasBottomNavigationTabSelected(wasSelected: Boolean) {
        _wasBottomNavigationTabSelected.update { wasSelected }
    }

    private fun markVisibleFragmentTransitionSetupCompleted() {
        if (!_wasBottomNavigationTabSelected.value) return
        _wasVisibleFragmentTransitionSetupCompleted.update { true }
    }

    private fun markInvisibleFragmentTransitionSetupCompleted() {
        if (!_wasBottomNavigationTabSelected.value) return
        _wasInvisibleFragmentTransitionSetupCompleted.update { true }
    }
}
