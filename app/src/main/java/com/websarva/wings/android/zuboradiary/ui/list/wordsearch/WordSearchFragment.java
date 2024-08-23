package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WordSearchFragment extends Fragment {

    // View関係
    private FragmentWordSearchBinding binding;
    private String lastText = ""; // 二重検索防止用

    // Navigation関係
    private NavController navController;
    private boolean shouldShowDiaryListLoadingErrorDialog;

    // ViewModel
    private WordSearchViewModel wordSearchViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        wordSearchViewModel = provider.get(WordSearchViewModel.class);
        wordSearchViewModel.initialize();

        // Navigation設定
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        binding = FragmentWordSearchBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        binding.setLifecycleOwner(this);
        binding.setWordSearchViewModel(wordSearchViewModel);

        // 画面遷移時のアニメーション設定
        // FROM:遷移元 TO:遷移先
        // FROM - TO の TO として現れるアニメーション
        MainActivity mainActivity = (MainActivity) requireActivity();
        if (mainActivity.getTabWasSelected()) {
            setEnterTransition(new MaterialFadeThrough());
            mainActivity.resetTabWasSelected();
        } else {
            setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        }
        // FROM - TO の FROM として消えるアニメーション
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        // TO - FROM の FROM として現れるアニメーション
        /*if (switchesReenterTransition != null && switchesReenterTransition) {
            setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        } else {
            setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        }*/
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        // TO - FROM の TO として消えるアニメーション
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpDialogResultReceiver();
        setUpToolBar();
        setUpWordSearchView();
        setUpWordSearchResultList();
        setUpErrorObserver();
    }

    // ダイアログフラグメントからの結果受取設定
    private void setUpDialogResultReceiver() {
        NavBackStackEntry navBackStackEntry =
                navController.getBackStackEntry(R.id.navigation_diary_list_fragment);
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    removeDialogResults(savedStateHandle);
                    retryErrorDialogShow();
                }
            }
        };
        navBackStackEntry.getLifecycle().addObserver(lifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                    // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                    //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    removeDialogResults(savedStateHandle);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });
    }

    private void removeDialogResults(SavedStateHandle savedStateHandle) {
        // LifecycleEventObserverにダイアログからの結果受取処理コードを記述したら、ここに削除処理を記述する。
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
                            // TODO:assert
                            return;
                        }
                        // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
                        //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
                        if (s.equals(lastText)) {
                            return;
                        }
                        wordSearchViewModel
                                .setIsVisibleSearchWordClearButton(!s.isEmpty());
                        wordSearchViewModel
                                .loadWordSearchResultList(
                                        WordSearchViewModel.LoadType.NEW,
                                        getResources().getColor(R.color.gray) // TODO:テーマカラーで切替
                                );
                        lastText = s;
                    }
                });

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
        DiaryYearMonthListAdapter wordSearchResultYearMonthListAdapter =
                new DiaryYearMonthListAdapter(
                        requireContext(),
                        binding.recyclerWordSearchResultList,
                        new DiaryYearMonthListAdapter.OnScrollEndItemLoadingListener() {
                            @Override
                            public void Load() {
                                wordSearchViewModel
                                        .loadWordSearchResultList(
                                                WordSearchViewModel.LoadType.ADD,
                                                getResources().getColor(R.color.gray) // TODO:テーマカラーで切替
                                        );
                            }
                        },
                        new DiaryYearMonthListAdapter.OnScrollLoadingConfirmationListener() {
                            @Override
                            public boolean isLoading() {
                                return wordSearchViewModel.getIsLoading();
                            }
                        },
                        new DiaryYearMonthListAdapter.OnClickChildItemListener() {
                            @Override
                            public void onClick(LocalDate date) {
                                showShowDiaryFragment(date);
                            }
                        },
                        false,
                        null
                );
        wordSearchResultYearMonthListAdapter.build();

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
                        List<DiaryYearMonthListItemBase> convertedList =
                                new ArrayList<>(wordSearchResultYearMonthListItems);
                        wordSearchResultYearMonthListAdapter.submitList(convertedList);
                    }
                });

        // 検索結果リスト更新
        List<WordSearchResultYearMonthListItem> list =
                wordSearchViewModel.getWordSearchResultListLiveData().getValue();
        if (list != null && !list.isEmpty()) {
            wordSearchViewModel
                    .loadWordSearchResultList(
                            WordSearchViewModel.LoadType.UPDATE,
                            getResources().getColor(R.color.gray) // TODO:テーマカラーで切替
                    );
        }

        binding.viewWordSearchResultListProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
            }
        });
    }

    private void setUpErrorObserver() {
        // エラー表示
        wordSearchViewModel.getIsDiaryListLoadingErrorLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean) {
                            showDiaryListLoadingErrorDialog();
                            wordSearchViewModel.clearIsDiaryListLoadingError();
                        }
                    }
                });
    }

    private void showShowDiaryFragment(LocalDate date) {
        NavDirections action =
                WordSearchFragmentDirections
                        .actionNavigationWordSearchFragmentToDiaryShowFragment(date);
        navController.navigate(action);
    }

    // 他のダイアログで表示できなかったダイアログを表示
    private void retryErrorDialogShow() {
        if (shouldShowDiaryListLoadingErrorDialog) {
            showDiaryListLoadingErrorDialog();
        }
    }

    private void showDiaryListLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog(getString(R.string.dialog_message_title_communication_error), getString(R.string.dialog_message_message_diary_word_search_error));
            shouldShowDiaryListLoadingErrorDialog = false;
        } else {
            shouldShowDiaryListLoadingErrorDialog = true;
        }
    }

    private void showMessageDialog(String title, String message) {
        NavDirections action =
                WordSearchFragmentDirections
                        .actionWordSearchFragmentToMessageDialog(
                                title, message);
        navController.navigate(action);
    }

    private boolean canShowDialog() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination == null) {
            return false;
        }
        int currentDestinationId = navController.getCurrentDestination().getId();
        return currentDestinationId == R.id.navigation_word_search_fragment;
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
}
