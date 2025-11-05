package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.message.CommonAppMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

internal abstract class BaseViewModel<S: UiState, E: UiEvent, M: AppMessage>(
    initialViewUiState: S
) : ViewModel() {

    //region Properties
    private val _uiState = MutableStateFlow(initialViewUiState)
    val uiState get() = _uiState.asStateFlow()

    protected val currentUiState
        get() = _uiState.value

    protected open val isReadyForOperation
        get() = !currentUiState.isInputDisabled

    private val _uiEvent = MutableSharedFlow<ConsumableEvent<E>>(replay = 1)
    val uiEvent get() = _uiEvent.asSharedFlow()
    //endregion

    //region UI State Update
    protected open fun updateUiState(state: S) {
        _uiState.value = state
    }

    protected open fun updateUiState(function: (S) -> S) {
        _uiState.update{ function(it) }
    }
    //endregion

    //region UI Event Emission
    protected abstract suspend fun emitAppMessageEvent(appMessage: M)

    protected abstract suspend fun emitCommonAppMessageEvent(appMessage: CommonAppMessage)

    protected abstract suspend fun emitUnexpectedAppMessage(e: Exception)

    protected suspend fun emitUiEvent(event: E) {
        _uiEvent.emit(
            ConsumableEvent(event)
        )
    }
    //endregion

    //region Error Handlers
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
            val initialState = rollbackState ?: currentUiState
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
        val initialState = rollbackState ?: currentUiState
        try {
            block()
        } catch (e: Exception) {
            handleUnexpectedError(e, initialState)
        }
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

    private suspend fun handleUnexpectedError(e: Exception, rollbackState: S) {
        // コルーチンのキャンセルはエラーではないため、再スローして処理を中断させる
        if (e is CancellationException) {
            throw e
        }
        Log.e(logTag, "予期せぬエラーが発生", e)
        updateUiState(rollbackState)
        emitUnexpectedAppMessage(e)
    }
    //endregion
}
