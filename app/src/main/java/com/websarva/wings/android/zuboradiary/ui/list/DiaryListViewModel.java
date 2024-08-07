package com.websarva.wings.android.zuboradiary.ui.list;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.DateConverter;
import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DiaryListViewModel extends ViewModel {

    private DiaryRepository diaryRepository;
    private Future<?> LoadingDiaryListFuture;
    private MutableLiveData<List<DiaryYearMonthListItem>> diaryList = new MutableLiveData<>();
    private boolean isLoading;
    private  MutableLiveData<Boolean> isVisibleDiaryList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    private final int LOAD_ITEM_NUM = 10; // TODO:仮数値の為、最後に設定
    private String sortConditionDate;
    private ExecutorService executorService;


    @Inject
    public DiaryListViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        initialize();
    }

    public void initialize() {
        this.diaryList.setValue(new ArrayList<>());
        this.isLoading = false;
        this.isVisibleUpdateProgressBar.setValue(false);
        this.isVisibleUpdateProgressBar.setValue(false);
        this.sortConditionDate = "";
    }

    public enum LoadType {
        NEW, UPDATE, ADD
    }

    public void loadList(LoadType loadType, Runnable exceptionHandling) {
        if (this.LoadingDiaryListFuture != null && !this.LoadingDiaryListFuture.isDone()) {
            this.LoadingDiaryListFuture.cancel(true);
        }
        Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());
        this.LoadingDiaryListFuture = executorService.submit(new Runnable() {
            @Override
            public void run() {
                // 日記リスト読込準備
                DiaryListViewModel.this.isLoading = true;
                DiaryListViewModel.this.isVisibleDiaryList.postValue(true);
                if (loadType == LoadType.UPDATE) {
                    DiaryListViewModel.this.isVisibleUpdateProgressBar.postValue(true);
                } else {
                    DiaryListViewModel.this.isVisibleUpdateProgressBar.postValue(false);
                }
                int numLoadingItems;
                int loadingOffset;
                if (loadType == LoadType.UPDATE) {
                    numLoadingItems = countDiaryListDayItem(DiaryListViewModel.this.diaryList.getValue());
                    loadingOffset = 0;
                } else if (loadType == LoadType.ADD) {
                    numLoadingItems = DiaryListViewModel.this.LOAD_ITEM_NUM;
                    loadingOffset = countDiaryListDayItem(DiaryListViewModel.this.diaryList.getValue());
                } else {
                    // LoadType.NEW
                    numLoadingItems = DiaryListViewModel.this.LOAD_ITEM_NUM;
                    loadingOffset = 0;
                }

                // 現時点のDiaryListをCloneで生成
                List<DiaryYearMonthListItem> currentDiaryList =
                        DiaryListViewModel.this.diaryList.getValue();
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
                    DiaryYearMonthListItem progressBar = new DiaryYearMonthListItem();
                    progressBar.setViewType(
                            DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR);
                    diaryListContainingProgressBar.add(progressBar);
                }
                DiaryListViewModel.this.diaryList.postValue(diaryListContainingProgressBar);

                // TODO:ProgressBarを表示させる為に仮で記述
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    DiaryListViewModel.this.diaryList.postValue(previousDiaryList);
                    throw new RuntimeException(e);
                }

                // 日記リスト読込
                int numExistingDiaries;
                List<DiaryYearMonthListItem> loadedData;
                try {
                    LocalDate date = DateConverter.toLocalDate(sortConditionDate);
                    ListenableFuture<Integer> listenableFuture =
                            diaryRepository.countDiaries(date);
                    // 日付が変更された時、カウントキャンセル
                    // TODO:下記while意味ある？
                    while (!listenableFuture.isDone()) {
                        if (Thread.currentThread().isInterrupted()) {
                            listenableFuture.cancel(true);
                            throw new InterruptedException();
                        }
                    }
                    numExistingDiaries = listenableFuture.get();
                    if (DiaryListViewModel.this.sortConditionDate.isEmpty()) {
                        loadedData =
                                DiaryListViewModel.this.diaryRepository.loadDiaryList(
                                        numLoadingItems,
                                        loadingOffset,
                                        null
                                );
                    } else {
                        loadedData =
                                DiaryListViewModel.this.diaryRepository.loadDiaryList(
                                        numLoadingItems,
                                        loadingOffset,
                                        DateConverter.toLocalDate(sortConditionDate)
                                );
                    }
                } catch (InterruptedException e) {
                    DiaryListViewModel.this.diaryList.postValue(previousDiaryList);
                    return;
                } catch (Exception e) {
                    DiaryListViewModel.this.diaryList.postValue(previousDiaryList);
                    DiaryListViewModel.this.isVisibleUpdateProgressBar.postValue(false);
                    DiaryListViewModel.this.isLoading = false;
                    handler.post(exceptionHandling);
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
                if (!loadedData.isEmpty()) {
                    if (loadType == LoadType.ADD) {
                        // 前回の読込リストの最終アイテムの年月取得
                        int previousDiaryListLastItemPosition = previousDiaryList.size() - 1;
                        DiaryYearMonthListItem previousDiaryYearMonthListLastItem =
                                previousDiaryList.get(previousDiaryListLastItemPosition);
                        int previousDiaryYearMonthListLastItemYear =
                                previousDiaryYearMonthListLastItem.getYear();
                        int previousDiaryYearMonthListLastItemMonth =
                                previousDiaryYearMonthListLastItem.getMonth();

                        // 今回の読込リストの先頭アイテムの年月取得
                        DiaryYearMonthListItem additionalDiaryListFirstItem =
                                loadedData.get(0);
                        int additionalDiaryListFirstItemYear =
                                additionalDiaryListFirstItem.getYear();
                        int additionalDiaryListFirstItemMonth =
                                additionalDiaryListFirstItem.getMonth();

                        // 前回の読込リストに今回の読込リストの年月が含まれていたら,
                        // そこにDiaryDayListItemを足し込む
                        if (previousDiaryYearMonthListLastItemYear == additionalDiaryListFirstItemYear
                                && previousDiaryYearMonthListLastItemMonth == additionalDiaryListFirstItemMonth) {
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
                    DiaryYearMonthListItem noDiaryMessage = new DiaryYearMonthListItem();
                    noDiaryMessage.setViewType(
                            DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE);
                    updateDiaryList.add(noDiaryMessage);
                }

                // 日記リスト読込完了処理
                if (updateDiaryList.isEmpty()) {
                    DiaryListViewModel.this.isVisibleDiaryList.postValue(false);
                }
                DiaryListViewModel.this.diaryList.postValue(updateDiaryList);
                DiaryListViewModel.this.isVisibleUpdateProgressBar.postValue(false);
                DiaryListViewModel.this.isLoading = false;
            }
        });
    }

    private int countDiaryListDayItem(List<DiaryYearMonthListItem> diaryList) {
        int count = 0;
        for (DiaryYearMonthListItem item: diaryList) {
            count += item.getDiaryDayListItemList().size();
        }
        return count;
    }

    public void updateSortConditionDate(int year, int month) {
        // 日付データ作成。
        // https://qiita.com/hanaaaa/items/8555aaabc6b949ec507d
        // https://nainaistar.hatenablog.com/entry/2021/05/13/120000
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate lastDayOfMonthDate = LocalDate
                    .of(year, month, 1)
                    .with(TemporalAdjusters.lastDayOfMonth());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            this.sortConditionDate= lastDayOfMonthDate.format(formatter);
        }
    }

    public void deleteDiary(LocalDate date) {
        Integer result;
        try {
            result = diaryRepository.deleteDiary(date).get();
        } catch (Exception e) {
            // TODO:ERROR
            return;
        }
        if (result == null) {
            return;
            // TODO:assert
        }
        // TODO:resultの成功値確認
        if (result != 0) {
            return;
            // TODO:ERROR
        }
        List<DiaryYearMonthListItem> updateDiaryList = new ArrayList<>();
        List<DiaryYearMonthListItem> currentDiaryList = DiaryListViewModel.this.diaryList.getValue();

        DiaryYearMonthListItem targetYearMonthListItem = new DiaryYearMonthListItem();
        List<DiaryDayListItem> targetDayList = new ArrayList<>();
        for (DiaryYearMonthListItem item: currentDiaryList) {
            if (item.getYear() == date.getYear()
                    && item.getMonth() == date.getMonthValue()) {
                targetYearMonthListItem = item;
                targetDayList = item.getDiaryDayListItemList();
                break;
            }
        }

        for (DiaryDayListItem item: targetDayList) {
            if (item.getDayOfMonth() == date.getDayOfMonth()) {
                targetDayList.remove(item);
                if (targetDayList.isEmpty()) {
                    currentDiaryList.remove(targetYearMonthListItem);
                }
                break;
            }
        }

        updateDiaryList.addAll(currentDiaryList);
        DiaryListViewModel.this.diaryList.postValue(updateDiaryList);

    }

    public int countDiaries() throws Exception {
        return this.diaryRepository.countDiaries(null).get();
    }


    public Diary loadNewestDiary() throws Exception {
        return this.diaryRepository.selectNewestDiary().get();
    }

    public Diary loadOldestDiary() throws Exception {
        return this.diaryRepository.selectOldestDiary().get();
    }


    // Getter/Setter
    public LiveData<List<DiaryYearMonthListItem>> getLiveDataDiaryList() {
        return this.diaryList;
    }
    public void setLiveDataDiaryList(List<DiaryYearMonthListItem> list) {
        this.diaryList.setValue(list);
    }

    public boolean getIsLoading() {
        return this.isLoading;
    }

    public LiveData<Boolean> getLiveIsVisibleUpdateProgressBar() {
        return this.isVisibleUpdateProgressBar;
    }

    public LiveData<Boolean> getLiveIsVisibleDiaryList() {
        return this.isVisibleDiaryList;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.executorService.shutdown();
    }
}
