package com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.theme.withTheme

internal abstract class ListBaseAdapter <T, VH : RecyclerView.ViewHolder> protected constructor(
    protected val themeColor: ThemeColorUi,
    diffUtilItemCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffUtilItemCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val themeColorInflater = inflater.withTheme(themeColor)
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

    fun getItemAt(position: Int): T {
        return getItem(position)
    }
}
