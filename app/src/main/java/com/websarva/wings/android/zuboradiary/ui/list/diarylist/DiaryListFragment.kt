package com.websarva.wings.android.zuboradiary.ui.list.diarylist

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter.OnClickChildItemListener
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.list.SwipeDiaryYearMonthListBaseAdapter.OnClickChildItemBackgroundButtonListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val diaryListViewModel: DiaryListViewModel by activityViewModels()

    // Uri関係
    private lateinit var pictureUriPermissionManager: UriPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pictureUriPermissionManager =
            object : UriPermissionManager(requireContext()) {
                override suspend fun checkUsedUriDoesNotExist(uri: Uri): Boolean? {
                    return diaryListViewModel.checkSavedPicturePathDoesNotExist(uri)
                }
            }
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentDiaryListBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@DiaryListFragment.viewLifecycleOwner
            listViewModel = this@DiaryListFragment.diaryListViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolBar()
        setUpFloatActionButton()
        setUpDiaryList()
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
        removeResulFromFragment(DiaryDeleteDialogFragment.KEY_DELETE_DIARY_DATE)
    }

    override fun setUpOtherAppMessageDialog() {
        launchAndRepeatOnViewLifeCycleStarted {
            diaryListViewModel.appMessageBufferList
                .collectLatest { value: AppMessageList ->
                    AppMessageBufferListObserver(diaryListViewModel).onChanged(value)
                }
        }
    }

    // 日付入力ダイアログフラグメントから結果受取
    private fun receiveDatePickerDialogResults() {
        val selectedYearMonth =
            receiveResulFromDialog<YearMonth>(StartYearMonthPickerDialogFragment.KEY_SELECTED_YEAR_MONTH)
                ?: return

        diaryListViewModel.updateSortConditionDate(selectedYearMonth)
        scrollDiaryListToFirstPosition()
        diaryListViewModel.loadNewDiaryList()
    }

    // 日記削除ダイアログフラグメントから結果受取
    private fun receiveDiaryDeleteDialogResults() {
        val deleteDiaryDate =
            receiveResulFromDialog<LocalDate>(DiaryDeleteDialogFragment.KEY_DELETE_DIARY_DATE)
                ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = diaryListViewModel.deleteDiary(deleteDiaryDate)
            if (!isSuccessful) return@launch

            val deleteDiaryPictureUri =
                receiveResulFromDialog<Uri>(DiaryDeleteDialogFragment.KEY_DELETE_DIARY_PICTURE_URI)
                    ?: return@launch
            pictureUriPermissionManager.releasePersistablePermission(deleteDiaryPictureUri)
        }
    }

    // ツールバー設定
    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    // リスト先頭年月切り替えダイアログ起動
                    val newestDiaryDate = diaryListViewModel.loadNewestSavedDiaryDate()
                    val oldestDiaryDate = diaryListViewModel.loadOldestSavedDiaryDate()
                    if (newestDiaryDate == null) return@launch
                    if (oldestDiaryDate == null) return@launch

                    val newestYear = Year.of(newestDiaryDate.year)
                    val oldestYear = Year.of(oldestDiaryDate.year)
                    withContext(Dispatchers.Main) {
                        showStartYearMonthPickerDialog(newestYear, oldestYear)
                    }
                }
            }

        binding.materialToolbarTopAppBar
            .setOnMenuItemClickListener { item: MenuItem ->
                // ワード検索フラグメント起動
                if (item.itemId == R.id.diaryListToolbarOptionWordSearch) {
                    showWordSearchFragment()
                    return@setOnMenuItemClickListener true
                }
                false
            }
    }

    // 新規作成FAB設定
    private fun setUpFloatActionButton() {
        binding.floatingActionButtonDiaryEdit.setOnClickListener {
            showEditDiary()
        }
    }

    // 日記リスト(年月)設定
    private fun setUpDiaryList() {
        binding.floatingActionButtonDiaryEdit.isEnabled = true
        val diaryListAdapter =
            DiaryListAdapter(
                requireContext(),
                binding.recyclerDiaryList,
                themeColor
            )
        diaryListAdapter.apply {
            build()
            onClickChildItemListener =
                OnClickChildItemListener { item: DiaryDayListBaseItem ->
                    showShowDiaryFragment(item.date)
                }
            onClickChildItemBackgroundButtonListener =
                OnClickChildItemBackgroundButtonListener { item: DiaryDayListBaseItem ->
                    if (item !is DiaryDayListItem) throw IllegalStateException()
                    showDiaryDeleteDialog(item.date, item.picturePath)
                }
            registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {

                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        diaryListViewModel.clearIsLoadingDiaryList()
                    }
                }
            )
        }


        launchAndRepeatOnViewLifeCycleStarted {
            diaryListViewModel.diaryList
                .collectLatest { value: DiaryYearMonthList ->
                    DiaryListObserver().onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryListViewModel.isLoadingDiaryList
                .collectLatest { value: Boolean ->
                    diaryListAdapter.setSwipeEnabled(!value)
                }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            diaryListViewModel.loadDiaryListOnSetUp()
        }

        navController.addOnDestinationChangedListener(DiaryListUpdateSetupListener())
    }

    private inner class DiaryListAdapter(
        context: Context,
        recyclerView: RecyclerView,
        themeColor: ThemeColor
    ) : DiaryYearMonthListAdapter(context, recyclerView, themeColor) {

        override fun loadListOnScrollEnd() {
            diaryListViewModel.loadAdditionDiaryList()
        }

        override fun canLoadList(): Boolean {
            return diaryListViewModel.canLoadDiaryList
        }
    }

    private inner class DiaryListObserver {
        fun onChanged(value: DiaryYearMonthList) {
            setUpListViewVisibility(value)
            setUpList(value)
        }

        private fun setUpListViewVisibility(list: DiaryYearMonthList) {
            val isNoDiary = list.diaryYearMonthListItemList.isEmpty()
            val noDiaryMessageVisibility: Int
            val diaryListVisibility: Int
            if (isNoDiary) {
                noDiaryMessageVisibility = View.VISIBLE
                diaryListVisibility = View.INVISIBLE
            } else {
                noDiaryMessageVisibility = View.INVISIBLE
                diaryListVisibility = View.VISIBLE
            }
            binding.apply {
                textNoDiaryMessage.visibility = noDiaryMessageVisibility
                recyclerDiaryList.visibility = diaryListVisibility
            }
        }

        private fun setUpList(list: DiaryYearMonthList) {
            val convertedItemList: List<DiaryYearMonthListBaseItem> = list.diaryYearMonthListItemList
            val listAdapter = binding.recyclerDiaryList.adapter as DiaryYearMonthListAdapter
            listAdapter.submitList(convertedItemList)
        }
    }

    private inner class DiaryListUpdateSetupListener : NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            // MEMO:本Fragment、Dialog以外のFragmentへ切り替わった時のみDiaryListを更新する。
            if (destination.id == R.id.navigation_diary_list_fragment
                || destination.id == R.id.navigation_start_year_month_picker_dialog) return

            diaryListViewModel.shouldUpdateDiaryList = true
            navController.removeOnDestinationChangedListener(this)
        }
    }

    @MainThread
    private fun showEditDiary() {
        if (isDialogShowing) return

        val directions =
            DiaryListFragmentDirections
                .actionNavigationDiaryListFragmentToDiaryEditFragment(
                    true,
                    false,
                    LocalDate.now()
                )
        navController.navigate(directions)
    }

    @MainThread
    private fun showShowDiaryFragment(date: LocalDate) {
        if (isDialogShowing) return

        val directions =
            DiaryListFragmentDirections
                .actionNavigationDiaryListFragmentToDiaryShowFragment(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun showWordSearchFragment() {
        if (isDialogShowing) return

        val directions =
            DiaryListFragmentDirections
                .actionNavigationDiaryListFragmentToWordSearchFragment()
        navController.navigate(directions)
    }

    @MainThread
    private fun showStartYearMonthPickerDialog(newestYear: Year, oldestYear: Year) {
        if (isDialogShowing) return

        val directions =
            DiaryListFragmentDirections
                .actionDiaryListFragmentToStartYearMonthPickerDialog(newestYear, oldestYear)
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryDeleteDialog(date: LocalDate, pictureUri: Uri?) {
        if (isDialogShowing) return

        val directions =
            DiaryListFragmentDirections
                .actionDiaryListFragmentToDiaryDeleteDialog(date, pictureUri)
        navController.navigate(directions)
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryListFragmentDirections
                .actionDiaryListFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    override fun retryOtherAppMessageDialogShow() {
        diaryListViewModel.triggerAppMessageBufferListObserver()
    }

    fun processOnReSelectNavigationItem() {
        scrollDiaryListToFirstPosition()
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    private fun scrollDiaryListToFirstPosition() {
        val listAdapter = binding.recyclerDiaryList.adapter as DiaryYearMonthListAdapter
        listAdapter.scrollToFirstPosition()
    }

    override fun destroyBinding() {
        _binding = null
    }
}
