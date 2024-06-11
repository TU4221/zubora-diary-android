package com.websarva.wings.android.zuboradiary.ui.diary.showdiary;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.FragmentShowDiaryBinding;
import com.websarva.wings.android.zuboradiary.ui.ShowDiaryLayout;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryViewModel;

public class ShowDiaryFragment extends Fragment {

    // View関係
    private FragmentShowDiaryBinding binding;
    private final int MAX_ITEMS_COUNT = DiaryViewModel.MAX_ITEMS_COUNT; // 項目入力欄最大数

    // Navigation関係
    private NavController navController;
    private static final String fromClassName = "From" + ShowDiaryFragment.class.getName();
    public static final String KEY_SHOWED_DIARY_DATE = "ShowedDiaryDate" + fromClassName;

    // ViewModel
    private DiaryViewModel diaryViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.diaryViewModel = provider.get(DiaryViewModel.class);
        this.diaryViewModel.initialize();

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

        // 画面表示データ準備
        int showDiaryDateYear =
                ShowDiaryFragmentArgs.fromBundle(requireArguments()).getShowDiaryDateYear();
        int showDiaryDateMonth =
                ShowDiaryFragmentArgs.fromBundle(requireArguments()).getShowDiaryDateMonth();
        int showDiaryDateDayOfMonth =
                ShowDiaryFragmentArgs.fromBundle(requireArguments()).getShowDiaryDateDayOfMonth();
        this.diaryViewModel.prepareDiary(
                showDiaryDateYear, showDiaryDateMonth, showDiaryDateDayOfMonth, true);


        // ツールバー設定
        this.binding.materialToolbarTopAppBar
                .setTitle(this.diaryViewModel.getLiveDate().getValue());
        this.binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //戻る(ナビフラグメント表示(日記表示フラグメント削除))。
                        backFragment(true);
                    }
                });
        this.binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //日記編集フラグメント起動。
                        if (item.getItemId() == R.id.displayDiaryToolbarOptionEditDiary) {
                            int editDiaryDateYear =
                                    ShowDiaryFragmentArgs.fromBundle(requireArguments())
                                            .getShowDiaryDateYear();
                            int editDiaryDateMonth =
                                    ShowDiaryFragmentArgs.fromBundle(requireArguments())
                                            .getShowDiaryDateMonth();
                            int editDiaryDateDayOfMonth =
                                    ShowDiaryFragmentArgs.fromBundle(requireArguments())
                                            .getShowDiaryDateDayOfMonth();
                            NavDirections action =
                                    ShowDiaryFragmentDirections
                                            .actionNavigationShowDiaryFragmentToEditDiaryFragment(
                                                    false,
                                                    false,
                                                    editDiaryDateYear,
                                                    editDiaryDateMonth,
                                                    editDiaryDateDayOfMonth
                                            );
                            ShowDiaryFragment.this.navController.navigate(action);
                            return true;
                        }
                        return false;
                    }
                });


        // 天気表示欄設定
        // MEMO:日記の中身を表示するレイアウトをShowDiaryFragmentとCalendarFragment共有させるため、
        //      別レイアウトに作成。各Fragmentにて設定が重複するため、下記クラス、メソッドを作成して使用。
        ShowDiaryLayout.setupVisibleWeather2Observer(
                this.diaryViewModel, getViewLifecycleOwner(),
                this.binding.includeShowDiary.textWeatherSlush,
                this.binding.includeShowDiary.textWeather2Selected
        );


        // 必要数の項目欄表示
        setupItemLayout();
    }


    private MotionLayout selectItemMotionLayout(int itemNumber) {
        switch (itemNumber) {
            case 1:
                return this.binding.includeShowDiary.includeItem1.motionLayoutShowDiaryItem;
            case 2:
                return this.binding.includeShowDiary.includeItem2.motionLayoutShowDiaryItem;

            case 3:
                return this.binding.includeShowDiary.includeItem3.motionLayoutShowDiaryItem;

            case 4:
                return this.binding.includeShowDiary.includeItem4.motionLayoutShowDiaryItem;

            case 5:
                return this.binding.includeShowDiary.includeItem5.motionLayoutShowDiaryItem;
            default:
                return null;
        }
    }

    private void setupItemLayout() {
        int visibleItemsCount = this.diaryViewModel.getVisibleItemsCount();
        for (int i = 0; i < this.MAX_ITEMS_COUNT; i++) {
            int itemNumber = i + 1;
            MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
            if (itemNumber <= visibleItemsCount) {
                itemMotionLayout
                        .transitionToState(R.id.motion_scene_show_diary_item_showed_state, 1);
            } else {
                itemMotionLayout
                        .transitionToState(R.id.motion_scene_show_diary_item_hided_state, 1);
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
