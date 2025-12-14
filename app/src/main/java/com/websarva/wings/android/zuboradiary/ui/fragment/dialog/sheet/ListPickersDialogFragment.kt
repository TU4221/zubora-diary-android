package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.navArgs
import com.websarva.wings.android.zuboradiary.databinding.DialogNumberPickersBinding
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSurfaceVariantColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.numberPickerBottomSheetDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.navigation.params.ListPickersArgs
import com.websarva.wings.android.zuboradiary.ui.navigation.params.ListPickersResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import kotlin.getValue

/**
 * アプリ内で共通して使用される、
 * 汎用的な複数のドラムロールピッカー([NumberPicker])を動的に表示するボトムシートダイアログ。
 * 表示する内容は [ListPickersArgs] を通じて外部から注入される。
 *
 * 以下の責務を持つ:
 * - [BaseBottomSheetDialogFragment]の機能の継承
 * - [NumberPicker]の初期化処理
 * - テーマカラーに応じた[NumberPicker]のテーマ適用とテキストカラー設定
 * - 決定/キャンセルボタンのクリックイベント処理
 */
class ListPickersDialogFragment
    : BaseBottomSheetDialogFragment<DialogNumberPickersBinding>() {

    /** ナビゲーション引数。 */
    private val navArgs: ListPickersDialogFragmentArgs by navArgs()

    /** [navArgs]から取り出した、ダイアログの表示内容を定義する引数。 */
    private val listPickersArgs: ListPickersArgs get() = navArgs.listPickersArgs

    /** 追加処理として、ボタンのリスナー設定とNumberPickerの初期化を行う。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            buttonDecision.setOnClickListener {
                Log.d(logTag, "onClick()_PositiveButton")
                val result =
                    ListPickersResult(
                        numberPickerFirst.value,
                        numberPickerSecond.value,
                        numberPickerThird.value
                    )
                setResult(listPickersArgs.resultKey, DialogResult.Positive(result))
                navigatePreviousFragment()
            }

            buttonCancel.setOnClickListener {
                Log.d(logTag, "onClick()_NegativeButton")
                setResult(listPickersArgs.resultKey, DialogResult.Negative)
                navigatePreviousFragment()
            }
        }

        setupNumberPickerTextColor(binding)
        setupNumberPickers()
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogNumberPickersBinding {

        // HACK:下記理由から、ThemeColor#getNumberPickerBottomSheetDialogThemeResId()から
        //      ThemeResIdを取得してInflaterを再作成。
        //      ・NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
        //      ・ThemeColorBlackの時は背景が黒となり、NumberPickerの値が見えない。
        val themeResId = themeColor.numberPickerBottomSheetDialogThemeResId
        val contextWithTheme: Context = ContextThemeWrapper(requireActivity(), themeResId)
        val cloneInflater = inflater.cloneInContext(contextWithTheme)

        return DialogNumberPickersBinding
            .inflate(cloneInflater, container, false)
    }

    /** 追加処理として、リスナの解放を行う。*/
    override fun clearViewBindings() {
        with(binding) {
            buttonDecision.setOnClickListener(null)
            buttonCancel.setOnClickListener (null)
        }

        super.clearViewBindings()
    }

    override fun handleOnCancel() {
        setResult(listPickersArgs.resultKey, DialogResult.Cancel)
    }

    /**
     * NumberPickerのテキストカラーをテーマに合わせて設定する。
     * @param binding カラーを設定するNumberPickerを含むViewBinding
     */
    // HACK:NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
    private fun setupNumberPickerTextColor(binding: DialogNumberPickersBinding) {
        if (Build.VERSION.SDK_INT >= 29) {
            val onSurfaceVariantColor = themeColor.asOnSurfaceVariantColorInt(resources)
            with(binding) {
                numberPickerFirst.textColor = onSurfaceVariantColor
                numberPickerSecond.textColor = onSurfaceVariantColor
                numberPickerThird.textColor = onSurfaceVariantColor
            }
        }
    }

    /**
     * 各NumberPickerの初期設定（最小値、最大値、初期値など）を行う。
     */
    private fun setupNumberPickers() {
        val pickers =
            listOf(
                binding.numberPickerFirst,
                binding.numberPickerSecond,
                binding.numberPickerThird
            )

        pickers.forEachIndexed { index, picker ->
            val config = listPickersArgs.pickerConfigs.getOrNull(index)
            if (config != null) {
                picker.apply {
                    visibility = View.VISIBLE
                    minValue = 0
                    maxValue = config.items.size - 1
                    displayedValues = config.items.toTypedArray()
                    value = config.initialIndex
                    wrapSelectorWheel = false
                }
            } else {
                picker.visibility = View.GONE
            }
        }
    }
}
