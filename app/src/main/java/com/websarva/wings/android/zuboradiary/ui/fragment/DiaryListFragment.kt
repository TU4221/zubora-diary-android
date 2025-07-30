package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter.OnClickChildItemListener
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.SwipeDiaryYearMonthListBaseAdapter.OnClickChildItemBackgroundButtonListener
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryListDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryListViewModel
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryYearMonthListAdapter
import com.websarva.wings.android.zuboradiary.ui.fragment.common.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.fragment.common.ReselectableFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.StartYearMonthPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryListState
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

@AndroidEntryPoint
class DiaryListFragment :
    BaseFragment<FragmentDiaryListBinding, DiaryListEvent>(),
    ReselectableFragment,
    RequiresBottomNavigation {

    override val destinationId = R.id.navigation_diary_list_fragment

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryListViewModel by viewModels()

    // RecyclerView関係
    // HACK:RecyclerViewのAdapterにセットするListを全て変更した時、
    //      変更前のListの内容で初期スクロール位置が定まらない不具合が発生。
    //      対策としてListを全て変更するタイミングでAdapterを新規でセットする。
    //      (親子関係でRecyclerViewを使用、又はListAdapterの機能による弊害？)
    // TODO:下記変数による処理を無効化しても上記不具合の確認ができない為、開発最後に必要か判断
    private var shouldInitializeListAdapter = false

    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentDiaryListBinding {
        return FragmentDiaryListBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolBar()
        setUpDiaryList()

        mainViewModel.onFragmentViewCreated()
    }

    override fun initializeFragmentResultReceiver() {
        setUpDatePickerDialogResultReceiver()
        setUpDiaryDeleteDialogResultReceiver()
    }

    // 日付入力ダイアログフラグメントから結果受取
    private fun setUpDatePickerDialogResultReceiver() {
        setUpDialogResultReceiver(
            StartYearMonthPickerDialogFragment.KEY_RESULT
        ) { result ->
            Log.d("20250714", "DatePickerDialogResultReceive")
            // TODO:シールドクラス Action -> Event に変更してから下記コードの処理方法を検討する。
            //      shouldInitializeListAdapterの必要性がないかもしれない。(変数宣言元のコメント参照)
            when (result) {
                is DialogResult.Positive<YearMonth> -> {
                    shouldInitializeListAdapter = true
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    // 処理なし
                }
            }

            mainViewModel.onDatePickerDialogResultReceived(result)
        }
    }

    // 日記削除ダイアログフラグメントから結果受取
    private fun setUpDiaryDeleteDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryListDeleteDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryDeleteDialogResultReceived(result)
        }
    }

    override fun onMainUiEventReceived(event: DiaryListEvent) {
        when (event) {
            is DiaryListEvent.NavigateDiaryShowFragment -> {
                navigateDiaryShowFragment(event.date)
            }
            is DiaryListEvent.NavigateDiaryEditFragment -> {
                navigateDiaryEditFragment()
            }
            is DiaryListEvent.NavigateWordSearchFragment -> {
                navigateWordSearchFragment()
            }
            is DiaryListEvent.NavigateStartYearMonthPickerDialog -> {
                navigateStartYearMonthPickerDialog(event.newestYear, event.oldestYear)
            }
            is DiaryListEvent.NavigateDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.parameters)
            }
            is DiaryListEvent.CommonEvent -> {
                when(event.wrappedEvent) {
                    is CommonUiEvent.NavigatePreviousFragment<*> -> {
                        navigatePreviousFragment()
                    }
                    is CommonUiEvent.NavigateAppMessage -> {
                        navigateAppMessageDialog(event.wrappedEvent.message)
                    }
                }
            }
        }
    }

    // ツールバー設定
    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setOnMenuItemClickListener { item: MenuItem ->
                // ワード検索フラグメント起動
                if (item.itemId == R.id.diaryListToolbarOptionWordSearch) {
                    mainViewModel.onWordSearchMenuClicked()
                    return@setOnMenuItemClickListener true
                }
                false
            }
    }

    // 日記リスト(年月)設定
    private fun setUpDiaryList() {
        val diaryListAdapter = setUpListAdapter()


        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.diaryList
                .collectLatest { value: DiaryYearMonthList ->
                    DiaryListObserver().onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState
                .collectLatest { value ->
                    val isEnabled =
                        value != DiaryListState.LoadingAdditionDiaryList
                    diaryListAdapter.setSwipeEnabled(isEnabled)
                }
        }
    }

    private fun setUpListAdapter(): DiaryListAdapter {
        val diaryListAdapter =
            DiaryListAdapter(
                binding.recyclerDiaryList,
                themeColor
            )
        diaryListAdapter.apply {
            build()
            onClickChildItemListener =
                OnClickChildItemListener { item: DiaryDayListBaseItem ->
                    mainViewModel.onDiaryListItemClicked(item.date)
                }
            onClickChildItemBackgroundButtonListener =
                OnClickChildItemBackgroundButtonListener { item: DiaryDayListBaseItem ->
                    if (item !is DiaryDayListItem) throw IllegalStateException()
                    mainViewModel.onDiaryListItemDeleteButtonClicked(item.date, item.imageUri)
                }
        }

        return diaryListAdapter
    }

    private inner class DiaryListAdapter(
        recyclerView: RecyclerView,
        themeColor: ThemeColor
    ) : DiaryYearMonthListAdapter(recyclerView, themeColor) {

        override fun loadListOnScrollEnd() {
            mainViewModel.onDiaryListEndScrolled()
        }
    }

    private inner class DiaryListObserver {
        fun onChanged(value: DiaryYearMonthList) {
            setUpList(value)
        }

        private fun setUpList(list: DiaryYearMonthList) {

            if (shouldInitializeListAdapter) {
                shouldInitializeListAdapter = false
                //setUpListAdapter()
            }

            val convertedItemList: List<DiaryYearMonthListBaseItem> = list.itemList
            val listAdapter = binding.recyclerDiaryList.adapter as DiaryYearMonthListAdapter
            listAdapter.submitList(convertedItemList) {
                mainViewModel.onDiaryListUpdated()
            }
        }
    }

    private fun navigateDiaryEditFragment() {
        Log.d("20250714", "navigateDiaryEditFragment()")
        val directions =
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToDiaryEditFragment(
                false,
                LocalDate.now()
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryShowFragment(date: LocalDate) {
        Log.d("20250714", "navigateDiaryShowFragment()")
        val directions =
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToDiaryShowFragment(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateWordSearchFragment() {
        Log.d("20250714", "navigateWordSearchFragment()")
        val directions =
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToWordSearchFragment()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateStartYearMonthPickerDialog(newestYear: Year, oldestYear: Year) {
        Log.d("20250714", "navigateStartYearMonthPickerDialog()")
        val directions =
            DiaryListFragmentDirections.actionDiaryListFragmentToStartYearMonthPickerDialog(
                newestYear,
                oldestYear
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryDeleteDialog(parameters: DiaryDeleteParameters) {
        Log.d("20250714", "navigateDiaryDeleteDialog")
        val directions =
            DiaryListFragmentDirections.actionDiaryListFragmentToDiaryDeleteDialog(parameters)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryListFragmentDirections.actionDiaryListFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }

    override fun onBottomNavigationItemReselected() {
        scrollDiaryListToFirstPosition()
    }

    private fun scrollDiaryListToFirstPosition() {
        val listAdapter = binding.recyclerDiaryList.adapter as DiaryYearMonthListAdapter
        listAdapter.scrollToTop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.onFragmentDestroyView()
    }
}
