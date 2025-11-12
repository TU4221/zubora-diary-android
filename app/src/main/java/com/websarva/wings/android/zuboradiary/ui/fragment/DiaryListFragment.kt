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
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.StandardDiaryListAdapter
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryListDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryListViewModel
import com.websarva.wings.android.zuboradiary.ui.fragment.common.ActivityCallbackUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.recyclerview.helper.DiaryListSetupHelper
import com.websarva.wings.android.zuboradiary.ui.fragment.common.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.recyclerview.helper.SwipeBackgroundButtonInteractionHelper
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet.StartYearMonthPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListUi
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Year

@AndroidEntryPoint
class DiaryListFragment :
    BaseFragment<FragmentDiaryListBinding, DiaryListUiEvent>(),
    RequiresBottomNavigation,
    ActivityCallbackUiEventHandler {
        
    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryListViewModel by viewModels()
    
    override val destinationId = R.id.navigation_diary_list_fragment

    private lateinit var diaryListAdapter: StandardDiaryListAdapter

    private var diaryListSetupHelper: DiaryListSetupHelper? = null

    private var swipeBackgroundButtonInteractionHelper: SwipeBackgroundButtonInteractionHelper? = null
    //endregion

    //region Fragment Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar()
        setUpDiaryList()

        mainViewModel.onUiReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mainViewModel.onUiGone()

        diaryListSetupHelper?.cleanup()
        diaryListSetupHelper = null

        swipeBackgroundButtonInteractionHelper?.cleanup()
        swipeBackgroundButtonInteractionHelper = null

    }
    //endregion
    
    //region View Binding Setup
    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentDiaryListBinding {
        return FragmentDiaryListBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }
    //endregion

    //region Fragment Result Observation Setup
    override fun setUpFragmentResultObservers() {
        observeDatePickerDialogResult()
        observeDiaryDeleteDialogResult()
    }

    // 日付入力ダイアログフラグメントから結果受取
    private fun observeDatePickerDialogResult() {
        observeDialogResult(
            StartYearMonthPickerDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDatePickerDialogResultReceived(result)
        }
    }

    // 日記削除ダイアログフラグメントから結果受取
    private fun observeDiaryDeleteDialogResult() {
        observeDialogResult(
            DiaryListDeleteDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryDeleteDialogResultReceived(result)
        }
    }
    //endregion

    //region UI Observation Setup
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

    override fun onCommonUiEventReceived(event: CommonUiEvent) {
        when (event) {
            is CommonUiEvent.NavigatePreviousFragment<*> -> {
                navigatePreviousFragmentOnce()
            }
            is CommonUiEvent.NavigateAppMessage -> {
                navigateAppMessageDialog(event.message)
            }
        }
    }

    override fun onActivityCallbackUiEventReceived(event: ActivityCallbackUiEvent) {
        when (event) {
            ActivityCallbackUiEvent.ProcessOnBottomNavigationItemReselect -> {
                scrollDiaryListToFirstPosition()
            }
        }
    }

    override fun setUpUiStateObservers() {
        super.setUpUiStateObservers()

        observeDiaryListItem()
        observeDiaryListSwipeEnabled()
    }

    override fun setUpUiEventObservers() {
        super.setUpUiEventObservers()

        observeUiEventFromActivity()
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

    private fun observeUiEventFromActivity() {
        fragmentHelper.observeActivityUiEvent(
            this,
            mainActivityViewModel,
            this
        )
    }
    //endregion

    //region View Setup
    private fun setUpToolbar() {
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

    private fun setUpDiaryList() {
        val diaryRecyclerView = binding.recyclerDiaryList
        diaryListAdapter = StandardDiaryListAdapter(
            themeColor,
            { mainViewModel.onDiaryListItemClick(it) },
            { mainViewModel.onDiaryListItemDeleteButtonClick(it) }
        )

        diaryListSetupHelper =
            DiaryListSetupHelper(
                diaryRecyclerView,
                diaryListAdapter
            ) {
                mainViewModel.onDiaryListEndScrolled()
            }.also { it.setup() }

        swipeBackgroundButtonInteractionHelper =
            SwipeBackgroundButtonInteractionHelper(
                diaryRecyclerView,
                diaryListAdapter
            ).also { it.setup() }
    }
    //endregion

    //region View Manipulation
    private fun updateDiaryList(diaryList: DiaryListUi<DiaryListItemContainerUi.Standard>) {
        diaryListAdapter.submitList(diaryList.itemList) {
            mainViewModel.onDiaryListUpdateCompleted()
        }
    }

    private fun updateDiaryListSwipeEnabled(isSwipeEnabled: Boolean) {
        swipeBackgroundButtonInteractionHelper?.updateItemSwipeEnabled(isSwipeEnabled)
    }

    private fun scrollDiaryListToFirstPosition() {
        binding.recyclerDiaryList.smoothScrollToPosition(0)
    }
    //endregion

    //region Navigation Helpers
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
    //endregion
}
