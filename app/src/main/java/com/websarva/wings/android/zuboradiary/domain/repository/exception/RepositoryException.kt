package com.websarva.wings.android.zuboradiary.domain.repository.exception

/**
 * リポジトリ層で発生しうる例外の基底クラス。
 *
 * 主にデータ層で発生した技術的な例外（I/Oエラー、ネットワークエラー、データベースエラーなど）を、
 * ドメイン層に合わせた形で抽象化（ラップ）するために使用される。
 * これにより、ドメイン層とデータ層間での例外処理の関心を分離し、
 * ドメイン層ではこの `RepositoryException` を継承した型の例外のみを取り扱うことを目指す。
 *
 * @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 * @param cause この例外の根本原因となった[Throwable]。データ層で発生した元の例外を含む。根本原因がない場合はnullとなる。
 */
internal abstract class RepositoryException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
