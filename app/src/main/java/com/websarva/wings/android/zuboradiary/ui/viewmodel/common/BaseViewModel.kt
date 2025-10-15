package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.navigation.PendingNavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

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
    private val logMsgPendingNavi = "保留ナビゲーション_"

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

    protected abstract fun createNavigatePreviousFragmentEvent(
        result: FragmentResult<*> = FragmentResult.None
    ): E

    protected suspend fun emitNavigatePreviousFragmentEvent(
        result: FragmentResult<*> = FragmentResult.None
    ) {
        val navigatePreviousFragmentEvent = createNavigatePreviousFragmentEvent(result)
        emitUiEvent(navigatePreviousFragmentEvent)
    }

    protected abstract fun createAppMessageEvent(appMessage: M): E

    protected suspend fun emitAppMessageEvent(appMessage: M) {
        val appMessageEvent = createAppMessageEvent(appMessage)
        emitUiEvent(appMessageEvent)
    }

    protected abstract fun createUnexpectedAppMessage(e: Exception): M

    protected suspend fun emitUnexpectedAppMessage(e: Exception) {
        val appMessage = createUnexpectedAppMessage(e)
        emitAppMessageEvent(appMessage)
    }

    /**
     * 予期せぬ例外のハンドリング付きでコルーチンを起動する。
     *
     * [block] 内で [CancellationException] 以外の予期せぬ例外が発生した場合、
     * エラーダイアログ表示イベントを発行する。
     * また、UIが処理中のまま固まるのを防ぐため、 [uiState] を指定された状態にロールバックする。
     *
     * @param rollbackState 予期せぬエラー発生時にロールバックするUI State。デフォルトは処理開始前のState。
     * @param block 実行するメインの処理ブロック。
     * @return 起動したコルーチンの[Job]。
     */
    protected fun launchWithUnexpectedErrorHandler(
        rollbackState: S? = null,
        block: suspend CoroutineScope.() -> Unit
    ) : Job {
        return viewModelScope.launch {
            val initialState = rollbackState ?: _uiState.value
            try {
                block()
            } catch (e: Exception) {
                // コルーチンのキャンセルはエラーではないため、再スローして処理を中断させる
                if (e is CancellationException) {
                    throw e
                }
                Log.e(logTag, "予期せぬエラーが発生", e)
                updateUiState(initialState)
                emitUnexpectedAppMessage(e)
            }
        }
    }

    protected open fun updateUiState(state: S) {
        _uiState.value = state
    }

    abstract fun onBackPressed()

    fun onFragmentNavigationFailure(command: NavigationCommand) {
        val newPendingCommand = PendingNavigationCommand(command)
        Log.d(
            logTag,
            "${logMsgPendingNavi}失敗したナビゲーションを保留リストに追加。コマンド: $newPendingCommand"
        )
        updatePendingNavigationCommandList { it + newPendingCommand }
    }

    fun onPendingFragmentNavigationComplete(command: PendingNavigationCommand) {
        Log.d(
            logTag,
            "${logMsgPendingNavi}保留中のナビゲーションが完了。リストから削除。コマンド: $command"
        )
        updatePendingNavigationCommandList { it - command }
    }

    fun onPendingFragmentNavigationFailure(command: PendingNavigationCommand) {
        Log.d(
            logTag,
            "${logMsgPendingNavi}保留中のナビゲーションが再度失敗。リトライ回数を更新。コマンド: $command"
        )
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
        val beforeList = _pendingNavigationCommandList.value
        Log.d(logTag, "${logMsgPendingNavi}更新前のリスト: $beforeList")

        _pendingNavigationCommandList.update(function)

        val afterList = _pendingNavigationCommandList.value
        Log.d(logTag, "${logMsgPendingNavi}更新後のリスト: $afterList")
    }
}
