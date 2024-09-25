package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.websarva.wings.android.zuboradiary.R;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.ListThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryListFragment extends BaseFragment {

    // View関係
    private FragmentDiaryListBinding binding;

    // ViewModel
    private DiaryListViewModel diaryListViewModel;
    private SettingsViewModel settingsViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryListViewModel = provider.get(DiaryListViewModel.class);
        settingsViewModel = provider.get(SettingsViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected View initializeDataBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        binding = FragmentDiaryListBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setListViewModel(diaryListViewModel);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpToolBar();
        setUpFloatActionButton();
    }

    @Override
    protected void setUpThemeColor() {
        settingsViewModel.getThemeColorSettingValueLiveData()
                .observe(getViewLifecycleOwner(), new Observer<ThemeColor>() {
                    @Override
                    public void onChanged(ThemeColor themeColor) {
                        if (themeColor == null) {
                            return;
                        }

                        // MEMO:RecyclerViewにThemeColorを適応させるため、
                        //      ViewModelのThemeColorValueに値が格納されてから処理すること。
                        //      RecyclerView設定後、色を変更するにはそれなりの工数がかかると判断。
                        setUpDiaryList(themeColor);

                        ListThemeColorSwitcher switcher =
                                new ListThemeColorSwitcher(requireContext(), themeColor);

                        switcher.switchToolbarColor(binding.materialToolbarTopAppBar);

                        ColorSwitchingViewList<FloatingActionButton> floatingActionButtonList =
                                new ColorSwitchingViewList<>(binding.fabEditDiary);
                        switcher.switchFloatingActionButtonColor(floatingActionButtonList);

                        switcher.switchCircularProgressBarColor(binding.progressBarDiaryListFullScreen);
                    }
                });
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        receiveDatePickerDialogResults(savedStateHandle);
        receiveDiaryDeleteConfirmationDialogResults(savedStateHandle);
    }

    @Override
    protected void removeDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
        savedStateHandle.remove(DiaryDeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
    }

    @Override
    protected void setUpErrorMessageDialog() {
        diaryListViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(diaryListViewModel));
    }

    // 日付入力ダイアログフラグメントから結果受取
    private void receiveDatePickerDialogResults(SavedStateHandle savedStateHandle) {
        YearMonth selectedYearMonth =
                receiveResulFromDialog(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
        if (selectedYearMonth == null) {
            return;
        }

        diaryListViewModel.updateSortConditionDate(selectedYearMonth);
        diaryListScrollToFirstPosition();
        diaryListViewModel.loadList(DiaryListViewModel.LoadType.NEW);
    }

    // 日記削除ダイアログフラグメントから結果受取
    private void receiveDiaryDeleteConfirmationDialogResults(SavedStateHandle savedStateHandle) {
        LocalDate deleteDiaryDate =
                receiveResulFromDialog(DiaryDeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
        if (deleteDiaryDate == null) {
            return;
        }

        diaryListViewModel.deleteDiary(deleteDiaryDate);
    }

    // ツールバー設定
    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // リスト先頭年月切り替えダイアログ起動
                        Diary newestDiary = diaryListViewModel.loadNewestDiary();
                        Diary oldestDiary = diaryListViewModel.loadOldestDiary();
                        if (newestDiary == null) {
                            throw new NullPointerException();
                        }
                        if (oldestDiary == null) {
                            throw new NullPointerException();
                        }

                        String newestDate = newestDiary.getDate();
                        String oldestDate = oldestDiary.getDate();
                        Year newestYear = Year.of(LocalDate.parse(newestDate).getYear());
                        Year oldestYear = Year.of(LocalDate.parse(oldestDate).getYear());
                        showStartYearMonthPickerDialog(newestYear, oldestYear);
                    }
                });

        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // ワード検索フラグメント起動
                        if (item.getItemId() == R.id.diaryListToolbarOptionWordSearch) {
                            showWordSearchFragment();
                            return true;
                        }
                        return false;
                    }
                });
    }

    // 新規作成FAB設定
    private void setUpFloatActionButton() {
        binding.fabEditDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDiary();
            }
        });
    }

    // 日記リスト(年月)設定
    private void setUpDiaryList(ThemeColor themeColor) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        DiaryListAdapter diaryListAdapter =
                new DiaryListAdapter(
                        requireContext(),
                        binding.recyclerDiaryYearMonthList,
                        themeColor,
                        true
                );
        diaryListAdapter.build();
        diaryListAdapter.setOnClickChildItemListener(new DiaryYearMonthListAdapter.OnClickChildItemListener() {
            @Override
            public void onClick(LocalDate date) {
                showShowDiaryFragment(date);
            }
        });
        diaryListAdapter.setOnClickChildItemBackgroundButtonListener(new DiaryYearMonthListAdapter.OnClickChildItemBackgroundButtonListener() {
            @Override
            public void onClick(LocalDate date) {
                showDiaryDeleteConfirmationDialog(date);
            }
        });

        diaryListViewModel.getDiaryListLiveData().observe(
                getViewLifecycleOwner(),
                new Observer<List<DiaryYearMonthListItem>>() {
                    @Override
                    public void onChanged(List<DiaryYearMonthListItem> diaryListItems) {
                        if (diaryListItems == null) {
                            return;
                        }
                        if (diaryListItems.isEmpty()) return;
                        DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                                (DiaryYearMonthListAdapter)
                                        binding.recyclerDiaryYearMonthList.getAdapter();
                        if (diaryYearMonthListAdapter == null) {
                            return;
                        }

                        boolean isNoDiary =
                                diaryListItems.get(0).getViewType() == DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE;
                        if (isNoDiary) {
                            binding.textDiaryListNoDiaryMessage.setVisibility(View.VISIBLE);
                            binding.recyclerDiaryYearMonthList.setVisibility(View.INVISIBLE);
                        } else {
                            binding.textDiaryListNoDiaryMessage.setVisibility(View.INVISIBLE);
                            binding.recyclerDiaryYearMonthList.setVisibility(View.VISIBLE);
                        }

                        List<DiaryYearMonthListItemBase> convertedList = new ArrayList<>(diaryListItems);
                        diaryYearMonthListAdapter.submitList(convertedList);
                    }
                }
        );

        // 画面全体ProgressBar表示中はタッチ無効化
        binding.viewDiaryListProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
            }
        });

        loadDiaryList();
    }

    private class DiaryListAdapter extends DiaryYearMonthListAdapter {

        public DiaryListAdapter(Context context, RecyclerView recyclerView, ThemeColor themeColor, boolean canSwipeItem) {
            super(context, recyclerView, themeColor, canSwipeItem);
        }

        @Override
        public void loadListOnScrollEnd() {
            diaryListViewModel.loadList(DiaryListViewModel.LoadType.ADD);
        }

        @Override
        public boolean canLoadList() {
            return diaryListViewModel.canLoadDiaryList();
        }
    }

    private void loadDiaryList() {
        List<DiaryYearMonthListItem> diaryYearMonthList =
                diaryListViewModel.getDiaryListLiveData().getValue();
        if (diaryYearMonthList == null || diaryYearMonthList.isEmpty()) {
            Integer numSavedDiaries = diaryListViewModel.countDiaries();
            if (numSavedDiaries != null && numSavedDiaries >= 1) {
                diaryListViewModel.loadList(DiaryListViewModel.LoadType.NEW);
            }
        } else {
            diaryListViewModel.loadList(DiaryListViewModel.LoadType.UPDATE);
        }
    }

    private void showEditDiary() {
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToDiaryEditFragment(
                                true,
                                false,
                                LocalDate.now()
                        );
        navController.navigate(action);
    }

    private void showShowDiaryFragment(LocalDate date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToDiaryShowFragment(date);
        navController.navigate(action);
    }

    private void showWordSearchFragment() {
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToWordSearchFragment();
        navController.navigate(action);
    }

    private void showStartYearMonthPickerDialog(Year newestYear, Year oldestYear) {
        if (newestYear == null) {
            throw new NullPointerException();
        }
        if (oldestYear == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToStartYearMonthPickerDialog(newestYear, oldestYear);
        navController.navigate(action);
    }

    private void showDiaryDeleteConfirmationDialog(LocalDate date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToDiaryDeleteConfirmationDialog(date);
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryErrorDialogShow() {
        diaryListViewModel.triggerAppErrorBufferListObserver();
    }

    public void processOnReSelectNavigationItem(){
        diaryListScrollToFirstPosition();
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    private void diaryListScrollToFirstPosition() {
        RecyclerView.Adapter<?> adapter = binding.recyclerDiaryYearMonthList.getAdapter();
        if (adapter instanceof DiaryYearMonthListAdapter) {
            DiaryYearMonthListAdapter diaryYearMonthListAdapter = (DiaryYearMonthListAdapter) adapter;
            diaryYearMonthListAdapter.scrollToFirstPosition();
        }
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
