package com.websarva.wings.android.zuboradiary.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import com.websarva.wings.android.zuboradiary.MobileNavigationDirections
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding
import com.websarva.wings.android.zuboradiary.ui.common.utils.isAccessLocationGranted
import com.websarva.wings.android.zuboradiary.ui.common.utils.isPostNotificationsGranted
import com.websarva.wings.android.zuboradiary.ui.common.fragment.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.common.fragment.ActivityCallbackUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.common.fragment.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.sheet.ListPickerConfig
import com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.sheet.ListPickersDialogResult
import com.websarva.wings.android.zuboradiary.ui.common.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.DummyNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.alert.ConfirmationDialogParams
import com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.sheet.ListPickersDialogParams
import com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.picker.TimePickerDialogParams
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.common.theme.asString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * アプリケーションの設定を管理するフラグメント。
 *
 * 以下の責務を持つ:
 * - デザイン設定（テーマカラー、週の開始曜日）の変更
 * - 機能設定（リマインダー通知、パスコード、天気情報取得）のON/OFF
 * - データ管理（全日記削除、設定初期化、全データ削除）
 * - OSSライセンスダイアログへの遷移
 * - 各種権限（通知、位置情報）の要求と状態反映
 */
@AndroidEntryPoint
class SettingsFragment : BaseFragment<
        FragmentSettingsBinding,
        SettingsUiEvent,
        SettingsNavDestination,
        DummyNavBackDestination
        >(),
    RequiresBottomNavigation,
    ActivityCallbackUiEventHandler {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    override val mainViewModel: SettingsViewModel by activityViewModels()

    override val destinationId = R.id.navigation_settings_fragment

    override val resultKey: String? get() = null

    /** 通知権限のリクエスト結果を処理するランチャー。 */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestPostNotificationsPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            onPostNotificationsPermissionResult(isGranted)
        }

    /** 位置情報権限のリクエスト結果を処理するランチャー。 */
    private val requestAccessLocationPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { o: Map<String, Boolean> ->
            val isAccessFineLocationGranted = o[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val isAccessCoarseLocationGranted = o[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val isAllGranted = isAccessFineLocationGranted && isAccessCoarseLocationGranted
            onAccessLocationPermissionResult(isAllGranted)
        }

    /** テーマカラー選択ダイアログで使用するピッカーリスト。 */
    private val themeColorPickerList = ThemeColorUi.entries

    /** カレンダー開始曜日選択ダイアログで使用するピッカーリスト。 */
    private val dayOfWeekPickerList =
        DayOfWeek.entries.sortedBy { dayOfWeek ->
            // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
            if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value
        }
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、各権限の状態を対応する設定スイッチに反映させる。 */
    override fun onStart() {
        super.onStart()

        initializeSettingsFromPermission()
    }

    /** 各権限の状態を対応する設定スイッチに反映させる。 */
    private fun initializeSettingsFromPermission() {
        initializeReminderNotificationSettingFromPermission()
        initializeWeatherInfoFetchSettingFromPermission()
    }
    //endregion

    //region View Binding Setup
    override fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }
    //endregion

    //region Fragment Result Observation Setup
    override fun setupFragmentResultObservers() {
        observeThemeColorPickerDialogResult()
        observeCalendarStartDayPickerDialogResult()
        observeReminderNotificationTimePickerDialogResult()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            observePostNotificationsPermissionDialogResult()
        }
        observeAccessLocationPermissionDialogResult()
        observeAllDiariesDeleteDialogResult()
        observeAllSettingsInitializationDialogResult()
        observeAllDataDeleteDialogResult()
    }

    /** テーマカラー選択ダイアログからの結果を監視する。 */
    private fun observeThemeColorPickerDialogResult() {
        observeDialogResult<ListPickersDialogResult>(
            RESULT_KEY_THEME_COLOR_SETTING
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    val selectedIndex = result.data.firstPickerValue
                    val selectedThemeColor = themeColorPickerList[selectedIndex]
                    mainViewModel
                        .onThemeColorSettingDialogPositiveResultReceived(selectedThemeColor)
                }
                DialogResult.Negative,
                DialogResult.Cancel -> { /* 処理なし */ }
            }
        }
    }

    /** カレンダー開始曜日選択ダイアログからの結果を監視する。 */
    private fun observeCalendarStartDayPickerDialogResult() {
        observeDialogResult<ListPickersDialogResult>(
            RESULT_KEY_CALENDAR_START_DAY_SETTING
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    val selectedIndex = result.data.firstPickerValue
                    val selectedDayOfWeek = dayOfWeekPickerList[selectedIndex]
                    mainViewModel
                        .onCalendarStartDayOfWeekSettingDialogPositiveResultReceived(
                            selectedDayOfWeek
                        )
                }
                DialogResult.Negative,
                DialogResult.Cancel -> { /* 処理なし */ }
            }
        }
    }

    /** リマインダー通知時間選択ダイアログからの結果を監視する。 */
    private fun observeReminderNotificationTimePickerDialogResult() {
        observeDialogResult(
            RESULT_KEY_POST_REMINDER_NOTIFICATION_TIME_SELECTION
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel
                        .onReminderNotificationSettingDialogPositiveResultReceived(result.data)
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onReminderNotificationSettingDialogNegativeResultReceived()
                }
            }
        }
    }

    /** 通知権限要求の理由説明ダイアログからの結果を監視する。 */
    @RequiresApi(api = 33)
    private fun observePostNotificationsPermissionDialogResult() {
        observeDialogResult<Unit>(
            RESULT_KEY_POST_NOTIFICATIONS_PERMISSION_RATIONALE
        ) { result ->
            when (result) {
                is DialogResult.Positive -> requestPostNotificationsPermission()
                DialogResult.Negative,
                DialogResult.Cancel -> mainViewModel.onPostNotificationsPermissionDenied()
            }
        }
    }

    /** 位置情報権限要求の理由説明ダイアログからの結果を監視する。 */
    private fun observeAccessLocationPermissionDialogResult() {
        observeDialogResult<Unit>(
            RESULT_KEY_ACCESS_LOCATION_PERMISSION_RATIONALE
        ) { result ->
            when (result) {
                is DialogResult.Positive -> requestAccessLocationPermission()
                DialogResult.Negative,
                DialogResult.Cancel -> mainViewModel.onAccessLocationPermissionDenied()
            }
        }
    }

    /** 全日記削除確認ダイアログからの結果を監視する。 */
    private fun observeAllDiariesDeleteDialogResult() {
        observeDialogResult<Unit>(
            RESULT_KEY_ALL_DIARIES_DELETE_CONFIRMATION
        ) { result ->
            when (result) {
                is DialogResult.Positive<Unit> -> {
                    mainViewModel.onAllDiariesDeleteDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> { /* 処理なし */ }
            }
        }
    }

    /** 全設定初期化確認ダイアログからの結果を監視する。 */
    private fun observeAllSettingsInitializationDialogResult() {
        observeDialogResult<Unit>(
            RESULT_KEY_ALL_SETTINGS_INITIALIZATION_CONFIRMATION
        ) { result ->
            when (result) {
                is DialogResult.Positive<Unit> -> {
                    mainViewModel.onAllSettingsInitializationDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> { /* 処理なし */ }
            }
        }
    }

    /** 全データ削除確認ダイアログからの結果を監視する。 */
    private fun observeAllDataDeleteDialogResult() {
        observeDialogResult<Unit>(
            RESULT_KEY_ALL_DATA_DELETE_CONFIRMATION
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel.onAllDataDeleteDialogResultPositiveReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> { /* 処理なし */ }
            }
        }
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ShowApplicationDetailsSettingsScreen -> {
                showApplicationDetailsSettings()
            }
            is SettingsUiEvent.CheckPostNotificationsPermission -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkPostNotificationsPermission()
                }
            }
            is SettingsUiEvent.CheckAccessLocationPermission -> {
                checkAccessLocationPermission()
            }
            is SettingsUiEvent.TurnReminderNotificationSettingSwitch -> {
                binding.includeReminderNotificationSetting.materialSwitch.isChecked = event.isChecked
            }
            is SettingsUiEvent.TurnPasscodeLockSettingSwitch -> {
                binding.includePasscodeLockSetting.materialSwitch.isChecked = event.isChecked
            }
            is SettingsUiEvent.TurnWeatherInfoFetchSettingSwitch -> {
                binding.includeWeatherInfoFetchSetting.materialSwitch.isChecked = event.isChecked
            }
        }
    }

    override fun onActivityCallbackUiEventReceived(event: ActivityCallbackUiEvent) {
        when (event) {
            ActivityCallbackUiEvent.ProcessOnBottomNavigationItemReselect -> {
                smoothScrollToTop()
            }
        }
    }

    override fun setupUiStateObservers() {
        super.setupUiStateObservers()

        observeViewColor()
    }

    override fun setupUiEventObservers() {
        super.setupUiEventObservers()

        observeUiEventFromActivity()
    }

    /** テーマカラーの変更を監視し、UIに反映させる。 */
    private fun observeViewColor() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.themeColor == new.themeColor
            }.map {
                it.themeColor
            }.filterNotNull().collect {
                switchViewColor(it)
            }
        }
    }

    /** ActivityからのUIイベントを監視する。 */
    private fun observeUiEventFromActivity() {
        fragmentHelper.observeActivityUiEvent(
            this,
            mainActivityViewModel,
            this
        )
    }
    //endregion

    //region View Control
    /**
     * テーマカラーに応じてUIの各要素の色を切り替える。
     * @param themeColor 適用するテーマカラー
     */
    private fun switchViewColor(themeColor: ThemeColorUi) {
        val changer = SettingsThemeColorChanger()

        changer.applyAppBackgroundColor(binding.root, themeColor)
        changer.applyAppToolbarColor(
            binding.materialToolbarTopAppBar,
            themeColor,
            binding.appBarLayout
        )

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
                    includeWeatherInfoFetchSetting.textTitle,
                    includeAllDiariesDeleteSetting.textTitle,
                    includeAllSettingsInitializationSetting.textTitle,
                    includeAllDataDeleteSetting.textTitle,
                    includeOpenSourceLicensesSetting.textTitle
                )
            },
            themeColor
        )

        changer.applyAppTextColorOnBackground(
            binding.run {
                listOf(
                    includeThemeColorSetting.textTitle,
                    includeThemeColorSetting.textValue,
                    includeCalendarStartDaySetting.textTitle,
                    includeCalendarStartDaySetting.textValue,
                    includeReminderNotificationSetting.textTitle,
                    includeReminderNotificationSetting.textValue,
                    includePasscodeLockSetting.textTitle,
                    includeWeatherInfoFetchSetting.textTitle,
                    includeOpenSourceLicensesSetting.textTitle
                )
            },
            themeColor
        )

        changer.applyAppTextErrorColor(
            binding.run {
                listOf(
                    includeAllDiariesDeleteSetting.textTitle,
                    includeAllSettingsInitializationSetting.textTitle,
                    includeAllDataDeleteSetting.textTitle
                )
            },
            themeColor
        )

        changer.applyAppSwitchColor(
            binding.run {
                listOf(
                    includeReminderNotificationSetting.materialSwitch,
                    includePasscodeLockSetting.materialSwitch,
                    includeWeatherInfoFetchSetting.materialSwitch
                )
            },
            themeColor
        )

        changer.applyAppDividerColor(
            binding.run {
                listOf(
                    materialDividerToolbar,
                    materialDividerThemeColorSetting,
                    materialDividerSectionSetting,
                    materialDividerCalendarStartDaySetting,
                    materialDividerReminderNotificationSetting,
                    materialDividerPasscodeLockSetting,
                    materialDividerWeatherInfoFetchSetting,
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

    /** 画面の最上部までスムーズにスクロールする。 */
    private fun smoothScrollToTop() {
        binding.scrollViewSettings.smoothScrollTo(0, 0)
    }
    //endregion

    //region Navigation Helpers
    override fun toNavDirections(destination: SettingsNavDestination): NavDirections {
        return when (destination) {
            is SettingsNavDestination.AppMessageDialog -> {
                navigationEventHelper.createAppMessageDialogNavDirections(destination.message)
            }
            is SettingsNavDestination.ThemeColorPickerDialog -> {
                createThemeColorPickerDialogNavDirections(destination.themeColor)
            }
            is SettingsNavDestination.CalendarStartDayPickerDialog -> {
                createCalendarStartDayPickerDialogNavDirections(destination.dayOfWeek)
            }
            is SettingsNavDestination.ReminderNotificationTimePickerDialog -> {
                createReminderNotificationTimePickerDialogNavDirections()
            }
            is SettingsNavDestination.AllDiariesDeleteDialog -> {
                createAllDiariesDeleteDialogNavDirections()
            }
            is SettingsNavDestination.AllSettingsInitializationDialog -> {
                createAllSettingsInitializationDialogNavDirections()
            }
            is SettingsNavDestination.AllDataDeleteDialog -> {
                createAllDataDeleteDialogNavDirections()
            }
            is SettingsNavDestination.OSSLicensesDialog -> {
                createOpenSourceSoftwareLicensesDialogNavDirections()
            }
            is SettingsNavDestination.NotificationPermissionRationaleDialog -> {
                createPostNotificationsPermissionRationaleDialogNavDirections()
            }
            is SettingsNavDestination.LocationPermissionRationaleDialog -> {
                createAccessLocationPermissionRationaleDialogNavDirections()
            }
        }
    }

    override fun toNavDestinationId(destination: DummyNavBackDestination): Int {
        // 処理なし
        throw IllegalStateException("NavDestinationIdへの変換は不要の為、未対応。")
    }

    /**
     * テーマカラー選択ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。
     *
     * @param themeColor 現在設定されているテーマカラー
     */
    private fun createThemeColorPickerDialogNavDirections(themeColor: ThemeColorUi): NavDirections {
        val themeColorStringPickerList = themeColorPickerList.map { themeColor ->
            themeColor.asString(requireContext())
        }
        val currentThemeColorString = themeColor.asString(requireContext())

        val params = ListPickersDialogParams(
            resultKey = RESULT_KEY_THEME_COLOR_SETTING,
            pickerConfigs = listOf(
                ListPickerConfig(
                    items = themeColorStringPickerList,
                    initialIndex = themeColorStringPickerList.indexOf(currentThemeColorString)
                )
            )
        )
        return MobileNavigationDirections.actionGlobalToListPickersDialog(params)
    }

    /**
     * カレンダー開始曜日選択ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。
     *
     * @param dayOfWeek 現在設定されている週の開始曜日
     */
    private fun createCalendarStartDayPickerDialogNavDirections(dayOfWeek: DayOfWeek): NavDirections {
        val dayOfWeekStringPickerList =
            dayOfWeekPickerList.map { dayOfWeek ->
                dayOfWeek.asCalendarStartDayOfWeekString(requireContext())
            }
        val currentDayOfWeekString = dayOfWeek.asCalendarStartDayOfWeekString(requireContext())

        val params = ListPickersDialogParams(
            resultKey = RESULT_KEY_CALENDAR_START_DAY_SETTING,
            pickerConfigs = listOf(
                ListPickerConfig(
                    items = dayOfWeekStringPickerList,
                    initialIndex = dayOfWeekStringPickerList.indexOf(currentDayOfWeekString)
                )
            )
        )
        return MobileNavigationDirections.actionGlobalToListPickersDialog(params)
    }

    /** リマインダー通知時間選択ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。 */
    private fun createReminderNotificationTimePickerDialogNavDirections(): NavDirections {
        val params = TimePickerDialogParams(
            resultKey = RESULT_KEY_POST_REMINDER_NOTIFICATION_TIME_SELECTION,
            initialTime = LocalTime.now()
        )
        return MobileNavigationDirections.actionGlobalToTimePickerDialog(params)
    }

    /** 通知権限要求の理由説明ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。 */
    private fun createPostNotificationsPermissionRationaleDialogNavDirections(): NavDirections {
        val params = ConfirmationDialogParams(
            resultKey = RESULT_KEY_POST_NOTIFICATIONS_PERMISSION_RATIONALE,
            titleRes = R.string.dialog_permission_title,
            messageText = getString(
                R.string.dialog_permission_message,
                getString(R.string.dialog_permission_name_notification)
            )
        )
        return MobileNavigationDirections.actionGlobalToConfirmationDialog(params)
    }

    /** 位置情報権限要求の理由説明ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。 */
    private fun createAccessLocationPermissionRationaleDialogNavDirections(): NavDirections {
        val params = ConfirmationDialogParams(
            resultKey = RESULT_KEY_ACCESS_LOCATION_PERMISSION_RATIONALE,
            titleRes = R.string.dialog_permission_title,
            messageText = getString(
                R.string.dialog_permission_message,
                getString(R.string.dialog_permission_name_location)
            )
        )
        return MobileNavigationDirections.actionGlobalToConfirmationDialog(params)
    }

    /** 全日記削除確認ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。 */
    private fun createAllDiariesDeleteDialogNavDirections(): NavDirections {
        val params = ConfirmationDialogParams(
            resultKey = RESULT_KEY_ALL_DIARIES_DELETE_CONFIRMATION,
            titleRes = R.string.dialog_all_diaries_delete_title,
            messageRes = R.string.dialog_all_diaries_delete_message
        )
        return MobileNavigationDirections.actionGlobalToConfirmationDialog(params)
    }

    /** 全設定初期化確認ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。 */
    private fun createAllSettingsInitializationDialogNavDirections(): NavDirections {
        val params = ConfirmationDialogParams(
            resultKey = RESULT_KEY_ALL_SETTINGS_INITIALIZATION_CONFIRMATION,
            titleRes = R.string.dialog_all_settings_initialization_title,
            messageRes = R.string.dialog_all_settings_initialization_message
        )
        return MobileNavigationDirections.actionGlobalToConfirmationDialog(params)
    }

    /** 全データ削除確認ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。 */
    private fun createAllDataDeleteDialogNavDirections(): NavDirections {
        val params = ConfirmationDialogParams(
            resultKey = RESULT_KEY_ALL_DATA_DELETE_CONFIRMATION,
            titleRes = R.string.dialog_all_data_delete_title,
            messageRes = R.string.dialog_all_data_delete_message
        )
        return MobileNavigationDirections.actionGlobalToConfirmationDialog(params)
    }

    /** OSSライセンスダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。 */
    private fun createOpenSourceSoftwareLicensesDialogNavDirections(): NavDirections {
        return SettingsFragmentDirections
            .actionNavigationSettingsFragmentToOpenSourceSoftwareLicensesDialog()
    }

    // TODO:不要だが残しておく(最終的に削除)
    /** アプリケーション詳細設定画面へ遷移する。 */
    private fun showApplicationDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }
    //endregion

    //region Permission Handling - Post Notifications
    /** 通知権限の有無を確認し、権限がない場合はRationale表示または権限要求を行う。 */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private fun checkPostNotificationsPermission() {
        val logPrefix = "位置情報権限"
        Log.d(logTag, "${logPrefix}の確認を開始。")
        when {
            requireContext().isPostNotificationsGranted() -> {
                Log.d(logTag, "${logPrefix}: 許可済み。")
                mainViewModel.onPostNotificationsPermissionGranted()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                Log.d(logTag, "${logPrefix}: Rationale表示が必要。")
                mainViewModel.onPostNotificationsPermissionRationaleRequired()
            }

            else -> {
                Log.d(logTag, "${logPrefix}: 初回要求または永久に拒否済み。ランチャーを起動。")
                requestPostNotificationsPermission()
            }
        }
    }

    /** システムの通知権限ダイアログを表示する。 */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private fun requestPostNotificationsPermission() {
        requestPostNotificationsPermissionLauncher
            .launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /** 通知権限要求のランチャー結果を処理する。 */
    private fun onPostNotificationsPermissionResult(isGranted: Boolean) {
        Log.d(logTag, "システムの通知権限ダイアログの結果: ${if (isGranted) "許可" else "拒否"}")
        if (isGranted) {
            mainViewModel.onPostNotificationsPermissionGranted()
        } else {
            mainViewModel.onPostNotificationsPermissionDenied()
        }
    }

    /** 端末の通知権限設定とアプリ内のリマインダー設定の同期するよう、ViewModelに通知する。 */
    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    private fun initializeReminderNotificationSettingFromPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isPostNotificationsGranted = requireContext().isPostNotificationsGranted()
            mainViewModel
                .onEnsureReminderNotificationSettingMatchesPermission(isPostNotificationsGranted)
        }
    }
    //endregion

    //region Permission Handling - Access Location
    /** 位置情報権限の有無を確認し、権限がない場合はRationale表示または権限要求を行う。 */
    private fun checkAccessLocationPermission() {
        val logPrefix = "位置情報権限"
        Log.d(logTag, "${logPrefix}の確認を開始。")
        when {
            requireContext().isAccessLocationGranted() -> {
                Log.d(logTag, "${logPrefix}: 許可済み。")
                mainViewModel.onAccessLocationPermissionGranted()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                Log.d(logTag, "${logPrefix}: Rationale表示が必要。")
                mainViewModel.onAccessLocationPermissionRationaleRequired()
            }

            else -> {
                Log.d(logTag, "${logPrefix}: 初回要求または永久に拒否済み。ランチャーを起動。")
                requestAccessLocationPermission()
            }
        }
    }

    /** システムの位置情報権限ダイアログを表示する。 */
    private fun requestAccessLocationPermission() {
        val requestPermissions =
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        requestAccessLocationPermissionLauncher.launch(requestPermissions)
    }

    /** 位置情報権限要求のランチャー結果を処理する。 */
    private fun onAccessLocationPermissionResult(isGranted: Boolean) {
        Log.d(logTag, "システムの位置情報権限ダイアログの結果: ${if (isGranted) "許可" else "拒否"}")
        if (isGranted) {
            mainViewModel.onAccessLocationPermissionGranted()
        } else {
            mainViewModel.onAccessLocationPermissionDenied()
        }
    }

    /** 端末の位置情報権限設定とアプリ内の天気情報取得設定の同期するよう、ViewModelに通知する。 */
    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    private fun initializeWeatherInfoFetchSettingFromPermission() {
        val isAccessLocationGranted = requireContext().isAccessLocationGranted()
        mainViewModel
            .onEnsureWeatherInfoFetchSettingMatchesPermission(isAccessLocationGranted)
    }
    //endregion

    internal companion object {
        /** テーマカラー選択の結果を受け取るためのキー。 */
        private const val RESULT_KEY_THEME_COLOR_SETTING = "theme_color_setting_result"

        /** カレンダー開始曜日選択の結果を受け取るためのキー。 */
        private const val RESULT_KEY_CALENDAR_START_DAY_SETTING = "calendar_start_day_setting_result"

        /** リマインダー通知時刻選択の結果を受け取るためのキー。 */
        private const val RESULT_KEY_POST_REMINDER_NOTIFICATION_TIME_SELECTION =
            "post_reminder_notification_time_selection_result"

        /** 全日記削除の確認ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_ALL_DIARIES_DELETE_CONFIRMATION =
            "all_diaries_delete_confirmation_result"

        /** 全設定初期化の確認ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_ALL_SETTINGS_INITIALIZATION_CONFIRMATION =
            "all_settings_initialization_confirmation_result"

        /** 全データ削除の確認ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_ALL_DATA_DELETE_CONFIRMATION =
            "all_data_delete_confirmation_result"

        /** 通知権限要求の理由説明ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_POST_NOTIFICATIONS_PERMISSION_RATIONALE =
            "post_notifications_permission_rationale_result"

        /** 位置情報権限要求の理由説明ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_ACCESS_LOCATION_PERMISSION_RATIONALE =
            "access_location_permission_rationale_result"
    }
}
