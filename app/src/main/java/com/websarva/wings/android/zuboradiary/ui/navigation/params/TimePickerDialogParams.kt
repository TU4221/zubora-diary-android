package com.websarva.wings.android.zuboradiary.ui.navigation.params

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalTime

/**
 * 時刻選択ダイアログへの遷移時に渡すパラメータ。
 *
 * Navigation Componentの引数として使用されることを想定している。
 *
 * @property resultKey ダイアログの結果を返すためのユニークなキー。
 * @property initialTime ダイアログが最初に表示する時刻。
 */
@Parcelize
data class TimePickerDialogParams(
    val resultKey: String,
    val initialTime: LocalTime
) : Parcelable
