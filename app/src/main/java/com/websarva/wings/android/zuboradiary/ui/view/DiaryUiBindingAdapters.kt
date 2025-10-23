package com.websarva.wings.android.zuboradiary.ui.view

import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.diary.ConditionUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.utils.asString
import com.websarva.wings.android.zuboradiary.ui.utils.formatDateString
import com.websarva.wings.android.zuboradiary.ui.utils.formatDateTimeWithSecondsString

internal object DiaryUiBindingAdapters {

    @JvmStatic
    @BindingAdapter("diaryDateText")
    fun setDiaryDateText(toolbar: Toolbar, diaryLoadState: LoadState<DiaryUi>) {
        if (diaryLoadState is LoadState.Success) {
            val diaryUi = diaryLoadState.data
            val dateText = diaryUi.date.formatDateString(toolbar.context)
            toolbar.title?.let {
                if (it.toString() == dateText) return
            }

            toolbar.title = dateText
        }
    }

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

    @BindingAdapter("diaryWeatherSpinnerText")
    @JvmStatic
    fun setDiaryWeatherSpinnerText(view: AutoCompleteTextView, weather: WeatherUi) {
        val newText = weather.asString(view.context)
        if (view.text.toString() != newText) {
            view.setText(newText, false)
        }
    }

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
            Log.d("20251021", "diaryItemNumber: $diaryItemNumber")
            val diaryUi = diaryLoadState.data
            val diaryItemTitle =
                when (diaryItemNumber) {
                    1 -> diaryUi.item1Title
                    2 -> diaryUi.item2Title
                    3 -> diaryUi.item3Title
                    4 -> diaryUi.item4Title
                    5 -> diaryUi.item5Title
                    else -> throw IllegalArgumentException("指定された日記項目番号($diaryItemNumber)が不正値。")
                }
            if (textView.text.toString() != diaryItemTitle) {
                textView.text = diaryItemTitle
            }
        }
    }

    @BindingAdapter("diaryConditionSpinnerText")
    @JvmStatic
    fun setDiaryConditionSpinnerText(view: AutoCompleteTextView, condition: ConditionUi) {
        val newText = condition.asString(view.context)
        if (view.text.toString() != newText) {
            view.setText(newText, false)
        }
    }

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
            val diaryItemComment =
                when (diaryItemNumber) {
                    1 -> diaryUi.item1Comment
                    2 -> diaryUi.item2Comment
                    3 -> diaryUi.item3Comment
                    4 -> diaryUi.item4Comment
                    5 -> diaryUi.item5Comment
                    else -> throw IllegalArgumentException("指定された日記項目番号($diaryItemNumber)が不正値。")
                }
            if (textView.text.toString() != diaryItemComment) {
                textView.text = diaryItemComment
            }
        }
    }
}
