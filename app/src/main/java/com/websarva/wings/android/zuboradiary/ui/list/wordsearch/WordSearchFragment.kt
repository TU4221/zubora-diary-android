package com.websarva.wings.android.zuboradiary.ui.list.wordsearch

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.EditTextSetup
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter.OnClickChildItemListener
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

class WordSearchFragment : BaseFragment() {
    // View関係
    private var _binding: FragmentWordSearchBinding? = null
    private val binding get() = checkNotNull(_binding)

    private var previousText = "" // 二重検索防止用
    private var resultWordColor = -1 // 検索結果ワード色
    private var resultWordBackgroundColor = -1 // 検索結果ワードマーカー色

    // ViewModel
    private lateinit var wordSearchViewModel: WordSearchViewModel

    override fun initializeViewModel() {
        val provider = ViewModelProvider(requireActivity())
        wordSearchViewModel = provider[WordSearchViewModel::class.java]
        wordSearchViewModel.initialize()
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentWordSearchBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@WordSearchFragment.viewLifecycleOwner
            wordSearchViewModel = this@WordSearchFragment.wordSearchViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpThemeColor()
        setUpToolBar()
        setUpWordSearchView()
        setUpWordSearchResultList()
        setUpFloatingActionButton()
    }

    private fun setUpThemeColor() {
        resultWordColor = themeColor.getOnTertiaryContainerColor(resources)
        resultWordBackgroundColor = themeColor.getTertiaryContainerColor(resources)
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // 処理なし
    }

    override fun handleOnReceivingDialogResult() {
        // 処理なし
    }

    override fun removeDialogResultOnDestroy() {
        // LifecycleEventObserverにダイアログからの結果受取処理コードを記述したら、ここに削除処理を記述する。
    }

    override fun setUpOtherAppMessageDialog() {
        launchAndRepeatOnViewLifeCycleStarted {
            wordSearchViewModel.appMessageBufferList
                .collectLatest { value: AppMessageList ->
                    AppMessageBufferListObserver(wordSearchViewModel).onChanged(value)
                }
        }
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                navController.navigateUp()
            }
    }

    private fun setUpWordSearchView() {
        val searchWord = wordSearchViewModel.searchWord.value
        if (searchWord.isEmpty()) {
            binding.editTextSearchWord.requestFocus()
            val keyboardInitializer = KeyboardInitializer(requireActivity())
            keyboardInitializer.show(binding.editTextSearchWord)
        }

        launchAndRepeatOnViewLifeCycleStarted {
            wordSearchViewModel.searchWord
                .collectLatest { value: String ->
                    // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
                    //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
                    if (value == previousText) return@collectLatest

                    // 検索結果表示Viewは別Observerにて表示
                    if (value.isEmpty()) {
                        binding.textNoWordSearchResultsMessage.visibility = View.INVISIBLE
                        binding.linerLayoutWordSearchResults.visibility = View.INVISIBLE
                        wordSearchViewModel.initialize()
                    } else {
                        wordSearchViewModel
                            .loadNewWordSearchResultList(resultWordColor, resultWordBackgroundColor)
                    }
                    previousText = value
                }
        }


        val editTextSetup = EditTextSetup(requireActivity())
        editTextSetup.setUpFocusClearOnClickBackground(
            binding.viewFullScreenBackground,
            binding.editTextSearchWord
        )
        editTextSetup.setUpKeyboardCloseOnEnter(binding.editTextSearchWord)
        editTextSetup.setUpClearButton(
            binding.editTextSearchWord,
            binding.imageButtonSearchWordClear
        )
    }

    private fun setUpWordSearchResultList() {
        val wordSearchResultListAdapter =
            WordSearchResultListAdapter(
                requireContext(),
                binding.recyclerWordSearchResultList,
                themeColor
            )
        wordSearchResultListAdapter.build()
        wordSearchResultListAdapter.onClickChildItemListener =
            OnClickChildItemListener { item: DiaryDayListBaseItem ->
                showShowDiaryFragment(item.date)
            }

        launchAndRepeatOnViewLifeCycleStarted {
            wordSearchViewModel.wordSearchResultList
                .collectLatest { value: WordSearchResultYearMonthList ->
                    WordSearchResultListObserver().onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            wordSearchViewModel.numWordSearchResults
                .collectLatest { value: Int ->
                    val visibility = if (value > 0) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                    binding.textNumWordSearchResults.visibility = visibility
                }
        }

        binding.includeProgressIndicator.viewBackground
            .setOnTouchListener { v: View, _: MotionEvent ->
                v.performClick()
                true
            }

        updateWordSearchResultList()
    }

    private inner class WordSearchResultListAdapter(
        context: Context,
        recyclerView: RecyclerView,
        themeColor: ThemeColor
    ) :
        WordSearchResultYearMonthListAdapter(context, recyclerView, themeColor) {
        override fun loadListOnScrollEnd() {
            wordSearchViewModel
                .loadAdditionWordSearchResultList(resultWordColor, resultWordBackgroundColor)
        }

        override fun canLoadList(): Boolean {
            return wordSearchViewModel.canLoadWordSearchResultList
        }
    }

    private inner class WordSearchResultListObserver :
        Observer<WordSearchResultYearMonthList> {
        override fun onChanged(value: WordSearchResultYearMonthList) {
            val listAdapter =
                checkNotNull(
                    binding.recyclerWordSearchResultList.adapter
                ) as WordSearchResultYearMonthListAdapter

            val searchWord = wordSearchViewModel.searchWord.value
            if (searchWord.isEmpty()) {
                binding.apply {
                    floatingActionButtonTopScroll.hide() // MEMO:初回起動用
                    textNoWordSearchResultsMessage.visibility = View.INVISIBLE
                    linerLayoutWordSearchResults.visibility = View.INVISIBLE
                }
            } else if (value.wordSearchResultYearMonthListItemList.isEmpty()) {
                binding.apply {
                    textNoWordSearchResultsMessage.visibility = View.VISIBLE
                    linerLayoutWordSearchResults.visibility = View.INVISIBLE
                }
            } else {
                binding.apply {
                    textNoWordSearchResultsMessage.visibility = View.INVISIBLE
                    linerLayoutWordSearchResults.visibility = View.VISIBLE
                }
            }

            val convertedList: List<DiaryYearMonthListBaseItem> =
                ArrayList<DiaryYearMonthListBaseItem>(value.wordSearchResultYearMonthListItemList)
            listAdapter.submitList(convertedList)
        }
    }

    private fun updateWordSearchResultList() {
        val list = wordSearchViewModel.wordSearchResultList.value
        if (list.wordSearchResultYearMonthListItemList.isEmpty()) return
        wordSearchViewModel.updateWordSearchResultList(resultWordColor, resultWordBackgroundColor)
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
        listAdapter.scrollToFirstPosition()
    }

    @MainThread
    private fun showShowDiaryFragment(date: LocalDate) {
        if (isDialogShowing) return

        val directions =
            WordSearchFragmentDirections
                .actionNavigationWordSearchFragmentToDiaryShowFragment(date)
        navController.navigate(directions)
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            WordSearchFragmentDirections
                .actionWordSearchFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    override fun retryOtherAppMessageDialogShow() {
        wordSearchViewModel.triggerAppMessageBufferListObserver()
    }

    override fun destroyBinding() {
        _binding = null
    }
}
