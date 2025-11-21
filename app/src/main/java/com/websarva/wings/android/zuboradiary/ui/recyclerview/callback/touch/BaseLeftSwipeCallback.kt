package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch

import android.graphics.Canvas
import android.util.Log
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.core.utils.logTag

/**
 * RecyclerViewのアイテムを左方向にスワイプするための、[ItemTouchHelper.Callback]の抽象基底クラス。
 *
 * 以下の責務を持つ:
 * - スワイプ方向を左のみに限定する。
 * - スワイプ状態（スワイプ中、スワイプ完了後）の位置情報を管理する。
 * - スワイプ機能の有効/無効を動的に切り替える。
 * - 他のアイテムがスワイプされている場合に、新たなスワイプを無効にする。
 * - スワイプされたアイテムを閉じる（元の位置に戻す）アニメーションを提供する。
 * - スワイプ中のビューの描画処理をサブクラスに委譲する。
 *
 * @param findViewHolder 指定されたポジションのViewHolderを見つけるための関数。
 * @param reattachViewHolderAfterCloseAnimation クローズアニメーションの完了後に、
 *        ViewHolderをItemTouchHelperに再アタッチするためのコールバック。
 * @param onSwiped アイテムが完全にスワイプされたときに呼び出されるコールバック。
 */
internal abstract class BaseLeftSwipeCallback(
    private val findViewHolder: (position: Int) -> RecyclerView.ViewHolder?,
    private val reattachViewHolderAfterCloseAnimation: (RecyclerView.ViewHolder) -> Unit,
    private val onSwiped: (position: Int) -> Unit = {}
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.ACTION_STATE_IDLE,
    ItemTouchHelper.LEFT
) {

    /**
     * スワイプ機能が現在有効であるかを示すフラグ。
     * RecyclerViewの更新中など、一時的にスワイプを無効化するために使用する。
     */
    // MEMO:スワイプ時、タッチ状態を継続したままRecyclerViewを更新するとonSwiped()が起動するが、
    //      対象ViewHolderのItemPositionが-1となるため、Overrideで記述したコードで例外が発生する。
    //      その為、RecyclerViewを更新時はgetSwipeDirs()をOverrideしてスワイプ機能を無効にする。
    private var isItemSwipeEnabled = true

    private val initializePosition = -1
    /** 現在ユーザーがスワイプ操作中のアイテムのアダプターポジション。操作中でなければ`-1`。 */
    private var swipingAdapterPosition: Int = initializePosition

    /** ユーザーの操作が離れ、スワイプされたままになっているアイテムのアダプターポジション。存在しなければ`-1`。 */
    var swipedAdapterPosition: Int = initializePosition
        private set

    /** スワイプ中およびスワイプ完了後のアイテムポジションをすべてリセットする。 */
    fun resetSwipeStatePositions() {
        resetSwipingAdapterPosition()
        resetSwipedAdapterPosition()
    }

    /** 現在スワイプされている（またはスワイプ中の）アイテムを閉じるアニメーションを開始する。 */
    fun closeSwipedItem() {
        if (swipingAdapterPosition != initializePosition) closeSwipedViewHolder(swipingAdapterPosition)
        if (swipedAdapterPosition != initializePosition) closeSwipedViewHolder(swipedAdapterPosition)
    }

    /**
     * スワイプ機能の有効/無効状態を更新する。
     * @param enabled 有効にする場合は`true`。
     */
    fun updateItemSwipeEnabledState(enabled: Boolean) {
        isItemSwipeEnabled = enabled
    }

    /**
     * 追加処理として、以下の条件を満たさない場合はスワイプを無効化する。
     * - `SwipeableViewHolder`を実装していること
     * - スワイプ機能が有効であること ([isItemSwipeEnabled])
     * - 他のアイテムがスワイプ中でないこと
     * - アイテムがクローズアニメーション中でないこと
     */
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

    /**
     * 追加処理として、スワイプ操作が開始された場合、そのアイテムのポジションをスワイプ中の状態として記録する。
     * また、もし他のアイテムがすでにスワイプされたままの状態であれば、そのアイテムを閉じるアニメーションを開始する。
     */
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

    /** ドラッグ＆ドロップはサポートしないため、常に`false`を返す。 */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
    ): Boolean {
        Log.d(logTag, "onMove()")
        return false
    }

    /** アイテムが完全にスワイプされた時に呼び出され、状態を更新し、イベントメソッドに通知する。 */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Log.d(logTag, "onSwiped()_position:${viewHolder.bindingAdapterPosition}")
        if (direction != ItemTouchHelper.LEFT) return

        swipedAdapterPosition = viewHolder.bindingAdapterPosition
        resetSwipingAdapterPosition()

        onSwiped(viewHolder.bindingAdapterPosition)
        onSwipedHook(viewHolder)
    }

    /**
     * サブクラスでスワイプ完了時の追加処理を実装するために使用する。[onSwiped]から呼び出される。
     * @param viewHolder スワイプされたViewHolder。
     */
    protected open fun onSwipedHook(viewHolder: RecyclerView.ViewHolder) {
        // サブクラスがこのメソッドをオーバーライドして、
        // スワイプ完了時の追加処理を実装する。
        // デフォルトでは何もしない。
    }

    /**
     * このメソッドは、RecyclerViewのアイテムが動いている間のフレームごとに呼び出される。
     * スワイプ操作でない場合や、アニメーション中、右方向へのスワイプなどの
     * 不要なケースを除外した後、[drawViewHolder]を呼び出す。
     */
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

        // TODO:下記どちらが正しいか後で確認
        /*if (!isItemSwipeEnabled)
            if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return*/
        if (!isItemSwipeEnabled) return
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return

        // アニメーション中は無効
        if (viewHolder.isRollingBack) return
        // 右スワイプ
        if (dX > 0f) return

        drawViewHolder(viewHolder, dX)
    }

    /**
     * ViewHolderの具体的な描画処理をサブクラスに委譲する。[onChildDraw]から呼び出される。
     * @param viewHolder 対象のViewHolder。
     * @param dX X方向への移動量。
     */
    protected abstract fun drawViewHolder(viewHolder: RecyclerView.ViewHolder, dX: Float)

    /**
     * 追加処理として、ユーザーがスワイプ操作を途中でやめて元の位置に戻した場合などに、
     * スワイプ中またはスワイプ完了後として記録されていた状態をリセットする。
     */
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

    /**
     * 指定されたポジションの、スワイプされたViewHolderを閉じるアニメーションを開始する。
     * @param position 閉じる対象のViewHolderのアダプターポジション。
     */
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

    /**
     * ViewHolderを閉じる実際のアニメーションを実行する。
     * @param position 閉じる対象のViewHolderのアダプターポジション。
     * @param viewHolder 閉じる対象のViewHolder。
     */
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

    /** スワイプ中のアイテムポジションをリセットする。 */
    private fun resetSwipingAdapterPosition() {
        swipingAdapterPosition = initializePosition
    }

    /** スワイプ完了後のアイテムポジションをリセットする。 */
    private fun resetSwipedAdapterPosition() {
        swipedAdapterPosition = initializePosition
    }

    /**
     * [ItemTouchHelper]のアクションステート定数を、デバッグ用の文字列に変換する。
     * @param actionState 変換対象のアクションステート。
     * @return アクションステートを表す文字列。
     */
    private fun toStringItemTouchHelperActionState(actionState: Int): String {
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_IDLE -> return "ACTION_STATE_IDLE"
            ItemTouchHelper.ACTION_STATE_SWIPE -> return "ACTION_STATE_SWIPE"
            ItemTouchHelper.ACTION_STATE_DRAG -> return "ACTION_STATE_DRAG"
        }

        return "UNKNOWN_ACTION_STATE"
    }
}
