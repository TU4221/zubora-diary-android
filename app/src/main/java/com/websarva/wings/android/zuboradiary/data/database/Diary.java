package com.websarva.wings.android.zuboradiary.data.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diaries")
public class Diary {
    @PrimaryKey
    @NonNull
    private String date; // TODO:曜日を含めないようにする。
    @NonNull
    private String log; // TODO:曜日を含めないようにする。
    @ColumnInfo(name = "weather_1")
    private Integer weather1;
    @ColumnInfo(name = "weather_2")
    private Integer weather2;
    private Integer condition;
    private String title;
    @ColumnInfo(name = "item_1_title")
    private String item1Title;
    @ColumnInfo(name = "item_1_comment")
    private String item1Comment;
    @ColumnInfo(name = "item_2_title")
    private String item2Title;
    @ColumnInfo(name = "item_2_comment")
    private String item2Comment;
    @ColumnInfo(name = "item_3_title")
    private String item3Title;
    @ColumnInfo(name = "item_3_comment")
    private String item3Comment;
    @ColumnInfo(name = "item_4_title")
    private String item4Title;
    @ColumnInfo(name = "item_4_comment")
    private String item4Comment;
    @ColumnInfo(name = "item_5_title")
    private String item5Title;
    @ColumnInfo(name = "item_5_comment")
    private String item5Comment;
    private String picturePath;

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Integer getWeather1() {
        return this.weather1;
    }

    public void setWeather1(Integer weather1) {
        this.weather1 = weather1;
    }

    public Integer getWeather2() {
        return this.weather2;
    }

    public void setWeather2(Integer weather2) {
        this.weather2 = weather2;
    }

    public Integer getCondition() {
        return condition;
    }

    public void setCondition(Integer condition) {
        this.condition = condition;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItem1Title() {
        return item1Title;
    }

    public void setItem1Title(String item1Title) {
        this.item1Title = item1Title;
    }

    public String getItem1Comment() {
        return item1Comment;
    }

    public void setItem1Comment(String item1Comment) {
        this.item1Comment = item1Comment;
    }

    public String getItem2Title() {
        return item2Title;
    }

    public void setItem2Title(String item2Title) {
        this.item2Title = item2Title;
    }

    public String getItem2Comment() {
        return item2Comment;
    }

    public void setItem2Comment(String item2Comment) {
        this.item2Comment = item2Comment;
    }

    public String getItem3Title() {
        return item3Title;
    }

    public void setItem3Title(String item3Title) {
        this.item3Title = item3Title;
    }

    public String getItem3Comment() {
        return item3Comment;
    }

    public void setItem3Comment(String item3Comment) {
        this.item3Comment = item3Comment;
    }

    public String getItem4Title() {
        return item4Title;
    }

    public void setItem4Title(String item4Title) {
        this.item4Title = item4Title;
    }

    public String getItem4Comment() {
        return item4Comment;
    }

    public void setItem4Comment(String item4Comment) {
        this.item4Comment = item4Comment;
    }

    public String getItem5Title() {
        return item5Title;
    }

    public void setItem5Title(String item5Title) {
        this.item5Title = item5Title;
    }

    public String getItem5Comment() {
        return item5Comment;
    }

    public void setItem5Comment(String item5Comment) {
        this.item5Comment = item5Comment;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }
}
