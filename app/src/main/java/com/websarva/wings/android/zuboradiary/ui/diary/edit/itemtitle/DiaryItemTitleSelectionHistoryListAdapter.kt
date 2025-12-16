package com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.websarva.wings.android.zuboradiary.databinding.RowItemTitleSelectionHistoryBinding
import com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle.DiaryItemTitleSelectionHistoryListAdapter.DiaryItemTitleSelectionHistoryViewHolder
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.common.recyclerview.adapter.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.common.recyclerview.callback.diffUtil.GenericDiffUtilItemCallback
import com.websarva.wings.android.zuboradiary.ui.common.recyclerview.callback.touch.SwipeableViewHolder
import com.websarva.wings.android.zuboradiary.ui.common.view.WindowInsetsViewHolder

/**
 * 日記項目タイトルの選択履歴リスト(RecyclerView)に表示されるリストのアダプター。
 *
 * [ListBaseAdapter]を継承し、履歴リストアイテムに特化したViewHolderの生成とデータバインドを実装する。
 * アイテムのスワイプによる削除機能もサポートする。
 *
 * @param themeColor アイテムのViewに適用するテーマカラー。
 * @param onItemClick 履歴アイテムがクリックされた際のコールバック。
 */
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

    /**
     * 日記項目タイトルの選択履歴リストアイテムデータ([DiaryItemTitleSelectionHistoryListItemUi])を
     * 表示するためのViewHolder。
     *
     * [SwipeableViewHolder]を実装し、スワイプ操作をサポートする。
     *
     * @property binding View Bindingのインスタンス。
     * @property onItemClickListener 履歴アイテムがクリックされた際のコールバック。
     */
    data class DiaryItemTitleSelectionHistoryViewHolder(
        private val binding: RowItemTitleSelectionHistoryBinding,
        private val onItemClickListener: (DiaryItemTitleSelectionHistoryListItemUi) -> Unit
    ) : WindowInsetsViewHolder(binding.root), SwipeableViewHolder {

        /** スワイプの対象となるフォアグラウンドビュー。 */
        override val foregroundView: View
            get() = binding.textTitle

        /** スワイプ後のロールバックアニメーション中であるかを示すフラグ。 */
        override var isRollingBack: Boolean = false

        /**
         * 履歴アイテムのデータをViewにバインドする。
         * @param item 表示する履歴アイテム。
         */
        fun bind(item: DiaryItemTitleSelectionHistoryListItemUi) {
            binding.textTitle.text = item.title

            foregroundView.setOnClickListener {
                onItemClickListener(item)
            }
        }
    }
}
