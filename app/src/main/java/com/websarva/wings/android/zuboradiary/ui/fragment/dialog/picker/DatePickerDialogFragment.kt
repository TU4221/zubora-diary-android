package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.websarva.wings.android.zuboradiary.ui.utils.datePickerDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.navigation.DatePickerArgs
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.getValue

/**
 * アプリ内で共通して使用される、[MaterialDatePicker]を使用した日付選択ダイアログ。
 * 最初に表示する日付は [DatePickerArgs] を通じて外部から注入される。
 *
 * 以下の責務を持つ:
 * - [MaterialDatePicker]のセットアップと表示
 * - テーマカラーに応じたダイアログスタイルの適用
 * - UTCを基準とした日付の変換と初期選択日付の設定
 * - Positive/Negativeボタンクリック、およびキャンセル時の処理
 */
class DatePickerDialogFragment : DialogFragment() {

    /** ナビゲーション引数。 */
    private val navArgs: DatePickerDialogFragmentArgs by navArgs()

    /** [navArgs]から取り出した、ダイアログの表示内容を定義する引数。 */
    private val datePickerArgs: DatePickerArgs get() = navArgs.datePickerArgs

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

        val initialDate = datePickerArgs.initialDate
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
            setResult(datePickerArgs.resultKey, DialogResult.Positive(selectedDate))
            dummyDialog.dismiss()
        }

        datePicker.addOnNegativeButtonClickListener {
            Log.d(logTag, "onClick()_NegativeButton")
            setResult(datePickerArgs.resultKey, DialogResult.Negative)
            dummyDialog.dismiss()
        }

        datePicker.addOnCancelListener {
            Log.d(logTag, "onCancel()")
            setResult(datePickerArgs.resultKey, DialogResult.Cancel)
            dummyDialog.dismiss()
        }
    }
}
