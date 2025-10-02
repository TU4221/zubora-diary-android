package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.model.FileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.PermissionException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceAlreadyExistsException

/**
 * ファイル関連へのアクセスと永続化を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、ファイルの操作機能を提供。
 *
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外([UseCaseException] のサブクラス) をスローする。
 */
internal interface FileRepository {

    /**
     * 指定された画像ファイル名からキャッシュ相対パスを構築する。
     *
     * @param fileName 相対パスを構築する対象のファイル名。
     * @return 構築されたファイルパス。
     */
    fun buildImageFileAbsolutePathFromCache(fileName: FileName): String

    /**
     * 指定された画像ファイル名から永続ストレージ相対パスを構築する。
     *
     * @param fileName 相対パスを構築する対象のファイル名。
     * @return 構築されたファイルパス。
     */
    fun buildImageFileAbsolutePathFromPermanent(fileName: FileName): String

    /**
     * 指定された画像ファイルがキャッシュに存在するか確認する。
     *
     * @param fileName 確認したいファイル名。
     * @return 指定された画像ファイルが存在すれば `true`、存在しなければ `false`。
     * @throws PermissionException ファイルへのアクセス権限がない場合。
     * @throws DataStorageException 存在の確認に失敗した場合。
     */
    suspend fun existsImageFileInCache(fileName: FileName): Boolean

    /**
     * 指定された画像ファイルが永続ストレージに存在するか確認する。
     *
     * @param fileName 確認したいファイル名。
     * @return 指定された画像ファイルが存在すれば `true`、存在しなければ `false`。
     * @throws PermissionException ファイルへのアクセス権限がない場合。
     * @throws DataStorageException 存在の確認に失敗した場合。
     */
    suspend fun existsImageFileInPermanent(fileName: FileName): Boolean

    /**
     * 指定された画像ファイルがバックアップストレージに存在するか確認する。
     *
     * @param fileName 確認したいファイル名。
     * @return 指定された画像ファイルが存在すれば `true`、存在しなければ `false`。
     * @throws PermissionException ファイルへのアクセス権限がない場合。
     * @throws DataStorageException 存在の確認に失敗した場合。
     */
    suspend fun existsImageFileInBackup(fileName: FileName): Boolean

    /**
     * 指定されたUriの画像ファイルをキャッシュストレージへキャッシュする。
     *
     * @param uriString キャッシュしたい画像ファイルのUri。
     * @param fileBaseName キャッシュされるファイルのベース名。
     * @return キャッシュストレージへ保存されたファイル名。
     * @throws ResourceNotFoundException 指定されたURI/ファイルパスの画像が見つからない場合。
     * @throws PermissionException ファイルへのアクセス権限がない場合。
     * @throws DataStorageException ファイルの保存に失敗した場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    suspend fun cacheImageFile(uriString: String, fileBaseName: String): FileName

    /**
     * 指定された画像ファイルをキャッシュストレージから永続ストレージへ移動する。
     *
     * @param fileName 移動したいファイル名。
     * @throws DataStorageException ファイルの移動に失敗した場合。
     * @throws ResourceNotFoundException 移動元ファイルが見つからない場合。
     * @throws PermissionException ファイルへのアクセス権限がない場合。
     * @throws ResourceAlreadyExistsException 移動先に同名のファイルが既に存在する場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    suspend fun moveImageFileToPermanent(fileName: FileName)

    /**
     * 指定された画像ファイルを永続ストレージからバックアップストレージへ移動する。
     *
     * @param fileName 移動したいファイル名。
     * @throws DataStorageException ファイルの移動に失敗した場合。
     * @throws ResourceNotFoundException 移動元ファイルが見つからない場合。
     * @throws PermissionException ファイルへのアクセス権限がない場合。
     * @throws ResourceAlreadyExistsException 移動先に同名のファイルが既に存在する場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    suspend fun moveImageFileToBackup(fileName: FileName)

    /**
     * 指定された画像ファイルを永続ストレージからキャッシュストレージに復元する。
     *
     * @param fileName 復元したいファイル名。
     * @throws DataStorageException ファイルの復元に失敗した場合。
     * @throws InvalidParameterException 指定されたファイル名が無効の場合。
     * @throws ResourceNotFoundException 復元元ファイルが見つからない場合。
     * @throws PermissionException ファイルへのアクセス権限がない場合。
     * @throws ResourceAlreadyExistsException 復元先に同名のファイルが既に存在する場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    suspend fun restoreImageFileFromPermanent(fileName: FileName)

    /**
     * 指定された画像ファイルをバックアップストレージから永続ストレージに復元する。
     *
     * @param fileName 復元したいファイル名。
     * @throws DataStorageException ファイルの復元に失敗した場合。
     * @throws InvalidParameterException 指定されたファイル名が無効の場合。
     * @throws ResourceNotFoundException 復元元ファイルが見つからない場合。
     * @throws PermissionException ファイルへのアクセス権限がない場合。
     * @throws ResourceAlreadyExistsException 復元先に同名のファイルが既に存在する場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    suspend fun restoreImageFileFromBackup(fileName: FileName)

    /**
     * 指定された画像ファイルを永続ストレージから削除する。
     *
     * @param fileName 削除したいファイル名。
     * @throws DataStorageException ファイルの削除に失敗した場合。
     * @throws InvalidParameterException 指定されたファイル名が無効の場合。
     * @throws ResourceNotFoundException 指定されたファイルが見つからなかった場合。
     * @throws PermissionException 指定されたファイルへのアクセス権限がない場合。
     */
    suspend fun deleteImageFileInPermanent(fileName: FileName)

    /**
     * バックアップストレージから画像ファイルを削除する。
     *
     * @throws DataStorageException ファイルの削除に失敗した場合。
     * @throws ResourceNotFoundException 削除対象が見つからなかった場合。
     * @throws PermissionException バックアップストレージへのアクセス権限がない場合。
     */
    suspend fun clearAllImageFilesInCache()

    /**
     * バックアップストレージから画像ファイルを削除する。
     *
     * @throws DataStorageException ファイルの削除に失敗した場合。
     * @throws ResourceNotFoundException 削除対象が見つからなかった場合。
     * @throws PermissionException バックアップストレージへのアクセス権限がない場合。
     */
    suspend fun clearAllImageFilesInBackup()


    /**
     * 全ての画像ファイルを削除する。
     *
     * @throws DataStorageException ファイルの削除に失敗した場合。
     */
    suspend fun clearAllImageFiles()
}
