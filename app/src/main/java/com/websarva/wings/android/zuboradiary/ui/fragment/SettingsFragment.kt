package com.websarva.wings.android.zuboradiary.ui.fragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.AllDataDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.AllDiariesDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.AllSettingsInitializationDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.CalendarStartDayPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.PermissionDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.ReminderNotificationTimePickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.theme.SettingsThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.ThemeColorPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.SettingsEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.utils.formatToHourMinuteString
import com.websarva.wings.android.zuboradiary.ui.utils.isAccessLocationGranted
import com.websarva.wings.android.zuboradiary.ui.utils.isPostNotificationsGranted
import com.websarva.wings.android.zuboradiary.ui.utils.toCalendarStartDayOfWeekString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.time.DayOfWeek
import java.time.LocalTime

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    // ViewModel
    // MEMO:本FragmentのMainViewModelはSettingsViewModelになる為、BaseFragmentのSettingsViewModel変数を取得。
    override val mainViewModel
        get() = settingsViewModel

    // ActivityResultLauncher関係
    private lateinit var requestPostNotificationsPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestAccessLocationPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ActivityResultLauncher設定
        // 通知権限取得結果処理
        // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationsPermissionLauncher =
                registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->

                    // 再確認
                    val recheck = requireContext().isPostNotificationsGranted()

                    mainViewModel
                        .onRequestPostNotificationsPermissionRationaleResultReceived(
                            isGranted && recheck
                        )
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
                val recheck = requireContext().isAccessLocationGranted()

                mainViewModel
                    .onRequestAccessLocationPermissionRationaleResultReceived(
                        isGrantedAll && recheck
                    )
            }
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpThemeColorSettingItem()
        setUpCalendarStartDaySettingItem()
        setUpReminderNotificationSettingItem()
    }

    override fun initializeFragmentResultReceiver() {
        receiveThemeColorPickerDialogResult()
        receiveCalendarStartDayPickerDialogResult()
        receiveReminderNotificationTimePickerDialogResult()
        receivePermissionDialogResult()
        receiveAllDiariesDeleteDialogResult()
        receiveAllSettingsInitializationDialogResult()
        receiveAllDataDeleteDialogResult()
    }

    // テーマカラー設定ダイアログフラグメントから結果受取
    private fun receiveThemeColorPickerDialogResult() {
        setUpDialogResultReceiver(
            ThemeColorPickerDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onThemeColorSettingDialogResultReceived(result)
        }
    }

    // カレンダー開始曜日設定ダイアログフラグメントから結果受取
    private fun receiveCalendarStartDayPickerDialogResult() {
        setUpDialogResultReceiver(
            CalendarStartDayPickerDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onCalendarStartDayOfWeekSettingDialogResultReceived(result)
        }
    }

    // リマインダー通知時間設定ダイアログフラグメントから結果受取
    private fun receiveReminderNotificationTimePickerDialogResult() {
        setUpDialogResultReceiver(
            ReminderNotificationTimePickerDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onReminderNotificationSettingDialogResultReceived(result)
        }
    }

    // 権限催促ダイアログフラグメントから結果受取
    private fun receivePermissionDialogResult() {
        setUpDialogResultReceiver(
            PermissionDialogFragment.KEY_RESULT
        ) { result: DialogResult<Unit> ->
            // TODO:シールドクラス Action -> Event に変更してから下記コードの処理方法を検討する。
            when (result) {
                is DialogResult.Positive<Unit> -> {
                    showApplicationDetailsSettings()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    // 処理なし
                }
            }
        }
    }

    private fun receiveAllDiariesDeleteDialogResult() {
        setUpDialogResultReceiver(
            AllDiariesDeleteDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onAllDiariesDeleteDialogResultReceived(result)
        }
    }

    private fun receiveAllSettingsInitializationDialogResult() {
        setUpDialogResultReceiver(
            AllSettingsInitializationDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onAllSettingsInitializationDialogResultReceived(result)
        }
    }

    private fun receiveAllDataDeleteDialogResult() {
        setUpDialogResultReceiver(
            AllDataDeleteDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onAllDataDeleteDialogResultReceived(result)
        }
    }

    override fun onMainViewModelEventReceived(event: ViewModelEvent) {
        when (event) {
            is SettingsEvent.NavigateThemeColorPickerDialog -> {
                navigateThemeColorPickerDialog()
            }
            is SettingsEvent.NavigateCalendarStartDayPickerDialog -> {
                navigateCalendarStartDayPickerDialog(event.dayOfWeek)
            }
            is SettingsEvent.NavigateReminderNotificationTimePickerDialog -> {
                navigateReminderNotificationTimePickerDialog()
            }
            is SettingsEvent.NavigateNotificationPermissionDialog -> {
                navigateNotificationPermissionDialog()
            }
            is SettingsEvent.NavigateLocationPermissionDialog -> {
                navigateLocationPermissionDialog()
            }
            is SettingsEvent.NavigateAllDiariesDeleteDialog -> {
                navigateAllDiariesDeleteDialog()
            }
            is SettingsEvent.NavigateAllSettingsInitializationDialog -> {
                navigateAllSettingsInitializationDialog()
            }
            is SettingsEvent.NavigateAllDataDeleteDialog -> {
                navigateAllDataDeleteDialog()
            }
            is SettingsEvent.NavigateOpenSourceLicensesFragment -> {
                navigateOpenSourceLicensesFragment()
            }
            is SettingsEvent.CheckPostNotificationsPermission -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkPostNotificationsPermission()
                }
            }
            is SettingsEvent.CheckShouldShowRequestPostNotificationsPermissionRationale -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkShouldShowRequestPostNotificationsPermissionRationale()
                }
            }
            is SettingsEvent.ShowRequestPostNotificationsPermissionRationale -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showRequestPostNotificationsPermissionRationale()
                }
            }
            is SettingsEvent.CheckAccessLocationPermission -> {
                checkAccessLocationPermission()
            }
            is SettingsEvent.CheckShouldShowRequestAccessLocationPermissionRationale -> {
                checkShouldShowRequestAccessLocationPermissionRationale()
            }
            is SettingsEvent.ShowRequestAccessLocationPermissionRationale -> {
                showRequestAccessLocationPermissionRationale()
            }
            is SettingsEvent.TurnOffReminderNotificationSettingSwitch -> {
                binding.includeReminderNotificationSetting.materialSwitch.isChecked = false
            }
            is SettingsEvent.TurnOffWeatherInfoAcquisitionSettingSwitch -> {
                binding.includeWeatherInfoAcquisitionSetting.materialSwitch.isChecked = false
            }
            ViewModelEvent.NavigatePreviousFragment -> {
                mainActivity.popBackStackToStartFragment()
            }
            is ViewModelEvent.NavigateAppMessage -> {
                navigateAppMessageDialog(event.message)
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    private fun setUpThemeColorSettingItem() {
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
        val changer = SettingsThemeColorChanger()

        changer.applyBackgroundColor(binding.viewFullScreenBackground, themeColor)
        changer.applyToolbarColor(binding.materialToolbarTopAppBar, themeColor)

        changer.applySettingItemSectionColor(
            binding.run {
                listOf(
                    textSettingsSectionDesign,
                    textSettingsSectionSetting,
                    textSettingsSectionEnd,
                    textSettingsSectionData,
                    textSettingsSectionOther
                )
            },
            themeColor
        )

        changer.applySettingItemIconColor(
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
            },
            themeColor
        )

        changer.applyTextColorOnBackground(
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
            },
            themeColor
        )

        changer.applyRedTextColorOnBackground(
            binding.run {
                listOf(
                    includeAllDiariesDeleteSetting.textTitle,
                    includeAllSettingsInitializationSetting.textTitle,
                    includeAllDataDeleteSetting.textTitle
                )
            },
            themeColor
        )

        changer.applySwitchColor(
            binding.run {
                listOf(
                    includeReminderNotificationSetting.materialSwitch,
                    includePasscodeLockSetting.materialSwitch,
                    includeWeatherInfoAcquisitionSetting.materialSwitch
                )
            },
            themeColor
        )

        changer.applyDividerColor(
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
            },
            themeColor
        )
    }

    private fun setUpCalendarStartDaySettingItem() {
        launchAndRepeatOnViewLifeCycleStarted {
            settingsViewModel.calendarStartDayOfWeek
                .collectLatest { value: DayOfWeek? ->
                    value ?: return@collectLatest

                    val strDayOfWeek = value.toCalendarStartDayOfWeekString(requireContext())
                    binding.includeCalendarStartDaySetting.textValue.text = strDayOfWeek
                }
        }
    }

    private fun setUpReminderNotificationSettingItem() {
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

                    val timeString = value.formatToHourMinuteString(requireContext())
                    binding.includeReminderNotificationSetting.textValue.text = timeString
                }
        }
    }

    private fun navigateThemeColorPickerDialog() {
        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToThemeColorPickerDialog()
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateCalendarStartDayPickerDialog(dayOfWeek: DayOfWeek) {
        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToCalendarStartDayPickerDialog(
                dayOfWeek
            )
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateReminderNotificationTimePickerDialog() {
        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToReminderNotificationTimePickerDialog()
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateNotificationPermissionDialog() {
        val permissionName = getString(R.string.fragment_settings_permission_name_notification)
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToPermissionDialog(permissionName)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateLocationPermissionDialog() {
        val permissionName = getString(R.string.fragment_settings_permission_name_location)
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToPermissionDialog(permissionName)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateAllDiariesDeleteDialog() {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllDiariesDeleteDialog()
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateAllSettingsInitializationDialog() {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllSettingsInitializationDialog()
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateAllDataDeleteDialog() {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllDataDeleteDialog()
        navigateFragment(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAppMessageDialog(appMessage)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateOpenSourceLicensesFragment() {
        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToOpenSourceLicensesFragment()
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun showApplicationDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.setData(uri)
        startActivity(intent)
        onStart()
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private fun checkPostNotificationsPermission() {
        val isGranted = requireContext().isPostNotificationsGranted()
        mainViewModel.onPostNotificationsPermissionChecked(isGranted)
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private fun checkShouldShowRequestPostNotificationsPermissionRationale() {
        val shouldShowRequest =
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.POST_NOTIFICATIONS
            )
        mainViewModel.onShouldShowRequestPostNotificationsPermissionRationaleChecked(shouldShowRequest)
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private fun showRequestPostNotificationsPermissionRationale() {
        requestPostNotificationsPermissionLauncher
            .launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun checkAccessLocationPermission() {
        val isGranted = requireContext().isAccessLocationGranted()
        mainViewModel.onAccessLocationPermissionChecked(isGranted)
    }

    private fun checkShouldShowRequestAccessLocationPermissionRationale() {
        val shouldShowRequest =
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
            )
        mainViewModel.onShouldShowRequestAccessLocationPermissionRationaleChecked(shouldShowRequest)
    }

    private fun showRequestAccessLocationPermissionRationale() {
        val requestPermissions =
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        requestAccessLocationPermissionLauncher.launch(requestPermissions)
    }

    override fun onStart() {
        super.onStart()
        initializeSettingFromPermission()
    }

    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    private fun initializeSettingFromPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isPostNotificationsGranted = requireContext().isPostNotificationsGranted()
            mainViewModel
                .onInitializeReminderNotificationSettingFromPermission(isPostNotificationsGranted)
        }

        val isAccessLocationGranted = requireContext().isAccessLocationGranted()
        mainViewModel
            .onInitializeWeatherInfoAcquisitionSettingFromPermission(isAccessLocationGranted)
    }
}
