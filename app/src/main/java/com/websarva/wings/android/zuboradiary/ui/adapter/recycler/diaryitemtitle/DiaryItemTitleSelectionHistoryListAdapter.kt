package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diaryitemtitle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.databinding.RowItemTitleSelectionHistoryBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeSimpleCallback.LeftSwipeViewHolder
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diaryitemtitle.DiaryItemTitleSelectionHistoryListAdapter.ItemTitleSelectionHistoryViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListItemUi
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView

internal class DiaryItemTitleSelectionHistoryListAdapter(
    recyclerView: SwipeRecyclerView,
    themeColor: ThemeColorUi
) : LeftSwipeListBaseAdapter<DiaryItemTitleSelectionHistoryListItemUi, ItemTitleSelectionHistoryViewHolder>(
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
        item: DiaryItemTitleSelectionHistoryListItemUi
    ) {
        holder.bind(
            item,
            { onItemClickListener?.onClick(it) },
            { onItemSwipeListener?.onSwipe(it) },
        )
    }

    class ItemTitleSelectionHistoryViewHolder(
        val binding: RowItemTitleSelectionHistoryBinding
    ) : LeftSwipeViewHolder<DiaryItemTitleSelectionHistoryListItemUi>(binding) {
        override val foregroundView
            get() = binding.textTitle
        override val backgroundButtonView
            get() = binding.includeBackground.imageButtonDelete

        override fun bind(
            item: DiaryItemTitleSelectionHistoryListItemUi,
            onItemClick: (DiaryItemTitleSelectionHistoryListItemUi) -> Unit,
            onDeleteButtonClick: (DiaryItemTitleSelectionHistoryListItemUi) -> Unit
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
        DiffUtil.ItemCallback<DiaryItemTitleSelectionHistoryListItemUi>() {

        override fun areItemsTheSame(
            oldItem: DiaryItemTitleSelectionHistoryListItemUi,
            newItem: DiaryItemTitleSelectionHistoryListItemUi
        ): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(
            oldItem: DiaryItemTitleSelectionHistoryListItemUi,
            newItem: DiaryItemTitleSelectionHistoryListItemUi
        ): Boolean {
            return false
        }
    }

    fun closeSwipedItem() {
        leftSwipeSimpleCallback.closeSwipedItem()
    }
}
