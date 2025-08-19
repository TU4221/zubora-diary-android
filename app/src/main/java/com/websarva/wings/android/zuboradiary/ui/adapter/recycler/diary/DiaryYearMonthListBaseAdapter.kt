package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryYearMonthListBinding
import com.websarva.wings.android.zuboradiary.databinding.RowNoDiaryMessageBinding
import com.websarva.wings.android.zuboradiary.databinding.RowProgressBarBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryYearMonthListBaseAdapter.DiaryYearMonthListViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.utils.createLogTag

// TODO:ViewHolderのViewに設定されたリスナを解除するように変更。
internal abstract class DiaryYearMonthListBaseAdapter<
        T : DiaryYearMonthListBaseItem,
        CT : DiaryDayListItem
> protected constructor(
    recyclerView: RecyclerView,
    themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtilItemCallback<T>
) : ListBaseAdapter<T, DiaryYearMonthListViewHolder>(
    recyclerView,
    themeColor,
    diffUtilItemCallback
) {

    private val logTag = createLogTag()

    private var onLayoutChangeListener: OnLayoutChangeListener? = null

    fun interface OnClickChildItemListener<T> {
        fun onClick(item: T)
    }
    protected var onClickChildItemListener: OnClickChildItemListener<CT>? = null

    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("RedundantSuppression")
    // MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
    enum class ViewType @Suppress("unused") constructor(val viewTypeNumber: Int) {
        DIARY(0),
        PROGRESS_INDICATOR(1),
        NO_DIARY_MESSAGE(2)
    }

    override fun build() {
        super.build()

        recyclerView.apply {
            // HACK:下記問題が発生する為アイテムアニメーションを無効化
            //      問題1.アイテム追加時もやがかかる。今回の構成(親Recycler:年月、子Recycler:日)上、
            //           既に表示されている年月に日のアイテムを追加すると、年月のアイテムに変更アニメーションが発生してしまう。
            //           これに対して、日のアイテムに追加アニメーションを発生させようとすると、
            //           年月のアイテムのサイズ変更にアニメーションが発生せず全体的に違和感となるアニメーションになってしまう。
            //      問題2.最終アイテムまで到達し、ProgressBarが消えた後にセクションバーがその分ずれる)
            itemAnimator = null
            setupSectionBar(this)

            addOnScrollListener(
                ListAdditionalLoadOnScrollListener {
                    loadListOnScrollEnd()
                }
            )
        }
    }

    override fun clearViewBindings() {
        super.clearViewBindings()

        recyclerView.apply {
            clearOnScrollListeners()
            removeOnLayoutChangeListener(onLayoutChangeListener)
        }
        onLayoutChangeListener = null
        onClickChildItemListener = null
    }

    override fun createViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater,
        viewType: Int
    ): DiaryYearMonthListViewHolder {
        when(viewType) {
            ViewType.DIARY.viewTypeNumber -> {
                val binding =
                    RowDiaryYearMonthListBinding.inflate(themeColorInflater, parent, false)
                return DiaryYearMonthListViewHolder.Item(binding)
            }

            ViewType.PROGRESS_INDICATOR.viewTypeNumber -> {
                val binding =
                    RowProgressBarBinding.inflate(themeColorInflater, parent, false)
                return DiaryYearMonthListViewHolder.ProgressBar(binding)
            }

            else -> {
                val binding =
                    RowNoDiaryMessageBinding.inflate(themeColorInflater, parent, false)
                return DiaryYearMonthListViewHolder.NoDiaryMessage(binding)
            }
        }
    }

    override fun bindViewHolder(holder: DiaryYearMonthListViewHolder, item: T) {
        holder.bind(item)
        when (holder) {
            is DiaryYearMonthListViewHolder.Item -> {
                createDiaryDayList(holder, item)
            }
            is DiaryYearMonthListViewHolder.NoDiaryMessage,
            is DiaryYearMonthListViewHolder.ProgressBar -> {
                // 処理不要
            }
        }

    }

    abstract fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder.Item,
        item: T
    )

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.viewType.viewTypeNumber
    }

    sealed class DiaryYearMonthListViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(item: DiaryYearMonthListBaseItem)

        data class Item(
            val binding: RowDiaryYearMonthListBinding
        ) : DiaryYearMonthListViewHolder(binding.root) {
            override fun bind(item: DiaryYearMonthListBaseItem) {
                // 対象行の情報を取得
                val diaryYearMonth = item.yearMonth

                // セクションバー設定
                // 左端に余白を持たせる為、最初にスペースを入力。
                val context = binding.root.context
                val diaryDate =
                    ("  " + diaryYearMonth.year + context.getString(R.string.row_diary_year_month_list_section_bar_year)
                            + diaryYearMonth.monthValue + context.getString(R.string.row_diary_year_month_list_section_bar_month))
                binding.textSection.text = diaryDate
                // 日記リストスクロール時に移動させているので、バインディング時に位置リセット
                binding.textSection.y = 0f
            }
        }

        data class NoDiaryMessage(
            val binding: RowNoDiaryMessageBinding
        ) : DiaryYearMonthListViewHolder(binding.root) {
            override fun bind(item: DiaryYearMonthListBaseItem) {
                // 処理なし
            }
        }

        data class ProgressBar(
            val binding: RowProgressBarBinding
        ) : DiaryYearMonthListViewHolder(binding.root) {
            override fun bind(item: DiaryYearMonthListBaseItem) {
                // 処理なし
            }
        }
    }

    protected abstract class DiffUtilItemCallback<T : DiaryYearMonthListBaseItem>
        : DiffUtil.ItemCallback<T>() {

            private val logTag = createLogTag()

            override fun areItemsTheSame(
                oldItem: T,
                newItem: T
            ): Boolean {
                Log.d(
                    logTag,
                    "areItemsTheSame()_oldItem.yearMonth = " + oldItem.yearMonth
                )
                Log.d(
                    logTag,
                    "areItemsTheSame()_newItem.yearMonth = " + newItem.yearMonth
                )

                if (!oldItem.areItemsTheSame(newItem)) {
                    Log.d(logTag, "areItemsTheSame()_不一致")
                    return false
                }

                // HACK:RecyclerViewの初回アイテム表示時にスクロール初期位置がズレる事がある。
                //      原因はプログレスバーの存在。最初にアイテムを表示する時、読込中の意味を込めてプログレスバーのみを表示させている。
                //      スクロール読込機能の仕様により、読込データをRecyclerViewに表示する際、アイテムリスト末尾にプログレスバーを追加している。
                //      これにより、初回読込中プログレスバーとアイテムリスト末尾のプログレスバーが同一アイテムと認識するため、
                //      ListAdapterクラスの仕様により表示されていたプログレスバーが更新後も表示されるようにスクロール位置がズレた。
                //      プログレスバー同士が同一アイテムと認識されないようにするために、下記条件を追加して対策。
                if (oldItem.viewType == ViewType.PROGRESS_INDICATOR) {
                    Log.d(logTag, "areItemsTheSame()_ViewType = ProgressIndicator(不一致)")
                    return false
                }

                Log.d(logTag, "areItemsTheSame()_全項目一致")
                return true
            }
    }

    fun registerOnClickChildItemListener(listener: OnClickChildItemListener<CT>) {
        onClickChildItemListener = listener
    }

    /**
     * RecyclerViewを最終端までスクロールした時にリストアイテムを追加読込するコードを記述すること。
     */
    abstract fun loadListOnScrollEnd()

    // MEMO:読込スクロールをスムーズに処理できるように下記項目を考慮してクラス作成
    //      1. リスト最終アイテムまで見え始める所までスクロールした時に、アイテム追加読込中の目印として最終アイテムの下に
    //         プログレスバーを追加する予定だったが、プログレスバーが追加される前にスクロールしきってしまい、
    //         プログレスバーが表示される前にスクロールが止まってしまうことがあった。
    //         これを解消する為、あらかじめにリスト最終アイテムにプログレスバーを追加するようにして、
    //         読込中はプログレスバーまでスクロールできるようにした。
    //      2. スクロール読込開始条件として、データベースの読込処理状態を監視していたが、読込完了からRecyclerViewへ
    //         反映されるまでの間にタイムラグがあるため、その間にスクロール読込開始条件が揃って読込処理が重複してしまう。
    //         これを解消するために独自のフラグを用意した。
    private class ListAdditionalLoadOnScrollListener(
        val processListAdditionalLoad: () -> Unit
    ) : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy < 0) return // MEMO:RecyclerView更新時にも処理できるように "<=" -> "<" とする。(画面回転対応)

            val layoutManager = checkNotNull(recyclerView.layoutManager) as LinearLayoutManager

            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount
            // MEMO:初回読込時除外のため"<= 1"とする。(ProgressIndicatorViewHolderのみ表示)
            if (totalItemCount <= 1) return

            val lastItemPosition = totalItemCount - 1
            if (lastVisibleItemPosition != lastItemPosition) return

            val recyclerViewAdapter = checkNotNull(recyclerView.adapter)
            val lastItemViewType = recyclerViewAdapter.getItemViewType(lastItemPosition)
            if (lastItemViewType == ViewType.PROGRESS_INDICATOR.viewTypeNumber) {
                processListAdditionalLoad()
            }
        }
    }

    private fun setupSectionBar(recyclerView: RecyclerView) {
        recyclerView.apply {
            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        updateVisibleSectionBarPosition(recyclerView)
                    }
                }
            )
            onLayoutChangeListener =
                OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                    updateVisibleSectionBarPosition(v as RecyclerView)
                }
            addOnLayoutChangeListener(onLayoutChangeListener)
        }
    }

    private fun updateVisibleSectionBarPosition(recyclerView: RecyclerView) {
        updateFirstVisibleSectionBarPosition(recyclerView)
        updateSecondVisibleSectionBarPosition(recyclerView)
    }

    private fun updateFirstVisibleSectionBarPosition(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        // MEMO:RecyclerViewが空の時nullとなる。
        val firstVisibleViewHolder =
            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition) ?: return
        firstVisibleViewHolder as DiaryYearMonthListViewHolder
        when (firstVisibleViewHolder) {
            is DiaryYearMonthListViewHolder.Item -> {
                // 処理継続
            }
            is DiaryYearMonthListViewHolder.NoDiaryMessage,
            is DiaryYearMonthListViewHolder.ProgressBar -> {
                return
            }
        }


        val firstVisibleItemView = checkNotNull(layoutManager.getChildAt(0))
        val secondVisibleItemView = layoutManager.getChildAt(1)
        val textSection = firstVisibleViewHolder.binding.textSection

        val firstVisibleItemViewPositionY = firstVisibleItemView.y
        if (secondVisibleItemView == null) {
            textSection.y = -(firstVisibleItemViewPositionY)
            return
        }

        val sectionHeight = textSection.height
        val secondVisibleItemViewPositionY = secondVisibleItemView.y
        val betweenSectionsMargin =
            firstVisibleViewHolder.binding.recyclerDayList.paddingBottom
        val border = sectionHeight + betweenSectionsMargin
        if (secondVisibleItemViewPositionY >= border) {
            textSection.y = -(firstVisibleItemViewPositionY)
        } else {
            if (secondVisibleItemViewPositionY < betweenSectionsMargin) {
                textSection.y = 0f
            } else if (textSection.y == 0f) {
                textSection.y = -(firstVisibleItemViewPositionY) - sectionHeight
            }
        }
    }

    private fun updateSecondVisibleSectionBarPosition(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val secondVisibleViewHolder =
            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition + 1) ?: return
        secondVisibleViewHolder as DiaryYearMonthListViewHolder
        when (secondVisibleViewHolder) {
            is DiaryYearMonthListViewHolder.Item -> {
                // 処理継続
            }
            is DiaryYearMonthListViewHolder.NoDiaryMessage,
            is DiaryYearMonthListViewHolder.ProgressBar -> {
                return
            }
        }
        secondVisibleViewHolder.binding.textSection.y = 0f // ズレ防止
    }

    fun scrollToTop() {
        Log.d(logTag, "scrollToFirstPosition()")
        // HACK:日記リスト(年月)のアイテム数が多い場合、
        //      ユーザーが数多くのアイテムをスクロールした状態でsmoothScrollToPosition(0)を起動すると先頭にたどり着くのに時間がかかる。
        //      その時間を回避する為に先頭付近へジャンプ(scrollToPosition())してからsmoothScrollToPosition()を起動させたかったが、
        //      エミュレーターでは処理落ちで上手く確認できなかった。(プログラムの可能性もある)
        val firstVisibleItemPosition = findFirstVisibleItemPosition()
        val jumpPosition = findJumpItemPosition(firstVisibleItemPosition)

        if (firstVisibleItemPosition > jumpPosition) {
            recyclerView.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        if (newState != RecyclerView.SCROLL_STATE_IDLE) return

                        recyclerView.removeOnScrollListener(this)
                        scrollToFirstPosition()
                    }
                }
            )
            recyclerView.scrollToPosition(jumpPosition)
        }

        scrollToFirstPosition()
    }

    private fun findFirstVisibleItemPosition(): Int {
        val layoutManager = recyclerView.layoutManager
        val linearLayoutManager = layoutManager as LinearLayoutManager
        return linearLayoutManager.findFirstVisibleItemPosition()
    }

    private fun findJumpItemPosition(firstVisiblePosition: Int): Int {
        if (firstVisiblePosition == 0) return 0

        var numInvisibleChildItems = 0
        var jumpItemPosition = 0
        for (i in 0 until firstVisiblePosition) {
            jumpItemPosition = i + 1
            numInvisibleChildItems += countChildItems(i)
            if (numInvisibleChildItems >= 14) return jumpItemPosition
        }
        return jumpItemPosition
    }

    private fun countChildItems(adapterPosition: Int): Int {
        val item = currentList[adapterPosition]
        return item.diaryDayList.countDiaries()
    }

    private fun scrollToFirstPosition() {
        // MEMO:RecyclerViewの先頭アイテム(年月)の上部が表示されている状態でRecyclerView#.smoothScrollToPosition(0)を呼び出すと、
        //      先頭アイテムの底部が表示されるようにスクロールしてしまう。対策として、下記条件追加。
        val canScrollUp = recyclerView.canScrollVertically(-1)
        if (canScrollUp) recyclerView.smoothScrollToPosition(0)
    }
}
