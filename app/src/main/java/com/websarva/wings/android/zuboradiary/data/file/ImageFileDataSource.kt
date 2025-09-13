package com.websarva.wings.android.zuboradiary.data.file

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.system.ErrnoException
import android.system.OsConstants
import android.util.Log
import com.websarva.wings.android.zuboradiary.data.file.exception.FileDeleteException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileOperationException
import com.websarva.wings.android.zuboradiary.data.file.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.data.file.exception.InvalidFilePathException
import com.websarva.wings.android.zuboradiary.data.file.exception.FilePermissionDeniedException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileReadException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileWriteException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileNotFoundException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths


/**
 * 画像ファイルの永続化およびキャッシュ処理を担当するデータソース。
 *
 * URIから画像を取得し、指定された形式（JPEG）でキャッシュディレクトリに保存する。
 * また、キャッシュされたファイルを永続ディレクトリへ移動したり、永続ディレクトリからファイルを削除したりする機能を提供する。
 * ファイル操作に関するエラーは、定義されたカスタム例外 ([FileOperationException] のサブクラスなど) をスローする。
 *
 * @property contentResolver コンテンツURIへのアクセスに必要。
 * @property cacheDirPath アプリケーションのキャッシュファイル用ディレクトリパス。
 * @property permanentDirPath アプリケーションの永続ファイル用ディレクトリパス。
 */
internal class ImageFileDataSource(
    private val contentResolver: ContentResolver,
    private val cacheDirPath: String,
    private val permanentDirPath: String
) {

    private val logTag = createLogTag()

    private val imageFileExtension = Bitmap.CompressFormat.JPEG

    private val invalidFilePathExceptionReason =
        "画像ファイル拡張子 (${imageFileExtension.name}) ではありません。"

    private val imageDirName = "images"

    private val imageCacheDir: File by lazy {
        File(cacheDirPath, imageDirName)
            .also { it.mkdir() } // 指定ディレクトリ作成
    }

    private val imagePermanentDir: File by lazy {
        File(permanentDirPath, imageDirName)
            .also { it.mkdir() } // 指定ディレクトリ作成
    }

    /**
     * 指定されたファイルパス文字列からファイル名を抽出する。
     *
     * APIレベル26以上が必要な `java.nio.file.Paths` を使用するが、
     * API desugaring により下位互換性を確保している。
     *
     * @param filePath ファイルパス文字列。
     * @return 抽出されたファイル名。
     */
    @SuppressLint("NewApi")
    fun extractFileName(filePath: String): String {
        return Paths.get(filePath).fileName.toString()
    }

    /**
     * 画像ファイル名から、画像ディレクトリ基準の相対パスを構築する。
     *
     * @param fileName 画像ファイル名。
     * @return "images/fileName" 形式の相対パス。
     */
    fun buildImageRelativePath(fileName: String): String {
        return "$imageDirName${File.separator}$fileName"
    }

    /**
     * 指定されたURIの画像をリサイズ・圧縮し、キャッシュディレクトリにJPEG形式で保存する。
     *
     * @param uriString 画像のURI文字列。
     * @param fileBaseName 保存するファイル名のベース部分 (拡張子なし)。
     * @param width リサイズ後の目標幅。0の場合は元の幅を維持。
     * @param height リサイズ後の目標高さ。0の場合は元の高さを維持。
     * @param quality JPEG圧縮品質 (0-100)。
     * @return 保存されたファイル名 (拡張子付き)。
     * @throws FileNotFoundException 指定されたURI/ファイルパスの画像が見つからない場合。
     * @throws FilePermissionDeniedException ファイルアクセス権限がない場合。
     * @throws FileReadException 画像の読み込みまたはデコードに失敗した場合。
     * @throws FileWriteException 画像の書き込みに失敗した場合。
     * @throws InsufficientStorageException ストレージの空き容量が不足した場合。
     */
    fun saveImageFileToCache(
        uriString: String,
        fileBaseName: String,
        width: Int = 0,
        height: Int = 0,
        quality: Int = 100
    ): String {
        val uri = Uri.parse(uriString)
        val outputFileName = "$fileBaseName.${imageFileExtension.name}"
        val outputFile = File(imageCacheDir, outputFileName)
        var bitmap: Bitmap? = null

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                bitmap = decodeSampledBitmapFromStream(inputStream, width, height)
            } ?: throw FileReadException(uri.path.toString())
        } catch (e: java.io.FileNotFoundException) {
            throw FileNotFoundException(uri.path.toString() ,e)
        } catch (e: IOException) {
            throw FileReadException(uri.path.toString(), e)
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(uri.toString(), e)
        }

        val bitmapNotNull = bitmap ?: throw FileOperationException("bitmap変数がNullのままです。")

        try {
            FileOutputStream(outputFile).use { outputStream ->
                val isSuccess = bitmapNotNull.compress(imageFileExtension, quality, outputStream)
                if (!isSuccess) throw FileWriteException(outputFile.path)
            }
        } catch (e: java.io.FileNotFoundException) {
            throw FileNotFoundException(outputFile.absolutePath, e)
        } catch (e: IOException) {
            if (isInsufficientStorageException(e)) {
                throw InsufficientStorageException(outputFile.absolutePath, e)
            } else {
                throw FileWriteException(outputFile.absolutePath, e)
            }
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(outputFile.absolutePath, e)
        } finally {
            bitmapNotNull.recycle()
        }

        return outputFile.name
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
     * 移動先ファイルが既に存在する場合は上書きする。
     *
     * @param fileName 移動する画像ファイル名 (拡張子付き)。
     * @throws InvalidFilePathException 指定されたファイル名の拡張子が無効の場合。
     * @throws FileNotFoundException 移動元ファイルが見つからない場合。
     * @throws FilePermissionDeniedException ファイルアクセス権限がない場合。
     * @throws FileWriteException 移動先への書き込みに失敗した場合。
     * @throws InsufficientStorageException ストレージの空き容量が不足した場合。
     * @throws FileDeleteException 移動元ファイルの削除に失敗した場合。
     */
    fun moveImageFileToPermanent(fileName: String) {
        if (!isImageFileExtension(fileName))
            throw InvalidFilePathException(fileName, invalidFilePathExceptionReason)

        // 移動元ファイルクラス生成
        val sourceFile = File(imagePermanentDir, fileName)
        try {
            if (!sourceFile.exists()) throw FileNotFoundException(sourceFile.absolutePath)
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(sourceFile.absolutePath, e)
        }

        // 移動先ファイルクラス生成、移動(コピー)
        val destinationFile = File(imagePermanentDir, sourceFile.name)
        try {
            sourceFile.inputStream().use { input ->

                try {
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } catch (e: java.io.FileNotFoundException) {
                    throw FileNotFoundException(destinationFile.absolutePath, e)
                } catch (e: IOException) {
                    // 不完全な移動先ファイルを削除する
                    try {
                        if (destinationFile.exists()) destinationFile.delete()
                    } catch (e: Exception) {
                        // 削除失敗のログは残しても良いが、元の例外を優先してスローする
                        Log.w(
                            logTag,
                            "移動先(永続)不完全ファイルの削除に失敗 (パス: ${destinationFile.absolutePath})。",
                            e
                        )
                    }

                    if (isInsufficientStorageException(e)) {
                        throw InsufficientStorageException(destinationFile.absolutePath, e)
                    } else {
                        throw FileWriteException(destinationFile.absolutePath, e)
                    }
                } catch (e: SecurityException) {
                    throw FilePermissionDeniedException(destinationFile.absolutePath, e)
                }

            }
        } catch (e: java.io.FileNotFoundException) {
            throw FileNotFoundException(sourceFile.absolutePath, e)
        } catch (e: IOException) {
            throw FileReadException(sourceFile.absolutePath, e)
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(sourceFile.absolutePath, e)
        }

        // 移動元ファイル削除
        val deleteFailureLogMessage = "元ファイル(キャッシュ)の削除に失敗 (パス: ${sourceFile.absolutePath})。"
        try {
            val isSuccess = sourceFile.delete()
            if (!isSuccess) Log.w(logTag, deleteFailureLogMessage)
        } catch (e: IOException) {
            Log.w(logTag, deleteFailureLogMessage)
        } catch (e: SecurityException) {
            Log.w(logTag, deleteFailureLogMessage)
        }
    }

    /**
     * 指定されたファイル名の画像を永続ディレクトリから削除する。
     *
     *
     * @param fileName 削除する画像ファイル名 (拡張子付き)。
     * @throws InvalidFilePathException 指定されたファイル名の拡張子が無効の場合。
     * @throws FileNotFoundException 削除対象ファイルが見つからない場合。
     * @throws FileDeleteException ファイルの削除に失敗した場合。
     * @throws FilePermissionDeniedException ファイルアクセス権限がない場合。
     */
    fun deleteImageFileFromPermanent(fileName: String) {
        if (!isImageFileExtension(fileName))
            throw InvalidFilePathException(fileName, invalidFilePathExceptionReason)

        val file = File(imagePermanentDir, fileName)
        try {
            if (file.exists()) {
                val isSuccess = file.delete()
                if (!isSuccess) throw FileDeleteException(file.absolutePath)
            } else {
                throw FileNotFoundException(file.absolutePath)
            }
        } catch (e: IOException) {
            throw FileDeleteException(file.absolutePath, e)
        } catch (e: SecurityException) {
            throw FilePermissionDeniedException(file.absolutePath, e)
        }
    }


    // TODO:不要？
    /**
     * 指定されたファイルからキャッシュディレクトリのルートからの相対パスを構築する。
     *
     * APIレベル26以上が必要な `java.nio.file.Paths`、`java.nio.file.Path` 等 を使用するが、
     * API desugaring により下位互換性を確保している。
     *
     * @param targetFile 相対パスを構築する対象のファイル。
     * @return キャッシュルートからの相対パス。
     */
    @SuppressLint("NewApi")
    private fun buildRelativePathToCache(targetFile: File): Path {
        val baseCachePath = Paths.get(cacheDirPath) // キャッシュディレクトリのルートパス
        val outputFilePath = targetFile.toPath() // 保存したファイルのPathオブジェクト
        return baseCachePath.relativize(outputFilePath) // 例: "image/my_image.jpg"
    }

    /**
     * 指定されたファイル名に画像ファイル拡張子 ( [imageFileExtension] ) が含まれているかを確認する。
     * 大文字・小文字は区別しない。
     *
     * @param fileName 確認するファイル名。
     * @return 画像ファイル拡張子であれば true、そうでなければ false。
     */
    private fun isImageFileExtension(fileName: String): Boolean {
        val lowercasedFileName = fileName.lowercase()
        return lowercasedFileName.endsWith(".${imageFileExtension.name}")
    }

    /**
     * 指定された IOException がストレージ容量不足に起因するかどうかを判定する。
     *
     * @param exception チェック対象の IOException。
     * @return ストレージ容量不足が原因の場合は true、そうでない場合は false。
     */
    private fun isInsufficientStorageException(exception: IOException): Boolean {
        var currentCause: Throwable? = exception
        while (currentCause != null) {
            if (currentCause is ErrnoException && currentCause.errno == OsConstants.ENOSPC) {
                return true
            }
            currentCause = currentCause.cause
        }

        return exception.message?.contains("No space left on device", ignoreCase = true) == true
    }
}
