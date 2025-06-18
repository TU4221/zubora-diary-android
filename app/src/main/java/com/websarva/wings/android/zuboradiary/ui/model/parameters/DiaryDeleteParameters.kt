package com.websarva.wings.android.zuboradiary.ui.model.parameters

import android.net.Uri
import java.io.Serializable
import java.time.LocalDate


// MEMO:日記表示、編集フラグメント以外からも削除できるように下記データ構成とする。
internal data class DiaryDeleteParameters(
    val loadedDate: LocalDate,
    val loadedPicturePath: Uri?
) : Serializable
