package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.navigation.PendingNavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal abstract class BaseViewModel<E: UiEvent, M: AppMessage, S: UiState>(
    initialViewUiState: S
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

    abstract val isProgressIndicatorVisible:StateFlow<Boolean>

    // 表示保留中Navigation
    private val _pendingNavigationCommandList =
        MutableStateFlow(emptyList<PendingNavigationCommand>())
    val pendingNavigationCommandList
        get() = _pendingNavigationCommandList.asStateFlow()

    protected suspend fun emitUiEvent(event: E) {
        _uiEvent.emit(
            ConsumableEvent(event)
        )
    }

    protected abstract suspend fun emitNavigatePreviousFragmentEvent(
        result: FragmentResult<*> = FragmentResult.None
    )

    protected abstract suspend fun emitAppMessageEvent(appMessage: M)

    protected open fun updateUiState(state: S) {
        _uiState.value = state
    }

    abstract fun onBackPressed()

    fun onFragmentNavigationFailure(command: NavigationCommand) {
        updatePendingNavigationCommandList { it + PendingNavigationCommand(command) }
    }

    fun onPendingFragmentNavigationComplete(command: PendingNavigationCommand) {
        updatePendingNavigationCommandList { it - command }
    }

    fun onPendingFragmentNavigationFailure(command: PendingNavigationCommand) {
        updatePendingNavigationCommandList { list ->
            list.map { commandInList ->
                if (commandInList == command) {
                    commandInList.incrementRetryCount()
                } else {
                    commandInList
                }
            }
        }
    }

    private fun updatePendingNavigationCommandList(
        function: (List<PendingNavigationCommand>) -> List<PendingNavigationCommand>
    ) {
        _pendingNavigationCommandList.update(function)
    }
}
