package com.websarva.wings.android.zuboradiary

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationBarView.OnItemReselectedListener
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.ActivityMainBinding
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.ThemeColorSwitcher
import com.websarva.wings.android.zuboradiary.ui.calendar.CalendarFragment
import com.websarva.wings.android.zuboradiary.ui.diary.diaryedit.DiaryEditFragment
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListFragment
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsFragment
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = checkNotNull(_binding)

    // BottomNavigationタブによる画面遷移関係
    var wasSelectedTab = false
        private set
    private lateinit var startNavigationMenuItem: MenuItem

    // ViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    // 位置情報取得
    private lateinit  var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    // ギャラリーから画像取得
    private val openDocumentResultLauncher = registerForActivityResult<Array<String>, Uri>(
        ActivityResultContracts.OpenDocument()
    ) { o: Uri? ->
        if (o == null) return@registerForActivityResult  // 未選択時

        val showedFragment = findShowedFragment()
        if (showedFragment is DiaryEditFragment) {
            showedFragment.attachPicture(o)
        }
    }

    @get:RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    val isGrantedPostNotifications: Boolean
        get() = (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        )
                == PackageManager.PERMISSION_GRANTED)

    val isGrantedAccessLocation: Boolean
        get() {
            val isGrantedAccessFineLocation =
                (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                        == PackageManager.PERMISSION_GRANTED)
            val isGrantedAccessCoarseLocation =
                (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                        == PackageManager.PERMISSION_GRANTED)
            return isGrantedAccessFineLocation && isGrantedAccessCoarseLocation
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpViewModel()
        setUpBinding()
        setUpLocationInfo()
        setUpThemeColor()
        setUpNavigation()
    }

    private fun setUpViewModel() {
        val provider = ViewModelProvider(this)
        settingsViewModel = provider[SettingsViewModel::class.java]
    }

    private fun setUpBinding() {
        val themeColor = settingsViewModel.loadThemeColorSettingValue()
        val creator = ThemeColorInflaterCreator(this, layoutInflater, themeColor)
        val themeColorInflater = creator.create()
        _binding = ActivityMainBinding.inflate(themeColorInflater)
        setContentView(binding.root)
    }

    private fun setUpLocationInfo() {
        val builder =
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000)
        locationRequest = builder.build()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        settingsViewModel.isCheckedWeatherInfoAcquisition
            .observe(this) { aBoolean: Boolean? ->
                var settingValue = aBoolean
                if (settingValue == null) {
                    settingValue = settingsViewModel.loadIsCheckedWeatherInfoAcquisitionSetting()
                }
                if (settingValue) {
                    updateLocationInformation()
                } else {
                    settingsViewModel.clearGeoCoordinates()
                }
            }
    }

    private fun updateLocationInformation() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    // アプリ起動時に一回だけ取得
                    val location = checkNotNull(locationResult.lastLocation)
                    val geoCoordinates =
                        GeoCoordinates(location.latitude, location.longitude)
                    settingsViewModel.updateGeoCoordinates(geoCoordinates)
                    fusedLocationProviderClient.removeLocationUpdates(this)
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun setUpThemeColor() {
        settingsViewModel.themeColor
            .observe(this) { themeColor: ThemeColor? ->
                var settingValue = themeColor
                if (settingValue == null) {
                    settingValue = settingsViewModel.loadThemeColorSettingValue()
                }
                switchThemeColor(settingValue)
            }
    }

    private fun switchThemeColor(themeColor: ThemeColor) {
        val switcher =
            ThemeColorSwitcher(applicationContext, themeColor)
        switcher.switchStatusBarColor(window)
        switcher.switchBackgroundColor(binding.viewFullScreenBackground)
        switcher.switchToolbarColor(binding.materialToolbarTopAppBar)
        switcher.switchBottomNavigationColor(binding.bottomNavigation)
    }

    private fun findNavHostFragment(): NavHostFragment {
        return checkNotNull(
            supportFragmentManager.findFragmentById(R.id.fragment_nav_host)
        ) as NavHostFragment
    }

    private fun findNavFragmentManager(): FragmentManager {
        val navHostFragment = findNavHostFragment()
        return navHostFragment.childFragmentManager
    }

    private fun findShowedFragment(): Fragment {
        val fragmentManager = findNavFragmentManager()
        val fragmentList = fragmentManager.fragments
        return checkNotNull(fragmentList[0])
    }

    private fun findSelectedBottomNavigationMenuItem(): MenuItem {
        val bottomNavigationView = binding.bottomNavigation
        val selectedItemId = bottomNavigationView.selectedItemId
        return bottomNavigationView.menu.findItem(selectedItemId)
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
        startNavigationMenuItem = findSelectedBottomNavigationMenuItem()

        bottomNavigationView.setOnItemSelectedListener(CustomOnItemSelectedListener(navController))
        bottomNavigationView.setOnItemReselectedListener(CustomOnItemReselectedListener())
        navController.addOnDestinationChangedListener(
            BottomNavigationStateOnDestinationChangedListener()
        )
    }

    private inner class CustomOnItemSelectedListener(private val navController: NavController):
        NavigationBarView.OnItemSelectedListener {

        private var previousItemSelected: MenuItem

        init {
            previousItemSelected = findSelectedBottomNavigationMenuItem()
            setUpEnabledNavigationSwitchFunction()
        }

        // MEMO:タブ選択で下記の様な画面遷移を行う時、Bを表示中にタブ選択でAを表示させようとすると、
        //      BのFragmentが消えた後、AのFragmentが表示されない不具合が生じる。
        //      (何も表示されない状態)
        //      これを回避するために、遷移先のFragmentが表示しきるまで、タブ選択できないようにする。
        //      Fragment A → B → A
        fun setUpEnabledNavigationSwitchFunction() {
            // 上記不具合対策で無効にしたBottomNavigationを有効にする
            // StartDestinationFragment用ナビゲーション有効オブサーバー設定
            findShowedFragment().lifecycle.addObserver(EnabledNavigationLifecycleEventObserver())
            // StartDestinationFragment以外用ナビゲーション有効オブサーバー設定
            findNavFragmentManager().addFragmentOnAttachListener { _, fragment: Fragment ->
                // MEMO:BottomNavigationタブに割り当てられているFragment以外は処理不要
                //      Dialogを表示した時は背面FragmentのLifecycleEventが"OnResume"のままとなるため、
                //      DialogにEnabledNavigationLifecycleEventObserverクラスをセットすると
                //      BottomNavigationが無効状態のままとなる。
                if (fragment !is DiaryListFragment
                    && fragment !is CalendarFragment
                    && fragment !is SettingsFragment) {
                    return@addFragmentOnAttachListener
                }
                fragment.lifecycle.addObserver(EnabledNavigationLifecycleEventObserver())
            }
        }

        private inner class EnabledNavigationLifecycleEventObserver : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                Log.d("20241107", "LifecycleOwner$source")
                Log.d("20241107", "LifecycleEvent$event")
                if (event != Lifecycle.Event.ON_RESUME) {
                    switchEnabledNavigation(false)
                    return
                }
                switchEnabledNavigation(true)
            }
        }

        fun switchEnabledNavigation(isEnabled: Boolean) {
            val menu = binding.bottomNavigation.menu
            val size = menu.size()
            for (i in 0 until size) {
                menu.getItem(i).setEnabled(isEnabled)
            }
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

            wasSelectedTab = true
            previousItemSelected = menuItem

            setUpFragmentTransition()
            onNavDestinationSelected(menuItem, navController)

            return true
        }

        fun setUpFragmentTransition() {
            // 表示中のFragmentを取得し、Transitionを設定
            val fragment = findShowedFragment()
            fragment.exitTransition = MaterialFadeThrough()
            fragment.returnTransition = MaterialFadeThrough()

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

        fun needsBottomNavigationView(
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

        fun isFragment(navDestination: NavDestination): Boolean {
            val navDestinationId = navDestination.id
            if (navDestinationId == R.id.navigation_diary_list_fragment) return true
            if (navDestinationId == R.id.navigation_calendar_fragment) return true
            if (navDestinationId == R.id.navigation_settings_fragment) return true
            if (navDestinationId == R.id.navigation_word_search_fragment) return true
            if (navDestinationId == R.id.navigation_diary_show_fragment) return true
            if (navDestinationId == R.id.navigation_diary_edit_fragment) return true
            return navDestinationId == R.id.navigation_diary_item_title_edit_fragment
        }

        fun isFragmentWithBottomNavigation(navDestination: NavDestination): Boolean {
            // MEMO:下記理由より、対象FragmentはBottomNavigationViewの各タブ先頭のFragmentのみとする。
            //      ・標準のNavigation機能は各タブ毎にFragment状態を保存しない為。
            //        例)WordSearchFragment
            //              -> CalendarFragment
            //              -> WordSearchFragment(onCreate()から処理)
            //              ViewModelが初期化される為検索状態が保持されない。保持することも出来るが複雑になるため避ける。
            val navDestinationId = navDestination.id
            if (navDestinationId == R.id.navigation_diary_list_fragment) return true
            if (navDestinationId == R.id.navigation_calendar_fragment) return true
            return navDestinationId == R.id.navigation_settings_fragment
        }
    }

    private inner class CustomOnItemReselectedListener : OnItemReselectedListener {
        override fun onNavigationItemReselected(menuItem: MenuItem) {
            val fragment = findShowedFragment()

            if (menuItem.toString() == getString(R.string.title_list)) {
                if (fragment !is DiaryListFragment) return

                fragment.processOnReSelectNavigationItem()
            } else if (menuItem.toString() == getString(R.string.title_calendar)) {
                if (fragment !is CalendarFragment) return

                fragment.processOnReselectNavigationItem()
            }
        }
    }

    // BottomNavigationタブ選択による画面遷移の遷移先FragmentのTransition設定完了後用リセットメソッド
    fun clearWasSelectedTab() {
        wasSelectedTab = false
    }

    fun popBackStackToStartFragment() {
        binding.bottomNavigation.selectedItemId = startNavigationMenuItem.itemId
    }

    public override fun onStart() {
        super.onStart()

        checkPermission()
    }

    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isGrantedPostNotifications) settingsViewModel.saveReminderNotificationInvalid()
        }
        if (!isGrantedAccessLocation) settingsViewModel.saveWeatherInfoAcquisition(false)
    }

    fun loadPicturePath() {
        openDocumentResultLauncher.launch(arrayOf("image/*"))
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}
