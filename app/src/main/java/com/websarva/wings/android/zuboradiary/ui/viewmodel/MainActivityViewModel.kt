package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.message.CommonAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.message.MainActivityAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.MainActivityUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
) : ViewModel() {

    companion object {
        private const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }

    private val _activityUiEvent = MutableSharedFlow<ConsumableEvent<MainActivityUiEvent>>(replay = 1)
    val activityUiEvent get() = _activityUiEvent.asSharedFlow()

    private val _activityCallbackUiEvent =
        MutableSharedFlow<ConsumableEvent<ActivityCallbackUiEvent>>(replay = 1)
    val activityCallbackUiEvent get() = _activityCallbackUiEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(
        handle.get<MainActivityUiState>(SAVED_STATE_UI_KEY)?.let { savedUiState ->
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
        _uiState.onEach {
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
                                emitActivityUiEvent(
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
        _uiState.distinctUntilChanged{ old, new ->
            old.isInputDisabled == new.isInputDisabled && old.isNavigating == new.isNavigating
        }.map {
            !it.isInputDisabled && !it.isNavigating
        }.distinctUntilChanged().onEach { isEnabled ->
            updateIsBottomNavigationEnabled(isEnabled)
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

    // TODO:BaseViewModelを実装する?(その場合BaseFragmentViewModelを用意する必要が出てくる。)
    private suspend fun emitAppMessage(appMessage: AppMessage) {
        emitActivityUiEvent(
            MainActivityUiEvent.NavigateAppMessage(appMessage)
        )
    }

    // TODO:BaseViewModelを実装する?(その場合BaseFragmentViewModelを用意する必要が出てくる。)
    private suspend fun emitUnexpectedAppMessage(e: Exception) {
        emitAppMessage(CommonAppMessage.Unexpected(e))
    }

    // TODO:BaseViewModelを実装する?(その場合BaseFragmentViewModelを用意する必要が出てくる。)
    /**
     * Flowストリーム内の予期せぬ例外をキャッチし、ログ出力とUIへのメッセージ通知を行う。
     * 例外発生後は、 [fallbackValue] をemitする。
     *
     * @param fallbackValue 例外発生時にFlowに流す代替の値。
     * @param block 例外発生時に加えて実行したい処理ブロック。発生した例外が引数として渡される。
     * @return エラーハンドリングが適用された新しいFlow。
     */
    private fun <T> Flow<T>.catchUnexpectedError(
        fallbackValue: T,
        block: suspend (e: Exception) -> Unit = { }
    ): Flow<T> {
        return this.catch { e ->
            if (e !is Exception) throw e

            Log.e(logTag, "Flowストリーム処理中に予期せぬエラーが発生しました。", e)
            emitUnexpectedAppMessage(e)
            block(e)
            emit(fallbackValue)
        }
    }

    private suspend fun emitActivityCallbackUiEvent(event: ActivityCallbackUiEvent) {
        _activityCallbackUiEvent.emit(
            ConsumableEvent(event)
        )
    }

    private fun updateThemeColor(themeColor: ThemeColorUi) {
        _uiState.update { it.copy(themeColor = themeColor) }
    }

    private fun updateIsBottomNavigationEnabled(isEnable: Boolean) {
        _uiState.update { it.copy(isBottomNavigationEnabled = isEnable) }
    }

    private fun updateIsNavigating(isNavigating: Boolean) {
        _uiState.update { it.copy(isNavigating = isNavigating) }
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

    private fun updateWasVisibleFragmentTransitionSetupCompleted(wasCompleted: Boolean) {
        _wasVisibleFragmentTransitionSetupCompleted.update { wasCompleted }
    }

    private fun updateWasInvisibleFragmentTransitionSetupCompleted(wasCompleted: Boolean) {
        _wasInvisibleFragmentTransitionSetupCompleted.update { wasCompleted }
    }

    private fun markVisibleFragmentTransitionSetupCompleted() {
        if (!_wasBottomNavigationTabSelected.value) return
        updateWasVisibleFragmentTransitionSetupCompleted(true)
    }

    private fun markInvisibleFragmentTransitionSetupCompleted() {
        if (!_wasBottomNavigationTabSelected.value) return
        updateWasInvisibleFragmentTransitionSetupCompleted(true)
    }
}
