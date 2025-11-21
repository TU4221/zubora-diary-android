package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.websarva.wings.android.zuboradiary.ui.utils.datePickerDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * [MaterialDatePicker]を使用した日付選択ダイアログの基底クラス。
 *
 * 以下の責務を持つ:
 * - [MaterialDatePicker]のセットアップと表示
 * - テーマカラーに応じたダイアログスタイルの適用
 * - UTCを基準とした日付の変換と初期選択日付の設定
 * - Positive/Negativeボタンクリック、およびキャンセル時のコールバック処理
 */
abstract class BaseDatePickerDialogFragment : DialogFragment() {

    /** [MainActivity]から取得する現在のテーマカラー。 */
    private val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    /** [MaterialDatePicker]の表示と、戻り値用のダミーダイアログの生成を行う。 */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(logTag, "onCreateDialog()")

        // MEMO:MaterialDatePickerはDialogクラスを作成できないのでダミーDialogを作成して戻り値として返し
        //      MaterialDatePicker#show()でDatePickerDialogを表示する。ダミーDialogも重なって表示されるので、
        //      MaterialDatePickerに追加したリスナーでダミーDialogを閉じる(Dialog#dismiss())。
        val dummyDialog = Dialog(requireContext())

        val datePicker = createDatePickerDialog(dummyDialog)
        datePicker.show(childFragmentManager, "")

        return dummyDialog
    }

    /**
     * MaterialDatePickerのインスタンスを生成し、各種設定を行う。
     * @param dummyDialog MaterialDatePicker表示後に閉じるためのダミーダイアログ
     * @return 設定済みのMaterialDatePickerインスタンス
     */
    private fun createDatePickerDialog(dummyDialog: Dialog): MaterialDatePicker<Long> {
        val builder = MaterialDatePicker.Builder.datePicker()

        val themeResId = themeColor.datePickerDialogThemeResId
        builder.setTheme(themeResId)

        val initialDate = createInitialDate()
        // MEMO：MaterialDatePickerはUTC基準の為UTC基準で変換
        val initialEpochMilli =
            initialDate
                .atStartOfDay(ZoneOffset.UTC) // UTCでのその日の始まりの時刻(00:00)を取得
                .toInstant() // UTC基準の時点に変換
                .toEpochMilli()
        builder.setSelection(initialEpochMilli)

        val datePicker = builder.build()

        setupOnClickListener(datePicker, dummyDialog)

        return datePicker
    }

    /**
     * DatePickerに表示する初期日付を生成する。[createDatePickerDialog] で呼び出される。
     * @return 初期選択状態にする日付
     */
    protected abstract fun createInitialDate(): LocalDate

    /**
     * DatePickerの各種ボタンクリック、およびキャンセル時のリスナーを設定する。
     * @param datePicker リスナーを設定するMaterialDatePicker
     * @param dummyDialog ボタン押下後に閉じるためのダミーダイアログ
     */
    private fun setupOnClickListener(datePicker: MaterialDatePicker<Long>, dummyDialog: Dialog) {
        datePicker.addOnPositiveButtonClickListener { selection: Long ->
            Log.d(logTag, "onClick()_PositiveButton")

            // 選択日付型変換(EpochMilli -> LocalDate)
            val instant = Instant.ofEpochMilli(selection)
            val selectedDate = LocalDate.ofInstant(instant, ZoneId.systemDefault())
            handleOnPositiveButtonClick(selectedDate)
            dummyDialog.dismiss()
        }

        datePicker.addOnNegativeButtonClickListener {
            Log.d(logTag, "onClick()_NegativeButton")
            handleOnNegativeButtonClick()
            dummyDialog.dismiss()
        }

        datePicker.addOnCancelListener {
            Log.d(logTag, "onCancel()")
            handleOnCancel()
            dummyDialog.dismiss()
        }
    }

    /**
     * Positiveボタンがクリックされた際の処理を定義する。
     * @param selectedDate ユーザーが選択した日付
     */
    protected abstract fun handleOnPositiveButtonClick(selectedDate: LocalDate)

    /**
     * Negativeボタンがクリックされた際の処理を定義する。
     */
    protected abstract fun handleOnNegativeButtonClick()

    /**
     * ダイアログがキャンセルされた際の処理を定義する。
     */
    protected abstract fun handleOnCancel()
}
