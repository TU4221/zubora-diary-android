package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryListSimpleCallback

// DiaryFragment、WordSearchFragmentの親RecyclerViewのListAdapter。
// 親RecyclerViewを同じ構成にする為、一つのクラスで両方の子RecyclerViewに対応できるように作成。
internal abstract class SwipeDiaryYearMonthListBaseAdapter<T : DiaryYearMonthListBaseItem> protected constructor(
    recyclerView: RecyclerView,
    themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtilItemCallback<T>
) : DiaryYearMonthListBaseAdapter<T>(recyclerView, themeColor, diffUtilItemCallback) {

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

    override fun clearViewBindings() {
        super.clearViewBindings()

        onClickChildItemBackgroundButtonListener = null
    }

    override fun createViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val holder = super.createViewHolder(parent, themeColorInflater, viewType)

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
