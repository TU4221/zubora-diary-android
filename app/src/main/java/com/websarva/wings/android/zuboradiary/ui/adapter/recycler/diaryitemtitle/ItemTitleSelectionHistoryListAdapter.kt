package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diaryitemtitle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowBackgroundDeleteButtonFullWideBinding
import com.websarva.wings.android.zuboradiary.databinding.RowItemTitleSelectionHistoryBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeSimpleCallback.LeftSwipeViewHolder
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diaryitemtitle.ItemTitleSelectionHistoryListAdapter.ItemTitleSelectionHistoryViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.list.selectionhistory.SelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView

internal class ItemTitleSelectionHistoryListAdapter(
    recyclerView: SwipeRecyclerView,
    themeColor: ThemeColor
) : LeftSwipeListBaseAdapter<SelectionHistoryListItem, ItemTitleSelectionHistoryViewHolder>(
    recyclerView,
    themeColor,
    DiaryItemTitleSelectionHistoryDiffUtilItemCallback()
) {

    override fun build() {
        super.build()
        recyclerView.apply {
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
        }
    }

    override fun clearViewBindings() {
        super.clearViewBindings()
        recyclerView.apply {
            for (i in 0 until itemDecorationCount) {
                removeItemDecorationAt(0)
            }
        }
    }

    override fun createViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater,
        viewType: Int
    ): ItemTitleSelectionHistoryViewHolder {
        val binding =
            RowItemTitleSelectionHistoryBinding.inflate(themeColorInflater, parent, false)
        return ItemTitleSelectionHistoryViewHolder(binding)
    }

    override fun bindViewHolder(
        holder: ItemTitleSelectionHistoryViewHolder,
        item: SelectionHistoryListItem
    ) {
        holder.bind(
            item,
            { onClickItemListener?.onClick(it) },
            { onSwipeListener?.onSwipe(it) },
        )
    }

    class ItemTitleSelectionHistoryViewHolder(
        val binding: RowItemTitleSelectionHistoryBinding
    ) : LeftSwipeViewHolder<SelectionHistoryListItem>(binding) {
        override val foregroundView
            get() = binding.textTitle
        override val backgroundButtonView
            get() = RowBackgroundDeleteButtonFullWideBinding.bind(binding.root).imageButtonDelete

        override fun bind(
            item: SelectionHistoryListItem,
            onItemClick: (SelectionHistoryListItem) -> Unit,
            onDeleteButtonClick: (SelectionHistoryListItem) -> Unit
        ) {
            foregroundView.text = item.title
            setUpForegroundViewOnClickListener {
                onItemClick.invoke(item)
            }
            setUpBackgroundViewOnClickListener {
                onDeleteButtonClick(item)
            }
        }
    }

    internal class DiaryItemTitleSelectionHistoryDiffUtilItemCallback :
        DiffUtil.ItemCallback<SelectionHistoryListItem>() {

        override fun areItemsTheSame(
            oldItem: SelectionHistoryListItem,
            newItem: SelectionHistoryListItem
        ): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(
            oldItem: SelectionHistoryListItem,
            newItem: SelectionHistoryListItem
        ): Boolean {
            return false
        }
    }

    fun closeSwipedItem() {
        leftSwipeSimpleCallback.closeSwipedItem()
    }
}
