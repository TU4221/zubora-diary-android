package com.websarva.wings.android.zuboradiary.ui.list.wordsearch

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.EditTextSetup
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter.OnClickChildItemListener
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.notNullValue
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
        binding.lifecycleOwner = this
        binding.wordSearchViewModel = wordSearchViewModel
        return binding
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
        val themeColor = requireThemeColor()
        resultWordColor = themeColor.getOnTertiaryContainerColor(resources)
        resultWordBackgroundColor = themeColor.getTertiaryContainerColor(resources)
    }

    override fun handleOnReceivingResultFromPreviousFragment(savedStateHandle: SavedStateHandle) {
        // 処理なし
    }

    override fun handleOnReceivingDialogResult(savedStateHandle: SavedStateHandle) {
        retryOtherAppMessageDialogShow()
    }

    override fun removeDialogResultOnDestroy(savedStateHandle: SavedStateHandle) {
        // LifecycleEventObserverにダイアログからの結果受取処理コードを記述したら、ここに削除処理を記述する。
    }

    override fun setUpOtherAppMessageDialog() {
        wordSearchViewModel.appMessageBufferList
            .observe(viewLifecycleOwner, AppMessageBufferListObserver(wordSearchViewModel))
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                navController.navigateUp()
            }
    }

    private fun setUpWordSearchView() {
        val searchWord = wordSearchViewModel.searchWord.notNullValue()
        if (searchWord.isEmpty()) {
            binding.editTextSearchWord.requestFocus()
            val keyboardInitializer = KeyboardInitializer(requireActivity())
            keyboardInitializer.show(binding.editTextSearchWord)
        }

        wordSearchViewModel.searchWord
            .observe(viewLifecycleOwner) { s: String ->
                Log.d("20250204", "searchWordObserve")
                // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
                //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
                if (s == previousText) return@observe

                // 検索結果表示Viewは別Observerにて表示
                if (s.isEmpty()) {
                    binding.textNoWordSearchResultsMessage.visibility = View.INVISIBLE
                    binding.linerLayoutWordSearchResults.visibility = View.INVISIBLE
                    wordSearchViewModel.initialize()
                } else {
                    wordSearchViewModel
                        .loadNewWordSearchResultList(resultWordColor, resultWordBackgroundColor)
                }
                previousText = s
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
                requireThemeColor()
            )
        wordSearchResultListAdapter.build()
        wordSearchResultListAdapter.onClickChildItemListener =
            OnClickChildItemListener { item: DiaryDayListBaseItem ->
                showShowDiaryFragment(item.date)
            }

        wordSearchViewModel.wordSearchResultList
            .observe(viewLifecycleOwner, WordSearchResultListObserver())

        wordSearchViewModel.numWordSearchResults
            .observe(viewLifecycleOwner) { integer: Int ->
                val visibility = if (integer > 0) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
                binding.textNumWordSearchResults.visibility = visibility
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
            return wordSearchViewModel.canLoadWordSearchResultList()
        }
    }

    private inner class WordSearchResultListObserver :
        Observer<WordSearchResultYearMonthList> {
        override fun onChanged(value: WordSearchResultYearMonthList) {
            val listAdapter =
                checkNotNull(
                    binding.recyclerWordSearchResultList.adapter
                ) as WordSearchResultYearMonthListAdapter

            val searchWord = wordSearchViewModel.searchWord.notNullValue()
            if (searchWord.isEmpty()) {
                binding.floatingActionButtonTopScroll.hide() // MEMO:初回起動用
                binding.textNoWordSearchResultsMessage.visibility = View.INVISIBLE
                binding.linerLayoutWordSearchResults.visibility = View.INVISIBLE
            } else if (value.wordSearchResultYearMonthListItemList.isEmpty()) {
                binding.textNoWordSearchResultsMessage.visibility = View.VISIBLE
                binding.linerLayoutWordSearchResults.visibility = View.INVISIBLE
            } else {
                binding.textNoWordSearchResultsMessage.visibility = View.INVISIBLE
                binding.linerLayoutWordSearchResults.visibility = View.VISIBLE
            }

            val convertedList: List<DiaryYearMonthListBaseItem> =
                ArrayList<DiaryYearMonthListBaseItem>(value.wordSearchResultYearMonthListItemList)
            listAdapter.submitList(convertedList)
        }
    }

    private fun updateWordSearchResultList() {
        val list = wordSearchViewModel.wordSearchResultList.notNullValue()
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

    private fun showShowDiaryFragment(date: LocalDate) {
        if (isDialogShowing()) return

        val action: NavDirections =
            WordSearchFragmentDirections
                .actionNavigationWordSearchFragmentToDiaryShowFragment(date)
        navController.navigate(action)
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val action: NavDirections =
            WordSearchFragmentDirections
                .actionWordSearchFragmentToAppMessageDialog(appMessage)
        navController.navigate(action)
    }

    override fun retryOtherAppMessageDialogShow() {
        wordSearchViewModel.triggerAppMessageBufferListObserver()
    }

    override fun destroyBinding() {
        _binding = null
    }
}
