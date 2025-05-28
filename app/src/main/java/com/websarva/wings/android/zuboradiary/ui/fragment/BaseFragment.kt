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
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import com.websarva.wings.android.zuboradiary.ui.model.AppMessageList
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitleEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.SettingsAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.WordSearchAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditPendingDialog
import com.websarva.wings.android.zuboradiary.ui.model.DiaryShowPendingDialog
import com.websarva.wings.android.zuboradiary.ui.model.PendingDialog
import com.websarva.wings.android.zuboradiary.ui.model.PendingDialogList
import com.websarva.wings.android.zuboradiary.ui.viewmodel.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    internal val canNavigateFragment
        get() = destinationId == currentDestinationId

    private val addedLifecycleEventObserverList = ArrayList<LifecycleEventObserver>()

    internal var pendingDialogNavigation: PendingDialogNavigation? = null

    internal fun launchAndRepeatOnViewLifeCycleStarted(
        block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    /**
     * 戻るボタン押下時の処理。
     */
    internal fun addOnBackPressedCallback(callback: OnBackPressedCallback) {
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

        setUpPreviousFragmentResultReceiver()
        setUpDialogResultReceiver()
        setUpAppMessageDialog()
        setUpPendingDialogObserver()
        setUpNavBackStackEntryLifecycleObserverDispose()
    }

    // MEMO:Fragment、DialogFragmentからの結果受け取り方法
    //      https://developer.android.com/guide/navigation/use-graph/programmatic?hl=ja
    private fun setUpPreviousFragmentResultReceiver() {
        handleOnReceivingResultFromPreviousFragment()
    }

    /**
     * BaseFragment#setUpPreviousFragmentResultReceiver()で呼び出される。
     */
    internal abstract fun handleOnReceivingResultFromPreviousFragment()

    internal fun <T> receiveResulFromPreviousFragment(key: String): StateFlow<T?> {
        val containsDialogResult = navBackStackEntry.savedStateHandle.contains(key)
        if (!containsDialogResult) return MutableStateFlow<T?>(null)

        return navBackStackEntry.savedStateHandle.getStateFlow(key, null)
    }

    internal fun removeResulFromFragment(key: String) {
        navBackStackEntry.savedStateHandle[key] = null
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

                    retryAppMessageDialogShow()
                }
            }

        addNavBackStackEntryLifecycleObserver(lifecycleEventObserver)
    }

    private fun addNavBackStackEntryLifecycleObserver(observer: LifecycleEventObserver) {
        navBackStackEntry.lifecycle.addObserver(observer)
        addedLifecycleEventObserverList.add(observer)
    }

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     */
    internal abstract fun receiveDialogResults()

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     */
    internal abstract fun removeDialogResults()

    internal fun <T> receiveResulFromDialog(key: String): T? {
        val containsDialogResult = navBackStackEntry.savedStateHandle.contains(key)
        if (!containsDialogResult) return null

        return navBackStackEntry.savedStateHandle.get<T>(key)
    }

    internal open fun setUpAppMessageDialog() {
        setUpMainAppMessageDialog()
        setUpSettingsAppMessageDialog()
    }

    private fun setUpMainAppMessageDialog() {
        mainViewModel ?: return
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel!!.appMessageBufferList
                .collectLatest { value: AppMessageList ->
                    MainAppMessageBufferListObserver(mainViewModel!!).onChanged(value)
                }
        }
    }

    private inner class MainAppMessageBufferListObserver(viewModel: BaseViewModel)
        : AppMessageBufferListObserver(viewModel) {

        override fun checkAppMessageTargetType(appMessage: AppMessage): Boolean {
            when (appMessage) {
                is DiaryListAppMessage -> {
                    return (this@BaseFragment is DiaryListFragment)
                }
                is WordSearchAppMessage -> {
                    return (this@BaseFragment is WordSearchFragment)
                }
                is CalendarAppMessage -> {
                    return (this@BaseFragment is CalendarFragment)
                }
                is SettingsAppMessage -> {
                    return (this@BaseFragment is SettingsFragment)
                }
                is DiaryShowAppMessage -> {
                    return (this@BaseFragment is DiaryShowFragment)
                }
                is DiaryEditAppMessage -> {
                    return (this@BaseFragment is DiaryEditFragment)
                }
                is DiaryItemTitleEditAppMessage -> {
                    return (this@BaseFragment is DiaryItemTitleEditFragment)
                }
                else -> return false
            }
        }
    }

    private fun setUpSettingsAppMessageDialog() {
        launchAndRepeatOnViewLifeCycleStarted {
            settingsViewModel.appMessageBufferList
                .collectLatest { value: AppMessageList ->
                    object : AppMessageBufferListObserver(settingsViewModel) {
                        override fun checkAppMessageTargetType(appMessage: AppMessage): Boolean {
                            return appMessage is SettingsAppMessage
                        }
                    }.onChanged(value)
                }
        }
    }

    internal abstract inner class AppMessageBufferListObserver(private val baseViewModel: BaseViewModel) {
        suspend fun onChanged(value: AppMessageList) {
            if (value.isEmpty) return

            val firstAppMessage = checkNotNull(value.findFirstItem())
            if (!checkAppMessageTargetType(firstAppMessage)) throw IllegalStateException()
            withContext(Dispatchers.Main) {
                val isSuccessful = navigateAppMessageDialog(firstAppMessage)
                if (isSuccessful) baseViewModel.removeAppMessageBufferListFirstItem()
            }
        }

        protected abstract fun checkAppMessageTargetType(appMessage: AppMessage): Boolean
    }

    private fun navigateAppMessageDialog(appMessage: AppMessage): Boolean {
        if (!canNavigateFragment) return false

        onNavigateAppMessageDialog(appMessage)
        return true
    }

    /**
     * BaseFragment#showAppMessageDialog()で呼び出される。
     */
    internal abstract fun onNavigateAppMessageDialog(appMessage: AppMessage)

    internal open fun retryAppMessageDialogShow() {
        retryMainAppMessageDialogShow()
        retrySettingsAppMessageDialogShow()
    }

    private fun retryMainAppMessageDialogShow() {
        mainViewModel?.triggerAppMessageBufferListObserver() ?: return
    }

    private fun retrySettingsAppMessageDialogShow() {
        settingsViewModel.triggerAppMessageBufferListObserver()
    }

    internal interface PendingDialogNavigation {
        fun navigatePendingDialog(pendingDialog: PendingDialog):Boolean
    }

    private fun setUpPendingDialogObserver() {
        mainViewModel ?: return

        addNavBackStackEntryLifecycleObserver { _, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mainViewModel!!.triggerPendingDialogListObserver()
            }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel!!.pendingDialogList
                .collectLatest { value: PendingDialogList ->
                    val pendingDialog = value.findFirstItem() ?: return@collectLatest
                    pendingDialogNavigation ?: throw IllegalStateException()
                    if (!checkPendingDialogTargetType(pendingDialog)) throw IllegalStateException()

                    withContext(Dispatchers.Main) {
                        // MEMO:下記条件はスパークラスのFragment切替メソッドに含まれているが、
                        //      そこで判断させると再度保留ダイアログが追加されるので、ここに記述する。(重複処理防止)
                        if (!canNavigateFragment) return@withContext

                        val isSuccessful = pendingDialogNavigation!!.navigatePendingDialog(pendingDialog)
                        if (isSuccessful) mainViewModel!!.removePendingDialogListFirstItem()
                    }
                }
        }
    }

    private fun checkPendingDialogTargetType(pendingDialog: PendingDialog): Boolean {
        return when (pendingDialog) {
            is DiaryShowPendingDialog -> {
                (this@BaseFragment is DiaryShowFragment)
            }
            is DiaryEditPendingDialog -> {
                (this@BaseFragment is DiaryEditFragment)
            }
        }
    }

    // MEMO:removeで削除しないと再度Fragment(前回表示Fragmentと同インスタンスの場合)を表示した時、Observerが重複する。
    private fun setUpNavBackStackEntryLifecycleObserverDispose() {
        viewLifecycleOwner.lifecycle
            .addObserver(LifecycleEventObserver { _, event: Lifecycle.Event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    Log.d(logTag, "Lifecycle.Event.ON_DESTROY")

                    for (observer in addedLifecycleEventObserverList) {
                        navBackStackEntry.lifecycle.removeObserver(observer)
                    }
                    addedLifecycleEventObserverList.clear()
                }
            })
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
