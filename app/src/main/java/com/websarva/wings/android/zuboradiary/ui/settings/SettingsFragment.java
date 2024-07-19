package com.websarva.wings.android.zuboradiary.ui.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
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
import com.websarva.wings.android.zuboradiary.ui.ThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.ViewModelFactory;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

public class SettingsFragment extends Fragment {

    // View関係
    private FragmentSettingsBinding binding;
    private boolean isTouchedReminderNotificationSwitch = false;
    private boolean isTouchedPasscodeLockSwitch = false;
    private boolean isTouchedGettingWeatherInformationSwitch = false;

    // Navigation関係
    private NavController navController;

    // ViewModel関係
    private SettingsViewModel settingsViewModel;

    // ActivityResultLauncher関係
    private ActivityResultLauncher<String> requestPostNotificationsPermissionLauncher;
    private ActivityResultLauncher<String[]> requestAccessLocationPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelFactory factory =
                new ViewModelFactory(requireContext(), requireActivity().getApplication());
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        this.settingsViewModel = provider.get(SettingsViewModel.class);

        // Navigation設定
        this.navController = NavHostFragment.findNavController(this);

        // ActivityResultLauncher設定
        // 通知権限取得結果処理
        requestPostNotificationsPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        new ActivityResultCallback<Boolean>() {
                            @Override
                            public void onActivityResult(Boolean isGranted) {
                                if (isGranted && isGrantedPostNotifications()/*再確認*/) {
                                    NavDirections action = SettingsFragmentDirections
                                            .actionNavigationSettingsFragmentToTimePickerDialog();
                                    navController.navigate(action);
                                } else {
                                    binding.switchReminderNotificationValue.setChecked(false);
                                }
                            }
                        }
                );

        // 位置情報利用権限取得結果処理
        requestAccessLocationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        new ActivityResultCallback<Map<String, Boolean>>() {
                            @Override
                            public void onActivityResult(Map<String, Boolean> o) {
                                Boolean isGrantedAccessFineLocation =
                                        o.get(Manifest.permission.ACCESS_FINE_LOCATION);
                                Boolean isGrantedAccessCoarseLocation =
                                        o.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                                boolean isGrantedAll =
                                        isGrantedAccessFineLocation != null
                                                && isGrantedAccessFineLocation
                                                && isGrantedAccessCoarseLocation != null
                                                && isGrantedAccessCoarseLocation;
                                if (isGrantedAll && isGrantedAccessLocation()/*再確認*/) {
                                    settingsViewModel.saveIsGettingWeatherInformation(true);
                                } else {
                                    binding.switchGettingWeatherInformationValue.setChecked(false);
                                }
                            }
                        }
                );


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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // ダイアログフラグメントからの結果受取設定
        NavBackStackEntry navBackStackEntry =
                this.navController.getBackStackEntry(R.id.navigation_settings_fragment);
        NaviBackStackEntryLifecycleEventObserver naviBackStackEntryLifecycleEventObserver =
                new NaviBackStackEntryLifecycleEventObserver(navBackStackEntry);
        navBackStackEntry.getLifecycle().addObserver(naviBackStackEntryLifecycleEventObserver);
        viewLifecycleEventObserver viewLifecycleEventObserver =
                new viewLifecycleEventObserver(navBackStackEntry, naviBackStackEntryLifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle().addObserver(viewLifecycleEventObserver);


        this.binding.textThemeColorSettingTitle.setOnClickListener(
                new OnClickListenerOfThemeColorSetting(this.navController, this.settingsViewModel)
        );
        this.binding.textCalendarStartDaySettingTitle.setOnClickListener(
                new OnClickListenerOfCalendarStartDayOfWeekSetting(this.navController, this.settingsViewModel)
        );
        binding.switchReminderNotificationValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    isTouchedReminderNotificationSwitch = true;
                }
                return false;
            }
        });
        this.binding.switchReminderNotificationValue.setOnCheckedChangeListener(
                new OnCheckedChangeListenerOfReminderNotificationSetting(this.navController, this.settingsViewModel)
        );
        this.binding.switchPasscodeLockValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    isTouchedPasscodeLockSwitch = true;
                }
                return false;
            }
        });
        this.binding.switchPasscodeLockValue.setOnCheckedChangeListener(
                new OnCheckedChangeListenerOfPasscodeLockSetting(this.navController, this.settingsViewModel)
        );
        this.binding.switchGettingWeatherInformationValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    isTouchedGettingWeatherInformationSwitch = true;
                }
                return false;
            }
        });
        this.binding.switchGettingWeatherInformationValue.setOnCheckedChangeListener(
                new OnCheckedChangeListenerOfGettingWeatherInformationSetting(this.navController, this.settingsViewModel)
        );

        this.settingsViewModel.getThemeColorLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
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



    private class NaviBackStackEntryLifecycleEventObserver implements LifecycleEventObserver {
        private NavBackStackEntry navBackStackEntry;
        public NaviBackStackEntryLifecycleEventObserver(NavBackStackEntry navBackStackEntry) {
            this.navBackStackEntry = navBackStackEntry;
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
                    settingsViewModel.saveThemeColor(selectedThemeColor);
                    savedStateHandle.remove(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
                }

                // カレンダー開始曜日設定ダイアログフラグメントから結果受取
                boolean containsDayOfWeekPickerDialogFragmentResults =
                        savedStateHandle.contains(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
                if (containsDayOfWeekPickerDialogFragmentResults) {
                    DayOfWeek selectedDayOfWeek =
                            savedStateHandle.get(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
                    settingsViewModel.saveCalendarStartDayOfWeek(selectedDayOfWeek);
                    savedStateHandle.remove(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
                }

                // リマインダー通知時間設定ダイアログフラグメントから結果受取
                boolean containsTimePickerDialogFragmentResults =
                        savedStateHandle.contains(TimePickerDialogFragment.KEY_SELECTED_HOUR)
                        && savedStateHandle.contains(TimePickerDialogFragment.KEY_SELECTED_MINUTE);
                if (containsTimePickerDialogFragmentResults) {
                    Integer selectedHour =
                            savedStateHandle.get(TimePickerDialogFragment.KEY_SELECTED_HOUR);
                    Integer selectedMinute =
                            savedStateHandle.get(TimePickerDialogFragment.KEY_SELECTED_MINUTE);
                    if (selectedHour == null || selectedMinute == null) {
                        binding.switchReminderNotificationValue.setChecked(false);
                    } else {
                        LocalTime settingTime = LocalTime.of(selectedHour, selectedMinute);
                        settingsViewModel.registerReminderNotificationWorker(settingTime);
                        settingsViewModel.saveIsReminderNotification(true);
                    }
                    savedStateHandle.remove(TimePickerDialogFragment.KEY_SELECTED_HOUR);
                    savedStateHandle.remove(TimePickerDialogFragment.KEY_SELECTED_MINUTE);
                }

                // 権限催促ダイアログフラグメントから結果受取
                boolean containsPermissionDialogFragmentResult =
                        savedStateHandle.contains(PermissionDialogFragment.KEY_SELECTED_BUTTON);
                if (containsPermissionDialogFragmentResult) {
                    Integer selectedButton =
                            savedStateHandle.get(PermissionDialogFragment.KEY_SELECTED_BUTTON);
                    if (selectedButton != null && selectedButton == Dialog.BUTTON_POSITIVE) {
                        actionApplicationDetailsSettings();
                    }
                    savedStateHandle.remove(PermissionDialogFragment.KEY_SELECTED_BUTTON);
                }
            }
        }
    }

    private class viewLifecycleEventObserver implements LifecycleEventObserver {
        private NavBackStackEntry navBackStackEntry;
        private NaviBackStackEntryLifecycleEventObserver naviBackStackEntryLifecycleEventObserver;
        public viewLifecycleEventObserver(
                NavBackStackEntry navBackStackEntry,
                NaviBackStackEntryLifecycleEventObserver naviBackStackEntryLifecycleEventObserver) {
            this.navBackStackEntry = navBackStackEntry;
            this.naviBackStackEntryLifecycleEventObserver = naviBackStackEntryLifecycleEventObserver;
        }
        @Override
        public void onStateChanged(
                @NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                savedStateHandle.remove(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
                savedStateHandle.remove(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
                savedStateHandle.remove(TimePickerDialogFragment.KEY_SELECTED_HOUR);
                savedStateHandle.remove(TimePickerDialogFragment.KEY_SELECTED_MINUTE);
                savedStateHandle.remove(PermissionDialogFragment.KEY_SELECTED_BUTTON);
                navBackStackEntry.getLifecycle().removeObserver(naviBackStackEntryLifecycleEventObserver);
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
                    this.settingsViewModel.getThemeColorLiveData().getValue();
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
                    this.settingsViewModel.getCalendarStartDayOfWeekLiveData().getValue();
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
            if (!isTouchedReminderNotificationSwitch) {
                return;
            }
            if (isChecked) {
                if (isGrantedPostNotifications()) {
                    NavDirections action = SettingsFragmentDirections
                            .actionNavigationSettingsFragmentToTimePickerDialog();
                    navController.navigate(action);
                } else {
                    boolean shouldShowRequestPermissionRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(), Manifest.permission.POST_NOTIFICATIONS);
                    if (shouldShowRequestPermissionRationale) {
                        requestPostNotificationsPermissionLauncher
                                .launch(Manifest.permission.POST_NOTIFICATIONS);
                    } else {
                        binding.switchReminderNotificationValue.setChecked(false);
                        String permissionName = "通知";
                        NavDirections action =
                                SettingsFragmentDirections
                                        .actionSettingsFragmentToPermissionDialog(permissionName);
                        navController.navigate(action);
                    }
                }
            } else {
                settingsViewModel.cancelReminderNotificationWorker();
                settingsViewModel.saveIsReminderNotification(false);
            }
            isTouchedReminderNotificationSwitch = false;
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
            if (!isTouchedGettingWeatherInformationSwitch) {
                return;
            }
            if (isChecked) {
                if (isGrantedAccessLocation()) {
                    settingsViewModel.saveIsGettingWeatherInformation(true);
                } else {
                    boolean shouldShowRequestPermissionRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                    && ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (shouldShowRequestPermissionRationale) {
                        String[] requestPermissions =
                                {Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION};
                        requestAccessLocationPermissionLauncher.launch(requestPermissions);
                    } else {
                        binding.switchGettingWeatherInformationValue.setChecked(false);
                        String permissionName = "位置情報利用";
                        NavDirections action =
                                SettingsFragmentDirections
                                        .actionSettingsFragmentToPermissionDialog(permissionName);
                        navController.navigate(action);
                    }
                }
            } else {
                settingsViewModel.saveIsGettingWeatherInformation(false);
            }
            isTouchedGettingWeatherInformationSwitch = false;
        }
    }

    private boolean isGrantedPostNotifications() {
        return ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isGrantedAccessLocation() {
        boolean isGrantedAccessFineLocation =
                ActivityCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
        boolean isGrantedAccessCoarseLocation =
                ActivityCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
        return isGrantedAccessFineLocation && isGrantedAccessCoarseLocation;
    }

    private void actionApplicationDetailsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
