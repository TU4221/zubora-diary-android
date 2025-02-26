package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowItemTitleSelectionHistoryBinding
import com.websarva.wings.android.zuboradiary.ui.LeftSwipeSimpleCallback
import com.websarva.wings.android.zuboradiary.ui.LeftSwipeSimpleCallback.LeftSwipeViewHolder
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit.ItemTitleSelectionHistoryListAdapter.ItemTitleSelectionHistoryViewHolder

internal class ItemTitleSelectionHistoryListAdapter
    (
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val themeColor: ThemeColor
) :
    ListAdapter<SelectionHistoryListItem, ItemTitleSelectionHistoryViewHolder>(
        DiaryItemTitleSelectionHistoryDiffUtilItemCallback()
    ) {

    private var onClickItemListener: OnClickItemListener? = null
    private var onClickDeleteButtonListener: OnClickDeleteButtonListener? = null

    private lateinit var leftSwipeSimpleCallback: LeftSwipeSimpleCallback

    fun build() {
        recyclerView.adapter = this
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        leftSwipeSimpleCallback = LeftSwipeSimpleCallback(recyclerView)
        leftSwipeSimpleCallback.build()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemTitleSelectionHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val creator = ThemeColorInflaterCreator(context, inflater, themeColor)
        val themeColorInflater = creator.create()
        val binding =
            RowItemTitleSelectionHistoryBinding.inflate(themeColorInflater, parent, false)
        return ItemTitleSelectionHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemTitleSelectionHistoryViewHolder, position: Int) {
        val item = checkNotNull(getItem(position))
        val title = item.title
        holder.binding.textTitle.text = title
        holder.binding.textTitle.setOnClickListener {
            if (onClickItemListener == null) return@setOnClickListener
            checkNotNull(onClickItemListener).onClick(title)
        }
        holder.binding.includeBackground.imageButtonDelete.setOnClickListener {
            if (onClickDeleteButtonListener == null) return@setOnClickListener

            // MEMO:onBindViewHolder()の引数であるpositionを使用すると警告がでる。
            checkNotNull(onClickDeleteButtonListener).onClick(holder.bindingAdapterPosition, title)
        }
    }

    internal fun interface OnClickItemListener {
        fun onClick(title: String)
    }

    fun setOnClickItemListener(onClickItemListener: OnClickItemListener) {
        this.onClickItemListener = onClickItemListener
    }

    internal fun interface OnClickDeleteButtonListener {
        fun onClick(position: Int, title: String)
    }

    fun setOnClickDeleteButtonListener(onClickDeleteButtonListener: OnClickDeleteButtonListener) {
        this.onClickDeleteButtonListener = onClickDeleteButtonListener
    }

    internal class ItemTitleSelectionHistoryViewHolder(
        val binding: RowItemTitleSelectionHistoryBinding,
    ) : LeftSwipeViewHolder(binding) {
        override val foregroundView = binding.textTitle
        override val backgroundButtonView = binding.includeBackground.imageButtonDelete
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
