package com.websarva.wings.android.zuboradiary.ui.diary.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding
import com.websarva.wings.android.zuboradiary.ui.common.keyboard.KeyboardManager
import com.websarva.wings.android.zuboradiary.ui.common.fragment.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListUi
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.DummyNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.diary.show.DiaryShowScreenParams
import com.websarva.wings.android.zuboradiary.ui.diary.common.recyclerview.DiaryListSetupHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * 日記のワード検索機能を提供するフラグメント。
 *
 * 以下の責務を持つ:
 * - 入力されたキーワードで日記を検索し、結果を一覧表示する
 * - スクロールに応じた追加の検索結果読み込み
 * - 検索結果リストアイテムをタップした際の日記詳細画面への遷移
 * - 画面表示時のキーボード自動表示
 */
@AndroidEntryPoint
class WordSearchFragment : BaseFragment<
        FragmentWordSearchBinding,
        WordSearchUiEvent,
        WordSearchNavDestination,
        DummyNavBackDestination
        >() {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: WordSearchViewModel by viewModels()

    override val destinationId = R.id.navigation_word_search_fragment

    override val resultKey: String? get() = null

    /** ワード検索結果の日記リストを表示するためのRecyclerViewアダプター。 */
    private var wordSearchResultDiaryListAdapter: WordSearchResultDiaryListAdapter? = null

    /** 日記リスト(RecyclerView)のセットアップを補助するヘルパークラス。 */
    private var diaryListSetupHelper: DiaryListSetupHelper? = null

    /** FABを制御するためのスクロールリスナー。 */
    private var fabScrollListener: RecyclerView.OnScrollListener? = null

    /** ソフトウェアキーボードを制御するマネージャークラス。 */
    private lateinit var keyboardManager: KeyboardManager
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、日記リスト、先頭スクロールFAB、キーボードマネージャーの初期設定を行う。*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWordSearchResultList()
        setupTopScrollFAB()
        setupKeyboardManager()
    }
    //endregion

    //region View Binding Setup
    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentWordSearchBinding {
        return FragmentWordSearchBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    /** 追加処理として、リスナ、アダプタ等の解放を行う。*/
    override fun clearViewBindings() {
        binding.recyclerWordSearchResultList.adapter = null
        wordSearchResultDiaryListAdapter = null

        diaryListSetupHelper?.cleanup()
        diaryListSetupHelper = null

        binding.floatingActionButtonTopScroll.setOnClickListener(null)
        fabScrollListener?.let {
            binding.recyclerWordSearchResultList.removeOnScrollListener(it)
        }
        fabScrollListener = null

        super.clearViewBindings()
    }
    //endregion

    //region Fragment Result Observation Setup
    override fun setupFragmentResultObservers() {
        // このフラグメントでは結果を受け取らないため、処理はなし
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: WordSearchUiEvent) {
        when (event) {
            WordSearchUiEvent.ShowKeyboard -> {
                showKeyboard()
            }
        }
    }

    override fun setupUiStateObservers() {
        super.setupUiStateObservers()

        observeWordSearchResultListItem()
    }

    /** ワード検索結果リストのデータの変更を監視し、UIを更新する。 */
    private fun observeWordSearchResultListItem() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.wordSearchResultList == new.wordSearchResultList
            }.map {
                it.wordSearchResultList
            }.collect {
                updateWordSearchResultList(it)
            }
        }
    }
    //endregion

    //region View Setup
    /** ワード検索結果リストを表示するRecyclerViewの初期設定を行う。 */
    private fun setupWordSearchResultList() {
        val diaryRecyclerView = binding.recyclerWordSearchResultList
        wordSearchResultDiaryListAdapter = WordSearchResultDiaryListAdapter(
            themeColor
        ) { mainViewModel.onWordSearchResultListItemClick(it) }.also {
            diaryListSetupHelper =
                DiaryListSetupHelper(
                    diaryRecyclerView,
                    it
                ) {
                    mainViewModel.onWordSearchResultListEndScrolled()
                }.apply { setup() }
        }
    }

    /** リスト先頭へスクロールするためのFloatingActionButtonを設定する。 */
    private fun setupTopScrollFAB() {
        val fab = binding.floatingActionButtonTopScroll.apply {
            hide() // MEMO:初回起動用
            setOnClickListener {
                binding.recyclerWordSearchResultList.smoothScrollToPosition(0)
            }
        }
        fabScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (recyclerView.canScrollVertically(-1)) {
                    fab.show()
                } else {
                    fab.hide()
                }
            }
        }.also { binding.recyclerWordSearchResultList.addOnScrollListener(it) }
    }

    /** キーボード操作を管理するクラスを初期化する。 */
    private fun setupKeyboardManager() {
        keyboardManager = KeyboardManager(requireContext())
    }
    //endregion

    //region View Control
    /** アダプターに新しい検索結果リストを送信し、UIを更新する。 */
    private fun updateWordSearchResultList(
        list: DiaryListUi<DiaryListItemContainerUi.WordSearchResult>
    ) {
        wordSearchResultDiaryListAdapter?.submitList(list.itemList) {
            mainViewModel.onWordSearchResultListUpdateCompleted()
        }
    }

    /** 検索ワード入力欄にフォーカスを当て、キーボードを表示する。 */
    private fun showKeyboard() {
        binding.textInputEditTextSearchWord
            .apply {
                requestFocus()
            }.also {
                keyboardManager.showKeyboard(it)
            }

    }
    //endregion

    //region Navigation Helpers
    override fun toNavDirections(destination: WordSearchNavDestination): NavDirections {
        return when (destination) {
            is WordSearchNavDestination.AppMessageDialog -> {
                navigationEventHelper.createAppMessageDialogNavDirections(destination.message)
            }
            is WordSearchNavDestination.DiaryShowScreen -> {
                createDiaryShowFragmentNavDirection(destination.id, destination.date)
            }
        }
    }

    override fun toNavDestinationId(destination: DummyNavBackDestination): Int {
        // 処理なし
        throw IllegalStateException("NavDestinationIdへの変換は不要の為、未対応。")
    }

    /**
     * 日記表示画面へ遷移する為の [NavDirections] オブジェクトを生成する。
     *
     * @param id 表示する日記のID
     * @param date 表示する日記の日付
     */
    private fun createDiaryShowFragmentNavDirection(id: String, date: LocalDate): NavDirections {
        val params = DiaryShowScreenParams(
            RESULT_KEY_DIARY,
            id,
            date
        )
        return WordSearchFragmentDirections
                .actionNavigationWordSearchFragmentToDiaryShowFragment(params)
    }
    //endregion

    internal companion object {
        /** 日記表示・編集画面からの遷移戻り時に、結果データを受け取るためのリクエストキー。 */
        private const val RESULT_KEY_DIARY = "diary_result"
    }
}
