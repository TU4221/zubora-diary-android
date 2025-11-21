package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper

/**
 * RecyclerViewのViewHolderが、スワイプによって表示される背景ボタンを持つことを示すインターフェース。
 *
 * [ItemTouchHelper.Callback]の実装クラスは、このインターフェースを介して背景のボタンビューにアクセスできる。
 */
interface BackgroundButtonViewHolder {

    /** スワイプアクションによって表示される背景のボタンビュー。 */
    val backgroundButtonView: View
}
