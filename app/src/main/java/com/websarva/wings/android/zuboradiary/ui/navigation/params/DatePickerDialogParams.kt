package com.websarva.wings.android.zuboradiary.ui.navigation.params

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * 日付選択ダイアログへの遷移時に渡すパラメータ。
 *
 * Navigation Componentの引数として使用されることを想定している。
 *
 * @property resultKey ダイアログの結果を返すためのユニークなキー。
 * @property initialDate ダイアログが最初に表示する日付。
 */
@Parcelize
data class DatePickerDialogParams(
    val resultKey: String,
    val initialDate: LocalDate
) : Parcelable
