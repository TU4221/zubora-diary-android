package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter.OnClickChildItemListener
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthListAdapter
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.WordSearchEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.viewmodel.WordSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

@AndroidEntryPoint
class WordSearchFragment : BaseFragment<FragmentWordSearchBinding, WordSearchEvent>() {

    override val destinationId = R.id.navigation_word_search_fragment

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
    // TODO:下記変数による処理を無効化しても上記不具合の確認ができない為、開発最後に必要か判断
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

    override fun onMainUiEventReceived(event: WordSearchEvent) {
        when (event) {
            is WordSearchEvent.NavigateDiaryShowFragment -> {
                navigateDiaryShowFragment(event.date)
            }
            WordSearchEvent.ShowKeyboard -> {
                showKeyboard()
            }
            is WordSearchEvent.CommonEvent -> {
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

    private fun setUpWordSearchView() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.searchWord
                .collectLatest { value: String ->
                    if (value.isNotEmpty()) shouldInitializeListAdapter = true

                    mainViewModel.onSearchWordChanged(value)
                }
        }
    }

    private fun setUpWordSearchResultList() {
        setUpListAdapter()

        binding.floatingActionButtonTopScroll.hide() // MEMO:初回起動用

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.wordSearchResultList
                .collectLatest { value: WordSearchResultYearMonthList ->
                    if (shouldInitializeListAdapter) {
                        shouldInitializeListAdapter = false
                        //setUpListAdapter()
                    }

                    val listAdapter =
                        binding.recyclerWordSearchResultList.adapter
                                as WordSearchResultYearMonthListAdapter
                    val convertedList: List<DiaryYearMonthListBaseItem> =
                        ArrayList<DiaryYearMonthListBaseItem>(value.itemList)
                    listAdapter.submitList(convertedList) {
                        mainViewModel.onWordSearchResultListUpdateCompleted()
                    }
                }
        }
    }

    private fun setUpListAdapter() {
        val wordSearchResultListAdapter =
            object : WordSearchResultYearMonthListAdapter(
                binding.recyclerWordSearchResultList,
                themeColor
            ) {
                override fun loadListOnScrollEnd() {
                    mainViewModel.onWordSearchResultListEndScrolled()
                }
            }

        wordSearchResultListAdapter.apply {
            build()
            onClickChildItemListener =
                OnClickChildItemListener { item: DiaryDayListBaseItem ->
                    mainViewModel.onWordSearchResultListItemClick(item.date)
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
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            WordSearchFragmentDirections.actionWordSearchFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }

    private fun showKeyboard() {
        binding.textInputEditTextSearchWord.requestFocus()
        KeyboardManager().showKeyboard(binding.textInputEditTextSearchWord)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.onFragmentDestroyView()
    }

    override fun clearViewBindings() {
        binding.recyclerWordSearchResultList.apply {
            val listAdapter = adapter as WordSearchResultYearMonthListAdapter
            listAdapter.clearRecyclerViewBindings()
            clearOnScrollListeners()
        }
        binding.floatingActionButtonTopScroll.setOnClickListener(null)

        super.clearViewBindings()
    }
}
