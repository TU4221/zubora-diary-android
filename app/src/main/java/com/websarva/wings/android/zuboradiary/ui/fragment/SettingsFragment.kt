package com.websarva.wings.android.zuboradiary.ui.fragment

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.appmessage.AppMessage
import com.websarva.wings.android.zuboradiary.ui.utils.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.ui.utils.DayOfWeekStringConverter
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding
import com.websarva.wings.android.zuboradiary.ui.utils.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.requireValue
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.AllDataDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.AllDiariesDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.AllSettingsInitializationDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.CalendarStartDayPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.PermissionDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.ReminderNotificationTimePickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsThemeColorSwitcher
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.ThemeColorPickerDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    // View関係
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    // MEMO:本FragmentのMainViewModelはSettingsViewModelになる為、BaseFragmentのSettingsViewModel変数を代入。
    override val mainViewModel = settingsViewModel

    // ActivityResultLauncher関係
    private lateinit var requestPostNotificationsPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestAccessLocationPermissionLauncher: ActivityResultLauncher<Array<String>>

    // Uri関係
    private lateinit var uriPermissionManager: UriPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mainActivity.popBackStackToStartFragment()
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
                    val recheck = mainActivity.isGrantedPostNotifications
                    if (isGranted && recheck) {
                        showReminderNotificationTimePickerDialog()
                    } else {
                        binding.includeReminderNotificationSetting.materialSwitch.isChecked = false
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
                val recheck = mainActivity.isGrantedAccessLocation
                if (isGrantedAll && recheck) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        settingsViewModel.saveWeatherInfoAcquisition(true)
                    }
                } else {
                    binding.includeWeatherInfoAcquisitionSetting
                        .materialSwitch.isChecked = false
                }
            }

        uriPermissionManager =
            object : UriPermissionManager(requireContext()) {
                override suspend fun checkUsedUriDoesNotExist(uri: Uri): Boolean {
                    return false // MEMO:本フラグメントではUri権限を個別に解放しないため常時false
                }
            }
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentSettingsBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@SettingsFragment.viewLifecycleOwner
            settingsViewModel = this@SettingsFragment.settingsViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpScrollPosition()
        setUpThemeColorSettingItem()
        setUpCalendarStartDaySettingItem()
        setUpReminderNotificationSettingItem()
        setUpPasscodeLockSettingItem()
        setUpWeatherInfoAcquisitionSettingItem()
        setUpAllDiariesDeleteSettingItem()
        setUpAllSettingsInitializationSettingItem()
        setUpAllDataDeleteSettingItem()
        setUpOpenSourceLicensesSettingItem()
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // 処理なし
    }

    override fun receiveDialogResults() {
        receiveThemeColorPickerDialogResult()
        receiveCalendarStartDayPickerDialogResult()
        receiveReminderNotificationTimePickerDialogResult()
        receivePermissionDialogResult()
        receiveAllDiariesDeleteDialogResult()
        receiveAllSettingsInitializationDialogResult()
        receiveAllDataDeleteDialogResult()
    }

    override fun removeDialogResults() {
        removeResulFromFragment(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR)
        removeResulFromFragment(CalendarStartDayPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK)
        removeResulFromFragment(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_TIME)
        removeResulFromFragment(PermissionDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(AllDiariesDeleteDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(AllSettingsInitializationDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(AllDataDeleteDialogFragment.KEY_SELECTED_BUTTON)
    }

    // テーマカラー設定ダイアログフラグメントから結果受取
    private fun receiveThemeColorPickerDialogResult() {
        val selectedThemeColor =
            receiveResulFromDialog<ThemeColor>(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR)
                ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            settingsViewModel.saveThemeColor(selectedThemeColor)
        }
    }

    // カレンダー開始曜日設定ダイアログフラグメントから結果受取
    private fun receiveCalendarStartDayPickerDialogResult() {
        val selectedDayOfWeek =
            receiveResulFromDialog<DayOfWeek>(CalendarStartDayPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK)
                ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            settingsViewModel.saveCalendarStartDayOfWeek(selectedDayOfWeek)
        }
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

        lifecycleScope.launch(Dispatchers.IO) {
            settingsViewModel.saveReminderNotificationValid(selectedTime)
        }
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

        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = settingsViewModel.deleteAllDiaries()
            if (isSuccessful) uriPermissionManager.releaseAllPersistablePermission()
        }
    }

    private fun receiveAllSettingsInitializationDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(AllSettingsInitializationDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        lifecycleScope.launch(Dispatchers.IO) {
            settingsViewModel.initializeAllSettings()
        }
    }

    private fun receiveAllDataDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(AllDataDeleteDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = settingsViewModel.deleteAllData()
            if (isSuccessful) uriPermissionManager.releaseAllPersistablePermission()
        }
    }

    private fun setUpScrollPosition() {
        binding.scrollViewSettings.scrollY = settingsViewModel.scrollPositionY
    }

    private fun setUpThemeColorSettingItem() {
        binding.includeThemeColorSetting.textTitle.setOnClickListener {
            showThemeColorPickerDialog()
        }

        launchAndRepeatOnViewLifeCycleStarted {
            settingsViewModel.themeColor
                .collectLatest { value: ThemeColor? ->
                    value ?: return@collectLatest

                    val strThemeColor = value.toSting(requireContext())
                    binding.includeThemeColorSetting.textValue.text = strThemeColor
                    switchViewColor(value)
                }
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
                    textSettingsSectionData,
                    textSettingsSectionOther
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
                    includeAllDataDeleteSetting.textTitle,
                    includeOpenSourceLicensesSetting.textTitle
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
                    includeWeatherInfoAcquisitionSetting.textTitle,
                    includeOpenSourceLicensesSetting.textTitle
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
                    materialDividerSectionOther,
                    materialDividerOpenSourceLicensesSetting,
                    materialDividerSectionEnd
                )
            }
        )
    }

    private fun setUpCalendarStartDaySettingItem() {
        binding.includeCalendarStartDaySetting.textTitle.setOnClickListener {
            val currentCalendarStartDayOfWeek =
                settingsViewModel.calendarStartDayOfWeek.requireValue()
            showCalendarStartDayPickerDialog(currentCalendarStartDayOfWeek)
        }

        launchAndRepeatOnViewLifeCycleStarted {
            settingsViewModel.calendarStartDayOfWeek
                .collectLatest { value: DayOfWeek? ->
                    value ?: return@collectLatest

                    val stringConverter = DayOfWeekStringConverter(requireContext())
                    val strDayOfWeek = stringConverter.toCalendarStartDayOfWeek(value)
                    binding.includeCalendarStartDaySetting.textValue.text = strDayOfWeek
                }
        }
    }

    private fun setUpReminderNotificationSettingItem() {
        binding.includeReminderNotificationSetting.materialSwitch
            .setOnCheckedChangeListener(
                ReminderNotificationOnCheckedChangeListener()
            )

        launchAndRepeatOnViewLifeCycleStarted {
            settingsViewModel.isCheckedReminderNotification
                .collectLatest { value: Boolean? ->
                    value ?: return@collectLatest

                    binding.includeReminderNotificationSetting.textValue.visibility =
                        if (value) {
                            View.VISIBLE
                        } else {
                            View.INVISIBLE
                        }
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            settingsViewModel.reminderNotificationTime
                .collectLatest { value: LocalTime? ->
                    // MEMO:未設定の場合nullが代入される。
                    //      その為、nullはエラーではないので下記メソッドの処理は不要(処理するとループする)
                    //      "SettingsViewModel#isCheckedReminderNotificationSetting()"
                    if (value == null) {
                        binding.includeReminderNotificationSetting.textValue.text = ""
                        return@collectLatest
                    }

                    val converter = DateTimeStringConverter()
                    val timeString = converter.toHourMinute(value)
                    binding.includeReminderNotificationSetting.textValue.text = timeString
                }
        }
    }

    private inner class ReminderNotificationOnCheckedChangeListener
        : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                // DateStorePreferences初回読込時の値がtrueの場合、本メソッドが呼び出される。
                // 初回読込時は処理不要のため下記条件追加。
                val settingValue = settingsViewModel.isCheckedReminderNotification.requireValue()
                if (isChecked == settingValue) return

                if (isChecked) {
                    // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPostNotificationsPermission()
                    } else {
                        showReminderNotificationTimePickerDialog()
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        settingsViewModel.saveReminderNotificationInvalid()
                    }
                }
            }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        fun requestPostNotificationsPermission() {
            val isGranted = mainActivity.isGrantedPostNotifications
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
                    binding.includeReminderNotificationSetting.materialSwitch.isChecked = false
                    val permissionName = getString(R.string.fragment_settings_permission_name_notification)
                    showPermissionDialog(permissionName)
                }
            }
        }
    }

    private fun setUpPasscodeLockSettingItem() {
        binding.includePasscodeLockSetting.materialSwitch
            .setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                // DateStorePreferences初回読込時の値がtrueの場合、本メソッドが呼び出される。
                // 初回読込時は処理不要のため下記条件追加。
                val settingValue = settingsViewModel.isCheckedPasscodeLock.requireValue()
                if (isChecked == settingValue) return@setOnCheckedChangeListener

                lifecycleScope.launch(Dispatchers.IO) {
                    settingsViewModel.savePasscodeLock(isChecked)
                }
            }

        launchAndRepeatOnViewLifeCycleStarted {
            settingsViewModel.isCheckedPasscodeLock
                .collectLatest { }
        }
    }

    private fun setUpWeatherInfoAcquisitionSettingItem() {
        // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
        val isGranted = mainActivity.isGrantedAccessLocation
        if (!isGranted) {
            lifecycleScope.launch(Dispatchers.IO) {
                settingsViewModel.saveWeatherInfoAcquisition(false)
            }
        }

        binding.includeWeatherInfoAcquisitionSetting.materialSwitch
            .setOnCheckedChangeListener(
                WeatherInfoAcquisitionOnCheckedChangeListener()
            )
    }

    private inner class WeatherInfoAcquisitionOnCheckedChangeListener
        : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            // DateStorePreferences初回読込時の値がtrueの場合、本メソッドが呼び出される。
            // 初回読込時は処理不要のため下記条件追加。
            val settingValue = settingsViewModel.isCheckedWeatherInfoAcquisition.requireValue()
            if (isChecked == settingValue) return

            if (isChecked) {
                val isGranted = mainActivity.isGrantedAccessLocation
                if (isGranted) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        settingsViewModel.saveWeatherInfoAcquisition(true)
                    }
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
                lifecycleScope.launch(Dispatchers.IO) {
                    settingsViewModel.saveWeatherInfoAcquisition(false)
                }
            }
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

    private fun setUpOpenSourceLicensesSettingItem() {
        binding.includeOpenSourceLicensesSetting.apply {
            textTitle.setOnClickListener {
                showOpenSourceLicensesFragment()
            }
            textValue.visibility = View.GONE
        }
    }

    private fun showOpenSourceLicensesFragment() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToOpenSourceLicensesFragment()
        navController.navigate(directions)
    }

    private fun saveScrollPosition() {
        settingsViewModel.scrollPositionY = binding.scrollViewSettings.scrollY
    }

    @MainThread
    private fun showThemeColorPickerDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToThemeColorPickerDialog()
        navController.navigate(directions)
    }

    @MainThread
    private fun showCalendarStartDayPickerDialog(dayOfWeek: DayOfWeek) {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToCalendarStartDayPickerDialog(
                dayOfWeek
            )
        navController.navigate(directions)
    }

    @MainThread
    private fun showReminderNotificationTimePickerDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToReminderNotificationTimePickerDialog()
        navController.navigate(directions)
    }

    @MainThread
    private fun showPermissionDialog(permissionName: String) {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToPermissionDialog(permissionName)
        navController.navigate(directions)
    }

    @MainThread
    private fun showAllDiariesDeleteDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllDiariesDeleteDialog()
        navController.navigate(directions)
    }

    @MainThread
    private fun showAllSettingsInitializationDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllSettingsInitializationDialog()
        navController.navigate(directions)
    }

    @MainThread
    private fun showAllDataDeleteDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllDataDeleteDialog()
        navController.navigate(directions)
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    private fun showApplicationDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.setData(uri)
        startActivity(intent)
        onStart()
    }

    override fun destroyBinding() {
        saveScrollPosition()
        _binding = null
    }
}
