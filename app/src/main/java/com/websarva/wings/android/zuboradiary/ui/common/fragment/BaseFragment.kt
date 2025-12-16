package com.websarva.wings.android.zuboradiary.ui.common.fragment

import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.main.MainActivity
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.FragmentNavigationEventHandler
import com.websarva.wings.android.zuboradiary.ui.common.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavDestination
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.NavigationResult
import com.websarva.wings.android.zuboradiary.ui.common.state.UiState
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.FragmentNavigationEventHelper
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.NavigationEvent
import com.websarva.wings.android.zuboradiary.ui.common.viewmodel.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.ui.main.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * このアプリケーションにおける全てのフラグメントの基底クラス。
 *
 * 以下の共通機能を提供する:
 * - [ViewBinding]のライフサイクル管理
 * - ViewModelとの基本的な連携
 * - Material Designに基づいた画面遷移アニメーションの設定
 * - UI状態([UiState])、UIイベント([UiEvent])、ナビゲーションイベント([NavigationEvent])との監視
 * - [FragmentNavigationEventHelper]を介したナビゲーションイベントの実行と保留・再実行機能の提供
 * - フラグメント、ダイアログからの結果受け取り機能のセットアップ
 * - Navigation Componentを利用した画面遷移のヘルパー
 * - バックプレス処理の共通化
 *
 * @param T ViewBindingの型
 * @param E このフラグメントが処理するUiEventの型
 * @param ND このフラグメントが処理するNavigationEventの前方遷移の型
 * @param NBD このフラグメントが処理するNavigationEventの後方遷移の型
 */
abstract class BaseFragment<
        T: ViewBinding,
        E : UiEvent,
        ND : AppNavDestination,
        NBD : AppNavBackDestination
> : LoggingFragment(), MainUiEventHandler<E>, FragmentNavigationEventHandler<ND, NBD> {

    //region Properties
    /** [ViewBinding]のインスタンス。onDestroyViewでnullに設定される。 */
    private var _binding: T? = null
    /** [ViewBinding]のインスタンスへの非nullアクセスを提供する。 */
    protected val binding get() = checkNotNull(_binding)

    /** このフラグメントに対応するViewModel。 */
    protected abstract val mainViewModel: BaseFragmentViewModel<out UiState, E, ND, NBD>

    /** アプリケーションの[MainActivity]と共有されるViewModel。 */
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    protected val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    /** このフラグメントがナビゲーショングラフの開始地点であるかを示す。 */
    private val isNavigationStartFragment: Boolean
        get() {
            val topLevelGraph = findNavController().graph
            val nestedStartGraphId = topLevelGraph.startDestinationId
            val nestedStartGraph = topLevelGraph.findNode(nestedStartGraphId) as NavGraph
            val startDestinationId = nestedStartGraph.startDestinationId
            return destinationId == startDestinationId
        }

    /** 現在のテーマカラー。 */
    protected val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    /** フラグメントの共通処理をまとめたヘルパークラス。 */
    protected val fragmentHelper = FragmentHelper()

    /** ナビゲーションイベント（[NavigationEvent]）の処理をまとめたヘルパークラス。 */
    protected val navigationEventHelper = FragmentNavigationEventHelper()
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、画面遷移アニメーションの設定と[ViewBinding]の初期化を行う。 */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        setupVisibleFragmentTransitionEffect()

        val themeColorInflater = fragmentHelper.createThemeColorInflater(inflater, themeColor)
        _binding = createViewBinding(themeColorInflater, requireNotNull(container))
        return binding.root
    }

    /**
     * 追加処理として、フラグメント、ダイアログからの結果の監視、UIの監視、ナビゲーションイベントの監視、
     * およびバックプレス処理の登録を行う。
     * また、[MainActivityViewModel]、[BaseFragmentViewModel]へ通知する。
     * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.onUiReady()
        mainActivityViewModel.onFragmentViewReady(this is RequiresBottomNavigation)

        setupFragmentResultObservers()
        setupUiObservers()
        setupNavigationEvent()
        if (!isNavigationStartFragment) registerOnBackPressedCallback()
    }

    /** 追加処理として、[MainActivityViewModel]へ通知する。 */
    override fun onResume() {
        super.onResume()
        mainActivityViewModel.onFragmentViewResumed()
    }

    /** 追加処理として、非表示になる時の画面遷移アニメーションを設定、[MainActivityViewModel]へ通知する。 */
    override fun onPause() {
        mainActivityViewModel.onFragmentViewPause()
        setupInvisibleFragmentTransitionEffect()
        super.onPause()
    }

    /** 追加処理として、[ViewBinding]を解放、[BaseFragmentViewModel]へ通知する。 */
    override fun onDestroyView() {
        clearViewBindings()
        mainViewModel.onUiGone()
        super.onDestroyView()
    }
    //endregion

    //region View Binding Setup
    /**
     * [ViewBinding]インスタンスを生成する。[onCreateView] で呼び出される。
     * @param themeColorInflater テーマカラーが適用されたLayoutInflater
     * @param container 親のViewGroup
     * @return 生成されたViewBindingインスタンス
     */
    protected abstract fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): T

    /** [ViewBinding]のインスタンスを解放する。[onDestroyView] で呼び出される。 */
    @CallSuper
    protected open fun clearViewBindings() {
        _binding = null
    }
    //endregion

    //region Fragment Transition Effect Setup
    /** 表示される際のFragment遷移アニメーションを設定する。 */
    private fun setupVisibleFragmentTransitionEffect() {
        // FROM:遷移元 TO:遷移先
        // FROM - TO の TO として現れるアニメーション

        // HACK:ボトムナビゲーションタブでFragment切替時はEnterTransitionで設定されるエフェクトを変更する。
        //      NavigationStartFragment(DiaryListFragment)はReenterTransitionで設定されたエフェクトが処理される。
        //      遷移元FragmentのエフェクトはMainActivityクラスにて設定。
        val enterTransitionType: Transition =
            if (mainActivityViewModel.wasSelectedTab.value) {
                MaterialFadeThrough().apply {
                    addListener(
                        createTransitionLogListener("Enter_MaterialFadeThrough")
                    )
                }
            } else {
                MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
                    addListener(
                        createTransitionLogListener("Enter_MaterialSharedAxis.X")
                    )
                }
            }
        enterTransition = enterTransitionType

        // FROM - TO の FROM として消えるアニメーション
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            addListener(
                createTransitionLogListener("Exit_MaterialSharedAxis.X")
            )
        }

        // TO - FROM の FROM として現れるアニメーション
        val reenterTransitionType: Transition =
            if (mainActivityViewModel.wasSelectedTab.value) {
                MaterialFadeThrough().apply {
                    addListener(
                        createTransitionLogListener("Reenter_MaterialFadeThrough")
                    )
                }
            } else {
                MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
                    addListener(
                        createTransitionLogListener("Reenter_MaterialSharedAxis.X")
                    )
                }
            }
        reenterTransition = reenterTransitionType

        // TO - FROM の TO として消えるアニメーション
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            addListener(
                createTransitionLogListener("Return_MaterialSharedAxis.X")
            )
        }

        mainActivityViewModel.onVisibleFragmentTransitionSetupCompleted()
    }

    /** 非表示になる際のFragment遷移アニメーションを設定する。 */
    private fun setupInvisibleFragmentTransitionEffect() {
        if (mainActivityViewModel.wasSelectedTab.value) {
            exitTransition =
                MaterialFadeThrough().apply {
                    addListener(
                        createTransitionLogListener("Exit_MaterialFadeThrough")
                    )
                }
            returnTransition =
                MaterialFadeThrough().apply {
                    addListener(
                        createTransitionLogListener("Return_MaterialFadeThrough")
                    )
                }
        }
        mainActivityViewModel.onInvisibleFragmentTransitionSetupCompleted()
    }

    /** 遷移アニメーションの各状態をログ出力するためのリスナーを生成する。 */
    private fun createTransitionLogListener(transitionName: String): Transition.TransitionListener {
        return object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
                Log.d(logTag, "${this@BaseFragment.javaClass.simpleName}: $transitionName - START")
            }

            override fun onTransitionEnd(transition: Transition) {
                Log.d(logTag, "${this@BaseFragment.javaClass.simpleName}: $transitionName - END")
                transition.removeListener(this)
            }

            override fun onTransitionCancel(transition: Transition) {
                Log.d(logTag, "${this@BaseFragment.javaClass.simpleName}: $transitionName - CANCEL")
                transition.removeListener(this)
            }

            override fun onTransitionPause(transition: Transition) {
                // 必要に応じて実装
            }

            override fun onTransitionResume(transition: Transition) {
                // 必要に応じて実装
            }
        }
    }
    //endregion

    //region Fragment Result Observation Setup
    /**
     *  Fragment Result APIの監視を設定する。
     *  具象クラスで [observeFragmentResult] や [observeDialogResult] を呼び出す。
     *  [onViewCreated] で呼び出される。
     */
    protected abstract fun setupFragmentResultObservers()

    /**
     * 他のフラグメントからの結果を監視する。
     * @param key 結果を識別するための一意なキー
     * @param block 結果を受け取った際の処理
     */
    protected fun <T> observeFragmentResult(key: String, block: (FragmentResult<T>) -> Unit) {
        executeFragmentResultObservation(key, block)
    }

    /**
     * ダイアログフラグメントからの結果を監視する。
     * @param key 結果を識別するための一意なキー
     * @param block 結果を受け取った際の処理
     */
    protected fun <T> observeDialogResult(key: String, block: (DialogResult<T>) -> Unit) {
        executeFragmentResultObservation(key, block)
    }

    /** Fragment Result APIの監視を実行する。 */
    private fun <R : NavigationResult> executeFragmentResultObservation(
        key: String,
        block: (R) -> Unit
    ) {
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
    private fun setupNavigationEvent() {
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
                        this@BaseFragment,
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
    protected fun launchAndRepeatOnViewLifeCycleStarted(
        block: suspend CoroutineScope.() -> Unit
    ) {
        fragmentHelper.launchAndRepeatOnViewLifeCycleStarted(this, block)
    }
    //endregion
}
