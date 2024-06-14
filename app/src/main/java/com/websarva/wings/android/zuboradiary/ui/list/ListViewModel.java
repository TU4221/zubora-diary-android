package com.websarva.wings.android.zuboradiary.ui.list;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.DateConverter;
import com.websarva.wings.android.zuboradiary.ui.diary.Diary;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListFragment.DiaryDayListAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListViewModel extends AndroidViewModel {

    private DiaryListRepository diaryListRepository;
    private MutableLiveData<List<DiaryYearMonthListItem>> diaryList =
            new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<DiaryListItem>> loadedListItemDiaries
            = new MutableLiveData<>(new ArrayList<>());
    private List<DiaryListItem> previousLoadedListItemDiaries = new ArrayList<>();
    private final int LOAD_ITEM_NUM = 50; // TODO:仮数値の為、最後に設定
    private int loadItemOffset = 0;
    private String sortConditionDate = "";
    private int scrollPointY = 0;


    public ListViewModel(@NonNull Application application) {
        super(application);
        diaryListRepository = new DiaryListRepository(getApplication());
    }

    public enum LoadType {
        NEW, UPDATE, ADD
    }

    public void loadList(LoadType loadType) {
        if (loadType == LoadType.UPDATE) {
        } else if (loadType == LoadType.ADD) {
        } else {
            ListViewModel.this.diaryList.setValue(new ArrayList<>());
        }


        Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.d("リスト読込確認", "起動");
                int loadItemNum;
                if (loadType == LoadType.UPDATE) {
                    loadItemNum = ListViewModel.this.loadItemOffset;
                    ListViewModel.this.loadItemOffset = 0;
                } else if (loadType == LoadType.ADD) {
                    loadItemNum = ListViewModel.this.LOAD_ITEM_NUM;
                } else {
                    // LoadType.NEW
                    loadItemNum = ListViewModel.this.LOAD_ITEM_NUM;
                    ListViewModel.this.loadItemOffset = 0;
                    ListViewModel.this.diaryList.postValue(new ArrayList<>());
                }

                // 現時点のDiaryListを保持
                List<DiaryYearMonthListItem> previousDiaryList =
                        ListViewModel.this.diaryList.getValue();

                // ProgressBar追加
                List<DiaryYearMonthListItem> diaryListContainingProgressBar = new ArrayList<>();
                DiaryYearMonthListItem progressBar = new DiaryYearMonthListItem();
                progressBar.setViewType(
                        DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR);
                diaryListContainingProgressBar.addAll(previousDiaryList);
                diaryListContainingProgressBar.add(progressBar);
                ListViewModel.this.diaryList.postValue(diaryListContainingProgressBar);

                // TODO:ProgressBarを表示させる為に仮で記述
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // データ読込
                List<DiaryListItem> loadedData = null;
                if (ListViewModel.this.sortConditionDate.equals("")) {
                    loadedData =
                            ListViewModel.this.diaryListRepository.getListItemDiaries(
                                    loadItemNum,
                                    ListViewModel.this.loadItemOffset,
                                    null
                            );
                } else {
                    loadedData =
                            ListViewModel.this.diaryListRepository.getListItemDiaries(
                                    loadItemNum,
                                    ListViewModel.this.loadItemOffset,
                                    ListViewModel.this.sortConditionDate
                            );
                }

                // 読込データ空確認
                if (loadedData.isEmpty()) {
                    ListViewModel.this.diaryList.postValue(previousDiaryList);
                    return;
                }

                // 読込データを日記リストへ追加
                ListViewModel.this.loadItemOffset += loadItemNum;
                List<DiaryYearMonthListItem> convertedList = toRecyclerViewFormat(loadedData);
                List<DiaryYearMonthListItem> newDiaryList = new ArrayList<>();
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
                            convertedList.get(0);
                    int additionalDiaryListFirstItemYear =
                            additionalDiaryListFirstItem.getYear();
                    int additionalDiaryListFirstItemMonth =
                            additionalDiaryListFirstItem.getMonth();

                    // 前回の読込リストに今回の読込リストの年月が含まれていたら,
                    // そこにDiaryDayListItemを足し込む
                    if (previousDiaryYearMonthListLastItemYear == additionalDiaryListFirstItemYear
                            && previousDiaryYearMonthListLastItemMonth == additionalDiaryListFirstItemMonth) {
                        List<DiaryDayListItem> previousDiaryDayListItemList =
                                previousDiaryYearMonthListLastItem.getDiaryDayListItemList();
                        List<DiaryDayListItem> additionalDiaryDayListItemList =
                                additionalDiaryListFirstItem.getDiaryDayListItemList();
                        previousDiaryDayListItemList.addAll(additionalDiaryDayListItemList);
                        convertedList.remove(0);
                    }

                    newDiaryList.addAll(previousDiaryList);
                    newDiaryList.addAll(convertedList);
                    ListViewModel.this.diaryList.postValue(newDiaryList);
                    Log.d("20240613", "Add");
                } else {
                    newDiaryList.addAll(convertedList);
                    ListViewModel.this.diaryList.postValue(convertedList);
                    Log.d("20240613", "New or Update");
                }
            }
        });

    }

    private List<DiaryYearMonthListItem> toRecyclerViewFormat(List<DiaryListItem> beforeList) {
        // 型変換:List<DiaryListItem> -> List<Map<String, Object>>
        List<DiaryDayListItem> diaryDayListItemList = new ArrayList<>();
        DiaryDayListItem diaryDayListItem;
        String date;
        String title;
        String picturePath;
        String year;
        String month;
        String dayOfMonth;
        String dayOfWeek;
        final int VIEW_TYPE_DIARY = DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_DIARY;
        int startIndex;
        int endIndex;
        for (DiaryListItem diaryListItem : beforeList) {
            date = diaryListItem.getDate();
            title = diaryListItem.getTitle();
            picturePath = diaryListItem.getPicturePath();
            startIndex = 0;
            endIndex = date.indexOf("年");
            year = date.substring(startIndex, endIndex);
            startIndex = endIndex + 1;
            endIndex = date.indexOf("月");
            month = date.substring(startIndex, endIndex);
            startIndex = endIndex + 1;
            endIndex = date.indexOf("日");
            dayOfMonth = date.substring(startIndex, endIndex);
            startIndex = date.indexOf("(") + 1;
            endIndex = date.indexOf(")");
            dayOfWeek = date.substring(startIndex, endIndex);
            diaryDayListItem = new DiaryDayListItem();
            diaryDayListItem.setYear(Integer.parseInt(year));
            diaryDayListItem.setMonth(Integer.parseInt(month));
            diaryDayListItem.setDayOfMonth(Integer.parseInt(dayOfMonth));
            diaryDayListItem.setDayOfWeek(dayOfWeek);
            diaryDayListItem.setTitle(title);
            diaryDayListItem.setPicturePath(picturePath);
            diaryDayListItemList.add(diaryDayListItem);
        }

        // 日記リストを月別に振り分ける
        List<DiaryDayListItem> sortingList= new ArrayList<>();
        List<DiaryYearMonthListItem> diaryYearMonthListItemList = new ArrayList<>();
        int sortingYear = 0;
        int sortingMonth = 0;

        for (DiaryDayListItem day: diaryDayListItemList) {
            int _year = day.getYear();
            int _Month = day.getMonth();

            if (sortingYear != 0 && sortingMonth != 0
                    && (_year != sortingYear || _Month != sortingMonth)) {
                addDiaryYearMonthListItem(
                        diaryYearMonthListItemList,
                        sortingYear, sortingMonth, sortingList, VIEW_TYPE_DIARY);
                sortingList= new ArrayList<>();
            }
            sortingList.add(day);
            sortingYear = _year;
            sortingMonth = _Month;
        }
        addDiaryYearMonthListItem(
                diaryYearMonthListItemList, sortingYear, sortingMonth, sortingList, VIEW_TYPE_DIARY);

        return diaryYearMonthListItemList;
    }

    private void addDiaryYearMonthListItem (List<DiaryYearMonthListItem> diaryYearMonthListItemList,
                                            int year, int Month, List<DiaryDayListItem> sortingList, int VIEW_TYPE_DIARY) {
        DiaryYearMonthListItem diaryYearMonthListItem = new DiaryYearMonthListItem();
        diaryYearMonthListItem.setYear(year);
        diaryYearMonthListItem.setMonth(Month);
        diaryYearMonthListItem.setDiaryDayListItemList(sortingList);
        diaryYearMonthListItem.setViewType(VIEW_TYPE_DIARY);
        diaryYearMonthListItemList.add(diaryYearMonthListItem);
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

    public void deleteDiary(String date) {
        diaryListRepository.deleteDiary(date);
        this.loadItemOffset--;

        LocalDate deleteDiaryDate = DateConverter.toLocalDate(date);

        List<DiaryYearMonthListItem> updateDiaryList = new ArrayList<>();
        List<DiaryYearMonthListItem> currentDiaryList = ListViewModel.this.diaryList.getValue();

        DiaryYearMonthListItem targetYearMonth = new DiaryYearMonthListItem();
        List<DiaryDayListItem> targetDayList = new ArrayList<>();
        for (DiaryYearMonthListItem item: currentDiaryList) {
            if (item.getYear() == deleteDiaryDate.getYear()
                    && item.getMonth() == deleteDiaryDate.getMonthValue()) {
                targetYearMonth = item;
                targetDayList = item.getDiaryDayListItemList();
                break;
            }
        }

        for (DiaryDayListItem item: targetDayList) {
            if (item.getDayOfMonth() == deleteDiaryDate.getDayOfMonth()) {
                targetDayList.remove(item);
                if (targetDayList.isEmpty()) {
                    currentDiaryList.remove(targetYearMonth);
                }
                break;
            }
        }

        updateDiaryList.addAll(currentDiaryList);
        ListViewModel.this.diaryList.postValue(updateDiaryList);

    }

    public int countDiaries() {
        return this.diaryListRepository.countDiaries();
    }






    // Getter/Setter
    public LiveData<List<DiaryYearMonthListItem>> getLiveDataDiaryList() {
        return this.diaryList;
    }
    public void setLiveDataDiaryList(List<DiaryYearMonthListItem> list) {
        this.diaryList.setValue(list);
    }

    public LiveData<List<DiaryListItem>> getLiveDataLoadedListItemDiaries() {
        return this.loadedListItemDiaries;
    }
    public void setLoadedListItemDiaries(List<DiaryListItem> list) {
        this.loadedListItemDiaries.setValue(list);
    }

    public List<DiaryListItem> getPreviousLoadedListItemDiaries() {
        return this.previousLoadedListItemDiaries;
    }
    public void setPreviousLoadedListItemDiaries(List<DiaryListItem> list) {
        this.previousLoadedListItemDiaries = list;
    }

    public int getScrollPointY() {
        return scrollPointY;
    }

    public void setScrollPointY(int scrollPointY) {
        this.scrollPointY = scrollPointY;
    }

    public Diary loadNewestDiary() {
        return this.diaryListRepository.selectNewestDiary();
    }

    public Diary loadOldestDiary() {
        return this.diaryListRepository.selectOldestDiary();
    }
}
