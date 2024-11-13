package com.websarva.wings.android.zuboradiary.data.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// MEMO:@NonNullに関する警告はROOMの機能で解消されるため無視すること。
@Entity(tableName = "diaries")
public class DiaryEntity {
    @PrimaryKey
    @NonNull
    private String date;
    @NonNull
    private String log;
    @ColumnInfo(name = "weather_1")
    //@NonNull
    private Integer weather1;
    @ColumnInfo(name = "weather_2")
    //@NonNull
    private Integer weather2;
    //@NonNull
    private Integer condition;
    //@NonNull
    private String title;
    @ColumnInfo(name = "item_1_title")
    //@NonNull
    private String item1Title;
    @ColumnInfo(name = "item_1_comment")
    //@NonNull
    private String item1Comment;
    @ColumnInfo(name = "item_2_title")
    //@NonNull
    private String item2Title;
    @ColumnInfo(name = "item_2_comment")
    //@NonNull
    private String item2Comment;
    @ColumnInfo(name = "item_3_title")
    //@NonNull
    private String item3Title;
    @ColumnInfo(name = "item_3_comment")
    //@NonNull
    private String item3Comment;
    @ColumnInfo(name = "item_4_title")
    //@NonNull
    private String item4Title;
    @ColumnInfo(name = "item_4_comment")
    //@NonNull
    private String item4Comment;
    @ColumnInfo(name = "item_5_title")
    //@NonNull
    private String item5Title;
    @ColumnInfo(name = "item_5_comment")
    //@NonNull
    private String item5Comment;
    //@NonNull
    private String picturePath;

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    @NonNull
    public String getLog() {
        return log;
    }

    public void setLog(@NonNull String log) {
        this.log = log;
    }

    @NonNull
    public Integer getWeather1() {
        return weather1;
    }

    public void setWeather1(@NonNull Integer weather1) {
        this.weather1 = weather1;
    }

    @NonNull
    public Integer getWeather2() {
        return this.weather2;
    }

    public void setWeather2(@NonNull Integer weather2) {
        this.weather2 = weather2;
    }

    @NonNull
    public Integer getCondition() {
        return condition;
    }

    public void setCondition(@NonNull Integer condition) {
        this.condition = condition;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getItem1Title() {
        return item1Title;
    }

    public void setItem1Title(@NonNull String item1Title) {
        this.item1Title = item1Title;
    }

    @NonNull
    public String getItem1Comment() {
        return item1Comment;
    }

    public void setItem1Comment(@NonNull String item1Comment) {
        this.item1Comment = item1Comment;
    }

    @NonNull
    public String getItem2Title() {
        return item2Title;
    }

    public void setItem2Title(@NonNull String item2Title) {
        this.item2Title = item2Title;
    }

    @NonNull
    public String getItem2Comment() {
        return item2Comment;
    }

    public void setItem2Comment(@NonNull String item2Comment) {
        this.item2Comment = item2Comment;
    }

    @NonNull
    public String getItem3Title() {
        return item3Title;
    }

    public void setItem3Title(@NonNull String item3Title) {
        this.item3Title = item3Title;
    }

    @NonNull
    public String getItem3Comment() {
        return item3Comment;
    }

    public void setItem3Comment(@NonNull String item3Comment) {
        this.item3Comment = item3Comment;
    }

    @NonNull
    public String getItem4Title() {
        return item4Title;
    }

    public void setItem4Title(@NonNull String item4Title) {
        this.item4Title = item4Title;
    }

    @NonNull
    public String getItem4Comment() {
        return item4Comment;
    }

    public void setItem4Comment(@NonNull String item4Comment) {
        this.item4Comment = item4Comment;
    }

    @NonNull
    public String getItem5Title() {
        return item5Title;
    }

    public void setItem5Title(@NonNull String item5Title) {
        this.item5Title = item5Title;
    }

    @NonNull
    public String getItem5Comment() {
        return item5Comment;
    }

    public void setItem5Comment(@NonNull String item5Comment) {
        this.item5Comment = item5Comment;
    }

    @NonNull
    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(@NonNull String picturePath) {
        this.picturePath = picturePath;
    }
}
