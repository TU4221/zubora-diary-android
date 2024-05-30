package com.websarva.wings.android.zuboradiary.ui.list;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// DiaryListSettingクラスで使用するフィールドをここで追加する。
// 注意)layout(xml)のViewとの紐づけを忘れないこと!
public abstract class DiaryYearMonthListViewHolder extends RecyclerView.ViewHolder {
    public TextView textSectionBar;

    public DiaryYearMonthListViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}
