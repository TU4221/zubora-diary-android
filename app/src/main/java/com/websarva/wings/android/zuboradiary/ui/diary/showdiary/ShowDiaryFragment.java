package com.websarva.wings.android.zuboradiary.ui.diary.showdiary;

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
import com.websarva.wings.android.zuboradiary.data.diary.ConditionConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.data.diary.WeatherConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;
import com.websarva.wings.android.zuboradiary.databinding.FragmentShowDiaryBinding;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.observer.ShowDiaryNumVisibleItemsObserver;
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
    private DiaryViewModel diaryViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryViewModel = provider.get(DiaryViewModel.class);
        diaryViewModel.initialize();

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
        binding.setDiaryViewModel(diaryViewModel);

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
        setupItemLayout();
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
                    if (canShowDialog()) {
                        retryErrorDialogShow();
                    }
                }
            }
        };
        navBackStackEntry.getLifecycle().addObserver(lifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                Log.d("LifecycleEventObserver", "CalendarFragment_ViewLifecycleOwner_event:" + event);
                if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                    // TODO:下記コード意味あるか検証。コメントアウトしてFragment切替後の状態を確認したがObserverが重複することはなかった。
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });
    }

    // 画面表示データ準備
    private void setUpDiaryData() {
        LocalDate showDiaryDate =
                ShowDiaryFragmentArgs.fromBundle(requireArguments()).getShowDiaryDate();
        diaryViewModel.prepareDiary(showDiaryDate, true);
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
                        if (item.getItemId() == R.id.displayDiaryToolbarOptionEditDiary) {
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
                        }
                        return false;
                    }
                });

        diaryViewModel.getLoadedDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s == null) {
                            return;
                        }
                        binding.materialToolbarTopAppBar.setTitle(s);
                    }
                });
    }

    // 天気表示欄設定
    private void setUpWeatherLayout() {
        diaryViewModel.getWeather1LiveData()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        if (integer == null) {
                            return;
                        }
                        // StringWeather1LiveDataへ反映
                        WeatherConverter converter = new WeatherConverter();
                        Weathers weather = converter.toWeather(integer);
                        String strWeather = diaryViewModel.getStrWeather1LiveData().getValue();
                        if (strWeather == null || !weather.toString(requireContext()).equals(strWeather)) {
                            diaryViewModel.updateStrWeather1(weather.toString(requireContext()));
                        }
                    }
                });

        diaryViewModel.getWeather2LiveData()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        if (integer == null) {
                            return;
                        }
                        // StringWeather2LiveDataへ反映
                        WeatherConverter converter = new WeatherConverter();
                        Weathers weather = converter.toWeather(integer);
                        String strWeather = diaryViewModel.getStrWeather1LiveData().getValue();
                        if (strWeather == null || !weather.toString(requireContext()).equals(strWeather)) {
                            diaryViewModel.updateStrWeather2(weather.toString(requireContext()));
                        }
                    }
                });

        diaryViewModel.getWeather2LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new ShowDiaryWeather2Observer(
                                binding.includeShowDiary.textWeatherSlush,
                                binding.includeShowDiary.textWeather2Selected
                        )
                );
    }

    private void setUpConditionLayout() {
        diaryViewModel.getConditionLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        if (integer == null) {
                            return;
                        }
                        // StringConditionLiveDataへ反映
                        ConditionConverter converter = new ConditionConverter();
                        Conditions condition = converter.toCondition(integer);
                        String strCondition = diaryViewModel.getStrConditionLiveData().getValue();
                        if (strCondition == null || !condition.toString(requireContext()).equals(strCondition)) {
                            diaryViewModel.updateStrCondition(condition.toString(requireContext()));
                        }
                    }
                });
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
        for (int i = 0; i < DiaryViewModel.MAX_ITEMS; i++) {
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

    private void setupItemLayout() {
        View[] itemLayouts = new View[DiaryViewModel.MAX_ITEMS];
        itemLayouts[0] = binding.includeShowDiary.includeItem1.linerLayoutShowDiaryItem;
        itemLayouts[1] = binding.includeShowDiary.includeItem2.linerLayoutShowDiaryItem;
        itemLayouts[2] = binding.includeShowDiary.includeItem3.linerLayoutShowDiaryItem;
        itemLayouts[3] = binding.includeShowDiary.includeItem4.linerLayoutShowDiaryItem;
        itemLayouts[4] = binding.includeShowDiary.includeItem5.linerLayoutShowDiaryItem;
        diaryViewModel.getNumVisibleItemsLiveData()
                .observe(getViewLifecycleOwner(), new ShowDiaryNumVisibleItemsObserver(itemLayouts));
    }

    private void setUpErrorObserver() {
        // エラー表示
        diaryViewModel.getIsDiaryLoadingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showDiaryLoadingErrorDialog();
                    diaryViewModel.clearDiaryLoadingError(false);
                }
            }
        });
        diaryViewModel.getIsDiaryDeleteErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showDiaryDeleteErrorDialog();
                    diaryViewModel.clearDiaryDeleteError(false);
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
        return currentDestinationId == R.id.navigation_edit_diary_fragment;
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
                LocalDate showedDiaryLocalDate = diaryViewModel.getDateLiveData().getValue();
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
