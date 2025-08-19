package com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.LeftSwipeBackgroundButtonListBaseAdapter
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryImageConfigurator
import com.websarva.wings.android.zuboradiary.ui.adapter.LeftSwipeSimpleCallback.LeftSwipeViewHolder
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseDiffUtilItemCallback
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayListAdapter.DiaryDayListViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.utils.toDiaryListDayOfWeekString
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView
import java.text.NumberFormat

internal class DiaryDayListAdapter(recyclerView: SwipeRecyclerView, themeColor: ThemeColor)
    : LeftSwipeBackgroundButtonListBaseAdapter<DiaryDayListItem, DiaryDayListViewHolder>(
        recyclerView,
        themeColor,
        DiffUtilItemCallback()
    ) {

    override fun build() {
        super.build()

        // MEMO:DiaryYearMonthListBaseAdapter#build()内にて理由記載)
        recyclerView.itemAnimator = null
    }

    override fun createViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater,
        viewType: Int
    ): DiaryDayListViewHolder {
        val binding =
            RowDiaryDayListBinding.inflate(themeColorInflater, parent, false)
        return DiaryDayListViewHolder(binding, themeColor)
    }

    override fun bindViewHolder(holder: DiaryDayListViewHolder, item: DiaryDayListItem) {
        holder.bind(
            item,
            { onClickItemListener?.onClick(it) },
            { onClickDeleteButtonListener?.onClick(it) }
        )
    }

    class DiaryDayListViewHolder(
        val binding: RowDiaryDayListBinding,
        val themeColor: ThemeColor
    ) : LeftSwipeViewHolder<DiaryDayListItem>(binding) {

        override val foregroundView
            get() = binding.linerLayoutForeground
        override val backgroundButtonView
            get() = binding.includeBackground.imageButtonDelete

        override fun bind(
            item: DiaryDayListItem,
            onItemClick: (DiaryDayListItem) -> Unit,
            onDeleteButtonClick: (DiaryDayListItem) -> Unit
        ) {
            val date = item.date
            val context = binding.root.context
            val strDayOfWeek = date.dayOfWeek.toDiaryListDayOfWeekString(context)
            binding.includeDay.textDayOfWeek.text = strDayOfWeek
            binding.includeDay.textDayOfMonth.text =
                NumberFormat.getInstance().format(date.dayOfMonth)
            binding.textTitle.text = item.title
            DiaryImageConfigurator()
                .setUpImageOnDiaryList(
                    binding.imageAttachedImage,
                    item.imageUri,
                    themeColor
                )

            setUpForegroundViewOnClickListener {
                onItemClick(item)
            }
            setUpBackgroundViewOnClickListener {
                onDeleteButtonClick(item)
            }
        }
    }

    private class DiffUtilItemCallback : DiaryDayListBaseDiffUtilItemCallback<DiaryDayListItem>() {

        val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryDayListItem,
            newItem: DiaryDayListItem
        ): Boolean {
            if (!oldItem.areContentsTheSame(newItem)) {
                Log.d(logTag, "areContentsTheSame()_全項目不一致")
                return false
            }

            Log.d(logTag, "areContentsTheSame()_全項目一致")
            return true
        }
    }
}
