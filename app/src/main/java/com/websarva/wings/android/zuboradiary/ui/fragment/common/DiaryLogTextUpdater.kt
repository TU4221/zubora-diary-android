package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.content.Context
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.utils.formatJapaneseDateTimeWithSecondsString
import java.time.LocalDateTime

internal class DiaryLogTextUpdater {

    fun update(context: Context, textView: TextView, dateTime: LocalDateTime) {
        val dateString = dateTime.formatJapaneseDateTimeWithSecondsString(context)
        textView.text = dateString
    }
}
