package com.websarva.wings.android.zuboradiary.ui.diary.list

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryListStandardBinding
import com.websarva.wings.android.zuboradiary.ui.diary.list.StandardDiaryListAdapter.DiaryListDiaryViewHolder
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListItemUi
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.recyclerview.DiaryListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.common.recyclerview.callback.touch.BackgroundButtonViewHolder
import com.websarva.wings.android.zuboradiary.ui.common.recyclerview.callback.touch.SwipeableViewHolder
import com.websarva.wings.android.zuboradiary.ui.diary.common.utils.asDiaryListDayOfWeekString
import java.text.NumberFormat

/**
 * 標準的な日記リスト(`RecyclerView`)に表示されるリストのアダプター。
 *
 * [DiaryListBaseAdapter]を継承し、通常の日記リストアイテムに特化したViewHolderの生成とデータバインドを実装する。
 * アイテムのスワイプ背面ボタンの削除機能もサポートする。
 *
 * @param themeColor アイテムのViewに適用するテーマカラー。
 * @param onDiaryClick 日記アイテムがクリックされた際のコールバック。
 * @param onDeleteButtonClick 削除ボタン（スワイプ後に表示）がクリックされた際のコールバック。
 */
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

    /**
     * 標準的な日記一覧のリストアイテムデータ([DiaryListItemContainerUi.Standard])を表示するためのViewHolder。
     *
     * [SwipeableViewHolder]と[BackgroundButtonViewHolder]を実装し、スワイプ操作と背景ボタンの機能を提供する。
     *
     * @property binding View Bindingのインスタンス。
     * @property onDiaryClick 日記アイテムがクリックされた際のコールバック。
     * @property onDeleteButtonClick 削除ボタンがクリックされた際のコールバック。
     */
    data class DiaryListDiaryViewHolder(
        private val binding: RowDiaryListStandardBinding,
        private val onDiaryClick: (DiaryListItemContainerUi.Standard) -> Unit,
        private val onDeleteButtonClick: (DiaryListItemContainerUi.Standard) -> Unit
    ) : DiaryListViewHolder(binding.root), SwipeableViewHolder, BackgroundButtonViewHolder {

        /** スワイプの対象となるフォアグラウンドビュー。 */
        override val foregroundView: View
            get() = binding.linerLayoutForeground

        /** スワイプ後のロールバックアニメーション中であるかを示すフラグ。 */
        override var isRollingBack: Boolean = false

        /** スワイプによって表示される背景の削除ボタンビュー。 */
        override val backgroundButtonView: View
            get() = binding.includeBackground.imageButtonDelete

        /**
         * 日記アイテムのデータをViewにバインドする。
         * @param item 表示する日記アイテム。
         */
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
