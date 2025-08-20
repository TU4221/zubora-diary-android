package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayBaseList
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem

// DiaryFragment、WordSearchFragmentの親RecyclerViewのListAdapter。
// 親RecyclerViewを同じ構成にする為、一つのクラスで両方の子RecyclerViewに対応できるように作成。
internal abstract class SwipeDiaryYearMonthListBaseAdapter<
        LT : DiaryDayBaseList,
        CLIT : DiaryDayListItem
> protected constructor(
    recyclerView: RecyclerView,
    themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtilItemCallback<LT>
) : DiaryYearMonthListBaseAdapter<LT, CLIT>(recyclerView, themeColor, diffUtilItemCallback) {

    fun interface OnChildItemBackgroundButtonClickListener<T> {
        fun onClick(item: T)
    }
    protected var onChildItemBackgroundButtonClickListener: OnChildItemBackgroundButtonClickListener<CLIT>? = null

    private val simpleCallbackList: MutableList<SwipeDiaryYearMonthListSimpleCallback> = ArrayList()

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

    override fun createViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater,
        viewType: Int
    ): DiaryYearMonthListViewHolder {
        val holder = super.createViewHolder(parent, themeColorInflater, viewType)

        when (holder) {
            is DiaryYearMonthListViewHolder.Item -> {
                SwipeDiaryYearMonthListSimpleCallback(recyclerView, holder.binding.recyclerDayList)
                    .apply {
                        build()
                        simpleCallbackList.add(this)
                    }
            }
            is DiaryYearMonthListViewHolder.NoDiaryMessage,
            is DiaryYearMonthListViewHolder.ProgressBar -> {
                // 処理不要
            }
        }

        return holder
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

    fun closeSwipedItemOtherDayList(selfSimpleCallback: SwipeDiaryYearMonthListSimpleCallback) {
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
