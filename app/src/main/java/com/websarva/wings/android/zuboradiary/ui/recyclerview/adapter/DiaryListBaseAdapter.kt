package com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.view.custom.WindowInsetsViewHolder
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryListHeaderBinding
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryListNoDiaryMessageBinding
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryListProgressBarBinding
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.DiaryListBaseAdapter.DiaryListViewHolder
import com.websarva.wings.android.zuboradiary.ui.recyclerview.decoration.SpacingItemProvider
import com.websarva.wings.android.zuboradiary.ui.recyclerview.decoration.StickyHeaderAdapter
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemUi
import com.websarva.wings.android.zuboradiary.ui.utils.formatYearMonthString

/**
 * 日記リスト(`RecyclerView`)に表示されるリストのアダプターの基底クラス。
 * ヘッダー、日記アイテム、プログレスバーなど、複数のビュータイプを扱う。
 *
 * 以下の責務を持つ:
 * - [ListBaseAdapter]を継承し、テーマカラー対応とDiffUtilの基本機能を提供する。
 * - [SpacingItemProvider]と[StickyHeaderAdapter]を実装し、アイテム間の間隔とスティッキーヘッダー機能を提供する。
 * - [ViewType.HEADER], [ViewType.PROGRESS_INDICATOR], [ViewType.NO_DIARY_MESSAGE]
 *   といった共通ビュータイプのViewHolderの生成とバインドを処理する。
 * - [ViewType.DIARY]のViewHolderの具体的な生成とバインドは、サブクラスに委譲する。
 *
 * @param T [DiaryListItemContainerUi]を継承する、日記リストアイテムの具体的なデータコンテナの型。
 * @param VH [DiaryListViewHolder]を継承する、日記アイテム用のViewHolderの型。
 * @param themeColor アイテムのViewに適用するテーマカラー。
 */
internal abstract class DiaryListBaseAdapter<T, VH> (
    themeColor: ThemeColorUi
) : ListBaseAdapter<DiaryListItemUi<T>, DiaryListViewHolder>(
    themeColor,
    DiaryListDiffUtilItemCallback()
), SpacingItemProvider, StickyHeaderAdapter
        where T: DiaryListItemContainerUi, VH: DiaryListViewHolder {

    /** RecyclerViewで表示するビューの種類を定義するenum。 */
    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("RedundantSuppression")
    // MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
    enum class ViewType @Suppress("unused") constructor(val viewTypeNumber: Int) {
        HEADER(0),
        DIARY(1),
        PROGRESS_INDICATOR(2),
        NO_DIARY_MESSAGE(3)
    }

    /** [viewType]に応じて、対応するViewHolderを生成する。 */
    override fun createViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater,
        viewType: Int
    ): DiaryListViewHolder {
        return when(viewType) {
            ViewType.HEADER.viewTypeNumber -> {
                val binding =
                    RowDiaryListHeaderBinding
                        .inflate(themeColorInflater, parent, false)
                DiaryListHeaderViewHolder(binding)
            }

            ViewType.DIARY.viewTypeNumber -> {
                onCreateDiaryViewHolder(parent, themeColorInflater)
            }

            ViewType.PROGRESS_INDICATOR.viewTypeNumber -> {
                val binding =
                    RowDiaryListProgressBarBinding
                        .inflate(themeColorInflater, parent, false)
                DiaryListProgressBarViewHolder(binding)
            }

            else -> {
                val binding =
                    RowDiaryListNoDiaryMessageBinding
                        .inflate(themeColorInflater, parent, false)
                DiaryListNoDiaryMessageViewHolder(binding)
            }
        }
    }

    /**
     * [ViewType.DIARY]のViewHolderを生成する。[createViewHolder]から呼び出される。
     * @param parent 親のViewGroup。
     * @param themeColorInflater テーマが適用されたLayoutInflater。
     * @return 生成されたViewHolderインスタンス。
     */
    protected abstract fun onCreateDiaryViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater
    ): DiaryListViewHolder

    /** [item]の種類に応じて、対応するViewHolderにデータをバインドする。 */
    override fun bindViewHolder(
        holder: DiaryListViewHolder,
        item: DiaryListItemUi<T>
    ) {
        when (item) {
            is DiaryListItemUi.Header -> {
                require(holder is DiaryListHeaderViewHolder)
                holder.bind(item)
            }
            is DiaryListItemUi.Diary -> {
                onBindDiaryViewHolder(holder, item)
            }
            is DiaryListItemUi.NoDiaryMessage,
            is DiaryListItemUi.ProgressIndicator -> {
                // 処理不要
            }
        }
    }

    /**
     * [ViewType.DIARY]のViewHolderにデータをバインドする。[bindViewHolder]から呼び出される。
     * @param holder データをバインドするViewHolder。
     * @param item バインドするデータアイテム。
     */
    protected abstract fun onBindDiaryViewHolder(
        holder: DiaryListViewHolder,
        item: DiaryListItemUi.Diary<T>
    )

    /** 指定されたポジションのアイテムの[ViewType]を返す。 */
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position) as DiaryListItemUi<T>
        return when (item) {
            is DiaryListItemUi.Header -> {
                ViewType.HEADER.viewTypeNumber
            }
            is DiaryListItemUi.Diary<T> -> {
                ViewType.DIARY.viewTypeNumber
            }
            is DiaryListItemUi.ProgressIndicator -> {
                ViewType.PROGRESS_INDICATOR.viewTypeNumber
            }
            is DiaryListItemUi.NoDiaryMessage -> {
                ViewType.NO_DIARY_MESSAGE.viewTypeNumber
            }
        }
    }

    /** [SpacingItemProvider]の実装。指定されたポジションのアイテムが、間隔を設けるべき対象か判定する。 */
    override fun isSpacingItem(itemPosition: Int): Boolean {
        return itemPosition in 0..<itemCount
    }

    /** [StickyHeaderAdapter]の実装。指定されたポジションのアイテムがヘッダーか判定する。 */
    override fun isHeader(itemPosition: Int): Boolean {
        if (itemPosition !in 0..<itemCount) return false
        return getItem(itemPosition) is DiaryListItemUi.Header
    }

    /** [StickyHeaderAdapter]の実装。指定されたポジションのアイテムに対応するスティッキーヘッダーのViewを返す。 */
    override fun getHeaderView(
        itemPosition: Int,
        recyclerView: RecyclerView
    ): View? {
        if (itemPosition !in 0..<itemCount) return null

        // 指定されたポジションのアイテムが属するヘッダーのポジションを見つける
        val headerPosition = findHeaderPositionFor(itemPosition)
        if (headerPosition == -1) return null

        // ヘッダーポジションに対応するViewHolderを、再利用プールから取得する
        val headerDataItem = getItem(headerPosition) as? DiaryListItemUi.Header<*> ?: return null
        val holder = findOrCreateHeaderViewHolder(recyclerView, headerPosition)
        holder.bind(headerDataItem)
        return holder.itemView
    }

    /**
     * 指定されたポジションのアイテムが属するヘッダーのポジションを探す。
     * @param itemPosition 現在のアイテムのポジション。
     * @return 見つかったヘッダーのポジション。見つからない場合は-1。
     */
    private fun findHeaderPositionFor(itemPosition: Int): Int {
        for (i in itemPosition downTo 0) {
            if (isHeader(i)) {
                return i
            }
        }
        return -1
    }

    /**
     * ヘッダー用のViewHolderをキャッシュから探すか、新しく生成する。
     * @param parent 親となるRecyclerView。
     * @param position ヘッダーアイテムのポジション。
     * @return [DiaryListHeaderViewHolder]のインスタンス。
     */
    private fun findOrCreateHeaderViewHolder(
        parent: RecyclerView,
        position: Int
    ): DiaryListHeaderViewHolder {
        val existingHolder = parent.findViewHolderForAdapterPosition(position) as? DiaryListHeaderViewHolder
        if (existingHolder != null) return existingHolder

        val newHolder = createViewHolder(parent, ViewType.HEADER.viewTypeNumber) as DiaryListHeaderViewHolder
        measureAndLayoutViewHolder(newHolder, parent)

        return newHolder
    }

    /**
     * 指定されたViewHolderのViewのサイズを測定し、レイアウトする。
     * @param holder レイアウトするViewHolder。
     * @param parent 親となるRecyclerView。
     */
    private fun measureAndLayoutViewHolder(holder: RecyclerView.ViewHolder, parent: RecyclerView) {
        with(holder.itemView) {
            val widthSpec =
                View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
            val heightSpec =
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            measure(widthSpec, heightSpec)
            layout(0, 0, measuredWidth, measuredHeight)
        }
    }

    /** このアダプターで利用されるViewHolderの基底クラス。 */
    abstract class DiaryListViewHolder(
        itemView: View
    ) : WindowInsetsViewHolder(itemView)

    /**
     * ヘッダーアイテム([DiaryListItemUi.Header])を表示するためのViewHolder。
     * @property binding View Bindingのインスタンス。
     */
    data class DiaryListHeaderViewHolder(
        private val binding: RowDiaryListHeaderBinding
    ) : DiaryListViewHolder(binding.root) {

        /**
         * ヘッダーアイテムのデータをViewにバインドする。
         * @param item 表示するヘッダーアイテム。
         */
        fun bind(item: DiaryListItemUi.Header<*>) {
            // 左端に余白を持たせる為、最初にスペースを入力。
            val context = binding.root.context
            binding.textHeader.text = item.yearMonth.formatYearMonthString(context)
        }
    }

    /**
     * 「日記がありません」というメッセージアイテム([DiaryListItemUi.NoDiaryMessage])を表示するためのViewHolder。
     * @property binding View Bindingのインスタンス。
     */
    data class DiaryListNoDiaryMessageViewHolder(
        private val binding: RowDiaryListNoDiaryMessageBinding
    ) : DiaryListViewHolder(binding.root)

    /**
     * プログレスバーアイテム([DiaryListItemUi.ProgressIndicator])を表示するためのViewHolder。
     * @property binding View Bindingのインスタンス。
     */
    data class DiaryListProgressBarViewHolder(
        private val binding: RowDiaryListProgressBarBinding
    ) : DiaryListViewHolder(binding.root)

    /**
     * [DiaryListItemUi]の差分を計算するための[DiffUtil.ItemCallback]。
     * @param T [DiaryListItemContainerUi]を継承する、日記リストアイテムの具体的なデータコンテナの型。
     */
    private class DiaryListDiffUtilItemCallback<T: DiaryListItemContainerUi>
        : DiffUtil.ItemCallback<DiaryListItemUi<T>>() {

        /** 2つのアイテムが同じオブジェクトを表しているか（通常はIDで比較）を判定する。 */
        override fun areItemsTheSame(
            oldItem: DiaryListItemUi<T>,
            newItem: DiaryListItemUi<T>
        ): Boolean {
            val result = when {
                // HACK:RecyclerViewの初回アイテム表示時にスクロール初期位置がズレる事がある。
                //      原因はプログレスバーの存在。最初にアイテムを表示する時、読込中の意味を込めてプログレスバーのみを表示させている。
                //      スクロール読込機能の仕様により、読込データをRecyclerViewに表示する際、アイテムリスト末尾にプログレスバーを追加している。
                //      これにより、初回読込中プログレスバーとアイテムリスト末尾のプログレスバーが同一アイテムと認識するため、
                //      ListAdapterクラスの仕様により表示されていたプログレスバーが更新後も表示されるようにスクロール位置がズレた。
                //      プログレスバー同士が同一アイテムと認識されないようにするために、下記条件を追加して対策。
                //      areContentsTheSame()では対応できない。
                oldItem is DiaryListItemUi.ProgressIndicator -> false

                oldItem::class != newItem::class -> false

                oldItem is DiaryListItemUi.Diary && newItem is DiaryListItemUi.Diary -> {
                    oldItem.containerUi.id == newItem.containerUi.id
                }

                else -> true
            }
            Log.d(
                logTag,
                "areItemsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
            )
            return result
        }

        /** 2つのアイテムのデータ内容が同じであるかを判定する。 */
        override fun areContentsTheSame(
            oldItem: DiaryListItemUi<T>,
            newItem: DiaryListItemUi<T>
        ): Boolean {
            val result = oldItem == newItem

            Log.d(
                logTag,
                "areContentsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
            )
            return result
        }
    }
}
