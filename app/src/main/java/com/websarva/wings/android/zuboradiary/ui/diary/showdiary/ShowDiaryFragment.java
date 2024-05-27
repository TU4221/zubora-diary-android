package com.websarva.wings.android.zuboradiary.ui.diary.showdiary;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.FragmentShowDiaryBinding;
import com.websarva.wings.android.zuboradiary.ui.calendar.CalendarFragmentDirections;
import com.websarva.wings.android.zuboradiary.ui.editdiary.DiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.editdiaryselectitemtitle.EditDiarySelectItemTitleFragment;

public class ShowDiaryFragment extends Fragment {

    // View関係
    private FragmentShowDiaryBinding binding;

    // Navigation関係
    private NavController navController;
    private static final String fromClassName = "From" + ShowDiaryFragment.class.getName();
    public static final String KEY_SHOWED_DIARY_DATE = "ShowedDiaryDate" + fromClassName;

    // ViewModel
    private DiaryViewModel diaryViewModel;

    // TODO:削除予定
    // Menu関係
    private MenuProvider showDiaryMenuProvider = new ShowDiaryMenuProvider();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.diaryViewModel = provider.get(DiaryViewModel.class);

        // Navigation設定
        this.navController = NavHostFragment.findNavController(this);

        // 戻るボタン押下時の処理
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        backFragment(false);

                    }
                }
        );

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        this.binding = FragmentShowDiaryBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        this.binding.setLifecycleOwner(this);
        this.binding.setDiaryViewModel(this.diaryViewModel);

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO:削除予定
        // アクションバーオプションメニュー更新。
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(
                showDiaryMenuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);



        // 画面表示データ準備
        this.diaryViewModel.prepareShowDiary();


        // 天気入力欄設定
        // MEMO:ShowDiaryFragmentの中身を一つのレイアウトとして独立させた。
        //      bindingで中身のViewを取得できなくなった為、findViewById()メソッドを使用してViewを取得。
        this.diaryViewModel.getLiveIntWeather1()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        ShowDiaryFragment.this.diaryViewModel.updateStrWeather1();
                    }
                });

        this.diaryViewModel.getLiveIntWeather2()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        TextView textWeatherSlush =
                                ShowDiaryFragment.this.binding.includeShowDiary.textWeatherSlush;
                        TextView textWeather2Selected =
                                ShowDiaryFragment.this.binding.includeShowDiary.textWeather2Selected;
                        if (integer != 0) {
                            textWeatherSlush.setVisibility(View.VISIBLE);
                            textWeather2Selected.setVisibility(View.VISIBLE);
                            ShowDiaryFragment.this.diaryViewModel.updateStrWeather2();
                        } else {
                            textWeatherSlush.setVisibility(View.GONE);
                            textWeather2Selected.setVisibility(View.GONE);
                        }
                    }
                });


        // 気分入力欄設定
        this.diaryViewModel.getLiveIntCondition()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        ShowDiaryFragment.this.diaryViewModel.updateStrCondition();
                    }
                });
    }

    // TODO:削除予定
    private class ShowDiaryMenuProvider implements MenuProvider {
            //アクションバーオプションメニュー設定。
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

                //ツールバーオプションメニュー設定
                menuInflater.inflate(R.menu.display_diary_toolbar_menu, menu);

                ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(null);

                actionBar.setTitle(diaryViewModel.getLiveDate().getValue());
            }

            //アクションバーメニュー選択処理設定。
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                //日記編集フラグメント起動。
                if (menuItem.getItemId() == R.id.displayDiaryToolbarOptionEditDiary) {
                    NavDirections action =
                            ShowDiaryFragmentDirections
                                    .actionNavigationShowDiaryFragmentToEditDiaryFragment(false);
                    ShowDiaryFragment.this.navController.navigate(action);
                    return true;

                    //戻る(ナビフラグメント表示(日記表示フラグメント削除))。
                } else if (menuItem.getItemId() == android.R.id.home) {
                    backFragment(true);
                    return true;

                } else {
                    return false;
                }
            }
    }

    // 一つ前のフラグメント(EDitDiaryFragment)を表示
    private void backFragment(boolean isNavigateUp) {
        NavBackStackEntry navBackStackEntry = this.navController.getPreviousBackStackEntry();
        int destinationId = navBackStackEntry.getDestination().getId();
        if (destinationId == R.id.navigation_calendar_fragment) {
            SavedStateHandle savedStateHandle =
                    this.navController.getPreviousBackStackEntry().getSavedStateHandle();
            String showedDiaryDate = this.diaryViewModel.getLiveDate().getValue();
            savedStateHandle.set(KEY_SHOWED_DIARY_DATE, showedDiaryDate);
        }

        if (isNavigateUp) {
            this.navController.navigateUp();
        } else {
            this.navController.popBackStack();
        }
    }

}
