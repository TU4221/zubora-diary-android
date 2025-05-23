package com.websarva.wings.android.zuboradiary.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationBarView.OnItemReselectedListener
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.ActivityMainBinding
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.fragment.CalendarFragment
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryEditFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryListFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.SettingsFragment
import com.websarva.wings.android.zuboradiary.ui.utils.isGrantedAccessLocation
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : LoggingActivity() {

    private val logTag = createLogTag()

    private var _binding: ActivityMainBinding? = null
    private val binding get() = checkNotNull(_binding)
    private var isMainActivityLayoutInflated = false

    // BottomNavigation
    internal var wasSelectedTab = false
        private set

    private val navHostFragment: NavHostFragment
        get() =
            checkNotNull(
                supportFragmentManager.findFragmentById(R.id.fragment_nav_host)
            ) as NavHostFragment

    private val navFragmentManager: FragmentManager
        get() = navHostFragment.childFragmentManager

    private val showedFragment: Fragment
        get() = navFragmentManager.fragments[0]

    private val selectedBottomNavigationMenuItem: MenuItem
        get() {
            val bottomNavigationView = binding.bottomNavigation
            val selectedItemId = bottomNavigationView.selectedItemId
            return bottomNavigationView.menu.findItem(selectedItemId)
        }

    private lateinit var startNavigationMenuItem: MenuItem

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    private val settingsViewModel: SettingsViewModel by viewModels()

    // ギャラリーから画像取得
    private val openDocumentResultLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { o: Uri? ->
        val showedFragment = showedFragment
        if (showedFragment is DiaryEditFragment) {
            showedFragment.attachPicture(o)
        }
    }

    @get:RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    internal val isGrantedPostNotifications
        get() = (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        )
                == PackageManager.PERMISSION_GRANTED)

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !isMainActivityLayoutInflated }
        setUpEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                settingsViewModel.isAllSettingsNotNull
                    .collectLatest { value: Boolean ->
                        if (!value) return@collectLatest

                        if (!isMainActivityLayoutInflated) setUpMainActivityBinding()
                        setUpThemeColor()
                        setUpNavigation()
                    }
            }
        }
    }

    // MEMO:EdgeToEdge対応。下記ページ参照。
    //      https://developer.android.com/develop/ui/views/layout/edge-to-edge?hl=ja
    //      https://developer.android.com/codelabs/edge-to-edge?hl=ja#2
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUpEdgeToEdge() {
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
    }

    private fun setUpMainActivityBinding() {
        val themeColor = settingsViewModel.themeColor.requireValue()
        val themeColorInflater = ThemeColorInflaterCreator().create(layoutInflater, themeColor)
        _binding = ActivityMainBinding.inflate(themeColorInflater)
        setContentView(binding.root)
        isMainActivityLayoutInflated = true
    }

    private fun setUpThemeColor() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.themeColor
                    .collectLatest { themeColor: ThemeColor? ->
                        themeColor ?: return@collectLatest

                        switchThemeColor(themeColor)
                    }
            }
        }
    }

    private fun switchThemeColor(themeColor: ThemeColor) {
        val changer = ThemeColorChanger()
        changer.applyStatusBarColor(window, themeColor)
        changer.applyBackgroundColor(binding.viewFullScreenBackground, themeColor)
        changer.applyToolbarColor(binding.materialToolbarTopAppBar, themeColor)
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
        val navController = navHostFragment.navController
        setupWithNavController(bottomNavigationView, navController)

        // ボトムナビゲーションのデフォルト選択アイテム情報取得
        startNavigationMenuItem = selectedBottomNavigationMenuItem

        setUpEnabledNavigationSwitchFunction()
        bottomNavigationView.setOnItemSelectedListener(CustomOnItemSelectedListener(navController))
        bottomNavigationView.setOnItemReselectedListener(CustomOnItemReselectedListener())
        navController.addOnDestinationChangedListener(
            BottomNavigationStateOnDestinationChangedListener()
        )
    }

    // MEMO:タブ選択で下記の様な画面遷移を行う時、Bを表示中にタブ選択でAを表示させようとすると、
    //      BのFragmentが消えた後、AのFragmentが表示されない不具合が生じる。
    //      (何も表示されない状態)
    //      これを回避するために、遷移先のFragmentが表示しきるまで、タブ選択できないようにする。
    //      Fragment A → B → A
    private fun setUpEnabledNavigationSwitchFunction() {
        navFragmentManager.registerFragmentLifecycleCallbacks(
            BottomNavigationEnabledSwitchCallbacks(),
            false
        )
    }

    private inner class BottomNavigationEnabledSwitchCallbacks :
        FragmentManager.FragmentLifecycleCallbacks() {

            init {
                // HACK:StartFragmentの最初のResumedの時点では、onFragmentResumed()が呼び出されない。
                //      理由は本クラスのインスタンスをActivity#onResume()よりも後に設定している為。
                //      本クラスはbinding変数を参照しているため、Activity#onCreate()で設定せずに、
                //      SettingViewModelの設定値の読込が完了してから設定している。
                //      有効状態から始めれるように初期化タイミングで有効処理するように対応。
                switchEnabledNavigation(true)
            }

            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentPaused(fm, f)
                if (isFragmentWithBottomNavigation(f)) switchEnabledNavigation(true)
            }

            override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
                super.onFragmentPaused(fm, f)
                if (isFragmentWithBottomNavigation(f)) switchEnabledNavigation(false)
            }

            private fun isFragmentWithBottomNavigation(f: Fragment): Boolean {
                return when(f) {
                    is DiaryListFragment,
                    is CalendarFragment,
                    is SettingsFragment -> true
                    else -> false
                }
            }

            private fun switchEnabledNavigation(isEnabled: Boolean) {
                val menu = binding.bottomNavigation.menu
                val size = menu.size()
                for (i in 0 until size) {
                    menu.getItem(i).setEnabled(isEnabled)
                }
            }
    }

    private inner class CustomOnItemSelectedListener(private val navController: NavController):
        NavigationBarView.OnItemSelectedListener {

        private var previousItemSelected: MenuItem

        init {
            previousItemSelected = selectedBottomNavigationMenuItem
        }

        // MEMO:下記動作を行った時にタブのアイコンが更新されない問題があるため、
        //      新しく"OnItemSelectedListener"をセットする。
        //      参考:https://inside.luchegroup.com/entry/2023/05/08/113236
        //      タブA切替 → タブA上で別fragmentへ切替 → タブB切替 → タブA切替(fragmentA表示)
        //      → タブAのアイコンが選択状態に更新されない
        //      デフォルトの"OnItemSelectedListener"はNavigationUI#onNavDestinationSelected()処理後、
        //      表示フラグメントと選択タブに設定されているフラグメントを比較し、同じでないと"true"が返されない。
        //      その為常時"true"が返す。
        //      (現在タブに設定されたFragmentから別Fragmentを表示する時は、BottomNavigationを非表示にしている為、
        //      本不具合は発生しないが、対策コードは残しておく。)
        override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
            // BottomNavigationのタブ選択による画面遷移
            if (previousItemSelected === menuItem) return true

            Log.i(logTag, "ボトムナビゲーション_フラグメント切替")
            wasSelectedTab = true
            previousItemSelected = menuItem

            setUpFragmentTransition()
            onNavDestinationSelected(menuItem, navController)
            return true
        }

        private fun setUpFragmentTransition() {
            // 表示中のFragmentを取得し、Transitionを設定
            val showedFragment = showedFragment
            showedFragment.exitTransition = MaterialFadeThrough()
            showedFragment.returnTransition = MaterialFadeThrough()

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
    }

    private inner class BottomNavigationStateOnDestinationChangedListener:
        NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            val motionResId =
                if (needsBottomNavigationView(controller, destination)) {
                    R.id.motion_scene_bottom_navigation_showed_state
                } else {
                    R.id.motion_scene_bottom_navigation_hided_state
                }
            binding.motionLayoutBottomNavigation.transitionToState(motionResId)
        }

        private fun needsBottomNavigationView(
            navController: NavController,
            navDestination: NavDestination
        ): Boolean {
            if (isFragment(navDestination)) {
                return (isFragmentWithBottomNavigation(navDestination))
            }

            // Fragment以外(Dialog)表示中は一つ前のFragmentを元に判断
            val previousNavBackStackEntry = checkNotNull(navController.previousBackStackEntry)
            val previousNavDestination = previousNavBackStackEntry.destination
            return isFragmentWithBottomNavigation(previousNavDestination)
        }

        private fun isFragment(navDestination: NavDestination): Boolean {
            return when (navDestination.id) {
                R.id.navigation_diary_list_fragment,
                R.id.navigation_calendar_fragment,
                R.id.navigation_settings_fragment,
                R.id.navigation_open_source_licenses_fragment,
                R.id.navigation_word_search_fragment,
                R.id.navigation_diary_show_fragment,
                R.id.navigation_diary_edit_fragment,
                R.id.navigation_diary_item_title_edit_fragment -> true
                else -> false
            }
        }

        private fun isFragmentWithBottomNavigation(navDestination: NavDestination): Boolean {
            // MEMO:下記理由より、対象FragmentはBottomNavigationViewの各タブ先頭のFragmentのみとする。
            //      ・標準のNavigation機能は各タブ毎にFragment状態を保存しない為。
            //        例)WordSearchFragment
            //              -> CalendarFragment
            //              -> WordSearchFragment(onCreate()から処理)
            //              ViewModelが初期化される為検索状態が保持されない。保持することも出来るが複雑になるため避ける。
            return when (navDestination.id) {
                R.id.navigation_diary_list_fragment,
                R.id.navigation_calendar_fragment,
                R.id.navigation_settings_fragment -> true
                else -> false
            }
        }
    }

    private inner class CustomOnItemReselectedListener : OnItemReselectedListener {
        override fun onNavigationItemReselected(menuItem: MenuItem) {
            val showedFragment = showedFragment

            Log.i(logTag, "ボトムナビゲーション_リセレクト")
            if (menuItem.toString() == getString(R.string.title_list)) {
                if (showedFragment !is DiaryListFragment) return

                showedFragment.onNavigationItemReselected()
            } else if (menuItem.toString() == getString(R.string.title_calendar)) {
                if (showedFragment !is CalendarFragment) return

                showedFragment.onNavigationItemReselected()
            }
        }
    }

    // BottomNavigationタブ選択による画面遷移の遷移先FragmentのTransition設定完了後用リセットメソッド
    internal fun clearWasSelectedTab() {
        wasSelectedTab = false
    }

    internal fun popBackStackToStartFragment() {
        binding.bottomNavigation.selectedItemId = startNavigationMenuItem.itemId
    }

    override fun onStart() {
        super.onStart()

        checkPermission()
    }

    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            settingsViewModel
                .onSetupReminderNotificationSettingFromPermission(isGrantedPostNotifications)
        }

        settingsViewModel
            .onSetupWeatherInfoAcquisitionSettingFromPermission(isGrantedAccessLocation())
    }

    internal fun loadPicturePath() {
        openDocumentResultLauncher.launch(arrayOf("image/*"))
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        isMainActivityLayoutInflated = false
    }
}
