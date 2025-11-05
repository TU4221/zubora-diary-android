package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import android.util.Log
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal abstract class BaseFragmentViewModel<S: UiState, E: UiEvent, M: AppMessage>(
    initialViewUiState: S
) : BaseViewModel<S, E, M>(initialViewUiState) {

    //region Properties
    // 表示保留中Navigation
    private val _pendingNavigationCommandList =
        MutableStateFlow(emptyList<PendingNavigationCommand>())
    val pendingNavigationCommandList
        get() = _pendingNavigationCommandList.asStateFlow()

    private val logMsgPendingNavi = "保留ナビゲーション_"
    
    private val _commonUiEvent = MutableSharedFlow<ConsumableEvent<CommonUiEvent>>(replay = 1)
    val commonUiEvent get() = _commonUiEvent.asSharedFlow()
    //endregion

    //region UI Event Handlers
    abstract fun onBackPressed()
    //endregion

    //region Navigation Event Handlers
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
    //endregion

    //region Internal State Update
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

    protected suspend fun emitNavigatePreviousFragmentEvent(
        result: FragmentResult<*> = FragmentResult.None
    ) {
        emitCommonUiEvent(CommonUiEvent.NavigatePreviousFragment(result))
    }

    private suspend fun emitCommonUiEvent(event: CommonUiEvent) {
        _commonUiEvent.emit(
            ConsumableEvent(event)
        )
    }
    //endregion
}
