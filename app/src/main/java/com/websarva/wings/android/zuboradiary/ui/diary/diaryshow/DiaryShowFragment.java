package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow;

import android.app.Dialog;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavDirections;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowConditionObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowLogObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowNumVisibleItemsObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowWeather1Observer;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowWeather2Observer;

import java.time.LocalDate;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryShowFragment extends BaseFragment {

    // View関係
    private FragmentDiaryShowBinding binding;// 項目入力欄最大数

    // Navigation関係
    private static final String fromClassName = "From" + DiaryShowFragment.class.getName();
    public static final String KEY_SHOWED_DIARY_DATE = "ShowedDiaryDate" + fromClassName;
    private boolean shouldShowDiaryLoadingErrorDialog;
    private boolean shouldShowDiaryDeleteErrorDialog;

    // ViewModel
    private DiaryShowViewModel diaryShowViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(this);
        diaryShowViewModel = provider.get(DiaryShowViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected View initializeDataBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        binding = FragmentDiaryShowBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setDiaryShowViewModel(diaryShowViewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpDiaryData();
        setUpToolBar();
        setUpWeatherLayout();
        setUpConditionLayout();
        setUpItemLayout();
        setUpLogShowLayout();
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingResulFromDialog(@NonNull SavedStateHandle savedStateHandle) {
        receiveDeleteConfirmationDialogResult(savedStateHandle);
        retryErrorDialogShow();
    }

    @Override
    protected void removeResultFromDialog(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DiaryDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
    }

    @Override
    protected void setUpErrorMessageDialog() {
        diaryShowViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(diaryShowViewModel));
    }

    // 日記削除確認ダイアログフラグメントからデータ受取
    private void receiveDeleteConfirmationDialogResult(SavedStateHandle savedStateHandle) {
        Integer selectedButton =
                receiveResulFromDialog(DiaryDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) {
            return;
        }

        if (selectedButton != Dialog.BUTTON_POSITIVE) {
            return;
        }
        diaryShowViewModel.deleteDiary();
        backFragment(true);
    }

    // 画面表示データ準備
    private void setUpDiaryData() {
        diaryShowViewModel.initialize();
        LocalDate diaryDate =
                DiaryShowFragmentArgs.fromBundle(requireArguments()).getShowDiaryDate();

        // 日記編集Fragmentで日記を削除して日記表示Fragmentに戻って来た時は更に一つ前のFragmentへ戻る。
        if (!diaryShowViewModel.hasDiary(diaryDate)) {
            navController.navigateUp();
            return;
        }

        diaryShowViewModel.loadDiary(diaryDate);
    }

    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        backFragment(true);
                    }
                });

        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // 日記編集フラグメント起動
                        if (item.getItemId() == R.id.diaryShowToolbarOptionEditDiary) {
                            LocalDate editDiaryDate = diaryShowViewModel.getDateLiveData().getValue();
                            showDiaryEdit(editDiaryDate);
                            return true;
                        } else if (item.getItemId() == R.id.diaryShowToolbarOptionDeleteDiary) {
                            LocalDate deleteDiaryDate = diaryShowViewModel.getDateLiveData().getValue();
                            showDiaryDeleteConfirmationDialog(deleteDiaryDate);
                            return true;
                        }
                        return false;
                    }
                });

        diaryShowViewModel.getDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
                    @Override
                    public void onChanged(LocalDate date) {
                        if (date == null) {
                            return;
                        }
                        DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
                        String stringDate = dateTimeStringConverter.toStringDate(date);
                        binding.materialToolbarTopAppBar.setTitle(stringDate);
                    }
                });
    }

    // 天気表示欄設定
    private void setUpWeatherLayout() {
        diaryShowViewModel.getWeather1LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowWeather1Observer(
                                requireContext(),
                                binding.includeDiaryShow.textWeather1Selected
                        )
                );

        diaryShowViewModel.getWeather2LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowWeather2Observer(
                                requireContext(),
                                binding.includeDiaryShow.textWeatherSlush,
                                binding.includeDiaryShow.textWeather2Selected
                        )
                );
    }

    private void setUpConditionLayout() {
        diaryShowViewModel.getConditionLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowConditionObserver(
                                requireContext(),
                                binding.includeDiaryShow.textConditionSelected
                        )
                );
    }

    private void setUpItemLayout() {
        View[] itemLayouts = new View[DiaryLiveData.MAX_ITEMS];
        itemLayouts[0] = binding.includeDiaryShow.includeItem1.linerLayoutDiaryShowItem;
        itemLayouts[1] = binding.includeDiaryShow.includeItem2.linerLayoutDiaryShowItem;
        itemLayouts[2] = binding.includeDiaryShow.includeItem3.linerLayoutDiaryShowItem;
        itemLayouts[3] = binding.includeDiaryShow.includeItem4.linerLayoutDiaryShowItem;
        itemLayouts[4] = binding.includeDiaryShow.includeItem5.linerLayoutDiaryShowItem;
        diaryShowViewModel.getNumVisibleItemsLiveData()
                .observe(getViewLifecycleOwner(), new DiaryShowNumVisibleItemsObserver(itemLayouts));
    }

    private void setUpLogShowLayout() {
        diaryShowViewModel.getLogLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowLogObserver(binding.includeDiaryShow.textLogValue)
                );
    }

    private void showDiaryEdit(LocalDate date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryShowFragmentDirections
                        .actionNavigationDiaryShowFragmentToDiaryEditFragment(
                                false,
                                true,
                                date
                        );
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
                DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryDeleteConfirmationDialog(date);
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                DiaryShowFragmentDirections.actionDiaryShowFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryErrorDialogShow() {
        diaryShowViewModel.triggerAppErrorBufferListObserver();
    }

    // 一つ前のフラグメントを表示
    // MEMO:ツールバーの戻るボタンと端末の戻るボタンを区別している。
    //      ツールバーの戻るボタン:アプリ内でのみ戻る
    //      端末の戻るボタン:端末内で戻る(アプリ外から本アプリを起動した場合起動もとへ戻る)
    private void backFragment(boolean isNavigateUp) {
        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
        if (navBackStackEntry != null) {
            int destinationId = navBackStackEntry.getDestination().getId();
            if (destinationId == R.id.navigation_calendar_fragment) {
                SavedStateHandle savedStateHandle =
                        navController.getPreviousBackStackEntry().getSavedStateHandle();
                LocalDate showedDiaryLocalDate = diaryShowViewModel.getDateLiveData().getValue();
                savedStateHandle.set(KEY_SHOWED_DIARY_DATE, showedDiaryLocalDate);
            }
        }

        if (isNavigateUp) {
            navController.navigateUp();
        } else {
            navController.popBackStack();
        }
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
