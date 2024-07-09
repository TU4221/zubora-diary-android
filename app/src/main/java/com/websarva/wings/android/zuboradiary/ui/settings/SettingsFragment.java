package com.websarva.wings.android.zuboradiary.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.settings.DayOfWeekNameResIdGetter;
import com.websarva.wings.android.zuboradiary.data.settings.ThemeColors;
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding;
import com.websarva.wings.android.zuboradiary.data.settings.SettingsRepository;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorSwitcher;

import java.time.DayOfWeek;

public class SettingsFragment extends Fragment {

    // View関係
    private FragmentSettingsBinding binding;

    // Navigation関係
    private NavController navController;

    // ViewModel
    private SettingsViewModel settingsViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        SettingsRepository settingsRepository = new SettingsRepository(requireContext());
        SettingsViewModelFactory factory = new SettingsViewModelFactory(settingsRepository);
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        this.settingsViewModel = provider.get(SettingsViewModel.class);

        // Navigation設定
        this.navController = NavHostFragment.findNavController(this);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // ビューバインディング設定
        this.binding = FragmentSettingsBinding.inflate(inflater, container, false);

        // データバインディング設定
        this.binding.setLifecycleOwner(this);
        this.binding.setSettingsViewModel(this.settingsViewModel);

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // ダイアログフラグメントからの結果受取設定
        NavBackStackEntry navBackStackEntry =
                this.navController.getBackStackEntry(R.id.navigation_settings_fragment);
        SettingsFragmentLifecycleEventObserver lifecycleEventObserver =
                new SettingsFragmentLifecycleEventObserver(navBackStackEntry, this.settingsViewModel);
        navBackStackEntry.getLifecycle().addObserver(lifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                    // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                    //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    savedStateHandle.remove(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
                    savedStateHandle.remove(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });


        this.binding.textThemeColorSettingTitle.setOnClickListener(
                new OnClickListenerOfThemeColorSetting(this.navController, this.settingsViewModel)
        );
        this.binding.textCalendarStartDaySettingTitle.setOnClickListener(
                new OnClickListenerOfCalendarStartDayOfWeekSetting(this.navController, this.settingsViewModel)
        );
        this.binding.switchReminderNotificationValue.setOnCheckedChangeListener(
                new OnCheckedChangeListenerOfReminderNotificationSetting(this.navController, this.settingsViewModel)
        );
        this.binding.switchPasscodeLockValue.setOnCheckedChangeListener(
                new OnCheckedChangeListenerOfPasscodeLockSetting(this.navController, this.settingsViewModel)
        );
        this.binding.switchGettingWeatherInformationValue.setOnCheckedChangeListener(
                new OnCheckedChangeListenerOfGettingWeatherInformationSetting(this.navController, this.settingsViewModel)
        );

        this.settingsViewModel.getLiveDataThemeColor().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String string) {
                ThemeColorSwitcher switcher = new ThemeColorSwitcher(getResources(), requireContext());
                ThemeColors themeColor = ThemeColors.values()[0]; // デフォルトテーマ
                TextView[] sections = {
                        binding.textSettingsSectionDesign,
                        binding.textSettingsSectionSettings,
                        binding.textSettingsSectionEnd};
                TextView[] icons = {
                        binding.textThemeColorSettingTitle,
                        binding.textCalendarStartDaySettingTitle,
                        binding.textReminderNotificationSettingTitle,
                        binding.textGettingWeatherInformationSettingTitle};
                MaterialSwitch[] switches = {
                        binding.switchReminderNotificationValue,
                        binding.switchPasscodeLockValue,
                        binding.switchGettingWeatherInformationValue};
                if (string.equals(getString(ThemeColors.WHITE.getThemeColorNameResId()))) {
                    themeColor = ThemeColors.WHITE;
                }
                if (string.equals(getString(ThemeColors.BLACK.getThemeColorNameResId()))) {
                    themeColor = ThemeColors.BLACK;
                }
                switcher.switchSectionView(themeColor, sections);
                switcher.switchTextIcon(themeColor, icons);
                switcher.switchSwitch(themeColor, switches);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    private class SettingsViewModelFactory implements ViewModelProvider.Factory {
        private SettingsRepository repository;

        private SettingsViewModelFactory(SettingsRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
                return (T) new SettingsViewModel(this.repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        };
    }

    private class SettingsFragmentLifecycleEventObserver implements LifecycleEventObserver {
        private NavBackStackEntry navBackStackEntry;
        private SettingsViewModel settingsViewModel;
        public SettingsFragmentLifecycleEventObserver(
                NavBackStackEntry navBackStackEntry, SettingsViewModel settingsViewModel) {
            this.navBackStackEntry = navBackStackEntry;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onStateChanged(
                @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
            SavedStateHandle savedStateHandle = this.navBackStackEntry.getSavedStateHandle();
            if (event.equals(Lifecycle.Event.ON_RESUME)) {
                // テーマカラー設定ダイアログフラグメントから結果受取
                boolean containsThemeColorPickerDialogFragmentResults =
                        savedStateHandle.contains(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
                if (containsThemeColorPickerDialogFragmentResults) {
                    ThemeColors selectedThemeColor =
                            savedStateHandle.get(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
                    this.settingsViewModel.saveThemeColor(selectedThemeColor);
                    savedStateHandle.remove(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
                }

                // テーマカラー設定ダイアログフラグメントから結果受取
                boolean containsDayOfWeekPickerDialogFragmentResults =
                        savedStateHandle.contains(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
                if (containsDayOfWeekPickerDialogFragmentResults) {
                    DayOfWeek selectedDayOfWeek =
                            savedStateHandle.get(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
                    this.settingsViewModel.saveCalendarStartDayOfWeek(selectedDayOfWeek);
                    savedStateHandle.remove(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
                }
            }
        }
    }

    private class OnClickListenerOfThemeColorSetting implements View.OnClickListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnClickListenerOfThemeColorSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onClick(View v) {
            String currentThemeColorName =
                    this.settingsViewModel.getLiveDataThemeColor().getValue();
            ThemeColors currentThemeColor = toThemeColors(currentThemeColorName);
            if (currentThemeColor == null) {
                currentThemeColor = ThemeColors.values()[0];
            }
            NavDirections action = SettingsFragmentDirections
                    .actionNavigationSettingsFragmentToThemeColorPickerDialog(currentThemeColor);
            this.navController.navigate(action);
        }
    }

    private ThemeColors toThemeColors(String themeColorName) {
        if (themeColorName == null || themeColorName.isEmpty()) {
            return null;
        }
        for (ThemeColors themeColor: ThemeColors.values()) {
            int themeColorNameResId = themeColor.getThemeColorNameResId();
            String _themeColorName = getString(themeColorNameResId);
            if (themeColorName.equals(_themeColorName)) {
                return themeColor;
            }
        }
        return null;
    }

    private class OnClickListenerOfCalendarStartDayOfWeekSetting implements View.OnClickListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnClickListenerOfCalendarStartDayOfWeekSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onClick(View v) {
            String currentCalendarStartDayOfWeekName =
                    this.settingsViewModel.getLiveDataCalendarStartDayOfWeek().getValue();
            DayOfWeek currentCalendarStartDayOfWeek = toDayOfWeek(currentCalendarStartDayOfWeekName);
            if (currentCalendarStartDayOfWeek == null) {
                currentCalendarStartDayOfWeek = DayOfWeek.SUNDAY;
            }
            NavDirections action = SettingsFragmentDirections
                    .actionNavigationSettingsFragmentToDayOfWeekPickerDialog(currentCalendarStartDayOfWeek);
            this.navController.navigate(action);
        }
    }

    private DayOfWeek toDayOfWeek(String dayOfWeekName) {
        if (dayOfWeekName == null || dayOfWeekName.isEmpty()) {
            return null;
        }
        for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
            DayOfWeekNameResIdGetter dayOfWeekNameResIdGetter = new DayOfWeekNameResIdGetter();
            int dayOfWeekNameResId = dayOfWeekNameResIdGetter.getResId(dayOfWeek);
            String _dayOfWeekName = getString(dayOfWeekNameResId);
            if (dayOfWeekName.equals(_dayOfWeekName)) {
                return dayOfWeek;
            }
        }
        return null;
    }

    private class OnCheckedChangeListenerOfReminderNotificationSetting
            implements CompoundButton.OnCheckedChangeListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnCheckedChangeListenerOfReminderNotificationSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d("20240708", "onCheckedChanged");
            this.settingsViewModel.saveIsReminderNotification(isChecked);
        }
    }

    private class OnCheckedChangeListenerOfPasscodeLockSetting
            implements CompoundButton.OnCheckedChangeListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnCheckedChangeListenerOfPasscodeLockSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            this.settingsViewModel.saveIsPasscodeLock(isChecked);
        }
    }

    private class OnCheckedChangeListenerOfGettingWeatherInformationSetting
            implements CompoundButton.OnCheckedChangeListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnCheckedChangeListenerOfGettingWeatherInformationSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            this.settingsViewModel.saveIsGettingWeatherInformation(isChecked);
        }
    }

}
