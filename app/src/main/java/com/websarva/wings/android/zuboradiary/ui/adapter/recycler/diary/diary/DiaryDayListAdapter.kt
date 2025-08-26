package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeBackgroundButtonListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeBackgroundButtonSimpleCallback
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryImageConfigurator
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeSimpleCallback.LeftSwipeViewHolder
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryDayListBaseDiffUtilItemCallback
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.diary.DiaryDayListAdapter.DiaryDayListViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.utils.toDiaryListDayOfWeekString
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView
import java.text.NumberFormat

internal class DiaryDayListAdapter
    : LeftSwipeBackgroundButtonListBaseAdapter<DiaryDayListItemUi.Standard, DiaryDayListViewHolder> {
    constructor(
        recyclerView: SwipeRecyclerView,
        themeColor: ThemeColor
    ): super(
        recyclerView,
        themeColor,
        DiffUtilItemCallback()
    )

    constructor(
        recyclerView: SwipeRecyclerView,
        themeColor: ThemeColor,
        leftSwipeBackgroundButtonSimpleCallback: LeftSwipeBackgroundButtonSimpleCallback
    ): super(
        recyclerView,
        themeColor,
        DiffUtilItemCallback(),
        leftSwipeBackgroundButtonSimpleCallback
    )
    override fun build() {
        super.build()

        // MEMO:DiaryYearMonthListBaseAdapter#build()内にて理由記載)
        recyclerView.itemAnimator = null
    }

    // HACK:ネストされたRecyclerViewの描画タイミングの問題への対応。
    //      Adapterインスタンスを再生成することで、親と子のリストのレイアウトパスが
    //      より同期的に実行されるようになり、表示のズレが軽減される模様。
    //      通常のデータ更新方法（空リストsubmit後に新データをsubmit等）では、
    //      このタイミング問題は十分に解決できなかった。
    //      親リスト(DiaryYearMonthList)のアイテムが、子リスト(DiaryDayList)の内容よりも
    //      先に描画されてしまうため、リスト更新時にセクションバーが一瞬ちらつく。
    //      このAdapter再生成は、そのちらつきを軽減するための策。
    //      (ViewHolderの完全再生成によるパフォーマンス影響に注意)
    fun refreshAdapter(): DiaryDayListAdapter {
        val oldAdapter = this
        val newAdapter =
            DiaryDayListAdapter(
                oldAdapter.recyclerView as SwipeRecyclerView,
                oldAdapter.themeColor,
                oldAdapter.leftSwipeBackgroundButtonSimpleCallback
            ).apply {
                build()
                oldAdapter.onItemClickListener?.let {
                    registerOnItemClickListener(it)
                }
                oldAdapter.onBackgroundButtonClickListener?.let {
                    registerOnClickDeleteButtonListener(it)
                }
            }
        return newAdapter
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

    override fun bindViewHolder(holder: DiaryDayListViewHolder, item: DiaryDayListItemUi.Standard) {
        holder.bind(
            item,
            { onItemClickListener?.onClick(it) },
            { onBackgroundButtonClickListener?.onClick(it) }
        )
    }

    class DiaryDayListViewHolder(
        val binding: RowDiaryDayListBinding,
        val themeColor: ThemeColor
    ) : LeftSwipeViewHolder<DiaryDayListItemUi.Standard>(binding) {

        override val foregroundView
            get() = binding.linerLayoutForeground
        override val backgroundButtonView
            get() = binding.includeBackground.imageButtonDelete

        override fun bind(
            item: DiaryDayListItemUi.Standard,
            onItemClick: (DiaryDayListItemUi.Standard) -> Unit,
            onDeleteButtonClick: (DiaryDayListItemUi.Standard) -> Unit
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

    private class DiffUtilItemCallback : DiaryDayListBaseDiffUtilItemCallback<DiaryDayListItemUi.Standard>() {

        val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryDayListItemUi.Standard,
            newItem: DiaryDayListItemUi.Standard
        ): Boolean {
            val result = oldItem.title == newItem.title && oldItem.imageUri == newItem.imageUri

            Log.d(
                logTag,
                "areContentsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
            )
            return result
        }
    }
}
