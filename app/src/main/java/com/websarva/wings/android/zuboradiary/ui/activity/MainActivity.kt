package com.websarva.wings.android.zuboradiary.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.databinding.ActivityMainBinding
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.fragment.common.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.fragment.common.ReselectableFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.MainActivityEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.MainActivityUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.MainActivityViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import androidx.core.view.size
import androidx.core.view.get
import com.websarva.wings.android.zuboradiary.core.utils.logTag

@AndroidEntryPoint
class MainActivity : LoggingActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = checkNotNull(_binding)
    private var isMainActivityLayoutInflated = false
    private var shouldJumpToInitialState = true

    private val navHostFragment: NavHostFragment
        get() =
            checkNotNull(
                supportFragmentManager.findFragmentById(R.id.fragment_nav_host)
            ) as NavHostFragment

    private val navFragmentManager: FragmentManager
        get() = navHostFragment.childFragmentManager

    private val currentFragment: Fragment
        get() = navFragmentManager.fragments[0]

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    @Suppress("unused", "RedundantSuppression")
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !isMainActivityLayoutInflated }
        setUpEdgeToEdge()
        super.onCreate(savedInstanceState)

        setUpFragmentLifeCycleCallBacks()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.themeColor.filterNotNull()
                    .collectLatest { value: ThemeColorUi ->
                        if (isMainActivityLayoutInflated) return@collectLatest
                        setUpMainActivityBinding(value)
                        isMainActivityLayoutInflated = true
                        setUpUiEvent()
                        setUpUiState()
                        setUpThemeColor()
                        setUpNavigation()
                    }
            }
        }
    }

    // MEMO:EdgeToEdge対応。下記ページ参照。
    //      https://developer.android.com/develop/ui/views/layout/edge-to-edge?hl=ja
    //      https://developer.android.com/codelabs/edge-to-edge?hl=ja#2
    private fun setUpEdgeToEdge() {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun setUpFragmentLifeCycleCallBacks() {
        // MEMO:Bindingインフレート後にCallbacksを登録していたが、インフレートのタイミングでFragmentが作成され
        //      Resume状態となる為、下記方法でインフレート前からCallbacksが処理されるように対応。
        //      また、"recursive = true"の理由は、NavHostFragmentからFragmentManagerを取得してCallBacksを
        //      登録しようとするにはコードが複雑になり、設定変更等によるアプリ再起動時を考慮すると登録タイミングが複雑になる為。
        supportFragmentManager.apply {
            registerFragmentLifecycleCallbacks(
                BottomNavigationEnabledSwitchCallbacks { isEnabled ->
                    mainActivityViewModel.onRequestBottomNavigationEnabledChange(isEnabled)
                },
                true
            )

            registerFragmentLifecycleCallbacks(
                BottomNavigationStateSwitchCallbacks { isVisible ->
                    mainActivityViewModel.onRequestBottomNavigationStateChange(isVisible)
                },
                true
            )
        }
    }

    // MEMO:タブ選択で下記の様な画面遷移を行う時、Bを表示中にタブ選択でAを表示させようとすると、
    //      BのFragmentが消えた後、AのFragmentが表示されない不具合が生じる。
    //      (何も表示されない状態)
    //      これを回避するために、遷移先のFragmentが表示しきるまで、タブ選択できないようにする。
    //      Fragment A → B → A
    private class BottomNavigationEnabledSwitchCallbacks(
        private val processEnabledBottomNavigation: (isEnabled: Boolean) -> Unit
    ) : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            super.onFragmentPaused(fm, f)
            if (f.parentFragment !is NavHostFragment) return

            if (isFragmentWithBottomNavigation(f)) {
                processEnabledBottomNavigation(true)
            }
        }

        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
            super.onFragmentPaused(fm, f)
            if (f.parentFragment !is NavHostFragment) return

            if (isFragmentWithBottomNavigation(f)) {
                processEnabledBottomNavigation(false)
            }
        }

        private fun isFragmentWithBottomNavigation(f: Fragment): Boolean {
            return f is RequiresBottomNavigation
        }
    }

    private class BottomNavigationStateSwitchCallbacks(
        private val processVisibleBottomNavigation: (isVisible: Boolean) -> Unit
    ) : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)
            if (f.parentFragment !is NavHostFragment) return

            if (f is DialogFragment) return

            processVisibleBottomNavigation(f is RequiresBottomNavigation)
        }
    }

    private fun setUpMainActivityBinding(themeColor: ThemeColorUi) {
        val themeColorInflater = ThemeColorInflaterCreator().create(layoutInflater, themeColor)
        _binding =
            ActivityMainBinding.inflate(themeColorInflater).apply {
                viewModel = mainActivityViewModel
                lifecycleOwner = this@MainActivity
            }
        setContentView(binding.root)
    }

    private fun setUpUiEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.uiEvent
                    .collectLatest { value: ConsumableEvent<MainActivityEvent> ->
                        val event = value.getContentIfNotHandled() ?: return@collectLatest
                        when (event) {
                            MainActivityEvent.NavigateStartTabFragment -> navigateBottomNavigationStartTabFragment()
                        }
                    }
            }
        }
    }

    private fun setUpUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.uiState
                    .collectLatest { state ->
                        switchBottomNavigationState(state)
                        switchBottomNavigationEnabled(state)
                    }
            }
        }
    }

    private fun switchBottomNavigationState(state: MainActivityUiState) {
        val motionResId =
            when (state) {
                is MainActivityUiState.ShowingBottomNavigation -> {
                    R.id.motion_scene_bottom_navigation_visible_state
                }
                is MainActivityUiState.HidingBottomNavigation -> {
                    R.id.motion_scene_bottom_navigation_invisible_state
                }
            }

        if (shouldJumpToInitialState) {
            shouldJumpToInitialState = false
            binding.motionLayoutBottomNavigation.jumpToState(motionResId)
        } else {
            // HACK:BottomNavigationViewを非表示から表示に変更した時Viewが一瞬ぶれる為、下記コードで対策。
            binding.motionLayoutBottomNavigation.apply {
                post {
                    transitionToState(motionResId)
                }
            }
        }
    }

    private fun switchBottomNavigationEnabled(state: MainActivityUiState) {
        val menu = binding.bottomNavigation.menu
        val size = menu.size
        for (i in 0 until size) {
            menu[i].isEnabled = state.isBottomNavigationEnabled
        }
    }

    private fun setUpThemeColor() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.themeColor.filterNotNull()
                    .collectLatest { themeColor: ThemeColorUi ->
                        switchThemeColor(themeColor)
                    }
            }
        }
    }

    private fun switchThemeColor(themeColor: ThemeColorUi) {
        val changer = ThemeColorChanger()
        changer.applyStatusBarIconColor(window, themeColor)
        changer.applyNavigationBarIconColor(window, themeColor)
        changer.applyBackgroundColor(binding.root, themeColor)
        changer.applyToolbarColor(binding.materialToolbarTopAppBar, themeColor, binding.appBarLayout)
        changer.applyBottomNavigationColor(binding.bottomNavigation, themeColor)
    }

    private fun setUpNavigation() {
        // Navigation設定
        // 参考:https://inside.luchegroup.com/entry/2023/05/08/113236
        val bottomNavigationView = binding.bottomNavigation
        val navHostFragment =
            checkNotNull(
                supportFragmentManager.findFragmentById(R.id.fragment_nav_host)
            ) as NavHostFragment
        // MEMO:Activity#findNavController()でNavControllerを取得する場合、
        //      アプリ設定(権限等)変更時でのアプリ再起動時にNavControllerの取得に失敗する為、
        //      NavHostFragmentから取得する。
        val navController = navHostFragment.navController
        setupWithNavController(bottomNavigationView, navController)

        bottomNavigationView.apply {
            setOnItemSelectedListener { menuItem: MenuItem ->
                mainActivityViewModel.onBottomNavigationItemSelect()
                setUpFragmentTransitionOnTabSelection()
                onNavDestinationSelected(menuItem, navController)
            }
            setOnItemReselectedListener {
                val currentFragment = currentFragment
                if (currentFragment !is ReselectableFragment) return@setOnItemReselectedListener

                Log.i(logTag, "ボトムナビゲーション_リセレクト")
                currentFragment.onBottomNavigationItemReselected()
            }
        }
    }

    private fun setUpFragmentTransitionOnTabSelection() {
        // 表示中のFragmentを取得し、Transitionを設定
        currentFragment.apply {
            exitTransition = MaterialFadeThrough()
            returnTransition = MaterialFadeThrough()
        }

        // MEMO:NavigationUI.onNavDestinationSelected()による、
        //      Fragment切替時の対象Transitionパターン表(StartDestination:A-1)
        //* A-1 → B-1 : Exit → Enter
        //* B-1 → A-1 : Return → Reenter

        //* A-2 → B-1 : Exit → Enter
        //* B-1 → A-2 : Exit → Enter

        //* B-2 → A-1 : Return → Reenter
        //* A-1 → B-2 : Exit → Enter

        //* A-2 → B-2 : Exit → Enter
        //* B-2 → A-2 : Exit → Enter

        //* B-1 → C-1 : Exit → Enter
        //* C-1 → B-1 : Exit → Enter

        //* B-2 → C-1 : Exit → Enter
        //* C-1 → B-2 : Exit → Enter

        //* C-2 → B-1 : Exit → Enter
        //* B-1 → C-2 : Exit → Enter

        //* B-2 → C-2 : Exit → Enter
        //* C-2 → B-2 : Exit → Enter
    }

    // MEMO:BottomNavigationView経由のFragment間でNavigateUpすると、意図しない遷移エフェクトになる。
    //      これは遷移エフェクトの設定方法の都合によるものになる。
    //      これを避け、ユーザーがタブを再選択した際と同じ挙動(正しいエフェクト)で開始Fragmentに戻すために、
    //      このメソッドを使用する。
    private fun navigateBottomNavigationStartTabFragment() {
        binding.bottomNavigation.apply {
            selectedItemId =
                menu[0].itemId // 初期メニューアイテム(アプリ起動で最初に選択されているアイテム)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        clearViewBindings()
    }

    private fun clearViewBindings() {
        binding.bottomNavigation.apply {
            setOnItemSelectedListener(null)
            setOnItemReselectedListener(null)
        }
        _binding = null
    }
}
