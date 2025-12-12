package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ThemeColorSettingLoadException
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.event.MainActivityUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.message.MainActivityAppMessage
import com.websarva.wings.android.zuboradiary.ui.navigation.event.NavigationEvent
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.MainActivityNavDestination
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.MainActivityUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * `MainActivity`のUIロジックと状態([MainActivityUiState])管理を担うViewModel。
 *
 * 以下の責務を持つ:
 * - アプリケーション全体のテーマカラー設定の読み込みと管理
 * - BottomNavigationViewの表示/非表示、および有効/無効の状態管理
 * - Fragment間の画面遷移アニメーションの制御
 * - FragmentからのUIイベント（表示準備完了、処理中状態の変更など）のハンドリング
 * - Fragmentへのコールバックイベント（BottomNavigationViewの再選択など）の発行
 */
@HiltViewModel
class MainActivityViewModel @Inject internal constructor(
    private val handle: SavedStateHandle,
    private val loadThemeColorSettingUseCase: LoadThemeColorSettingUseCase,
) : BaseViewModel<
        MainActivityUiState,
        MainActivityUiEvent,
        NavigationEvent.To<MainActivityNavDestination>
>(
    handle.get<MainActivityUiState>(SAVED_STATE_UI_KEY)?.let {
        MainActivityUiState.fromSavedState(it)
    } ?: MainActivityUiState()
) {

    //region Properties
    /** BottomNavigationViewのタブが選択されたかどうかを示すStateFlow。 */
    private val _wasBottomNavigationTabSelected = MutableStateFlow(false)
    val wasSelectedTab get() = _wasBottomNavigationTabSelected.asStateFlow()

    /** 表示されるFragmentの遷移アニメーション設定が完了したかを示すStateFlow。 */
    private val _wasVisibleFragmentTransitionSetupCompleted = MutableStateFlow(false)

    /** 非表示になるFragmentの遷移アニメーション設定が完了したかを示すStateFlow。 */
    private val _wasInvisibleFragmentTransitionSetupCompleted = MutableStateFlow(false)
    
    /** Fragmentへのコールバックイベントを通知するためのSharedFlow。 */
    private val _activityCallbackUiEvent =
        MutableSharedFlow<ConsumableEvent<ActivityCallbackUiEvent>>(replay = 1)
    val activityCallbackUiEvent get() = _activityCallbackUiEvent.asSharedFlow()
    //endregion

    //region Initialization
    init {
        collectUiStates()
    }

    /** UI状態の監視を開始する。 */
    private fun collectUiStates() {
        collectUiState()
        collectThemeColorSetting()
        collectFragmentTransitionSetupCompleted()
        collectBottomNavigationEnabled()
    }

    /** UI状態を[SavedStateHandle]に保存する。 */
    private fun collectUiState() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    /** テーマカラー設定の変更を監視し、UIに反映させる。 */
    private fun collectThemeColorSetting() {
        loadThemeColorSettingUseCase()
            .onEach {
                when (it) {
                    is UseCaseResult.Success -> { /*処理なし*/ }
                    is UseCaseResult.Failure -> {
                        when (it.exception) {
                            is ThemeColorSettingLoadException.LoadFailure -> {
                                showAppMessageDialog(
                                    MainActivityAppMessage.SettingsLoadFailure
                                )
                            }
                            is ThemeColorSettingLoadException.Unknown -> {
                                showUnexpectedAppMessageDialog(it.exception)
                            }
                        }
                    }
                }
            }.map {
                when (it) {
                    is UseCaseResult.Success -> it.value
                    is UseCaseResult.Failure -> it.exception.fallbackSetting
                }
            }.catchUnexpectedError(
                ThemeColorSetting.default()
            ).distinctUntilChanged().onEach {
                updateThemeColor(it.themeColor.toUiModel())
            }.launchIn(viewModelScope)
    }

    /** Fragmentの遷移アニメーション設定完了状態を監視し、関連フラグをリセットする。 */
    private fun collectFragmentTransitionSetupCompleted() {
        combine(
            _wasVisibleFragmentTransitionSetupCompleted,
            _wasInvisibleFragmentTransitionSetupCompleted
        ) { wasVisibleFragmentCompleted, wasInvisibleFragmentCompleted ->
            wasVisibleFragmentCompleted && wasInvisibleFragmentCompleted
        }.onEach {
            if (it) {
                updateWasBottomNavigationTabSelected(false)
                updateWasVisibleFragmentTransitionSetupCompleted(false)
                updateWasInvisibleFragmentTransitionSetupCompleted(false)
            }
        }.launchIn(viewModelScope)
    }

    /** BottomNavigationViewの有効/無効状態を決定するUI状態を監視し、UIに反映させる。 */
    private fun collectBottomNavigationEnabled() {
        uiState.distinctUntilChanged{ old, new ->
            old.isInputDisabled == new.isInputDisabled && old.isNavigating == new.isNavigating
        }.map {
            !it.isInputDisabled && !it.isNavigating
        }.distinctUntilChanged().onEach { isEnabled ->
            updateIsBottomNavigationEnabled(isEnabled)
        }.launchIn(viewModelScope)
    }
    //endregion

    //region Activity UI Event Handlers
    /**
     * BottomNavigationViewのアイテムが選択された時に呼び出される事を想定。
     * タブ選択フラグを立てる。
     */
    internal fun onBottomNavigationItemSelect() {
        updateWasBottomNavigationTabSelected(true)
    }

    /**
     * BottomNavigationViewの同じアイテムが再選択された時に呼び出される事を想定。
     * Fragmentにコールバックイベントを発行する。
     */
    internal fun onBottomNavigationItemReselect() {
        viewModelScope.launch {
            emitActivityCallbackUiEvent(ActivityCallbackUiEvent.ProcessOnBottomNavigationItemReselect)
        }
    }
    //endregion

    //region Fragment UI Event Handlers
    /**
     * FragmentのView準備が完了した時に呼び出される事を想定。
     * BottomNavigationViewの表示/非表示を決定する。
     * @param needsBottomNavigation BottomNavigationViewが必要な画面の場合はtrue
     */
    internal fun onFragmentViewReady(needsBottomNavigation: Boolean) {
        if (needsBottomNavigation) {
            updateToBottomNavigationVisibleState()
        } else {
            updateToBottomNavigationInvisibleState()
        }
    }

    /**
     * Fragmentが再開した時に呼び出される事を想定。
     * ナビゲーション中フラグを解除する。
     */
    internal fun onFragmentViewResumed() {
        updateIsNavigating(false)
    }

    /**
     * Fragmentが一時停止した時に呼び出される事を想定。
     * タブ選択による画面遷移の場合、ナビゲーション中フラグを立てる。
     */
    internal fun onFragmentViewPause() {
        if (!_wasBottomNavigationTabSelected.value) return
        updateIsNavigating(true)
    }

    /**
     * Fragmentの処理中状態が変更された時に呼び出される事を想定。
     * UIの状態を更新する。
     * @param isProcessing Fragmentが処理中の場合はtrue
     */
    internal fun onFragmentProcessingStateChanged(isProcessing: Boolean) {
        if (isProcessing) {
            updateToProcessingState()
        } else {
            updateToIdleState()
        }
    }

    /**
     * 表示されるFragmentの遷移アニメーション設定が完了した時に呼び出される事を想定。
     * 完了フラグを立てる。
     */
    internal fun onVisibleFragmentTransitionSetupCompleted() {
        markVisibleFragmentTransitionSetupCompleted()
    }

    /**
     * 非表示になるFragmentの遷移アニメーション設定が完了した時に呼び出される事を想定。
     * 完了フラグを立てる。
     */
    internal fun onInvisibleFragmentTransitionSetupCompleted() {
        markInvisibleFragmentTransitionSetupCompleted()
    }
    //endregion

    //region Business Logic
    /** 表示されるFragmentの遷移アニメーション設定完了をマークする。タブ選択時のみ処理する。 */
    private fun markVisibleFragmentTransitionSetupCompleted() {
        if (!_wasBottomNavigationTabSelected.value) return
        updateWasVisibleFragmentTransitionSetupCompleted(true)
    }

    /** 非表示になるFragmentの遷移アニメーション設定完了をマークする。タブ選択時のみ処理する。 */
    private fun markInvisibleFragmentTransitionSetupCompleted() {
        if (!_wasBottomNavigationTabSelected.value) return
        updateWasInvisibleFragmentTransitionSetupCompleted(true)
    }
    //endregion

    //region UI State Update
    /**
     * テーマカラーを更新する。
     * @param themeColor 新しいテーマカラー
     */
    private fun updateThemeColor(themeColor: ThemeColorUi) {
        updateUiState { it.copy(themeColor = themeColor) }
    }

    /**
     * BottomNavigationViewの有効/無効状態を更新する。
     * @param isEnable 有効にする場合はtrue
     */
    private fun updateIsBottomNavigationEnabled(isEnable: Boolean) {
        updateUiState { it.copy(isBottomNavigationEnabled = isEnable) }
    }

    /**
     * ナビゲーション中の状態を更新する。
     * @param isNavigating ナビゲーション中の場合はtrue
     */
    private fun updateIsNavigating(isNavigating: Boolean) {
        updateUiState { it.copy(isNavigating = isNavigating) }
    }

    /** UIをアイドル状態（操作可能）に更新する。 */
    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    /** UIを処理中の状態（操作不可）に更新する。 */
    private fun updateToProcessingState() {
        updateUiState {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    /** UIをBottomNavigationViewが表示される状態に更新する。 */
    private fun updateToBottomNavigationVisibleState() {
        updateUiState {
            it.copy(
                isBottomNavigationVisible = true,
                isInputDisabled = false
            )
        }
    }

    /** UIをBottomNavigationViewが非表示の状態に更新する。 */
    private fun updateToBottomNavigationInvisibleState() {
        updateUiState {
            it.copy(
                isBottomNavigationVisible = false,
                isInputDisabled = true
            )
        }
    }
    //endregion

    //region Internal State Update
    /**
     * BottomNavigationViewのタブが選択されたかどうかのフラグを更新する。
     * @param wasSelected 選択された場合はtrue
     */
    private fun updateWasBottomNavigationTabSelected(wasSelected: Boolean) {
        _wasBottomNavigationTabSelected.update { wasSelected }
    }

    /**
     * 表示されるFragmentの遷移アニメーション設定が完了したかどうかのフラグを更新する。
     * @param wasCompleted 完了した場合はtrue
     */
    private fun updateWasVisibleFragmentTransitionSetupCompleted(wasCompleted: Boolean) {
        _wasVisibleFragmentTransitionSetupCompleted.update { wasCompleted }
    }

    /**
     * 非表示になるFragmentの遷移アニメーション設定が完了したかどうかのフラグを更新する。
     * @param wasCompleted 完了した場合はtrue
     */
    private fun updateWasInvisibleFragmentTransitionSetupCompleted(wasCompleted: Boolean) {
        _wasInvisibleFragmentTransitionSetupCompleted.update { wasCompleted }
    }
    //endregion

    //region UI Event Emission
    override suspend fun showUnexpectedAppMessageDialog(e: Exception) {
        showAppMessageDialog(MainActivityAppMessage.Unexpected(e))
    }

    /**
     * アプリケーションメッセージダイアログを表示する（イベント発行）。
     * @param appMessage 表示するメッセージ。
     */
    private suspend fun showAppMessageDialog(appMessage: MainActivityAppMessage) {
        emitNavigationEvent(
            NavigationEvent.To(
                MainActivityNavDestination.AppMessageDialog(appMessage),
                NavigationEvent.Policy.Retry
            )
        )
    }

    /**
     * Fragmentへのコールバックイベントを発行する。
     * @param event 発行するコールバックイベント
     */
    private suspend fun emitActivityCallbackUiEvent(event: ActivityCallbackUiEvent) {
        _activityCallbackUiEvent.emit(
            ConsumableEvent(event)
        )
    }
    //endregion

    private companion object {
        /** SavedStateHandleにUI状態を保存するためのキー。 */
        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
