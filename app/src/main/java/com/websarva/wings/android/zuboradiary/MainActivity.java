package com.websarva.wings.android.zuboradiary;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.ActivityMainBinding;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator;
import com.websarva.wings.android.zuboradiary.ui.calendar.CalendarFragment;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListFragment;
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.internal.Preconditions;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // BottomNavigationタブによる画面遷移関係
    private boolean wasSelectedTab = false;
    private MenuItem startNavigationMenuItem;

    // ViewModel
    private SettingsViewModel settingsViewModel;

    // 位置情報取得
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    // Dialog用ThemeColor
    private ThemeColor dialogThemeColor = ThemeColor.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpViewModel();
        setUpBinding();
        setUpLocationInformation();
        setUpThemeColor();
        setUpNavigation();
    }

    private void setUpViewModel() {
        ViewModelProvider provider = new ViewModelProvider(this);
        settingsViewModel = provider.get(SettingsViewModel.class);
    }

    private void setUpBinding() {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        ThemeColorInflaterCreator creator =
                new ThemeColorInflaterCreator(this, getLayoutInflater(), themeColor);
        LayoutInflater themeColorInflater = creator.create();
        binding = ActivityMainBinding.inflate(themeColorInflater);
        setContentView(binding.getRoot());
    }

    private void setUpLocationInformation() {
        LocationRequest.Builder builder =
                new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000);
        locationRequest = builder.build();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsViewModel.getIsCheckedWeatherInfoAcquisitionLiveData()
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {
                        Boolean settingValue = aBoolean;
                        if (settingValue == null) {
                            settingValue = settingsViewModel.isCheckedWeatherInfoAcquisitionSetting();
                        }

                        if (settingValue) {
                            updateLocationInformation();
                        } else {
                            settingsViewModel.clearGeoCoordinates();
                        }
                    }
                });
    }

    private void updateLocationInformation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // アプリ起動時に一回だけ取得
                GeoCoordinates geoCoordinates =
                        new GeoCoordinates(location.getLatitude(), location.getLongitude());
                settingsViewModel.updateGeoCoordinates(geoCoordinates);
                fusedLocationProviderClient.removeLocationUpdates(this);
            }
        }, Looper.getMainLooper());
    }

    private void setUpThemeColor() {
        settingsViewModel.getThemeColorSettingValueLiveData()
                .observe(this, new Observer<ThemeColor>() {
                    @Override
                    public void onChanged(@Nullable ThemeColor themeColor) {
                        ThemeColor settingValue = themeColor;
                        if (settingValue == null) {
                            settingValue = settingsViewModel.loadThemeColorSettingValue();
                        };

                        switchThemeColor(settingValue);
                    }
                });
    }

    private void switchThemeColor(ThemeColor themeColor) {
        Objects.requireNonNull(themeColor);

        dialogThemeColor = themeColor;

        ThemeColorSwitcher switcher =
                new ThemeColorSwitcher(getApplicationContext(), themeColor);
        switcher.switchStatusBarColor(getWindow());
        switcher.switchBackgroundColor(binding.viewFullScreenBackground);
        switcher.switchToolbarColor(binding.mtbMainToolbar);
        switcher.switchBottomNavigationColor(binding.navView);
    }

    @NonNull
    private NavHostFragment findNavHostFragment() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_main);
        Objects.requireNonNull(navHostFragment);
        return navHostFragment;
    }

    @NonNull
    private FragmentManager findNavFragmentManager() {
        NavHostFragment navHostFragment = findNavHostFragment();
        return navHostFragment.getChildFragmentManager();
    }

    @NonNull
    private Fragment findShowedFragment() {
        FragmentManager fragmentManager = findNavFragmentManager();
        List<Fragment> fragmentList  = fragmentManager.getFragments();
        Fragment fragment = fragmentList.get(0);
        Objects.requireNonNull(fragment);
        return fragment;
    }

    @NonNull
    private MenuItem findSelectedBottomNavigationMenuItem() {
        BottomNavigationView navView = binding.navView;
        int selectedItemId = navView.getSelectedItemId();
        return navView.getMenu().findItem(selectedItemId);
    }
    
    private void setUpNavigation() {
        // Navigation設定
        // 参考:https://inside.luchegroup.com/entry/2023/05/08/113236
        BottomNavigationView navView = binding.navView;
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        // ボトムナビゲーションのデフォルト選択アイテム情報取得
        startNavigationMenuItem = findSelectedBottomNavigationMenuItem();

        navView.setOnItemSelectedListener(new CustomOnItemSelectedListener(navController));
        navView.setOnItemReselectedListener(new CustomOnItemReselectedListener());
        navController.addOnDestinationChangedListener(new BottomNavigationStateOnDestinationChangedListener());
    }

    private class CustomOnItemSelectedListener implements NavigationBarView.OnItemSelectedListener {

        private final NavController navController;

        private MenuItem previousItemSelected;

        private CustomOnItemSelectedListener(NavController navController) {
            Objects.requireNonNull(navController);

            this.navController = navController;
            previousItemSelected = findSelectedBottomNavigationMenuItem();
            setUpEnabledNavigationSwitchFunction();
        }

        // MEMO:タブ選択で下記の様な画面遷移を行う時、Bを表示中にタブ選択でAを表示させようとすると、
        //      BのFragmentが消えた後、AのFragmentが表示されない不具合が生じる。
        //      (何も表示されない状態)
        //      これを回避するために、遷移先のFragmentが表示しきるまで、タブ選択できないようにする。
        //      Fragment A → B → A
        private void setUpEnabledNavigationSwitchFunction() {
            // 上記不具合対策で無効にしたBottomNavigationを有効にする
            // StartDestinationFragment用ナビゲーション有効オブサーバー設定
            findShowedFragment().getLifecycle().addObserver(new EnabledNavigationLifecycleEventObserver());
            // StartDestinationFragment以外用ナビゲーション有効オブサーバー設定
            findNavFragmentManager().addFragmentOnAttachListener(new FragmentOnAttachListener() {
                @Override
                public void onAttachFragment(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment) {
                    fragment.getLifecycle().addObserver(new EnabledNavigationLifecycleEventObserver());
                }
            });
        }
        
        private class EnabledNavigationLifecycleEventObserver implements LifecycleEventObserver {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner,
                    @NonNull Lifecycle.Event event) {
                if (event != Lifecycle.Event.ON_RESUME) {
                    switchEnabledNavigation(false);
                    return;
                }
                switchEnabledNavigation(true);
            }
        }

        private void switchEnabledNavigation(boolean isEnabled) {
            Menu menu = binding.navView.getMenu();
            int size = menu.size();
            for (int i = 0; i < size; i++) {
                menu.getItem(i).setEnabled(isEnabled);
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
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            // BottomNavigationのタブ選択による画面遷移
            if (previousItemSelected == menuItem) return true;

            wasSelectedTab = true;
            previousItemSelected = menuItem;

            setUpFragmentTransition();
            NavigationUI.onNavDestinationSelected(menuItem, navController);

            return true;
        }

        private void setUpFragmentTransition() {
            // 表示中のFragmentを取得し、Transitionを設定
            Fragment fragment = findShowedFragment();
            fragment.setExitTransition(new MaterialFadeThrough());
            fragment.setReturnTransition(new MaterialFadeThrough());

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

    private class BottomNavigationStateOnDestinationChangedListener implements NavController.OnDestinationChangedListener {
        @Override
        public void onDestinationChanged(@NonNull NavController navController,
                @NonNull NavDestination navDestination,
                @Nullable Bundle bundle) {
            int motionResId;
            if (needsBottomNavigationView(navController, navDestination)) {
                motionResId = R.id.motion_scene_bottom_navigation_showed_state;
            } else {
                motionResId = R.id.motion_scene_bottom_navigation_hided_state;
            }
            binding.motionLayoutBottomNavigation.transitionToState(motionResId);
        }

        private boolean needsBottomNavigationView(NavController navController, NavDestination navDestination) {
            Objects.requireNonNull(navController);
            Objects.requireNonNull(navDestination);

            if (isFragment(navDestination)) {
                return (isFragmentWithBottomNavigation(navDestination));
            }

            // Fragment以外(Dialog)表示中は一つ前のFragmentを元に判断
            NavBackStackEntry previousNavBackStackEntry = navController.getPreviousBackStackEntry();
            Objects.requireNonNull(previousNavBackStackEntry);
            NavDestination previousNavDestination = previousNavBackStackEntry.getDestination();
            return isFragmentWithBottomNavigation(previousNavDestination);
        }

        private boolean isFragment(NavDestination navDestination) {
            Objects.requireNonNull(navDestination);

            int navDestinationId = navDestination.getId();
            if (navDestinationId == R.id.navigation_diary_list_fragment) return true;
            if (navDestinationId == R.id.navigation_calendar_fragment) return true;
            if (navDestinationId == R.id.navigation_settings_fragment) return true;
            if (navDestinationId == R.id.navigation_word_search_fragment) return true;
            if (navDestinationId == R.id.navigation_diary_show_fragment) return true;
            if (navDestinationId == R.id.navigation_diary_edit_fragment) return true;
            return navDestinationId == R.id.navigation_diary_item_title_edit_fragment;
        }

        private boolean isFragmentWithBottomNavigation(NavDestination navDestination) {
            Objects.requireNonNull(navDestination);

            // MEMO:下記理由より、対象FragmentはBottomNavigationViewの各タブ先頭のFragmentのみとする。
            //      ・標準のNavigation機能は各タブ毎にFragment状態を保存しない為。
            //        例)WordSearchFragment
            //              -> CalendarFragment
            //              -> WordSearchFragment(onCreate()から処理)
            //              ViewModelが初期化される為検索状態が保持されない。保持することも出来るが複雑になるため避ける。
            int navDestinationId = navDestination.getId();
            if (navDestinationId == R.id.navigation_diary_list_fragment) return true;
            if (navDestinationId == R.id.navigation_calendar_fragment) return true;
            return navDestinationId == R.id.navigation_settings_fragment;
        }
    }

    private class CustomOnItemReselectedListener implements NavigationBarView.OnItemReselectedListener {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
            Fragment fragment = findShowedFragment();

            if (menuItem.toString().equals(getString(R.string.title_list))) {
                if (!(fragment instanceof DiaryListFragment)) return;

                DiaryListFragment diaryListFragment = (DiaryListFragment) fragment;
                diaryListFragment.processOnReSelectNavigationItem();

            } else if (menuItem.toString().equals(getString(R.string.title_calendar))) {
                if (!(fragment instanceof CalendarFragment)) return;

                CalendarFragment calendarFragment = (CalendarFragment) fragment;
                calendarFragment.processOnReselectNavigationItem();
            }
        }
    }

    public boolean getWasSelectedTab() {
        return this.wasSelectedTab;
    }

    // BottomNavigationタブ選択による画面遷移の遷移先FragmentのTransition設定完了後用リセットメソッド
    public void clearWasSelectedTab() {
        this.wasSelectedTab = false;
    }

    public void popBackStackToStartFragment() {
        binding.navView.setSelectedItemId(startNavigationMenuItem.getItemId());
    }

    /**
     * DialogFragment用メソッド
     * */
    // HACK:各FragmentからDialogへThemeColorを渡すのは冗長になるため。
    @NonNull
    public ThemeColor requireDialogThemeColor() {
        Preconditions.checkNotNull(dialogThemeColor);
        return dialogThemeColor;
    }
}
