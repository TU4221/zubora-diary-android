package com.websarva.wings.android.zuboradiary.ui.recyclerview.decoration

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface StickyHeaderAdapter {
    fun isHeader(itemPosition: Int): Boolean
    fun getHeaderView(itemPosition: Int, recyclerView: RecyclerView): View?
}
