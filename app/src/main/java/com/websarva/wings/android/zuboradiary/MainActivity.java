package com.websarva.wings.android.zuboradiary;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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


        //MainActivityでアクションバーを使用したくないので、空のツールバーをセット。
        Toolbar mainToolbar = binding.mtbMainToolbar;
        setSupportActionBar(mainToolbar);

        BottomNavigationView navView = binding.navView;
        navView.setOnItemReselectedListener(new NavViewOnTabSelectedListener());

        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // HACK:ナビゲーションによるアクションバータイトル更新不要。
        //      デフォルトで記述してあり、現時点では他に機能しているか判断できないためコメントアウトで機能停止。
        /*AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_list, R.id.navigation_calendar,
                R.id.navigation_notifications
        ).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);*/
        NavigationUI.setupWithNavController(navView, navController);
    }

    public class NavViewOnTabSelectedListener implements NavigationBarView.OnItemReselectedListener {
        @Override
        public void onNavigationItemReselected(MenuItem item) {
            Log.d("ボトムナビゲーションタップ確認", item.toString());

            //NavHostFragmentを取得し、そこからListFragmentを取得
            Fragment navHostFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_activity_main);
            Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);

            if (item.toString().equals(getString(R.string.title_list))) {
                if (fragment != null && fragment instanceof ListFragment) {
                    ListFragment listFragment = (ListFragment) fragment;
                    listFragment.diaryListScrollToFirstPosition();
                }
            }

            if (item.toString().equals(getString(R.string.title_calendar))) {
                if (fragment != null && fragment instanceof CalendarFragment) {
                    CalendarFragment calendarFragment = (CalendarFragment) fragment;
                    calendarFragment.onNavigationItemReselected();
                }
            }
        }
    }
}
