package com.websarva.wings.android.zuboradiary.ui.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorUi
import com.websarva.wings.android.zuboradiary.databinding.ActivityMainBinding
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.common.event.ConsumableEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import androidx.core.view.size
import androidx.core.view.get
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.common.activity.LoggingActivity
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.ActivityNavigationEventHandler
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.ActivityNavigationEventHelper
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.FragmentNavigationEventHelper
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.NavigationEvent
import com.websarva.wings.android.zuboradiary.ui.common.theme.withTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * このアプリケーションの単一のActivity。
 * Fragmentのホストとして機能し、全体的なUIとナビゲーションを管理する。
 *
 * 以下の責務を持つ:
 * - スプラッシュスクリーンの表示と制御
 * - エッジ・ツー・エッジ表示のセットアップ
 * - アプリケーション全体のテーマカラーの適用と動的変更
 * - [NavHostFragment]と[BottomNavigationView]のセットアップ
 * - Fragmentのライフサイクルに応じたUIの状態管理
 * - UI状態([MainActivityUiState])、UIイベント([MainActivityUiEvent])、ナビゲーションイベント([NavigationEvent])との監視
 * - [FragmentNavigationEventHelper]を介したナビゲーションイベントの実行と保留・再実行機能の提供
 */
@AndroidEntryPoint
class MainActivity : LoggingActivity(), ActivityNavigationEventHandler<MainActivityNavDestination> {

    companion object {
        private const val SAVED_STATE_THEME_COLOR = "saved_state_theme_color"
    }

    /** [ViewBinding]のインスタンス。onDestroyでnullに設定される。 */
    private var _binding: ActivityMainBinding? = null
    /** [ViewBinding]のインスタンスへの非nullアクセスを提供する。 */
    private val binding get() = checkNotNull(_binding)

    /** MainActivityのレイアウトがインフレートされたかを示すフラグ。スプラッシュスクリーンの表示制御に使用する。 */
    private var isMainActivityLayoutInflated = false

    /** MotionLayoutの状態遷移時にアニメーションなしでジャンプすべきかを示すフラグ。 */
    private var shouldJumpToInitialState = true

    /** 現在のテーマカラー。 */
    internal lateinit var themeColor: ThemeColorUi
        private set

    private val themeColorChanger = ThemeColorChanger()

    /** ナビゲーションイベント（[NavigationEvent]）の処理をまとめたヘルパークラス。 */
    private val activityNavigationEventHelper = ActivityNavigationEventHelper()

    /** このActivityに対応するViewModel。 */
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    /** アプリケーションのナビゲーションを管理する[NavController]。 */
    private val navController by lazy {
        // MEMO:Activity#findNavController()でNavControllerを取得する場合、
        //      アプリ設定(権限等)変更時でのアプリ再起動時にNavControllerの取得に失敗する為、
        //      NavHostFragmentから取得する。
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_nav_host) as NavHostFragment
        navHostFragment.navController
    }

    /** 追加処理として、スプラッシュスクリーンの表示、エッジツーエッジのセットアップ、UIの初期設定を行う。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !isMainActivityLayoutInflated }
        setupEdgeToEdge()
        restoreThemeColor(savedInstanceState)
        super.onCreate(savedInstanceState)

        setupUi()
    }

    /** 追加処理として、現在のテーマカラーを保存する。 */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::themeColor.isInitialized) {
            outState.putParcelable(SAVED_STATE_THEME_COLOR, themeColor)
        }
    }

    /** エッジ・ツー・エッジ表示を有効にする。 */
    // MEMO:EdgeToEdge対応。下記ページ参照。
    //      https://developer.android.com/develop/ui/views/layout/edge-to-edge?hl=ja
    //      https://developer.android.com/codelabs/edge-to-edge?hl=ja#2
    private fun setupEdgeToEdge() {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    /**
     * [savedInstanceState]からテーマカラーを復元する。
     * @param savedInstanceState 復元元のBundle
     */
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

    /** UIの初期設定を行う。テーマカラーの取得後、Viewのバインディング、UIイベント/ステートの監視、ナビゲーションのセットアップを順に行う。 */
    private fun setupUi() {
        lifecycleScope.launch {
            themeColor =
                mainActivityViewModel.uiState.map { it.themeColor }.filterNotNull().first()
            setupMainActivityBinding()
            isMainActivityLayoutInflated = true
            observeUiEvent()
            observeUiState()
            observeNavigationEvent()
            setupNavigation()
            mainActivityViewModel.onUiReady()
        }
    }

    /** テーマカラーを適用したInflaterでViewを生成し、コンテンツビューとして設定する。 */
    private fun setupMainActivityBinding() {
        val themeColorInflater = layoutInflater.withTheme(themeColor)
        _binding =
            ActivityMainBinding.inflate(themeColorInflater).apply {
                viewModel = mainActivityViewModel
                lifecycleOwner = this@MainActivity
            }
        setContentView(binding.root)
    }

    /** MainActivity固有のUIイベントを監視する。 */
    private fun observeUiEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.uiEvent
                    .collectLatest { value: ConsumableEvent<MainActivityUiEvent> ->
                        value.getContentIfNotHandled() ?: return@collectLatest

                        // 処理イベントなし
                    }
            }
        }
    }

    /** MainActivity固有のUI状態の変更を監視し、UIを更新する。 */
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

    /**
     * [BottomNavigationView]の表示/非表示をアニメーションで切り替える。
     * @param isShowed 表示する場合はtrue
     */
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
            binding.motionLayoutBottomNavigation.doOnPreDraw { 
                (it as MotionLayout).transitionToState(motionResId)
            }
        }
    }

    /**
     * [BottomNavigationView]の各メニューアイテムの有効/無効状態を切り替える。
     * @param isEnabled 有効にする場合はtrue
     */
    private fun switchBottomNavigationEnabled(isEnabled: Boolean) {
        val menu = binding.bottomNavigation.menu
        val size = menu.size
        for (i in 0 until size) {
            menu[i].isEnabled = isEnabled
        }
    }

    /**
     * 指定されたテーマカラーにUI全体の配色を切り替える。
     * @param themeColor 適用するテーマカラー
     */
    private fun switchThemeColor(themeColor: ThemeColorUi) {
        with(themeColorChanger) {
            applyStatusBarIconColor(window, themeColor)
            applyNavigationBarIconColor(window, themeColor)
            applyAppBackgroundColor(binding.root, themeColor)
            applyAppToolbarColor(binding.materialToolbarTopAppBar, themeColor, binding.appBarLayout)
            applyAppBottomNavigationColor(binding.bottomNavigation, themeColor)
        }
    }

    /** [BottomNavigationView]と[NavController]を連携させる。 */
    private fun setupNavigation() {
        // Navigation設定
        // 参考:https://inside.luchegroup.com/entry/2023/05/08/113236
        val bottomNavigationView = binding.bottomNavigation
        setupWithNavController(bottomNavigationView, navController)
        bottomNavigationView.apply {
            // MEMO:setupWithNavController()で設定したOnItemSelectedListenerを下記で上書き
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

    /**
     * このアクティビティ固有のViewModelが発行するナビゲーションイベント([NavigationEvent])を監視する。
     * 新規イベントを受け取ると、[FragmentNavigationEventHelper]にナビゲーション処理の実行を委譲する。
     */
    private fun observeNavigationEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.navigationEvent
                    .collect { value: ConsumableEvent<NavigationEvent.To<MainActivityNavDestination>> ->
                        val event = value.getContentIfNotHandled().also {
                            Log.d(logTag, "NavigationEvent_Collect(): $it")
                        } ?: return@collect

                        activityNavigationEventHelper
                            .executeFragmentNavigation(
                                lifecycle,
                                navController,
                                event,
                                this@MainActivity,
                                mainActivityViewModel
                            )
                    }
            }
        }
    }

    override fun toNavDirections(destination: MainActivityNavDestination): NavDirections {
        return when (destination) {
            is MainActivityNavDestination.AppMessageDialog -> {
                activityNavigationEventHelper
                    .createAppMessageDialogNavDirections(destination.message)
            }
        }
    }

    /** 追加処理として、[ViewBinding]の解放、[MainActivityViewModel]への通知を行う。 */
    override fun onDestroy() {
        clearViewBindings()
        mainActivityViewModel.onUiGone()

        super.onDestroy()
    }

    /** [ViewBinding]とリスナーを解放する。 */
    private fun clearViewBindings() {
        with(binding.bottomNavigation) {
            setOnItemSelectedListener(null)
            setOnItemReselectedListener(null)
        }
        _binding = null
    }
}
