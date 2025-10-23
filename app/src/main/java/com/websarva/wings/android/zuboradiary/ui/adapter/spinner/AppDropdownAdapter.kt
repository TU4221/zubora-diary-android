package com.websarva.wings.android.zuboradiary.ui.adapter.spinner

import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.utils.themeResId
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi


internal class AppDropdownAdapter(
    context: Context,
    themeColor: ThemeColorUi,
    objects: List<String>
) : ArrayAdapter<String>(
    ContextThemeWrapper(context, themeColor.themeResId),
    R.layout.layout_drop_down_list_item,
    objects
)
