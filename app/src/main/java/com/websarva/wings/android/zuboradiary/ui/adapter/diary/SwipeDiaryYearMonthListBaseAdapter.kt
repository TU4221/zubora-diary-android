package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryListSimpleCallback

// DiaryFragment、WordSearchFragmentの親RecyclerViewのListAdapter。
// 親RecyclerViewを同じ構成にする為、一つのクラスで両方の子RecyclerViewに対応できるように作成。
internal abstract class SwipeDiaryYearMonthListBaseAdapter protected constructor(
    recyclerView: RecyclerView,
    themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtilItemCallback
) : DiaryYearMonthListBaseAdapter(recyclerView, themeColor, diffUtilItemCallback) {

    fun interface OnClickChildItemBackgroundButtonListener {
        fun onClick(item: DiaryDayListBaseItem)
    }
    var onClickChildItemBackgroundButtonListener: OnClickChildItemBackgroundButtonListener? = null

    private val simpleCallbackList: MutableList<DiaryListSimpleCallback> = ArrayList()

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = super.onCreateViewHolder(parent, viewType)

        if (holder is DiaryYearMonthListViewHolder) {
            DiaryListSimpleCallback(recyclerView, holder.binding.recyclerDayList)
                .apply {
                    build()
                    simpleCallbackList.add(this)
                }
        }

        return holder
    }

    fun closeAllSwipedItem() {
        for (i in simpleCallbackList) {
            i.closeSwipedItem()
        }
    }

    fun closeSwipedItemOtherDayList(selfSimpleCallback: DiaryListSimpleCallback) {
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
