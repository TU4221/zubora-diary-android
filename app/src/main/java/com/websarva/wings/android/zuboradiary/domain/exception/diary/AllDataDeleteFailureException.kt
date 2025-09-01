package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * データベースの全てのレコードの削除処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * この例外は、データベース操作の失敗など、削除プロセスにおける何らかの問題を示唆する。
 *
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class AllDataDeleteFailureException (
    cause: Throwable
    ) : DomainException("すべての日記データの削除に失敗しました。", cause)
