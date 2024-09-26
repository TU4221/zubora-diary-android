package com.websarva.wings.android.zuboradiary;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
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
import com.websarva.wings.android.zuboradiary.ui.BaseThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.calendar.CalendarFragment;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListFragment;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchFragment;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.internal.Preconditions;

//  MEMO:GitHubトークン(有効期限20240408から30日後)_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

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // BottomNavigationタブによる画面遷移関係
    private boolean tabWasSelected = false;
    private MenuItem startItem;
    private MenuItem previousItemSelected;

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

        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        setUpViewModel();
        setUpLocationInformation();
        setUpThemeColor();

        //アクションバー設定
        //setSupportActionBar(this.binding.mtbMainToolbar);

        setUpNavigation();
    }

    private void setUpViewModel() {
        ViewModelProvider provider = new ViewModelProvider(this);
        settingsViewModel = provider.get(SettingsViewModel.class);
    }

    private void setUpLocationInformation() {
        LocationRequest.Builder builder =
                new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000);
        locationRequest = builder.build();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsViewModel.getIsCheckedGettingWeatherInformationLiveData()
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
                            updateLocationInformation();
                        } else {
                            settingsViewModel.clearLocationInformation();
                        }
                    }
                });
    }

    public boolean updateLocationInformation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // アプリ起動時に一回だけ取得
                settingsViewModel.updateLocationInformation(location.getLatitude(), location.getLongitude());
                fusedLocationProviderClient.removeLocationUpdates(this);
            }
        }, Looper.getMainLooper());
        return true;
    }

    private void setUpThemeColor() {
        settingsViewModel.getThemeColorSettingValueLiveData()
                .observe(this, new Observer<ThemeColor>() {
                    @Override
                    public void onChanged(ThemeColor themeColor) {
                        if (themeColor == null) {
                            return;
                        }

                        MainActivity.this.dialogThemeColor = themeColor;

                        BaseThemeColorSwitcher switcher =
                                new BaseThemeColorSwitcher(getApplicationContext(), themeColor);

                        switcher.switchStatusBarColor(getWindow());

                        switcher.switchBackgroundColor(binding.layoutBackground);

                        switcher.switchToolbarColor(binding.mtbMainToolbar);

                        switcher.switchBottomNavigationColor(binding.navView);
                    }
                });
    }

    private void setUpNavigation() {
        // Navigation設定
        // 参考:https://inside.luchegroup.com/entry/2023/05/08/113236
        BottomNavigationView navView = this.binding.navView;
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        // BottomNavigationのタブ選択による画面遷移
        // 現在選択中のボトムナビゲーションのアイテム情報取得
        int selectedItemId = navView.getSelectedItemId();
        this.startItem = navView.getMenu().findItem(selectedItemId);
        this.previousItemSelected = this.startItem;
        // MEMO:下記動作を行った時にタブのアイコンが更新されない問題があるため、
        //      新しく"OnItemSelectedListener"をセットする。
        //      参考:https://inside.luchegroup.com/entry/2023/05/08/113236
        //      タブA切替 → タブA上でfragmentAへ切替 → タブB切替 → タブA切替(fragmentA表示)
        //      → タブAのアイコンが選択状態に更新されない
        //      デフォルトの"OnItemSelectedListener"はNavigationUI#onNavDestinationSelected処理後、
        //      表示フラグメントと選択タブに設定されているフラグメントを比較し、同じでないと"true"が返されない。
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // BottomNavigationのタブ選択による画面遷移
                if (MainActivity.this.previousItemSelected != menuItem) {
                    MainActivity.this.tabWasSelected = true;
                    MainActivity.this.previousItemSelected = menuItem;

                    // 表示中のFragmentを取得し、Transitionを設定
                    NavHostFragment navHostFragment =
                            (NavHostFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.nav_host_fragment_activity_main);
                    Fragment fragment =
                            navHostFragment.getChildFragmentManager().getFragments().get(0);
                    fragment.setExitTransition(new MaterialFadeThrough());
                    fragment.setReturnTransition(new MaterialFadeThrough());

                    boolean bool = NavigationUI.onNavDestinationSelected(menuItem, navController);
                    // NavigationUI.onNavDestinationSelected()による、
                    // Fragment切替時の対象Transitionパターン表(StartDestination:A-1)
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


                    // MEMO:タブ選択で下記の様な画面遷移を行う時、Bを表示中にタブ選択でAを表示させようとすると、
                    //      BのFragmentが消えた後、AのFragmentが表示されない不具合が生じる。
                    //      (何も表示されない状態)
                    //      これを回避するために、遷移先のFragmentが表示しきるまで、タブ選択できないようにした。
                    //      Fragment A → B → A
                    switchEnabledBottomNavigation(false);
                }
                return true;
            }
        });
        // 上記不具合対策で無効にしたBottomNavigationを有効にする
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_main);
        FragmentManager fragmentManager = navHostFragment.getChildFragmentManager();
        // StartDestinationFragment用ナビゲーション有効オブサーバー設定
        fragmentManager.getFragments().get(0).getLifecycle()
                .addObserver(new LifecycleEventObserver() {
                    @Override
                    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner,
                                               @NonNull Lifecycle.Event event) {
                        if (event == Lifecycle.Event.ON_RESUME) {
                            switchEnabledBottomNavigation(true);
                        }
                    }
                });
        // StartDestinationFragment以外用ナビゲーション有効オブサーバー設定
        fragmentManager.addFragmentOnAttachListener(new FragmentOnAttachListener() {
            @Override
            public void onAttachFragment(@NonNull FragmentManager fragmentManager,
                                         @NonNull Fragment fragment) {
                fragment.getLifecycle().addObserver(new LifecycleEventObserver() {
                    @Override
                    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner,
                                               @NonNull Lifecycle.Event event) {
                        switchEnabledBottomNavigation(true);
                    }
                });
            }
        });

        // BottomNavigationの選択中タブ再選択によるFragment毎の処理
        navView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
                Log.d("ボトムナビゲーションタップ確認", menuItem.toString());

                // 表示中のFragmentを取得
                Fragment navHostFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_main);
                Fragment fragment = navHostFragment.getChildFragmentManager()
                        .getFragments().get(0);

                if (menuItem.toString().equals(getString(R.string.title_list))) {
                    if (fragment instanceof DiaryListFragment) {
                        DiaryListFragment diaryListFragment = (DiaryListFragment) fragment;
                        diaryListFragment.processOnReSelectNavigationItem();
                    } else if (fragment instanceof WordSearchFragment) {
                        WordSearchFragment wordSearchFragment = (WordSearchFragment) fragment;
                        wordSearchFragment.processOnReselectNavigationItem();
                    }
                }

                if (menuItem.toString().equals(getString(R.string.title_calendar))) {
                    if (fragment instanceof CalendarFragment) {
                        CalendarFragment calendarFragment = (CalendarFragment) fragment;
                        calendarFragment.processOnReselectNavigationItem();
                    }
                }


            }
        });

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
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
        });
    }



    public boolean getTabWasSelected() {
        return this.tabWasSelected;
    }

    // BottomNavigationタブ選択による画面遷移の遷移先FragmentのTransition設定完了後用リセットメソッド
    public void resetTabWasSelected() {
        this.tabWasSelected = false;
    }

    // NaviGraphのStartDestinationが含まれないタブの最初のFragmentで戻るボタンを押すと、
    // StartDestinationに戻る。BottomNavigationViewのタブ切り替えと同等の画面遷移を
    // 行う為に下記コード記述。
    public void preparePopBackStackToStartDestination() {
        this.tabWasSelected = true;
        this.previousItemSelected = this.startItem;
    }

    public void popBackStackToStartDestination() {
        binding.navView.setSelectedItemId(startItem.getItemId());
    }

    private void switchEnabledBottomNavigation(boolean isEnabled) {
        Menu menu = binding.navView.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            menu.getItem(i).setEnabled(isEnabled);
        }
    }

    private boolean needsBottomNavigationView(NavController navController, NavDestination navDestination) {
        Objects.requireNonNull(navDestination);

        if (isFragment(navDestination)) {
            return (isBottomNavigationFragment(navDestination));
        }

        // Fragment以外(Dialog)表示中は一つ前のFragmentを元に判断
        NavBackStackEntry previousNavBackStackEntry = navController.getPreviousBackStackEntry();
        Objects.requireNonNull(previousNavBackStackEntry);
        NavDestination previousNavDestination = previousNavBackStackEntry.getDestination();
        return isBottomNavigationFragment(previousNavDestination);
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

    private boolean isBottomNavigationFragment(NavDestination navDestination) {
        Objects.requireNonNull(navDestination);

        int navDestinationId = navDestination.getId();
        if (navDestinationId == R.id.navigation_diary_list_fragment) return true;
        if (navDestinationId == R.id.navigation_calendar_fragment) return true;
        return navDestinationId == R.id.navigation_settings_fragment;
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
