package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding
import com.websarva.wings.android.zuboradiary.ui.view.edittext.EditTextConfigurator
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter.OnClickChildItemListener
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthListAdapter
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.WordSearchEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.viewmodel.WordSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

@AndroidEntryPoint
class WordSearchFragment : BaseFragment<FragmentWordSearchBinding>() {

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: WordSearchViewModel by viewModels()

    // RecyclerView関係
    // HACK:RecyclerViewのAdapterにセットするListを全て変更した時、
    //      変更前のListの内容で初期スクロール位置が定まらない不具合が発生。
    //      対策としてListを全て変更するタイミングでAdapterを新規でセットする。
    //      (親子関係でRecyclerViewを使用、又はListAdapterの機能による弊害？)
    private var shouldInitializeListAdapter = false

    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentWordSearchBinding {
        return FragmentWordSearchBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpWordSearchView()
        setUpWordSearchResultList()
        setUpFloatingActionButton()
    }

    override fun initializeFragmentResultReceiver() {
        // 処理なし
    }

    override fun onMainViewModelEventReceived(event: ViewModelEvent) {
        when (event) {
            is WordSearchEvent.NavigateDiaryShowFragment -> {
                navigateDiaryShowFragment(event.date)
            }
            WordSearchEvent.ShowKeyboard -> {
                showKeyboard()
            }
            ViewModelEvent.NavigatePreviousFragment -> {
                navigatePreviousFragment()
            }
            is ViewModelEvent.NavigateAppMessage -> {
                navigateAppMessageDialog(event.message)
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    private fun setUpWordSearchView() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.searchWord
                .collectLatest { value: String ->
                    if (value.isNotEmpty()) shouldInitializeListAdapter = true

                    mainViewModel.onSearchWordChanged(value)
                }
        }


        val editTextConfigurator = EditTextConfigurator()
        editTextConfigurator.setUpFocusClearOnClickBackground(
            binding.root,
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

    private fun navigateDiaryShowFragment(date: LocalDate) {
        val directions =
            WordSearchFragmentDirections.actionNavigationWordSearchFragmentToDiaryShowFragment(date)
        navigateFragment(NavigationCommand.To(directions))
        mainViewModel.onNextFragmentNavigated()
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            WordSearchFragmentDirections.actionWordSearchFragmentToAppMessageDialog(appMessage)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun showKeyboard() {
        binding.editTextSearchWord.requestFocus()
        KeyboardManager().showKeyboard(binding.editTextSearchWord)
    }
}
