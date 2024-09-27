package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.ListThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class WordSearchFragment extends BaseFragment {

    // View関係
    private FragmentWordSearchBinding binding;
    private String previousText = ""; // 二重検索防止用

    int resultWordColor = -1; // 検索結果ワード色
    int resultWordBackgroundColor = -1; // 検索結果ワードマーカー色

    // ViewModel
    private WordSearchViewModel wordSearchViewModel;
    private SettingsViewModel settingsViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        wordSearchViewModel = provider.get(WordSearchViewModel.class);
        wordSearchViewModel.initialize();
        settingsViewModel = provider.get(SettingsViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected View initializeDataBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        LayoutInflater themeColorInflater = createThemeColorInflater(inflater, themeColor);
        binding = FragmentWordSearchBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setWordSearchViewModel(wordSearchViewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpToolBar();
        setUpWordSearchView();
        setUpWordSearchResultList();
    }

    @Override
    protected void setUpThemeColor() {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        resultWordColor = themeColor.getOnTertiaryContainerColor(getResources());
        resultWordBackgroundColor = themeColor.getTertiaryContainerColor(getResources());
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        retryErrorDialogShow();
    }

    @Override
    protected void removeDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        // LifecycleEventObserverにダイアログからの結果受取処理コードを記述したら、ここに削除処理を記述する。
    }

    @Override
    protected void setUpErrorMessageDialog() {
        wordSearchViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(wordSearchViewModel));
    }

    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navController.navigateUp();
                    }
                });
    }

    private void setUpWordSearchView() {
        String searchWord = wordSearchViewModel.getSearchWordMutableLiveData().getValue();
        if (searchWord == null || searchWord.isEmpty()) {
            binding.editTextKeyWordSearch.requestFocus();
            KeyboardInitializer keyboardInitializer = new KeyboardInitializer(requireActivity());
            keyboardInitializer.show(binding.editTextKeyWordSearch);
        }
        wordSearchViewModel.getSearchWordMutableLiveData()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s == null) {
                            return;
                        }

                        if (s.isEmpty()) {
                            binding.imageButtonKeyWordClear.setVisibility(View.INVISIBLE);
                            binding.textWordSearchNoResults.setVisibility(View.INVISIBLE);
                            binding.linerLayoutWordSearchResults.setVisibility(View.INVISIBLE);
                        } else {
                            // 検索結果表示Viewは別Observerにて表示
                            binding.imageButtonKeyWordClear.setVisibility(View.VISIBLE);
                        }
                        // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
                        //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
                        if (s.equals(previousText)) {
                            return;
                        }
                        ThemeColor themeColor = settingsViewModel.getThemeColorSettingValueLiveData().getValue();
                        if (themeColor == null) {
                            throw new NullPointerException();
                        }

                        wordSearchViewModel
                                .loadWordSearchResultList(
                                        WordSearchViewModel.LoadType.NEW,
                                        resultWordColor,
                                        resultWordBackgroundColor
                                );
                        previousText = s;
                    }
                });

        // TODO:TextInputSetUpクラスを改良して下記コードと置き換える。
        binding.editTextKeyWordSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                View viewForHidingKeyboard = binding.viewForHidingKeyboard;
                if (hasFocus) {
                    viewForHidingKeyboard.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            v.performClick();
                            KeyboardInitializer keyboardInitializer =
                                    new KeyboardInitializer(requireActivity());
                            keyboardInitializer.hide(v);
                            binding.editTextKeyWordSearch.clearFocus();
                            return false;
                        }
                    });
                } else {
                    viewForHidingKeyboard.setOnTouchListener(null);
                }
            }
        });

        // エンターキー押下時の処理
        // HACK:setImeOptions()メソッドを使用しなくても、onEditorAction()のactionIdはIME_ACTION_DONEとなるが、
        //      一応設定しておく。onEditorAction()のeventは常時nullとなっている。(ハードキーボードなら返ってくる？)
        //      https://vividcode.hatenablog.com/entry/android-app/oneditoractionlistener-practice
        binding.editTextKeyWordSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        binding.editTextKeyWordSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    KeyboardInitializer keyboardInitializer =
                            new KeyboardInitializer(requireActivity());
                    keyboardInitializer.hide(v);
                    v.clearFocus();
                    return true;
                }
                return false;
            }
        });



        binding.imageButtonKeyWordClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordSearchViewModel.clearSearchWord();
                binding.editTextKeyWordSearch.requestFocus();
                KeyboardInitializer keyboardInitializer = new KeyboardInitializer(requireActivity());
                keyboardInitializer.show(binding.editTextKeyWordSearch);
            }
        });
    }

    private void setUpWordSearchResultList() {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        WordSearchResultListAdapter wordSearchResultListAdapter =
                new WordSearchResultListAdapter(
                        requireContext(),
                        binding.recyclerWordSearchResultList,
                        themeColor,
                        false
                );
        wordSearchResultListAdapter.build();
        wordSearchResultListAdapter.setOnClickChildItemListener(new DiaryYearMonthListAdapter.OnClickChildItemListener() {
            @Override
            public void onClick(LocalDate date) {
                showShowDiaryFragment(date);
            }
        });

        // データベースから読み込んだ日記リストをリサクラービューに反映
        wordSearchViewModel.getWordSearchResultListLiveData()
                .observe(getViewLifecycleOwner(), new Observer<List<WordSearchResultYearMonthListItem>>() {
                    @Override
                    public void onChanged(
                            List<WordSearchResultYearMonthListItem> wordSearchResultYearMonthListItems) {
                        if (wordSearchResultYearMonthListItems == null) {
                            return;
                        }
                        DiaryYearMonthListAdapter wordSearchResultYearMonthListAdapter =
                                (DiaryYearMonthListAdapter)
                                        binding.recyclerWordSearchResultList.getAdapter();
                        if (wordSearchResultYearMonthListAdapter == null) {
                            return;
                        }

                        String searchWord = wordSearchViewModel.getSearchWordLiveData().getValue();
                        if (searchWord == null || searchWord.isEmpty()) {
                            binding.textWordSearchNoResults.setVisibility(View.INVISIBLE);
                            binding.linerLayoutWordSearchResults.setVisibility(View.INVISIBLE);
                        } else if (wordSearchResultYearMonthListItems.isEmpty()) {
                            binding.textWordSearchNoResults.setVisibility(View.VISIBLE);
                            binding.linerLayoutWordSearchResults.setVisibility(View.INVISIBLE);
                        } else {
                            binding.textWordSearchNoResults.setVisibility(View.INVISIBLE);
                            binding.linerLayoutWordSearchResults.setVisibility(View.VISIBLE);
                        }

                        List<DiaryYearMonthListItemBase> convertedList =
                                new ArrayList<>(wordSearchResultYearMonthListItems);
                        Log.d("WordSearchList", "submitList前");
                        for (DiaryYearMonthListItemBase i: convertedList) {
                            YearMonth  yearMonth = i.getYearMonth();
                            if (yearMonth == null) {
                                Log.d("WordSearchList", "null");
                            } else {
                                Log.d("WordSearchList", yearMonth.toString());
                            }
                        }
                        Log.d("WordSearchList", "submitList");
                        Log.d("ListAdapterTest", "submitList");
                        wordSearchResultYearMonthListAdapter.submitList(convertedList);
                    }
                });

        wordSearchViewModel.getNumWordSearchResults()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        if (integer == null) {
                            return;
                        }

                        if (integer > 0) {
                            binding.textWordSearchResults.setVisibility(View.VISIBLE);
                        } else {
                            binding.textWordSearchResults.setVisibility(View.INVISIBLE);
                        }
                    }
                });

        // 検索結果リスト更新
        List<WordSearchResultYearMonthListItem> list =
                wordSearchViewModel.getWordSearchResultListLiveData().getValue();
        if (list != null && !list.isEmpty()) {
            wordSearchViewModel
                    .loadWordSearchResultList(
                            WordSearchViewModel.LoadType.UPDATE,
                            resultWordColor,
                            resultWordBackgroundColor
                    );
        }

        binding.includeProgressIndicator.viewBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
            }
        });
    }

    private class WordSearchResultListAdapter extends DiaryYearMonthListAdapter {

        public WordSearchResultListAdapter(Context context, RecyclerView recyclerView, ThemeColor themeColor, boolean canSwipeItem) {
            super(context, recyclerView, themeColor, canSwipeItem);
        }

        @Override
        public void loadListOnScrollEnd() {
            ThemeColor themeColor = settingsViewModel.getThemeColorSettingValueLiveData().getValue();
            if (themeColor == null) {
                throw new NullPointerException();
            }

            wordSearchViewModel
                    .loadWordSearchResultList(
                            WordSearchViewModel.LoadType.ADD,
                            resultWordColor,
                            resultWordBackgroundColor
                    );
        }

        @Override
        public boolean canLoadList() {
            return wordSearchViewModel.canLoadWordSearchResultList();
        }
    }

    private void showShowDiaryFragment(LocalDate date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                WordSearchFragmentDirections
                        .actionNavigationWordSearchFragmentToDiaryShowFragment(date);
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                WordSearchFragmentDirections
                        .actionWordSearchFragmentToMessageDialog(
                                title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryErrorDialogShow() {
        wordSearchViewModel.triggerAppErrorBufferListObserver();
    }

    // 選択中ボトムナビゲーションタブを再選択時の処理
    public void processOnReselectNavigationItem() {
        if (binding.recyclerWordSearchResultList.canScrollVertically(-1)) {
            resultListScrollToFirstPosition();
        } else {
            navController.navigateUp();
        }
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    public void resultListScrollToFirstPosition() {
        RecyclerView.Adapter<?> adapter = binding.recyclerWordSearchResultList.getAdapter();
        if (adapter instanceof DiaryYearMonthListAdapter) {
            DiaryYearMonthListAdapter diaryYearMonthListAdapter = (DiaryYearMonthListAdapter) adapter;
            diaryYearMonthListAdapter.scrollToFirstPosition();
        }
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
