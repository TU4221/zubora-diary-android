package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding
import com.websarva.wings.android.zuboradiary.ui.view.edittext.EditTextConfigurator
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter.OnClickChildItemListener
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthListAdapter
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.WordSearchFragmentAction
import com.websarva.wings.android.zuboradiary.ui.viewmodel.WordSearchViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

class WordSearchFragment : BaseFragment() {
    // View関係
    private var _binding: FragmentWordSearchBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: WordSearchViewModel by activityViewModels()

    // RecyclerView関係
    // HACK:RecyclerViewのAdapterにセットするListを全て変更した時、
    //      変更前のListの内容で初期スクロール位置が定まらない不具合が発生。
    //      対策としてListを全て変更するタイミングでAdapterを新規でセットする。
    //      (親子関係でRecyclerViewを使用、又はListAdapterの機能による弊害？)
    private var shouldInitializeListAdapter = false

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentWordSearchBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@WordSearchFragment.viewLifecycleOwner
            wordSearchViewModel = mainViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpKeyboard()
        setUpWordSearchView()
        setUpWordSearchResultList()
        setUpFloatingActionButton()

        setUpFragmentAction()
        setUpNavDestinationChangedListener()
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // 処理なし
    }

    override fun receiveDialogResults() {
        // 処理なし
    }

    override fun removeDialogResults() {
        // LifecycleEventObserverにダイアログからの結果受取処理コードを記述したら、ここに削除処理を記述する。
    }

    private fun setUpKeyboard() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.shouldShowKeyboard
                .collectLatest { value: Boolean ->
                    if (value) showKeyboard()
                }
        }
    }

    private fun showKeyboard() {
        binding.editTextSearchWord.requestFocus()
        KeyboardManager().showKeyboard(binding.editTextSearchWord)
        mainViewModel.onShowedKeyboard()
    }

    private fun setUpWordSearchView() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.searchWord
                .collectLatest { value: String ->
                    if (value.isNotEmpty()) shouldInitializeListAdapter = true

                    mainViewModel.onSearchWordChanged()
                }
        }


        val editTextConfigurator = EditTextConfigurator()
        editTextConfigurator.setUpFocusClearOnClickBackground(
            binding.viewFullScreenBackground,
            binding.editTextSearchWord
        )
        editTextConfigurator.setUpKeyboardCloseOnEnter(binding.editTextSearchWord)
        editTextConfigurator.setUpClearButton(
            binding.editTextSearchWord,
            binding.imageButtonSearchWordClear
        )
    }

    private fun setUpWordSearchResultList() {
        setUpListAdapter()

        binding.floatingActionButtonTopScroll.hide() // MEMO:初回起動用

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.wordSearchResultList
                .collectLatest { value: WordSearchResultYearMonthList ->
                    WordSearchResultListObserver().onChanged(value)
                }
        }
    }

    private fun setUpListAdapter() {
        val wordSearchResultListAdapter =
            WordSearchResultListAdapter(
                binding.recyclerWordSearchResultList,
                themeColor
            )
        wordSearchResultListAdapter.apply {
            build()
            onClickChildItemListener =
                OnClickChildItemListener { item: DiaryDayListBaseItem ->
                    mainViewModel.onWordSearchResultListItemClicked(item.date)
                }
        }
    }

    private inner class WordSearchResultListAdapter(
        recyclerView: RecyclerView,
        themeColor: ThemeColor
    ) :
        WordSearchResultYearMonthListAdapter(recyclerView, themeColor) {
        override fun loadListOnScrollEnd() {
            mainViewModel.onWordSearchResultListEndScrolled()
        }
    }

    private inner class WordSearchResultListObserver :
        Observer<WordSearchResultYearMonthList> {
        override fun onChanged(value: WordSearchResultYearMonthList) {
            if (shouldInitializeListAdapter) {
                shouldInitializeListAdapter = false
                setUpListAdapter()
            }

            val listAdapter =
                binding.recyclerWordSearchResultList.adapter
                        as WordSearchResultYearMonthListAdapter
            val convertedList: List<DiaryYearMonthListBaseItem> =
                ArrayList<DiaryYearMonthListBaseItem>(value.itemList)
            listAdapter.submitList(convertedList) {
                mainViewModel.onWordSearchResultListUpdated()
            }
        }
    }

    private fun setUpFloatingActionButton() {
        binding.floatingActionButtonTopScroll.setOnClickListener {
            resultListScrollToFirstPosition()
        }
        binding.recyclerWordSearchResultList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (recyclerView.canScrollVertically(-1)) {
                    binding.floatingActionButtonTopScroll.show()
                } else {
                    binding.floatingActionButtonTopScroll.hide()
                }
            }
        })
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    private fun resultListScrollToFirstPosition() {
        val adapter = binding.recyclerWordSearchResultList.adapter
        val listAdapter = adapter as WordSearchResultYearMonthListAdapter
        listAdapter.scrollToTop()
    }

    private fun setUpFragmentAction() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.fragmentAction
                .collect { value: FragmentAction ->
                    when (value) {
                        is WordSearchFragmentAction.NavigateDiaryShowFragment -> {
                            navigateDiaryShowFragment(value.date)
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

    private fun navigateDiaryShowFragment(date: LocalDate) {
        if (!canNavigateFragment) return

        val directions =
            WordSearchFragmentDirections.actionNavigationWordSearchFragmentToDiaryShowFragment(date)
        navController.navigate(directions)
    }

    override fun onNavigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            WordSearchFragmentDirections.actionWordSearchFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    private fun setUpNavDestinationChangedListener() {
        navController.addOnDestinationChangedListener(NavDestinationChangedListener())
    }

    private inner class NavDestinationChangedListener
        : NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            if (destination.id == R.id.navigation_word_search_fragment) return

            if (destination.id == R.id.navigation_diary_show_fragment) {
                mainViewModel.onNextFragmentNavigated()
            } else {
                mainViewModel.onPreviousFragmentNavigated()
            }

            navController.removeOnDestinationChangedListener(this)
        }
    }

    override fun destroyBinding() {
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()

        mainViewModel.onFragmentDestroyed()
    }
}
