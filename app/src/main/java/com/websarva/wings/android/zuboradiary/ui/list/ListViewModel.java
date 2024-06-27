package com.websarva.wings.android.zuboradiary.ui.list;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.DateConverter;
import com.websarva.wings.android.zuboradiary.ui.diary.Diary;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListViewModel extends AndroidViewModel {

    private DiaryRepository diaryRepository;
    private MutableLiveData<List<DiaryYearMonthListItem>> diaryList = new MutableLiveData<>();
    private boolean isLoading;
    private  MutableLiveData<Boolean> isVisibleDiaryList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    private final int LOAD_ITEM_NUM = 10; // TODO:仮数値の為、最後に設定
    private String sortConditionDate;
    private ExecutorService executorService;


    public ListViewModel(@NonNull Application application) {
        super(application);
        this.diaryRepository = new DiaryRepository(getApplication());
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

    public void loadList(LoadType loadType, Runnable runnable) {
        Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                ListViewModel.this.isLoading = true;
                ListViewModel.this.isVisibleDiaryList.postValue(true);

                // 日記リスト読込準備
                int numLoadingItems;
                int loadingOffset;
                if (loadType == LoadType.UPDATE) {
                    numLoadingItems = countDiaryListDayItem(ListViewModel.this.diaryList.getValue());
                    loadingOffset = 0;
                } else if (loadType == LoadType.ADD) {
                    numLoadingItems = ListViewModel.this.LOAD_ITEM_NUM;
                    loadingOffset = countDiaryListDayItem(ListViewModel.this.diaryList.getValue());
                } else {
                    // LoadType.NEW
                    numLoadingItems = ListViewModel.this.LOAD_ITEM_NUM;
                    loadingOffset = 0;
                }

                // 現時点のDiaryListをCloneで生成
                List<DiaryYearMonthListItem> currentDiaryList =
                        ListViewModel.this.diaryList.getValue();
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
                if (loadType == LoadType.UPDATE) {
                    ListViewModel.this.isVisibleUpdateProgressBar.postValue(true);
                } else {
                    DiaryYearMonthListItem progressBar = new DiaryYearMonthListItem();
                    progressBar.setViewType(
                            DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR);
                    diaryListContainingProgressBar.add(progressBar);
                }
                ListViewModel.this.diaryList.postValue(diaryListContainingProgressBar);

                // TODO:ProgressBarを表示させる為に仮で記述
                try {
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // 日記リスト読込
                List<DiaryYearMonthListItem> loadedData = new ArrayList<>();
                try {
                    if (ListViewModel.this.sortConditionDate.equals("")) {
                        loadedData =
                                ListViewModel.this.diaryRepository.loadDiaryList(
                                        numLoadingItems,
                                        loadingOffset,
                                        null
                                );
                    } else {
                        loadedData =
                                ListViewModel.this.diaryRepository.loadDiaryList(
                                        numLoadingItems,
                                        loadingOffset,
                                        ListViewModel.this.sortConditionDate
                                );
                    }
                } catch (Exception e) {
                    ListViewModel.this.diaryList.postValue(previousDiaryList);
                    ListViewModel.this.isVisibleUpdateProgressBar.postValue(false);
                    ListViewModel.this.isLoading = false;
                    handler.post(runnable);
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
                int numExistingDiaries = 0;
                try {
                    numExistingDiaries =
                            ListViewModel.this.diaryRepository
                                    .countDiaries(ListViewModel.this.sortConditionDate);
                } catch (Exception e) {
                    ListViewModel.this.diaryList.postValue(previousDiaryList);
                    ListViewModel.this.isVisibleUpdateProgressBar.postValue(false);
                    ListViewModel.this.isLoading = false;
                    handler.post(runnable);
                    return;
                }
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
                    ListViewModel.this.isVisibleDiaryList.postValue(false);
                }
                ListViewModel.this.diaryList.postValue(updateDiaryList);
                ListViewModel.this.isVisibleUpdateProgressBar.postValue(false);
                ListViewModel.this.isLoading = false;
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

    public void deleteDiary(String date) throws Exception {
        diaryRepository.deleteDiary(date);

        LocalDate deleteDiaryDate = DateConverter.toLocalDate(date);

        List<DiaryYearMonthListItem> updateDiaryList = new ArrayList<>();
        List<DiaryYearMonthListItem> currentDiaryList = ListViewModel.this.diaryList.getValue();

        DiaryYearMonthListItem targetYearMonthListItem = new DiaryYearMonthListItem();
        List<DiaryDayListItem> targetDayList = new ArrayList<>();
        for (DiaryYearMonthListItem item: currentDiaryList) {
            if (item.getYear() == deleteDiaryDate.getYear()
                    && item.getMonth() == deleteDiaryDate.getMonthValue()) {
                targetYearMonthListItem = item;
                targetDayList = item.getDiaryDayListItemList();
                break;
            }
        }

        for (DiaryDayListItem item: targetDayList) {
            if (item.getDayOfMonth() == deleteDiaryDate.getDayOfMonth()) {
                targetDayList.remove(item);
                if (targetDayList.isEmpty()) {
                    currentDiaryList.remove(targetYearMonthListItem);
                }
                break;
            }
        }

        updateDiaryList.addAll(currentDiaryList);
        ListViewModel.this.diaryList.postValue(updateDiaryList);

    }

    public int countDiaries() throws Exception {
        return this.diaryRepository.countDiaries(null);
    }


    public Diary loadNewestDiary() throws Exception {
        return this.diaryRepository.selectNewestDiary();
    }

    public Diary loadOldestDiary() throws Exception {
        return this.diaryRepository.selectOldestDiary();
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
}
