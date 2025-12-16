package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.picker

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.websarva.wings.android.zuboradiary.ui.common.theme.timePickerDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.main.MainActivity
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.setResult
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.DialogResult
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import kotlin.getValue

/**
 * アプリ内で共通して使用される、[MaterialTimePicker]を使用した時刻選択ダイアログ。
 * 最初に表示する日付は Navigation Component の引数を通じて外部から注入される。
 *
 * 以下の責務を持つ:
 * - [MaterialTimePicker]のセットアップと表示
 * - テーマカラーに応じたダイアログスタイルの適用
 * - 24時間形式、クロック入力モードの指定
 * - Positive/Negativeボタンクリック、およびキャンセル時の処理
 */
@AndroidEntryPoint
class TimePickerDialogFragment : DialogFragment() {

    /** 画面遷移時に渡された引数。 */
    private val navArgs: TimePickerDialogFragmentArgs by navArgs()

    /** [MainActivity]から取得する現在のテーマカラー。 */
    private val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    /** [MaterialTimePicker]の表示と、戻り値用のダミーダイアログの生成を行う。 */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(logTag, "onCreateDialog()")

        // MEMO:MaterialTimePickerはDialogクラスを作成できないのでダミーDialogを作成して戻り値として返し
        //      MaterialTimePicker#show()でTimePickerDialogを表示する。ダミーDialogも重なって表示されるので、
        //      MaterialTimePickerに追加したリスナーでダミーDialogを閉じる(Dialog#dismiss())。
        val dummyDialog = Dialog(requireContext())

        val timePicker = createTimePickerDialog(dummyDialog)
        timePicker.show(childFragmentManager, "")

        return dummyDialog
    }

    /**
     * MaterialTimePickerのインスタンスを生成し、各種設定を行う。
     * @param dummyDialog MaterialTimePicker表示後に閉じるためのダミーダイアログ
     * @return 設定済みのMaterialTimePickerインスタンス
     */
    private fun createTimePickerDialog(dummyDialog: Dialog): MaterialTimePicker {
        val themeResId = themeColor.timePickerDialogThemeResId
        val initialTime = navArgs.params.initialTime

        val timePicker =
            MaterialTimePicker.Builder()
                .setTheme(themeResId)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(INPUT_MODE_CLOCK)
                .setHour(initialTime.hour)
                .setMinute(initialTime.minute)
                .build()

        setupOnClickListener(timePicker, dummyDialog)

        return timePicker
    }

    /**
     * TimePickerの各種ボタンクリック、およびキャンセル時のリスナーを設定する。
     * @param timePicker リスナーを設定するMaterialTimePicker
     * @param dummyDialog ボタン押下後に閉じるためのダミーダイアログ
     */
    private fun setupOnClickListener(timePicker: MaterialTimePicker, dummyDialog: Dialog) {
        timePicker.addOnPositiveButtonClickListener { _ ->
            Log.d(logTag, "onClick()_PositiveButton")

            // 選択日付型変換(Int -> LocalTime)
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            val selectedTime = LocalTime.of(selectedHour, selectedMinute)
            setResult(navArgs.params.resultKey, DialogResult.Positive(selectedTime))
            dummyDialog.dismiss()
        }

        timePicker.addOnNegativeButtonClickListener {
            Log.d(logTag, "onClick()_NegativeButton")
            setResult(navArgs.params.resultKey, DialogResult.Negative)
            dummyDialog.dismiss()
        }

        timePicker.addOnCancelListener {
            Log.d(logTag, "onCancel()")
            setResult(navArgs.params.resultKey, DialogResult.Cancel)
            dummyDialog.dismiss()
        }
    }
}
