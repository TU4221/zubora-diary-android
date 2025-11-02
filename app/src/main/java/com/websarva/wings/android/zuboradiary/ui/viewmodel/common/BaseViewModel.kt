package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.navigation.PendingNavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.message.CommonAppMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    
    private val logMsgPendingNavi = "保留ナビゲーション_"

    private val _uiEvent = MutableSharedFlow<ConsumableEvent<E>>(replay = 1)
    val uiEvent get() = _uiEvent.asSharedFlow()

    private val _commonUiEvent = MutableSharedFlow<ConsumableEvent<CommonUiEvent>>(replay = 1)
    val commonUiEvent get() = _commonUiEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(initialViewUiState)
    val uiState get() = _uiState.asStateFlow()

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

    private suspend fun emitCommonUiEvent(event: CommonUiEvent) {
        _commonUiEvent.emit(
            ConsumableEvent(event)
        )
    }

    protected suspend fun emitNavigatePreviousFragmentEvent(
        result: FragmentResult<*> = FragmentResult.None
    ) {
        emitCommonUiEvent(CommonUiEvent.NavigatePreviousFragment(result))
    }

    protected suspend fun emitAppMessageEvent(appMessage: M) {
        emitCommonUiEvent(CommonUiEvent.NavigateAppMessage(appMessage))
    }

    protected suspend fun emitUnexpectedAppMessage(e: Exception) {
        emitCommonAppMessageEvent(CommonAppMessage.Unexpected(e))
    }

    private suspend fun emitCommonAppMessageEvent(appMessage: CommonAppMessage) {
        emitCommonUiEvent(CommonUiEvent.NavigateAppMessage(appMessage))
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
                handleUnexpectedError(e, initialState)
            }
        }
    }

    protected suspend fun withUnexpectedErrorHandler(
        rollbackState: S? = null,
        block: suspend () -> Unit
    ) {
        val initialState = rollbackState ?: _uiState.value
        try {
            block()
        } catch (e: Exception) {
            handleUnexpectedError(e, initialState)
        }
    }

    private suspend fun handleUnexpectedError(e: Exception, rollbackState: S) {
        // コルーチンのキャンセルはエラーではないため、再スローして処理を中断させる
        if (e is CancellationException) {
            throw e
        }
        Log.e(logTag, "予期せぬエラーが発生", e)
        updateUiState(rollbackState)
        emitUnexpectedAppMessage(e)
    }

    /**
     * Flowストリーム内の予期せぬ例外をキャッチし、ログ出力とUIへのメッセージ通知を行う。
     * 例外発生後は、 [fallbackValue] をemitする。
     *
     * @param fallbackValue 例外発生時にFlowに流す代替の値。
     * @param block 例外発生時に加えて実行したい処理ブロック。発生した例外が引数として渡される。
     * @return エラーハンドリングが適用された新しいFlow。
     */
    protected fun <T> Flow<T>.catchUnexpectedError(
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

    protected open fun updateUiState(state: S) {
        _uiState.value = state
    }

    protected open fun updateUiState(function: (S) -> S) {
        _uiState.update{ function(it) }
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

    fun onPendingFragmentNavigationRetryLimitReached(command: PendingNavigationCommand) {
        Log.e(
            logTag,
            "${logMsgPendingNavi}保留中のナビゲーションがリトライ回数に到達。コマンド: $command"
        )
        updatePendingNavigationCommandList { it - command }
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
