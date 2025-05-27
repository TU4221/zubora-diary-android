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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.ViewDataBinding
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding
import com.websarva.wings.android.zuboradiary.ui.permission.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.AllDataDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.AllDiariesDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.AllSettingsInitializationDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.CalendarStartDayPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.PermissionDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.ReminderNotificationTimePickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.theme.SettingsThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.ThemeColorPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.SettingsFragmentAction
import com.websarva.wings.android.zuboradiary.ui.utils.formatToHourMinuteString
import com.websarva.wings.android.zuboradiary.ui.utils.isAccessLocationGranted
import com.websarva.wings.android.zuboradiary.ui.utils.isPostNotificationsGranted
import com.websarva.wings.android.zuboradiary.ui.utils.toCalendarStartDayOfWeekString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.time.DayOfWeek
import java.time.LocalTime

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    // View関係
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    // MEMO:本FragmentのMainViewModelはSettingsViewModelになる為、BaseFragmentのSettingsViewModel変数を取得。
    override val mainViewModel
        get() = settingsViewModel

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

        uriPermissionManager =
            object : UriPermissionManager() {
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

        setUpFragmentAction()
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

        mainViewModel.onThemeColorSettingDialogPositiveButtonClicked(selectedThemeColor)
    }

    // カレンダー開始曜日設定ダイアログフラグメントから結果受取
    private fun receiveCalendarStartDayPickerDialogResult() {
        val selectedDayOfWeek =
            receiveResulFromDialog<DayOfWeek>(CalendarStartDayPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK)
                ?: return

        settingsViewModel
            .onCalendarStartDayOfWeekSettingDialogPositiveButtonClicked(selectedDayOfWeek)
    }

    // リマインダー通知時間設定ダイアログフラグメントから結果受取
    private fun receiveReminderNotificationTimePickerDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) {
            mainViewModel.onReminderNotificationSettingDialogNegativeButtonClicked()
            return
        }

        val selectedTime =
            checkNotNull(
                receiveResulFromDialog<LocalTime>(
                    ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_TIME
                )
            )
        mainViewModel.onReminderNotificationSettingDialogPositiveButtonClicked(selectedTime)
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

        mainViewModel.onAllDiariesDeleteDialogPositiveButtonClicked()
    }

    private fun receiveAllSettingsInitializationDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(AllSettingsInitializationDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        mainViewModel.onAllSettingsInitializationDialogPositiveButtonClicked()
    }

    private fun receiveAllDataDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(AllDataDeleteDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        mainViewModel.onAllDataDeleteDialogPositiveButtonClicked()
    }

    private fun setUpScrollPosition() {
        binding.scrollViewSettings.scrollY = settingsViewModel.scrollPositionY
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

    private fun setUpFragmentAction() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.fragmentAction.collect { value: FragmentAction ->
                when (value) {
                    is SettingsFragmentAction.NavigateThemeColorPickerDialog -> {
                        navigateThemeColorPickerDialog()
                    }
                    is SettingsFragmentAction.NavigateCalendarStartDayPickerDialog -> {
                        navigateCalendarStartDayPickerDialog(value.dayOfWeek)
                    }
                    is SettingsFragmentAction.NavigateReminderNotificationTimePickerDialog -> {
                        navigateReminderNotificationTimePickerDialog()
                    }
                    is SettingsFragmentAction.NavigateNotificationPermissionDialog -> {
                        navigateNotificationPermissionDialog()
                    }
                    is SettingsFragmentAction.NavigateLocationPermissionDialog -> {
                        navigateLocationPermissionDialog()
                    }
                    is SettingsFragmentAction.NavigateAllDiariesDeleteDialog -> {
                        navigateAllDiariesDeleteDialog()
                    }
                    is SettingsFragmentAction.NavigateAllSettingsInitializationDialog -> {
                        navigateAllSettingsInitializationDialog()
                    }
                    is SettingsFragmentAction.NavigateAllDataDeleteDialog -> {
                        navigateAllDataDeleteDialog()
                    }
                    is SettingsFragmentAction.NavigateOpenSourceLicensesFragment -> {
                        navigateOpenSourceLicensesFragment()
                    }
                    is SettingsFragmentAction.ReleaseAllPersistablePermission -> {
                        uriPermissionManager.releaseAllPersistablePermission(requireContext())
                    }
                    is SettingsFragmentAction.CheckPostNotificationsPermission -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            checkPostNotificationsPermission()
                        }
                    }
                    is SettingsFragmentAction.CheckShouldShowRequestPostNotificationsPermissionRationale -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            checkShouldShowRequestPostNotificationsPermissionRationale()
                        }
                    }
                    is SettingsFragmentAction.ShowRequestPostNotificationsPermissionRationale -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            showRequestPostNotificationsPermissionRationale()
                        }
                    }
                    is SettingsFragmentAction.CheckAccessLocationPermission -> {
                        checkAccessLocationPermission()
                    }
                    is SettingsFragmentAction.CheckShouldShowRequestAccessLocationPermissionRationale -> {
                        checkShouldShowRequestAccessLocationPermissionRationale()
                    }
                    is SettingsFragmentAction.ShowRequestAccessLocationPermissionRationale -> {
                        showRequestAccessLocationPermissionRationale()
                    }
                    is SettingsFragmentAction.TurnOffReminderNotificationSettingSwitch -> {
                        binding.includeReminderNotificationSetting.materialSwitch.isChecked = false
                    }
                    is SettingsFragmentAction.TurnOffWeatherInfoAcquisitionSettingSwitch -> {
                        binding.includeWeatherInfoAcquisitionSetting.materialSwitch.isChecked = false
                    }
                    else -> {
                        throw IllegalArgumentException()
                    }
                }
            }
        }
    }

    private fun saveScrollPosition() {
        settingsViewModel.scrollPositionY = binding.scrollViewSettings.scrollY
    }

    private fun navigateThemeColorPickerDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToThemeColorPickerDialog()
        navController.navigate(directions)
    }

    private fun navigateCalendarStartDayPickerDialog(dayOfWeek: DayOfWeek) {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToCalendarStartDayPickerDialog(
                dayOfWeek
            )
        navController.navigate(directions)
    }

    private fun navigateReminderNotificationTimePickerDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToReminderNotificationTimePickerDialog()
        navController.navigate(directions)
    }

    private fun navigateNotificationPermissionDialog() {
        if (!canNavigateFragment) return

        val permissionName = getString(R.string.fragment_settings_permission_name_notification)
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToPermissionDialog(permissionName)
        navController.navigate(directions)
    }

    private fun navigateLocationPermissionDialog() {
        if (!canNavigateFragment) return

        val permissionName = getString(R.string.fragment_settings_permission_name_location)
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToPermissionDialog(permissionName)
        navController.navigate(directions)
    }

    private fun navigateAllDiariesDeleteDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllDiariesDeleteDialog()
        navController.navigate(directions)
    }

    private fun navigateAllSettingsInitializationDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllSettingsInitializationDialog()
        navController.navigate(directions)
    }

    private fun navigateAllDataDeleteDialog() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllDataDeleteDialog()
        navController.navigate(directions)
    }

    override fun onNavigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    private fun navigateOpenSourceLicensesFragment() {
        if (!canNavigateFragment) return

        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToOpenSourceLicensesFragment()
        navController.navigate(directions)
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

    override fun destroyBinding() {
        saveScrollPosition()
        _binding = null
    }
}
