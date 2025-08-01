package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.view.View
import android.widget.LinearLayout
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber

internal class DiaryItemsVisibilityUpdater {

    fun update(itemLayouts: Array<LinearLayout>, numVisibleItems: Int) {
        require(itemLayouts.size >= ItemNumber.MIN_NUMBER && itemLayouts.size <= ItemNumber.MAX_NUMBER)
        require(numVisibleItems >= ItemNumber.MIN_NUMBER && numVisibleItems <= ItemNumber.MAX_NUMBER)

        for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
            val itemArrayNumber = i - 1
            if (i <= numVisibleItems) {
                itemLayouts[itemArrayNumber].visibility = View.VISIBLE
            } else {
                itemLayouts[itemArrayNumber].visibility = View.GONE
            }
        }
    }
}
