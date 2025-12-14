package com.websarva.wings.android.zuboradiary.ui.navigation.params

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker.DatePickerDialogFragment
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * 日付選択ダイアログ（[DatePickerDialogFragment]）に渡すための引数モデル。
 *
 * @property resultKey ダイアログの結果を返すためのユニークなキー。
 * @property initialDate ダイアログが最初に表示する日付。
 */
@Parcelize
data class DatePickerArgs(
    val resultKey: String,
    val initialDate: LocalDate
) : Parcelable
