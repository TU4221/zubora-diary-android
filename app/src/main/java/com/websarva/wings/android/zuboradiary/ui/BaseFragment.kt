package com.websarva.wings.android.zuboradiary.ui

import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.websarva.wings.android.zuboradiary.MainActivity
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList
import com.websarva.wings.android.zuboradiary.createLogTag
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseFragment : CustomFragment() {

    private val logTag = createLogTag()

    protected val mainActivity
        get() = requireActivity() as MainActivity

    protected lateinit var navController: NavController
        private set

    // MEMO:NavController#currentBackStackEntry()はFragmentライフサイクル状態で取得する値が異なるため、
    //      Create状態の時の値を保持して使用。
    //      (ViewLifeCycleEventが"OnDestroy"の時は、NavのCurrentBackStackが切替先のFragmentに更新される)
    private lateinit var navBackStackEntry: NavBackStackEntry

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    protected val settingsViewModel: SettingsViewModel by activityViewModels()

    protected val themeColor
        get() = settingsViewModel.themeColor.requireValue()

    private var destinationId = 0 // MEMO:Int型は遅延初期化不可
    private val currentDestinationId: Int get() {
        val navDestination = navController.currentDestination
        return checkNotNull(navDestination).id
    }
    protected val isDialogShowing
        get() = destinationId != currentDestinationId

    private val addedLifecycleEventObserverList = ArrayList<LifecycleEventObserver>()

    protected fun launchAndRepeatOnViewLifeCycleStarted(
        block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    /**
     * 戻るボタン押下時の処理。
     */
    protected fun addOnBackPressedCallback(callback: OnBackPressedCallback) {
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
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
    protected abstract fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding

    // ThemeColorに合わせたインフレーター作成
    private fun createThemeColorInflater(inflater: LayoutInflater): LayoutInflater {
        val creator = ThemeColorInflaterCreator(requireContext(), inflater, themeColor)
        return creator.create()
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

    protected fun addTransitionListener(listener: Transition.TransitionListener) {
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

        setUpPreviousFragmentResultReceiver()
        setUpDialogResultReceiver()
        setUpSettingsAppMessageDialog()
        setUpOtherAppMessageDialog()
    }

    // MEMO:Fragment、DialogFragmentからの結果受け取り方法
    //      https://developer.android.com/guide/navigation/use-graph/programmatic?hl=ja
    private fun setUpPreviousFragmentResultReceiver() {
        handleOnReceivingResultFromPreviousFragment()
    }

    /**
     * BaseFragment#setUpPreviousFragmentResultReceiver()で呼び出される。
     */
    protected abstract fun handleOnReceivingResultFromPreviousFragment()

    protected fun <T> receiveResulFromPreviousFragment(key: String): StateFlow<T?> {
        val containsDialogResult = navBackStackEntry.savedStateHandle.contains(key)
        if (!containsDialogResult) return MutableStateFlow<T?>(null)

        return navBackStackEntry.savedStateHandle.getStateFlow(key, null)
    }

    protected fun removeResulFromFragment(key: String) {
        navBackStackEntry.savedStateHandle.remove<Any>(key)
    }

    private fun setUpDialogResultReceiver() {

        val lifecycleEventObserver =
            LifecycleEventObserver { _, event: Lifecycle.Event ->
                // MEMO:Dialog表示中:Lifecycle.Event.ON_PAUSE
                //      Dialog非表示中:Lifecycle.Event.ON_RESUME
                if (event == Lifecycle.Event.ON_RESUME) {
                    Log.d(logTag, "Lifecycle.Event.ON_RESUME")
                    receiveDialogResults()
                    // MEMO:Results残留防止。"ON_RESUME"で呼び出される為、
                    //      削除しないと端末ホーム画面からのアプリ再表示時に受取メソッドが処理される。
                    removeDialogResults()

                    retrySettingsAppMessageDialogShow()
                    retryOtherAppMessageDialogShow()
                }
            }

        addNavBackStackEntryLifecycleObserver(lifecycleEventObserver)
        viewLifecycleOwner.lifecycle
            .addObserver(LifecycleEventObserver { _, event: Lifecycle.Event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    Log.d(logTag, "Lifecycle.Event.ON_DESTROY")

                    // MEMO:removeで削除しないと再度Fragment(前回表示Fragmentと同インスタンスの場合)を表示した時、Observerが重複する。
                    for (observer in addedLifecycleEventObserverList) {
                        navBackStackEntry.lifecycle.removeObserver(observer)
                    }
                    addedLifecycleEventObserverList.clear()
                }
            })
    }

    protected fun addNavBackStackEntryLifecycleObserver(observer: LifecycleEventObserver) {
        navBackStackEntry.lifecycle.addObserver(observer)
        addedLifecycleEventObserverList.add(observer)
    }

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     */
    protected abstract fun receiveDialogResults()

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     */
    protected abstract fun removeDialogResults()

    protected fun <T> receiveResulFromDialog(key: String): T? {
        val containsDialogResult = navBackStackEntry.savedStateHandle.contains(key)
        if (!containsDialogResult) return null

        return navBackStackEntry.savedStateHandle.get<T>(key)
    }

    private fun setUpSettingsAppMessageDialog() {
        launchAndRepeatOnViewLifeCycleStarted {
            settingsViewModel.appMessageBufferList
                .collectLatest { value: AppMessageList ->
                    AppMessageBufferListObserver(settingsViewModel).onChanged(value)
                }
        }
    }

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     * BaseViewModelのAppMessageBufferListのObserverを設定する。
     */
    protected abstract fun setUpOtherAppMessageDialog()

    protected inner class AppMessageBufferListObserver(private val baseViewModel: BaseViewModel) {
        suspend fun onChanged(value: AppMessageList) {
            if (value.isEmpty) return

            val firstAppMessage = checkNotNull(value.findFirstItem())
            showAppMessageDialog(firstAppMessage)
        }

        private suspend fun showAppMessageDialog(appMessage: AppMessage) {
            if (isDialogShowing) return

            withContext(Dispatchers.Main) {
                navigateAppMessageDialog(appMessage)
                baseViewModel.removeAppMessageBufferListFirstItem()
            }
        }
    }

    /**
     * BaseFragment#showAppMessageDialog()で呼び出される。
     */
    @MainThread
    protected abstract fun navigateAppMessageDialog(appMessage: AppMessage)

    protected abstract fun retryOtherAppMessageDialogShow()

    private fun retrySettingsAppMessageDialogShow() {
        settingsViewModel.triggerAppMessageBufferListObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        destroyBinding()
    }

    /**
     * Bindingクラス変数のメモリリーク対策として変数にNullを代入すること。
     */
    protected abstract fun destroyBinding()
}
