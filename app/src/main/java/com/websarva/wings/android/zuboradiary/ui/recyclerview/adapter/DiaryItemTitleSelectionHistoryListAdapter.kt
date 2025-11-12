package com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.view.custom.WindowInsetsViewHolder
import com.websarva.wings.android.zuboradiary.databinding.RowItemTitleSelectionHistoryBinding
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.diffUtil.GenericDiffUtilItemCallback
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.DiaryItemTitleSelectionHistoryListAdapter.DiaryItemTitleSelectionHistoryViewHolder
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.SwipeableViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListItemUi

internal class DiaryItemTitleSelectionHistoryListAdapter (
    themeColor: ThemeColorUi,
    private val onItemClick: (DiaryItemTitleSelectionHistoryListItemUi) -> Unit
) : ListBaseAdapter<DiaryItemTitleSelectionHistoryListItemUi, DiaryItemTitleSelectionHistoryViewHolder>(
    themeColor,
    GenericDiffUtilItemCallback()
) {

    override fun createViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater,
        viewType: Int
    ): DiaryItemTitleSelectionHistoryViewHolder {
        val binding =
            RowItemTitleSelectionHistoryBinding.inflate(themeColorInflater, parent, false)
        return DiaryItemTitleSelectionHistoryViewHolder(
            binding,
            onItemClick
        )
    }

    override fun bindViewHolder(
        holder: DiaryItemTitleSelectionHistoryViewHolder,
        item: DiaryItemTitleSelectionHistoryListItemUi
    ) {
        holder.bind(item)
    }

    data class DiaryItemTitleSelectionHistoryViewHolder(
        private val binding: RowItemTitleSelectionHistoryBinding,
        private val onItemClickListener: (DiaryItemTitleSelectionHistoryListItemUi) -> Unit
    ) : WindowInsetsViewHolder(binding.root), SwipeableViewHolder {

        override val foregroundView: View
            get() = binding.textTitle

        override var isRollingBack: Boolean = false

        fun bind(item: DiaryItemTitleSelectionHistoryListItemUi) {
            binding.textTitle.text = item.title

            foregroundView.setOnClickListener {
                onItemClickListener(item)
            }
        }
    }
}
