package com.websarva.wings.android.zuboradiary.ui.diary.showdiary;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.FragmentShowDiaryBinding;
import com.websarva.wings.android.zuboradiary.ChangeFragment;
import com.websarva.wings.android.zuboradiary.ui.calendar.CalendarFragment;
import com.websarva.wings.android.zuboradiary.ui.editdiary.EditDiaryFragment;
import com.websarva.wings.android.zuboradiary.ui.editdiary.EditDiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.list.ListFragment;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchFragment;

public class ShowDiaryFragment extends Fragment {

    private FragmentShowDiaryBinding binding;
    private EditDiaryViewModel diaryViewModel;

    private MenuProvider showDiaryMenuProvider = new ShowDiaryMenuProvider();

    public ShowDiaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryViewModel = provider.get(EditDiaryViewModel.class);

        // 戻るボタン押下時の処理
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        backFragment();

                    }
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_display_diary, container, false);

        binding = FragmentShowDiaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //データバインディング設定(ビューモデルのライブデータ画面反映設定)
        binding.setLifecycleOwner(ShowDiaryFragment.this);
        binding.setDiaryViewModel(diaryViewModel);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // アクションバーオプションメニュー更新。
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(
                showDiaryMenuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);



        // 画面表示データ準備
        diaryViewModel.prepareShowDiary();


        // 天気入力欄設定
        // MEMO:ShowDiaryFragmentの中身を一つのレイアウトとして独立させた。
        //      bindingで中身のViewを取得できなくなった為、findViewById()メソッドを使用してViewを取得。
        TextView textWeatherSlush = getActivity().findViewById(R.id.text_weather_slush);
        TextView textWeather2Selected =
                getActivity().findViewById(R.id.text_weather_2_selected);
        diaryViewModel.getLiveIntWeather2().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if ((integer != 0) && (integer != null)) {
                    /*binding.textWeatherSlush.setVisibility(View.VISIBLE);
                    binding.textWeather2Selected.setVisibility(View.VISIBLE);*/
                    textWeatherSlush.setVisibility(View.VISIBLE);
                    textWeather2Selected.setVisibility(View.VISIBLE);

                } else {
                    /*binding.textWeatherSlush.setVisibility(View.GONE);
                    binding.textWeather2Selected.setVisibility(View.GONE);*/
                    textWeatherSlush.setVisibility(View.GONE);
                    textWeather2Selected.setVisibility(View.GONE);

                }
            }
        });

        diaryViewModel.getLiveIntWeather1().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                diaryViewModel.updateStrWeather1();
            }
        });

        diaryViewModel.getLiveIntWeather2().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                diaryViewModel.updateStrWeather2();
            }
        });


        // 気分入力欄設定
        diaryViewModel.getLiveIntCondition().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                diaryViewModel.updateStrCondition();
            }
        });

    }

    private class ShowDiaryMenuProvider implements MenuProvider {
            //アクションバーオプションメニュー設定。
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

                //ツールバーオプションメニュー設定
                menuInflater.inflate(R.menu.display_diary_toolbar_menu, menu);

                ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(null);

                actionBar.setTitle(diaryViewModel.getLiveDate().getValue());
            }

            //アクションバーメニュー選択処理設定。
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                //日記編集フラグメント起動。
                if (menuItem.getItemId() == R.id.displayDiaryToolbarOptionEditDiary) {
                    Toast.makeText(getView().getContext(), menuItem.toString(), Toast.LENGTH_SHORT).show();

                    FragmentManager fragmentManager = getParentFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setReorderingAllowed(true);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.front_fragmentContainerView_activity_main, EditDiaryFragment.class, null);
                    fragmentTransaction.commit();

                    return true;

                    //戻る(ナビフラグメント表示(日記表示フラグメント削除))。
                } else if (menuItem.getItemId() == android.R.id.home) {
                    backFragment();
                    return true;

                } else {
                    return false;
                }
            }
    }

    // 一つ前のフラグメント(EDitDiaryFragment)を表示
    private void backFragment() {
        Toast.makeText(getView().getContext(), "戻る", Toast.LENGTH_SHORT).show();
        FragmentManager parentFragmentManager = getParentFragmentManager();

        // ナビフラグメント取得
        FragmentManager activityFragmentManager = getActivity().getSupportFragmentManager();
        Fragment navFragment = activityFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        Fragment navChildFragment = navFragment.getChildFragmentManager().getFragments().get(0);

        //ナビフラグメントがリストフラグメントの時、メニューバー更新。
        // HACK:EditDiaryFragmentからListFragmentを表示した時、
        //      ListFragmentとEditDiaryFragmentのメニューバーが混在する。
        //      ListFragmentResultListenerでメニューバーの更新を設定しているが、
        //      ListFragmentがonResume状態で背面に存在するしたいるため、
        //      EditDiaryFragmentがonPause状態になる前に、
        //      ListFragmentResultListenerが起動して一時的に混在すると思われる。
        //      対策として下記コードを記述する。
        MenuHost menuHost = requireActivity();
        menuHost.removeMenuProvider(showDiaryMenuProvider);

        // ナビフラグメントがリストフラグメントの時、リスト更新。
        if (navChildFragment instanceof ListFragment) {
            Bundle result = new Bundle();
            parentFragmentManager.setFragmentResult(
                    "ToListFragment_ShowDiaryFragmentRequestKey", result);

        }

        // ナビフラグメントがワード検索フラグメントの時、リスト更新。
        if (navChildFragment instanceof WordSearchFragment) {
            Bundle result = new Bundle();
            parentFragmentManager.setFragmentResult(
                    "ToWordSearchFragment_ShowDiaryFragmentRequestKey", result);

        }

        // ナビフラグメントがカレンダーフラグメントの時、カレンダー更新。
        if (navChildFragment instanceof CalendarFragment) {
            Bundle result = new Bundle();
            parentFragmentManager.setFragmentResult(
                    "ToCalendarFragment_ShowDiaryFragmentRequestKey", result);

        }

        ChangeFragment.popBackStackOnFrontFragment(parentFragmentManager, true);

    }

}
