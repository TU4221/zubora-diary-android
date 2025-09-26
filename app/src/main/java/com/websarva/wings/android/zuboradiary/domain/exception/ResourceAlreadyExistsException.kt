package com.websarva.wings.android.zuboradiary.domain.exception

/**
 * 作成または保存しようとしたリソースが、指定された識別子や場所で既に存在することを示す例外。
 *
 * この例外は、一意であるべきリソースが既に存在しているために操作を完了できない場合にスローされる。
 *
 *  @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 *                デフォルトは「指定されたリソースは既に存在します。」
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class ResourceAlreadyExistsException(
    message: String = "指定されたリソースは既に存在します。",
    cause: Throwable? = null
) : DomainException(message, cause)
