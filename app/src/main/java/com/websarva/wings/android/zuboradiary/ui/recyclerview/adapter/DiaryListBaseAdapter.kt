package com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
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

internal abstract class DiaryListBaseAdapter<T, VH> (
    themeColor: ThemeColorUi
) : ListBaseAdapter<DiaryListItemUi<T>, DiaryListViewHolder>(
    themeColor,
    DiaryListDiffUtilItemCallback()
), SpacingItemProvider, StickyHeaderAdapter
        where T: DiaryListItemContainerUi, VH: DiaryListViewHolder {

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

    protected abstract fun onCreateDiaryViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater
    ): DiaryListViewHolder

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

    protected abstract fun onBindDiaryViewHolder(
        holder: DiaryListViewHolder,
        item: DiaryListItemUi.Diary<T>
    )

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

    override fun isSpacingItem(itemPosition: Int): Boolean {
        if (itemPosition !in 0..<itemCount) return false
        return getItem(itemPosition) is DiaryListItemUi.Diary
    }

    override fun isHeader(itemPosition: Int): Boolean {
        if (itemPosition !in 0..<itemCount) return false
        return getItem(itemPosition) is DiaryListItemUi.Header
    }

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

        // TODO:リスト最後尾の時だけヘッダーが上手く表示さない。
        // ヘッダーポジションに対応するViewHolderを、再利用プールから取得する
        /*val headerHolder =
            recyclerView
                .findViewHolderForAdapterPosition(headerPosition) as? DiaryListHeaderViewHolder

        Log.d(
            logTag,
            "headerHolder = ${
                if (headerHolder is DiaryListHeaderViewHolder) {
                    headerHolder.date
                } else {
                    "miss"
                }
            }"
        )*/

        // iewHolderが画面外でnullの場合は、新しく生成してバインドする
        /*return headerHolder?.itemView ?: run {
            val createdHolder =
                onCreateViewHolder(
                    recyclerView,
                    ViewType.HEADER.viewTypeNumber
                ) as DiaryListHeaderViewHolder
            onBindViewHolder(createdHolder, headerPosition)

            // Viewを描画するためにmeasure/layoutを呼び出す
            createdHolder.itemView.apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(
                        recyclerView.width, View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(
                        0, View.MeasureSpec.UNSPECIFIED
                    )
                )
                layout(0, 0, measuredWidth, measuredHeight)
            }
        }*/
    }

    /**
     * 指定されたポジションのアイテムが属するヘッダーのポジションを探すヘルパーメソッド
     */
    private fun findHeaderPositionFor(itemPosition: Int): Int {
        for (i in itemPosition downTo 0) {
            if (isHeader(i)) {
                return i
            }
        }
        return -1
    }

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
     * ViewHolderのビューのサイズを測定し、レイアウトするためのヘルパーメソッド。
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

    abstract class DiaryListViewHolder(
        itemView: View
    ) : WindowInsetsViewHolder(itemView)

    data class DiaryListHeaderViewHolder(
        private val binding: RowDiaryListHeaderBinding
    ) : DiaryListViewHolder(binding.root) {

        lateinit var date: String

        fun bind(item: DiaryListItemUi.Header<*>) {
            // 対象行の情報を取得
            val diaryYearMonth = item.yearMonth

            // セクションバー設定
            // 左端に余白を持たせる為、最初にスペースを入力。
            val context = binding.root.context
            val diaryDate =
                (diaryYearMonth.year.toString() + context.getString(R.string.row_diary_year_month_list_section_bar_year)
                        + diaryYearMonth.monthValue + context.getString(R.string.row_diary_year_month_list_section_bar_month))
            binding.textSection.text = item.yearMonth.formatYearMonthString(context).also { date = it }
            // 日記リストスクロール時に移動させているので、バインディング時に位置リセット
            binding.textSection.y = 0f
        }
    }

    data class DiaryListNoDiaryMessageViewHolder(
        private val binding: RowDiaryListNoDiaryMessageBinding
    ) : DiaryListViewHolder(binding.root)

    data class DiaryListProgressBarViewHolder(
        private val binding: RowDiaryListProgressBarBinding
    ) : DiaryListViewHolder(binding.root)

    private class DiaryListDiffUtilItemCallback<T: DiaryListItemContainerUi>
        : DiffUtil.ItemCallback<DiaryListItemUi<T>>() {

        override fun areItemsTheSame(
            oldItem: DiaryListItemUi<T>,
            newItem: DiaryListItemUi<T>
        ): Boolean {
            val result = run {
                // HACK:RecyclerViewの初回アイテム表示時にスクロール初期位置がズレる事がある。
                //      原因はプログレスバーの存在。最初にアイテムを表示する時、読込中の意味を込めてプログレスバーのみを表示させている。
                //      スクロール読込機能の仕様により、読込データをRecyclerViewに表示する際、アイテムリスト末尾にプログレスバーを追加している。
                //      これにより、初回読込中プログレスバーとアイテムリスト末尾のプログレスバーが同一アイテムと認識するため、
                //      ListAdapterクラスの仕様により表示されていたプログレスバーが更新後も表示されるようにスクロール位置がズレた。
                //      プログレスバー同士が同一アイテムと認識されないようにするために、下記条件を追加して対策。
                //      areContentsTheSame()では対応できない。
                if (oldItem is DiaryListItemUi.ProgressIndicator) return@run false

                if (oldItem::class != newItem::class) return@run false

                if (oldItem is DiaryListItemUi.Diary && newItem is DiaryListItemUi.Diary) {
                    return@run oldItem.containerUi.id == newItem.containerUi.id
                }

                return@run true
            }
            Log.d(
                logTag,
                "areItemsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
            )
            return result
        }

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
