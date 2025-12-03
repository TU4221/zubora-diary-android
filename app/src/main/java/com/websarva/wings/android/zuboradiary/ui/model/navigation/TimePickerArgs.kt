package com.websarva.wings.android.zuboradiary.ui.model.navigation

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker.TimePickerDialogFragment
import kotlinx.parcelize.Parcelize
import java.time.LocalTime

/**
 * 時刻選択ダイアログ（[TimePickerDialogFragment]）に渡すための引数モデル。
 *
 * @property resultKey ダイアログの結果を返すためのユニークなキー。
 * @property initialTime ダイアログが最初に表示する時刻。
 */
@Parcelize
data class TimePickerArgs(
    val resultKey: String,
    val initialTime: LocalTime
) : Parcelable
