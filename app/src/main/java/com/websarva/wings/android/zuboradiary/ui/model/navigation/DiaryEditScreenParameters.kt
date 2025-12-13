package com.websarva.wings.android.zuboradiary.ui.model.navigation

import android.os.Parcelable
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
 */
@Parcelize
data class DiaryEditScreenParameters(
    val resultKey: String,
    val diaryId: String? = null,
    val diaryDate: LocalDate,
) : Parcelable
