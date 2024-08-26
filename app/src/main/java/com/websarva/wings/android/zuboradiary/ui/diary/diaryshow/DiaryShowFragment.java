package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow;

import android.app.Dialog;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateConverter;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding;
import com.websarva.wings.android.zuboradiary.ui.CustomFragment;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowConditionObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowLogObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowNumVisibleItemsObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowWeather1Observer;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowWeather2Observer;

import java.time.LocalDate;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryShowFragment extends CustomFragment {

    // View関係
    private FragmentDiaryShowBinding binding;// 項目入力欄最大数

    // Navigation関係
    private NavController navController;
    private static final String fromClassName = "From" + DiaryShowFragment.class.getName();
    public static final String KEY_SHOWED_DIARY_DATE = "ShowedDiaryDate" + fromClassName;
    private boolean shouldShowDiaryLoadingErrorDialog;
    private boolean shouldShowDiaryDeleteErrorDialog;

    // ViewModel
    private DiaryShowViewModel diaryShowViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(this);
        diaryShowViewModel = provider.get(DiaryShowViewModel.class);

        // Navigation設定
        navController = NavHostFragment.findNavController(this);

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
        binding = FragmentDiaryShowBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        binding.setLifecycleOwner(this);
        binding.setDiaryShowViewModel(diaryShowViewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpDialogResultReceiver();
        setUpDiaryData();
        setUpToolBar();
        setUpWeatherLayout();
        setUpConditionLayout();
        setUpItemLayout();
        setUpLogShowLayout();
        setUpErrorObserver();
    }

    private void setUpDialogResultReceiver() {
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        if (navBackStackEntry == null) {
            return;
        }
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                // MEMO:Dialog表示中:Lifecycle.Event.ON_PAUSE
                //      Dialog非表示中:Lifecycle.Event.ON_RESUME
                Log.d("LifecycleEventObserver", "DiaryShowFragment_NavBackStackEntry_event:" + event);
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    receiveDeleteConfirmationDialogResult(savedStateHandle);
                    removeDialogResults(savedStateHandle);
                    retryErrorDialogShow();
                }
            }
        };
        navBackStackEntry.getLifecycle().addObserver(lifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                Log.d("LifecycleEventObserver", "DiaryShowFragment_ViewLifecycleOwner_event:" + event);
                if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                    // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                    //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    removeDialogResults(savedStateHandle);
                    // TODO:下記コード意味あるか検証。コメントアウトしてFragment切替後の状態を確認したがObserverが重複することはなかった。
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });
    }

    private void removeDialogResults(SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DiaryDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
    }

    // 日記削除確認ダイアログフラグメントからデータ受取
    private void receiveDeleteConfirmationDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(DiaryDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        if (containsDialogResult) {
            Integer selectedButton =
                    savedStateHandle.get(DiaryDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
            if (selectedButton == null) {
                return;
            }
            if (selectedButton == Dialog.BUTTON_POSITIVE) {
                diaryShowViewModel.deleteDiary();
                backFragment(true);
            }
        }
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
                            if (editDiaryDate == null) {
                                // TODO:assert
                                return false;
                            }
                            showDiaryEdit(editDiaryDate);
                            return true;
                        } else if (item.getItemId() == R.id.diaryShowToolbarOptionDeleteDiary) {
                            LocalDate deleteDiaryDate = diaryShowViewModel.getDateLiveData().getValue();
                            showDiaryDeleteConfirmationDialog(deleteDiaryDate);
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
                        DateConverter dateConverter = new DateConverter();
                        String stringDate = dateConverter.toStringLocalDate(date);
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

    private void setUpErrorObserver() {
        // エラー表示
        diaryShowViewModel.getIsDiaryLoadingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                if (aBoolean) {
                    showDiaryLoadingErrorDialog();
                    diaryShowViewModel.clearDiaryLoadingError();
                }
            }
        });
        diaryShowViewModel.getIsDiaryDeleteErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                if (aBoolean) {
                    showDiaryDeleteErrorDialog();
                    diaryShowViewModel.clearDiaryDeleteError();
                }
            }
        });
    }

    private void showDiaryEdit(@NonNull LocalDate date) {
        NavDirections action =
                DiaryShowFragmentDirections
                        .actionNavigationDiaryShowFragmentToDiaryEditFragment(
                                false,
                                true,
                                date
                        );
        navController.navigate(action);
    }

    private boolean canShowDialog() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination == null) {
            return false;
        }
        int currentDestinationId = navController.getCurrentDestination().getId();
        return currentDestinationId == R.id.navigation_diary_show_fragment;
    }

    private void showDiaryDeleteConfirmationDialog(LocalDate date) {
        if (canShowDialog()) {
            if (date == null) {
                return;
            }
            NavDirections action =
                    DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryDeleteConfirmationDialog(date);
            navController.navigate(action);
        }
    }

    // 他のダイアログで表示できなかったダイアログを表示
    private void retryErrorDialogShow() {
        if (shouldShowDiaryLoadingErrorDialog) {
            showDiaryLoadingErrorDialog();
            return;
        }
        if (shouldShowDiaryDeleteErrorDialog) {
            showDiaryDeleteErrorDialog();
        }
    }

    private void showDiaryLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog(getString(R.string.dialog_message_title_communication_error), getString(R.string.dialog_message_message_diary_loading_error));
            shouldShowDiaryLoadingErrorDialog = false;
        } else {
            shouldShowDiaryLoadingErrorDialog = true;
        }
    }

    private void showDiaryDeleteErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog(getString(R.string.dialog_message_title_communication_error), getString(R.string.dialog_message_message_diary_delete_error));
            shouldShowDiaryDeleteErrorDialog = false;
        } else {
            shouldShowDiaryDeleteErrorDialog = true;
        }
    }

    private void showMessageDialog(String title, String message) {
        NavDirections action =
                DiaryShowFragmentDirections.actionDiaryShowFragmentToMessageDialog(title, message);
        navController.navigate(action);
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

}
