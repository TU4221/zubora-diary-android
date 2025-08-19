package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeBackgroundButtonListBaseAdapter
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryImageConfigurator
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeSimpleCallback.LeftSwipeViewHolder
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryDayListBaseDiffUtilItemCallback
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.diary.DiaryDayListAdapter.DiaryDayListViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.utils.toDiaryListDayOfWeekString
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView
import java.text.NumberFormat

internal class DiaryDayListAdapter(recyclerView: SwipeRecyclerView, themeColor: ThemeColor)
    : LeftSwipeBackgroundButtonListBaseAdapter<DiaryDayListItem.Standard, DiaryDayListViewHolder>(
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

    override fun bindViewHolder(holder: DiaryDayListViewHolder, item: DiaryDayListItem.Standard) {
        holder.bind(
            item,
            { onItemClickListener?.onClick(it) },
            { onBackgroundButtonClickListener?.onClick(it) }
        )
    }

    class DiaryDayListViewHolder(
        val binding: RowDiaryDayListBinding,
        val themeColor: ThemeColor
    ) : LeftSwipeViewHolder<DiaryDayListItem.Standard>(binding) {

        override val foregroundView
            get() = binding.linerLayoutForeground
        override val backgroundButtonView
            get() = binding.includeBackground.imageButtonDelete

        override fun bind(
            item: DiaryDayListItem.Standard,
            onItemClick: (DiaryDayListItem.Standard) -> Unit,
            onDeleteButtonClick: (DiaryDayListItem.Standard) -> Unit
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

    private class DiffUtilItemCallback : DiaryDayListBaseDiffUtilItemCallback<DiaryDayListItem.Standard>() {

        val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryDayListItem.Standard,
            newItem: DiaryDayListItem.Standard
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
