package com.websarva.wings.android.zuboradiary.ui.list;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DiaryListViewModel extends ViewModel {

    private final DiaryRepository diaryRepository;
    private Future<?> LoadingDiaryListFuture;
    private final MutableLiveData<List<DiaryYearMonthListItem>> diaryList = new MutableLiveData<>();
    private boolean isLoading;
    private final MutableLiveData<Boolean> isVisibleDiaryList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    private static final int LOAD_ITEM_NUM = 10; // TODO:仮数値の為、最後に設定
    private LocalDate sortConditionDate;
    private final ExecutorService executorService;

    // エラー関係
    private final MutableLiveData<Boolean> isDiaryListLoadingError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDiaryInformationLoadingError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDiaryDeleteError = new MutableLiveData<>();


    @Inject
    public DiaryListViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
        executorService = Executors.newSingleThreadExecutor();
        initialize();
    }

    public void initialize() {
        diaryList.setValue(new ArrayList<>());
        isLoading = false;
        isVisibleUpdateProgressBar.setValue(false);
        isVisibleUpdateProgressBar.setValue(false);
        sortConditionDate = null;
        isDiaryListLoadingError.setValue(false);
        isDiaryInformationLoadingError.setValue(false);
        isDiaryDeleteError.setValue(false);
    }

    public enum LoadType {
        NEW, UPDATE, ADD
    }

    public void loadList(LoadType loadType) {
        if (LoadingDiaryListFuture != null && !LoadingDiaryListFuture.isDone()) {
            LoadingDiaryListFuture.cancel(true);
        }
        LoadingDiaryListFuture = executorService.submit(new Runnable() {
            @Override
            public void run() {
                // 日記リスト読込準備
                Log.d("DiaryListLoading", "prepare");
                isLoading = true;
                isVisibleDiaryList.postValue(true);
                if (loadType == LoadType.UPDATE) {
                    isVisibleUpdateProgressBar.postValue(true);
                } else {
                    isVisibleUpdateProgressBar.postValue(false);
                }
                int numLoadingItems;
                int loadingOffset;
                if (loadType == LoadType.UPDATE) {
                    Log.d("DiaryListLoading", "beforeStartLoading1");
                    numLoadingItems = countDiaryListDayItem(diaryList.getValue());
                    Log.d("DiaryListLoading", "beforeStartLoading2");
                    loadingOffset = 0;
                } else if (loadType == LoadType.ADD) {
                    numLoadingItems = LOAD_ITEM_NUM;
                    loadingOffset = countDiaryListDayItem(diaryList.getValue());
                } else {
                    // LoadType.NEW
                    numLoadingItems = LOAD_ITEM_NUM;
                    loadingOffset = 0;
                }

                // 現時点のDiaryListをCloneで生成
                List<DiaryYearMonthListItem> currentDiaryList = diaryList.getValue();
                List<DiaryYearMonthListItem> previousDiaryList = new ArrayList<>();
                if (loadType != LoadType.NEW) {
                    for (DiaryYearMonthListItem item : currentDiaryList) {
                        DiaryYearMonthListItem cloneItem = item.clone();
                        previousDiaryList.add(cloneItem);
                    }
                }

                // ProgressBar表示
                List<DiaryYearMonthListItem> diaryListContainingProgressBar = new ArrayList<>();
                if (loadType != LoadType.NEW) {
                    diaryListContainingProgressBar.addAll(previousDiaryList);
                }
                if (loadType != LoadType.UPDATE) {
                    DiaryYearMonthListItem progressBar =
                            new DiaryYearMonthListItem(
                                    DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR
                            );
                    diaryListContainingProgressBar.add(progressBar);
                }
                diaryList.postValue(diaryListContainingProgressBar);

                // TODO:ProgressBarを表示させる為に仮で記述
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    diaryList.postValue(previousDiaryList);
                    throw new RuntimeException(e);
                }

                // 日記リスト読込
                Log.d("DiaryListLoading", "startLoading");
                int numExistingDiaries;
                List<DiaryYearMonthListItem> loadedData;
                try {
                    ListenableFuture<Integer> listenableFuture =
                            diaryRepository.countDiaries(sortConditionDate);
                    // 日付が変更された時、カウントキャンセル
                    // TODO:下記while意味ある？
                    while (!listenableFuture.isDone()) {
                        if (Thread.currentThread().isInterrupted()) {
                            listenableFuture.cancel(true);
                            throw new InterruptedException();
                        }
                    }
                    numExistingDiaries = listenableFuture.get();
                    loadedData =
                            diaryRepository.loadDiaryList(
                                    numLoadingItems,
                                    loadingOffset,
                                    sortConditionDate
                            );
                } catch (InterruptedException e) {
                    // TODO:例外を分けている理由は不明
                    diaryList.postValue(previousDiaryList);
                    return;
                } catch (Exception e) {
                    diaryList.postValue(previousDiaryList);
                    isVisibleUpdateProgressBar.postValue(false);
                    isLoading = false;
                    isDiaryListLoadingError.postValue(true);
                    return;
                }


                // 更新用日記リスト準備
                List<DiaryYearMonthListItem> updateDiaryList = new ArrayList<>();
                if (loadType == LoadType.ADD) {
                    for (DiaryYearMonthListItem item : previousDiaryList) {
                        DiaryYearMonthListItem cloneItem = item.clone();
                        updateDiaryList.add(cloneItem);
                    }
                }


                // 読込データを更新用日記リストへ追加
                Log.d("DiaryListLoading", "LoadedDataSize:" + loadedData.size());
                if (!loadedData.isEmpty()) {
                    if (loadType == LoadType.ADD) {
                        // 前回の読込リストの最終アイテムの年月取得
                        int previousDiaryListLastItemPosition = previousDiaryList.size() - 1;
                        DiaryYearMonthListItem previousDiaryYearMonthListLastItem =
                                previousDiaryList.get(previousDiaryListLastItemPosition);
                        YearMonth previousDiaryYearMonthListLastItemYearMonth =
                                previousDiaryYearMonthListLastItem.getYearMonth();

                        // 今回の読込リストの先頭アイテムの年月取得
                        DiaryYearMonthListItem additionalDiaryListFirstItem =
                                loadedData.get(0);
                        YearMonth additionalDiaryListFirstItemYearMonth =
                                additionalDiaryListFirstItem.getYearMonth();

                        // 前回の読込リストに今回の読込リストの年月が含まれていたら,
                        // そこにDiaryDayListItemを足し込む
                        if (previousDiaryYearMonthListLastItemYearMonth
                                == additionalDiaryListFirstItemYearMonth) {
                            List<DiaryDayListItem> additionalDiaryDayListItemList =
                                    additionalDiaryListFirstItem.getDiaryDayListItemList();
                            updateDiaryList.get(previousDiaryListLastItemPosition)
                                    .getDiaryDayListItemList().addAll(additionalDiaryDayListItemList);
                            loadedData.remove(0);
                        }
                    }
                    updateDiaryList.addAll(loadedData);
                }


                // 次回読み込む日記あり確認
                boolean existsUnloadedDiaries =
                        countDiaryListDayItem(updateDiaryList) < numExistingDiaries;
                if (numExistingDiaries > 0 && !existsUnloadedDiaries) {
                    DiaryYearMonthListItem noDiaryMessage =
                            new DiaryYearMonthListItem(
                                    DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE
                            );
                    updateDiaryList.add(noDiaryMessage);
                }

                // 日記リスト読込完了処理
                if (updateDiaryList.isEmpty()) {
                    isVisibleDiaryList.postValue(false);
                }
                diaryList.postValue(updateDiaryList);
                isVisibleUpdateProgressBar.postValue(false);
                isLoading = false;
            }
        });
    }

    private int countDiaryListDayItem(List<DiaryYearMonthListItem> diaryList) {
        int count = 0;
        for (DiaryYearMonthListItem item: diaryList) {
            if (item.getViewType() == DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
                count += item.getDiaryDayListItemList().size();
            }
        }
        return count;
    }

    public void updateSortConditionDate(YearMonth yearMonth) {
        sortConditionDate= yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth());
    }

    public void deleteDiary(LocalDate date) {
        Integer result;
        try {
            result = diaryRepository.deleteDiary(date).get();
        } catch (Exception e) {
            isDiaryDeleteError.setValue(true);
            return;
        }
        if (result == null) {
            return;
            // TODO:assert
        }
        // TODO:resultの成功値確認
        if (result != 0) {
            isDiaryDeleteError.setValue(true);
            return;
        }

        List<DiaryYearMonthListItem> currentDiaryList = diaryList.getValue();
        if (currentDiaryList == null) {
            // TODO:assert
            return;
        }
        DiaryYearMonthListItem targetYearMonthListItem =
                new DiaryYearMonthListItem(
                        DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE
                );
        List<DiaryDayListItem> targetDayList = new ArrayList<>();

        for (DiaryYearMonthListItem item: currentDiaryList) {
            YearMonth deleteDiaryYearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
            if (item.getYearMonth().equals(deleteDiaryYearMonth)) {
                targetYearMonthListItem = item;
                targetDayList = item.getDiaryDayListItemList();
                break;
            }
        }

        for (DiaryDayListItem item: targetDayList) {
            if (item.getDate().equals(date)) {
                targetDayList.remove(item);
                if (targetDayList.isEmpty()) {
                    currentDiaryList.remove(targetYearMonthListItem);
                }
                break;
            }
        }

        List<DiaryYearMonthListItem> updateDiaryList = new ArrayList<>(currentDiaryList);
        diaryList.postValue(updateDiaryList);

    }

    @Nullable
    public Integer countDiaries() {
        try {
            return diaryRepository.countDiaries(null).get();
        } catch (Exception e) {
            isDiaryInformationLoadingError.setValue(true);
            return null;
        }
    }


    @Nullable
    public Diary loadNewestDiary() {
        try {
            return diaryRepository.selectNewestDiary().get();
        } catch (Exception e) {
            isDiaryInformationLoadingError.setValue(true);
            return null;
        }
    }

    @Nullable
    public Diary loadOldestDiary() {
        try {
            return diaryRepository.selectOldestDiary().get();
        } catch (Exception e) {
            isDiaryListLoadingError.setValue(true);
            return null;
        }
    }

    // エラー関係
    public void clearIsDiaryListLoadingError() {
        isDiaryListLoadingError.setValue(false);
    }

    public void clearIsDiaryInformationLoadingError() {
        isDiaryInformationLoadingError.setValue(false);
    }

    public void clearIsDiaryDeleteError() {
        isDiaryDeleteError.setValue(false);
    }

    // Getter
    public boolean getIsLoading() {
        return isLoading;
    }

    // LiveDataGetter
    public LiveData<List<DiaryYearMonthListItem>> getDiaryListLiveData() {
        return diaryList;
    }

    public LiveData<Boolean> getIsVisibleUpdateProgressBarLiveData() {
        return isVisibleUpdateProgressBar;
    }

    public LiveData<Boolean> getIsVisibleDiaryListLiveData() {
        return isVisibleDiaryList;
    }

    public LiveData<Boolean> getIsDiaryListLoadingErrorLiveData() {
        return isDiaryListLoadingError;
    }

    public LiveData<Boolean> getIsDiaryInformationLoadingErrorLiveData() {
        return isDiaryInformationLoadingError;
    }

    public LiveData<Boolean> getIsDiaryDeleteErrorLiveData() {
        return isDiaryDeleteError;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
