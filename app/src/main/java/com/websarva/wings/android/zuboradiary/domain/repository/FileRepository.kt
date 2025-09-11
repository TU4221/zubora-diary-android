package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.model.ImageSize
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException

/**
 * ファイル関連へのアクセスと永続化を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、ファイルの操作機能を提供。
 *
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外([UseCaseException] のサブクラス) をスローする。
 */
internal interface FileRepository {

    /**
     * 指定されたUriを画像ファイルに変換してキャッシュディレクトリへ保存する。
     *
     * @param uriString 保存したいUri。
     * @param fileBaseName 保存されるファイルのベース名。
     * @return キャッシュディレクトリへ保存されたファイルパス。
     * @throws DataStorageException ファイルの保存に失敗した場合。
     */
    suspend fun saveImageFileToCache(uriString: String, fileBaseName: String, size: ImageSize): String

    /**
     * 指定されたファイルを永続ディレクトリへ移動する。
     *
     * @param filePath 移動したいファイルパス。
     * @return 永続ディレクトリへ保存されたファイルパス。
     * @throws DataStorageException ファイルの移動に失敗した場合。
     */
    suspend fun moveFileToPermanent(filePath: String): String

    /**
     * 指定されたファイルを削除する。
     *
     * @param filePath 削除したいファイルパス。
     * @throws DataStorageException ファイルの削除に失敗した場合。
     */
    suspend fun deleteFile(filePath: String)
}
