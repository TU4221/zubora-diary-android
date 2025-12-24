package com.websarva.wings.android.zuboradiary.ui.diary.edit

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.diary.common.navigation.DiaryFlowLaunchSource
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * 日記編集画面への遷移時に渡すパラメータ。
 *
 * Navigation Componentの引数として使用されることを想定している。
 *
 * @property resultKey 日記編集画面が最終的に編集していた日記の日付を返すためのユニークなキー。
 * @property diaryId 編集対象の日記のID。新規作成の場合はnull。
 * @property diaryDate 編集対象の日記の日付。
 * @property launchSource 日記フロー（詳細表示・編集など）の呼び出し元。
 */
@Parcelize
data class DiaryEditScreenParams(
    val resultKey: String,
    val diaryId: String? = null,
    val diaryDate: LocalDate,
    val launchSource: DiaryFlowLaunchSource
) : Parcelable
