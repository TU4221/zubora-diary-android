package com.websarva.wings.android.zuboradiary.ui.diary.common.navigation

/**
 * 日記フロー（詳細表示・編集など）の呼び出し元（起動元）を表す列挙型。
 *
 * この情報は、日記フローを終了して複数のバックスタックエントリをまとめて破棄(Pop)して戻る際に、
 * 適切な遷移先（日記一覧、カレンダー、検索結果など）を決定するために使用される。
 */
enum class DiaryFlowLaunchSource {
    /** 日記一覧画面から起動されたことを表す。 */
    DiaryList,

    /** ワード検索画面から起動されたことを表す。 */
    WordSearch,

    /** カレンダー画面から起動されたことを表す。 */
    Calendar
}
