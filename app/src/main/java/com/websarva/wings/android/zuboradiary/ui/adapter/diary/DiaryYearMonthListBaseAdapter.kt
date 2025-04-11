package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryYearMonthListBinding
import com.websarva.wings.android.zuboradiary.databinding.RowNoDiaryMessageBinding
import com.websarva.wings.android.zuboradiary.databinding.RowProgressBarBinding
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator

// DiaryFragment、WordSearchFragmentの親RecyclerViewのListAdapter。
// 親RecyclerViewを同じ構成にする為、一つのクラスで両方の子RecyclerViewに対応できるように作成。
internal abstract class DiaryYearMonthListBaseAdapter protected constructor(
    protected val context: Context,
    protected val recyclerView: RecyclerView,
    protected val themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtilItemCallback
) : ListAdapter<DiaryYearMonthListBaseItem, RecyclerView.ViewHolder>(diffUtilItemCallback) {

    private val logTag = createLogTag()

    fun interface OnClickChildItemListener {
        fun onClick(item: DiaryDayListBaseItem)
    }
    var onClickChildItemListener: OnClickChildItemListener? = null
    
    protected var isLoadingListOnScrolled = false
        private set

    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("RedundantSuppression")
    // MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
    enum class ViewType @Suppress("unused") constructor(val viewTypeNumber: Int) {
        DIARY(0),
        PROGRESS_INDICATOR(1),
        NO_DIARY_MESSAGE(2)
    }

    open fun build() {
        recyclerView.apply {
            adapter = this@DiaryYearMonthListBaseAdapter
            layoutManager = LinearLayoutManager(context)

            // HACK:下記問題が発生する為アイテムアニメーションを無効化
            //      問題1.アイテム追加時もやがかかる。今回の構成(親Recycler:年月、子Recycler:日)上、
            //           既に表示されている年月に日のアイテムを追加すると、年月のアイテムに変更アニメーションが発生してしまう。
            //           これに対して、日のアイテムに追加アニメーションを発生させようとすると、
            //           年月のアイテムのサイズ変更にアニメーションが発生せず全体的に違和感となるアニメーションになってしまう。
            //      問題2.最終アイテムまで到達し、ProgressBarが消えた後にセクションバーがその分ずれる)
            itemAnimator = null
            addOnScrollListener(SectionBarTranslationOnScrollListener())
            addOnLayoutChangeListener(SectionBarInitializationOnLayoutChangeListener())

            addOnScrollListener(ListAdditionalLoadingOnScrollListener())
        }

        registerAdapterDataObserver(ListLoadingCompleteNotificationAdapterDataObserver())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val creator = ThemeColorInflaterCreator(context, inflater, themeColor)
        val themeColorInflater = creator.create()

        when(viewType) {
            ViewType.DIARY.viewTypeNumber -> {
                val binding =
                    RowDiaryYearMonthListBinding.inflate(themeColorInflater, parent, false)
                val holder = DiaryYearMonthListViewHolder(binding)

                holder.binding
                    .recyclerDayList.apply {
                        // ホルダーアイテムアニメーション設定(build()メソッド内にて理由記載)
                        // MEMO:子RecyclerViewのアニメーションを共通にする為、親Adapterクラス内で実装。
                        itemAnimator = null
                    }

                return holder
            }

            ViewType.PROGRESS_INDICATOR.viewTypeNumber -> {
                val binding =
                    RowProgressBarBinding.inflate(themeColorInflater, parent, false)
                return ProgressBarViewHolder(binding)
            }

            else -> {
                val binding =
                    RowNoDiaryMessageBinding.inflate(themeColorInflater, parent, false)
                return NoDiaryMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DiaryYearMonthListViewHolder) {
            val item = getItem(position)
            holder.apply {
                // 対象行の情報を取得
                val diaryYearMonth = item.yearMonth

                // セクションバー設定
                // 左端に余白を持たせる為、最初にスペースを入力。
                val diaryDate =
                    ("  " + diaryYearMonth.year + context.getString(R.string.row_diary_year_month_list_section_bar_year)
                            + diaryYearMonth.monthValue + context.getString(R.string.row_diary_year_month_list_section_bar_month))
                binding.textSection.text = diaryDate
                // 日記リストスクロール時に移動させているので、バインディング時に位置リセット
                binding.textSection.y = 0f

                // 日記リスト(日)設定
                // MEMO:日記リスト(年月)のLinearLayoutManagerとは併用できないので、
                //      日記リスト(日)用のLinearLayoutManagerをインスタンス化する。
                binding.recyclerDayList.layoutManager = LinearLayoutManager(context)
            }

            createDiaryDayList(holder, item)
        }
    }

    abstract fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder,
        item: DiaryYearMonthListBaseItem
    )

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.viewType.viewTypeNumber
    }

    class DiaryYearMonthListViewHolder(val binding: RowDiaryYearMonthListBinding) :
        RecyclerView.ViewHolder(binding.root)

    class NoDiaryMessageViewHolder(binding: RowNoDiaryMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ProgressBarViewHolder(binding: RowProgressBarBinding) :
        RecyclerView.ViewHolder(binding.root)

    protected abstract class DiffUtilItemCallback
        : DiffUtil.ItemCallback<DiaryYearMonthListBaseItem>() {

            private val logTag = createLogTag()

            override fun areItemsTheSame(
                oldItem: DiaryYearMonthListBaseItem,
                newItem: DiaryYearMonthListBaseItem
            ): Boolean {
                Log.d(
                    logTag,
                    "areItemsTheSame()_oldItem.yearMonth = " + oldItem.yearMonth
                )
                Log.d(
                    logTag,
                    "areItemsTheSame()_newItem.yearMonth = " + newItem.yearMonth
                )

                // ViewType
                if (oldItem.viewType != newItem.viewType) {
                    Log.d(logTag, "areItemsTheSame()_ViewType不一致")
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

                // 年月
                if (oldItem.yearMonth != newItem.yearMonth) {
                    Log.d(logTag, "areItemsTheSame()_YearMonth不一致")
                    return false
                }

                Log.d(logTag, "areItemsTheSame()_全項目一致")
                return true
            }
    }

    /**
     * RecyclerViewを最終端までスクロールした時にリストアイテムを追加読込するコードを記述すること。
     */
    abstract fun loadListOnScrollEnd()

    /**
     * RecyclerViewを最終端までスクロールした時にリストアイテムを追加読込可能か確認するコードを記述すること。
     */
    abstract fun canLoadList(): Boolean

    // MEMO:読込スクロールをスムーズに処理できるように下記項目を考慮してクラス作成
    //      1. リスト最終アイテムまで見え始める所までスクロールした時に、アイテム追加読込中の目印として最終アイテムの下に
    //         プログレスバーを追加する予定だったが、プログレスバーが追加される前にスクロールしきってしまい、
    //         プログレスバーが表示される前にスクロールが止まってしまうことがあった。
    //         これを解消する為、あらかじめにリスト最終アイテムにプログレスバーを追加するようにして、
    //         読込中はプログレスバーまでスクロールできるようにした。
    //      2. スクロール読込開始条件として、データベースの読込処理状態を監視していたが、読込完了からRecyclerViewへ
    //         反映されるまでの間にタイムラグがあるため、その間にスクロール読込開始条件が揃って読込処理が重複してしまう。
    //         これを解消するために独自のフラグを用意した。
    private inner class ListAdditionalLoadingOnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (isLoadingListOnScrolled) return
            if (!canLoadList()) return
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
                loadListOnScrollEnd()
                isLoadingListOnScrolled = true
            }
        }
    }

    private inner class ListLoadingCompleteNotificationAdapterDataObserver : AdapterDataObserver() {
        override fun onChanged() {
            Log.d(logTag, "OnChanged()")
            super.onChanged()
            clearIsLoadingListOnScrolled()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            Log.d(logTag, "onItemRangeChanged()")
            super.onItemRangeChanged(positionStart, itemCount)
            clearIsLoadingListOnScrolled()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            Log.d(logTag, "onItemRangeChanged()")
            super.onItemRangeChanged(positionStart, itemCount, payload)
            clearIsLoadingListOnScrolled()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            Log.d(logTag, "onItemRangeInserted()")
            super.onItemRangeInserted(positionStart, itemCount)
            clearIsLoadingListOnScrolled()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            Log.d(logTag, "onItemRangeRemoved()")
            super.onItemRangeRemoved(positionStart, itemCount)
            clearIsLoadingListOnScrolled()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            Log.d(logTag, "onItemRangeMoved()")
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            clearIsLoadingListOnScrolled()
        }

        override fun onStateRestorationPolicyChanged() {
            Log.d(logTag, "onStateRestorationPolicyChanged()")
            super.onStateRestorationPolicyChanged()
        }
    }

    private fun clearIsLoadingListOnScrolled() {
        if (itemCount == 0) return
        if (getItemViewType(0) != ViewType.PROGRESS_INDICATOR.viewTypeNumber) {
            isLoadingListOnScrolled = false
        }
    }

    private inner class SectionBarTranslationOnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            updateVisibleSectionBarPosition()
        }
    }

    private inner class SectionBarInitializationOnLayoutChangeListener : OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View, left: Int, top: Int, right: Int, bottom: Int,
            oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
        ) {
            updateVisibleSectionBarPosition()
        }
    }

    private fun updateVisibleSectionBarPosition() {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        layoutManager.apply {
            updateFirstVisibleSectionBarPosition(this)
            updateSecondVisibleSectionBarPosition(this)
        }
    }

    private fun updateFirstVisibleSectionBarPosition(layoutManager: LinearLayoutManager) {

        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        // MEMO:RecyclerViewが空の時nullとなる。
        val firstVisibleViewHolder =
            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition) ?: return
        if (firstVisibleViewHolder !is DiaryYearMonthListViewHolder) return

        firstVisibleViewHolder.apply {
            val firstVisibleItemView = checkNotNull(layoutManager.getChildAt(0))
            val secondVisibleItemView = layoutManager.getChildAt(1)

            val firstVisibleItemViewPositionY = firstVisibleItemView.y
            if (secondVisibleItemView == null) {
                binding.textSection.y = -(firstVisibleItemViewPositionY)
                return
            }

            val sectionHeight = binding.textSection.height
            val secondVisibleItemViewPositionY = secondVisibleItemView.y
            val betweenSectionsMargin = binding.recyclerDayList.paddingBottom
            val border = sectionHeight + betweenSectionsMargin
            if (secondVisibleItemViewPositionY >= border) {
                binding.textSection.y = -(firstVisibleItemViewPositionY)
            } else {
                if (secondVisibleItemViewPositionY < betweenSectionsMargin) {
                    binding.textSection.y = 0f
                } else if (binding.textSection.y == 0f) {
                    binding.textSection.y = -(firstVisibleItemViewPositionY) - sectionHeight
                }
            }
        }


    }

    private fun updateSecondVisibleSectionBarPosition(layoutManager: LinearLayoutManager) {
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val secondVisibleViewHolder =
            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition + 1) ?: return
        if (secondVisibleViewHolder !is DiaryYearMonthListViewHolder) return
        secondVisibleViewHolder.binding.textSection.y = 0f // ズレ防止
    }

    fun scrollToFirstPosition() {
        Log.d(logTag, "scrollToFirstPosition()")
        // HACK:日記リスト(年月)のアイテム数が多い場合、
        //      ユーザーが数多くのアイテムをスクロールした状態でsmoothScrollToPosition(0)を起動すると先頭にたどり着くのに時間がかかる。
        //      その時間を回避する為に先頭付近へジャンプ(scrollToPosition())してからsmoothScrollToPosition()を起動させたかったが、
        //      エミュレーターでは処理落ちで上手く確認できなかった。(プログラムの可能性もある)
        val layoutManager = recyclerView.layoutManager
        val linearLayoutManager = layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
        val jumpPosition = 2
        if (firstVisibleItemPosition >= jumpPosition) {
            recyclerView.scrollToPosition(jumpPosition)
        }

        // MEMO:RecyclerViewの先頭アイテム(年月)の上部が表示されている状態でRecyclerView#.smoothScrollToPosition(0)を呼び出すと、
        //      先頭アイテムの底部が表示されるようにスクロールしてしまう。対策として、下記条件追加。
        val canScrollUp = recyclerView.canScrollVertically(-1)
        if (canScrollUp) recyclerView.smoothScrollToPosition(0)
    }
}
