package com.websarva.wings.android.zuboradiary.ui.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavDirections;

import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends BaseFragment {

    // View関係
    private FragmentSettingsBinding binding;
    private boolean isTouchedReminderNotificationSwitch = false;
    private boolean isTouchedPasscodeLockSwitch = false;
    private boolean isTouchedWeatherInfoAcquisitionSwitch = false;

    // ActivityResultLauncher関係
    private ActivityResultLauncher<String> requestPostNotificationsPermissionLauncher;
    private ActivityResultLauncher<String[]> requestAccessLocationPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addOnBackPressedCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireMainActivity().popBackStackToStartFragment();
            }
        });

        // ActivityResultLauncher設定
        // 通知権限取得結果処理
        // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationsPermissionLauncher =
                    registerForActivityResult(
                            new ActivityResultContracts.RequestPermission(),
                            new ActivityResultCallback<Boolean>() {
                                @Override
                                public void onActivityResult(Boolean isGranted) {

                                    // 再確認
                                    boolean _isGranted = ((MainActivity) requireActivity()).isGrantedPostNotifications();

                                    if (isGranted && _isGranted) {
                                        showReminderNotificationTimePickerDialog();
                                    } else {
                                        binding.includePasscodeLockSetting
                                                .materialSwitch.setChecked(false);
                                    }
                                }
                            }
                    );
        }


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

                                // 再確認
                                boolean _isGranted = ((MainActivity)requireActivity()).isGrantedAccessLocation();

                                if (isGrantedAll && _isGranted) {
                                    settingsViewModel.saveWeatherInfoAcquisition(true);
                                } else {
                                    binding.includeWeatherInfoAcquisitionSetting
                                            .materialSwitch.setChecked(false);
                                }
                            }
                        }
                );
    }

    @Override
    protected void initializeViewModel() {
        // 処理なし
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected ViewDataBinding initializeDataBinding(@NonNull LayoutInflater themeColorInflater, @NonNull ViewGroup container) {
        binding = FragmentSettingsBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setSettingsViewModel(settingsViewModel);
        return binding;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpThemeColorSettingItem();
        setUpCalendarStartDaySettingItem();
        setUpReminderNotificationSettingItem();
        setUpPasscodeLockSettingItem();
        setUpWeatherInfoAcquisitionSettingItem();
        setUpAllDiariesDeleteSettingItem();
        setUpAllSettingsInitializationSettingItem();
        setUpAllDataDeleteSettingItem();
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        receiveThemeColorPickerDialogResult();
        receiveCalendarStartDayPickerDialogResult();
        receiveReminderNotificationTimePickerDialogResult();
        receivePermissionDialogResult();
        receiveAllDiariesDeleteDialogResult();
        receiveAllSettingsInitializationDialogResult();
        receiveAllDataDeleteDialogResult();
    }

    @Override
    protected void removeDialogResultOnDestroy(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
        savedStateHandle.remove(CalendarStartDayPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
        savedStateHandle.remove(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_TIME);
        savedStateHandle.remove(PermissionDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(AllDiariesDeleteDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(AllSettingsInitializationDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(AllDataDeleteDialogFragment.KEY_SELECTED_BUTTON);
    }

    @Override
    protected void setUpOtherAppMessageDialog() {
        // 処理なし
    }

    // テーマカラー設定ダイアログフラグメントから結果受取
    private void receiveThemeColorPickerDialogResult() {
        ThemeColor selectedThemeColor =
                receiveResulFromDialog(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
        if (selectedThemeColor == null) return;

        settingsViewModel.saveThemeColor(selectedThemeColor);
    }

    // カレンダー開始曜日設定ダイアログフラグメントから結果受取
    private void receiveCalendarStartDayPickerDialogResult() {
        DayOfWeek selectedDayOfWeek =
                receiveResulFromDialog(CalendarStartDayPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
        if (selectedDayOfWeek == null) return;

        settingsViewModel.saveCalendarStartDayOfWeek(selectedDayOfWeek);
    }

    // リマインダー通知時間設定ダイアログフラグメントから結果受取
    private void receiveReminderNotificationTimePickerDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) {
            binding.includeReminderNotificationSetting.materialSwitch.setChecked(false);
            return;
        }

        LocalTime selectedTime =
                receiveResulFromDialog(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_TIME);
        Objects.requireNonNull(selectedTime);
        settingsViewModel.saveReminderNotificationValid(selectedTime);
    }

    // 権限催促ダイアログフラグメントから結果受取
    private void receivePermissionDialogResult() {
        Integer selectedButton = receiveResulFromDialog(PermissionDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != Dialog.BUTTON_POSITIVE) return;

        showApplicationDetailsSettings();
    }

    private void receiveAllDiariesDeleteDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(AllDiariesDeleteDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != Dialog.BUTTON_POSITIVE) return;

        settingsViewModel.deleteAllDiaries();
    }

    private void receiveAllSettingsInitializationDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(AllSettingsInitializationDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != Dialog.BUTTON_POSITIVE) return;

        settingsViewModel.deleteAllSettings();
    }

    private void receiveAllDataDeleteDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(AllDataDeleteDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != Dialog.BUTTON_POSITIVE) return;

        settingsViewModel.deleteAllData();
    }

    private void setUpThemeColorSettingItem() {
        binding.includeThemeColorSetting.textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeColorPickerDialog();
            }
        });

        settingsViewModel.getThemeColorSettingValueLiveData()
                .observe(getViewLifecycleOwner(), new Observer<ThemeColor>() {
                    @Override
                    public void onChanged(ThemeColor themeColor) {
                        Objects.requireNonNull(themeColor);

                        String strThemeColor = themeColor.toSting(requireContext());
                        binding.includeThemeColorSetting.textValue.setText(strThemeColor);
                        switchViewColor(themeColor);
                    }
                });
    }

    private void switchViewColor(ThemeColor themeColor) {
        Objects.requireNonNull(themeColor);

        SettingsThemeColorSwitcher switcher =
                new SettingsThemeColorSwitcher(requireContext(), themeColor);

        switcher.switchBackgroundColor(binding.viewFullScreenBackground);
        switcher.switchToolbarColor(binding.materialToolbarTopAppBar);

        switcher.switchSettingItemSectionColor(
                Arrays.asList(
                        binding.textSettingsSectionDesign,
                        binding.textSettingsSectionSetting,
                        binding.textSettingsSectionEnd,
                        binding.textSettingsSectionData
                )
        );

        switcher.switchSettingItemIconColor(
                Arrays.asList(
                        binding.includeThemeColorSetting.textTitle,
                        binding.includeCalendarStartDaySetting.textTitle,
                        binding.includeReminderNotificationSetting.textTitle,
                        binding.includePasscodeLockSetting.textTitle,
                        binding.includeWeatherInfoAcquisitionSetting.textTitle,
                        binding.includeAllDiariesDeleteSetting.textTitle,
                        binding.includeAllSettingsInitializationSetting.textTitle,
                        binding.includeAllDataDeleteSetting.textTitle
                )
        );

        switcher.switchTextColorOnBackground(
                Arrays.asList(
                        binding.includeThemeColorSetting.textTitle,
                        binding.includeThemeColorSetting.textValue,
                        binding.includeCalendarStartDaySetting.textTitle,
                        binding.includeCalendarStartDaySetting.textValue,
                        binding.includeReminderNotificationSetting.textTitle,
                        binding.includeReminderNotificationSetting.textValue,
                        binding.includePasscodeLockSetting.textTitle,
                        binding.includeWeatherInfoAcquisitionSetting.textTitle
                )
        );

        switcher.switchRedTextColorOnBackground(
                Arrays.asList(
                        binding.includeAllDiariesDeleteSetting.textTitle,
                        binding.includeAllSettingsInitializationSetting.textTitle,
                        binding.includeAllDataDeleteSetting.textTitle
                )
        );

        switcher.switchSwitchColor(
                Arrays.asList(
                        binding.includeReminderNotificationSetting.materialSwitch,
                        binding.includePasscodeLockSetting.materialSwitch,
                        binding.includeWeatherInfoAcquisitionSetting.materialSwitch
                )
        );

        switcher.switchDividerColor(
                Arrays.asList(
                        binding.materialDividerToolbar,
                        binding.materialDividerThemeColorSetting,
                        binding.materialDividerSectionSetting,
                        binding.materialDividerCalendarStartDaySetting,
                        binding.materialDividerReminderNotificationSetting,
                        binding.materialDividerPasscodeLockSetting,
                        binding.materialDividerWeatherInfoAcquisitionSetting,
                        binding.materialDividerSectionData,
                        binding.materialDividerAllDiariesDeleteSetting,
                        binding.materialDividerAllSettingsInitializationSetting,
                        binding.materialDividerAllDataDeleteSetting,
                        binding.materialDividerSectionEnd
                )
        );
    }

    private void setUpCalendarStartDaySettingItem() {
        binding.includeCalendarStartDaySetting.textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                DayOfWeek currentCalendarStartDayOfWeek =
                        settingsViewModel.loadCalendarStartDaySettingValue();
                Objects.requireNonNull(currentCalendarStartDayOfWeek);

                showCalendarStartDayPickerDialog(currentCalendarStartDayOfWeek);
            }
        });

        settingsViewModel.getCalendarStartDayOfWeekLiveData()
                .observe(getViewLifecycleOwner(), new Observer<DayOfWeek>() {
                    @Override
                    public void onChanged(DayOfWeek dayOfWeek) {
                        DayOfWeek settingValue = dayOfWeek;
                        if (settingValue == null) {
                            settingValue = settingsViewModel.loadCalendarStartDaySettingValue();
                        }

                        DayOfWeekStringConverter stringConverter =
                                new DayOfWeekStringConverter(requireContext());
                        String strDayOfWeek =
                                stringConverter.toCalendarStartDayOfWeek(settingValue);
                        binding.includeCalendarStartDaySetting.textValue.setText(strDayOfWeek);
                    }
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpReminderNotificationSettingItem() {

        binding.includeReminderNotificationSetting.materialSwitch
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Objects.requireNonNull(v);
                        Objects.requireNonNull(event);

                        if (event.getAction() == MotionEvent.ACTION_DOWN){
                            isTouchedReminderNotificationSwitch = true;
                        }
                        return false;
                    }
                });
        binding.includeReminderNotificationSetting.materialSwitch
                .setOnCheckedChangeListener(
                        new ReminderNotificationOnCheckedChangeListener()
                );

        settingsViewModel.getIsCheckedReminderNotificationLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        Boolean settingValue = aBoolean;
                        if (settingValue == null) {
                            settingValue = settingsViewModel.isCheckedReminderNotificationSetting();
                        }

                        if (settingValue) {
                            binding.includeReminderNotificationSetting
                                    .textValue.setVisibility(View.VISIBLE);
                        } else {
                            binding.includeReminderNotificationSetting
                                    .textValue.setVisibility(View.INVISIBLE);
                        }
                    }
                });

        settingsViewModel.getReminderNotificationTimeLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalTime>() {
                    @Override
                    public void onChanged(@Nullable LocalTime time) {
                        // MEMO:未設定の場合nullが代入される。
                        //      その為、nullはエラーではないので下記メソッドの処理は不要(処理するとループする)
                        //      "SettingsViewModel#isCheckedReminderNotificationSetting()"
                        if (time == null) {
                            binding.includeReminderNotificationSetting.textValue.setText("");
                            return;
                        }

                        DateTimeStringConverter converter = new DateTimeStringConverter();
                        String strTime = converter.toHourMinute(time);
                        binding.includeReminderNotificationSetting.textValue.setText(strTime);
                    }
                });
    }

    private class ReminderNotificationOnCheckedChangeListener
            implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Objects.requireNonNull(buttonView);
            if (!isTouchedReminderNotificationSwitch) return;

            if (isChecked) {
                // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPostNotificationsPermission();
                } else {
                    showReminderNotificationTimePickerDialog();
                }
            } else {
                settingsViewModel.saveReminderNotificationInvalid();
            }
            isTouchedReminderNotificationSwitch = false;
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        private void requestPostNotificationsPermission() {
            boolean isGranted = ((MainActivity)requireActivity()).isGrantedPostNotifications();
            if (isGranted) {
                showReminderNotificationTimePickerDialog();
            } else {
                boolean shouldShowRequestPermissionRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(), Manifest.permission.POST_NOTIFICATIONS);
                if (shouldShowRequestPermissionRationale) {
                    requestPostNotificationsPermissionLauncher
                            .launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    binding.includePasscodeLockSetting.materialSwitch.setChecked(false);
                    String permissionName = getString(R.string.fragment_settings_permission_name_notification);
                    showPermissionDialog(permissionName);
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpPasscodeLockSettingItem() {
        binding.includePasscodeLockSetting.materialSwitch
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Objects.requireNonNull(v);
                        Objects.requireNonNull(event);

                        if (event.getAction() == MotionEvent.ACTION_DOWN){
                            isTouchedPasscodeLockSwitch = true;
                        }
                        return false;
                    }
                });
        binding.includePasscodeLockSetting.materialSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Objects.requireNonNull(buttonView);
                        if (!isTouchedPasscodeLockSwitch) return;

                        settingsViewModel.savePasscodeLock(isChecked);
                        isTouchedPasscodeLockSwitch = false;
                    }
                });

        settingsViewModel.getIsCheckedPasscodeLockLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                    }
                });
    }

    // HACK:WeatherInfoAcquisitionSettingのMaterialSwitchがOn状態(SettingViewModelのisCheckedLiveDataが"true")だと、
    //      本Fragment起動時に他のMaterialSwitchのOnCheckedChangeListenerがOn状態("true")で起動してしまう。
    //      (他のMaterialSwitchがOn状態でも本問題は起きない)
    //      SettingViewModelの対象isCheckedLiveDataは"false"かつ、
    //      OnCheckedChangeListenerはユーザーがタッチした時に限り処理されるよう条件が入っている為、問題は発生していない。
    //      原因は不明。(Fragment、layout.xmlでのMaterialSwitchの設定に問題なし)
    @SuppressLint("ClickableViewAccessibility")
    private void setUpWeatherInfoAcquisitionSettingItem() {
        // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
        boolean isGranted = ((MainActivity)requireActivity()).isGrantedAccessLocation();
        if (!isGranted) {
            settingsViewModel.saveWeatherInfoAcquisition(false);
        }

        binding.includeWeatherInfoAcquisitionSetting.materialSwitch
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Objects.requireNonNull(v);
                        Objects.requireNonNull(event);

                        if (event.getAction() == MotionEvent.ACTION_DOWN){
                            isTouchedWeatherInfoAcquisitionSwitch = true;
                        }
                        return false;
                    }
                });

        binding.includeWeatherInfoAcquisitionSetting.materialSwitch
                .setOnCheckedChangeListener(
                        new WeatherInfoAcquisitionOnCheckedChangeListener()
                );
    }

    private class WeatherInfoAcquisitionOnCheckedChangeListener
            implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Objects.requireNonNull(buttonView);
            if (!isTouchedWeatherInfoAcquisitionSwitch) return;

            if (isChecked) {
                boolean isGranted = ((MainActivity)requireActivity()).isGrantedAccessLocation();
                if (isGranted) {
                    settingsViewModel.saveWeatherInfoAcquisition(true);
                } else {
                    binding.includeWeatherInfoAcquisitionSetting.materialSwitch.setChecked(false);
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
                        binding.includeWeatherInfoAcquisitionSetting.materialSwitch.setChecked(false);
                        String permissionName = getString(R.string.fragment_settings_permission_name_location);
                        showPermissionDialog(permissionName);
                    }
                }
            } else {
                settingsViewModel.saveWeatherInfoAcquisition(false);
            }
            isTouchedWeatherInfoAcquisitionSwitch = false;
        }
    }

    private void setUpAllDiariesDeleteSettingItem() {
        binding.includeAllDiariesDeleteSetting.textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                showAllDiariesDeleteDialog();
            }
        });

        binding.includeAllDiariesDeleteSetting.textValue.setVisibility(View.GONE);
    }

    private void setUpAllSettingsInitializationSettingItem() {
        binding.includeAllSettingsInitializationSetting.textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                showAllSettingsInitializationDialog();
            }
        });

        binding.includeAllSettingsInitializationSetting.textValue.setVisibility(View.GONE);
    }

    private void setUpAllDataDeleteSettingItem() {
        binding.includeAllDataDeleteSetting.textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                showAllDataDeleteDialog();
            }
        });

        binding.includeAllDataDeleteSetting.textValue.setVisibility(View.GONE);
    }

    private void showThemeColorPickerDialog() {
        if (!canShowFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionNavigationSettingsFragmentToThemeColorPickerDialog();
        navController.navigate(action);
    }

    private void showCalendarStartDayPickerDialog(DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek);
        if (!canShowFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionNavigationSettingsFragmentToCalendarStartDayPickerDialog(dayOfWeek);
        navController.navigate(action);
    }

    private void showReminderNotificationTimePickerDialog() {
        if (!canShowFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionNavigationSettingsFragmentToReminderNotificationTimePickerDialog();
        navController.navigate(action);
    }

    private void showPermissionDialog(String permissionName) {
        Objects.requireNonNull(permissionName);
        if (!canShowFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToPermissionDialog(permissionName);
        navController.navigate(action);
    }

    private void showAllDiariesDeleteDialog() {
        if (!canShowFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToAllDiariesDeleteDialog();
        navController.navigate(action);
    }

    private void showAllSettingsInitializationDialog() {
        if (!canShowFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToAllSettingsInitializationDialog();
        navController.navigate(action);
    }

    private void showAllDataDeleteDialog() {
        if (!canShowFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToAllDataDeleteDialog();
        navController.navigate(action);
    }

    @Override
    protected void navigateAppMessageDialog(@NonNull AppMessage appMessage) {
        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToAppMessageDialog(appMessage);
        navController.navigate(action);
    }

    @Override
    protected void retryOtherAppMessageDialogShow() {
        // 処理なし
    }

    private void showApplicationDetailsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);onStart();
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
