package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import com.websarva.wings.android.zuboradiary.ui.fragment.common.CommonUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.fragment.common.MainUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.fragment.common.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.result.NavigationResult
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

abstract class BaseFragment<T: ViewBinding, E : UiEvent>
    : LoggingFragment(), MainUiEventHandler<E>, CommonUiEventHandler {

    //region Properties
    // View関係
    private var _binding: T? = null
    internal val binding get() = checkNotNull(_binding)

    internal abstract val mainViewModel: BaseFragmentViewModel<out UiState, E, out AppMessage>

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    internal val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    internal abstract val destinationId: Int
    private val isNavigationStartFragment: Boolean
        get() {
            val topLevelGraph = findNavController().graph
            val nestedStartGraphId = topLevelGraph.startDestinationId
            val nestedStartGraph = topLevelGraph.findNode(nestedStartGraphId) as NavGraph
            val startDestinationId = nestedStartGraph.startDestinationId
            return destinationId == startDestinationId
        }

    internal val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    protected val fragmentHelper = FragmentHelper()
    //endregion

    //region Fragment Lifecycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        setUpVisibleFragmentTransitionEffect()

        val themeColorInflater = fragmentHelper.createThemeColorInflater(inflater, themeColor)
        _binding = createViewBinding(themeColorInflater, requireNotNull(container))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivityViewModel.onFragmentViewReady(this is RequiresBottomNavigation)

        setUpFragmentResultReceivers()
        setUpUiObservers()
        if (!isNavigationStartFragment) registerOnBackPressedCallback()
    }

    override fun onResume() {
        super.onResume()
        mainActivityViewModel.onFragmentViewResumed()
    }

    override fun onPause() {
        mainActivityViewModel.onFragmentViewPause()
        setUpInvisibleFragmentTransitionEffect()
        super.onPause()
    }

    override fun onDestroyView() {
        clearViewBindings()

        super.onDestroyView()
    }
    //endregion

    //region View Binding Setup
    /**
     * BaseFragment#onCreateView()で呼び出される。
     */
    internal abstract fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): T

    internal open fun clearViewBindings() {
        _binding = null
    }
    //endregion

    //region Fragment Transition Effect Setup
    private fun setUpVisibleFragmentTransitionEffect() {
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

    private fun setUpInvisibleFragmentTransitionEffect() {
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

    //region Fragment Result Receiver Setup
    /**
     *  setUpFragmentResultReceiver()、setUpDialogResultReceiver()を使用してフラグメント、ダイアログからの結果の処理内容を設定する
     * */
    internal abstract fun setUpFragmentResultReceivers()

    internal fun <T> setUpFragmentResultReceiver(key: String, block: (FragmentResult<T>) -> Unit) {
        setUpFragmentResultReceiverInternal(key, block)
    }

    internal fun <T> setUpDialogResultReceiver(key: String, block: (DialogResult<T>) -> Unit) {
        setUpFragmentResultReceiverInternal(key, block)
    }

    private fun <R : NavigationResult> setUpFragmentResultReceiverInternal(
        key: String,
        block: (R) -> Unit
    ) {
        fragmentHelper
            .setUpFragmentResultReceiverInternal(
                findNavController(),
                destinationId,
                key,
                block
            )
    }
    //endregion

    //region UI Observation Setup
    private fun setUpUiObservers() {
        setUpUiStateObservers()
        setUpUiEventObservers()
        observePendingNavigation()
    }

    internal open fun setUpUiStateObservers() {
        observeProcessingState()
    }

    private fun observeProcessingState() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.isProcessing == new.isProcessing
            }.map { it.isProcessing }.collect {
                mainActivityViewModel.onFragmentProcessingStateChanged(it)
            }
        }
    }

    internal open fun setUpUiEventObservers() {
        observeMainUiEvent()
        observeCommonUiEvent()
    }

    private fun observeMainUiEvent() {
        fragmentHelper
            .setUpMainUiEvent(
                this,
                mainViewModel,
                this
            )
    }

    private fun observeCommonUiEvent() {
        fragmentHelper
            .setUpCommonUiEvent(
                this,
                mainViewModel,
                this
            )
    }

    private fun observePendingNavigation() {
        fragmentHelper
            .setUpPendingNavigation(
                findNavController(),
                destinationId,
                mainViewModel
            )
    }
    //endregion

    //region Back Press Setup
    private fun registerOnBackPressedCallback() {
        fragmentHelper.registerOnBackPressedCallback(this, mainViewModel)
    }
    //endregion

    //region Navigation Helpers
    internal fun navigateFragmentOnce(command: NavigationCommand) {
        fragmentHelper
            .navigateFragmentOnce(
                findNavController(),
                destinationId,
                command
            )
    }

    internal fun navigateFragmentWithRetry(command: NavigationCommand) {
        fragmentHelper
            .navigateFragmentWithRetry(
                findNavController(),
                destinationId,
                mainViewModel,
                command
            )
    }

    internal fun navigatePreviousFragmentOnce(
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

    internal fun navigatePreviousFragmentWithRetry(
        resultKey: String? = null,
        result: FragmentResult<*> = FragmentResult.None
    ) {
        fragmentHelper
            .navigatePreviousFragmentWithRetry(
                findNavController(),
                destinationId,
                resultKey,
                result,
                mainViewModel
            )
    }

    internal abstract fun navigateAppMessageDialog(appMessage: AppMessage)
    //endregion

    //region Internal Helpers
    internal fun launchAndRepeatOnViewLifeCycleStarted(
        block: suspend CoroutineScope.() -> Unit
    ) {
        fragmentHelper.launchAndRepeatOnViewLifeCycleStarted(this, block)
    }
    //endregion
}
