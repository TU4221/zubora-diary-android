package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.view.View
import android.widget.LinearLayout

internal class DiaryItemsVisibilityUpdater {

    fun update(itemLayouts: Array<LinearLayout>, numVisibleItems: Int) {
        itemLayouts.forEachIndexed { index, linearLayout ->
            if (index < numVisibleItems) {
                linearLayout.visibility = View.VISIBLE
            } else {
                linearLayout.visibility = View.GONE
            }
        }
    }
}
