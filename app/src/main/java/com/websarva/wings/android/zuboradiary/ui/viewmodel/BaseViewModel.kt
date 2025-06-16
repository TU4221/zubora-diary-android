package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.state.ViewModelState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal abstract class BaseViewModel<E: ViewModelEvent, M: AppMessage, S: ViewModelState> : ViewModel() {

    private val logTag = createLogTag()

    private val _viewModelEvent = MutableSharedFlow<ConsumableEvent<ViewModelEvent>>(replay = 1)
    val viewModelEvent get() = _viewModelEvent.asSharedFlow()

    private val initialViewModelState = ViewModelState.Idle
    private val _viewModelState = MutableStateFlow<ViewModelState>(initialViewModelState)
    val viewModelState get() = _viewModelState.asStateFlow()
    // TODO:下記が必要か最後に判断
    val isProcessingStateFlow: StateFlow<Boolean> =
        _viewModelState
            .map { state -> state != ViewModelState.Idle }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )
    val isProcessing get() = isProcessingStateFlow.value

    // 表示保留中Navigation
    private val initialPendingNavigationCommand = NavigationCommand.None
    private val _pendingNavigationCommand =
        MutableStateFlow<NavigationCommand>(initialPendingNavigationCommand)
    val pendingNavigationCommand
        get() = _pendingNavigationCommand.asStateFlow()

    open fun initialize() {
        Log.d(logTag, "initialize()")
        _viewModelState.value = initialViewModelState
        _pendingNavigationCommand.value = initialPendingNavigationCommand
    }

    protected suspend fun emitViewModelEvent(event: E) {
        _viewModelEvent.emit(
            ConsumableEvent(event)
        )
    }

    protected suspend fun emitNavigatePreviousFragmentEvent() {
        _viewModelEvent.emit(
            ConsumableEvent(
                ViewModelEvent.NavigatePreviousFragment
            )
        )
    }

    protected suspend fun emitAppMessageEvent(appMessage: M) {
        _viewModelEvent.emit(
            ConsumableEvent(
                ViewModelEvent.NavigateAppMessage(
                    appMessage
                )
            )
        )
    }

    protected fun updateViewModelState(state: S) {
        _viewModelState.value = state
    }

    protected fun updateViewModelIdleState() {
        _viewModelState.value = ViewModelState.Idle
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
