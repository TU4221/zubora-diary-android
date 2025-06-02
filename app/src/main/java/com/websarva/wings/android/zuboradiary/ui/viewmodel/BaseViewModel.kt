package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

internal abstract class BaseViewModel<E: ViewModelEvent, M: AppMessage> : ViewModel() {

    private val logTag = createLogTag()

    private val _viewModelEvent = MutableSharedFlow<ConsumableEvent<ViewModelEvent>>(replay = 1)
    val viewModelEvent = _viewModelEvent.asSharedFlow()

    // 表示保留中Navigation
    private val _pendingNavigationCommand =
        MutableStateFlow<NavigationCommand>(NavigationCommand.None)

    open fun initialize() {
        Log.d(logTag, "initialize()")
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

    abstract fun onBackPressed()

    val pendingNavigationCommand
        get() = _pendingNavigationCommand.asStateFlow()

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
