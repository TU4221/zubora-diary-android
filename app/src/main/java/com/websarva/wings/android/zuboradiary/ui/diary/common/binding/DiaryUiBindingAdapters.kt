package com.websarva.wings.android.zuboradiary.ui.diary.common.binding

import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.common.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.common.utils.formatDateString
import com.websarva.wings.android.zuboradiary.ui.common.utils.formatDateTimeWithSecondsString
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.ConditionUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.utils.asString

/**
 * 日記に関連するカスタムBinding Adapterを定義するオブジェクト。
 */
internal object DiaryUiBindingAdapters {

    /**
     * [com.websarva.wings.android.zuboradiary.ui.common.state.LoadState]から日記([com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryUi])の日付を取得し、
     * 日本語の日付書式にフォーマットして[android.widget.TextView]に設定する。
     * @param textView 対象のTextView。
     * @param diaryLoadState 日記の読み込み状態。
     */
    @JvmStatic
    @BindingAdapter("diaryDateText")
    fun setDiaryDateText(textView: TextView, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            val dateText = diaryUi.date.formatDateString(textView.context)
            if (textView.text.toString() != dateText) {
                textView.text = dateText
            }
        }
    }

    /**
     * [LoadState]から元の日記([DiaryUi])の日付を取得し、「編集対象日記：」という接頭辞をつけて[TextView]に設定する。
     * @param textView 対象のTextView。
     * @param diaryLoadState 日記の読み込み状態。
     */
    @JvmStatic
    @BindingAdapter("originalDiaryDateText")
    fun setOriginalDiaryDateText(textView: TextView, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            val date = diaryUi.date
            val dateText = textView.context.getString(
                R.string.fragment_diary_edit_editing_diary,
                date.formatDateString(textView.context)
            )
            if (textView.text.toString() != dateText) {
                textView.text = dateText
            }
        }
    }

    /**
     * [LoadState]から日記([DiaryUi])の最終更新日時を取得し、
     * 日本語の日付書式にフォーマットして[TextView]に設定する。
     * @param textView 対象のTextView。
     * @param diaryLoadState 日記の読み込み状態。
     */
    @JvmStatic
    @BindingAdapter("diaryLogText")
    fun setDiaryLogText(textView: TextView, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            val logText = diaryUi.log.formatDateTimeWithSecondsString(textView.context)
            if (textView.text.toString() != logText) {
                textView.text = logText
            }
        }
    }

    /**
     * [LoadState]から日記([DiaryUi])の天気1を取得し、文字列に変換して[TextView]に設定する。
     * @param textView 対象のTextView。
     * @param diaryLoadState 日記の読み込み状態。
     */
    @JvmStatic
    @BindingAdapter("diaryWeather1Text")
    fun setDiaryWeather1Text(textView: TextView, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            val weatherText = diaryUi.weather1.asString(textView.context)
            if (textView.text.toString() != weatherText) {
                textView.text = weatherText
            }
        }
    }

    /**
     * [LoadState]から日記([DiaryUi])の天気2を取得し、文字列に変換して[TextView]に設定する。
     * @param textView 対象のTextView。
     * @param diaryLoadState 日記の読み込み状態。
     */
    @JvmStatic
    @BindingAdapter("diaryWeather2Text")
    fun setDiaryWeather2Text(textView: TextView, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            val weatherText = diaryUi.weather2.asString(textView.context)
            if (textView.text.toString() != weatherText) {
                textView.text = weatherText
            }
        }
    }

    /**
     * [LoadState]から日記([DiaryUi])の体調を取得し、文字列に変換して[TextView]に設定する。
     * @param textView 対象のTextView。
     * @param diaryLoadState 日記の読み込み状態。
     */
    @JvmStatic
    @BindingAdapter("diaryConditionText")
    fun setDiaryConditionText(textView: TextView, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            val weatherText = diaryUi.condition.asString(textView.context)
            if (textView.text.toString() != weatherText) {
                textView.text = weatherText
            }
        }
    }

    /**
     * [LoadState]から日記([DiaryUi])のタイトルを取得し、[TextView]に設定する。
     * @param textView 対象のTextView。
     * @param diaryLoadState 日記の読み込み状態。
     */
    @JvmStatic
    @BindingAdapter("diaryTitleText")
    fun setDiaryTitleText(textView: TextView, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            if (textView.text.toString() != diaryUi.title) {
                textView.text = diaryUi.title
            }
        }
    }

    /**
     * [com.websarva.wings.android.zuboradiary.ui.diary.common.model.WeatherUi]を文字列に変換し、[android.widget.AutoCompleteTextView]に設定する。
     * この時、ドロップダウンリストは表示しない。
     * @param view 対象のAutoCompleteTextView。
     * @param weather 設定する天気。
     */
    @BindingAdapter("diaryWeatherSpinnerText")
    @JvmStatic
    fun setDiaryWeatherSpinnerText(view: AutoCompleteTextView, weather: WeatherUi) {
        val newText = weather.asString(view.context)
        if (view.text.toString() != newText) {
            view.setText(newText, false)
        }
    }

    /**
     * [com.websarva.wings.android.zuboradiary.ui.diary.common.model.ConditionUi]を文字列に変換し、[AutoCompleteTextView]に設定する。
     * この時、ドロップダウンリストは表示しない。
     * @param view 対象のAutoCompleteTextView。
     * @param condition 設定する体調。
     */
    @BindingAdapter("diaryConditionSpinnerText")
    @JvmStatic
    fun setDiaryConditionSpinnerText(view: AutoCompleteTextView, condition: ConditionUi) {
        val newText = condition.asString(view.context)
        if (view.text.toString() != newText) {
            view.setText(newText, false)
        }
    }

    /**
     * [LoadState]の日記([DiaryUi])と項目番号から日記項目のタイトルを取得し、[TextView]に設定する。
     * @param textView 対象のTextView。
     * @param diaryItemNumber 項目番号。
     * @param diaryLoadState 日記の読み込み状態。
     */
    @JvmStatic
    @BindingAdapter(
        value = [
            "diaryItemNumber",
            "diaryItemTitleText"
        ],
        requireAll = true // 全ての属性が指定されれば呼び出される
    )
    fun setDiaryItemTitleText(textView: TextView, diaryItemNumber: Int, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            val diaryItemTitle = diaryUi.itemTitles[diaryItemNumber]
            if (textView.text.toString() != diaryItemTitle) {
                textView.text = diaryItemTitle
            }
        }
    }

    /**
     * 項目番号を「項目X」という書式に変換し、[androidx.appcompat.widget.Toolbar]のタイトルとして設定する。
     * @param toolbar 対象のToolbar。
     * @param number 項目番号。
     */
    @JvmStatic
    @BindingAdapter("diaryItemNumberText")
    fun setDiaryItemNumberText(toolbar: Toolbar, number: Int) {
        val dateText =
            toolbar.context.getString(
                R.string.dialog_diary_item_title_edit_toolbar_title,
                number.toString()
            )
        toolbar.title?.let {
            if (it.toString() == dateText) return
        }

        toolbar.title = dateText
    }

    /**
     * [LoadState]の日記([DiaryUi])と項目番号から日記項目のコメントを取得し、[TextView]に設定する。
     * @param textView 対象のTextView。
     * @param diaryItemNumber 項目番号。
     * @param diaryLoadState 日記の読み込み状態。
     */
    @JvmStatic
    @BindingAdapter(
        value = [
            "diaryItemNumber",
            "diaryItemCommentText"
        ],
        requireAll = true // 全ての属性が指定されれば呼び出される
    )
    fun setDiaryItemCommentText(textView: TextView, diaryItemNumber: Int, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            val diaryItemComment = diaryUi.itemComments[diaryItemNumber]
            if (textView.text.toString() != diaryItemComment) {
                textView.text = diaryItemComment
            }
        }
    }
}
