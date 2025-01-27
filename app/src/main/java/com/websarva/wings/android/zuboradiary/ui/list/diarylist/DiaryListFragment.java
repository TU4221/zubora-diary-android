package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.UriPermissionManager;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryListFragment extends BaseFragment {

    // View関係
    private FragmentDiaryListBinding binding;

    // ViewModel
    private DiaryListViewModel diaryListViewModel;

    // Uri関係
    private UriPermissionManager pictureUriPermissionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pictureUriPermissionManager =
                new UriPermissionManager(requireContext()) {
                    @Override
                    public boolean checkUsedUriDoesNotExist(@NonNull Uri uri) {
                        return diaryListViewModel.checkSavedPicturePathDoesNotExist(uri);
                    }
                };
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryListViewModel = provider.get(DiaryListViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected ViewDataBinding initializeDataBinding(
            @NonNull LayoutInflater themeColorInflater, @NonNull ViewGroup container) {
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
        receiveDiaryDeleteDialogResults(savedStateHandle);
    }

    @Override
    protected void removeDialogResultOnDestroy(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
        savedStateHandle.remove(DiaryDeleteDialogFragment.KEY_DELETE_DIARY_DATE);
    }

    @Override
    protected void setUpOtherAppMessageDialog() {
        diaryListViewModel.getAppMessageBufferList()
                .observe(getViewLifecycleOwner(), new AppMessageBufferListObserver(diaryListViewModel));
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
    private void receiveDiaryDeleteDialogResults(SavedStateHandle savedStateHandle) {
        Objects.requireNonNull(savedStateHandle);

        LocalDate deleteDiaryDate =
                receiveResulFromDialog(DiaryDeleteDialogFragment.KEY_DELETE_DIARY_DATE);
        if (deleteDiaryDate == null) return;

        boolean isSuccessful = diaryListViewModel.deleteDiary(deleteDiaryDate);
        if (!isSuccessful) return;

        Uri deleteDiaryPictureUri =
                receiveResulFromDialog(DiaryDeleteDialogFragment.KEY_DELETE_DIARY_PICTURE_URI);
        if (deleteDiaryPictureUri == null) return;
        releasePersistableLoadedPictureUriPermission(deleteDiaryPictureUri);
    }

    private void releasePersistableLoadedPictureUriPermission(Uri uri) {
        Objects.requireNonNull(uri);

        pictureUriPermissionManager.releasePersistablePermission(uri);
    }

    // ツールバー設定
    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(v -> {
                    Objects.requireNonNull(v);

                    // リスト先頭年月切り替えダイアログ起動
                    LocalDate newestDiaryDate = diaryListViewModel.loadNewestSavedDiary();
                    LocalDate oldestDiaryDate = diaryListViewModel.loadOldestSavedDiary();
                    if (newestDiaryDate == null) return;
                    if (oldestDiaryDate == null) return;

                    Year newestYear = Year.of(newestDiaryDate.getYear());
                    Year oldestYear = Year.of(oldestDiaryDate.getYear());
                    showStartYearMonthPickerDialog(newestYear, oldestYear);
                });

        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(item -> {
                    Objects.requireNonNull(item);

                    // ワード検索フラグメント起動
                    if (item.getItemId() == R.id.diaryListToolbarOptionWordSearch) {
                        showWordSearchFragment();
                        return true;
                    }
                    return false;
                });
    }

    // 新規作成FAB設定
    private void setUpFloatActionButton() {
        binding.floatingActionButtonDiaryEdit.setOnClickListener(v -> {
            Objects.requireNonNull(v);

            showEditDiary();
        });
    }

    // 日記リスト(年月)設定
    private void setUpDiaryList() {
        DiaryListAdapter diaryListAdapter =
                new DiaryListAdapter(
                        requireContext(),
                        binding.recyclerDiaryList,
                        requireThemeColor()
                );
        diaryListAdapter.build();
        diaryListAdapter.setOnClickChildItemListener(item -> {
            Objects.requireNonNull(item);

            showShowDiaryFragment(item.getDate());
        });
        diaryListAdapter.setOnClickChildItemBackgroundButtonListener(item -> {
            Objects.requireNonNull(item);

            DiaryDayListItem _item = (DiaryDayListItem) item;
            showDiaryDeleteDialog(_item.getDate(), _item.getPicturePath());
        });

        diaryListViewModel.getDiaryListLiveData().observe(getViewLifecycleOwner(), new DiaryListObserver());

        // 画面全体ProgressBar表示中はタッチ無効化
        binding.includeProgressIndicator.viewBackground.setOnTouchListener((v, event) -> {
            Objects.requireNonNull(v);
            Objects.requireNonNull(event);

            v.performClick();
            return true;
        });

        loadDiaryList();
    }

    private class DiaryListAdapter extends DiaryYearMonthListAdapter {

        public DiaryListAdapter(Context context, RecyclerView recyclerView, ThemeColor themeColor) {
            super(context, recyclerView, themeColor);
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
                binding.textNoDiaryMessage.setVisibility(View.VISIBLE);
                binding.recyclerDiaryList.setVisibility(View.INVISIBLE);
            } else {
                binding.textNoDiaryMessage.setVisibility(View.INVISIBLE);
                binding.recyclerDiaryList.setVisibility(View.VISIBLE);
            }
        }

        private void setUpList(DiaryYearMonthList list) {
            Objects.requireNonNull(list);

            List<DiaryYearMonthListBaseItem> convertedItemList =
                    new ArrayList<>(list.getDiaryYearMonthListItemList());
            DiaryYearMonthListAdapter listAdapter =
                    (DiaryYearMonthListAdapter) binding.recyclerDiaryList.getAdapter();
            Objects.requireNonNull(listAdapter);
            listAdapter.submitList(convertedItemList);
        }
    }

    private void loadDiaryList() {
        DiaryYearMonthList diaryList = diaryListViewModel.getDiaryListLiveData().getValue();
        Objects.requireNonNull(diaryList);

        if (diaryList.getDiaryYearMonthListItemList().isEmpty()) {
            Integer numSavedDiaries = diaryListViewModel.countSavedDiaries();
            if (numSavedDiaries == null) return;

            if (numSavedDiaries >= 1) diaryListViewModel.loadNewDiaryList();
        } else {
            diaryListViewModel.updateDiaryList();
        }
    }

    private void showEditDiary() {
        if (isDialogShowing()) return;

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
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToDiaryShowFragment(date);
        navController.navigate(action);
    }

    private void showWordSearchFragment() {
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToWordSearchFragment();
        navController.navigate(action);
    }

    private void showStartYearMonthPickerDialog(Year newestYear, Year oldestYear) {
        Objects.requireNonNull(newestYear);
        Objects.requireNonNull(oldestYear);
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToStartYearMonthPickerDialog(newestYear, oldestYear);
        navController.navigate(action);
    }

    private void showDiaryDeleteDialog(LocalDate date, Uri pictureUri) {
        Objects.requireNonNull(date);
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryListFragmentDirections.actionDiaryListFragmentToDiaryDeleteDialog(date, pictureUri);
        navController.navigate(action);
    }

    @Override
    protected void navigateAppMessageDialog(@NonNull AppMessage appMessage) {
        NavDirections action =
                DiaryListFragmentDirections.actionDiaryListFragmentToAppMessageDialog(appMessage);
        navController.navigate(action);
    }

    @Override
    protected void retryOtherAppMessageDialogShow() {
        diaryListViewModel.triggerAppMessageBufferListObserver();
    }

    public void processOnReSelectNavigationItem(){
        scrollDiaryListToFirstPosition();
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    private void scrollDiaryListToFirstPosition() {
        DiaryYearMonthListAdapter listAdapter =
                (DiaryYearMonthListAdapter) binding.recyclerDiaryList.getAdapter();
        Objects.requireNonNull(listAdapter);

        listAdapter.scrollToFirstPosition();
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
