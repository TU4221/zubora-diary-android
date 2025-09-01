package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.content.Context
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.model.ConditionUi

internal class DiaryConditionTextUpdater {

    fun update(context: Context, textView: TextView, condition: ConditionUi) {
        textView.text = condition.toString(context)
    }
}
