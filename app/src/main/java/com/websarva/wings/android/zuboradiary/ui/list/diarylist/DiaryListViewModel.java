package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;

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

    // TODO:Visible変数を削除してFragment上で制御できるか検討
    private final DiaryRepository diaryRepository;
    private Future<?> loadingDiaryListFuture; // キャンセル用
    private final MutableLiveData<List<DiaryYearMonthListItem>> diaryList = new MutableLiveData<>();
    private boolean isLoading;
    private final MutableLiveData<Boolean> isVisibleDiaryList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    private static final int NUM_LOADING_ITEMS = 10; //リストが画面全体に表示される値にすること。 // TODO:仮数値の為、最後に設定
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
        if (loadingDiaryListFuture != null && !loadingDiaryListFuture.isDone()) {
            loadingDiaryListFuture.cancel(true);
        }
        loadingDiaryListFuture = executorService.submit(new Runnable() {
            @Override
            public void run() {
                List<DiaryYearMonthListItem> previousDiaryList = new ArrayList<>();
                try {
                    // 日記リスト読込準備
                    Log.d("DiaryListLoading", "prepare");
                    List<DiaryYearMonthListItem> currentDiaryList = diaryList.getValue();
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
                        if (currentDiaryList == null || currentDiaryList.isEmpty()) {
                            return;
                        }
                        numLoadingItems = countDiaryListDayItem(currentDiaryList);
                        // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
                        //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
                        //      対策として下記コードを記述。
                        if (numLoadingItems < NUM_LOADING_ITEMS) {
                            numLoadingItems = NUM_LOADING_ITEMS;
                        }
                        loadingOffset = 0;
                    } else if (loadType == LoadType.ADD) {
                        numLoadingItems = NUM_LOADING_ITEMS;
                        if (currentDiaryList == null || currentDiaryList.isEmpty()) {
                            // TODO:assert
                            return;
                        } else {
                            loadingOffset = countDiaryListDayItem(currentDiaryList);
                        }
                    } else {
                        // LoadType.NEW
                        numLoadingItems = NUM_LOADING_ITEMS;
                        loadingOffset = 0;
                    }

                    // 現時点のDiaryListをCloneで生成
                    if (loadType != LoadType.NEW) {
                        if (currentDiaryList != null && !currentDiaryList.isEmpty()) {
                            for (DiaryYearMonthListItem item : currentDiaryList) {
                                DiaryYearMonthListItem cloneItem = item.clone();
                                previousDiaryList.add(cloneItem);
                            }
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
                                        DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR
                                );
                        diaryListContainingProgressBar.add(progressBar);
                    }
                    diaryList.postValue(diaryListContainingProgressBar);

                    // TODO:ProgressBarを表示させる為に仮で記述
                    Thread.sleep(1000);

                    // 日記リスト読込
                    Log.d("DiaryListLoading", "startLoading");
                    Integer numExistingDiaries;
                    List<DiaryYearMonthListItem> convertedLoadingData = new ArrayList<>();
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
                    ListenableFuture<List<DiaryListItem>> listListenableFuture =
                            diaryRepository.selectDiaryListOrderByDateDesc(
                                    numLoadingItems,
                                    loadingOffset,
                                    sortConditionDate
                            );

                    // 日付が変更された時、リスト読込キャンセル
                    // TODO:下記while意味ある？
                    while (!listListenableFuture.isDone()) {
                        if (Thread.currentThread().isInterrupted()) {
                            listListenableFuture.cancel(true);
                            throw new InterruptedException();
                        }
                    }
                    List<DiaryListItem> loadingData = listListenableFuture.get();
                    if (!loadingData.isEmpty()) {
                        convertedLoadingData = toDiaryYearMonthListFormat(loadingData);
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
                    Log.d("DiaryListLoading", "LoadedDataSize:" + convertedLoadingData.size());
                    if (!convertedLoadingData.isEmpty()) {
                        if (loadType == LoadType.ADD) {
                            // 前回の読込リストの最終アイテムの年月取得
                            int previousDiaryListLastItemPosition = previousDiaryList.size() - 1;
                            DiaryYearMonthListItem previousDiaryYearMonthListLastItem =
                                    previousDiaryList.get(previousDiaryListLastItemPosition);
                            YearMonth previousDiaryYearMonthListLastItemYearMonth =
                                    previousDiaryYearMonthListLastItem.getYearMonth();

                            // 今回の読込リストの先頭アイテムの年月取得
                            DiaryYearMonthListItem additionalDiaryListFirstItem =
                                    convertedLoadingData.get(0);
                            YearMonth additionalDiaryListFirstItemYearMonth =
                                    additionalDiaryListFirstItem.getYearMonth();

                            // 前回の読込リストに今回の読込リストの年月が含まれていたら,
                            // そこにDiaryDayListItemを足し込む
                            if (previousDiaryYearMonthListLastItemYearMonth
                                    .equals(additionalDiaryListFirstItemYearMonth)) {
                                List<DiaryDayListItem> additionalDiaryDayListItemList =
                                        additionalDiaryListFirstItem.getDiaryDayListItemList();
                                updateDiaryList.get(previousDiaryListLastItemPosition)
                                        .getDiaryDayListItemList().addAll(additionalDiaryDayListItemList);
                                convertedLoadingData.remove(0);
                            }
                        }
                        updateDiaryList.addAll(convertedLoadingData);
                    }

                    // 次回読み込む日記あり確認
                    boolean existsUnloadedDiaries =
                            countDiaryListDayItem(updateDiaryList) < numExistingDiaries;
                    if (numExistingDiaries > 0 && !existsUnloadedDiaries) {
                        DiaryYearMonthListItem noDiaryMessage =
                                new DiaryYearMonthListItem(
                                        DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE
                                );
                        updateDiaryList.add(noDiaryMessage);
                    }

                    // 日記リスト読込完了処理
                    if (updateDiaryList.isEmpty()) {
                        isVisibleDiaryList.postValue(false);
                    }
                    diaryList.postValue(updateDiaryList);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (previousDiaryList.isEmpty()) {
                        isVisibleDiaryList.postValue(false);
                    }
                    diaryList.postValue(previousDiaryList);
                    isDiaryListLoadingError.postValue(true);
                } finally {
                    isVisibleUpdateProgressBar.postValue(false);
                    isLoading = false;
                }
            }
        });
    }

    private int countDiaryListDayItem(List<DiaryYearMonthListItem> diaryList) {
        int count = 0;
        for (DiaryYearMonthListItem item: diaryList) {
            if (item.getViewType() == DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
                count += item.getDiaryDayListItemList().size();
            }
        }
        return count;
    }

    private List<DiaryYearMonthListItem> toDiaryYearMonthListFormat(List<DiaryListItem> beforeList) {
        List<DiaryDayListItem> diaryDayList = toDiaryDayList(beforeList);
        return toDiaryYearMonthList(diaryDayList);
    }

    private List<DiaryDayListItem> toDiaryDayList(List<DiaryListItem> beforeList) {
        List<DiaryDayListItem> diaryDayList = new ArrayList<>();
        for (DiaryListItem diaryListItem : beforeList) {
            String strDate = diaryListItem.getDate();
            LocalDate date = LocalDate.parse(strDate);
            String title = diaryListItem.getTitle();
            String picturePath = diaryListItem.getPicturePath();
            DiaryDayListItem diaryDayListItem = new DiaryDayListItem(date, title, picturePath);
            diaryDayList.add(diaryDayListItem);
        }
        return diaryDayList;
    }

    private List<DiaryYearMonthListItem> toDiaryYearMonthList(List<DiaryDayListItem> beforeList) {
        final int VIEW_TYPE_DIARY = DiaryYearMonthListAdapter.VIEW_TYPE_DIARY;
        List<DiaryDayListItem> sortingList= new ArrayList<>();
        List<DiaryYearMonthListItem> diaryYearMonthList = new ArrayList<>();
        DiaryYearMonthListItem diaryYearMonthListItem;
        YearMonth sortingYearMonth = null;

        for (DiaryDayListItem day: beforeList) {
            LocalDate date = day.getDate();
            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonth());

            if (sortingYearMonth != null && !yearMonth.equals(sortingYearMonth)) {
                diaryYearMonthListItem =
                        new DiaryYearMonthListItem(sortingYearMonth, sortingList, VIEW_TYPE_DIARY);
                diaryYearMonthList.add(diaryYearMonthListItem);
                sortingList= new ArrayList<>();
            }
            sortingList.add(day);
            sortingYearMonth = yearMonth;
        }
        if (sortingYearMonth != null) {
            diaryYearMonthListItem =
                    new DiaryYearMonthListItem(sortingYearMonth, sortingList, VIEW_TYPE_DIARY);
            diaryYearMonthList.add(diaryYearMonthListItem);
        } else {
            // TODO:assert
        }

        return diaryYearMonthList;
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
                new DiaryYearMonthListItem(DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE);
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
            isDiaryInformationLoadingError.setValue(true);
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
