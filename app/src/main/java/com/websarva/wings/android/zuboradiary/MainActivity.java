package com.websarva.wings.android.zuboradiary;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.websarva.wings.android.zuboradiary.databinding.ActivityMainBinding;
import com.websarva.wings.android.zuboradiary.ui.calendar.CalendarFragment;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListFragment;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import dagger.hilt.android.AndroidEntryPoint;

//  MEMO:GitHubトークン(有効期限20240408から30日後)_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

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // BottomNavigationタブによる画面遷移関係
    private boolean tabWasSelected = false;
    private MenuItem startItem;
    private MenuItem previousItemSelected;

    // BottomNavigationView表示/非表示切り替え関係
    private int bottomNavigationDefaultHigh;
    private boolean bottomNavigationIsHided = false;

    // ViewModel
    private SettingsViewModel settingsViewModel;

    // 位置情報取得
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        setUpViewModel();
        setUpLocationInformation();

        //アクションバー設定
        //setSupportActionBar(this.binding.mtbMainToolbar);

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
                    Menu menu = navView.getMenu();
                    int size = menu.size();
                    for (int i = 0; i < size; i++) {
                        menu.getItem(i).setEnabled(false);
                    }
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
                            enableBottomBottomNavigation();
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
                        enableBottomBottomNavigation();
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
                        diaryListFragment.diaryListScrollToFirstPosition();
                    }
                }

                if (menuItem.toString().equals(getString(R.string.title_calendar))) {
                    if (fragment instanceof CalendarFragment) {
                        CalendarFragment calendarFragment = (CalendarFragment) fragment;
                        calendarFragment.onNavigationItemReselected();
                    }
                }


            }
        });

        // BottomNavigationView表示/非表示切り替え設定
        this.binding.navView.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.bottomNavigationDefaultHigh =
                        MainActivity.this.binding.navView.getHeight();
            }
        });
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController,
                                             @NonNull NavDestination navDestination,
                                             @Nullable Bundle bundle) {
                if (isNoBottomNavigationFragment(navDestination)) {
                    if (!MainActivity.this.bottomNavigationIsHided) {
                        if (true) {
                            MainActivity.this.binding.navView.setTransitionVisibility(View.GONE);
                            //MainActivity.this.binding.navView.setVisibility(View.GONE);
                            MainActivity.this.bottomNavigationIsHided = true;
                            return;
                        }
                        ValueAnimator anim= ValueAnimator
                                .ofInt(
                                        MainActivity.this.bottomNavigationDefaultHigh,
                                        0
                                );
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                                ViewGroup.LayoutParams layoutParams =
                                        MainActivity.this.binding.navView.getLayoutParams();
                                layoutParams.height = (int) animation.getAnimatedValue();
                                MainActivity.this.binding.navView.setLayoutParams(layoutParams);
                            }
                        });
                        anim.setDuration(300); // 画面遷移時のDuration値
                        anim.start();
                        //MainActivity.this.binding.navView.setVisibility(View.GONE);
                        MainActivity.this.bottomNavigationIsHided = true;
                    }

                } else {
                    if (MainActivity.this.bottomNavigationIsHided) {
                        if (true) {
                            MainActivity.this.binding.navView.setTransitionVisibility(View.VISIBLE);
                            //MainActivity.this.binding.navView.setVisibility(View.VISIBLE);
                            MainActivity.this.bottomNavigationIsHided = false;
                            return;
                        }
                        ValueAnimator anim= ValueAnimator
                                .ofInt(0, MainActivity.this.bottomNavigationDefaultHigh);
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                                ViewGroup.LayoutParams layoutParams =
                                        MainActivity.this.binding.navView.getLayoutParams();
                                layoutParams.height = (int) animation.getAnimatedValue();
                                MainActivity.this.binding.navView.setLayoutParams(layoutParams);

                            }
                        });
                        anim.setDuration(300); // 画面遷移時のDuration値
                        anim.start();
                        //MainActivity.this.binding.navView.setVisibility(View.VISIBLE);
                        MainActivity.this.bottomNavigationIsHided = false;
                    }
                }
            }
        });
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

    private void enableBottomBottomNavigation() {
        Menu menu = this.binding.navView.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            menu.getItem(i).setEnabled(true);
        }
    }

    private boolean isNoBottomNavigationFragment(@NonNull NavDestination navDestination) {
        return navDestination.getId() == R.id.navigation_show_diary_fragment
                || navDestination.getId() == R.id.navigation_edit_diary_fragment
                || navDestination.getId() == R.id.navigation_date_picker_dialog_for_edit_diary_fragment
                || navDestination.getId() == R.id.navigation_delete_confirmation_dialog_for_edit_diary_fragment
                || navDestination.getId() == R.id.navigation_load_existing_diary_dialog_for_edit_diary_fragment
                || navDestination.getId() == R.id.navigation_update_existing_diary_dialog_for_edit_diary_fragment
                || navDestination.getId() == R.id.navigation_edit_diary_select_item_title_fragment
                || navDestination.getId() == R.id.navigation_delete_confirmation_dialog_for_edit_diary_select_item_title_fragment;
    }
}
