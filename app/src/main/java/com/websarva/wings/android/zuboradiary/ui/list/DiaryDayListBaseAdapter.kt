package com.websarva.wings.android.zuboradiary.ui.list

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator

internal abstract class DiaryDayListBaseAdapter protected constructor(
    protected val context: Context,
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
        recyclerView.adapter = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val creator = ThemeColorInflaterCreator(parent.context, inflater, themeColor)
        val themeColorInflater = creator.create()

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
        override fun areItemsTheSame(
            oldItem: DiaryDayListBaseItem,
            newItem: DiaryDayListBaseItem
        ): Boolean {
            Log.d(javaClass.simpleName, "areItemsTheSame()")
            Log.d(javaClass.simpleName, "oldItem_Date = " + oldItem.date)
            Log.d(javaClass.simpleName, "newItem_Date = " + newItem.date)

            return oldItem.date == newItem.date
        }
    }
}
