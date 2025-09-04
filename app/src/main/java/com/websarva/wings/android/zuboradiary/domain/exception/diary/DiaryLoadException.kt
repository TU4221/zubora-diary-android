package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.LocalDate

/**
 * 特定の日記の読み込み処理中にエラーが発生した場合にスローされる例外基底クラス。
 *
 * @param message エラーメッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。nullの場合もある。
 */
internal sealed class DiaryLoadException (
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause) {

    /**
     * 日記データへのアクセスに失敗し、読み込みができなかった場合にスローされる例外。
     *
     * @param date アクセスに失敗した日記の日付。特定の日付が関連しない場合は `null` (デフォルト値)。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class AccessFailure(
        date: LocalDate? = null,
        cause: Throwable
    ) :DiaryLoadException(
        (if (date == null) "" else "指定された日付 '$date' の") + "日記の読込に失敗しました。" ,
        cause
    )

    /**
     * 読込対象日付の日記が見つからなかった場合にスローされる例外。
     *
     * @param date 日記読込対象の日付。特定の日付が関連しない場合は `null` (デフォルト値)。
     */
    class DataNotFound(
        date: LocalDate? = null
    ) :DiaryLoadException(
        (if (date == null) "" else "指定された日付 '$date' の") + "日記が見つかりませんでした。" )
}
