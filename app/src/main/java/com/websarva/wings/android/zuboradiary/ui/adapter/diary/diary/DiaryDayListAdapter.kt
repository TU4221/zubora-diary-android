package com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryImageConfigurator
import com.websarva.wings.android.zuboradiary.ui.adapter.LeftSwipeSimpleCallback.LeftSwipeViewHolder
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.SwipeDiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.utils.toDiaryListDayOfWeekString
import java.text.NumberFormat

internal class DiaryDayListAdapter(recyclerView: RecyclerView, themeColor: ThemeColor)
    : SwipeDiaryDayListBaseAdapter(
        recyclerView,
        themeColor,
        DiffUtilItemCallback()
    ) {

    override fun createDiaryDayViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater
    ): RecyclerView.ViewHolder {
        val binding =
            RowDiaryDayListBinding.inflate(themeColorInflater, parent, false)
        return DiaryDayListViewHolder(binding, themeColor)
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: DiaryDayListBaseItem) {
        holder as DiaryDayListViewHolder
        item as DiaryDayListItem

        holder.bind(
            item,
            { onClickItemListener?.onClick(it) },
            { onClickDeleteButtonListener?.onClick(it) }
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder as DiaryDayListViewHolder
        val item = getItem(position) as DiaryDayListItem

        holder.bind(
            item,
            { onClickItemListener?.onClick(it) },
            { onClickDeleteButtonListener?.onClick(it) }
        )
    }

    override fun onBindDeleteButtonClickListener(
        holder: RecyclerView.ViewHolder,
        item: DiaryDayListBaseItem
    ) {
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

    private class DiffUtilItemCallback : DiaryDayListBaseAdapter.DiffUtilItemCallback() {

        val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryDayListBaseItem,
            newItem: DiaryDayListBaseItem
        ): Boolean {
            if (oldItem !is DiaryDayListItem) throw IllegalStateException()
            if (newItem !is DiaryDayListItem) throw IllegalStateException()

            if (!oldItem.areContentsTheSame(newItem)) {
                Log.d(logTag, "areContentsTheSame()_全項目不一致")
                return false
            }

            Log.d(logTag, "areContentsTheSame()_全項目一致")
            return true
        }
    }
}
