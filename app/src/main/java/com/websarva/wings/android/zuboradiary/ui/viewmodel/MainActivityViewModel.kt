package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ThemeColorSettingLoadException
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.event.MainActivityUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.message.CommonAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.message.MainActivityAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.MainActivityUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
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
    private val handle: SavedStateHandle,
    private val loadThemeColorSettingUseCase: LoadThemeColorSettingUseCase,
) : BaseViewModel<MainActivityUiEvent, MainActivityAppMessage, MainActivityUiState>(
    handle.get<MainActivityUiState>(SAVED_STATE_UI_KEY)?.let { savedUiState ->
        MainActivityUiState().copy(
            themeColor = savedUiState.themeColor,
            isBottomNavigationVisible = savedUiState.isBottomNavigationVisible
        )
    } ?: MainActivityUiState()
) {

    //region Properties
    private val _wasBottomNavigationTabSelected = MutableStateFlow(false)
    val wasSelectedTab get() = _wasBottomNavigationTabSelected.asStateFlow()

    private val _wasVisibleFragmentTransitionSetupCompleted = MutableStateFlow(false)

    private val _wasInvisibleFragmentTransitionSetupCompleted = MutableStateFlow(false)
    
    private val _activityCallbackUiEvent =
        MutableSharedFlow<ConsumableEvent<ActivityCallbackUiEvent>>(replay = 1)
    val activityCallbackUiEvent get() = _activityCallbackUiEvent.asSharedFlow()
    //endregion

    //region Initialization
    init {
        collectUiStates()
    }

    private fun collectUiStates() {
        collectUiState()
        collectThemeColorSetting()
        collectFragmentTransitionSetupCompleted()
        collectBottomNavigationEnabled()
    }

    private fun collectUiState() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    private fun collectThemeColorSetting() {
        loadThemeColorSettingUseCase()
            .onEach {
                when (it) {
                    is UseCaseResult.Success -> { /*処理なし*/ }
                    is UseCaseResult.Failure -> {
                        when (it.exception) {
                            is ThemeColorSettingLoadException.LoadFailure -> {
                                emitUiEvent(
                                    MainActivityUiEvent.NavigateAppMessage(
                                        MainActivityAppMessage.SettingsLoadFailure
                                    )
                                )
                            }
                            is ThemeColorSettingLoadException.Unknown -> {
                                emitUnexpectedAppMessage(it.exception)
                            }
                        }
                    }
                }
            }.map {
                when (it) {
                    is UseCaseResult.Success -> it.value
                    is UseCaseResult.Failure -> it.exception.fallbackSetting
                }
            }.catchUnexpectedError(
                ThemeColorSetting.default()
            ).distinctUntilChanged().onEach {
                updateThemeColor(it.themeColor.toUiModel())
            }.launchIn(viewModelScope)
    }

    private fun collectFragmentTransitionSetupCompleted() {
        combine(
            _wasVisibleFragmentTransitionSetupCompleted,
            _wasInvisibleFragmentTransitionSetupCompleted
        ) { wasVisibleFragmentCompleted, wasInvisibleFragmentCompleted ->
            wasVisibleFragmentCompleted && wasInvisibleFragmentCompleted
        }.onEach {
            if (it) {
                updateWasBottomNavigationTabSelected(false)
                updateWasVisibleFragmentTransitionSetupCompleted(false)
                updateWasInvisibleFragmentTransitionSetupCompleted(false)
            }
        }.launchIn(viewModelScope)
    }

    private fun collectBottomNavigationEnabled() {
        uiState.distinctUntilChanged{ old, new ->
            old.isInputDisabled == new.isInputDisabled && old.isNavigating == new.isNavigating
        }.map {
            !it.isInputDisabled && !it.isNavigating
        }.distinctUntilChanged().onEach { isEnabled ->
            updateIsBottomNavigationEnabled(isEnabled)
        }.launchIn(viewModelScope)
    }
    //endregion

    //region Activity UI Event Handlers
    fun onBottomNavigationItemSelect() {
        updateWasBottomNavigationTabSelected(true)
    }

    fun onBottomNavigationItemReselect() {
        viewModelScope.launch {
            emitActivityCallbackUiEvent(ActivityCallbackUiEvent.ProcessOnBottomNavigationItemReselect)
        }
    }

    fun onNavigateBackFromBottomNavigationTab() {
        viewModelScope.launch {
            emitUiEvent(MainActivityUiEvent.NavigateStartTabFragment)
        }
    }
    //endregion

    //region Fragment UI Event Handlers
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

    fun onVisibleFragmentTransitionSetupCompleted() {
        markVisibleFragmentTransitionSetupCompleted()
    }

    fun onInvisibleFragmentTransitionSetupCompleted() {
        markInvisibleFragmentTransitionSetupCompleted()
    }
    //endregion

    //region Business Logic
    private fun markVisibleFragmentTransitionSetupCompleted() {
        if (!_wasBottomNavigationTabSelected.value) return
        updateWasVisibleFragmentTransitionSetupCompleted(true)
    }

    private fun markInvisibleFragmentTransitionSetupCompleted() {
        if (!_wasBottomNavigationTabSelected.value) return
        updateWasInvisibleFragmentTransitionSetupCompleted(true)
    }
    //endregion

    //region UI State Update
    private fun updateThemeColor(themeColor: ThemeColorUi) {
        updateUiState { it.copy(themeColor = themeColor) }
    }

    private fun updateIsBottomNavigationEnabled(isEnable: Boolean) {
        updateUiState { it.copy(isBottomNavigationEnabled = isEnable) }
    }

    private fun updateIsNavigating(isNavigating: Boolean) {
        updateUiState { it.copy(isNavigating = isNavigating) }
    }

    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updateToProcessingState() {
        updateUiState {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    private fun updateToBottomNavigationVisibleState() {
        updateUiState {
            it.copy(
                isBottomNavigationVisible = true,
                isInputDisabled = false
            )
        }
    }

    private fun updateToBottomNavigationInvisibleState() {
        updateUiState {
            it.copy(
                isBottomNavigationVisible = false,
                isInputDisabled = true
            )
        }
    }
    //endregion

    //region Internal State Update
    private fun updateWasBottomNavigationTabSelected(wasSelected: Boolean) {
        _wasBottomNavigationTabSelected.update { wasSelected }
    }

    private fun updateWasVisibleFragmentTransitionSetupCompleted(wasCompleted: Boolean) {
        _wasVisibleFragmentTransitionSetupCompleted.update { wasCompleted }
    }

    private fun updateWasInvisibleFragmentTransitionSetupCompleted(wasCompleted: Boolean) {
        _wasInvisibleFragmentTransitionSetupCompleted.update { wasCompleted }
    }
    //endregion

    //region UI Event Emission
    override suspend fun emitAppMessageEvent(appMessage: MainActivityAppMessage) {
        emitUiEvent(
            MainActivityUiEvent.NavigateAppMessage(appMessage)
        )
    }

    override suspend fun emitUnexpectedAppMessage(e: Exception) {
        emitCommonAppMessageEvent(
            CommonAppMessage.Unexpected(e)
        )
    }

    override suspend fun emitCommonAppMessageEvent(appMessage: CommonAppMessage) {
        emitUiEvent(
            MainActivityUiEvent.NavigateAppMessage(appMessage)
        )
    }

    private suspend fun emitActivityCallbackUiEvent(event: ActivityCallbackUiEvent) {
        _activityCallbackUiEvent.emit(
            ConsumableEvent(event)
        )
    }
    //endregion

    companion object {
        private const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
