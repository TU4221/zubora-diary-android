package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

internal abstract class BaseViewModel<E: UiEvent, M: AppMessage, S: UiState>(
    private val initialViewUiState: S
) : ViewModel() {

    fun <T> Flow<T>.stateInWhileSubscribed(
        initialValue: T
    ): StateFlow<T> {
        return this.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            initialValue
        )
    }

    private val logTag = createLogTag()

    private val _uiEvent = MutableSharedFlow<ConsumableEvent<E>>(replay = 1)
    val uiEvent get() = _uiEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(initialViewUiState)
    val uiState get() = _uiState.asStateFlow()

    abstract val isProcessingState: StateFlow<Boolean>
    val isProcessing get() = isProcessingState.value

    open val isProgressIndicatorVisible get() = isProcessingState

    // 表示保留中Navigation
    private val initialPendingNavigationCommand = NavigationCommand.None
    private val _pendingNavigationCommand =
        MutableStateFlow<NavigationCommand>(initialPendingNavigationCommand)
    val pendingNavigationCommand
        get() = _pendingNavigationCommand.asStateFlow()

    open fun initialize() {
        Log.d(logTag, "initialize()")
        _uiState.value = initialViewUiState
        _pendingNavigationCommand.value = initialPendingNavigationCommand
    }

    protected suspend fun emitUiEvent(event: E) {
        _uiEvent.emit(
            ConsumableEvent(event)
        )
    }

    protected abstract suspend fun emitNavigatePreviousFragmentEvent()

    protected abstract suspend fun emitAppMessageEvent(appMessage: M)

    protected fun updateUiState(state: S) {
        _uiState.value = state
    }

    abstract fun onBackPressed()

    fun onFragmentNavigationFailed(command: NavigationCommand) {
        _pendingNavigationCommand.value = command
    }

    fun onPendingFragmentNavigationCompleted() {
        _pendingNavigationCommand.value = NavigationCommand.None
    }

    fun onPendingFragmentNavigationFailed() {
        _pendingNavigationCommand.value = NavigationCommand.None
    }
}
