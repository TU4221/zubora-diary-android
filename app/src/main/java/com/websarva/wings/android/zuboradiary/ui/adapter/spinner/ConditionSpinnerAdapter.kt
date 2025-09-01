package com.websarva.wings.android.zuboradiary.ui.adapter.spinner

import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.ConditionUi
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor


internal class ConditionSpinnerAdapter(
    context: Context,
    themeColor: ThemeColor,
) : ArrayAdapter<String>(
    ContextThemeWrapper(context, themeColor.themeResId),
    R.layout.layout_drop_down_list_item,
    ConditionUi.entries.toList().map { it.toString(context) }
)
