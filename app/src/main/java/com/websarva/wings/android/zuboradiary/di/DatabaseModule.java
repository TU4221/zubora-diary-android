package com.websarva.wings.android.zuboradiary.di;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO;
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryDAO;
import com.websarva.wings.android.zuboradiary.data.diary.Condition;
import com.websarva.wings.android.zuboradiary.data.diary.Weather;

import java.util.Objects;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    @Singleton
    @Provides
    @NonNull
    public static DiaryDatabase provideDiaryDatabase(@ApplicationContext Context context) {
        Objects.requireNonNull(context);
        // TODO:下記上手くいかなかったので余裕があれば調べる
        Migration migration_2_3 = new Migration(2, 3) {

            @Override
            public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("ALTER TABLE diaries ADD COLUMN weather_01 INTEGER");
                supportSQLiteDatabase.execSQL("ALTER TABLE diaries ADD COLUMN weather_02 INTEGER");
                supportSQLiteDatabase.execSQL("ALTER TABLE diaries ADD COLUMN condition_01 INTEGER");

                // カーソルでデータを取得
                Cursor cursor = supportSQLiteDatabase.query("SELECT date, weather_1, weather_2, condition, FROM diaries");

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex("date") - 1);
                    String oldStringWeather1 = cursor.getString(cursor.getColumnIndex("weather_1") - 1);
                    String oldStringWeather2 = cursor.getString(cursor.getColumnIndex("weather_2") - 1);
                    String oldStringCondition = cursor.getString(cursor.getColumnIndex("condition") - 1);

                    // 変換
                    Weather weather1 = Weather.of(context, oldStringWeather1);
                    Integer intWeather1 = weather1.toNumber();
                    Weather weather2 = Weather.of(context, oldStringWeather2);
                    Integer intWeather2 = weather2.toNumber();
                    Condition condition = Condition.of(context, oldStringCondition);
                    Integer intCondition = condition.toNumber();

                    // 新しいカラムにデータを挿入
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("weather_01", intWeather1);
                    contentValues.put("weather_02", intWeather2);
                    contentValues.put("condition_01", intCondition);

                    supportSQLiteDatabase.update("diaries", SQLiteDatabase.CONFLICT_REPLACE, contentValues, "date=?", new String[]{String.valueOf(id)});
                }
                cursor.close();

            }
        };
        DiaryDatabase diaryDatabase =
                Room.databaseBuilder(context, DiaryDatabase.class, "diary_db")
                // MEMO:データベース初期化
                //      https://www.bedroomcomputing.com/2020/06/2020-0627-db-prepopulate/
                //.createFromAsset("database/diary_db.db")
                //.addMigrations(migration_2_3)
                //.fallbackToDestructiveMigration()
                .build();

        return Objects.requireNonNull(diaryDatabase);
    }

    @Singleton
    @Provides
    @NonNull
    public static DiaryDAO provideDiaryDAO(DiaryDatabase diaryDatabase) {
        Objects.requireNonNull(diaryDatabase);

        DiaryDAO dao = diaryDatabase.createDiaryDAO();
        return Objects.requireNonNull(dao);
    }

    @Singleton
    @Provides
    @NonNull
    public static DiaryItemTitleSelectionHistoryDAO provideSelectedItemTitlesHistoryDAO(DiaryDatabase diaryDatabase) {
        Objects.requireNonNull(diaryDatabase);

        DiaryItemTitleSelectionHistoryDAO dao = diaryDatabase.createSelectedItemTitlesHistoryDAO();
        return Objects.requireNonNull(dao);
    }
}
