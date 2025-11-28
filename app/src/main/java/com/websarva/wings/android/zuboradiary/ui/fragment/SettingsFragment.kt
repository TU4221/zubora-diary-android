package com.websarva.wings.android.zuboradiary.ui.fragment

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
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding
import com.websarva.wings.android.zuboradiary.ui.fragment.common.ActivityCallbackUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.fragment.common.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.AllDataDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.AllDiariesDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.AllSettingsInitializationDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet.CalendarStartDayPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.PermissionRationaleDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen.OpenSourceSoftwareLicensesDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker.ReminderNotificationTimePickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.theme.SettingsThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet.ThemeColorPickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.event.SettingsUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.permission.RequestPermissionType
import com.websarva.wings.android.zuboradiary.ui.utils.isAccessLocationGranted
import com.websarva.wings.android.zuboradiary.ui.utils.isPostNotificationsGranted
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import kotlin.getValue

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
class SettingsFragment :
    BaseFragment<FragmentSettingsBinding, SettingsUiEvent>(),
    RequiresBottomNavigation,
    ActivityCallbackUiEventHandler {
        
    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    override val mainViewModel: SettingsViewModel by activityViewModels()

    override val destinationId = R.id.navigation_settings_fragment

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
        observePermissionDialogResult()
        observeAllDiariesDeleteDialogResult()
        observeAllSettingsInitializationDialogResult()
        observeAllDataDeleteDialogResult()
    }

    /** テーマカラー選択ダイアログからの結果を監視する。 */
    private fun observeThemeColorPickerDialogResult() {
        observeDialogResult(
            ThemeColorPickerDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel
                        .onThemeColorSettingDialogPositiveResultReceived(result.data)
                }
                DialogResult.Negative,
                DialogResult.Cancel -> { /* 処理なし */ }
            }
        }
    }

    /** カレンダー開始曜日選択ダイアログからの結果を監視する。 */
    private fun observeCalendarStartDayPickerDialogResult() {
        observeDialogResult(
            CalendarStartDayPickerDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel
                        .onCalendarStartDayOfWeekSettingDialogPositiveResultReceived(
                            result.data
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
            ReminderNotificationTimePickerDialogFragment.RESULT_KEY
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

    /** 権限要求の理由説明ダイアログからの結果を監視する。 */
    private fun observePermissionDialogResult() {
        observeDialogResult<RequestPermissionType>(
            PermissionRationaleDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    when (result.data) {
                        RequestPermissionType.POST_NOTIFICATIONS -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestPostNotificationsPermission()
                            }
                        }
                        RequestPermissionType.ACCESS_LOCATION -> {
                            requestAccessLocationPermission()
                        }
                    }
                }
                DialogResult.Negative,
                DialogResult.Cancel -> { /*処理なし*/ }
            }
        }
    }

    /** 全日記削除確認ダイアログからの結果を監視する。 */
    private fun observeAllDiariesDeleteDialogResult() {
        observeDialogResult<Unit>(
            AllDiariesDeleteDialogFragment.RESULT_KEY
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
            AllSettingsInitializationDialogFragment.RESULT_KEY
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
            AllDataDeleteDialogFragment.RESULT_KEY
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
            is SettingsUiEvent.ShowThemeColorPickerDialog -> {
                navigateThemeColorPickerDialog()
            }
            is SettingsUiEvent.ShowCalendarStartDayPickerDialog -> {
                navigateCalendarStartDayPickerDialog(event.dayOfWeek)
            }
            is SettingsUiEvent.ShowReminderNotificationTimePickerDialog -> {
                navigateReminderNotificationTimePickerDialog()
            }
            is SettingsUiEvent.ShowAllDiariesDeleteDialog -> {
                navigateAllDiariesDeleteDialog()
            }
            is SettingsUiEvent.ShowAllSettingsInitializationDialog -> {
                navigateAllSettingsInitializationDialog()
            }
            is SettingsUiEvent.ShowAllDataDeleteDialog -> {
                navigateAllDataDeleteDialog()
            }
            is SettingsUiEvent.ShowOSSLicensesDialog -> {
                navigateOpenSourceSoftwareLicensesDialog()
            }
            is SettingsUiEvent.ShowNotificationPermissionRationaleDialog -> {
                navigatePostNotificationsPermissionRationaleDialog()
            }
            is SettingsUiEvent.ShowLocationPermissionRationaleDialog -> {
                navigateAccessLocationPermissionRationaleDialog()
            }
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

    //region CommonUiEventHandler Overrides
    override fun navigatePreviousFragment() {
        mainActivityViewModel.onNavigateBackFromBottomNavigationTab()
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
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
    /**
     * テーマカラー選択ダイアログ ([ThemeColorPickerDialogFragment])へ遷移する。
     */
    private fun navigateThemeColorPickerDialog() {
        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToThemeColorPickerDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * カレンダー開始曜日選択ダイアログ([CalendarStartDayPickerDialogFragment])へ遷移する。
     * @param dayOfWeek 現在設定されている週の開始曜日
     */
    private fun navigateCalendarStartDayPickerDialog(dayOfWeek: DayOfWeek) {
        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToCalendarStartDayPickerDialog(
                dayOfWeek
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** リマインダー通知時間選択ダイアログ([ReminderNotificationTimePickerDialogFragment])へ遷移する。 */
    private fun navigateReminderNotificationTimePickerDialog() {
        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToReminderNotificationTimePickerDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** 通知権限要求の理由説明ダイアログ([PermissionRationaleDialogFragment])へ遷移する。 */
    private fun navigatePostNotificationsPermissionRationaleDialog() {
        val directions =
            SettingsFragmentDirections
                .actionSettingsFragmentToPermissionRationaleDialog(RequestPermissionType.POST_NOTIFICATIONS)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** 位置情報権限要求の理由説明ダイアログ([PermissionRationaleDialogFragment])へ遷移する。 */
    private fun navigateAccessLocationPermissionRationaleDialog() {
        val directions =
            SettingsFragmentDirections
                .actionSettingsFragmentToPermissionRationaleDialog(RequestPermissionType.ACCESS_LOCATION)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** 全日記削除確認ダイアログ([AllDiariesDeleteDialogFragment])へ遷移する。 */
    private fun navigateAllDiariesDeleteDialog() {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllDiariesDeleteDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** 全設定初期化確認ダイアログ([AllSettingsInitializationDialogFragment])へ遷移する。 */
    private fun navigateAllSettingsInitializationDialog() {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllSettingsInitializationDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** 全データ削除確認ダイアログ([AllDataDeleteDialogFragment])へ遷移する。 */
    private fun navigateAllDataDeleteDialog() {
        val directions =
            SettingsFragmentDirections.actionSettingsFragmentToAllDataDeleteDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** OSSライセンスダイアログ([OpenSourceSoftwareLicensesDialogFragment])へ遷移する。 */
    private fun navigateOpenSourceSoftwareLicensesDialog() {
        val directions =
            SettingsFragmentDirections.actionNavigationSettingsFragmentToOpenSourceSoftwareLicensesDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

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
                navigatePostNotificationsPermissionRationaleDialog()
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
                navigateAccessLocationPermissionRationaleDialog()
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
}
