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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * このアプリケーションにおける全てのViewModelの基底クラス。
 *
 * 以下の責務を持つ:
 * - UIの状態([UiState])の管理と、[StateFlow]としての公開
 * - UIイベント([UiEvent])の発生と、[SharedFlow]としての公開
 * - [viewModelScope]内でのコルーチンの起動と、予期せぬ例外の共通ハンドリング
 *
 * @param S このViewModelが管理するUI状態の型。
 * @param E このViewModelが発行するUIイベントの型。
 * @param M このViewModelが発行する固有のアプリケーションメッセージの型。
 */
abstract class BaseViewModel<S: UiState, E: UiEvent, M: AppMessage> internal constructor(
    initialViewUiState: S
) : ViewModel() {

    //region Properties
    /** UIの状態を保持し、外部に公開するためのStateFlow。 */
    private val _uiState = MutableStateFlow(initialViewUiState)
    val uiState get() = _uiState.asStateFlow()

    /** 現在のUI状態[UiState]へのスナップショットアクセスを提供する。 */
    protected val currentUiState
        get() = _uiState.value

    /** ViewModelが操作を受け入れ可能な状態かを示す。デフォルトでは[UiState.isInputDisabled]がfalseであるかを確認する。 */
    protected open val isReadyForOperation
        get() = !currentUiState.isInputDisabled

    /** 画面遷移やメッセージ表示などの一度きりのUIイベントを通知するためのSharedFlow。 */
    private val _uiEvent = MutableSharedFlow<ConsumableEvent<E>>(replay = 1)
    internal val uiEvent get() = _uiEvent.asSharedFlow()
    //endregion

    //region UI State Update
    /**
     * UIの状態[UiState]を新しい状態で完全に置き換える。
     * @param state 新しいUI状態。
     */
    protected fun updateUiState(state: S) {
        _uiState.value = state
    }

    /**
     * 現在のUI状態[UiState]を基に、安全に状態を更新する。
     * @param function 現在の状態を受け取り、新しい状態を返す関数。
     */
    protected fun updateUiState(function: (S) -> S) {
        _uiState.update{ function(it) }
    }
    //endregion

    //region UI Event Emission
    //TODO:クラス内で参照されていない為、記述場所検討
    /**
     * 画面固有のアプリケーションメッセージ([AppMessage])をUIに通知するイベントを発行する。
     * @param appMessage 表示するメッセージ。
     */
    protected abstract suspend fun emitAppMessageEvent(appMessage: M)

    //TODO:クラス内で参照されていない為、記述場所検討
    /**
     * アプリケーション共通のメッセージ([CommonAppMessage])をUIに通知するイベントを発行する。
     * @param appMessage 表示する共通メッセージ。
     */
    protected abstract suspend fun emitCommonAppMessageEvent(appMessage: CommonAppMessage)

    /**
     * [launchWithUnexpectedErrorHandler]や[catchUnexpectedError]で補足された予期せぬ例外をUIに通知する。
     * @param e 補足された例外。
     */
    protected abstract suspend fun emitUnexpectedAppMessage(e: Exception)

    /**
     * 画面遷移や一時的なUIの変更など、一度きりのUIイベント([UiEvent])を発行する。
     * @param event 発行するUIイベント。
     */
    protected suspend fun emitUiEvent(event: E) {
        _uiEvent.emit(
            ConsumableEvent(event)
        )
    }
    //endregion

    //region Error Handling
    /**
     * 予期せぬ例外のハンドリング付きでコルーチンを起動する。
     *
     * [block]内で[CancellationException]以外の予期せぬ例外が発生した場合、
     * エラーダイアログ表示イベントを発行する。
     * また、UIが処理中のまま固まるのを防ぐため、[uiState]を指定された状態にロールバックする。
     *
     * @param rollbackState 予期せぬエラー発生時にロールバックするUI状態。デフォルトは処理開始前の状態。
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

    /**
     * 既存のコルーチン内で、予期せぬ例外のハンドリング付きで処理を実行する。
     *
     * [block]内で[CancellationException]以外の予期せぬ例外が発生した場合、
     * [handleUnexpectedError]を呼び出して共通のエラー処理を行う。
     *
     * @param rollbackState 予期せぬエラー発生時にロールバックするUI状態。デフォルトは処理開始前の状態。
     * @param block 実行する中断可能な処理ブロック。
     */
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
     * [Flow]ストリーム内の予期せぬ例外をキャッチし、UIへのメッセージ通知を行う。
     * 例外発生後は、[fallbackValue]をemitする。
     *
     * @param fallbackValue 例外発生時にFlowに流す代替の値。
     * @param block 例外発生時に加えて実行したい処理ブロック。発生した例外が引数として渡される。
     * @return エラーハンドリングが適用された新しい[Flow]。
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

    /**
     * 予期せぬ例外を処理する共通ハンドラ。
     *
     * [CancellationException]は処理を中断させるために再スローする。
     * それ以外の例外は、UI状態をロールバックした上で、
     * [emitUnexpectedAppMessage]を呼び出してユーザーにエラーを通知する。
     *
     * @param e 補足された例外。
     * @param rollbackState ロールバック先のUI状態。
     */
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
