package com.websarva.wings.android.zuboradiary.di

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room.databaseBuilder
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryDAO
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Objects
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideDiaryDatabase(@ApplicationContext context: Context): DiaryDatabase {
        // TODO:下記上手くいかなかったので余裕があれば調べる
        val migration2to3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diaries ADD COLUMN weather_01 INTEGER")
                db.execSQL("ALTER TABLE diaries ADD COLUMN weather_02 INTEGER")
                db.execSQL("ALTER TABLE diaries ADD COLUMN condition_01 INTEGER")

                // カーソルでデータを取得
                val cursor =
                    db.query("SELECT date, weather_1, weather_2, condition, FROM diaries")

                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndex("date") - 1)
                    val oldStringWeather1 = cursor.getString(cursor.getColumnIndex("weather_1") - 1)
                    val oldStringWeather2 = cursor.getString(cursor.getColumnIndex("weather_2") - 1)
                    val oldStringCondition =
                        cursor.getString(cursor.getColumnIndex("condition") - 1)

                    // 変換
                    val weather1 = Weather.of(context, oldStringWeather1)
                    val intWeather1 = weather1.toNumber()
                    val weather2 = Weather.of(context, oldStringWeather2)
                    val intWeather2 = weather2.toNumber()
                    val condition = Condition.of(context, oldStringCondition)
                    val intCondition = condition.toNumber()

                    // 新しいカラムにデータを挿入
                    val contentValues = ContentValues()
                    contentValues.put("weather_01", intWeather1)
                    contentValues.put("weather_02", intWeather2)
                    contentValues.put("condition_01", intCondition)

                    db.update(
                        "diaries",
                        SQLiteDatabase.CONFLICT_REPLACE,
                        contentValues,
                        "date=?",
                        arrayOf<String?>(id.toString())
                    )
                }
                cursor.close()
            }
        }

        return databaseBuilder(context, DiaryDatabase::class.java, "diary_db") // MEMO:データベース初期化
            //      https://www.bedroomcomputing.com/2020/06/2020-0627-db-prepopulate/
            //.createFromAsset("database/diary_db.db")
            //.addMigrations(migration_2_3)
            //.fallbackToDestructiveMigration()
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDiaryDAO(diaryDatabase: DiaryDatabase): DiaryDAO {
        return diaryDatabase.createDiaryDAO()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSelectedItemTitlesHistoryDAO(diaryDatabase: DiaryDatabase): DiaryItemTitleSelectionHistoryDAO {
        return diaryDatabase.createDiaryItemTitleSelectionHistoryDAO()
    }
}
