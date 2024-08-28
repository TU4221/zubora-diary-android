package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding;
import com.websarva.wings.android.zuboradiary.ui.CustomFragment;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryListFragment extends CustomFragment {

    // View関係
    private FragmentDiaryListBinding binding;

    // Navigation関係
    private NavController navController;
    private boolean shouldShowDiaryListLoadingErrorDialog;
    private boolean shouldShowDiaryInformationLoadingErrorDialog;
    private boolean shouldShowDiaryDeleteErrorDialog;

    // ViewModel
    private DiaryListViewModel diaryListViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryListViewModel = provider.get(DiaryListViewModel.class);

        // Navigation設定
        navController = NavHostFragment.findNavController(this);
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        binding = FragmentDiaryListBinding.inflate(inflater, container, false);

        // データバインディング設定
        binding.setLifecycleOwner(this);
        binding.setListViewModel(diaryListViewModel);

        // 画面遷移時のアニメーション設定
        // FROM:遷移元 TO:遷移先
        // FROM - TO の TO として現れるアニメーション
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        // FROM - TO の FROM として消えるアニメーション
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        // TO - FROM の FROM として現れるアニメーション
        MainActivity mainActivity = (MainActivity) requireActivity();
        if (mainActivity.getTabWasSelected()) {
            setReenterTransition(new MaterialFadeThrough());
            mainActivity.resetTabWasSelected();
        } else {
            setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        }
        // TO - FROM の TO として消えるアニメーション
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpDialogResultReceiver();
        setUpToolBar();
        setUpFloatActionButton();
        setUpDiaryList();
        setUpErrorObserver();
    }

    // ダイアログフラグメントからの結果受取設定
    private void setUpDialogResultReceiver() {
        NavBackStackEntry navBackStackEntry =
                navController.getBackStackEntry(R.id.navigation_diary_list_fragment);
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    receiveDatePickerDialogResults(savedStateHandle);
                    receiveDiaryDeleteConfirmationDialogResults(savedStateHandle);
                    removeDialogResults(savedStateHandle);
                    retryErrorDialogShow();
                }
            }
        };
        navBackStackEntry.getLifecycle().addObserver(lifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                    // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                    //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    removeDialogResults(savedStateHandle);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });
    }

    private void removeDialogResults(SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
        savedStateHandle.remove(DiaryDeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
    }

    // 日付入力ダイアログフラグメントから結果受取
    private void receiveDatePickerDialogResults(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
        if (containsDialogResult) {
            YearMonth selectedYearMonth =
                    savedStateHandle.get(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
            if (selectedYearMonth == null) {
                return;
            }
            diaryListViewModel.updateSortConditionDate(selectedYearMonth);
            diaryListScrollToFirstPosition();
            diaryListViewModel.loadList(DiaryListViewModel.LoadType.NEW);
        }
    }

    // 日記削除ダイアログフラグメントから結果受取
    private void receiveDiaryDeleteConfirmationDialogResults(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(DiaryDeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
        if (containsDialogResult) {
            LocalDate deleteDiaryDate =
                    savedStateHandle.get(DiaryDeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
            if (deleteDiaryDate == null) {
                return;
            }
            diaryListViewModel.deleteDiary(deleteDiaryDate);
        }
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
                        String newestDate;
                        String oldestDate;
                        if (newestDiary == null || oldestDiary == null) {
                            return;
                        } else {
                            newestDate = newestDiary.getDate();
                            oldestDate = oldestDiary.getDate();
                        }
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
    private void setUpDiaryList() {
        DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                new DiaryYearMonthListAdapter(
                        requireContext(),
                        binding.recyclerDiaryYearMonthList,
                        new DiaryYearMonthListAdapter.OnScrollEndItemLoadingListener() {
                            @Override
                            public void Load() {
                                diaryListViewModel.loadList(DiaryListViewModel.LoadType.ADD);
                            }
                        },
                        new DiaryYearMonthListAdapter.OnScrollLoadingConfirmationListener() {
                            @Override
                            public boolean isLoading() {
                                return diaryListViewModel.getIsLoading();
                            }
                        },
                        new DiaryYearMonthListAdapter.OnClickChildItemListener() {
                            @Override
                            public void onClick(LocalDate date) {
                                showShowDiaryFragment(date);
                            }
                        },
                        true,
                        new DiaryYearMonthListAdapter.OnClickChildItemBackgroundButtonListener() {
                            @Override
                            public void onClick(LocalDate date) {
                                showDiaryDeleteConfirmationDialog(date);
                            }
                        }
                );
        diaryYearMonthListAdapter.build();

        diaryListViewModel.getDiaryListLiveData().observe(
                getViewLifecycleOwner(),
                new Observer<List<DiaryYearMonthListItem>>() {
                    @Override
                    public void onChanged(List<DiaryYearMonthListItem> diaryListItems) {
                        if (diaryListItems == null) {
                            return;
                        }
                        DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                                (DiaryYearMonthListAdapter)
                                        binding.recyclerDiaryYearMonthList.getAdapter();
                        if (diaryYearMonthListAdapter == null) {
                            return;
                        }

                        if (diaryListItems.isEmpty()) {
                            binding.textDiaryListNoDiaryMessage.setVisibility(View.VISIBLE);
                            binding.recyclerDiaryYearMonthList.setVisibility(View.INVISIBLE);
                        } else {
                            binding.textDiaryListNoDiaryMessage.setVisibility(View.INVISIBLE);
                            binding.recyclerDiaryYearMonthList.setVisibility(View.VISIBLE);
                        }

                        List<DiaryYearMonthListItemBase> convertedList = new ArrayList<>(diaryListItems);

                        Log.d("DiaryList", "submitList前");
                        for (DiaryYearMonthListItemBase i: convertedList) {
                            YearMonth  yearMonth = i.getYearMonth();
                            if (yearMonth == null) {
                                Log.d("DiaryList", "null");
                            } else {
                                Log.d("DiaryList", yearMonth.toString());
                            }
                        }
                        Log.d("DiaryList", "submitList");
                        Log.d("ListAdapterTest", "submitList");
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

    private void setUpErrorObserver() {
        // エラー表示
        diaryListViewModel.getIsDiaryListLoadingErrorLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean) {
                            showDiaryListLoadingErrorDialog();
                            diaryListViewModel.clearIsDiaryListLoadingError();
                        }
                    }
                });

        diaryListViewModel.getIsDiaryInformationLoadingErrorLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean) {
                            showDiaryInformationLoadingErrorDialog();
                            diaryListViewModel.clearIsDiaryInformationLoadingError();
                        }
                    }
                });

        diaryListViewModel.getIsDiaryDeleteErrorLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean) {
                            showDiaryDeleteErrorDialog();
                            diaryListViewModel.clearIsDiaryDeleteError();
                        }
                    }
                });
    }

    private void showEditDiary() {
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
        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToDiaryShowFragment(date);
        navController.navigate(action);
    }

    private void showWordSearchFragment() {
        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToWordSearchFragment();
        navController.navigate(action);
    }

    private void showStartYearMonthPickerDialog(Year newestYear, Year oldestYear) {
        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToStartYearMonthPickerDialog(newestYear, oldestYear);
        navController.navigate(action);
    }

    private void showDiaryDeleteConfirmationDialog(LocalDate date) {
        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToDiaryDeleteConfirmationDialog(date);
        navController.navigate(action);
    }

    // 他のダイアログで表示できなかったダイアログを表示
    private void retryErrorDialogShow() {
        if (shouldShowDiaryListLoadingErrorDialog) {
            showDiaryListLoadingErrorDialog();
            return;
        }
        if (shouldShowDiaryInformationLoadingErrorDialog) {
            showDiaryInformationLoadingErrorDialog();
            return;
        }
        if (shouldShowDiaryDeleteErrorDialog) {
            showDiaryDeleteErrorDialog();
        }
    }

    private void showDiaryListLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog(getString(R.string.dialog_message_title_communication_error), getString(R.string.dialog_message_message_diary_list_loading_error));
            shouldShowDiaryListLoadingErrorDialog = false;
        } else {
            shouldShowDiaryListLoadingErrorDialog = true;
        }
    }

    private void showDiaryInformationLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog(getString(R.string.dialog_message_title_communication_error), getString(R.string.dialog_message_message_diary_information_loading_error));
            shouldShowDiaryInformationLoadingErrorDialog = false;
        } else {
            shouldShowDiaryInformationLoadingErrorDialog = true;
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
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    private boolean canShowDialog() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination == null) {
            return false;
        }
        int currentDestinationId = navController.getCurrentDestination().getId();
        return currentDestinationId == R.id.navigation_diary_list_fragment;
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
}
