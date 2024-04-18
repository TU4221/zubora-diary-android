package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.google.android.material.transition.MaterialSharedAxis;
import com.websarva.wings.android.zuboradiary.Keyboard;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.UnitConverter;
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding;

public class WordSearchFragment extends Fragment {

    private FragmentWordSearchBinding binding;

    private InputMethodManager inputMethodManager;

    public WordSearchFragment() {
        // Required empty public constructor
    }

@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 戻るボタン押下時の処理
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        backFragment();

                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // データバインディング設定
        this.binding =
                FragmentWordSearchBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();

        // クラスフィールド初期化
        this.inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(
                new MenuProvider() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public void onCreateMenu(
                            @NonNull Menu menu, @NonNull MenuInflater menuInflater) {

                        menuInflater.inflate(R.menu.word_search_toolbar_menu, menu);

                        ActionBar actionBar = ((AppCompatActivity) requireActivity())
                                .getSupportActionBar();
                        actionBar.setTitle(null);
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        actionBar.setHomeAsUpIndicator(null);
                        actionBar.getCustomView();

                        // 検索欄設定
                        MenuItem menuItem = menu.findItem(R.id.word_search_toolbar_menu_search);
                        SearchView search = (SearchView) menuItem.getActionView();
                        search.setIconifiedByDefault(false); // "false"でバー状態を常時表示
                        int color = getResources().getColor(R.color.white);
                        search.setBackgroundColor(color);
                        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                Log.d("20240418", "Focus");

                                FrameLayout frameLayoutBackground = binding.frameLayoutBackground;
                                frameLayoutBackground.setOnTouchListener(
                                        new View.OnTouchListener() {
                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {
                                                Keyboard.hide(v);
                                                search.clearFocus();

                                                return false;
                                            }
                                        }
                                );
                            }
                        });

                        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                return false;
                            }
                        });

                        // 検索欄横幅変更
                        // HACK:アクションバーのメニューアイコンはLayoutParamsのMarginを変更しても
                        //      変化しないのでX位置を変更して右余白を作成。)
                        //      代わりにアイコン本体が変更した分見切れてしまう。
                        //      その為検索欄をアクションバーの水平中央に移動させようとすると、おおきく見切れてしまう
                        //      ので右余白を作成した状態で検索欄の横幅を大きくして対応。
                        //      検索欄をアクションバー上に常時表示させようとすると、本来は個別でアクションバーに
                        //      依存しないツールバーを用意する必要があるのかもしれない。
                        ViewTreeObserver viewTreeObserver = search.getViewTreeObserver();
                        viewTreeObserver.addOnGlobalLayoutListener(
                                new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        ViewGroup.LayoutParams searchLayoutParams =
                                                                        search.getLayoutParams();
                                        ViewGroup.MarginLayoutParams marginLayoutParams =
                                                (ViewGroup.MarginLayoutParams) searchLayoutParams;

                                        //検索欄右余白設定
                                        float rightMargin =
                                                UnitConverter.convertPx(16, getContext());
                                        search.setX(- rightMargin); //初期位置が基準の為、"-"とする。

                                        //検索欄横幅設定
                                        marginLayoutParams.width = (int) (
                                                //画面横幅
                                                getView().getWidth()
                                                //戻るボタン横幅
                                                - UnitConverter.convertPx(48, getContext())
                                                //検索欄右余白
                                                - UnitConverter.convertPx(16, getContext())
                                        );
                                        search.setLayoutParams(marginLayoutParams);

                                        ViewTreeObserver _viewTreeObserver =
                                                search.getViewTreeObserver();
                                        _viewTreeObserver.removeOnGlobalLayoutListener(this);

                                    }
                                }
                        );
                        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                return false;
                            }
                        });


                    }

                    @Override
                    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                        if (menuItem.getItemId() == android.R.id.home) {
                            backFragment();
                            return true;
                        }

                        return false;
                    }
                },
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED
        );
    }

    public void backFragment() {
        // TODO:前フラグメントへの処理有無後回し

        // HACK:キーボード表示状態で戻るボタンを押下すると、キーボドが表示したまま戻ってしまう。
        //      SearchView用キーボードだから？
        Keyboard.hide(getView()); // とりあえずフラグメントのレイアウトルートビューを代入

        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        NavController navController = navHostFragment.getNavController();
        navController.popBackStack();

    }


}
