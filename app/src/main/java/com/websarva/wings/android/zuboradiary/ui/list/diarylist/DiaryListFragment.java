package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.graphics.Canvas;
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
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListListenerSetting;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryListFragment extends Fragment {

    // View関係
    private FragmentDiaryListBinding binding;
    private CustomLinearLayoutManager diaryListYearMonthLinearLayoutManager;

    // Navigation関係
    private NavController navController;
    private boolean shouldShowDiaryListLoadingErrorDialog;
    private boolean shouldShowDiaryInformationLoadingErrorDialog;
    private boolean shouldShowDiaryDeleteErrorDialog;

    // ViewModel
    private DiaryListViewModel diaryListViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("フラグメントライフサイクル確認", "onCreate()処理");
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
        Log.d("フラグメントライフサイクル確認", "onCreateView()処理");
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
                    receiveConfirmDeleteDialogResults(savedStateHandle);
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
    private void receiveConfirmDeleteDialogResults(SavedStateHandle savedStateHandle) {
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
        RecyclerView recyclerDiaryYearMonthList = binding.recyclerDiaryYearMonthList;
        diaryListYearMonthLinearLayoutManager = new CustomLinearLayoutManager(getContext());
        recyclerDiaryYearMonthList.setLayoutManager(diaryListYearMonthLinearLayoutManager);
        DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                new DiaryYearMonthListAdapter(requireContext(), this::showShowDiaryFragment, true);
        recyclerDiaryYearMonthList.setAdapter(diaryYearMonthListAdapter);
        // HACK:下記問題が発生する為アイテムアニメーションを無効化
        //      問題1.アイテム追加時もやがかかる。今回の構成(親Recycler:年月、子Recycler:日)上、
        //           既に表示されている年月に日のアイテムを追加すると、年月のアイテムに変更アニメーションが発生してしまう。
        //           これに対して、日のアイテムに追加アニメーションを発生させようとすると、
        //           年月のアイテムのサイズ変更にアニメーションが発生せず全体的に違和感となるアニメーションになってしまう。
        //      問題2.最終アイテムまで到達し、ProgressBarが消えた後にセクションバーがその分ずれる)
        recyclerDiaryYearMonthList.setItemAnimator(null);

        DiaryListListenerSetting diaryListListenerSetting = new DiaryListListenerSetting() {
            @Override
            public boolean isLoadingDiaryList() {
                return diaryListViewModel.getIsLoading();
            }

            @Override
            public void loadDiaryList() {
                diaryListViewModel.loadList(DiaryListViewModel.LoadType.ADD);
            }
        };
        diaryListListenerSetting
                .setUp(
                        recyclerDiaryYearMonthList,
                        DiaryYearMonthListAdapter.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL
                );

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
                        List<DiaryYearMonthListItemBase> convertedList = new ArrayList<>(diaryListItems);
                        diaryYearMonthListAdapter.submitList(convertedList);
                    }
                }
        );

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

    // TODO:下記必要か確認
    //日記リスト(年月)(リサイクラービュー)のライナーレイアウトマネージャークラス
    //(コンストラクタに引数があるため、匿名クラス作成不可。メンバクラス作成。)
    private static class CustomLinearLayoutManager extends LinearLayoutManager {
        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        //recyclerView.smoothScrollToPositionの挙動をWEBを参考にオーバーライドで修正(修正出来てるか怪しい？)
        //https://qiita.com/Air_D/items/c253d385f9d443283602
        @Override
        public void smoothScrollToPosition(
                RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                @Override
                protected int getVerticalSnapPreference() {
                    //下記リターン値を固定とした。
                    return SNAP_TO_START;
                }
            };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }
    }

    // TODO:上手くいかないので保留
    private static class DiaryDayListSimpleCallBack extends ItemTouchHelper.SimpleCallback {
        SwipedDiaryDate swipedDate;
        public DiaryDayListSimpleCallBack(int dragDirs, int swipeDirs, SwipedDiaryDate swipedDate) {
            super(dragDirs, swipeDirs);
            this.swipedDate = swipedDate;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return 0.5F;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder =
                    (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
            swipedDate.setDate(diaryDayListViewHolder.date);
            Log.d("20240701_1", "onSwiped");
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX,
                                float dY,
                                int actionState,
                                boolean isCurrentlyActive) {
            Log.d("20240701", "dX:" + dX);
            Log.d("20240701", "viewHolderWidth:" + viewHolder.itemView.getWidth());

            View itemView = viewHolder.itemView;
            float transitionX =  dX;
            float absTransitionX =  Math.abs(dX);
            if (absTransitionX > itemView.getWidth()) {
                transitionX = -((float) itemView.getWidth() / 4);
            }

            super.onChildDraw(c, recyclerView, viewHolder, transitionX, dY, actionState, isCurrentlyActive);
            DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder =
                    (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.frameLayoutRowDiaryDayList,
                            0, 0, actionState, isCurrentlyActive
                    );

            /*if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
                DiaryDayListViewHolder diaryDayListViewHolder = (DiaryDayListViewHolder) viewHolder;
                diaryDayListViewHolder.binding.linerLayoutFront.setTranslationX(dX);
            }*/

            /*super.onChildDraw(c, recyclerView, viewHolder, transitionX, dY, actionState, isCurrentlyActive);
            DiaryDayListViewHolder diaryDayListViewHolder = (DiaryDayListViewHolder) viewHolder;
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.frameLayoutRowDiaryDayList,
                            0, 0, actionState, isCurrentlyActive
                    );
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.linerLayoutFront,
                            transitionX, dY, actionState, isCurrentlyActive
                    );*/


            /*View itemView = viewHolder.itemView;
            float transitionX =  dX;
            float absTransitionX =  Math.abs(dX);
            if (absTransitionX > itemView.getWidth()) {
                transitionX = -((float) itemView.getWidth() / 4);
            }
            super.onChildDraw(c, recyclerView, viewHolder, transitionX, dY, actionState, isCurrentlyActive);

            DiaryDayListViewHolder diaryDayListViewHolder = (DiaryDayListViewHolder) viewHolder;
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.textDeleteDiary,
                            0, 0, actionState, isCurrentlyActive
                    );
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.linerLayoutFront,
                            dX, dY, actionState, isCurrentlyActive
                    );*/
        }

        @Override
        public void clearView(     @NonNull androidx.recyclerview.widget.RecyclerView recyclerView,
                                   @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder ) {
            super.clearView(recyclerView, viewHolder);
            Log.d("20240701_1", "clearView");
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

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    public void diaryListScrollToFirstPosition() {
        Log.d("ボトムナビゲーションタップ確認", "scrollToFirstPosition()呼び出し");
        int position;
        position = diaryListYearMonthLinearLayoutManager.findLastVisibleItemPosition();
        Log.d("スクロール動作確認", "position：" + position);

        // TODO:保留
        // 日記リスト(年月)のアイテム数が多い場合、
        // ユーザーが数多くのアイテムをスクロールした状態でsmoothScrollToPosition()を起動すると先頭にたどり着くのに時間がかかる。
        // その時間を回避する為に先頭付近へジャンプ(scrollToPosition())してからsmoothScrollToPosition()を起動させたかったが、
        // エミュレーターでは処理落ちで上手く確認できなかった。(プログラムの可能性もある)
                /*
                if (position >= 1) {
                    Log.d("スクロール動作確認", "scrollToPosition()呼出");
                    diaryListYearMonthRecyclerView.scrollToPosition(1);
                }
                 */

        Log.d("スクロール動作確認", "smoothScrollToPosition()呼出");
        binding.recyclerDiaryYearMonthList.smoothScrollToPosition(0);
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
            showMessageDialog("通信エラー", "日記リストの読込に失敗しました。");
            shouldShowDiaryListLoadingErrorDialog = false;
        } else {
            shouldShowDiaryListLoadingErrorDialog = true;
        }
    }

    private void showDiaryInformationLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "日記情報の読込に失敗しました。");
            shouldShowDiaryInformationLoadingErrorDialog = false;
        } else {
            shouldShowDiaryInformationLoadingErrorDialog = true;
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
}
