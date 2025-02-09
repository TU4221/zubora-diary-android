package com.websarva.wings.android.zuboradiary.ui

import android.os.Bundle
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.websarva.wings.android.zuboradiary.MainActivity
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel

abstract class BaseFragment : CustomFragment() {

    protected lateinit var settingsViewModel: SettingsViewModel
    protected lateinit var navController: NavController
    private var destinationId: Int = 0
    protected val mainActivity get() = requireActivity() as MainActivity
    private val navBackStackEntrySavedStateHandle: SavedStateHandle get() {
        val navBackStackEntry = checkNotNull(navController.currentBackStackEntry)
        return navBackStackEntry.savedStateHandle
    }
    protected val themeColor get() = settingsViewModel.loadThemeColorSettingValue()

    protected val isDialogShowing
        get() = destinationId != getCurrentDestinationId()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViewModel()

        settingsViewModel = createSettingsViewModel()
        navController = NavHostFragment.findNavController(this)
        destinationId = getCurrentDestinationId()
    }

    /**
     * BaseFragment#onCreate()で呼び出される。
     */
    protected abstract fun initializeViewModel()

    private fun createSettingsViewModel(): SettingsViewModel {
        val provider = ViewModelProvider(requireActivity())
        return provider[SettingsViewModel::class.java]
    }

    private fun getCurrentDestinationId(): Int {
        val navDestination = navController.currentDestination
        return checkNotNull(navDestination).id
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

        enterTransition = if (mainActivity.wasSelectedTab) {
            MaterialFadeThrough() as Transition // MEMO:"as Transition"は警告対策
        } else {
            MaterialSharedAxis(MaterialSharedAxis.X, true)
        }

        // FROM - TO の FROM として消えるアニメーション
        exitTransition = MaterialSharedAxis(
            MaterialSharedAxis.X,
            true
        )

        // TO - FROM の FROM として現れるアニメーション
        reenterTransition = if (mainActivity.wasSelectedTab) {
            MaterialFadeThrough() as Transition // MEMO:"as Transition"は警告対策
        } else {
            MaterialSharedAxis(
                MaterialSharedAxis.X,
                false
            )
        }

        // TO - FROM の TO として消えるアニメーション
        returnTransition = MaterialSharedAxis(
            MaterialSharedAxis.X,
            false
        )

        mainActivity.clearWasSelectedTab()
    }

    protected fun addTransitionListener(listener: Transition.TransitionListener) {
        val enterTransition = checkNotNull(enterTransition as MaterialSharedAxis)
        enterTransition.addListener(listener)

        val exitTransition = checkNotNull(exitTransition as MaterialSharedAxis)
        exitTransition.addListener(listener)

        val reenterTransition = checkNotNull(reenterTransition as MaterialSharedAxis)
        reenterTransition.addListener(listener)

        val returnTransition = checkNotNull(returnTransition as MaterialSharedAxis)
        returnTransition.addListener(listener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPreviousFragmentResultReceiver()
        setUpDialogResultReceiver()
        setUpSettingsAppMessageDialog()
        setUpOtherAppMessageDialog()
    }

    private fun setUpPreviousFragmentResultReceiver() {
        handleOnReceivingResultFromPreviousFragment(navBackStackEntrySavedStateHandle)
    }


    /**
     * BaseFragment#setUpPreviousFragmentResultReceiver()で呼び出される。
     */
    protected abstract fun handleOnReceivingResultFromPreviousFragment(savedStateHandle: SavedStateHandle)

    private fun setUpDialogResultReceiver() {
        val navBackStackEntry = checkNotNull(navController.currentBackStackEntry)

        val lifecycleEventObserver =
            LifecycleEventObserver { _, event: Lifecycle.Event ->
                // MEMO:Dialog表示中:Lifecycle.Event.ON_PAUSE
                //      Dialog非表示中:Lifecycle.Event.ON_RESUME
                if (event == Lifecycle.Event.ON_RESUME) {
                    val savedStateHandle = navBackStackEntry.savedStateHandle
                    handleOnReceivingDialogResult(savedStateHandle)
                    retrySettingsAppMessageDialogShow()
                    retryOtherAppMessageDialogShow()
                    removeDialogResultOnDestroy(savedStateHandle)
                }
            }

        navBackStackEntry.lifecycle.addObserver(lifecycleEventObserver)
        viewLifecycleOwner.lifecycle
            .addObserver(LifecycleEventObserver { _, event: Lifecycle.Event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                    //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                    val savedStateHandle = navBackStackEntry.savedStateHandle
                    removeDialogResultOnDestroy(savedStateHandle)

                    // MEMO:removeで削除しないと再度Fragment(前回表示Fragmentと同インスタンスの場合)を表示した時、Observerが重複する。
                    navBackStackEntry.lifecycle.removeObserver(lifecycleEventObserver)
                }
            })
    }

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     */
    protected abstract fun handleOnReceivingDialogResult(savedStateHandle: SavedStateHandle)

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     */
    protected abstract fun removeDialogResultOnDestroy(savedStateHandle: SavedStateHandle)

    fun <T> receiveResulFromDialog(key: String): T? {
        val containsDialogResult = navBackStackEntrySavedStateHandle.contains(key)
        if (!containsDialogResult) return null

        return navBackStackEntrySavedStateHandle.get<T>(key)
    }

    private fun setUpSettingsAppMessageDialog() {
        settingsViewModel.appMessageBufferList
            .observe(viewLifecycleOwner, AppMessageBufferListObserver(settingsViewModel))
    }

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     * BaseViewModelのAppMessageBufferListのObserverを設定する。
     */
    protected abstract fun setUpOtherAppMessageDialog()

    protected inner class AppMessageBufferListObserver(private val baseViewModel: BaseViewModel):
        Observer<AppMessageList> {
        override fun onChanged(value: AppMessageList) {
            if (value.isEmpty) return

            val firstAppMessage = checkNotNull(value.findFirstItem())
            showAppMessageDialog(firstAppMessage)
            baseViewModel.removeAppMessageBufferListFirstItem()
        }
    }

    private fun showAppMessageDialog(appMessage: AppMessage) {
        if (isDialogShowing) return

        navigateAppMessageDialog(appMessage)
    }

    /**
     * BaseFragment#showAppMessageDialog()で呼び出される。
     */
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
