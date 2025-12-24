package com.websarva.wings.android.zuboradiary.ui.diary.show

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.diary.common.navigation.DiaryFlowLaunchSource
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * 日記表示画面への遷移時に渡すパラメータ。
 *
 * Navigation Componentの引数として使用されることを想定している。
 *
 * @property resultKey 日記表示画面が最終的に表示していた日記の日付を返すためのユニークなキー。
 * @property diaryId 表示対象の日記のID。新規作成の場合はnull。
 * @property diaryDate 表示対象の日記の日付。
 * @property launchSource 日記フロー（詳細表示・編集など）の呼び出し元。
 */
@Parcelize
data class DiaryShowScreenParams(
    val resultKey: String,
    val diaryId: String,
    val diaryDate: LocalDate,
    val launchSource: DiaryFlowLaunchSource
) : Parcelable
