package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import com.websarva.wings.android.zuboradiary.ui.fragment.common.CommonUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.fragment.common.MainUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.MainActivityViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.getValue

/**
 * ViewModelと連携する機能を持つ全画面ダイアログの基底クラス。
 *
 * 以下の責務を持つ:
 * - [BaseSimpleFullScreenDialogFragment]の機能の継承
 * - ViewModelとの基本的な連携
 * - UIイベントとナビゲーションコマンドの監視と処理
 * - Fragment Result APIの監視セットアップ
 * - バックプレス処理の移譲
 *
 * @param T ViewBindingの型
 * @param E このフラグメントが処理するUiEventの型
 */
abstract class BaseFullScreenDialogFragment<T: ViewBinding, E: UiEvent>
    : BaseSimpleFullScreenDialogFragment<T>(), MainUiEventHandler<E>, CommonUiEventHandler {

    //region Properties
    /** このフラグメントに対応するViewModel。 */
    protected abstract val mainViewModel: BaseFragmentViewModel<out UiState, E, out AppMessage>

    /** アプリケーションの[MainActivity]と共有されるViewModel。 */
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    protected val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    /** Navigation Componentにおけるこのフラグメントのdestination ID。 */
    protected abstract val destinationId: Int
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、Fragment ResultとUIイベントの監視、およびバックプレス処理の登録を行う。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFragmentResultObservers()
        setupUiObservers()
        registerOnBackPressedCallback()
    }
    //endregion

    //region Fragment Result Observation Setup
    /**
     * ダイアログからの結果を監視する設定を行う。具象クラスで[observeDialogResult]を呼び出す。
     * [onViewCreated] で呼び出される。
     * */
    protected abstract fun setupFragmentResultObservers()

    /**
     * ダイアログからの結果を監視する。
     * @param key 結果を識別するための一意なキー
     * @param block 結果を受け取った際の処理
     */
    protected fun <T> observeDialogResult(key: String, block: (DialogResult<T>) -> Unit) {
        fragmentHelper
            .observeFragmentResult(
                findNavController(),
                destinationId,
                key,
                block
            )
    }
    //endregion

    //region UI Observation Setup
    /** UI関連の全ての監視を開始する。 */
    private fun setupUiObservers() {
        setupUiStateObservers()
        setupUiEventObservers()
        observePendingNavigation()
    }

    /** ViewModelのUI状態(State)の監視を設定する。 */
    @CallSuper
    protected open fun setupUiStateObservers() {
        observeProcessingState()
    }

    /** 処理中状態([UiState.isProcessing])を監視し、[MainActivity]に通知する。 */
    private fun observeProcessingState() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.isProcessing == new.isProcessing
            }.map { it.isProcessing }.collect {
                mainActivityViewModel.onFragmentProcessingStateChanged(it)
            }
        }
    }

    /** UI関連の全てのイベント監視を開始する。 */
    private fun setupUiEventObservers() {
        observeMainUiEvent()
        observeCommonUiEvent()
    }

    /** このフラグメント固有のUIイベントを監視する。 */
    private fun observeMainUiEvent() {
        fragmentHelper
            .observeMainUiEvent(
                this,
                mainViewModel,
                this
            )
    }

    /** 全てのフラグメントで共通のUIイベントを監視する。 */
    private fun observeCommonUiEvent() {
        fragmentHelper
            .observeCommonUiEvent(
                this,
                mainViewModel,
                this
            )
    }

    /** 保留中の画面遷移コマンドを監視し、実行する。 */
    private fun observePendingNavigation() {
        fragmentHelper
            .observePendingNavigation(
                findNavController(),
                destinationId,
                mainViewModel
            )
    }
    //endregion

    //region Back Press Setup
    /** バックプレス時のコールバックを登録する。 */
    private fun registerOnBackPressedCallback() {
        fragmentHelper.registerOnBackPressedCallback(this, mainViewModel)
    }
    //endregion

    //region Navigation Helpers
    /**
     * 画面遷移を一度だけ実行する。
     * @param command 画面遷移情報を持つコマンド
     */
    protected fun navigateFragmentOnce(command: NavigationCommand) {
        fragmentHelper
            .navigateFragmentOnce(
                findNavController(),
                destinationId,
                command
            )
    }

    /**
     * 画面遷移を試み、失敗した場合は再試行する。
     * @param command 画面遷移情報を持つコマンド
     */
    protected fun navigateFragmentWithRetry(command: NavigationCommand) {
        fragmentHelper
            .navigateFragmentWithRetry(
                findNavController(),
                destinationId,
                mainViewModel,
                command
            )
    }

    /**
     * 前の画面に戻る。
     * @param resultKey 遷移元に渡す結果のキー
     * @param result 渡す結果データ
     */
    protected fun navigatePreviousFragment(
        resultKey: String? = null,
        result: FragmentResult<*> = FragmentResult.None
    ) {
        fragmentHelper
            .navigatePreviousFragmentOnce(
                findNavController(),
                destinationId,
                resultKey,
                result
            )
    }
    //endregion

    //region Internal Helpers
    /**
     * FragmentのViewライフサイクル（STARTEDからSTOPPED）に合わせてコルーチンを起動する。
     * @param block 実行する中断可能な処理ブロック
     */
    internal fun launchAndRepeatOnViewLifeCycleStarted(
        block: suspend CoroutineScope.() -> Unit
    ) {
        fragmentHelper.launchAndRepeatOnViewLifeCycleStarted(this, block)
    }
    //endregion
}
