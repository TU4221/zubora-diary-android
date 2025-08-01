package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.content.Context
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateTimeWithSecondsString
import java.time.LocalDateTime

internal class DiaryLogTextUpdater {

    fun update(context: Context, textView: TextView, dateTime: LocalDateTime) {
        val dateString = dateTime.toJapaneseDateTimeWithSecondsString(context)
        textView.text = dateString
    }
}
