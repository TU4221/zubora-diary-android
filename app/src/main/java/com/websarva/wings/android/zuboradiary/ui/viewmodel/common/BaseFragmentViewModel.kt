package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import android.util.Log
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.navigation.PendingNavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.message.CommonAppMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Fragmentと連携するViewModelの基底クラス。
 *
 * 以下の責務を持つ:
 * - [BaseViewModel]の機能の継承
 * - 画面遷移に失敗した場合の再試行ロジックを管理するための、保留中ナビゲーションコマンドのリスト管理
 * - 複数の画面で共通のUIイベント([CommonUiEvent])の発行
 * - バックプレス処理のハンドリング
 *
 * @param S このViewModelが管理するUI状態の型。
 * @param E このViewModelが発行するUIイベントの型。
 * @param M このViewModelが発行する固有のアプリケーションメッセージの型。
 */
abstract class BaseFragmentViewModel<S: UiState, E: UiEvent, M: AppMessage> internal constructor(
    initialViewUiState: S
) : BaseViewModel<S, E, M>(initialViewUiState) {

    //region Properties
    /** 画面遷移に失敗し、再試行を待つナビゲーションコマンドのリストを保持するStateFlow。 */
    private val _pendingNavigationCommandList =
        MutableStateFlow(emptyList<PendingNavigationCommand>())
    internal val pendingNavigationCommandList
        get() = _pendingNavigationCommandList.asStateFlow()

    /** 保留ナビゲーション関連のログメッセージ用プレフィックス。 */
    private val logMsgPendingNavi = "保留ナビゲーション_"
    
    /** 画面遷移や共通メッセージ表示など、複数の画面で共有されるUIイベントを通知するためのSharedFlow。 */
    private val _commonUiEvent = MutableSharedFlow<ConsumableEvent<CommonUiEvent>>(replay = 1)
    internal val commonUiEvent get() = _commonUiEvent.asSharedFlow()
    //endregion

    //region UI Event Handlers
    //TODO:クラス内で参照されていない為、記述場所検討
    /**
     * バックボタンが押下された時に、`Fragment`から呼び出される事を想定。
     * バックプレスイベントを処理する。
     */
    abstract fun onBackPressed()
    //endregion

    //region Navigation Event Handlers
    /**
     * 画面遷移が失敗した時に、`Fragment`から呼び出される事を想定。
     * 失敗した画面遷移コマンドを保留リストに追加する。`Fragment`から呼び出される事を想定。
     * @param command 失敗したナビゲーションコマンド
     */
    internal fun onFragmentNavigationFailure(command: NavigationCommand) {
        val newPendingCommand = PendingNavigationCommand(command)
        Log.d(
            logTag,
            "${logMsgPendingNavi}失敗したナビゲーションを保留リストに追加。コマンド: $newPendingCommand"
        )
        updatePendingNavigationCommandList { it + newPendingCommand }
    }

    /**
     * 保留ナビゲーションコマンドでの画面遷移が成功した時に、`Fragment`から呼び出される事を想定。
     * 完了した保留ナビゲーションコマンドをリストから削除する。
     * @param command 完了した保留ナビゲーションコマンド
     */
    internal fun onPendingFragmentNavigationComplete(command: PendingNavigationCommand) {
        Log.d(
            logTag,
            "${logMsgPendingNavi}保留中のナビゲーションが完了。リストから削除。コマンド: $command"
        )
        updatePendingNavigationCommandList { it - command }
    }

    /**
     * 保留ナビゲーションコマンドでの画面遷移が失敗した時に、`Fragment`から呼び出される事を想定。
     * 失敗した保留ナビゲーションコマンドのリトライ回数を更新する。
     * @param command 失敗した保留ナビゲーションコマンド
     */
    internal fun onPendingFragmentNavigationFailure(command: PendingNavigationCommand) {
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

    /**
     * 保留ナビゲーションコマンドでの画面遷移が失敗、リトライ回数が上限に達した時に、
     * `Fragment`から呼び出される事を想定。
     * リトライ回数上限に達した保留ナビゲーションコマンドをリストから削除する。
     * @param command リトライ回数上限に達した保留ナビゲーションコマンド
     */
    internal fun onPendingFragmentNavigationRetryLimitReached(command: PendingNavigationCommand) {
        Log.e(
            logTag,
            "${logMsgPendingNavi}保留中のナビゲーションがリトライ回数に到達。コマンド: $command"
        )
        updatePendingNavigationCommandList { it - command }
    }
    //endregion

    //region Internal State Update
    /**
     * 保留ナビゲーションコマンドのリストを更新する。
     * @param function 現在のリストを受け取り、新しいリストを返す関数
     */
    private fun updatePendingNavigationCommandList(
        function: (List<PendingNavigationCommand>) -> List<PendingNavigationCommand>
    ) {
        val beforeList = _pendingNavigationCommandList.value
        Log.d(logTag, "${logMsgPendingNavi}更新前のリスト: $beforeList")

        _pendingNavigationCommandList.update(function)

        val afterList = _pendingNavigationCommandList.value
        Log.d(logTag, "${logMsgPendingNavi}更新後のリスト: $afterList")
    }
    //endregion

    //region UI Event Emission
    override suspend fun emitAppMessageEvent(appMessage: M) {
        emitCommonUiEvent(CommonUiEvent.NavigateAppMessage(appMessage))
    }

    override suspend fun emitCommonAppMessageEvent(appMessage: CommonAppMessage) {
        emitCommonUiEvent(CommonUiEvent.NavigateAppMessage(appMessage))
    }

    override suspend fun emitUnexpectedAppMessage(e: Exception) {
        emitCommonAppMessageEvent(CommonAppMessage.Unexpected(e))
    }

    /**
     * 前の画面へ戻るための共通UIイベント([CommonUiEvent.NavigatePreviousFragment])を発行する。
     */
    protected suspend fun emitNavigatePreviousFragmentEvent() {
        emitCommonUiEvent(CommonUiEvent.NavigatePreviousFragment)
    }

    /**
     * 共通UIイベント([CommonUiEvent])をラップして発行する。
     * @param event 発行する共通UIイベント
     */
    private suspend fun emitCommonUiEvent(event: CommonUiEvent) {
        _commonUiEvent.emit(
            ConsumableEvent(event)
        )
    }
    //endregion
}
