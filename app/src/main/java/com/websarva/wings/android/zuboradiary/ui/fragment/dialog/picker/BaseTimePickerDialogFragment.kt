package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.websarva.wings.android.zuboradiary.ui.utils.timePickerDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime

/**
 * [MaterialTimePicker]を使用した時刻選択ダイアログの基底クラス。
 *
 * 以下の責務を持つ:
 * - [MaterialTimePicker]のセットアップと表示
 * - テーマカラーに応じたダイアログスタイルの適用
 * - 24時間形式、クロック入力モードの指定
 * - Positive/Negativeボタンクリック、およびキャンセル時のコールバック処理
 */
@AndroidEntryPoint
abstract class BaseTimePickerDialogFragment : DialogFragment() {

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
        val builder = MaterialTimePicker.Builder()

        val themeResId = themeColor.timePickerDialogThemeResId
        builder.setTheme(themeResId)

        builder
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(INPUT_MODE_CLOCK)

        setupInitializationTime(builder)

        val timePicker = builder.build()

        setupOnClickListener(timePicker, dummyDialog)

        return timePicker
    }

    /**
     * TimePickerに表示する初期時刻を設定する。[createTimePickerDialog] で呼び出される。
     * @param builder 初期時刻を設定する対象のビルダー
     */
    protected abstract fun setupInitializationTime(builder: MaterialTimePicker.Builder)

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
            val localTime = LocalTime.of(selectedHour, selectedMinute)
            handleOnPositiveButtonClick(localTime)
            dummyDialog.dismiss()
        }

        timePicker.addOnNegativeButtonClickListener {
            Log.d(logTag, "onClick()_NegativeButton")
            handleOnNegativeButtonClick()
            dummyDialog.dismiss()
        }

        timePicker.addOnCancelListener {
            Log.d(logTag, "onCancel()")
            handleOnCancel()
            dummyDialog.dismiss()
        }
    }

    /**
     * Positiveボタンがクリックされた際の処理を定義する。
     * @param selectedTime ユーザーが選択した時刻
     */
    protected abstract fun handleOnPositiveButtonClick(selectedTime: LocalTime)

    /**
     * Negativeボタンがクリックされた際の処理を定義する。
     */
    protected abstract fun handleOnNegativeButtonClick()

    /**
     * ダイアログがキャンセルされた際の処理を定義する。
     */
    protected abstract fun handleOnCancel()
}
