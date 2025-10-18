package com.websarva.wings.android.zuboradiary.ui.adapter.recycler

import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView
import com.websarva.wings.android.zuboradiary.ui.view.custom.WindowInsetsViewHolder
import com.websarva.wings.android.zuboradiary.core.utils.logTag

internal open class LeftSwipeSimpleCallback(protected val recyclerView: SwipeRecyclerView) :
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT) {

    abstract class LeftSwipeViewHolder<T>(
        binding: ViewBinding
    ) : WindowInsetsViewHolder(binding.root) {

        abstract val foregroundView: View
        abstract val backgroundButtonView: View

        fun setClickableAllView(clickable: Boolean) {
            foregroundView.isClickable = clickable
            backgroundButtonView.isClickable = clickable
        }

        abstract fun bind(
            item: T,
            onItemClick: (T) -> Unit,
            onDeleteButtonClick: (T) -> Unit
        )

        fun setUpForegroundViewOnClickListener(listener: View.OnClickListener) {
            foregroundView.setOnClickListener(listener)
        }

        fun setUpBackgroundViewOnClickListener(listener: View.OnClickListener) {
            backgroundButtonView.setOnClickListener(listener)
        }
    }

    private lateinit var itemTouchHelper: ItemTouchHelper

    // MEMO:スワイプ時、タッチ状態を継続したままRecyclerViewを更新するとonSwiped()が起動するが、
    //      対象ViewHolderのItemPositionが-1となるため、Overrideで記述したコードで例外が発生する。
    //      その為、RecyclerViewを更新時はgetSwipeDirs()をOverrideしてスワイプ機能を無効にする。
    private var isItemInteractionEnabled = true

    private val initializePosition = -1
    private var swipingAdapterPosition: Int = initializePosition
    protected var swipedAdapterPosition: Int = initializePosition
        private set
    private var invalidSwipeAdapterPosition: Int = initializePosition

    private val previousMotionEventAction
        get() = recyclerView.previousMotionEventAction

    fun interface OnSelectedChangedListener {
        fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int)
    }
    private var onSelectedChangedListener: OnSelectedChangedListener? = null

    open fun build() {
        itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        setUpLeftSwipeItem()
        setUpSwipeStatePositionsResetObserver()
        setUpSwipedItemCloseOnScrollListener()
    }

    private fun setUpLeftSwipeItem() {
        recyclerView.setOnTouchUpListener {
            clearInvalidSwipeViewHolder()
        }
    }

    private fun setUpSwipeStatePositionsResetObserver() {
        val adapter = recyclerView.adapter as ListAdapter<*, *>
        adapter.registerAdapterDataObserver(
            SwipeStatePositionsResetObserver {
                resetSwipeStatePositions()
            }
        )
    }

    private fun setUpSwipedItemCloseOnScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) return

                // スクロール時スワイプ閉
                closeSwipedItem()
            }
        })
    }

    private class SwipeStatePositionsResetObserver(
        private val processResetSwipeStatePositions: () -> Unit
    ) : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            processResetSwipeStatePositions()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            processResetSwipeStatePositions()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            processResetSwipeStatePositions()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            processResetSwipeStatePositions()
        }
    }

    // MEMO:スワイプクローズアニメーション開始時にfalseとなり、終了時にtrueとなるようにしているが、
    //      終了時にタッチ中の場合はfalseのままとしているため、ここでtrueにする。
    //      理由は"InvalidSwipeAdapterPosition"書き込みコード参照。
    private fun clearInvalidSwipeViewHolder() {
        if (invalidSwipeAdapterPosition == initializePosition) return

        val lockedViewHolder =
            recyclerView.findViewHolderForAdapterPosition(invalidSwipeAdapterPosition)

        val leftSwipeViewHolder = lockedViewHolder as LeftSwipeViewHolder<*>
        leftSwipeViewHolder.setClickableAllView(true)

        resetInvalidSwipeAdapterPosition()
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

        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>
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
            }
            .withEndAction {
                // MEMO:Viewをアニメーションで視覚的にスワイプ前の状態に戻しても、
                //      内部(ItemTouchHelper)的にはスワイプ状態が続く為、下記でリセットする。
                itemTouchHelper.onChildViewDetachedFromWindow(viewHolder.itemView)
                itemTouchHelper.onChildViewAttachedToWindow(viewHolder.itemView)

                if (swipingAdapterPosition == position) resetSwipingAdapterPosition()
                if (swipedAdapterPosition == position) resetSwipedAdapterPosition()

                // MEMO:アニメーション中にスワイプしてそのままタッチを継続されると、
                //      アニメーション終了後にスワイプ分、前面Viewが移動してしまう。
                //      対策として、下記条件コード記述。
                if (previousMotionEventAction != MotionEvent.ACTION_UP) {
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

        if (!isItemInteractionEnabled) return 0

        // 他ViewHolderスワイプ中スワイプ機能無効
        if (swipingAdapterPosition != initializePosition
            && viewHolder.bindingAdapterPosition != swipingAdapterPosition) return 0

        // アニメーション中スワイプ機能無効
        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>
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

        onSelectedChangedListener?.onSelectedChanged(viewHolder, actionState)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
    ): Boolean {
        Log.d(logTag, "onMove()")
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Log.d(logTag, "onSwiped()_position = " + viewHolder.bindingAdapterPosition)
        if (direction != ItemTouchHelper.LEFT) return
        if (swipingAdapterPosition != viewHolder.getBindingAdapterPosition()) return

        swipedAdapterPosition = swipingAdapterPosition
        resetSwipingAdapterPosition()

        processOnSwiped(viewHolder)
    }

    protected open fun processOnSwiped(viewHolder: RecyclerView.ViewHolder) {
        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>
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

        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>

        // アニメーション中は無効
        if (!leftSwipeViewHolder.foregroundView.isClickable) return
        // 右スワイプ
        if (dX > 0f) return

        // 手動でスワイプを戻した時にクリア
        if (dX == 0f) {
            if (swipedAdapterPosition == viewHolder.getBindingAdapterPosition()) {
                resetSwipedAdapterPosition()
            }
        }

        translateForegroundView(viewHolder, dX, actionState)
    }

    protected open fun translateForegroundView(
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        actionState: Int
    ) {
        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>
        leftSwipeViewHolder.foregroundView.translationX = dX
    }

    override fun clearView(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ) {
        Log.d(logTag, "clearView()_position = " + viewHolder.bindingAdapterPosition)
        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>
        leftSwipeViewHolder.foregroundView.translationX = 0F
        super.clearView(recyclerView, viewHolder)
    }

    private fun resetSwipeStatePositions() {
        resetSwipingAdapterPosition()
        resetSwipedAdapterPosition()
        resetInvalidSwipeAdapterPosition()
    }

    private fun resetSwipingAdapterPosition() {
        swipingAdapterPosition = initializePosition
    }

    private fun resetSwipedAdapterPosition() {
        swipedAdapterPosition = initializePosition
    }

    private fun resetInvalidSwipeAdapterPosition() {
        invalidSwipeAdapterPosition = initializePosition
    }

    fun updateIsItemMovementEnabled(enabled: Boolean) {
        isItemInteractionEnabled = enabled
    }

    fun closeSwipedItem() {
        if (swipingAdapterPosition != initializePosition) closeSwipedViewHolder(swipingAdapterPosition)
        if (swipedAdapterPosition != initializePosition) closeSwipedViewHolder(swipedAdapterPosition)
    }

    fun registerOnSelectedChangedListener(listener: OnSelectedChangedListener) {
        onSelectedChangedListener = listener
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
