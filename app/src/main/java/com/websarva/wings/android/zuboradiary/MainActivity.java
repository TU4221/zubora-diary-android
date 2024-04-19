package com.websarva.wings.android.zuboradiary;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationBarView;
import com.websarva.wings.android.zuboradiary.databinding.ActivityMainBinding;
import com.websarva.wings.android.zuboradiary.ui.calendar.CalendarFragment;
import com.websarva.wings.android.zuboradiary.ui.list.ListFragment;

//  MEMO:GitHubトークン(有効期限20240408から30日後)_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

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Keyboardクラス設定
        Keyboard.setInputMethodManager(this);

        //アクションバー設定
        Toolbar mainToolbar = binding.mtbMainToolbar;
        setSupportActionBar(mainToolbar);

        // Navigation設定
        // 参考:https://inside.luchegroup.com/entry/2023/05/08/113236
        BottomNavigationView navView = binding.navView;

        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // HACK:ナビゲーションによるアクションバータイトル更新不要。
        //      デフォルトで記述してあり、現時点では他に機能しているか判断できないためコメントアウトで機能停止。
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_list, R.id.navigation_calendar,
                R.id.navigation_notifications
        ).build();
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // MEMO:下記動作を行った時にタブのアイコンが更新されない問題があるため、
        //      新しく"OnItemSelectedListener"をセットする。
        //      参考:https://inside.luchegroup.com/entry/2023/05/08/113236
        //      タブA切替 → タブA上でfragmentAへ切替 → タブB切替 → タブA切替(fragmentA表示)
        //      → タブAのアイコンが選択状態に更新されない
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // デフォルトの"OnItemSelectedListener"は下記メソッド処理後、
                // 表示フラグメントと選択タブに設定されているフラグメントを比較し、同じでないと"true"が返されない。
                NavigationUI.onNavDestinationSelected(menuItem, navController);
                return true;
            }
        });
        navView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
                Log.d("ボトムナビゲーションタップ確認", menuItem.toString());

                //NavHostFragmentを取得し、そこからListFragmentを取得
                Fragment navHostFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_main);
                Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);

                if (menuItem.toString().equals(getString(R.string.title_list))) {
                    if (fragment != null && fragment instanceof ListFragment) {
                        ListFragment listFragment = (ListFragment) fragment;
                        listFragment.diaryListScrollToFirstPosition();
                    }
                }

                if (menuItem.toString().equals(getString(R.string.title_calendar))) {
                    if (fragment != null && fragment instanceof CalendarFragment) {
                        CalendarFragment calendarFragment = (CalendarFragment) fragment;
                        calendarFragment.onNavigationItemReselected();
                    }
                }
            }
        });
    }
}
