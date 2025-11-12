package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch

import android.view.View

interface SwipeableViewHolder {
    val foregroundView: View
    var isRollingBack: Boolean
}
