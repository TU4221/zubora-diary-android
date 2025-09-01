package com.websarva.wings.android.zuboradiary.ui.adapter.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator

internal abstract class ListBaseAdapter <T, VH : RecyclerView.ViewHolder> protected constructor(
    protected val recyclerView: RecyclerView,
    protected val themeColor: ThemeColorUi,
    diffUtilItemCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffUtilItemCallback) {

    fun interface OnItemClickListener<T> {
        fun onClick(item: T)
    }
    protected var onItemClickListener: OnItemClickListener<T>? = null

    open fun build() {
        recyclerView.apply {
            adapter = this@ListBaseAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val themeColorInflater = ThemeColorInflaterCreator().create(inflater, themeColor)

        return createViewHolder(parent, themeColorInflater, viewType)
    }

    protected abstract fun createViewHolder(
        parent: ViewGroup, themeColorInflater: LayoutInflater, viewType: Int
    ): VH

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        bindViewHolder(holder, item)
    }

    abstract fun bindViewHolder(holder: VH, item: T)

    fun registerOnItemClickListener(listener: OnItemClickListener<T>) {
        onItemClickListener = listener
    }
}
