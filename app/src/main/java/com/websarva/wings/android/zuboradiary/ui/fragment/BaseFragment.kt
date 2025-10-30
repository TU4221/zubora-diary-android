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
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.result.NavigationResult
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.MainActivityViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest

abstract class BaseFragment<T: ViewBinding, E : UiEvent> : LoggingFragment() {

    // View関係
    private var _binding: T? = null
    internal val binding get() = checkNotNull(_binding)

    internal abstract val mainViewModel: BaseViewModel<E, out AppMessage, out UiState>

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    internal val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    @Suppress("unused", "RedundantSuppression")
    internal val settingsViewModel: SettingsViewModel by activityViewModels()

    internal abstract val destinationId: Int
    private val isNavigationStartFragment: Boolean
        get() {
            val topLevelGraph = findNavController().graph
            val nestedStartGraphId = topLevelGraph.startDestinationId
            val nestedStartGraph = topLevelGraph.findNode(nestedStartGraphId) as NavGraph
            val startDestinationId = nestedStartGraph.startDestinationId
            return destinationId == startDestinationId
        }

    private val fragmentHelper = FragmentHelper()

    internal val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    internal fun launchAndRepeatOnViewLifeCycleStarted(
        block: suspend CoroutineScope.() -> Unit
    ) {
        fragmentHelper.launchAndRepeatOnViewLifeCycleStarted(this, block)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        setUpVisibleFragmentTransitionEffect()

        val themeColorInflater = fragmentHelper.createThemeColorInflater(inflater, themeColor)
        _binding = createViewBinding(themeColorInflater, requireNotNull(container))
        return binding.root
    }

    /**
     * BaseFragment#onCreateView()で呼び出される。
     */
    internal abstract fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): T

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFragmentResultReceiver()
        setUpUiEvent()
        setUpPendingNavigationCollector()
        setUpProgressIndicator()
        if (!isNavigationStartFragment) registerOnBackPressedCallback()
    }

    /**
     *  setUpFragmentResultReceiver()、setUpDialogResultReceiver()を使用してフラグメント、ダイアログからの結果の処理内容を設定する
     * */
    internal abstract fun initializeFragmentResultReceiver()

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

    private fun setUpUiEvent() {
        setUpMainUiEvent()
        setUpSettingsUiEvent()
    }

    private fun setUpMainUiEvent() {
        fragmentHelper
            .setUpMainUiEvent(
                this,
                mainViewModel,
                ::onMainUiEventReceived
            )
    }

    internal abstract fun onMainUiEventReceived(event: E)

    private fun setUpSettingsUiEvent() {
        fragmentHelper.setUpSettingsUiEvent(
            this,
            mainViewModel,
            settingsViewModel,
            ::navigateAppMessageDialog
        )
    }

    private fun setUpPendingNavigationCollector() {
        fragmentHelper
            .setUpPendingNavigationCollector(
                findNavController(),
                destinationId,
                mainViewModel
            )
    }

    private fun setUpProgressIndicator() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.isProgressIndicatorVisible.collectLatest {
                mainActivityViewModel.onFragmentProgressStateChanged(it)
            }
        }
    }

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

    private fun registerOnBackPressedCallback() {
        fragmentHelper.registerOnBackPressedCallback(this, mainViewModel)
    }

    override fun onPause() {
        setUpInvisibleFragmentTransitionEffect()
        super.onPause()
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

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        clearViewBindings()

        super.onDestroyView()
    }

    internal open fun clearViewBindings() {
        _binding = null
    }
}
