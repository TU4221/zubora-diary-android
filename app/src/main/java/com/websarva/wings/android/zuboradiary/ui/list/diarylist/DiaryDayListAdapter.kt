package com.websarva.wings.android.zuboradiary.ui.list.diarylist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding
import com.websarva.wings.android.zuboradiary.createLogTag
import com.websarva.wings.android.zuboradiary.ui.DiaryPictureManager
import com.websarva.wings.android.zuboradiary.ui.LeftSwipeSimpleCallback.LeftSwipeViewHolder
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.list.SwipeDiaryDayListBaseAdapter
import java.text.NumberFormat

internal class DiaryDayListAdapter(context: Context, recyclerView: RecyclerView, themeColor: ThemeColor)
    : SwipeDiaryDayListBaseAdapter(
        context,
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
        val dayOfWeekStringConverter = DayOfWeekStringConverter(context)
        val strDayOfWeek = dayOfWeekStringConverter.toDiaryListDayOfWeek(date.dayOfWeek)
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
        onBindPicture(holder, item)
    }

    private fun onBindTitle(holder: DiaryDayListViewHolder, item: DiaryDayListItem) {
        val title = item.title
        holder.binding.textTitle.text = title
    }

    private fun onBindPicture(holder: DiaryDayListViewHolder, item: DiaryDayListItem) {
        val diaryPictureManager =
            DiaryPictureManager(
                context,
                holder.binding.imageAttachedPicture,
                themeColor.getOnSecondaryContainerColor(context.resources)
            )
        val pictureUri = item.picturePath
        diaryPictureManager.setUpPictureOnDiaryList(pictureUri)
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

            Log.d(logTag, "DiffUtil.ItemCallback_areContentsTheSame()")
            if (oldItem.title != newItem.title) {
                Log.d(logTag, "Title不一致")
                return false
            }
            if (oldItem.picturePath != newItem.picturePath) {
                Log.d(logTag, "PicturePath不一致")
                return false
            }
            return true
        }
    }
}
