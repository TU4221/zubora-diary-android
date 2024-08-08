package com.websarva.wings.android.zuboradiary.ui.diary.showdiary;

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
import com.websarva.wings.android.zuboradiary.databinding.FragmentShowDiaryBinding;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.observer.ShowDiaryConditionObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.ShowDiaryLogObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.ShowDiaryNumVisibleItemsObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.ShowDiaryWeather1Observer;
import com.websarva.wings.android.zuboradiary.ui.observer.ShowDiaryWeather2Observer;

import java.time.LocalDate;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ShowDiaryFragment extends Fragment {

    // View関係
    private FragmentShowDiaryBinding binding;// 項目入力欄最大数

    // Navigation関係
    private NavController navController;
    private static final String fromClassName = "From" + ShowDiaryFragment.class.getName();
    public static final String KEY_SHOWED_DIARY_DATE = "ShowedDiaryDate" + fromClassName;
    private boolean shouldShowDiaryLoadingErrorDialog;
    private boolean shouldShowDiaryDeleteErrorDialog;

    // ViewModel
    private ShowDiaryViewModel showDiaryViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(this);
        showDiaryViewModel = provider.get(ShowDiaryViewModel.class);

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
        binding = FragmentShowDiaryBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        binding.setLifecycleOwner(this);
        binding.setShowDiaryViewModel(showDiaryViewModel);

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
                Log.d("LifecycleEventObserver", "CalendarFragment_NavBackStackEntry_event:" + event);
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
                Log.d("LifecycleEventObserver", "CalendarFragment_ViewLifecycleOwner_event:" + event);
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
        savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
    }

    // 日記削除確認ダイアログフラグメントからデータ受取
    private void receiveDeleteConfirmationDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(DeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        if (containsDialogResult) {
            Integer selectedButton =
                    savedStateHandle.get(DeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
            if (selectedButton == null) {
                return;
            }
            if (selectedButton == Dialog.BUTTON_POSITIVE) {
                showDiaryViewModel.deleteDiary();
                backFragment(true);
            }
        }
    }

    // 画面表示データ準備
    private void setUpDiaryData() {
        showDiaryViewModel.initialize();
        LocalDate showDiaryDate =
                ShowDiaryFragmentArgs.fromBundle(requireArguments()).getShowDiaryDate();
        showDiaryViewModel.loadDiary(showDiaryDate);
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
                        if (item.getItemId() == R.id.showDiaryToolbarOptionEditDiary) {
                            LocalDate editDiaryDate =
                                    ShowDiaryFragmentArgs.fromBundle(requireArguments())
                                            .getShowDiaryDate();
                            NavDirections action =
                                    ShowDiaryFragmentDirections
                                            .actionNavigationShowDiaryFragmentToEditDiaryFragment(
                                                    false,
                                                    false,
                                                    editDiaryDate
                                            );
                            navController.navigate(action);
                            return true;
                        } else if (item.getItemId() == R.id.showDiaryToolbarOptionDeleteDiary) {
                            showDiaryDeleteConfirmationDialog();
                        }
                        return false;
                    }
                });

        showDiaryViewModel.getDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
                    @Override
                    public void onChanged(LocalDate date) {
                        if (date == null) {
                            return;
                        }
                        String stringDate = DateConverter.toStringLocalDate(date);
                        binding.materialToolbarTopAppBar.setTitle(stringDate);
                    }
                });
    }

    // 天気表示欄設定
    private void setUpWeatherLayout() {
        showDiaryViewModel.getWeather1LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new ShowDiaryWeather1Observer(
                                requireContext(),
                                binding.includeShowDiary.textWeather1Selected
                        )
                );

        showDiaryViewModel.getWeather2LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new ShowDiaryWeather2Observer(
                                requireContext(),
                                binding.includeShowDiary.textWeatherSlush,
                                binding.includeShowDiary.textWeather2Selected
                        )
                );
    }

    private void setUpConditionLayout() {
        showDiaryViewModel.getConditionLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new ShowDiaryConditionObserver(
                                requireContext(),
                                binding.includeShowDiary.textConditionSelected
                        )
                );
    }

    // TODO:不要確認後削除
    /*private MotionLayout selectItemMotionLayout(int itemNumber) {
        switch (itemNumber) {
            case 1:
                return binding.includeShowDiary.includeItem1.motionLayoutShowDiaryItem;
            case 2:
                return binding.includeShowDiary.includeItem2.motionLayoutShowDiaryItem;

            case 3:
                return binding.includeShowDiary.includeItem3.motionLayoutShowDiaryItem;

            case 4:
                return binding.includeShowDiary.includeItem4.motionLayoutShowDiaryItem;

            case 5:
                return binding.includeShowDiary.includeItem5.motionLayoutShowDiaryItem;
            default:
                return null;
        }
    }*/

    // TODO:不要確認後削除
    /*private void setupItemLayout() {
        Integer numVisibleItems = diaryViewModel.getNumVisibleItemsLiveData().getValue();
        if (numVisibleItems == null) {
            return;
        }
        for (int i = 0; i < EditDiaryViewModel.MAX_ITEMS; i++) {
            int itemNumber = i + 1;
            MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
            if (itemMotionLayout == null) {
                return;
            }
            if (itemNumber <= numVisibleItems) {
                itemMotionLayout
                        .transitionToState(R.id.motion_scene_show_diary_item_showed_state, 1);
            } else {
                itemMotionLayout
                        .transitionToState(R.id.motion_scene_show_diary_item_hided_state, 1);
            }
        }
    }*/

    private void setUpItemLayout() {
        View[] itemLayouts = new View[DiaryLiveData.MAX_ITEMS];
        itemLayouts[0] = binding.includeShowDiary.includeItem1.linerLayoutShowDiaryItem;
        itemLayouts[1] = binding.includeShowDiary.includeItem2.linerLayoutShowDiaryItem;
        itemLayouts[2] = binding.includeShowDiary.includeItem3.linerLayoutShowDiaryItem;
        itemLayouts[3] = binding.includeShowDiary.includeItem4.linerLayoutShowDiaryItem;
        itemLayouts[4] = binding.includeShowDiary.includeItem5.linerLayoutShowDiaryItem;
        showDiaryViewModel.getNumVisibleItemsLiveData()
                .observe(getViewLifecycleOwner(), new ShowDiaryNumVisibleItemsObserver(itemLayouts));
    }

    private void setUpLogShowLayout() {
        showDiaryViewModel.getLogLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new ShowDiaryLogObserver(binding.includeShowDiary.textLogValue)
                );
    }

    private void setUpErrorObserver() {
        // エラー表示
        showDiaryViewModel.getIsDiaryLoadingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showDiaryLoadingErrorDialog();
                    showDiaryViewModel.clearDiaryLoadingError();
                }
            }
        });
        showDiaryViewModel.getIsDiaryDeleteErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showDiaryDeleteErrorDialog();
                    showDiaryViewModel.clearDiaryDeleteError();
                }
            }
        });
    }

    private boolean canShowDialog() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination == null) {
            return false;
        }
        int currentDestinationId = navController.getCurrentDestination().getId();
        return currentDestinationId == R.id.navigation_show_diary_fragment;
    }

    private void showDiaryDeleteConfirmationDialog() {
        if (canShowDialog()) {
            LocalDate date = showDiaryViewModel.getDateLiveData().getValue();
            if (date == null) {
                return;
            }
            NavDirections action =
                    ShowDiaryFragmentDirections.actionShowDiaryFragmentToDeleteConfirmationDialog(date);
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
            showMessageDialog("通信エラー", "日記の読込に失敗しました。");
            shouldShowDiaryLoadingErrorDialog = false;
        } else {
            shouldShowDiaryLoadingErrorDialog = true;
        }
    }

    private void showDiaryDeleteErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "日記の削除に失敗しました。");
            shouldShowDiaryDeleteErrorDialog = false;
        } else {
            shouldShowDiaryDeleteErrorDialog = true;
        }
    }

    private void showMessageDialog(String title, String message) {
        NavDirections action =
                ShowDiaryFragmentDirections.actionShowDiaryFragmentToMessageDialog(title, message);
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
                LocalDate showedDiaryLocalDate = showDiaryViewModel.getDateLiveData().getValue();
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
