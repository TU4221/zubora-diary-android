package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator

internal abstract class DiaryDayListBaseAdapter protected constructor(
    protected val recyclerView: RecyclerView,
    protected val themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtilItemCallback
) :

    ListAdapter<DiaryDayListBaseItem, RecyclerView.ViewHolder>(diffUtilItemCallback) {

    fun interface OnClickItemListener {
        fun onClick(item: DiaryDayListBaseItem)
    }
    var onClickItemListener: OnClickItemListener? = null

    fun build() {
        recyclerView.apply {
            adapter = this@DiaryDayListBaseAdapter
            layoutManager = LinearLayoutManager(context)
            // MEMO:DiaryYearMonthAdapter#build()内にて理由記載)
            itemAnimator = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val themeColorInflater = ThemeColorInflaterCreator().create(inflater, themeColor)

        return onCreateDiaryDayViewHolder(parent, themeColorInflater)
    }

    protected abstract fun onCreateDiaryDayViewHolder(
        parent: ViewGroup, themeColorInflater: LayoutInflater
    ): RecyclerView.ViewHolder

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        onBindDate(holder, item)
        onBindItemClickListener(holder, item)
        onBindOtherView(holder, item)
    }

    protected abstract fun onBindDate(holder: RecyclerView.ViewHolder, item: DiaryDayListBaseItem)

    protected abstract fun onBindItemClickListener(
        holder: RecyclerView.ViewHolder,
        item: DiaryDayListBaseItem
    )

    protected abstract fun onBindOtherView(
        holder: RecyclerView.ViewHolder,
        item: DiaryDayListBaseItem
    )

    protected fun onClickItem(item: DiaryDayListBaseItem) {
        onClickItemListener?.onClick(item) ?: return
    }

    protected abstract class DiffUtilItemCallback : DiffUtil.ItemCallback<DiaryDayListBaseItem>() {

        private val logTag = createLogTag()

        override fun areItemsTheSame(
            oldItem: DiaryDayListBaseItem,
            newItem: DiaryDayListBaseItem
        ): Boolean {
            Log.d(logTag, "areItemsTheSame()")
            Log.d(logTag, "oldItem_Date = " + oldItem.date)
            Log.d(logTag, "newItem_Date = " + newItem.date)

            return oldItem.areItemsTheSame(newItem)
        }
    }
}
