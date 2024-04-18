package com.websarva.wings.android.zuboradiary.ui.list;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListViewModel extends AndroidViewModel {

    private ListRepository listRepository;
    private MutableLiveData<List<ListItemDiary>> loadedListItemDiaries
            = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Boolean> isVisibleHeaderSectionBar = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final int LOAD_ITEM_NUM = 15; // TODO:仮数値の為、最後に設定
    private int loadItemOffset = 0;
    private String sortConditionDate = "";


    public ListViewModel(@NonNull Application application) {
        super(application);
        listRepository = new ListRepository(getApplication());
    }

    public enum LoadType {
        NEW, UPDATE, ADD
    }
    public void loadList(LoadType loadType) {
        Log.d("リスト読込確認", "起動");
        List<ListItemDiary> loadedData = new ArrayList<>();
        int loadItemNum;
        if (loadType == LoadType.NEW) {
            loadItemNum = this.LOAD_ITEM_NUM;
            this.loadItemOffset = 0;
        } else if(loadType == LoadType.UPDATE) {
            loadItemNum = this.loadItemOffset;
            this.loadItemOffset = 0;
        } else {
            loadItemNum = this.LOAD_ITEM_NUM;
            loadedData = this.loadedListItemDiaries.getValue();
        }

        if (this.sortConditionDate.equals("")) {
            loadedData.addAll(
                    this.listRepository.getListItemDiaries(
                            loadItemNum,
                            this.loadItemOffset,
                            null
                    )
            );
        } else {
            loadedData.addAll(
                    this.listRepository.getListItemDiaries(
                            loadItemNum,
                            this.loadItemOffset,
                            this.sortConditionDate
                    )
            );
        }

        this.loadItemOffset += loadItemNum;
        this.loadedListItemDiaries.setValue(loadedData);
    }

    public void updateSortConditionDate(int year, int month ,int dayOfMonth) {
        // 日付データ作成。
        // https://qiita.com/hanaaaa/items/8555aaabc6b949ec507d
        // https://nainaistar.hatenablog.com/entry/2021/05/13/120000
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate lastDayOfMonthDate = LocalDate
                    .of(year, month, dayOfMonth)
                    .with(TemporalAdjusters.lastDayOfMonth());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            this.sortConditionDate= lastDayOfMonthDate.format(formatter);
        }
    }

    public void deleteDiary(String date) {
        // TODO:テスト中の為コメントアウト。終了次第戻す。
        //listRepository.deleteDiary(date);

        List<ListItemDiary> loadedData = this.loadedListItemDiaries.getValue();
        Iterator<ListItemDiary> iterator = loadedData.iterator();
        while (iterator.hasNext()) {
            ListItemDiary item = iterator.next();
            if (item.getDate().equals(date)) {
                iterator.remove();
                // break;
            }
        }
        loadedListItemDiaries.setValue(loadedData);
    }

    public int countDiaries() {
        return this.listRepository.countDiaries();
    }


    public LiveData<List<ListItemDiary>> getLoadedListItemDiaries() {
        return this.loadedListItemDiaries;
    }

    public void setLiveIsLoading(boolean bool) {
        this.isLoading.setValue(bool);
        Log.d("progressbar確認", String.valueOf(this.isLoading.getValue()));
    }

    public void setLiveIsVisibleHeaderSectionBar(boolean bool) {
        this.isVisibleHeaderSectionBar.setValue(bool);
    }

    // 単一・双方向データバインディング用メソッド
    // MEMO:単一の場合、ゲッターの戻り値はLiveData<>にすること。
    // TODO:双方向は未確認
    public LiveData<Boolean> getIsLoading() {
        return this.isLoading;
    }

    public LiveData<Boolean> getIsVisibleHeaderSectionBar() {
        return this.isVisibleHeaderSectionBar;
    }
}
