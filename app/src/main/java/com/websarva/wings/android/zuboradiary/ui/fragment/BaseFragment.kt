package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseFragment : LoggingFragment() {

    private val logTag = createLogTag()

    internal val mainActivity
        get() = requireActivity() as MainActivity

    // MEMO:ViewModelが無いFragmentに対応できるようにNull許容型とする。
    internal abstract val mainViewModel: BaseViewModel?

    internal lateinit var navController: NavController
        private set

    // MEMO:NavController#currentBackStackEntry()はFragmentライフサイクル状態で取得する値が異なるため、
    //      Create状態の時の値を保持して使用。
    //      (ViewLifeCycleEventが"OnDestroy"の時は、NavのCurrentBackStackが切替先のFragmentに更新される)
    private lateinit var navBackStackEntry: NavBackStackEntry

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    internal val settingsViewModel: SettingsViewModel by activityViewModels()

    internal val themeColor
        get() = settingsViewModel.themeColor.requireValue()

    private var destinationId = 0 // MEMO:Int型は遅延初期化不可
    private val currentDestinationId: Int get() {
        val navDestination = navController.currentDestination
        return checkNotNull(navDestination).id
    }
    private val canNavigateFragment
        get() = destinationId == currentDestinationId

    internal fun launchAndRepeatOnViewLifeCycleStarted(
        block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        setUpFragmentTransitionEffect()

        val themeColorInflater = createThemeColorInflater(inflater)
        val dataBinding = initializeDataBinding(themeColorInflater, requireNotNull(container))
        return dataBinding.root
    }

    /**
     * BaseFragment#onCreateView()で呼び出される。
     */
    internal abstract fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding

    // ThemeColorに合わせたインフレーター作成
    private fun createThemeColorInflater(inflater: LayoutInflater): LayoutInflater {
        return ThemeColorInflaterCreator().create(inflater, themeColor)
    }

    private fun setUpFragmentTransitionEffect() {
        // FROM:遷移元 TO:遷移先
        // FROM - TO の TO として現れるアニメーション

        // HACK:ボトムナビゲーションタブでFragment切替時はEnterTransitionで設定されるエフェクトを変更する。
        //      NavigationStartFragment(DiaryListFragment)はReenterTransitionで設定されたエフェクトが処理される。
        //      遷移元FragmentのエフェクトはMainActivityクラスにて設定。
        val enterTransitionType: Transition = if (mainActivity.wasSelectedTab) {
            MaterialFadeThrough()
        } else {
            MaterialSharedAxis(MaterialSharedAxis.X, true)
        }
        enterTransition = enterTransitionType

        // FROM - TO の FROM として消えるアニメーション
        exitTransition = MaterialSharedAxis(
            MaterialSharedAxis.X,
            true
        )

        // TO - FROM の FROM として現れるアニメーション
        val reenterTransitionType: Transition = if (mainActivity.wasSelectedTab) {
            MaterialFadeThrough()
        } else {
            MaterialSharedAxis(
                MaterialSharedAxis.X,
                false
            )
        }
        reenterTransition = reenterTransitionType

        // TO - FROM の TO として消えるアニメーション
        returnTransition = MaterialSharedAxis(
            MaterialSharedAxis.X,
            false
        )

        mainActivity.clearWasSelectedTab()
    }

    internal fun addTransitionListener(listener: Transition.TransitionListener) {
        val enterTransition = checkNotNull(enterTransition) as MaterialSharedAxis
        enterTransition.addListener(listener)

        val exitTransition = checkNotNull(exitTransition) as MaterialSharedAxis
        exitTransition.addListener(listener)

        val reenterTransition = checkNotNull(reenterTransition) as MaterialSharedAxis
        reenterTransition.addListener(listener)

        val returnTransition = checkNotNull(returnTransition) as MaterialSharedAxis
        returnTransition.addListener(listener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = NavHostFragment.findNavController(this)
        navBackStackEntry = checkNotNull(navController.currentBackStackEntry)
        destinationId = currentDestinationId

        initializeFragmentResultReceiver()
        setUpViewModelEvent()
        setUpPendingNavigationCollector()
        registerOnBackPressedCallback()
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

    private fun <R> setUpFragmentResultReceiverInternal(key: String, block: (R) -> Unit) {
        val savedStateHandle = navBackStackEntry.savedStateHandle
        val result = savedStateHandle.getStateFlow(key, null)
        launchAndRepeatOnViewLifeCycleStarted {
            result.collectLatest { value: R? ->
                if (value == null) return@collectLatest
                block(value)

                savedStateHandle[key] = null
            }
        }
    }

    private fun setUpViewModelEvent() {
        setUpMainViewModelEvent()
        setUpSettingsViewModelEvent()
    }

    private fun setUpMainViewModelEvent() {
        mainViewModel ?: return

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel!!.viewModelEvent
                .collect { value: ConsumableEvent<ViewModelEvent> ->
                    val event = value.getContentIfNotHandled()
                    Log.d(logTag, "ViewModelEvent_Collect(): $event")
                    event ?: return@collect
                    onMainViewModelEventReceived(event)
                }
        }
    }

    internal abstract fun onMainViewModelEventReceived(event: ViewModelEvent)

    private fun setUpSettingsViewModelEvent() {
        if (mainViewModel == settingsViewModel) return

        launchAndRepeatOnViewLifeCycleStarted {
            settingsViewModel.viewModelEvent
                .collect { value: ConsumableEvent<ViewModelEvent> ->
                    val event = value.getContentIfNotHandled()
                    Log.d(logTag, "SettingsViewModelEvent_Collect(): $event")
                    event ?: return@collect
                    when (event) {
                        is ViewModelEvent.NavigateAppMessage -> {
                            navigateAppMessageDialog(event.message)
                        }
                        else -> {
                            throw IllegalArgumentException()
                        }
                    }
                }
        }
    }

    private fun setUpPendingNavigationCollector() {
        mainViewModel ?: return

        navBackStackEntry.lifecycleScope.launch {
            navBackStackEntry.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel!!.pendingNavigationCommand
                    .collectLatest { value ->
                        Log.d("20250530", "collect()_$value")
                        when (value) {
                            NavigationCommand.None -> {
                                // 処理なし
                            }
                            else -> {
                                if (!canNavigateFragment) {
                                    mainViewModel!!.onPendingFragmentNavigationFailed()
                                    return@collectLatest
                                }

                                navigateFragment(value)
                                mainViewModel!!.onPendingFragmentNavigationCompleted()
                            }
                        }
                    }
            }
        }
    }

    internal fun navigateFragment(command: NavigationCommand) {
        mainViewModel ?: return

        if (!canNavigateFragment) {
            mainViewModel!!.onFragmentNavigationFailed(command)
            return
        }

        when (command) {
            is NavigationCommand.To -> {
                navController.navigate(command.directions)
            }
            is NavigationCommand.Up<*> -> {
                if (command.resultKey != null) {
                    val previousBackStackEntry = checkNotNull(navController.previousBackStackEntry)
                    previousBackStackEntry.savedStateHandle[command.resultKey] = command.result
                }
                navController.navigateUp()
            }
            is NavigationCommand.Pop<*> -> {
                if (command.resultKey != null) {
                    val previousBackStackEntry = checkNotNull(navController.previousBackStackEntry)
                    previousBackStackEntry.savedStateHandle[command.resultKey] = command.result
                }
                navController.popBackStack()
            }
            is NavigationCommand.PopTo<*> -> {
                if (command.resultKey != null) {
                    val previousBackStackEntry = navController.getBackStackEntry(command.destinationId)
                    previousBackStackEntry.savedStateHandle[command.resultKey] = command.result
                }
                navController.popBackStack(command.destinationId, command.inclusive)
            }
            NavigationCommand.None -> {
                // 処理なし
            }
        }
    }

    internal open fun navigatePreviousFragment() {
        navigateFragment(NavigationCommand.Up<Nothing>())
    }

    internal abstract fun navigateAppMessageDialog(appMessage: AppMessage)

    private fun registerOnBackPressedCallback() {
        requireActivity().onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (mainViewModel == null) {
                            navigatePreviousFragment()
                        } else {
                            mainViewModel!!.onBackPressed()
                        }
                    }
                }
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        destroyBinding()
    }

    /**
     * Bindingクラス変数のメモリリーク対策として変数にNullを代入すること。
     */
    internal abstract fun destroyBinding()
}
