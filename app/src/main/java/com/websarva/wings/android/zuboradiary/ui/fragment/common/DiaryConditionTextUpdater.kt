package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.content.Context
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.domain.model.Condition

internal class DiaryConditionTextUpdater {

    fun update(context: Context, textView: TextView, condition: Condition) {
        textView.text = condition.toString(context)
    }
}
