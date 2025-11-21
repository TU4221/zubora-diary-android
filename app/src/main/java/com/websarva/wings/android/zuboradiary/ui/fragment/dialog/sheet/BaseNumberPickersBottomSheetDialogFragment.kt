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
import com.websarva.wings.android.zuboradiary.databinding.DialogNumberPickersBinding
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSurfaceVariantColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.numberPickerBottomSheetDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag

/**
 * 複数の[NumberPicker]を持つボトムシートダイアログの基底クラス。
 *
 * 以下の責務を持つ:
 * - [BaseBottomSheetDialogFragment]の機能の継承
 * - 複数の[NumberPicker]を持つ共通レイアウトの提供
 * - テーマカラーに応じた[NumberPicker]のテーマ適用とテキストカラー設定
 * - 決定/キャンセルボタンのクリックイベント処理
 * - [NumberPicker]の初期化処理の移譲
 */
abstract class BaseNumberPickersBottomSheetDialogFragment
    : BaseBottomSheetDialogFragment<DialogNumberPickersBinding>() {

    /** 追加処理として、ボタンのリスナー設定とNumberPickerの初期化を行う。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            buttonDecision.setOnClickListener {
                Log.d(logTag, "onClick()_PositiveButton")
                handleOnPositiveButtonClick(
                    numberPickerFirst.value,
                    numberPickerSecond.value,
                    numberPickerThird.value
                )
                navigatePreviousFragment()
            }

            buttonCancel.setOnClickListener {
                Log.d(logTag, "onClick()_NegativeButton")
                handleOnNegativeButtonClick()
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
     * 決定ボタンがクリックされた際の処理を定義する。
     * @param firstPickerValue 1つ目のNumberPickerで選択された値
     * @param secondPickerValue 2つ目のNumberPickerで選択された値
     * @param thirdPickerValue 3つ目のNumberPickerで選択された値
     */
    protected abstract fun handleOnPositiveButtonClick(
        firstPickerValue: Int,
        secondPickerValue: Int,
        thirdPickerValue: Int
    )

    /**
     * キャンセルボタンがクリックされた際の処理を定義する。
     */
    protected abstract fun handleOnNegativeButtonClick()

    /**
     * 各NumberPickerの初期設定（最小値、最大値、初期値など）を行う。
     */
    protected abstract fun setupNumberPickers()
}
