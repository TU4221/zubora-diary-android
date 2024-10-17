package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import static com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter.*;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter.OnClickChildItemListener;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;
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
    protected ViewDataBinding initializeDataBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        LayoutInflater themeColorInflater = createThemeColorInflater(inflater, themeColor);
        binding = FragmentDiaryListBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setListViewModel(diaryListViewModel);
        return binding;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpToolBar();
        setUpFloatActionButton();
        setUpDiaryList();
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
        Objects.requireNonNull(savedStateHandle);

        YearMonth selectedYearMonth =
                receiveResulFromDialog(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
        if (selectedYearMonth == null) return;

        diaryListViewModel.updateSortConditionDate(selectedYearMonth);
        scrollDiaryListToFirstPosition();
        diaryListViewModel.loadNewDiaryList();
    }

    // 日記削除ダイアログフラグメントから結果受取
    private void receiveDiaryDeleteConfirmationDialogResults(SavedStateHandle savedStateHandle) {
        Objects.requireNonNull(savedStateHandle);

        LocalDate deleteDiaryDate =
                receiveResulFromDialog(DiaryDeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
        if (deleteDiaryDate == null) return;

        diaryListViewModel.deleteDiary(deleteDiaryDate);
    }

    // ツールバー設定
    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Objects.requireNonNull(v);

                        // リスト先頭年月切り替えダイアログ起動
                        LocalDate newestDiaryDate = diaryListViewModel.loadNewestDiary();
                        LocalDate oldestDiaryDate = diaryListViewModel.loadOldestDiary();
                        if (newestDiaryDate == null) return;
                        if (oldestDiaryDate == null) return;

                        Year newestYear = Year.of(newestDiaryDate.getYear());
                        Year oldestYear = Year.of(oldestDiaryDate.getYear());
                        showStartYearMonthPickerDialog(newestYear, oldestYear);
                    }
                });

        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Objects.requireNonNull(item);

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
                Objects.requireNonNull(v);

                showEditDiary();
            }
        });
    }

    // 日記リスト(年月)設定
    private void setUpDiaryList() {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();

        DiaryListAdapter diaryListAdapter =
                new DiaryListAdapter(
                        requireContext(),
                        binding.recyclerDiaryYearMonthList,
                        themeColor,
                        true
                );
        diaryListAdapter.build();
        diaryListAdapter.setOnClickChildItemListener(new OnClickChildItemListener() {
            @Override
            public void onClick(LocalDate date) {
                Objects.requireNonNull(date);

                showShowDiaryFragment(date);
            }
        });
        diaryListAdapter.setOnClickChildItemBackgroundButtonListener(new OnClickChildItemBackgroundButtonListener() {
            @Override
            public void onClick(LocalDate date) {
                Objects.requireNonNull(date);

                showDiaryDeleteConfirmationDialog(date);
            }
        });

        diaryListViewModel.getDiaryListLiveData().observe(getViewLifecycleOwner(), new DiaryListObserver());

        // 画面全体ProgressBar表示中はタッチ無効化
        binding.includeProgressIndicator.viewBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Objects.requireNonNull(v);
                Objects.requireNonNull(event);

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
            diaryListViewModel.loadAdditionDiaryList();
        }

        @Override
        public boolean canLoadList() {
            return diaryListViewModel.canLoadDiaryList();
        }
    }

    private class DiaryListObserver implements Observer<DiaryYearMonthList> {

        @Override
        public void onChanged(DiaryYearMonthList list) {
            Objects.requireNonNull(list);

            setUpListViewVisibility(list);
            setUpList(list);
        }

        private void setUpListViewVisibility(DiaryYearMonthList list) {
            Objects.requireNonNull(list);

            boolean isNoDiary = list.getDiaryYearMonthListItemList().isEmpty();
            if (isNoDiary) {
                binding.textDiaryListNoDiaryMessage.setVisibility(View.VISIBLE);
                binding.recyclerDiaryYearMonthList.setVisibility(View.INVISIBLE);
            } else {
                binding.textDiaryListNoDiaryMessage.setVisibility(View.INVISIBLE);
                binding.recyclerDiaryYearMonthList.setVisibility(View.VISIBLE);
            }
        }

        private void setUpList(DiaryYearMonthList list) {
            Objects.requireNonNull(list);

            List<DiaryYearMonthListItemBase> convertedItemList =
                    new ArrayList<>(list.getDiaryYearMonthListItemList());
            DiaryYearMonthListAdapter listAdapter =
                    (DiaryYearMonthListAdapter)
                            binding.recyclerDiaryYearMonthList.getAdapter();
            Objects.requireNonNull(listAdapter);
            listAdapter.submitList(convertedItemList);
        }
    }

    private void loadDiaryList() {
        DiaryYearMonthList diaryList = diaryListViewModel.getDiaryListLiveData().getValue();
        Objects.requireNonNull(diaryList);

        if (diaryList.getDiaryYearMonthListItemList().isEmpty()) {
            Integer numSavedDiaries = diaryListViewModel.countDiaries();
            if (numSavedDiaries == null) return;

            if (numSavedDiaries >= 1) diaryListViewModel.loadNewDiaryList();
        } else {
            diaryListViewModel.updateDiaryList();
        }
    }

    private void showEditDiary() {
        if (!canShowOtherFragment()) return;

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
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToDiaryShowFragment(date);
        navController.navigate(action);
    }

    private void showWordSearchFragment() {
        if (!canShowOtherFragment()) return;

        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToWordSearchFragment();
        navController.navigate(action);
    }

    private void showStartYearMonthPickerDialog(Year newestYear, Year oldestYear) {
        Objects.requireNonNull(newestYear);
        Objects.requireNonNull(oldestYear);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToStartYearMonthPickerDialog(newestYear, oldestYear);
        navController.navigate(action);
    }

    private void showDiaryDeleteConfirmationDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

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
        scrollDiaryListToFirstPosition();
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    private void scrollDiaryListToFirstPosition() {
        DiaryYearMonthListAdapter adapter =
                (DiaryYearMonthListAdapter) binding.recyclerDiaryYearMonthList.getAdapter();
        Objects.requireNonNull(adapter);

        adapter.scrollToFirstPosition();
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
