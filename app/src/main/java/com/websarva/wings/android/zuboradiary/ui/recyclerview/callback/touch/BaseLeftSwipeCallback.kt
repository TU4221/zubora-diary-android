package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch

import android.graphics.Canvas
import android.util.Log
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.core.utils.logTag

internal abstract class BaseLeftSwipeCallback(
    private val findViewHolder: (position: Int) -> RecyclerView.ViewHolder?,
    private val reattachViewHolderAfterCloseAnimation: (RecyclerView.ViewHolder) -> Unit,
    private val onSwiped: (position: Int) -> Unit = {}
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.ACTION_STATE_IDLE,
    ItemTouchHelper.LEFT
) {

    // MEMO:スワイプ時、タッチ状態を継続したままRecyclerViewを更新するとonSwiped()が起動するが、
    //      対象ViewHolderのItemPositionが-1となるため、Overrideで記述したコードで例外が発生する。
    //      その為、RecyclerViewを更新時はgetSwipeDirs()をOverrideしてスワイプ機能を無効にする。
    private var isItemSwipeEnabled = true

    private val initializePosition = -1
    private var swipingAdapterPosition: Int = initializePosition
    var swipedAdapterPosition: Int = initializePosition
        private set

    fun resetSwipeStatePositions() {
        resetSwipingAdapterPosition()
        resetSwipedAdapterPosition()
    }

    fun closeSwipedItem() {
        if (swipingAdapterPosition != initializePosition) closeSwipedViewHolder(swipingAdapterPosition)
        if (swipedAdapterPosition != initializePosition) closeSwipedViewHolder(swipedAdapterPosition)
    }

    fun updateItemSwipeEnabledState(enabled: Boolean) {
        isItemSwipeEnabled = enabled
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        Log.d(logTag, "getMovementFlags()_position:${viewHolder.bindingAdapterPosition}")

        if (viewHolder !is SwipeableViewHolder) return 0
        if (!isItemSwipeEnabled) return 0

        // 他ViewHolderスワイプ中スワイプ機能無効
        if (swipingAdapterPosition != initializePosition
            && swipingAdapterPosition != viewHolder.bindingAdapterPosition) return 0

        // スワイプ状態でもないのにisRollingBackがtrueの場合リセットする
        if (swipedAdapterPosition == initializePosition && viewHolder.isRollingBack) {
            viewHolder.isRollingBack = false
        }
        // アニメーション中スワイプ機能無効
        if (viewHolder.isRollingBack) return 0

        return super.getMovementFlags(recyclerView, viewHolder)
    }

    // MEMO:タッチダウン、アップで呼び出し
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        val position = viewHolder?.bindingAdapterPosition ?: initializePosition
        Log.d(logTag,
            "onSelectedChanged()_position:$position"
                    + "_actionState:${toStringItemTouchHelperActionState(actionState)}"
        )

        super.onSelectedChanged(viewHolder, actionState)

        if (viewHolder == null) return
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return

        swipingAdapterPosition = viewHolder.bindingAdapterPosition

        // 他ViewHolderがスワイプ状態時の処理
        Log.d(logTag, "onSelectedChanged()_swipedAdapterPosition:$swipedAdapterPosition")
        if (swipedAdapterPosition >= 0
            && swipedAdapterPosition != viewHolder.bindingAdapterPosition
        ) {
            closeSwipedViewHolder(swipedAdapterPosition)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
    ): Boolean {
        Log.d(logTag, "onMove()")
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Log.d(logTag, "onSwiped()_position:${viewHolder.bindingAdapterPosition}")
        if (direction != ItemTouchHelper.LEFT) return

        swipedAdapterPosition = viewHolder.bindingAdapterPosition
        resetSwipingAdapterPosition()

        onSwiped(viewHolder.bindingAdapterPosition)
        onSwipedHook(viewHolder)
    }

    protected open fun onSwipedHook(viewHolder: RecyclerView.ViewHolder) {
        // サブクラスがこのメソッドをオーバーライドして、
        // スワイプ完了時の追加処理を実装する。
        // デフォルトでは何もしない。
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        Log.d(logTag,
            "onChildDraw()_position:${viewHolder.bindingAdapterPosition}"
                    + "_dX:$dX"
                    + "_actionState:${toStringItemTouchHelperActionState(actionState)}"
                    + "_isCurrentlyActive:$isCurrentlyActive"
        )
        if (viewHolder !is SwipeableViewHolder) return
        if (!isItemSwipeEnabled)
            if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return

        // アニメーション中は無効
        if (viewHolder.isRollingBack) return
        // 右スワイプ
        if (dX > 0f) return

        drawViewHolder(viewHolder, dX)
    }

    protected abstract fun drawViewHolder(viewHolder: RecyclerView.ViewHolder, dX: Float)

    // MEMO:clearView()起動タイミング
    //      ・スワイプ状態でスワイプ操作開始
    //      ・スワイプ操作でアイテムをスワイプ前の状態に戻した時
    //      ・アイテムをデタッチアタッチした時
    override fun clearView(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ) {
        Log.d(logTag, "clearView()_position:${viewHolder.bindingAdapterPosition}")
        super.clearView(recyclerView, viewHolder)

        // ユーザーがスワイプ操作でアイテムをスワイプ前に戻した時
        if (swipedAdapterPosition == swipingAdapterPosition
            && swipedAdapterPosition == viewHolder.getBindingAdapterPosition()) {
            resetSwipedAdapterPosition()
        }
        if (swipingAdapterPosition == viewHolder.getBindingAdapterPosition()) {
            resetSwipingAdapterPosition()
        }
    }

    // MEMO:スワイプメニューを閉じるアニメーション中も onChildDraw が反応する
    //
    //      1、閉じる前に clearView すると onChildDraw は反応しなくなるが、アニメーションが効かなくなって半開きのまま再利用されてしまう
    //      2、アニメーション終了後に notifyItemChanged して再度開く時の onChildDraw の dX をリセットしておく必要がある
    //      3、notifyItemChanged を使うと、高速でスワイプさせた時に複数箇所を開けてしまい挙動が安定しない
    //
    //      アニメーション開始前に ItemTouchHelper#onChildViewDetached/AttachedFromWindow でリセットしておくと、これらの問題を解消できる
    //      アニメーション中のフラグ isAnimating が無いので isClickable で代用する
    //      (参照:https://mt312.com/3182)
    protected fun closeSwipedViewHolder(position: Int) {
        Log.d(logTag, "closeSwipedViewHolder()_position:$position")

        val viewHolder = findViewHolder(position) ?: return

        animateCloseSwipedViewHolder(position, viewHolder)
    }

    private fun animateCloseSwipedViewHolder(position: Int, viewHolder: RecyclerView.ViewHolder) {
        Log.d(logTag, "animateCloseSwipedViewHolder()_position:$position")

        val leftSwipeViewHolder = viewHolder as? SwipeableViewHolder ?: return
        leftSwipeViewHolder.foregroundView.animate()
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
            .translationX(0f)
            .withStartAction {
                // MEMO:アニメーション中のViewHolderをタッチすると、
                //      ItemTouchHelper.Callback#getMovementFlags()で
                //      スワイプ機能を無効にするようにしている為、クリック機能が有効となる。
                //      アニメーション中はリスナーを機能させたくないので下記コードを記述。
                leftSwipeViewHolder.isRollingBack = true
            }
            .withEndAction {
                // MEMO:Viewをアニメーションで視覚的にスワイプ前の状態に戻しても、
                //      内部(ItemTouchHelper)的にはスワイプ状態が続く為、下記でリセットする。
                reattachViewHolderAfterCloseAnimation(viewHolder)

                leftSwipeViewHolder.isRollingBack = false
            }
            .start()
    }

    private fun resetSwipingAdapterPosition() {
        swipingAdapterPosition = initializePosition
    }

    private fun resetSwipedAdapterPosition() {
        swipedAdapterPosition = initializePosition
    }

    private fun toStringItemTouchHelperActionState(actionState: Int): String {
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_IDLE -> return "ACTION_STATE_IDLE"
            ItemTouchHelper.ACTION_STATE_SWIPE -> return "ACTION_STATE_SWIPE"
            ItemTouchHelper.ACTION_STATE_DRAG -> return "ACTION_STATE_DRAG"
        }

        return "UNKNOWN_ACTION_STATE"
    }
}
