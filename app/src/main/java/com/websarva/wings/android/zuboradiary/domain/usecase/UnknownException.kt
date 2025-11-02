package com.websarva.wings.android.zuboradiary.domain.usecase

/**
 * UseCaseの実行時に発生した、ビジネスロジック上想定されていない
 * 未知の例外であることを示すためのマーカーインターフェース。
 *
 * このインターフェースを実装する例外は、アプリ全体で共通の
 *「予期せぬエラー」として扱われるべきであることを示す。
 */
internal interface UnknownException
