package com.websarva.wings.android.zuboradiary.data.file

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.websarva.wings.android.zuboradiary.data.file.exception.DirectoryDeletionFailedException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileAlreadyExistsException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileDeleteException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileOperationException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileInsufficientStorageException
import com.websarva.wings.android.zuboradiary.data.file.exception.FilePermissionDeniedException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileReadException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileWriteException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileNotFoundException
import com.websarva.wings.android.zuboradiary.data.utils.isInsufficientStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import androidx.core.net.toUri
import com.websarva.wings.android.zuboradiary.data.file.exception.InvalidFileOperationParameterException
import com.websarva.wings.android.zuboradiary.utils.logTag

/**
 * 画像ファイルの永続化およびキャッシュ処理を担当するデータソース。
 *
 * URIから画像を取得し、指定された形式（JPEG）でキャッシュディレクトリに保存する。
 * また、キャッシュされたファイルを永続ディレクトリへ移動したり、永続ディレクトリからファイルを削除したりする機能を提供する。
 * ファイル操作に関するエラーは、定義されたカスタム例外 ([FileOperationException] のサブクラスなど) をスローする。
 *
 * @property contentResolver コンテンツURIへのアクセスに必要。
 * @property cacheDir アプリケーションのキャッシュファイル用ディレクトリ。
 * @property permanentDir アプリケーションの永続ファイル用ディレクトリ。
 * @property dispatcher ファイル操作を実行するスレッドプール。
 */
class ImageFileDataSource(
    private val contentResolver: ContentResolver,
    private val cacheDir: File,
    private val permanentDir: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val imageFileExtension = Bitmap.CompressFormat.JPEG

    private val imageDirName = "images"

    private val imageCacheDir: File by lazy {
        File(cacheDir, imageDirName)
            .also { it.mkdir() } // 指定ディレクトリ作成
    }

    private val imagePermanentDir: File by lazy {
        File(permanentDir, imageDirName)
            .also { it.mkdir() }
    }

    private val imageBackupDir: File by lazy {
        File(imageCacheDir, "backup")
            .also { it.mkdir() }
    }

    /**
     * 指定された画像ファイルから、キャッシュ画像ディレクトリ絶対パスを構築する。
     *
     * @param fileName 構築対象の画像ファイルの名前 (空でない、またはブランクのみでないこと)。
     * @return 構築された絶対パス。
     */
    fun buildImageFileAbsolutePathFromCache(fileName: ImageFileName): String {
        return File(imageCacheDir, fileName.fullName).absolutePath
    }

    /**
     * 指定された画像ファイルから、永続画像ディレクトリ絶対パスを構築する。
     *
     * @param fileName 構築対象の画像ファイルの名前 (空でない、またはブランクのみでないこと)。
     * @return 構築された絶対パス。
     */
    fun buildImageFileAbsolutePathFromPermanent(fileName: ImageFileName): String {
        return File(imagePermanentDir, fileName.fullName).absolutePath
    }

    /**
     * 指定された画像ファイルがキャッシュディレクトリに存在するか確認する。
     *
     * @param fileName 確認したいファイルの名前。
     * @throws FilePermissionDeniedException ファイルへのアクセス権限がない場合。
     * @throws FileReadException ファイルの存在確認に失敗した場合。
     * @return 構築された絶対パス。
     */
    suspend fun existsImageFileInCache(fileName: ImageFileName): Boolean {
        val file = File(imageCacheDir, fileName.fullName)
        return withContext(dispatcher) {
            try {
                file.exists()
            } catch (e: SecurityException) {
                throw FilePermissionDeniedException(file.absolutePath, e)
            }
        }
    }

    /**
     * 指定された画像ファイルが永続ディレクトリに存在するか確認する。
     *
     * @param fileName 確認したいファイルの名前。
     * @throws FilePermissionDeniedException ファイルへのアクセス権限がない場合。
     * @throws FileReadException ファイルの存在確認に失敗した場合。
     * @return 構築された絶対パス。
     */
    suspend fun existsImageFileInPermanent(fileName: ImageFileName): Boolean {
        val file = File(imagePermanentDir, fileName.fullName)
        return withContext(dispatcher) {
            try {
                file.exists()
            } catch (e: SecurityException) {
                throw FilePermissionDeniedException(file.absolutePath, e)
            }
        }
    }

    /**
     * 指定された画像ファイルがバックアップディレクトリに存在するか確認する。
     *
     * @param fileName 確認したいファイルの名前。
     * @throws FilePermissionDeniedException ファイルへのアクセス権限がない場合。
     * @throws FileReadException ファイルの存在確認に失敗した場合。
     * @return 構築された絶対パス。
     */
    suspend fun existsImageFileInBackup(fileName: ImageFileName): Boolean {
        val file = File(imageBackupDir, fileName.fullName)
        return withContext(dispatcher) {
            try {
                file.exists()
            } catch (e: SecurityException) {
                throw FilePermissionDeniedException(file.absolutePath, e)
            }
        }
    }

    /**
     * 指定されたURIの画像ファイルをリサイズ・圧縮し、キャッシュディレクトリにJPEG形式でキャッシュする。
     *
     * @param uriString 画像のURI文字列。
     * @param fileBaseName 保存するファイル名のベース部分 (空でなく、拡張子なしであること)。
     * @param width リサイズ後の目標幅。0の場合は元の幅を維持。
     * @param height リサイズ後の目標高さ。0の場合は元の高さを維持。
     * @param quality JPEG圧縮品質 (0-100)。
     * @return 保存されたファイル名 (拡張子付き)。
     * @throws InvalidFileOperationParameterException ファイルベース名が不正な場合。
     * @throws FileNotFoundException 指定されたURI/ファイルパスの画像が見つからない場合。
     * @throws FilePermissionDeniedException ファイルへのアクセス権限がない場合。
     * @throws FileReadException 画像の読み込みまたはデコードに失敗した場合。
     * @throws FileWriteException 画像の書き込みに失敗した場合。
     * @throws FileInsufficientStorageException ストレージの空き容量が不足した場合。
     */
    suspend fun cacheImageFile(
        uriString: String,
        fileBaseName: String,
        width: Int = 0,
        height: Int = 0,
        quality: Int = 100
    ): String {
        try {
            require(fileBaseName.isNotBlank()) {"ファイルベース名が空文字列"}
        } catch (e: IllegalArgumentException) {
            throw InvalidFileOperationParameterException(e)
        }

        return withContext(dispatcher) {
            val uri = uriString.toUri()
            val outputFileName = "$fileBaseName.${imageFileExtension.name}"
            val outputFile = File(imageCacheDir, outputFileName)
            var bitmap: Bitmap? = null

            // URI指定画像ファイル読み出し
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    bitmap = decodeSampledBitmapFromStream(inputStream, width, height)
                } ?: throw FileReadException(uri.path.toString())
            } catch (e: java.io.FileNotFoundException) {
                throw FileNotFoundException(uri.path.toString(), e)
            } catch (e: IOException) {
                throw FileReadException(uri.path.toString(), e)
            } catch (e: SecurityException) {
                throw FilePermissionDeniedException(uri.toString(), e)
            }

            val bitmapNotNull = bitmap ?: throw FileOperationException("bitmap変数がNullのままです。")

            // キャッシュディレクトリへ書き込み(保存)
            try {
                FileOutputStream(outputFile).use { outputStream ->
                    val isSuccess =
                        bitmapNotNull.compress(imageFileExtension, quality, outputStream)
                    if (!isSuccess) throw FileWriteException(outputFile.path)
                }
            } catch (e: java.io.FileNotFoundException) {
                throw FileNotFoundException(outputFile.absolutePath, e)
            } catch (e: IOException) {
                if (e.isInsufficientStorage()) {
                    throw FileInsufficientStorageException(outputFile.absolutePath, e)
                } else {
                    throw FileWriteException(outputFile.absolutePath, e)
                }
            } catch (e: SecurityException) {
                throw FilePermissionDeniedException(outputFile.absolutePath, e)
            } finally {
                bitmapNotNull.recycle()
            }

            outputFile.name
        }
    }

    /**
     * InputStreamから画像を効率的にデコードする。
     * 指定された幅と高さに基づいて画像をサンプリングし、メモリ使用量を削減する。
     *
     * @param inputStream 画像データの入力ストリーム。
     * @param reqWidth 要求の幅。
     * @param reqHeight 要求の高さ。
     * @return デコードされた [Bitmap]。
     * @throws IOException ストリームからの読み込みまたはデコードに失敗した場合。
     */
    private fun decodeSampledBitmapFromStream(
        inputStream: InputStream,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        if (!inputStream.markSupported()) {
            val tempBytes = inputStream.readBytes()
            val tempOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(tempBytes, 0, tempBytes.size, tempOptions)
            tempOptions.inSampleSize = calculateInSampleSize(tempOptions, reqWidth, reqHeight)
            tempOptions.inJustDecodeBounds = false
            return BitmapFactory.decodeByteArray(tempBytes, 0, tempBytes.size, tempOptions)
                ?: throw IOException("サンプリング後のバイト配列からのビットマップデコードに失敗しました。")
        }

        inputStream.mark(1024 * 1024)

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        inputStream.reset()
        return BitmapFactory.decodeStream(inputStream, null, options)
            ?: throw IOException("サンプリング後のストリームからのビットマップデコードに失敗しました。")
    }

    /**
     * [BitmapFactory.Options] と目標の幅・高さから、適切な [BitmapFactory.Options.inSampleSize] 値を計算する。
     *
     * このメソッドは、画像をメモリに読み込む際に、指定された目標サイズに近づけるために
     * どの程度画像をサンプリング（縮小）すべきかを決定する。
     * inSampleSize は2のべき乗である必要があるため、計算結果もそれに従う。
     * 要求の幅または高さが0以下の場合は、サンプリングを行わず元のサイズで読み込むため1を返す。
     *
     * @param options 画像の元のサイズ (outWidth, outHeight) を含む [BitmapFactory.Options] 。
     * @param reqWidth 要求の幅。
     * @param reqHeight 要求の高さ。
     * @return 計算された [BitmapFactory.Options.inSampleSize] 値。
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (reqHeight > 0 && reqWidth > 0 && (height > reqHeight || width > reqWidth)) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 指定されたファイル名の画像をキャッシュディレクトリから永続ディレクトリへ移動する。
     *
     * このメソッドは内部でキャッシュディレクトリと永続ディレクトリを指定して [moveImageFile] を呼び出している。
     * 処理内容、例外の詳細は [moveImageFile]を参照。
     *
     * @param fileName 移動する画像ファイル名 (拡張子付き)。
     */
    suspend fun moveImageFileToPermanent(fileName: ImageFileName) {
        withContext(dispatcher) {
            moveImageFile(fileName, imageCacheDir, imagePermanentDir)
        }
    }

    /**
     * 指定されたファイル名の画像を永続ディレクトリからキャッシュディレクトリへ戻す。
     *
     * このメソッドは内部で永続ディレクトリとキャッシュディレクトリを指定して [moveImageFile] を呼び出している。
     * 処理内容、例外の詳細は [moveImageFile]を参照。
     *
     * @param fileName 移動する画像ファイル名 (拡張子付き)。
     */
    suspend fun restoreImageFileFromPermanent(fileName: ImageFileName) {
        withContext(dispatcher) {
            moveImageFile(fileName, imagePermanentDir, imageCacheDir)
        }
    }

    /**
     * 指定されたファイル名の画像を永続ディレクトリからバックアップディレクトリへ移動する。
     *
     * このメソッドは内部で永続ディレクトリとバックアップディレクトリを指定して [moveImageFile] を呼び出している。
     * 処理内容、例外の詳細は [moveImageFile]を参照。
     *
     * @param fileName 移動する画像ファイル名 (拡張子付き)。
     */
    suspend fun moveImageFileToBackup(fileName: ImageFileName) {
        withContext(dispatcher) {
            moveImageFile(fileName, imagePermanentDir, imageBackupDir)
        }
    }

    /**
     * 指定されたファイル名の画像をバックアップディレクトリから永続ディレクトリへ戻す。
     *
     * このメソッドは内部でバックアップディレクトリと永続ディレクトリを指定して [moveImageFile] を呼び出している。
     * 処理内容、例外の詳細は [moveImageFile]を参照。
     *
     * @param fileName 移動する画像ファイル名 (拡張子付き)。
     */
    suspend fun restoreImageFileFromBackup(fileName: ImageFileName) {
        withContext(dispatcher) {
            moveImageFile(fileName, imageBackupDir, imagePermanentDir)
        }
    }

    /**
     * 指定されたファイル名の画像をキャッシュディレクトリから永続ディレクトリへ移動する。
     *
     * 移動先に同名のファイルが既に存在する場合は [FileAlreadyExistsException] をスローする。
     *
     * @param fileName 移動する画像ファイル名 (拡張子付き)。
     * @throws FileNotFoundException 移動元ファイルが見つからない場合。
     * @throws FilePermissionDeniedException ファイルへのアクセス権限がない場合。
     * @throws FileAlreadyExistsException 移動先に同名のファイルが既に存在する場合。
     * @throws FileWriteException 移動先への書き込みに失敗した場合。
     * @throws FileInsufficientStorageException ストレージの空き容量が不足した場合。
     * @throws FileDeleteException 移動元ファイルの削除に失敗した場合。
     */
    private fun moveImageFile(fileName: ImageFileName, sourceDir: File, destinationDir: File) {
        // 移動元ファイルクラス生成
        val sourceFile = File(sourceDir, fileName.fullName)
        try {
            if (!sourceFile.exists()) throw FileNotFoundException(sourceFile.absolutePath)
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(sourceFile.absolutePath, e)
        } catch (e: IOException) {
            throw FileReadException(sourceFile.absolutePath, e)
        }

        // 移動先ファイルクラス生成、同名ファイル存在確認
        val destinationFile = File(destinationDir, sourceFile.name)
        try {
            if (destinationFile.exists())
                throw FileAlreadyExistsException(destinationFile.absolutePath)
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(destinationFile.absolutePath, e)
        } catch (e: IOException) {
            throw FileReadException(destinationFile.absolutePath, e)
        }

        // 移動(コピー)
        try {
            sourceFile.inputStream().use { input ->

                try {
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } catch (e: java.io.FileNotFoundException) {
                    throw FileNotFoundException(destinationFile.absolutePath, e)
                } catch (e: SecurityException) {
                    throw FilePermissionDeniedException(destinationFile.absolutePath, e)
                } catch (e: IOException) {
                    // 不完全な移動先ファイルを削除する
                    try {
                        if (destinationFile.exists()) destinationFile.delete()
                    } catch (e: Exception) {
                        // 削除失敗のログは残しても良いが、元の例外を優先してスローする
                        Log.w(
                            logTag,
                            "移動先の不完全ファイルの削除に失敗 (パス: ${destinationFile.absolutePath})。",
                            e
                        )
                    }

                    if (e.isInsufficientStorage()) {
                        throw FileInsufficientStorageException(destinationFile.absolutePath, e)
                    } else {
                        throw FileWriteException(destinationFile.absolutePath, e)
                    }
                }

            }
        } catch (e: java.io.FileNotFoundException) {
            throw FileNotFoundException(sourceFile.absolutePath, e)
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(sourceFile.absolutePath, e)
        } catch (e: IOException) {
            throw FileReadException(sourceFile.absolutePath, e)
        }

        // 移動元ファイル削除
        val deleteFailureLogMessage = "元ファイルの削除に失敗 (パス: ${sourceFile.absolutePath})。"
        try {
            val isSuccess = sourceFile.delete()
            if (!isSuccess) Log.w(logTag, deleteFailureLogMessage)
        } catch (e: IOException) {
            Log.w(logTag, deleteFailureLogMessage, e)
        } catch (e: SecurityException) {
            Log.w(logTag, deleteFailureLogMessage, e)
        }
    }

    /**
     * 指定されたファイル名の画像をキャッシュディレクトリから削除する。

     * このメソッドは内部でキャッシュディレクトリを指定して [deleteImageFile] を呼び出している。
     * 処理内容、例外の詳細は [deleteImageFile]を参照。
     *
     * @param fileName 削除する画像ファイルの名前 (拡張子付き)。
     */
    @Suppress("unused") //MEMO:将来対応用
    suspend fun deleteImageFileInCache(fileName: ImageFileName) {
        withContext(dispatcher) {
            deleteImageFile(fileName, imageCacheDir)
        }
    }

    /**
     * 指定されたファイル名の画像を永続ディレクトリから削除する。

     * このメソッドは内部で永続ディレクトリを指定して [deleteImageFile] を呼び出している。
     * 処理内容、例外の詳細は [deleteImageFile]を参照。
     *
     * @param fileName 削除する画像ファイルの名前 (拡張子付き)。
     */
    suspend fun deleteImageFileInPermanent(fileName: ImageFileName) {
        withContext(dispatcher) {
            deleteImageFile(fileName, imagePermanentDir)
        }
    }

    /**
     * 指定されたファイル名の画像をバックアップディレクトリから削除する。

     * このメソッドは内部でバックアップディレクトリを指定して [deleteImageFile] を呼び出している。
     * 処理内容、例外の詳細は [deleteImageFile]を参照。
     *
     * @param fileName 削除する画像ファイルの名前 (拡張子付き)。
     */
    @Suppress("unused") //MEMO:将来対応用
    suspend fun deleteImageFileInBackup(fileName: ImageFileName) {
        withContext(dispatcher) {
            deleteImageFile(fileName, imageBackupDir)
        }
    }

    /**
     * 指定されたファイル名の画像ファイルを指定されたディレクトリから削除する。
     *
     * @param fileName 削除する画像ファイルの名前 (拡張子付き)。
     * @param targetDir 削除する画像ファイルが配置されているディレクトリ。
     * @throws FileNotFoundException 削除対象ファイルが見つからない場合。
     * @throws FilePermissionDeniedException ファイルへのアクセス権限がない場合。
     * @throws FileDeleteException ファイルの削除に失敗した場合。
     */
    private fun deleteImageFile(fileName: ImageFileName, targetDir: File) {
        val file = File(targetDir, fileName.fullName)
        try {
            if (file.exists()) {
                val isSuccess = file.delete()
                if (!isSuccess) throw FileDeleteException(file.absolutePath)
            } else {
                throw FileNotFoundException(file.absolutePath)
            }
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(file.absolutePath, e)
        } catch (e: IOException) {
            throw FileDeleteException(file.absolutePath, e)
        }
    }

    /**
     * キャッシュディレクトリ直下および全てのサブディレクトリ内の全てのファイルを削除する。
     *
     * キャッシュディレクトリのサブディレクトリであるバックアップディレクトリも対象。
     *
     * このメソッドは内部でキャッシュディレクトリを指定して [deleteAllFilesInDirectory] を呼び出している。
     * 処理内容、例外の詳細は [deleteAllFilesInDirectory]を参照。
     */
    suspend fun deleteAllFilesInCache() {
        withContext(dispatcher) {
            deleteAllFilesInDirectory(imageCacheDir)
        }
    }

    /**
     * 永続ディレクトリ直下および全てのサブディレクトリ内の全てのファイルを削除する。
     *
     * このメソッドは内部で永続ディレクトリを指定して [deleteAllFilesInDirectory] を呼び出している。
     * 処理内容、例外の詳細は [deleteAllFilesInDirectory]を参照。
     */
    private suspend fun deleteAllFilesInPermanent() {
        withContext(dispatcher) {
            deleteAllFilesInDirectory(imagePermanentDir)
        }
    }

    /**
     * バックアップディレクトリ直下および全てのサブディレクトリ内の全てのファイルを削除する。
     *
     * このメソッドは内部でバックアップ(永続)ディレクトリを指定して [deleteAllFilesInDirectory] を呼び出している。
     * 処理内容、例外の詳細は [deleteAllFilesInDirectory]を参照。
     */
    suspend fun deleteAllFilesInBackup() {
        withContext(dispatcher) {
            deleteAllFilesInDirectory(imageBackupDir)
        }
    }

    /**
     * 全てのファイルを削除する。
     *
     * このメソッドは内部で [deleteAllFilesInCache] 、 [deleteAllFilesInPermanent] を呼び出している。
     * 処理内容は [deleteAllFilesInDirectory]を参照。
     *
     * @throws DirectoryDeletionFailedException 一つ以上のファイルの削除に失敗した場合。
     */
    suspend fun deleteAllFiles() {
        withContext(dispatcher) {
            val allFailures = mutableListOf<Pair<String, Exception>>()
            try {
                deleteAllFilesInCache()
            } catch (e: FileNotFoundException) {
                allFailures.add(Pair(imageCacheDir.absolutePath, e))
            } catch (e: FilePermissionDeniedException) {
                allFailures.add(Pair(imageCacheDir.absolutePath, e))
            } catch (e: DirectoryDeletionFailedException) {
                allFailures.addAll(e.individualFailures)
            }

            try {
                deleteAllFilesInPermanent()
            } catch (e: FileNotFoundException) {
                allFailures.add(Pair(imagePermanentDir.absolutePath, e))
            } catch (e: FilePermissionDeniedException) {
                allFailures.add(Pair(imagePermanentDir.absolutePath, e))
            } catch (e: DirectoryDeletionFailedException) {
                allFailures.addAll(e.individualFailures)
            }

            if (allFailures.isNotEmpty())
                throw DirectoryDeletionFailedException(individualFailures = allFailures)
        }
    }

    /**
     * 指定されたディレクトリ直下および全てのサブディレクトリ内の全てのファイルを削除する。
     *
     * このメソッドは再帰的に動作し、指定されたディレクトリ (`directory`) 及びその全ての
     * サブディレクトリを探索し、見つかった全てのファイルを削除対象とする。
     * サブディレクトリの構造自体は保持され、サブディレクトリが空になるだけで、サブディレクトリ自体は削除されない。
     * 指定されたトップレベルの `directory` 自体も削除されない。
     *
     * 一つ以上のファイルの削除に失敗した場合、
     * このメソッドは処理を継続し、削除可能なファイルは全て削除しようと試みる。
     * 最終的に、一つでもファイルの削除に失敗すれば、
     * 全ての失敗情報を含む [DirectoryDeletionFailedException] をスローする。
     *
     * @param directory ファイル削除の起点となるディレクトリ。
     * @throws FileNotFoundException 指定されたディレクトリが存在しないか、ディレクトリではない場合。
     * @throws FilePermissionDeniedException 指定されたディレクトリへのアクセス権限がない場合。
     * @throws FileOperationException 指定されたディレクトリ内のファイルの操作に失敗した場合。
     * @throws DirectoryDeletionFailedException 一つ以上のファイルの削除に失敗した場合。
     */
    private fun deleteAllFilesInDirectory(directory: File) {
        // 指定ファイルクラスのディレクトリ確認
        try {
            if (!directory.isDirectory) {
                throw FileNotFoundException(directory.absolutePath)
            }
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(directory.absolutePath, e)
        }

        // ディレクトリ内のファイル(ディレクトリ)を配列化
        val files =
            try {
                directory.listFiles()
            } catch (e: SecurityException) {
                throw FilePermissionDeniedException(directory.absolutePath, e)
            } catch (e: IOException) {
                throw FileOperationException(
                    "ディレクトリ内のファイルのリスト化に失敗しました。 (パス: ${directory.absolutePath})",
                    e
                )
            }

        // 削除に関する例外を記録するためのリストを用意
        val failures = mutableListOf<Pair<String, Exception>>()

        files?.forEach { file ->
            try {
                if (file.isFile) {
                    if (!file.delete()) {
                        failures.add(
                            Pair(file.absolutePath, FileDeleteException(file.absolutePath))
                        )
                    }
                } else if (file.isDirectory) {
                    try {
                        deleteAllFilesInDirectory(file) // 再帰呼び出し
                    } catch (e: DirectoryDeletionFailedException) {
                        failures.addAll(e.individualFailures) // 再帰呼び出しで発生した例外を追加
                    }
                }
            } catch (e: SecurityException) {
                failures.add(
                    Pair(
                        file.absolutePath,
                        FilePermissionDeniedException(file.absolutePath, e)
                    )
                )
            } catch (e: IOException) {
                failures.add(
                    Pair(
                        file.absolutePath,
                        FileDeleteException(file.absolutePath, e)
                    )
                )
            } catch (e: FileOperationException) { // 再帰呼び出し用
                failures.add(Pair(file.absolutePath, e))
            }
        }

        // 削除に関する例外が一つでもあればスロー
        if (failures.isNotEmpty()) {
            throw DirectoryDeletionFailedException(directory.absolutePath, failures)
        }
    }
}
