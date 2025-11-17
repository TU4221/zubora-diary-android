package com.websarva.wings.android.zuboradiary.ui.recyclerview.decoration

import android.graphics.Canvas
import android.view.View
import androidx.core.graphics.withTranslation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

internal class StickyHeaderDecoration(
    private val stickyHeaderAdapter: StickyHeaderAdapter
) : RecyclerView.ItemDecoration() {

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        // リストの一番上に表示されているアイテムのポジションを取得
        // MEMO:画面遷移時、MotionLayoutによりRecyclerViewの高さが変わる時がある為、
        //      RecyclerViewから取得しようとすると安定して値を取得することができない(最後尾のアイテムポジションを取得する)。
        //      その為、LinearLayoutManagerから取得するようにする。
        val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return
        val topChildPosition = layoutManager.findFirstVisibleItemPosition()
        if (topChildPosition == RecyclerView.NO_POSITION) return

        // アイテムが属するヘッダーのViewを取得
        val currentHeader =
            stickyHeaderAdapter
                .getHeaderView(topChildPosition, parent) ?: return

        // 次のヘッダーを見つけて、押し上げる処理
        val contactPoint = currentHeader.bottom
        val childInContact = findChildInContact(parent, contactPoint)

        val pushUpOffset = if (childInContact != null) {
            val childAdapterPosition = parent.getChildAdapterPosition(childInContact)
            if (childAdapterPosition != -1
                && stickyHeaderAdapter.isHeader(childAdapterPosition)) {
                // 次のヘッダーが接触してきたら、現在のヘッダーを押し上げる
                (contactPoint - childInContact.top).toFloat()
            } else {
                0f
            }
        } else {
            0f
        }

        // ヘッダーを描画
        drawHeader(c, currentHeader, -pushUpOffset)
    }

    /**
     * 指定されたY座標にある子Viewを見つけるヘルパーメソッド。
     */
    private fun findChildInContact(parent: RecyclerView, contactPoint: Int): View? {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.bottom > contactPoint && child.top <= contactPoint) {
                return child
            }
        }
        return null
    }

    /**
     * Canvasにヘッダーを描画するヘルパーメソッド。
     */
    private fun drawHeader(c: Canvas, header: View, pushUpOffset: Float) {
        c.withTranslation(0f, pushUpOffset) {
            header.draw(this)
        }
    }
}
