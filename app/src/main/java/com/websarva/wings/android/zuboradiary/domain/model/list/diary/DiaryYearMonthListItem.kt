package com.websarva.wings.android.zuboradiary.domain.model.list.diary

import java.time.YearMonth

/**
 * 年月ごとの日記リストの各アイテムを表す基底クラス。
 *
 * このクラスは、年月単位でグループ化された日記リストの要素の共通の型を提供する。
 * 具体的なアイテムの種類（日記の年月ごとの集まり、日記なしメッセージ、プログレスインジケータ）に応じて、
 * このクラスを継承したサブクラスが定義される。
 *
 * @param T [DiaryDayListItem] を実装するアイテムの型。これは [Diary] サブクラス内の日記リストの型として使用される。
 */
internal sealed class DiaryYearMonthListItem<T : DiaryDayListItem> {

    /**
     * 特定の年月に属する日記のリストを表すアイテム。
     *
     * @param T [DiaryDayListItem] を実装するアイテムの型。
     * @property yearMonth この日記リストが対象とする年月。
     * @property diaryDayList その年月の対象となる日単位の日記リスト。
     */
    data class Diary<T : DiaryDayListItem>(
        val yearMonth: YearMonth,
        val diaryDayList: DiaryDayList<T>
    ) : DiaryYearMonthListItem<T>()

    /**
     * 日記が存在しないことを示すメッセージアイテム。
     *
     * @param T [DiaryDayListItem] を実装するアイテムの型。
     */
    class NoDiaryMessage<T : DiaryDayListItem> : DiaryYearMonthListItem<T>()

    /**
     * 続きの日記リスト読み込み中を示すプログレスインジケータアイテム。
     *
     * @param T [DiaryDayListItem] を実装するアイテムの型。
     */
    class ProgressIndicator<T : DiaryDayListItem> : DiaryYearMonthListItem<T>()
}
