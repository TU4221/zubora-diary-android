package com.websarva.wings.android.zuboradiary.ui.model.parameters

import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import java.io.Serializable
import java.time.LocalDate


// MEMO:日記表示、編集フラグメント以外からも削除できるように下記データ構成とする。
internal data class DiaryDeleteParameters(
    val date: LocalDate,
    val imageFileName: ImageFileName? // TODO:IDを持たせるように変更する
) : Serializable
