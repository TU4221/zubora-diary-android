package com.websarva.wings.android.zuboradiary.ui.list;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayListCreator {
    public DayListCreator() {
    }

    //テスト用サンプル日記リスト作成メソッド
    //(最終的にデータベースから50件分の日記を抽出して戻す)
    public List<Map<String, Object>> createDiaryList(int year, int month) {
        List<Map<String, Object>> sampleDiaryListYearMonth = new ArrayList<>();
        Map<String, Object> sampleDiaryYearMonth = new HashMap<>();
        List<Map<String, Object>> sampleDiaryListDay = new ArrayList<>();
        Map<String, Object> sampleDiaryDay = new HashMap<>();
        DiaryListDayAdapter sampleDiaryListDayAdapter;
        RecyclerView rvSampleDiaryListDay = null;
        int _year = year;
        int __year;
        int _month = month;
        int __month;
        int _day = 20;
        int __day;
        for (int i = 0; i < 3; i++) {
            __year = _year;
            for (int j = 0; j < 6; j++) {
                __month = _month - j;
                if (__month < 1) {
                    __month += 12;
                    if (__month == 12) {
                        _year -= 1;
                        __year = _year;
                    }
                }
                for (int k = 0; k < 7; k++) {
                    __day = _day - k;
                    sampleDiaryDay.put("年", __year);
                    sampleDiaryDay.put("月", __month);
                    sampleDiaryDay.put("日", __day);
                    switch (k + 1) {
                        case 1:
                            sampleDiaryDay.put("曜日", "日");
                            break;
                        case 2:
                            sampleDiaryDay.put("曜日", "土");
                            break;
                        case 3:
                            sampleDiaryDay.put("曜日", "金");
                            break;
                        case 4:
                            sampleDiaryDay.put("曜日", "木");
                            break;
                        case 5:
                            sampleDiaryDay.put("曜日", "水");
                            break;
                        case 6:
                            sampleDiaryDay.put("曜日", "火");
                            break;
                        case 7:
                            sampleDiaryDay.put("曜日", "月");
                            break;
                        default:
                            sampleDiaryDay.put("曜日", "-");
                            break;
                    }
                    sampleDiaryDay.put("タイトル", "無題");
                    Log.d("リスト表示確認",(__month) + "月" + (__day) + "日");
                    sampleDiaryListDay.add(sampleDiaryDay);
                    sampleDiaryDay = new HashMap<>();
                }
                sampleDiaryYearMonth.put("年", __year);
                sampleDiaryYearMonth.put("月", __month);
                sampleDiaryListDayAdapter = new DiaryListDayAdapter(sampleDiaryListDay);
                sampleDiaryYearMonth.put("DiaryListDayAdapter", sampleDiaryListDayAdapter);

                sampleDiaryListYearMonth.add(sampleDiaryYearMonth);
                sampleDiaryListDay =   new ArrayList<>();
                sampleDiaryYearMonth = new HashMap<>();
            }
        }

        return sampleDiaryListYearMonth;
    }

    //日記リスト(日)リサイクルビューホルダークラス
    public class DiaryListDayViewHolder extends RecyclerView.ViewHolder implements Serializable {
        public TextView _tvRowDiaryListDay_DayOfWeek;
        public TextView _tvRowDiaryListDay_Day;
        public TextView _tvRowDiaryListDay_Title;
        public int diaryInfo_Year;
        public int diaryInfo_Month;
        public int diaryInfo_Day;
        public String diaryInfo_DayOfWeek;
        public String diaryInfo_Title;
        public CustomSimpleCallback diaryInfo_SimpleCallback;
        public DiaryListDayViewHolder(View itemView) {
            super(itemView);
            _tvRowDiaryListDay_DayOfWeek = itemView.findViewById(R.id.text_day_of_week);
            _tvRowDiaryListDay_Day = itemView.findViewById(R.id.text_day_of_month);
            _tvRowDiaryListDay_Title = itemView.findViewById(R.id.text_row_diary_list_day_title);
        }
    }

    //日記リスト(日)リサイクルビューアダプタクラス
    public class DiaryListDayAdapter extends RecyclerView.Adapter<DiaryListDayViewHolder> implements Serializable {
        private List<Map<String, Object>> _DiaryListDay;

        public DiaryListDayAdapter(List<Map<String, Object>> DiaryListDay){
            _DiaryListDay = DiaryListDay;
        }

        //日記リスト(日)のホルダーと日記リスト(日)のアイテムレイアウトを紐づける。
        @Override
        public DiaryListDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_diary_day_list, parent, false);
            DiaryListDayViewHolder holder = new DiaryListDayViewHolder(view);
            return holder;
        }

        //日記リスト(日)の各行アイテム(ホルダー)情報を設定。
        @Override
        public void onBindViewHolder(DiaryListDayViewHolder holder, int position) {
            Map<String, Object> item = _DiaryListDay.get(position);
            String tvRowListDay_DayOfWeek = (String) item.get("曜日");
            String tvRowListDay_Day = String.valueOf((Integer) item.get("日"));
            String tvRowListDay_Title = (String) item.get("タイトル");
            holder._tvRowDiaryListDay_DayOfWeek.setText(tvRowListDay_DayOfWeek);
            holder._tvRowDiaryListDay_Day.setText(tvRowListDay_Day);
            holder._tvRowDiaryListDay_Title.setText(tvRowListDay_Title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //**********注意**********
                    //ここに日記表示フラグメント起動プログラム作成(後回し)
                    //下記プログラムはテスト
                    String testYear = String.valueOf((Integer) item.get("年"));
                    String testMonth = String.valueOf((Integer) item.get("月"));
                    String testDay = String.valueOf((Integer) item.get("日"));
                    String testDayOfWeek = (String) item.get("曜日");
                    String testTitle = (String) item.get("タイトル");
                    String testToast = testYear + "年" + testMonth + "月" + testDay + "日" + "(" + testDayOfWeek + ")" + "タイトル：" + testTitle;
                    Toast.makeText(v.getContext(), testToast, Toast.LENGTH_SHORT).show();
                }
            });

            //ホルダー毎に日記情報を持たせる。
            holder.diaryInfo_Year = (Integer) item.get("年");
            holder.diaryInfo_Month = (Integer) item.get("月");
            holder.diaryInfo_Day = (Integer) item.get("日");
            holder.diaryInfo_DayOfWeek = (String) item.get("曜日");
            holder.diaryInfo_Title = (String) item.get("タイトル");
        }

        //日記リスト(日)のアイテム数を戻すメソッド
        @Override
        public int getItemCount() {
            return _DiaryListDay.size();
        }

        //日記リスト(日)の選択アイテム削除メソッド
        public void deleteItem(int position) {
            //DiaryListFragment.DiaryListDayItemDecorationクラスは、最後尾のアイテムのデコレーションをが他のデコレーションと異なる為、
            //最後尾のアイテムを削除した時は表示に関して違和感が生じる。
            //その為、最後尾のアイテムを削除する時は notifyItemChanged メソッドを使用して違和感をなくす。
            if (position == (getItemCount() - 1)) {
                _DiaryListDay.remove(position);
                notifyItemRemoved(position);
                notifyItemChanged(getItemCount() - 1);
            } else {
                _DiaryListDay.remove(position);
                notifyItemRemoved(position);
            }

            //**********注意**********
            //ここにデータベースの日記情報を削除するプログラム作成(後回し)
        }
    }
}
