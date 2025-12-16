package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.fullscreen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.main.MainActivity
import com.websarva.wings.android.zuboradiary.ui.common.fragment.MainUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.common.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.FragmentNavigationEventHandler
import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavDestination
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.common.state.UiState
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.FragmentNavigationEventHelper
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.NavigationEvent
import com.websarva.wings.android.zuboradiary.ui.main.MainActivityViewModel
import com.websarva.wings.android.zuboradiary.ui.common.viewmodel.BaseFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * ViewModelと連携する機能を持つ全画面ダイアログの基底クラス。
 *
 * 以下の責務を持つ:
 * - [BaseSimpleFullScreenDialogFragment]の機能の継承
 * - ViewModelとの基本的な連携
 * - UI状態([UiState])、UIイベント([UiEvent])、ナビゲーションイベント([NavigationEvent])との監視
 * - [FragmentNavigationEventHelper]を介したナビゲーションイベントの実行と保留・再実行機能の提供
 * - フラグメント、ダイアログからの結果受け取り機能のセットアップ
 * - バックプレス処理の移譲
 *
 * @param T ViewBindingの型
 * @param E このフラグメントが処理するUiEventの型
 * @param ND このフラグメントが発行する前方遷移の型
 * @param NBD このフラグメントが発行する後方遷移の型
 */
abstract class BaseFullScreenDialogFragment<
        T: ViewBinding,
        E: UiEvent,
        ND : AppNavDestination,
        NBD : AppNavBackDestination
> : BaseSimpleFullScreenDialogFragment<T>(), MainUiEventHandler<E>, FragmentNavigationEventHandler<ND, NBD> {

    //region Properties
    /** このフラグメントに対応するViewModel。 */
    protected abstract val mainViewModel: BaseFragmentViewModel<out UiState, E, ND, NBD>

    /** アプリケーションの[MainActivity]と共有されるViewModel。 */
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    protected val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    /** ナビゲーションイベント（[NavigationEvent]）の処理をまとめたヘルパークラス。 */
    protected val navigationEventHelper = FragmentNavigationEventHelper()
    //endregion

    //region Fragment Lifecycle
    /**
     * 追加処理として、フラグメント、ダイアログからの結果の監視、UIの監視、、ナビゲーションイベントの監視、
     * およびバックプレス処理の登録を行う。
     * また、[BaseFragmentViewModel]へ通知する。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.onUiReady()

        setupFragmentResultObservers()
        setupUiObservers()
        setupNavigationEvent()
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

    /** ViewModelのUIイベント(Event)の監視を設定する。 */
    @CallSuper
    protected open fun setupUiEventObservers() {
        observeMainUiEvent()
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
    //endregion

    //region Navigation Event Setup
    /** ナビゲーションイベント関連の設定を行う。 */
    fun setupNavigationEvent() {
        observeNavigationEvent()
        setupNavigationEnabledNotifier()
    }

    /**
     * このフラグメント固有のViewModelが発行するナビゲーションイベント([NavigationEvent])を監視する。
     * 新規イベントを受け取ると、[FragmentNavigationEventHelper]にナビゲーション処理の実行を委譲する。
     */
    private fun observeNavigationEvent() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.navigationEvent
                .collect { value: ConsumableEvent<NavigationEvent<ND, NBD>> ->
                    val event = value.getContentIfNotHandled().also {
                        Log.d(logTag, "NavigationEvent_Collect(): $it")
                    } ?: return@collect

                    navigationEventHelper.executeFragmentNavigation(
                        viewLifecycleOwner.lifecycle,
                        findNavController(),
                        event,
                        this@BaseFullScreenDialogFragment,
                        mainViewModel
                    )
                }
        }
    }

    /**
     * ナビゲーション操作が可能になったことを通知する仕組みを設定する。
     *
     * このフラグメント（[NavBackStackEntry]）が [Lifecycle.State.RESUMED] 状態になるたび、
     * ViewModelへ「ナビゲーション有効」であることを通知する。
     */
    fun setupNavigationEnabledNotifier() {
        val backStackEntry = findNavController().getBackStackEntry(destinationId)
        backStackEntry.lifecycleScope.launch {
            backStackEntry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainActivityViewModel.onNavigationEnabled()
                mainViewModel.onNavigationEnabled()
            }
        }
    }
    //endregion

    //region Back Press Setup
    /** バックプレス時のコールバックを登録する。 */
    private fun registerOnBackPressedCallback() {
        fragmentHelper.registerOnBackPressedCallback(this, mainViewModel)
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
