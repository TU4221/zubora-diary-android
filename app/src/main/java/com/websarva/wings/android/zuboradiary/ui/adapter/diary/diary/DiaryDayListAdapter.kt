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

    override fun onCreateDiaryDayViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater
    ): RecyclerView.ViewHolder {
        val binding =
            RowDiaryDayListBinding.inflate(themeColorInflater, parent, false)
        return DiaryDayListViewHolder(binding)
    }

    override fun onBindDate(holder: RecyclerView.ViewHolder, item: DiaryDayListBaseItem) {
        if (holder !is DiaryDayListViewHolder) throw IllegalStateException()

        val date = item.date
        val context = holder.binding.root.context
        val strDayOfWeek = date.dayOfWeek.toDiaryListDayOfWeekString(context)
        holder.binding.includeDay.textDayOfWeek.text = strDayOfWeek
        holder.binding.includeDay.textDayOfMonth.text =
            NumberFormat.getInstance().format(date.dayOfMonth)
    }

    override fun onBindItemClickListener(
        holder: RecyclerView.ViewHolder,
        item: DiaryDayListBaseItem
    ) {
        if (holder !is DiaryDayListViewHolder) throw IllegalStateException()
        holder.foregroundView.setOnClickListener {
            onClickItem(item)
        }
    }

    override fun onBindOtherView(holder: RecyclerView.ViewHolder, item: DiaryDayListBaseItem) {
        if (holder !is DiaryDayListViewHolder) throw IllegalStateException()
        if (item !is DiaryDayListItem) throw IllegalStateException()

        onBindTitle(holder, item)
        onBindImage(holder, item)
    }

    private fun onBindTitle(holder: DiaryDayListViewHolder, item: DiaryDayListItem) {
        val title = item.title
        holder.binding.textTitle.text = title
    }

    private fun onBindImage(holder: DiaryDayListViewHolder, item: DiaryDayListItem) {
        val imageUri = item.imageUri
        DiaryImageConfigurator()
            .setUpImageOnDiaryList(
                holder.binding.imageAttachedImage,
                imageUri,
                themeColor
            )
    }

    override fun onBindDeleteButtonClickListener(
        holder: RecyclerView.ViewHolder,
        item: DiaryDayListBaseItem
    ) {
        if (holder !is DiaryDayListViewHolder) throw IllegalStateException()

        holder.backgroundButtonView.setOnClickListener {
            onClickDeleteButton(item)
        }
    }

    class DiaryDayListViewHolder(val binding: RowDiaryDayListBinding)
        : LeftSwipeViewHolder(binding) {
        override val foregroundView
            get() = binding.linerLayoutForeground
        override val backgroundButtonView
            get() = binding.includeBackground.imageButtonDelete
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
