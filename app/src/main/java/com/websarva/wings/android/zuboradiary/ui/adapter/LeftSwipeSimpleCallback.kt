package com.websarva.wings.android.zuboradiary.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal open class LeftSwipeSimpleCallback(protected val recyclerView: RecyclerView) :
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT) {

    private val logTag = createLogTag()

    abstract class LeftSwipeViewHolder(binding: ViewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        abstract val foregroundView: View
        abstract val backgroundButtonView: View

        fun setClickableAllView(clickable: Boolean) {
            foregroundView.isClickable = clickable
            backgroundButtonView.isClickable = clickable
        }
    }

    private lateinit var itemTouchHelper: ItemTouchHelper

    private val initializePosition = -1
    protected var swipingAdapterPosition: Int = initializePosition
        private set
    protected var swipedAdapterPosition: Int = initializePosition
    private var invalidSwipeAdapterPosition: Int = initializePosition
    protected var previousMotionEvent: Int = initializePosition


    // TODO:
    @SuppressLint("ClickableViewAccessibility")
    open fun build() {
        itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.setOnTouchListener(OnTouchSwipedItemListener())
    }

    // TODO:
    @SuppressLint("ClickableViewAccessibility")
    protected open inner class OnTouchSwipedItemListener : OnTouchListener {
        // MEMO:スワイプ状態はItemTouchHelperが効いていてonClickListenerが反応しない為、
        //      onTouchListenerを使ってボタンの境界を判定して処理させる。
        //      通常スワイプ時、ACTION_DOWN -> MOVE -> UPとなるが
        //      未スワイプ状態からはACTION_DOWNは取得できず、ACTION_MOVE -> UPとなる。
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            Log.d(logTag, "onTouch()_MotionEvent = " + event.action)
            if (event.action == MotionEvent.ACTION_UP) clearInvalidSwipeViewHolder()
            previousMotionEvent = event.action
            return false
        }
    }

    // MEMO:スワイプクローズアニメーション開始時にfalseとなり、終了時にtrueとなるようにしているが、
    //      終了時にタッチ中の場合はfalseのままとしているため、ここでtrueにする。
    //      理由は"InvalidSwipeAdapterPosition"書き込みコード参照。
    protected fun clearInvalidSwipeViewHolder() {
        if (invalidSwipeAdapterPosition == initializePosition) return

        val lockedViewHolder =
            recyclerView.findViewHolderForAdapterPosition(invalidSwipeAdapterPosition)

        val leftSwipeViewHolder = lockedViewHolder as LeftSwipeViewHolder
        leftSwipeViewHolder.setClickableAllView(true)

        clearInvalidSwipeAdapterPosition()
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
        Log.d(logTag, "closeSwipedViewHolder()_position = $position")

        require(position >= 0)
        val adapter = checkNotNull(recyclerView.adapter)
        val listSize = adapter.itemCount
        require(position < listSize)

        val viewHolder =
            checkNotNull(recyclerView.findViewHolderForAdapterPosition(position))

        animateCloseSwipedViewHolder(position, viewHolder)
    }

    private fun animateCloseSwipedViewHolder(position: Int, viewHolder: RecyclerView.ViewHolder) {
        Log.d(logTag, "animateCloseSwipedViewHolder()_position = $position")

        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder
        leftSwipeViewHolder.foregroundView.animate()
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
            .translationX(0f)
            .withStartAction {
                // MEMO:アニメーション中のViewHolderをタッチすると、
                //      ItemTouchHelper.Callback#getMovementFlags()で
                //      スワイプ機能を無効にするようにしている為、クリック機能が有効となる。
                //      アニメーション中はリスナーを機能させたくないので下記コードを記述。
                leftSwipeViewHolder.setClickableAllView(false)

                itemTouchHelper.onChildViewDetachedFromWindow(viewHolder.itemView)
                itemTouchHelper.onChildViewAttachedToWindow(viewHolder.itemView)
            }
            .withEndAction {
                // MEMO:StartActionのリセットのみでは、スワイプしたアイテムをタッチしてスワイプ状態を戻した後、
                //      アイテムをクリックしてもアイテム前面Viewのクリックリスナーが反応しない。
                //      2回目以降は反応する。対策として下記コードを記述。
                itemTouchHelper.onChildViewDetachedFromWindow(viewHolder.itemView)
                itemTouchHelper.onChildViewAttachedToWindow(viewHolder.itemView)

                if (swipingAdapterPosition == position) clearSwipingAdapterPosition()
                if (swipedAdapterPosition == position) clearSwipedAdapterPosition()

                // MEMO:アニメーション中にスワイプしてそのままタッチを継続されると、
                //      アニメーション終了後にスワイプ分、前面Viewが移動してしまう。
                //      対策として、下記条件コード記述。
                if (previousMotionEvent != MotionEvent.ACTION_UP) {
                    invalidSwipeAdapterPosition = position
                    return@withEndAction
                }
                leftSwipeViewHolder.setClickableAllView(true)
            }
            .start()
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        Log.d(logTag, "getMovementFlags()_position = " + viewHolder.bindingAdapterPosition)

        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder

        // アニメーション中スワイプ機能無効
        if (!leftSwipeViewHolder.foregroundView.isClickable) return 0

        return super.getMovementFlags(recyclerView, viewHolder)
    }

    // MEMO:タッチダウン、アップで呼び出し
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        val position = viewHolder?.bindingAdapterPosition ?: initializePosition
        Log.d(logTag, "onSelectedChanged()_position = $position")
        Log.d(
            logTag,
            "onSelectedChanged()_actionState = " + toStringItemTouchHelperActionState(actionState)
        )

        super.onSelectedChanged(viewHolder, actionState)

        if (viewHolder == null) return
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return

        // 他ViewHolderがスワイプ中時の処理
        Log.d(logTag, "onSelectedChanged()_swipingAdapterPosition = $swipingAdapterPosition")
        if (swipingAdapterPosition >= 0
            && swipingAdapterPosition != viewHolder.bindingAdapterPosition
        ) {
            closeSwipedViewHolder(swipingAdapterPosition)
        }
        swipingAdapterPosition = viewHolder.bindingAdapterPosition

        // 他ViewHolderがスワイプ状態時の処理
        Log.d(logTag, "onSelectedChanged()_swipedAdapterPosition = $swipedAdapterPosition")
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
        if (direction != ItemTouchHelper.LEFT) return

        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder
        leftSwipeViewHolder.backgroundButtonView.performClick()
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
        Log.d(logTag, "onChildDraw()_position = " + viewHolder.bindingAdapterPosition)
        Log.d(logTag, "onChildDraw()_dX = $dX")
        Log.d(
            logTag,
            "onChildDraw()_actionState = " + toStringItemTouchHelperActionState(actionState)
        )

        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return

        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder

        // アニメーション中は無効
        if (!leftSwipeViewHolder.foregroundView.isClickable) return
        // 右スワイプ
        if (dX > 0f) return

        // 手動でスワイプを戻した時にクリア
        if (dX == 0f) {
            if (swipedAdapterPosition == viewHolder.getBindingAdapterPosition()) {
                clearSwipedAdapterPosition()
            }
        }

        translateForegroundView(viewHolder, dX, actionState)
    }

    protected open fun translateForegroundView(
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        actionState: Int
    ) {
        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder
        leftSwipeViewHolder.foregroundView.translationX = dX
    }

    override fun clearView(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ) {
        Log.d(logTag, "clearView()_position = " + viewHolder.bindingAdapterPosition)
        super.clearView(recyclerView, viewHolder)
    }

    protected fun clearSwipingAdapterPosition() {
        swipingAdapterPosition = initializePosition
    }

    private fun clearSwipedAdapterPosition() {
        swipedAdapterPosition = initializePosition
    }

    private fun clearInvalidSwipeAdapterPosition() {
        invalidSwipeAdapterPosition = initializePosition
    }

    fun closeSwipedItem() {
        if (swipingAdapterPosition != initializePosition) closeSwipedViewHolder(swipingAdapterPosition)
        if (swipedAdapterPosition != initializePosition) closeSwipedViewHolder(swipedAdapterPosition)
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
