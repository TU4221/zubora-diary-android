package com.websarva.wings.android.zuboradiary.domain.model

import android.net.Uri
import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.data.model.Condition
import com.websarva.wings.android.zuboradiary.data.model.Weather
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime

@Parcelize // MEMO:"@Parcelize"でSavedStateHandle対応
internal data class Diary(
    val date: LocalDate,
    val log: LocalDateTime,
    val weather1: Weather,
    val weather2: Weather,
    val condition: Condition,
    val title: String,
    val item1Title: String,
    val item1Comment: String,
    val item2Title: String,
    val item2Comment: String,
    val item3Title: String,
    val item3Comment: String,
    val item4Title: String,
    val item4Comment: String,
    val item5Title: String,
    val item5Comment: String,
    val picturePath: Uri?
) : Parcelable {

    /**
     * Diaryオブジェクトの内容を比較します。
     * `log` プロパティは比較対象から除外されます。
     *
     * @param other 比較対象のDiaryオブジェクト。nullの場合はfalseを返します。
     * @return `log` を除いた他の全てのプロパティが等しい場合はtrue、そうでない場合はfalse。
     */
    fun isContentEqualToIgnoringLog(other: Diary?): Boolean {
        // 同じインスタンスまたは相手がnullの場合は早期リターン
        if (this === other) return true
        if (other == null) return false

        // log以外の全てのプロパティを比較
        return this.date == other.date &&
                // this.log == other.log (この行を除外)
                this.weather1 == other.weather1 &&
                this.weather2 == other.weather2 &&
                this.condition == other.condition &&
                this.title == other.title &&
                this.item1Title == other.item1Title &&
                this.item1Comment == other.item1Comment &&
                this.item2Title == other.item2Title &&
                this.item2Comment == other.item2Comment &&
                this.item3Title == other.item3Title &&
                this.item3Comment == other.item3Comment &&
                this.item4Title == other.item4Title &&
                this.item4Comment == other.item4Comment &&
                this.item5Title == other.item5Title &&
                this.item5Comment == other.item5Comment &&
                this.picturePath == other.picturePath
    }
}
