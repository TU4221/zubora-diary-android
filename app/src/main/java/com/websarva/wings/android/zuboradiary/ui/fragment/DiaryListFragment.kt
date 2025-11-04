package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryListDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryListViewModel
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.diary.DiaryYearMonthListAdapter
import com.websarva.wings.android.zuboradiary.ui.fragment.common.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet.StartYearMonthPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Year

@AndroidEntryPoint
class DiaryListFragment :
    BaseFragment<FragmentDiaryListBinding, DiaryListUiEvent>(),
    RequiresBottomNavigation {

    override val destinationId = R.id.navigation_diary_list_fragment

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryListViewModel by viewModels()

    private lateinit var diaryListAdapter: DiaryYearMonthListAdapter

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

        observeUiEventFromActivity()
        observeUiState()
        setUpToolBar()
        setUpDiaryList()

        mainViewModel.onUiReady()
    }

    override fun initializeFragmentResultReceiver() {
        setUpDatePickerDialogResultReceiver()
        setUpDiaryDeleteDialogResultReceiver()
    }

    // 日付入力ダイアログフラグメントから結果受取
    private fun setUpDatePickerDialogResultReceiver() {
        setUpDialogResultReceiver(
            StartYearMonthPickerDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDatePickerDialogResultReceived(result)
        }
    }

    // 日記削除ダイアログフラグメントから結果受取
    private fun setUpDiaryDeleteDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryListDeleteDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryDeleteDialogResultReceived(result)
        }
    }

    override fun onMainUiEventReceived(event: DiaryListUiEvent) {
        when (event) {
            is DiaryListUiEvent.NavigateDiaryShowFragment -> {
                navigateDiaryShowFragment(event.id, event.date)
            }
            is DiaryListUiEvent.NavigateDiaryEditFragment -> {
                navigateDiaryEditFragment(event.id, event.date)
            }
            is DiaryListUiEvent.NavigateWordSearchFragment -> {
                navigateWordSearchFragment()
            }
            is DiaryListUiEvent.NavigateStartYearMonthPickerDialog -> {
                navigateStartYearMonthPickerDialog(event.newestYear, event.oldestYear)
            }
            is DiaryListUiEvent.NavigateDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.date)
            }
        }
    }

    override fun onNavigatePreviousFragmentEventReceived(result: FragmentResult<*>) {
        navigatePreviousFragmentOnce()
    }

    override fun onNavigateAppMessageEventReceived(appMessage: AppMessage) {
        navigateAppMessageDialog(appMessage)
    }

    private fun observeUiEventFromActivity() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainActivityViewModel.activityCallbackUiEvent
                .collect { value: ConsumableEvent<ActivityCallbackUiEvent> ->
                    val event = value.getContentIfNotHandled()
                    event ?: return@collect
                    when (event) {
                        ActivityCallbackUiEvent.ProcessOnBottomNavigationItemReselect -> {
                            scrollDiaryListToFirstPosition()
                        }
                    }
                }
        }
    }

    private fun observeUiState() {
        observeDiaryListItem()
        observeDiaryListSwipeEnabled()
    }

    private fun observeDiaryListItem() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.diaryList == new.diaryList
            }.map {
                it.diaryList
            }.collect {
                updateDiaryList(it)
            }
        }
    }

    private fun observeDiaryListSwipeEnabled() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.isLoadingOnScrolled.map { !it }.collect {
                updateDiaryListSwipeEnabled(it)
            }
        }
    }

    private fun updateDiaryList(diaryList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>) {
        diaryListAdapter.submitList(diaryList.itemList) {
            mainViewModel.onDiaryListUpdateCompleted()
        }
    }

    private fun updateDiaryListSwipeEnabled(isSwipeEnabled: Boolean) {
        diaryListAdapter.setSwipeEnabled(isSwipeEnabled)
    }

    // ツールバー設定
    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setOnMenuItemClickListener { item: MenuItem ->
                // ワード検索フラグメント起動
                if (item.itemId == R.id.diaryListToolbarOptionWordSearch) {
                    mainViewModel.onWordSearchMenuClick()
                    return@setOnMenuItemClickListener true
                }
                false
            }
    }

    // 日記リスト(年月)設定
    private fun setUpDiaryList() {
        diaryListAdapter =
            object : DiaryYearMonthListAdapter(binding.recyclerDiaryList, themeColor) {
                override fun loadListOnScrollEnd() {
                    mainViewModel.onDiaryListEndScrolled()
                }
            }.apply {
                build()
                registerOnChildItemClickListener { item: DiaryDayListItemUi.Standard ->
                    mainViewModel.onDiaryListItemClick(item)
                }
                registerOnChildItemBackgroundButtonClickListener { item: DiaryDayListItemUi.Standard ->
                    mainViewModel.onDiaryListItemDeleteButtonClick(item)
                }
            }
    }

    private fun navigateDiaryEditFragment(id: String?, date: LocalDate) {
        Log.d("20250714", "navigateDiaryEditFragment()")
        val directions =
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToDiaryEditFragment(
                id,
                date
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryShowFragment(id: String, date: LocalDate) {
        Log.d("20250714", "navigateDiaryShowFragment()")
        val directions =
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToDiaryShowFragment(id, date)
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

    private fun navigateDiaryDeleteDialog(date: LocalDate) {
        Log.d("20250714", "navigateDiaryDeleteDialog")
        val directions =
            DiaryListFragmentDirections.actionDiaryListFragmentToDiaryDeleteDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryListFragmentDirections.actionDiaryListFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }

    private fun scrollDiaryListToFirstPosition() {
        val listAdapter = binding.recyclerDiaryList.adapter as DiaryYearMonthListAdapter
        listAdapter.scrollToTop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mainViewModel.onUiGone()
    }
}
