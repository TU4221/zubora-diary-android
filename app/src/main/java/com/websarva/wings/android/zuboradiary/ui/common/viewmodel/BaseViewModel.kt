package com.websarva.wings.android.zuboradiary.ui.common.viewmodel

import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.ui.common.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.common.state.UiState
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.NavigationEventCallback
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.NavigationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus
import kotlin.coroutines.cancellation.CancellationException

/**
 * このアプリケーションにおける全てのViewModelの基底クラス。
 *
 * 主な責務:
 * - UI状態（[UiState]）の管理と公開
 * - UIイベント（[UiEvent]）および遷移イベント（[NavigationEvent]）の発行
 * - 遷移イベントの保留・リトライ制御
 * - コルーチン内での予期せぬ例外の捕捉と共通ハンドリング
 *
 * @param US 管理するUI状態の型。
 * @param UE 発行するUIイベントの型。
 * @param NE 発行する遷移イベントの型。
 */
abstract class BaseViewModel<
        US: UiState,
        UE: UiEvent,
        NE: NavigationEvent<*, *>
> internal constructor(
    initialViewUiState: US
) : ViewModel(), NavigationEventCallback<NE> {

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

    /** UIの変更などの一度きりのUIイベントを通知するためのSharedFlow。 */
    private val _uiEvent = MutableSharedFlow<ConsumableEvent<UE>>(replay = 1)
    internal val uiEvent get() = _uiEvent.asSharedFlow()

    /** 画面遷移やメッセージ表示などの一度きりのナビゲーションイベントを通知するためのSharedFlow。 */
    private val _navigationEvent = MutableSharedFlow<ConsumableEvent<NE>>(replay = 1)
    internal val navigationEvent get() = _navigationEvent.asSharedFlow()

    /** 画面遷移に失敗し、再試行を待つナビゲーションイベントのリストを保持するStateFlow。 */
    private val _pendingNavigationEventList = MutableStateFlow(emptyList<NE>())

    /** 保留ナビゲーション関連のログメッセージ用プレフィックス。 */
    private val logMsgPendingNavi = "保留ナビゲーション_"
    //endregion

    init {
        _pendingNavigationEventList
            .onEach { list ->
                Log.d(logTag, "${logMsgPendingNavi}リストが更新されました。現在のリスト: $list")
            }
            .launchIn(viewModelScope)
    }

    //region UI Event Handlers
    /**
     * UIが準備完了した時に呼び出される事を想定。
     * サブクラスで追加の処理が必要な場合にオーバーライドして使用する。
     * */
    @CallSuper
    internal open fun onUiReady() {
        // 処理なし
    }

    /**
     * UIが非表示になる時に呼び出される事を想定。
     * サブクラスで追加の処理が必要な場合にオーバーライドして使用する。
     *
     * 保留ナビゲーションイベントをクリアする。
     * */
    @CallSuper
    internal open fun onUiGone() {
        clearPendingNavigationEvents()
    }

    /**
     * UIがナビゲーション可能になった時に呼び出される事を想定。
     *
     * 保留ナビゲーションイベントを発行する。
     * */
    internal fun onNavigationEnabled() {
        emitPendingNavigationEvent()
    }

    /**
     * 画面遷移成功の時に呼び出される事を想定。
     *
     * 該当イベントが保留リスト（Pending List）に存在する場合、処理完了とみなして削除する。
     *
     * @param event 成功した遷移イベント。
     */
    override fun onNavigationEventSuccess(event: NE) {
        if (_pendingNavigationEventList.value.contains(event)) {
            Log.d(
                logTag,
                "${logMsgPendingNavi}成功したナビゲーションを保留リストから削除。イベント: $event"
            )
            updatePendingNavigationEventList { it - event }
        }
    }

    /**
     * 画面遷移失敗の時に呼び出される事を想定。
     *
     * イベントのポリシーに応じて以下の処理を行う:
     * - [NavigationEvent.Policy.Single]: 処理を断念し、保留リストから削除する。
     * - [NavigationEvent.Policy.Retry]: 次回の遷移可能タイミングで再試行するため、保留リストに追加する。
     *
     * @param event 失敗した遷移イベント。
     */
    override fun onNavigationEventFailure(event: NE) {
        Log.d(
            logTag,
            "${logMsgPendingNavi}失敗したナビゲーションを保留リストに追加。イベント: $event"
        )
        when (event.policy) {
            NavigationEvent.Policy.Single -> {
                if (!_pendingNavigationEventList.value.contains(event)) return

                updatePendingNavigationEventList { it - event }
            }
            is NavigationEvent.Policy.Retry -> {
                if (_pendingNavigationEventList.value.contains(event)) return

                updatePendingNavigationEventList { it + event }
            }
        }
    }
    //endregion

    //region UI State Update
    /**
     * UIの状態[UiState]を新しい状態で完全に置き換える。
     * @param state 新しいUI状態。
     */
    protected fun updateUiState(state: US) {
        _uiState.value = state
    }

    /**
     * 現在のUI状態[UiState]を基に、安全に状態を更新する。
     * @param function 現在の状態を受け取り、新しい状態を返す関数。
     */
    protected fun updateUiState(function: (US) -> US) {
        _uiState.update{
            function(it).also { newState ->
                Log.d(logTag, "UI状態更新: $newState")
            }
        }
    }
    //endregion

    //region UI Event Emission
    /**
     * UI変更の一度きりのUIイベント([UiEvent])を発行する。
     * @param event 発行するUIイベント。
     */
    protected suspend fun emitUiEvent(event: UE) {
        _uiEvent.emit(
            ConsumableEvent(event)
        )
    }
    //endregion

    //region Navigation Event
    /**
     * 画面遷移の一度きりのナビゲーションイベント([NavigationEvent])を発行する。
     * @param event 発行するナビゲーションイベント。
     */
    protected suspend fun emitNavigationEvent(event: NE) {
        val isRetryingFirstPendingEvent = _pendingNavigationEventList.value.firstOrNull() == event
        if (isRetryingFirstPendingEvent || _pendingNavigationEventList.value.isEmpty()) {
            _navigationEvent.emit(
                ConsumableEvent(event)
            )
        } else {
            if (event.policy !is NavigationEvent.Policy.Retry) return
            if (_pendingNavigationEventList.value.contains(event)) return

            updatePendingNavigationEventList { it + event }.also {
                Log.d(
                    logTag,
                    "${logMsgPendingNavi}前回ナビゲーションが保留されてるため新規ナビゲーションを保留リストに追加。イベント: $event"
                )
            }
        }
    }

    /**
     * 捕捉された予期せぬ例外をユーザーへ通知する処理を実行する。
     *
     * 実装クラスにて、例外に応じたアプリケーションメッセージダイアログを表示するイベントを発行する。
     *
     * @param e 発生した例外。
     */
    protected abstract suspend fun showUnexpectedAppMessageDialog(e: Exception)

    /**
     * 保留中の遷移イベントが存在する場合、その先頭のイベントを再発行する。
     */
    private fun emitPendingNavigationEvent() {
        val event = _pendingNavigationEventList.value.firstOrNull().also {
            Log.d(
                logTag,
                "${logMsgPendingNavi}要求。イベント: $it"
            )
        } ?: return
        viewModelScope.launch {
            emitNavigationEvent(event)
        }
    }

    /**
     * 保留中のイベントを全て破棄し、リストを空にする。
     */
    private fun clearPendingNavigationEvents() {
        if (_pendingNavigationEventList.value.isEmpty()) return
        updatePendingNavigationEventList { emptyList() }
    }

    /**
     * 保留ナビゲーションイベントのリストを更新する。
     * @param function 現在のリストを受け取り、新しいリストを返す関数
     */
    private fun updatePendingNavigationEventList(
        function: (List<NE>) -> List<NE>
    ) {
        _pendingNavigationEventList.update(function)
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
     * @param onError 例外発生時に追加で実行する処理ブロック。発生した例外が引数として渡される。
     * @param block 実行するメインの処理ブロック。
     * @return 起動したコルーチンの[Job]。
     */
    protected fun launchWithUnexpectedErrorHandler(
        rollbackState: US? = null,
        onError: suspend (e: Exception) -> Unit = { },
        block: suspend CoroutineScope.() -> Unit
    ) : Job {
        return viewModelScope.launch {
            val initialState = rollbackState ?: currentUiState
            try {
                block()
            } catch (e: Exception) {
                handleUnexpectedError(e, initialState, onError)
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
     * @param onError 例外発生時に追加で実行する処理ブロック。発生した例外が引数として渡される。
     * @param block 実行する中断可能な処理ブロック。
     */
    protected suspend fun withUnexpectedErrorHandler(
        rollbackState: US? = null,
        onError: suspend (e: Exception) -> Unit = { },
        block: suspend () -> Unit
    ) {
        val initialState = rollbackState ?: currentUiState
        try {
            block()
        } catch (e: Exception) {
            handleUnexpectedError(e, initialState, onError)
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
            showUnexpectedAppMessageDialog(e)
            block(e)
            emit(fallbackValue)
        }
    }

    /**
     * 予期せぬ例外を処理する共通ハンドラ。
     *
     * [CancellationException]は処理を中断させるために再スローする。
     * それ以外の例外は、UI状態をロールバックした上で、
     * [showUnexpectedAppMessageDialog]を呼び出してユーザーにエラーを通知する。
     *
     * @param e 補足された例外。
     * @param rollbackState ロールバック先のUI状態。
     * @param onError 例外発生時に追加で実行する処理ブロック。発生した例外が引数として渡される。
     */
    private suspend fun handleUnexpectedError(
        e: Exception,
        rollbackState: US,
        onError: suspend (e: Exception) -> Unit = { }
    ) {
        // コルーチンのキャンセルはエラーではないため、再スローして処理を中断させる
        if (e is CancellationException) {
            throw e
        }
        Log.e(logTag, "予期せぬエラーが発生", e)
        updateUiState(rollbackState)
        showUnexpectedAppMessageDialog(e)
        onError(e)
    }
    //endregion
}
