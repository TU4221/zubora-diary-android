package com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowBackgroundDeleteButtonFullWideBinding
import com.websarva.wings.android.zuboradiary.databinding.RowItemTitleSelectionHistoryBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.LeftSwipeSimpleCallback
import com.websarva.wings.android.zuboradiary.ui.adapter.LeftSwipeSimpleCallback.LeftSwipeViewHolder
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.ItemTitleSelectionHistoryListAdapter.ItemTitleSelectionHistoryViewHolder
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView

internal class ItemTitleSelectionHistoryListAdapter(
    private val recyclerView: SwipeRecyclerView,
    private val themeColor: ThemeColor
) : ListAdapter<SelectionHistoryListItem, ItemTitleSelectionHistoryViewHolder>(
        DiaryItemTitleSelectionHistoryDiffUtilItemCallback()
    ) {

    private var onClickItemListener: OnClickItemListener? = null
    private var onClickDeleteButtonListener: OnClickDeleteButtonListener? = null

    private lateinit var leftSwipeSimpleCallback: LeftSwipeSimpleCallback

    fun build() {
        recyclerView.apply {
            adapter = this@ItemTitleSelectionHistoryListAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
        }

        leftSwipeSimpleCallback = LeftSwipeSimpleCallback(recyclerView)
        leftSwipeSimpleCallback.build()
    }

    fun clearViewBindings() {
        recyclerView.apply {
            adapter = null
            layoutManager = null
            for (i in 0 until itemDecorationCount) {
                removeItemDecorationAt(0)
            }
        }
        leftSwipeSimpleCallback.clearViewBindings()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemTitleSelectionHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val themeColorInflater = ThemeColorInflaterCreator().create(inflater, themeColor)
        val binding =
            RowItemTitleSelectionHistoryBinding.inflate(themeColorInflater, parent, false)
        return ItemTitleSelectionHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemTitleSelectionHistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(
            item,
            { onClickItemListener?.onClick(it) },
            { onClickDeleteButtonListener?.onClick(it) },
        )
    }

    fun interface OnClickItemListener {
        fun onClick(item: SelectionHistoryListItem)
    }

    fun setOnClickItemListener(onClickItemListener: OnClickItemListener) {
        this.onClickItemListener = onClickItemListener
    }

    fun interface OnClickDeleteButtonListener {
        fun onClick(item: SelectionHistoryListItem)
    }

    fun setOnClickDeleteButtonListener(onClickDeleteButtonListener: OnClickDeleteButtonListener) {
        this.onClickDeleteButtonListener = onClickDeleteButtonListener
    }

    internal class ItemTitleSelectionHistoryViewHolder(
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

    internal class DiaryItemTitleSelectionHistoryDiffUtilItemCallback

        : DiffUtil.ItemCallback<SelectionHistoryListItem>() {
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
