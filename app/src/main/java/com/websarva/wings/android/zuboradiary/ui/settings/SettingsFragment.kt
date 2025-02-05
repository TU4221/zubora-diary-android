package com.websarva.wings.android.zuboradiary.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.system.Os.remove
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.UriPermissionManager
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalTime

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    // View関係
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = checkNotNull(_binding)

    private var isTouchedReminderNotificationSwitch = false
    private var isTouchedPasscodeLockSwitch = false
    private var isTouchedWeatherInfoAcquisitionSwitch = false

    // ActivityResultLauncher関係
    private lateinit var requestPostNotificationsPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestAccessLocationPermissionLauncher: ActivityResultLauncher<Array<String>>

    // Uri関係
    private lateinit var uriPermissionManager: UriPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireMainActivity().popBackStackToStartFragment()
            }
        })

        // ActivityResultLauncher設定
        // 通知権限取得結果処理
        // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationsPermissionLauncher =
                registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->

                    // 再確認
                    val recheck = requireMainActivity().isGrantedPostNotifications
                    if (isGranted && recheck) {
                        showReminderNotificationTimePickerDialog()
                    } else {
                        binding.includePasscodeLockSetting
                            .materialSwitch.isChecked = false
                    }
                }
        }


        // 位置情報利用権限取得結果処理
        requestAccessLocationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { o: Map<String, Boolean> ->
                val isGrantedAccessFineLocation = o[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                val isGrantedAccessCoarseLocation = o[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

                val isGrantedAll = isGrantedAccessFineLocation && isGrantedAccessCoarseLocation

                // 再確認
                val recheck = requireMainActivity().isGrantedAccessLocation
                if (isGrantedAll && recheck) {
                    settingsViewModel.saveWeatherInfoAcquisition(true)
                } else {
                    binding.includeWeatherInfoAcquisitionSetting
                        .materialSwitch.isChecked = false
                }
            }

        uriPermissionManager =
            object : UriPermissionManager(requireContext()) {
                override fun checkUsedUriDoesNotExist(uri: Uri): Boolean {
                    return false // MEMO:本フラグメントではUri権限を個別に解放しないため常時false
                }
            }
    }

    override fun initializeViewModel() {
        // 処理なし
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentSettingsBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            binding.lifecycleOwner = this@SettingsFragment
            binding.settingsViewModel = this@SettingsFragment.settingsViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpThemeColorSettingItem()
        setUpCalendarStartDaySettingItem()
        setUpReminderNotificationSettingItem()
        setUpPasscodeLockSettingItem()
        setUpWeatherInfoAcquisitionSettingItem()
        setUpAllDiariesDeleteSettingItem()
        setUpAllSettingsInitializationSettingItem()
        setUpAllDataDeleteSettingItem()
    }

    override fun handleOnReceivingResultFromPreviousFragment(savedStateHandle: SavedStateHandle) {
        // 処理なし
    }

    override fun handleOnReceivingDialogResult(savedStateHandle: SavedStateHandle) {
        receiveThemeColorPickerDialogResult()
        receiveCalendarStartDayPickerDialogResult()
        receiveReminderNotificationTimePickerDialogResult()
        receivePermissionDialogResult()
        receiveAllDiariesDeleteDialogResult()
        receiveAllSettingsInitializationDialogResult()
        receiveAllDataDeleteDialogResult()
    }

    override fun removeDialogResultOnDestroy(savedStateHandle: SavedStateHandle) {
        savedStateHandle.apply {
            remove<Any>(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR)
            remove<Any>(CalendarStartDayPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK)
            remove<Any>(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_BUTTON)
            remove<Any>(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_TIME)
            remove<Any>(PermissionDialogFragment.KEY_SELECTED_BUTTON)
            remove<Any>(AllDiariesDeleteDialogFragment.KEY_SELECTED_BUTTON)
            remove<Any>(AllSettingsInitializationDialogFragment.KEY_SELECTED_BUTTON)
            remove<Any>(AllDataDeleteDialogFragment.KEY_SELECTED_BUTTON)
        }
    }

    override fun setUpOtherAppMessageDialog() {
        // 処理なし
    }

    // テーマカラー設定ダイアログフラグメントから結果受取
    private fun receiveThemeColorPickerDialogResult() {
        val selectedThemeColor =
            receiveResulFromDialog<ThemeColor>(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR)
                ?: return

        settingsViewModel.saveThemeColor(selectedThemeColor)
    }

    // カレンダー開始曜日設定ダイアログフラグメントから結果受取
    private fun receiveCalendarStartDayPickerDialogResult() {
        val selectedDayOfWeek =
            receiveResulFromDialog<DayOfWeek>(CalendarStartDayPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK)
                ?: return

        settingsViewModel.saveCalendarStartDayOfWeek(selectedDayOfWeek)
    }

    // リマインダー通知時間設定ダイアログフラグメントから結果受取
    private fun receiveReminderNotificationTimePickerDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) {
            binding.includeReminderNotificationSetting.materialSwitch.isChecked = false
            return
        }

        val selectedTime =
            checkNotNull(
                receiveResulFromDialog<LocalTime>(
                    ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_TIME
                )
            )
        settingsViewModel.saveReminderNotificationValid(selectedTime)
    }

    // 権限催促ダイアログフラグメントから結果受取
    private fun receivePermissionDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(PermissionDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        showApplicationDetailsSettings()
    }

    private fun receiveAllDiariesDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(AllDiariesDeleteDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        settingsViewModel.deleteAllDiaries()
        uriPermissionManager.releaseAllPersistablePermission()
    }

    private fun receiveAllSettingsInitializationDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(AllSettingsInitializationDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        settingsViewModel.deleteAllSettings()
    }

    private fun receiveAllDataDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(AllDataDeleteDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        settingsViewModel.deleteAllData()
        uriPermissionManager.releaseAllPersistablePermission()
    }

    private fun setUpThemeColorSettingItem() {
        binding.includeThemeColorSetting.textTitle.setOnClickListener {
            showThemeColorPickerDialog()
        }

        settingsViewModel.themeColor
            .observe(viewLifecycleOwner) { themeColor: ThemeColor? ->
                var settingValue = themeColor
                if (settingValue == null) {
                    settingValue = settingsViewModel.loadThemeColorSettingValue()
                }
                val strThemeColor = settingValue.toSting(requireContext())
                binding.includeThemeColorSetting.textValue.text = strThemeColor
                switchViewColor(settingValue)
            }
    }

    private fun switchViewColor(themeColor: ThemeColor) {
        val switcher =
            SettingsThemeColorSwitcher(requireContext(), themeColor)

        switcher.switchBackgroundColor(binding.viewFullScreenBackground)
        switcher.switchToolbarColor(binding.materialToolbarTopAppBar)

        switcher.switchSettingItemSectionColor(
            binding.run {
                listOf(
                    textSettingsSectionDesign,
                    textSettingsSectionSetting,
                    textSettingsSectionEnd,
                    textSettingsSectionData
                )
            }
        )

        switcher.switchSettingItemIconColor(
            binding.run {
                listOf(
                    includeThemeColorSetting.textTitle,
                    includeCalendarStartDaySetting.textTitle,
                    includeReminderNotificationSetting.textTitle,
                    includePasscodeLockSetting.textTitle,
                    includeWeatherInfoAcquisitionSetting.textTitle,
                    includeAllDiariesDeleteSetting.textTitle,
                    includeAllSettingsInitializationSetting.textTitle,
                    includeAllDataDeleteSetting.textTitle
                )
            }
        )

        switcher.switchTextColorOnBackground(
            binding.run {
                listOf(
                    includeThemeColorSetting.textTitle,
                    includeThemeColorSetting.textValue,
                    includeCalendarStartDaySetting.textTitle,
                    includeCalendarStartDaySetting.textValue,
                    includeReminderNotificationSetting.textTitle,
                    includeReminderNotificationSetting.textValue,
                    includePasscodeLockSetting.textTitle,
                    includeWeatherInfoAcquisitionSetting.textTitle
                )
            }
        )

        switcher.switchRedTextColorOnBackground(
            binding.run {
                listOf(
                    includeAllDiariesDeleteSetting.textTitle,
                    includeAllSettingsInitializationSetting.textTitle,
                    includeAllDataDeleteSetting.textTitle
                )
            }
        )

        switcher.switchSwitchColor(
            binding.run {
                listOf(
                    includeReminderNotificationSetting.materialSwitch,
                    includePasscodeLockSetting.materialSwitch,
                    includeWeatherInfoAcquisitionSetting.materialSwitch
                )
            }
        )

        switcher.switchDividerColor(
            binding.run {
                listOf(
                    materialDividerToolbar,
                    materialDividerThemeColorSetting,
                    materialDividerSectionSetting,
                    materialDividerCalendarStartDaySetting,
                    materialDividerReminderNotificationSetting,
                    materialDividerPasscodeLockSetting,
                    materialDividerWeatherInfoAcquisitionSetting,
                    materialDividerSectionData,
                    materialDividerAllDiariesDeleteSetting,
                    materialDividerAllSettingsInitializationSetting,
                    materialDividerAllDataDeleteSetting,
                    materialDividerSectionEnd
                )
            }
        )
    }

    private fun setUpCalendarStartDaySettingItem() {
        binding.includeCalendarStartDaySetting.textTitle.setOnClickListener {
            val currentCalendarStartDayOfWeek = settingsViewModel.loadCalendarStartDaySettingValue()
            showCalendarStartDayPickerDialog(currentCalendarStartDayOfWeek)
        }

        settingsViewModel.calendarStartDayOfWeek
            .observe(viewLifecycleOwner) { dayOfWeek: DayOfWeek? ->
                var settingValue = dayOfWeek
                if (settingValue == null) {
                    settingValue = settingsViewModel.loadCalendarStartDaySettingValue()
                }

                val stringConverter = DayOfWeekStringConverter(requireContext())
                val strDayOfWeek = stringConverter.toCalendarStartDayOfWeek(settingValue)
                binding.includeCalendarStartDaySetting.textValue.text = strDayOfWeek
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpReminderNotificationSettingItem() {
        binding.includeReminderNotificationSetting.materialSwitch
            .setOnTouchListener { _: View, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    isTouchedReminderNotificationSwitch = true
                }
                false
            }
        binding.includeReminderNotificationSetting.materialSwitch
            .setOnCheckedChangeListener(
                ReminderNotificationOnCheckedChangeListener()
            )

        settingsViewModel.isCheckedReminderNotification
            .observe(viewLifecycleOwner) { aBoolean: Boolean? ->
                var settingValue = aBoolean
                if (settingValue == null) {
                    settingValue = settingsViewModel.loadIsCheckedReminderNotificationSetting()
                }
                if (settingValue) {
                    binding.includeReminderNotificationSetting
                        .textValue.visibility = View.VISIBLE
                } else {
                    binding.includeReminderNotificationSetting
                        .textValue.visibility = View.INVISIBLE
                }
            }

        settingsViewModel.reminderNotificationTime
            .observe(viewLifecycleOwner) { time: LocalTime? ->
                // MEMO:未設定の場合nullが代入される。
                //      その為、nullはエラーではないので下記メソッドの処理は不要(処理するとループする)
                //      "SettingsViewModel#isCheckedReminderNotificationSetting()"
                if (time == null) {
                    binding.includeReminderNotificationSetting.textValue.text = ""
                    return@observe
                }

                val converter = DateTimeStringConverter()
                val timeString = converter.toHourMinute(time)
                binding.includeReminderNotificationSetting.textValue.text = timeString
            }
    }

    private inner class ReminderNotificationOnCheckedChangeListener

        : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            if (!isTouchedReminderNotificationSwitch) return

            if (isChecked) {
                // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPostNotificationsPermission()
                } else {
                    showReminderNotificationTimePickerDialog()
                }
            } else {
                settingsViewModel.saveReminderNotificationInvalid()
            }
            isTouchedReminderNotificationSwitch = false
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        fun requestPostNotificationsPermission() {
            val isGranted = requireMainActivity().isGrantedPostNotifications
            if (isGranted) {
                showReminderNotificationTimePickerDialog()
            } else {
                val shouldShowRequestPermissionRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), Manifest.permission.POST_NOTIFICATIONS
                    )
                if (shouldShowRequestPermissionRationale) {
                    requestPostNotificationsPermissionLauncher
                        .launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    binding.includePasscodeLockSetting.materialSwitch.isChecked = false
                    val permissionName =
                        getString(R.string.fragment_settings_permission_name_notification)
                    showPermissionDialog(permissionName)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPasscodeLockSettingItem() {
        binding.includePasscodeLockSetting.materialSwitch
            .setOnTouchListener { _: View, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    isTouchedPasscodeLockSwitch = true
                }
                false
            }
        binding.includePasscodeLockSetting.materialSwitch
            .setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (!isTouchedPasscodeLockSwitch) return@setOnCheckedChangeListener

                settingsViewModel.savePasscodeLock(isChecked)
                isTouchedPasscodeLockSwitch = false
            }

        settingsViewModel.isCheckedPasscodeLock
            .observe(viewLifecycleOwner) { }
    }

    // HACK:WeatherInfoAcquisitionSettingのMaterialSwitchがOn状態(SettingViewModelのisCheckedLiveDataが"true")だと、
    //      本Fragment起動時に他のMaterialSwitchのOnCheckedChangeListenerがOn状態("true")で起動してしまう。
    //      (他のMaterialSwitchがOn状態でも本問題は起きない)
    //      SettingViewModelの対象isCheckedLiveDataは"false"かつ、
    //      OnCheckedChangeListenerはユーザーがタッチした時に限り処理されるよう条件が入っている為、問題は発生していない。
    //      原因は不明。(Fragment、layout.xmlでのMaterialSwitchの設定に問題なし)
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpWeatherInfoAcquisitionSettingItem() {
        // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
        val isGranted = requireMainActivity().isGrantedAccessLocation
        if (!isGranted) {
            settingsViewModel.saveWeatherInfoAcquisition(false)
        }

        binding.includeWeatherInfoAcquisitionSetting.materialSwitch
            .setOnTouchListener { _: View, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    isTouchedWeatherInfoAcquisitionSwitch = true
                }
                false
            }

        binding.includeWeatherInfoAcquisitionSetting.materialSwitch
            .setOnCheckedChangeListener(
                WeatherInfoAcquisitionOnCheckedChangeListener()
            )
    }

    private inner class WeatherInfoAcquisitionOnCheckedChangeListener

        : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            if (!isTouchedWeatherInfoAcquisitionSwitch) return

            if (isChecked) {
                val isGranted = requireMainActivity().isGrantedAccessLocation
                if (isGranted) {
                    settingsViewModel.saveWeatherInfoAcquisition(true)
                } else {
                    binding.includeWeatherInfoAcquisitionSetting.materialSwitch.isChecked = false
                    val shouldShowRequestPermissionRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
                        )
                                && ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    if (shouldShowRequestPermissionRationale) {
                        val requestPermissions =
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        requestAccessLocationPermissionLauncher.launch(requestPermissions)
                    } else {
                        binding.includeWeatherInfoAcquisitionSetting.materialSwitch.isChecked =
                            false
                        val permissionName =
                            getString(R.string.fragment_settings_permission_name_location)
                        showPermissionDialog(permissionName)
                    }
                }
            } else {
                settingsViewModel.saveWeatherInfoAcquisition(false)
            }
            isTouchedWeatherInfoAcquisitionSwitch = false
        }
    }

    private fun setUpAllDiariesDeleteSettingItem() {
        binding.includeAllDiariesDeleteSetting.apply {
            textTitle.setOnClickListener {
                showAllDiariesDeleteDialog()
            }
            textValue.visibility = View.GONE
        }
    }

    private fun setUpAllSettingsInitializationSettingItem() {
        binding.includeAllSettingsInitializationSetting.apply {
            textTitle.setOnClickListener {
                showAllSettingsInitializationDialog()
            }
            textValue.visibility = View.GONE
        }
    }

    private fun setUpAllDataDeleteSettingItem() {
        binding.includeAllDataDeleteSetting.apply {
            textTitle.setOnClickListener {
                showAllDataDeleteDialog()
            }
            textValue.visibility = View.GONE
        }
    }

    private fun showThemeColorPickerDialog() {
        if (isDialogShowing()) return

        val action =
            SettingsFragmentDirections
                .actionNavigationSettingsFragmentToThemeColorPickerDialog()
        navController.navigate(action)
    }

    private fun showCalendarStartDayPickerDialog(dayOfWeek: DayOfWeek) {
        if (isDialogShowing()) return

        val action: NavDirections =
            SettingsFragmentDirections
                .actionNavigationSettingsFragmentToCalendarStartDayPickerDialog(dayOfWeek)
        navController.navigate(action)
    }

    private fun showReminderNotificationTimePickerDialog() {
        if (isDialogShowing()) return

        val action =
            SettingsFragmentDirections
                .actionNavigationSettingsFragmentToReminderNotificationTimePickerDialog()
        navController.navigate(action)
    }

    private fun showPermissionDialog(permissionName: String) {
        if (isDialogShowing()) return

        val action: NavDirections =
            SettingsFragmentDirections
                .actionSettingsFragmentToPermissionDialog(permissionName)
        navController.navigate(action)
    }

    private fun showAllDiariesDeleteDialog() {
        if (isDialogShowing()) return

        val action =
            SettingsFragmentDirections
                .actionSettingsFragmentToAllDiariesDeleteDialog()
        navController.navigate(action)
    }

    private fun showAllSettingsInitializationDialog() {
        if (isDialogShowing()) return

        val action =
            SettingsFragmentDirections
                .actionSettingsFragmentToAllSettingsInitializationDialog()
        navController.navigate(action)
    }

    private fun showAllDataDeleteDialog() {
        if (isDialogShowing()) return

        val action =
            SettingsFragmentDirections
                .actionSettingsFragmentToAllDataDeleteDialog()
        navController.navigate(action)
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val action: NavDirections =
            SettingsFragmentDirections
                .actionSettingsFragmentToAppMessageDialog(appMessage)
        navController.navigate(action)
    }

    override fun retryOtherAppMessageDialogShow() {
        // 処理なし
    }

    private fun showApplicationDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.setData(uri)
        startActivity(intent)
        onStart()
    }

    override fun destroyBinding() {
        _binding = null
    }
}
