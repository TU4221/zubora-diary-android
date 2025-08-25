package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary

import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeBackgroundButtonListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeBackgroundButtonSimpleCallback
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItemUi

// DiaryFragment、WordSearchFragmentの親RecyclerViewのListAdapter。
// 親RecyclerViewを同じ構成にする為、一つのクラスで両方の子RecyclerViewに対応できるように作成。
internal abstract class SwipeDiaryYearMonthListBaseAdapter<
        CLIT : DiaryDayListItemUi
> protected constructor(
    recyclerView: RecyclerView,
    themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtilItemCallback<CLIT>
) : DiaryYearMonthListBaseAdapter<CLIT>(recyclerView, themeColor, diffUtilItemCallback) {

    fun interface OnChildItemBackgroundButtonClickListener<T> {
        fun onClick(item: T)
    }
    protected var onChildItemBackgroundButtonClickListener: OnChildItemBackgroundButtonClickListener<CLIT>? = null

    private val simpleCallbackList: MutableList<LeftSwipeBackgroundButtonSimpleCallback> = ArrayList()

    override fun build() {
        super.build()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) return

                // スクロール時スワイプ閉
                closeAllSwipedItem()
            }
        })
    }

    override fun clearViewBindings() {
        super.clearViewBindings()

        onChildItemBackgroundButtonClickListener = null
    }

    override fun bindViewHolder(
        holder: DiaryYearMonthListViewHolder,
        item: DiaryYearMonthListItemUi<CLIT>
    ) {
        super.bindViewHolder(holder, item)

        when (holder) {
            is DiaryYearMonthListViewHolder.Item -> {
                val adapter = holder.binding.recyclerDayList.adapter as LeftSwipeBackgroundButtonListBaseAdapter<*, *>
                val simpleCallback = adapter.leftSwipeBackgroundButtonSimpleCallback
                simpleCallback.registerOnSelectedChangedListener { _, _ ->
                    closeSwipedItemOtherDayList(simpleCallback)
                }
                simpleCallbackList.add(simpleCallback)
            }
            is DiaryYearMonthListViewHolder.NoDiaryMessage,
            is DiaryYearMonthListViewHolder.ProgressBar -> {
                // 処理不要
            }
        }
    }

    fun registerOnChildItemBackgroundButtonClickListener(
        listener: OnChildItemBackgroundButtonClickListener<CLIT>
    ) {
        onChildItemBackgroundButtonClickListener = listener
    }

    fun closeAllSwipedItem() {
        for (i in simpleCallbackList) {
            i.closeSwipedItem()
        }
    }

    private fun closeSwipedItemOtherDayList(selfSimpleCallback: LeftSwipeBackgroundButtonSimpleCallback) {
        for (i in simpleCallbackList.indices) {
            if (simpleCallbackList[i] !== selfSimpleCallback) {
                simpleCallbackList[i].closeSwipedItem()
            }
        }
    }

    fun setSwipeEnabled(enabled: Boolean) {
        for (i in simpleCallbackList) {
            i.isSwipeEnabled = enabled
        }
    }
}
