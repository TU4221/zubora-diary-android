package com.websarva.wings.android.zuboradiary.ui.adapter.spinner

import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.diary.ConditionUi
import com.websarva.wings.android.zuboradiary.ui.mapper.asString
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi


internal class ConditionSpinnerAdapter(
    context: Context,
    themeColor: ThemeColorUi,
) : ArrayAdapter<String>(
    ContextThemeWrapper(context, themeColor.themeResId),
    R.layout.layout_drop_down_list_item,
    ConditionUi.entries.toList().map { it.asString(context) }
)
