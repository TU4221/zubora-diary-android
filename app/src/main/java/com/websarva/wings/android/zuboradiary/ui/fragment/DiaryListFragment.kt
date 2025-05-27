package com.websarva.wings.android.zuboradiary.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding
import com.websarva.wings.android.zuboradiary.ui.permission.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter.OnClickChildItemListener
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.SwipeDiaryYearMonthListBaseAdapter.OnClickChildItemBackgroundButtonListener
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryListDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryListViewModel
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryYearMonthListAdapter
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.StartYearMonthPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryListState
import com.websarva.wings.android.zuboradiary.ui.model.action.DiaryListFragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

@AndroidEntryPoint
class DiaryListFragment : BaseFragment() {
    // View関係
    private var _binding: FragmentDiaryListBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryListViewModel by activityViewModels()

    // Uri関係
    private lateinit var pictureUriPermissionManager: UriPermissionManager

    // RecyclerView関係
    // HACK:RecyclerViewのAdapterにセットするListを全て変更した時、
    //      変更前のListの内容で初期スクロール位置が定まらない不具合が発生。
    //      対策としてListを全て変更するタイミングでAdapterを新規でセットする。
    //      (親子関係でRecyclerViewを使用、又はListAdapterの機能による弊害？)
    private var shouldInitializeListAdapter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pictureUriPermissionManager =
            object : UriPermissionManager() {
                override suspend fun checkUsedUriDoesNotExist(uri: Uri): Boolean? {
                    return mainViewModel.checkSavedPicturePathDoesNotExist(uri)
                }
            }
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentDiaryListBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@DiaryListFragment.viewLifecycleOwner
            listViewModel = mainViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragmentAction()
        setUpToolBar()
        setUpDiaryList()

        mainViewModel.onFragmentViewCreated()
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // 処理なし
    }

    override fun receiveDialogResults() {
        receiveDatePickerDialogResults()
        receiveDiaryDeleteDialogResults()
    }

    override fun removeDialogResults() {
        removeResulFromFragment(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH)
        removeResulFromFragment(DiaryListDeleteDialogFragment.KEY_DELETE_DIARY_DATE)
    }

    // 日付入力ダイアログフラグメントから結果受取
    private fun receiveDatePickerDialogResults() {
        val selectedYearMonth =
            receiveResulFromDialog<YearMonth>(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH)
                ?: return

        shouldInitializeListAdapter = true
        mainViewModel.onDataReceivedFromDatePickerDialog(selectedYearMonth)
    }

    // 日記削除ダイアログフラグメントから結果受取
    private fun receiveDiaryDeleteDialogResults() {
        val deleteDiaryDate =
            receiveResulFromDialog<LocalDate>(DiaryListDeleteDialogFragment.KEY_DELETE_DIARY_DATE)
                ?: return
        val deleteDiaryPictureUri =
            receiveResulFromDialog<Uri>(DiaryListDeleteDialogFragment.KEY_DELETE_DIARY_PICTURE_URI)

        mainViewModel.onDataReceivedFromDiaryDeleteDialog(deleteDiaryDate, deleteDiaryPictureUri)
    }

    private fun setUpFragmentAction() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.fragmentAction.collect { value: FragmentAction ->
                when (value) {
                    is DiaryListFragmentAction.NavigateDiaryShowFragment -> {
                        navigateDiaryShowFragment(value.date)
                    }
                    is DiaryListFragmentAction.NavigateDiaryEditFragment -> {
                        navigateDiaryEditFragment()
                    }
                    is DiaryListFragmentAction.NavigateWordSearchFragment -> {
                        navigateWordSearchFragment()
                    }
                    is DiaryListFragmentAction.NavigateStartYearMonthPickerDialog -> {
                        navigateStartYearMonthPickerDialog(value.newestYear, value.oldestYear)
                    }
                    is DiaryListFragmentAction.NavigateDiaryDeleteDialog -> {
                        navigateDiaryDeleteDialog(value.date, value.uri)
                    }
                    is DiaryListFragmentAction.ReleasePersistablePermissionUri -> {
                        pictureUriPermissionManager
                            .releasePersistablePermission(requireContext(), value.uri)
                    }
                    FragmentAction.NavigatePreviousFragment -> {
                        navController.navigateUp()
                    }
                    else -> {
                        throw IllegalArgumentException()
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
            mainViewModel.diaryListState
                .collectLatest { value: DiaryListState ->
                    val isEnabled =
                        value != DiaryListState.AdditionLoading
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
                    mainViewModel.onDiaryListItemDeleteButtonClicked(item.date, item.picturePath)
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
                setUpListAdapter()
            }

            val convertedItemList: List<DiaryYearMonthListBaseItem> = list.itemList
            val listAdapter = binding.recyclerDiaryList.adapter as DiaryYearMonthListAdapter
            listAdapter.submitList(convertedItemList) {
                mainViewModel.onDiaryListUpdated()
            }
        }
    }

    private fun navigateDiaryEditFragment() {
        if (!canNavigateFragment) return

        val directions =
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToDiaryEditFragment(
                true,
                false,
                LocalDate.now()
            )
        navController.navigate(directions)
    }

    private fun navigateDiaryShowFragment(date: LocalDate) {
        if (!canNavigateFragment) return

        val directions =
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToDiaryShowFragment(date)
        navController.navigate(directions)
    }

    private fun navigateWordSearchFragment() {
        if (!canNavigateFragment) return

        val directions =
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToWordSearchFragment()
        navController.navigate(directions)
    }

    private fun navigateStartYearMonthPickerDialog(newestYear: Year, oldestYear: Year) {
        if (!canNavigateFragment) return

        val directions =
            DiaryListFragmentDirections.actionDiaryListFragmentToStartYearMonthPickerDialog(
                newestYear,
                oldestYear
            )
        navController.navigate(directions)
    }

    private fun navigateDiaryDeleteDialog(date: LocalDate, pictureUri: Uri?) {
        if (!canNavigateFragment) return

        val directions =
            DiaryListFragmentDirections.actionDiaryListFragmentToDiaryDeleteDialog(date, pictureUri)
        navController.navigate(directions)
    }

    override fun onNavigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryListFragmentDirections.actionDiaryListFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    internal fun onNavigationItemReselected() {
        scrollDiaryListToFirstPosition()
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    private fun scrollDiaryListToFirstPosition() {
        val listAdapter = binding.recyclerDiaryList.adapter as DiaryYearMonthListAdapter
        listAdapter.scrollToTop()
    }

    override fun destroyBinding() {
        _binding = null
    }
}
