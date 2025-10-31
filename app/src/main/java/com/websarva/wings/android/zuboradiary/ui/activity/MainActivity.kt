package com.websarva.wings.android.zuboradiary.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.databinding.ActivityMainBinding
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.MainActivityUiEvent
import com.websarva.wings.android.zuboradiary.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import androidx.core.view.size
import androidx.core.view.get
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class MainActivity : LoggingActivity() {

    companion object {
        private const val SAVED_STATE_THEME_COLOR = "saved_state_theme_color"
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = checkNotNull(_binding)
    private var isMainActivityLayoutInflated = false
    private var shouldJumpToInitialState = true
    internal lateinit var themeColor: ThemeColorUi
        private set

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !isMainActivityLayoutInflated }
        setUpEdgeToEdge()
        restoreThemeColor(savedInstanceState)
        super.onCreate(savedInstanceState)

        setUpUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::themeColor.isInitialized) {
            outState.putParcelable(SAVED_STATE_THEME_COLOR, themeColor)
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

    private fun restoreThemeColor(savedInstanceState: Bundle?) {
        val savedThemeColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState?.getParcelable(
                SAVED_STATE_THEME_COLOR,
                ThemeColorUi::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            savedInstanceState?.getParcelable(SAVED_STATE_THEME_COLOR)
        }
        savedThemeColor?.let { themeColor = it }
    }

    private fun setUpUi() {
        lifecycleScope.launch {
            themeColor =
                mainActivityViewModel.uiState.map { it.themeColor }.filterNotNull().first()
            setUpMainActivityBinding()
            isMainActivityLayoutInflated = true
            setUpUiEvent()
            observeUiState()
            setUpNavigation()
        }
    }

    private fun setUpMainActivityBinding() {
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
                mainActivityViewModel.activityUiEvent
                    .collectLatest { value: ConsumableEvent<MainActivityUiEvent> ->
                        val event = value.getContentIfNotHandled() ?: return@collectLatest
                        when (event) {
                            MainActivityUiEvent.NavigateStartTabFragment -> navigateBottomNavigationStartTabFragment()
                        }
                    }
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.uiState
                    .map { it.themeColor }.filterNotNull().distinctUntilChanged().collect {
                        themeColor = it
                        switchThemeColor(it)
                    }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.uiState
                    .map { it.isBottomNavigationVisible }.distinctUntilChanged().collect {
                        switchBottomNavigationState(it)
                    }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.uiState
                    .map { it.isBottomNavigationEnabled }.distinctUntilChanged().collect {
                        switchBottomNavigationEnabled(it)
                    }
            }
        }
    }

    private fun switchBottomNavigationState(isShowed: Boolean) {
        val motionResId =
            if (isShowed) {
                R.id.motion_scene_bottom_navigation_visible_state
            } else {
                R.id.motion_scene_bottom_navigation_invisible_state
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

    private fun switchBottomNavigationEnabled(isEnabled: Boolean) {
        val menu = binding.bottomNavigation.menu
        val size = menu.size
        for (i in 0 until size) {
            menu[i].isEnabled = isEnabled
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
                Log.i(logTag, "ボトムナビゲーション_セレクト")
                mainActivityViewModel.onBottomNavigationItemSelect()
                onNavDestinationSelected(menuItem, navController)
            }
            setOnItemReselectedListener {
                Log.i(logTag, "ボトムナビゲーション_リセレクト")
                mainActivityViewModel.onBottomNavigationItemReselect()
            }
        }
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
