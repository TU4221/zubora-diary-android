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
) : Parcelable
