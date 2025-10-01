package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Roomデータベースで使用する型コンバーターをまとめたオブジェクト。
 */
internal object DiaryDatabaseConverter {

    // --- LocalDateTime <-> String (YYYY-MM-DD) ---
    private val LOCAL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * [LocalDate] 型からデータベース格納用の [String] 型（YYYY-MM-DD形式）に変換。
     *
     * @param date 変換元の [LocalDate] オブジェクト。
     * @return 変換後の日付文字列。
     */
    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(LOCAL_DATE_FORMATTER)
    }

    /**
     * データベース格納用の [String] 型（YYYY-MM-DD形式）から [LocalDate] 型に変換。
     *
     * @param dateString 変換元の YYYY-MM-DD形式の日付文字列。null許容。
     * @return 変換後の [LocalDate] オブジェクト。入力がnullまたは空文字列の場合はnullを返す。
     * @throws DateTimeParseException 入力が有効な形式でない場合。
     */
    @TypeConverter
    @JvmStatic
    fun toLocalDate(dateString: String?): LocalDate? {
        return if (dateString.isNullOrBlank()) {
            null
        } else {
            LocalDate.parse(dateString, LOCAL_DATE_FORMATTER)
        }
    }

    // --- LocalDateTime <-> String (YYYY-MM-DDTHH:MM:SS) ---
    private val LOCAL_DATETIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /**
     * [LocalDateTime] 型からデータベース格納用の [String] 型（YYYY-MM-DDTHH:MM:SS形式）に変換。
     *
     * @param dateTime 変換元の [LocalDateTime] オブジェクト。null許容。
     * @return 変換後の日時文字列。入力がnullの場合はnullを返す。
     */
    @TypeConverter
    @JvmStatic
    fun fromLocalDateTimeToString(dateTime: LocalDateTime?): String? {
        return dateTime?.format(LOCAL_DATETIME_FORMATTER)
    }

    /**
     * データベース格納用の [String] 型（YYYY-MM-DDTHH:MM:SS形式）から [LocalDateTime] 型に変換。
     *
     * @param dateTimeString 変換元の YYYY-MM-DDTHH:MM:SS形式の日時文字列。null許容。
     * @return 変換後の [LocalDateTime] オブジェクト。入力がnullまたは空文字列の場合はnullを返す。
     * @throws DateTimeParseException 入力が有効な形式でない場合。
     */
    @TypeConverter
    @JvmStatic
    fun toLocalDateTimeFromString(dateTimeString: String?): LocalDateTime? {
        return if (dateTimeString.isNullOrBlank()) {
            null
        } else {
            LocalDateTime.parse(dateTimeString, LOCAL_DATETIME_FORMATTER)
        }
    }
}
