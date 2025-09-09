package com.websarva.wings.android.zuboradiary.domain

/**
 * ドメイン層で扱う例外の基底クラス。
 *
 * 主にデータ層で発生した例外をドメイン層のコンテキストに合わせた形でラップするために使用される。
 * これにより、ドメイン層とデータ層間での例外処理の関心を分離し、
 * ドメイン層ではこの `DomainException` を継承した型の例外のみを取り扱うことを目的とする。
 *
 * アプリケーション固有の業務ルール違反や、ドメインロジック内での予期せぬ状態が発生した場合にも、
 * このクラスを継承して具体的な例外クラスを定義する。
 *
 * @param message この例外に関する詳細メッセージ。
 * @param cause この例外の根本原因となった[Throwable]。データ層で発生した元の例外を含むことが多い。nullの場合もある。
 */
internal abstract class TestDomainException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
