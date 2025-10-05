package com.websarva.wings.android.zuboradiary.data.database

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import android.util.Log
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseCorruptionException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseInitializationException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseStateException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseStorageFullException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordDeleteException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordNotFoundException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordReadException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordUpdateException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * データベース操作のデータソースクラス。
 *
 * このクラスは、[DiaryDatabase]、[DiaryDao]、[DiaryItemTitleSelectionHistoryDao] を使用して、
 * 日記データと日記項目タイトル選択履歴データへのアクセスを一元的に管理する。
 * データベース操作中に発生する可能性のある特定の例外を [DatabaseException] のサブクラスにラップする。
 *
 * @property diaryDatabase 日記データベースのインスタンス。
 * @property diaryDao 日記データアクセスオブジェクト。
 * @property diaryItemTitleSelectionHistoryDao 日記項目タイトル選択履歴データアクセスオブジェクト。
 * @property dispatcher データベース操作を実行するスレッドプール。
 */
internal class DiaryDataSource(
    private val diaryDatabase: DiaryDatabase,
    private val diaryDao: DiaryDao,
    private val diaryItemTitleSelectionHistoryDao: DiaryItemTitleSelectionHistoryDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val logTag = createLogTag()

    //region Diary
    /**
     * 指定された日付より前の日記の総数を取得する。
     *
     * 開始日が指定されていない場合は、全ての日記を対象とする。
     *
     * @param date この日付以前の日記をカウントする (この日付を含む)。nullの場合は全日記対象。
     * @return 指定された日付より前の日記の総数。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun countDiaries(date: LocalDate?): Int {
        return withContext(dispatcher) {
            executeSuspendDbReadOperation {
                if (date == null) {
                    diaryDao.countDiaries()
                } else {
                    diaryDao.countDiaries(date)
                }
            }
        }
    }

    /**
     * 指定された日付の日記が存在するかどうかを確認する。
     *
     * @param date 確認する日記の日付。
     * @return 日記が存在すればtrue、しなければfalse。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun existsDiary(date: LocalDate): Boolean {
        return withContext(dispatcher) {
            executeSuspendDbReadOperation {
                diaryDao.existsDiary(date)
            }
        }
    }

    /**
     * 指定された日付の日記データIDを取得する。
     *
     * @param date 取得する日記の日付。
     * @return 指定された日付の日記データ。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun selectDiaryId(date: LocalDate): List<String> {
        return withContext(dispatcher) {
            executeSuspendDbReadOperation {
                diaryDao.selectDiaryId(date)
            }
        }
    }

    /**
     * 指定されたIDの日記データを取得する。
     *
     * @param id 取得する日記のID。
     * @return 指定された日付の日記データ。
     * @throws RecordNotFoundException 指定された日付の日記データが見つからなかった場合。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun selectDiary(id: String): DiaryEntity {
        return withContext(dispatcher) {
            executeSuspendDbReadOperation {
                diaryDao.selectDiary(id)
            } ?: throw RecordNotFoundException()
        }
    }

    /**
     * 最新の日記データを1件取得する。
     *
     * @return 最新の日記データ。
     * @throws RecordNotFoundException 日記データが見つからなかった場合。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun selectNewestDiary(): DiaryEntity {
        return withContext(dispatcher) {
            executeSuspendDbReadOperation {
                diaryDao.selectNewestDiary()
            } ?: throw RecordNotFoundException()
        }
    }

    /**
     * 最古の日記データを1件取得する。
     *
     * @return 最古の日記データ。
     * @throws RecordNotFoundException 日記データが見つからなかった場合。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun selectOldestDiary(): DiaryEntity {
        return withContext(dispatcher) {
            executeSuspendDbReadOperation {
                diaryDao.selectOldestDiary()
            } ?: throw RecordNotFoundException()
        }
    }

    /**
     * 日記リストのデータを日付の降順で指定された件数・オフセットで取得する。
     *
     * 開始日が指定されていない場合は、全ての日記を対象とする。
     *
     * @param num 取得する日記の件数 (1以上である必要がある)。
     * @param offset 取得を開始するオフセット位置 (0以上である必要がある)。
     * @param date この日付以前の日記を取得する (この日付を含む)。nullの場合は全日記対象。
     * @return 日記リストアイテムデータのリスト。対象の日記が存在しない場合は空のリストを返す。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     * @throws IllegalArgumentException numまたはoffsetの引数が不正な場合。
     */
    suspend fun selectDiaryListOrderByDateDesc(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryListItemData> {
        Log.d(logTag, "selectDiaryList(num = $num, offset = $offset, date = $date)")
        require(num >= 1)
        require(offset >= 0)

        return withContext(dispatcher) {
            executeSuspendDbReadOperation {
                if (date == null) {
                    diaryDao.selectDiaryListOrderByDateDesc(num, offset)
                } else {
                    diaryDao.selectDiaryListOrderByDateDesc(num, offset, date)
                }
            }
        }
    }

    /**
     * 日記データと日記項目タイトル選択履歴データをデータベースに保存する。
     *
     * この操作はトランザクションとして実行される。
     *
     * @param diary 保存する日記データ。
     * @throws RecordUpdateException データベースからのレコードの更新に失敗した場合。
     * @throws DatabaseStorageFullException ストレージ容量が不足している場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun saveDiary(diary: DiaryEntity) {
        withContext(dispatcher) {
            executeSuspendDbUpdateOperation {
                diaryDao.insertDiary(diary)
            }
        }
    }

    /**
     * 保存する日記データと同じ日付の日記データを削除し、保存する日記データを保存する。
     *
     * この操作はトランザクションとして実行される。
     *
     * @param diary 新しく保存する日記データ。
     * @throws RecordUpdateException データベースからのレコードの更新に失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun deleteAndSaveDiary(diary: DiaryEntity) {
        withContext(dispatcher) {
            executeSuspendDbUpdateOperation {
                diaryDao.deleteAndSaveDiary(diary)
            }
        }
    }

    /**
     * 指定された日付の日記をデータベースから削除する。
     *
     * @param date 削除する日記の日付。
     * @throws RecordDeleteException データベースからのレコードの書き込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun deleteDiary(date: LocalDate) {
        withContext(dispatcher) {
            executeSuspendDbDeleteOperation {
                diaryDao.deleteDiary(date)
            }
        }
    }

    /**
     * 全ての日記をデータベースから削除する。
     *
     * @throws RecordDeleteException データベースからのレコードの書き込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun deleteAllDiaries() {
        withContext(dispatcher) {
            executeSuspendDbDeleteOperation {
                diaryDao.deleteAllDiaries()
            }
        }
    }
    //endregion

    //region WordSearchResult
    /**
     * 指定された単語がタイトルまたは各項目に含まれる日記の総数を取得する。
     *
     * @param searchWord 検索する単語 (空でない、またはブランクのみでないこと)。
     * @return 検索条件に一致する日記の総数。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     * @throws IllegalArgumentException 引数が不正な場合。
     */
    suspend fun countWordSearchResults(searchWord: String): Int {
        require(searchWord.isNotBlank()) { "検索する単語が空、またはブランクのみ" }

        return withContext(dispatcher) {
            executeSuspendDbReadOperation {
                diaryDao.countWordSearchResults(searchWord)
            }
        }
    }

    /**
     * 指定された単語がタイトルまたは各項目に含まれる日記の検索結果リストを指定された件数・オフセットで取得する。
     *
     * 結果は日付の降順でソートされる。
     *
     * @param num 取得する日記の件数 (1以上である必要がある)。
     * @param offset 取得を開始するオフセット位置 (0以上である必要がある)。
     * @param searchWord 検索する単語 (空でない、またはブランクのみでないこと)。
     * @return 単語検索結果リストアイテムデータのリスト。対象の日記が存在しない場合は空のリストを返す。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     * @throws IllegalArgumentException 引数が不正な場合。
     */
    suspend fun selectWordSearchResultListOrderByDateDesc(
        num: Int,
        offset: Int,
        searchWord: String
    ): List<WordSearchResultListItemData> {
        require(num >= 1) { "取得件数 `$num` が不正値" }
        require(offset >= 0) { "取得開始位置 `$num` が不正値" }
        require(searchWord.isNotBlank()) { "検索する単語が空、またはブランクのみ" }

        return withContext(dispatcher) {
            executeSuspendDbReadOperation {
                diaryDao.selectWordSearchResultListOrderByDateDesc(num, offset, searchWord)
            }
        }
    }
    //endregion

    //region DiaryItemTitleSelectionHistory
    /**
     * 日記項目タイトル選択履歴を指定された件数・オフセットで、最終使用日時の降順で取得する。
     *
     * 結果はFlowとして監視可能であり、データベース関連の例外はラップされる。
     *
     * @param num 取得する履歴の件数 (1以上である必要がある)。
     * @param offset 取得を開始するオフセット位置 (0以上である必要がある)。
     * @return 日記項目タイトル選択履歴データのリストをFlowでラップしたもの。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。([Flow] 内部で発生)
     * @throws DatabaseCorruptionException データベースが破損している場合。([Flow] 内部で発生)
     * @throws DatabaseStateException データベースの状態が不正だった場合。([Flow] 内部で発生)
     * @throws IllegalArgumentException 引数が不正な場合。
     */
    fun selectHistoryListOrderByLogDesc(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryEntity>> {
        require(num >= 1) { "取得件数 `$num` が不正値" }
        require(offset >= 0) { "取得開始位置 `$num` が不正値" }

        return diaryItemTitleSelectionHistoryDao
            .selectHistoryListOrderByLogDesc(num, offset)
            .wrapDatabaseExceptions()
    }

    /**
     * 日記項目タイトル選択履歴データをデータベースに保存する。
     *
     * 日記項目タイトル選択履歴データを保存後、最新の50件を除く最終使用日時が古い順の履歴を削除する。
     *
     * この操作はトランザクションとして実行される。
     *
     * @param historyItemList 更新する日記項目タイトル選択履歴データのリスト。
     * @throws RecordUpdateException データベースからのレコードの更新に失敗した場合。
     * @throws DatabaseStorageFullException ストレージ容量が不足している場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun updateDiaryItemTitleSelectionHistory(
        historyItemList: List<DiaryItemTitleSelectionHistoryEntity>
    ) {
        withContext(dispatcher) {
            executeSuspendDbUpdateOperation {
                diaryItemTitleSelectionHistoryDao
                    .updateDiaryItemTitleSelectionHistory(
                        historyItemList
                    )
            }
        }
    }

    /**
     * 指定されたタイトルの履歴項目をデータベースから削除する。
     *
     * @param title 削除する履歴のタイトル。
     * @throws RecordDeleteException データベースからのレコードの書き込みに失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun deleteHistoryItem(title: String) {
        withContext(dispatcher) {
            executeSuspendDbDeleteOperation {
                diaryItemTitleSelectionHistoryDao.deleteHistory(title)
            }
        }
    }
    //endregion

    //region Options
    /**
     * データベースの全てのデータ(日記データ、日記項目タイトル選択履歴のテーブル) を初期化する。
     *
     * @throws DatabaseInitializationException データベースの初期化に失敗した場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    suspend fun initializeAllData() {
        withContext(dispatcher) {
            executeSuspendDbInitializationOperation {
                diaryDatabase.clearAllTables()
            }
        }
    }
    //endregion

    /**
     * suspend関数として定義されたデータベース読み込み操作を実行し、特定の例外をラップするヘルパーメソッド。
     *
     * このメソッドは、データベース操作中に発生する可能性のある特定の例外を以下のように処理します。
     * - [SQLiteFullException] は、 [DatabaseStorageFullException] にラップされます。
     * - [SQLiteDatabaseCorruptException] は、 [DatabaseCorruptionException] にラップされます。
     * - 上記以外の [SQLiteException] は、[RecordReadException] にラップされます。
     * - [IllegalStateException] は [DatabaseStateException] にラップされます。
     * - その他の種類の例外は、キャッチされずにそのまま再スローされます。
     *
     * @param R 操作の結果の型。
     * @param operation 実行するsuspend関数形式のデータベース操作。
     * @return データベース操作の結果。
     * @throws DatabaseStorageFullException ストレージ容量が不足している場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    private suspend fun <R> executeSuspendDbReadOperation(
        operation: suspend () -> R
    ): R {
        return executeSuspendDbOperation(operation) { e -> RecordReadException(e) }
    }

    /**
     * suspend関数として定義されたデータベース更新操作を実行し、特定の例外をラップするヘルパーメソッド。
     *
     * このメソッドは、データベース操作中に発生する可能性のある特定の例外を以下のように処理します。
     * - [SQLiteFullException] は、 [DatabaseStorageFullException] にラップされます。
     * - [SQLiteDatabaseCorruptException] は、 [DatabaseCorruptionException] にラップされます。
     * - 上記以外の [SQLiteException] は、[RecordUpdateException] にラップされます。
     * - [IllegalStateException] は [DatabaseStateException] にラップされます。
     * - その他の種類の例外は、キャッチされずにそのまま再スローされます。
     *
     * @param R 操作の結果の型。
     * @param operation 実行するsuspend関数形式のデータベース操作。
     * @return データベース操作の結果。
     * @throws DatabaseStorageFullException ストレージ容量が不足している場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws RecordUpdateException データベースからのレコードの更新に失敗した場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    private suspend fun <R> executeSuspendDbUpdateOperation(
        operation: suspend () -> R
    ): R {
        return executeSuspendDbOperation(operation) { e -> RecordUpdateException(e) }
    }

    /**
     * suspend関数として定義されたデータベース削除操作を実行し、特定の例外をラップするヘルパーメソッド。
     *
     * このメソッドは、データベース操作中に発生する可能性のある特定の例外を以下のように処理します。
     * - [SQLiteFullException] は、 [DatabaseStorageFullException] にラップされます。
     * - [SQLiteDatabaseCorruptException] は、 [DatabaseCorruptionException] にラップされます。
     * - 上記以外の [SQLiteException] は、[RecordDeleteException] にラップされます。
     * - [IllegalStateException] は [DatabaseStateException] にラップされます。
     * - その他の種類の例外は、キャッチされずにそのまま再スローされます。
     *
     * @param R 操作の結果の型。
     * @param operation 実行するsuspend関数形式のデータベース操作。
     * @return データベース操作の結果。
     * @throws DatabaseStorageFullException ストレージ容量が不足している場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws RecordDeleteException データベースからのレコードの書き込みに失敗した場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    private suspend fun <R> executeSuspendDbDeleteOperation(
        operation: suspend () -> R
    ): R {
        return executeSuspendDbOperation(operation) { e -> RecordDeleteException(e) }
    }

    /**
     * suspend関数として定義されたデータベース、またはテーブルの初期化操作を実行し、特定の例外をラップするヘルパーメソッド。
     *
     * このメソッドは、データベース操作中に発生する可能性のある特定の例外を以下のように処理します。
     * - [SQLiteFullException] は、 [DatabaseStorageFullException] にラップされます。
     * - [SQLiteDatabaseCorruptException] は、 [DatabaseCorruptionException] にラップされます。
     * - 上記以外の [SQLiteException] は、[DatabaseInitializationException] にラップされます。
     * - [IllegalStateException] は [DatabaseStateException] にラップされます。
     * - その他の種類の例外は、キャッチされずにそのまま再スローされます。
     *
     * @param R 操作の結果の型。
     * @param operation 実行するsuspend関数形式のデータベース操作。
     * @return データベース操作の結果。
     * @throws DatabaseStorageFullException ストレージ容量が不足している場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws DatabaseInitializationException データベース、又はテーブルの初期化に失敗した場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    private suspend fun <R> executeSuspendDbInitializationOperation(
        operation: suspend () -> R
    ): R {
        return executeSuspendDbOperation(operation) { e -> DatabaseInitializationException(e) }
    }

    /**
     * suspend関数として定義されたデータベース操作を実行し、特定の例外をラップする共通ヘルパーメソッド。
     *
     * このメソッドは、データベース操作中に発生する可能性のある特定の例外を以下のように処理します。
     * - [SQLiteFullException] は、他のマッピングより優先して [DatabaseStorageFullException] にラップされます。
     * - [SQLiteDatabaseCorruptException] は、他のマッピングより優先して [DatabaseCorruptionException] にラップされます。
     * - 上記以外の [SQLiteException] は、引数 `[sqliteExceptionMapper]` を用いて
     *   指定された [DatabaseException] のサブタイプにラップされます。
     * - [IllegalStateException] は [DatabaseStateException] にラップされます。
     * - その他の種類の例外は、キャッチされずにそのまま再スローされます。
     *
     * @param R 操作の結果の型。
     * @param E [SQLiteException] から変換される例外の型。これは [DatabaseException] のサブタイプである必要がある。
     * @param operation 実行するsuspend関数形式のデータベース操作。
     * @param sqliteExceptionMapper [SQLiteException] を特定の [DatabaseException] サブタイプに変換する関数。
     *   ただし、[SQLiteFullException] と [SQLiteDatabaseCorruptException] はこのマッパーの対象外となる。
     * @return データベース操作の結果。
     * @throws DatabaseStorageFullException ストレージ容量が不足している場合。
     * @throws DatabaseCorruptionException データベースが破損している場合。
     * @throws E [SQLiteException] が発生し、[sqliteExceptionMapper] によって変換された場合。
     * @throws DatabaseStateException データベースの状態が不正だった場合。
     */
    private suspend fun <R, E : DatabaseException> executeSuspendDbOperation(
        operation: suspend () -> R,
        sqliteExceptionMapper: (SQLiteException) -> E
    ): R {
        return try {
            operation()
        } catch (e: SQLiteFullException) {
            throw DatabaseStorageFullException(e)
        } catch (e: SQLiteDatabaseCorruptException) {
            throw DatabaseCorruptionException(e)
        } catch (e: SQLiteException) {
            throw sqliteExceptionMapper(e)
        } catch (e: IllegalStateException) {
            throw DatabaseStateException(e)
        }
    }

    /**
     * Flowストリーム内で発生する特定のデータベース関連例外をラップする拡張関数。
     *
     * [SQLiteFullException] が発生した場合は [DatabaseStorageFullException]、
     * [SQLiteDatabaseCorruptException] が発生した場合は [DatabaseCorruptionException]、
     * その他の [SQLiteException] が発生した場合は [RecordReadException] (Flowは主に読み込みに使われるため)、
     * [IllegalStateException] が発生した場合は [DatabaseStateException]でラップして再スローする。
     * その他の例外はそのまま再スローする。
     *
     * @param T Flowが放出する要素の型。
     * @return 例外処理が追加されたFlow。
     * @throws RecordReadException データベースからのレコードの読み込みに失敗した場合。([Flow] 内部で発生)
     * @throws DatabaseStateException データベースの状態が不正だった場合。([Flow] 内部で発生)
     * @throws DatabaseStorageFullException ストレージ容量が不足している場合。([Flow] 内部で発生)
     * @throws DatabaseCorruptionException データベースが破損している場合。([Flow] 内部で発生)
     */
    private fun <T> Flow<T>.wrapDatabaseExceptions(): Flow<T> {
        return this.catch { exception -> // 'this' は拡張対象のFlowインスタンスを指す
            throw  when (exception) {
                is SQLiteFullException -> DatabaseStorageFullException(exception)
                is SQLiteDatabaseCorruptException -> DatabaseCorruptionException(exception)
                is SQLiteException -> RecordReadException(exception)
                is IllegalStateException -> DatabaseStateException(exception)
                else -> exception
            }
        }
    }
}
