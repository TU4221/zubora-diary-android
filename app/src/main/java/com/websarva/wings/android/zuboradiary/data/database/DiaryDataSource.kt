package com.websarva.wings.android.zuboradiary.data.database

import android.database.sqlite.SQLiteException
import android.util.Log
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.time.LocalDate

/**
 * データベース操作のデータソースクラス。
 *
 * このクラスは、[DiaryDatabase]、[DiaryDao]、[DiaryItemTitleSelectionHistoryDao] を使用して、
 * 日記データと日記項目タイトル選択履歴データへのアクセスを一元的に管理する。
 * データベース操作中に発生する可能性のある特定の例外を [DataBaseAccessFailureException] にラップする。
 *
 * @property diaryDatabase 日記データベースのインスタンス。
 * @property diaryDao 日記データアクセスオブジェクト。
 * @property diaryItemTitleSelectionHistoryDao 日記項目タイトル選択履歴データアクセスオブジェクト。
 */
internal class DiaryDataSource(
    private val diaryDatabase: DiaryDatabase,
    private val diaryDao: DiaryDao,
    private val diaryItemTitleSelectionHistoryDao: DiaryItemTitleSelectionHistoryDao
) {

    private val logTag = createLogTag()

    // TODO:メソッドをクラスの下の方に移動
    /**
     * suspend関数として定義されたデータベース操作を実行し、特定の例外をラップするヘルパーメソッド。
     *
     * [SQLiteException]、[IllegalStateException] が発生した場合、
     * それを [DataBaseAccessFailureException] でラップして再スローする。
     *
     * @param R 操作の結果の型。
     * @param operation 実行するsuspend関数形式のデータベース操作。
     * @return データベース操作の結果。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    private suspend fun <R> executeSuspendDbOperation(
        operation: suspend () -> R
    ): R {
        return try {
            operation()
        } catch (e: SQLiteException) {
            throw DataBaseAccessFailureException(e)
        } catch (e: IllegalStateException) {
            throw DataBaseAccessFailureException(e)
        }
    }

    // TODO:メソッドをクラスの下の方に移動
    /**
     * Flowストリーム内で発生する特定のデータベース関連例外をラップする拡張関数。
     *
     * [SQLiteException]、[IllegalStateException] が発生した場合、
     * それを [DataBaseAccessFailureException] でラップして再スローする。
     * その他の例外はそのまま再スローする。
     *
     * @param T Flowが放出する要素の型。
     * @return 例外処理が追加されたFlow。
     * @throws Throwable [SQLiteException]、[IllegalStateException]以外の例外。
     */
    private fun <T> Flow<T>.wrapDatabaseExceptions(): Flow<T> {
        return this.catch { exception -> // 'this' は拡張対象のFlowインスタンスを指す
            when (exception) {
                is SQLiteException,
                is IllegalStateException -> throw DataBaseAccessFailureException(exception)
                else -> throw exception
            }
        }
    }

    /**
     * データベースに保存されている日記の総数を取得する。
     *
     * @return 日記の総数。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun countDiaries(): Int {
        return executeSuspendDbOperation {
            diaryDao.countDiaries()
        }
    }

    /**
     * 指定された日付より前の日記の総数を取得する。
     *
     * @param date この日付以前の日記をカウントする (この日付を含む)。
     * @return 指定された日付より前の日記の総数。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    @Throws(DataBaseAccessFailureException::class)
    suspend fun countDiaries(date: LocalDate): Int {
        return executeSuspendDbOperation {
            diaryDao.countDiaries(date.toString())
        }
    }

    /**
     * 指定された日付の日記が存在するかどうかを確認する。
     *
     * @param date 確認する日記の日付。
     * @return 日記が存在すればtrue、しなければfalse。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    @Throws(DataBaseAccessFailureException::class)
    suspend fun existsDiary(date: LocalDate): Boolean {
        return executeSuspendDbOperation {
            diaryDao.existsDiary(date.toString())
        }
    }

    /**
     * 指定された画像URIを持つ日記が存在するかどうかを確認する。
     *
     * @param uriString 確認する画像のURI文字列。
     * @return 画像URIを持つ日記が存在すればtrue、しなければfalse。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun existsImageUri(uriString: String): Boolean {
        return executeSuspendDbOperation {
            diaryDao.existsImageUri(uriString)
        }
    }

    /**
     * 指定された日付の日記データを取得する。
     *
     * @param date 取得する日記の日付。
     * @return 指定された日付の日記データ。見つからない場合はnull。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun selectDiary(date: LocalDate): DiaryEntity? {
        return executeSuspendDbOperation {
            diaryDao.selectDiary(date.toString())
        }
    }

    /**
     * 最新の日記データを1件取得する。
     *
     * @return 最新の日記データ。日記が存在しない場合はnull。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun selectNewestDiary(): DiaryEntity? {
        return executeSuspendDbOperation {
            diaryDao.selectNewestDiary()
        }
    }

    /**
     * 最古の日記データを1件取得する。
     *
     * @return 最古の日記データ。日記が存在しない場合はnull。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun selectOldestDiary(): DiaryEntity? {
        return executeSuspendDbOperation {
            diaryDao.selectOldestDiary()
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
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
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

        return executeSuspendDbOperation {
            if (date == null) {
                diaryDao.selectDiaryListOrderByDateDesc(num, offset)
            } else {
                diaryDao.selectDiaryListOrderByDateDesc(num, offset, date.toString())
            }
        }
    }

    /**
     * 指定された単語がタイトルまたは各項目に含まれる日記の総数を取得する。
     *
     * @param searchWord 検索する単語。
     * @return 検索条件に一致する日記の総数。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun countWordSearchResults(searchWord: String): Int {
        return executeSuspendDbOperation {
            diaryDao.countWordSearchResults(searchWord)
        }
    }
    /**
     * 指定された単語がタイトルまたは各項目に含まれる日記の検索結果リストを指定された件数・オフセットで取得する。
     *
     * 結果は日付の降順でソートされる。
     *
     * @param num 取得する日記の件数 (1以上である必要がある)。
     * @param offset 取得を開始するオフセット位置 (0以上である必要がある)。
     * @param searchWord 検索する単語。
     * @return 単語検索結果リストアイテムデータのリスト。対象の日記が存在しない場合は空のリストを返す。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     * @throws IllegalArgumentException numまたはoffsetの引数が不正な場合。
     */
    suspend fun selectWordSearchResultListOrderByDateDesc(
        num: Int,
        offset: Int,
        searchWord: String
    ): List<WordSearchResultListItemData> {
        require(num >= 1)
        require(offset >= 0)


        return executeSuspendDbOperation {
            diaryDao.selectWordSearchResultListOrderByDateDesc(num, offset, searchWord)
        }
    }

    /**
     * 日記データと日記項目タイトル選択履歴データをデータベースに保存する。
     *
     * この操作はトランザクションとして実行される。
     *
     * @param diary 保存する日記データ。
     * @param historyItemList 保存する日記項目タイトル選択履歴データのリスト。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun saveDiary(
        diary: DiaryEntity,
        historyItemList: List<DiaryItemTitleSelectionHistoryEntity>
    ) {
        executeSuspendDbOperation {
            diaryDatabase.saveDiary(
                diary,
                historyItemList
            )
        }
    }

    /**
     * 指定された日付の日記を削除し、新しい日記データと日記項目タイトル選択履歴をデータベースに保存する。
     *
     * この操作はトランザクションとして実行される。
     *
     * @param deleteDiaryDate 削除する日記の日付。
     * @param newDiary 新しく保存する日記データ。
     * @param historyItemList 保存する日記項目タイトル選択履歴データのリスト。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun deleteAndSaveDiary(
        deleteDiaryDate: LocalDate,
        newDiary: DiaryEntity,
        historyItemList: List<DiaryItemTitleSelectionHistoryEntity>
    ) {
        executeSuspendDbOperation {
            diaryDatabase.deleteAndSaveDiary(
                deleteDiaryDate,
                newDiary,
                historyItemList
            )
        }
    }

    /**
     * 指定された日付の日記をデータベースから削除する。
     *
     * @param date 削除する日記の日付。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun deleteDiary(date: LocalDate) {
        executeSuspendDbOperation {
            diaryDao.deleteDiary(date.toString())
        }
    }

    /**
     * 全ての日記をデータベースから削除する。
     *
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun deleteAllDiaries() {
        executeSuspendDbOperation {
            diaryDao.deleteAllDiaries()
        }
    }

    /**
     * 全ての日記データと日記項目タイトル選択履歴データをデータベースから削除する。
     *
     * この操作はトランザクションとして実行される。
     *
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun deleteAllData() {
        executeSuspendDbOperation {
            diaryDatabase.deleteAllData()
        }
    }

    /**
     * 日記項目タイトル選択履歴を指定された件数・オフセットで、最終使用日時の降順で取得する。
     *
     * 結果はFlowとして監視可能であり、データベース関連の例外はラップされる。
     *
     * @param num 取得する履歴の件数 (1以上である必要がある)。
     * @param offset 取得を開始するオフセット位置 (0以上である必要がある)。
     * @return 日記項目タイトル選択履歴データのリストをFlowでラップしたもの。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合 (Flowのcatch内で発生)。
     * @throws IllegalArgumentException numまたはoffsetの引数が不正な場合。
     */
    fun selectHistoryListOrderByLogDesc(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryEntity>> {
        require(num >= 1)
        require(offset >= 0)

        return diaryItemTitleSelectionHistoryDao
            .selectHistoryListOrderByLogDesc(num, offset)
            .wrapDatabaseExceptions()
    }

    /**
     * 指定されたタイトルの履歴項目をデータベースから削除する。
     *
     * @param title 削除する履歴のタイトル。
     * @throws DataBaseAccessFailureException データベースアクセスに失敗した場合。
     */
    suspend fun deleteHistoryItem(title: String) {
        return executeSuspendDbOperation {
            diaryItemTitleSelectionHistoryDao.deleteHistory(title)
        }
    }
}
