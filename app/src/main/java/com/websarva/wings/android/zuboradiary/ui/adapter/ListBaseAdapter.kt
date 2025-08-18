package com.websarva.wings.android.zuboradiary.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator

internal abstract class ListBaseAdapter <T, VH : RecyclerView.ViewHolder> protected constructor(
    protected val recyclerView: RecyclerView,
    protected val themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffUtilItemCallback) {

    fun interface OnClickItemListener<T> {
        fun onClick(item: T)
    }
    protected var onClickItemListener: OnClickItemListener<T>? = null

    open fun build() {
        recyclerView.apply {
            adapter = this@ListBaseAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    open fun clearViewBindings() {
        recyclerView.apply {
            adapter = null
            layoutManager = null
        }
        onClickItemListener = null
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

    fun registerOnClickItemListener(onClickItemListener: OnClickItemListener<T>) {
        this.onClickItemListener = onClickItemListener
    }
}
