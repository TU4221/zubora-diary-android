package com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryListStandardBinding
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.StandardDiaryListAdapter.DiaryListDiaryViewHolder
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.BackgroundButtonViewHolder
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.SwipeableViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemUi
import com.websarva.wings.android.zuboradiary.ui.utils.asDiaryListDayOfWeekString
import java.text.NumberFormat

internal class StandardDiaryListAdapter (
    themeColor: ThemeColorUi,
    private val onDiaryClick: (DiaryListItemContainerUi.Standard) -> Unit,
    private val onDeleteButtonClick: (DiaryListItemContainerUi.Standard) -> Unit
) : DiaryListBaseAdapter<DiaryListItemContainerUi.Standard, DiaryListDiaryViewHolder>(themeColor) {

    override fun onCreateDiaryViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater
    ): DiaryListViewHolder {
        val binding =
            RowDiaryListStandardBinding.inflate(themeColorInflater, parent, false)
        return DiaryListDiaryViewHolder(
            binding,
            onDiaryClick,
            onDeleteButtonClick
        )
    }

    override fun onBindDiaryViewHolder(
        holder: DiaryListViewHolder,
        item: DiaryListItemUi.Diary<DiaryListItemContainerUi.Standard>
    ) {
        if (holder is DiaryListDiaryViewHolder) {
            holder.bind(item.containerUi)
        } else {
            Log.e(logTag, "予期しないViewHolderの型。")
        }
    }

    data class DiaryListDiaryViewHolder(
        private val binding: RowDiaryListStandardBinding,
        private val onDiaryClick: (DiaryListItemContainerUi.Standard) -> Unit,
        private val onDeleteButtonClick: (DiaryListItemContainerUi.Standard) -> Unit
    ) : DiaryListViewHolder(binding.root), SwipeableViewHolder, BackgroundButtonViewHolder {

        override val foregroundView: View
            get() = binding.linerLayoutForeground

        override var isRollingBack: Boolean = false

        override val backgroundButtonView: View
            get() = binding.includeBackground.imageButtonDelete

        fun bind(item: DiaryListItemContainerUi.Standard) {
            val date = item.date
            val context = binding.root.context
            val strDayOfWeek = date.dayOfWeek.asDiaryListDayOfWeekString(context)
            val imageFilePath = item.imageFilePath
            binding.includeDay.textDayOfWeek.text = strDayOfWeek
            binding.includeDay.textDayOfMonth.text =
                NumberFormat.getInstance().format(date.dayOfMonth)
            binding.textTitle.text = item.title
            binding.imageProgressAttachedImage.loadImage(imageFilePath?.path)

            foregroundView.setOnClickListener {
                onDiaryClick(item)
            }
            backgroundButtonView.setOnClickListener {
                onDeleteButtonClick(item)
            }
        }
    }
}
