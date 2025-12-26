package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.model.diary.Condition
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.Weather
import com.websarva.wings.android.zuboradiary.domain.model.settings.IsFirstLaunchSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * アプリケーションの初回起動時に、サンプル日記データを準備するユースケース。
 *
 * このユースケースは一度だけ実行されることを想定している。
 * 実行が試みられた後は、成功・失敗にかかわらず次回以降は実行されない。
 * 内部でWelcomeメッセージ用の日記と、デバッグビルド時のみ追加のサンプルデータを保存する。
 *
 * @property diaryRepository 日記データおよび項目タイトル選択履歴へのアクセスを提供するリポジトリ。
 * @property settingsRepository ユーザー設定へのアクセスを提供するリポジトリ。
 */
internal class PrepareSampleDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository
) {

    private val logMsg = "サンプル日記データ準備_"

    /**
     * ユースケースを実行し、初回起動時にサンプル日記データを準備する。
     *
     * 初回起動でない場合は、何もせずに処理を終了する。
     * 初回起動の場合、サンプルデータの保存処理を試み、その成否にかかわらず次回以降は実行されないよう初回起動フラグを更新する。
     */
    suspend operator fun invoke() {
        Log.i(logTag, "${logMsg}開始")

        // サンプル日記データ保存
        var isFirstLaunch = false
        try {
            isFirstLaunch =
                settingsRepository
                    .loadIsFirstLaunchSetting().map{
                        it ?: IsFirstLaunchSetting.default()
                    }.first()
                    .isFirstLaunch
            if (!isFirstLaunch) return

            // 共通: ユーザー向けWelcome日記を入れる
            saveWelcomeDiary()

            // デバッグ限定: デバッグ用のサンプルデータを追加で入れる
            if (BuildConfig.DEBUG) {
                Log.i(logTag, "${logMsg}デバッグ用のサンプルデータ保存")
                saveSampleDiaries()
            }
        } catch (e: InsufficientStorageException) {
            Log.e(logTag, "${logMsg}失敗_ストレージ容量不足", e)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_保存エラー", e)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
        } finally {
            if (isFirstLaunch) {
                try {
                    settingsRepository.updateIsFirstLaunchSetting(
                        IsFirstLaunchSetting(false)
                    )
                    Log.i(logTag, "${logMsg}初回起動フラグを更新")
                } catch (e: Exception) {
                    Log.e(logTag, "${logMsg}初回起動フラグの更新失敗", e)
                }
                Log.i(logTag, "${logMsg}完了")
            } else {
                Log.i(logTag, "${logMsg}初回起動でない為、スキップ")
            }
        }
    }

    /**
     * Welcomeメッセージとして表示するための特別な日記データを1件保存する。
     *
     * この日記には、アプリの基本的な使い方や機能紹介が含まれる。
     *
     * @throws DataStorageException 日記の保存に失敗した場合。
     */
    private suspend fun saveWelcomeDiary() {
        val today = LocalDate.now()
        val nowTime = LocalDateTime.now()

        // Mapを作成して項目をセット
        val itemTitles = mapOf(
            1 to DiaryItemTitle("箇条書きによる記録"),
            2 to DiaryItemTitle("入力履歴の活用"),
            3 to DiaryItemTitle("閲覧・検索・操作性"),
            4 to DiaryItemTitle("安全なデータ管理"),
            5 to DiaryItemTitle("設定・カスタマイズ")
        )

        val itemComments = mapOf(
            1 to DiaryItemComment("通常の文章ではなく箇条書き形式を採用することにより、思考を整理しやすくしています。また、項目の追加・削除も直感的に行えます。"),
            2 to DiaryItemComment("項目タイトルは過去の履歴から選択可能です。「学習」や「食事」などのルーティンを、文字入力なしでスムーズに記録できます。"),
            3 to DiaryItemComment("キーワード検索やカレンダー連携で過去の日記をすぐに探せます。一覧のスワイプ操作による削除も可能です。"),
            4 to DiaryItemComment("日付の重複や上書き保存を検知する機能を搭載しています。誤操作による、意図しないデータの消失を防ぎます。"),
            5 to DiaryItemComment("気分に合わせたテーマカラーの変更や、記入済みの日には通知しないスマートなリマインダー設定が可能です。")
        )

        val welcomeDiary = Diary.create(
            id = DiaryId.generate(),
            date = today,
            log = nowTime,
            weather1 = Weather.UNKNOWN,
            weather2 = Weather.UNKNOWN,
            condition = Condition.UNKNOWN,
            title = DiaryTitle("ようこそ！ズボラ日記へ"),
            itemTitles = itemTitles,
            itemComments = itemComments,
            imageFileName = null
        )

        diaryRepository.saveDiary(welcomeDiary)
    }

    /**
     * デバッグビルド時に、アプリの動作確認やプレビューを目的としたサンプルの日記データを複数件保存する。
     *
     * 過去6ヶ月間にわたって、多様なパターンの日記データと項目タイトル履歴を生成し、保存する。
     *
     * @throws DataStorageException サンプル日記または履歴の保存に失敗した場合。
     */
    private suspend fun saveSampleDiaries() {
        // --- データソース定義 (5種類 x 各10パターン) ---

        // List 1: 【開発・学習】 (Item 1用)
        val list1Dev = listOf(
            "Android学習" to "Roomデータベースのマイグレーションについて公式ドキュメントを読んだ。",
            "個人開発" to "UIのレイアウト崩れを修正。ConstraintLayoutの制約を見直した。",
            "技術調査" to "新しいDIライブラリの比較検討を行った。Hiltがやはり使いやすそうだ。",
            "コードレビュー" to "過去に書いたコードのリファクタリング。可読性が向上した。",
            "アルゴリズム" to "LeetCodeの問題を1問解いた。計算量の削減に苦戦した。",
            "デザイン調整" to "マテリアルデザインのガイドラインを確認し、配色を調整した。",
            "テスト実装" to "ViewModelの単体テストを作成。カバレッジを80%まで上げた。",
            "リリース作業" to "Google Play Consoleでリリースの準備を行った。",
            "バグ修正" to "特定条件下でクラッシュする不具合を修正。Nullチェックを追加した。",
            "ミーティング" to "開発定例に参加。来週のスケジュールを確認した。"
        )

        // List 2: 【食事】 (Item 2用)
        val list2Food = listOf(
            "昼食" to "駅前のカフェで新作のパスタを食べた。少し辛かった。",
            "自炊" to "冷蔵庫の余り物で野菜炒めを作った。味付けは成功。",
            "夕食" to "友人と焼き肉へ行った。久しぶりにリフレッシュできた。",
            "間食" to "コンビニの新作スイーツを購入。糖分補給完了。",
            "朝食" to "少し早起きして、しっかり和食を作って食べた。",
            "ランチ" to "同僚と定食屋へ。唐揚げ定食がボリューム満点だった。",
            "テイクアウト" to "帰りにピザを買って帰った。たまにはジャンクフードも良い。",
            "飲み会" to "プロジェクトの打ち上げに参加。料理が美味しかった。",
            "カフェ" to "開発の合間にコーヒーブレイク。頭がスッキリした。",
            "夜食" to "小腹が空いたのでうどんを作って食べた。"
        )

        // List 3: 【健康・運動】 (Item 3用)
        val list3Health = listOf(
            "運動" to "近所を30分ほどランニング。いい汗をかいた。",
            "睡眠" to "昨日は早めに寝たので、目覚めが良い。",
            "散歩" to "天気が良かったので公園を散歩した。気分転換になった。",
            "ストレッチ" to "風呂上がりにストレッチを行った。体が軽くなった。",
            "筋トレ" to "ジムで軽くトレーニング。筋肉痛が心地よい。",
            "体調管理" to "少し頭痛がしたので薬を飲んで早めに休憩した。",
            "瞑想" to "寝る前に5分間のマインドフルネスを行った。",
            "歯科検診" to "歯医者で定期検診。特に問題なくて安心した。",
            "体重測定" to "最近少し食べすぎているので、明日から調整しよう。",
            "アイケア" to "目が疲れたのでホットアイマスクをして休んだ。"
        )

        // List 4: 【趣味・エンタメ】 (Item 4用)
        val list4Hobby = listOf(
            "読書" to "積読していたミステリー小説を読み始めた。止まらない。",
            "映画鑑賞" to "気になっていたSF映画を配信で観た。映像美に感動。",
            "ゲーム" to "息抜きにパズルゲームをした。ハイスコアを更新。",
            "音楽" to "好きなアーティストの新曲を聴いた。歌詞が深い。",
            "動画鑑賞" to "技術カンファレンスのアーカイブ動画を視聴した。",
            "ポッドキャスト" to "お気に入りのテック系ポッドキャストを聴いた。",
            "SNS" to "技術トレンドについて情報収集を行った。",
            "漫画" to "話題の漫画を大人買いして一気読みした。",
            "イラスト" to "タブレットで落書き。絵を描くのは楽しい。",
            "ドライブ" to "気分転換に少し遠くまでドライブした。"
        )

        // List 5: 【生活・家事・その他】 (Item 5用)
        val list5Life = listOf(
            "掃除" to "部屋の模様替えを行った。デスク周りがスッキリした。",
            "買い物" to "日用品の買い出しへ行った。洗剤などを補充。",
            "洗濯" to "溜まっていた洗濯物を片付けた。柔軟剤のいい香り。",
            "ニュース" to "気になっていたニュース記事を深掘りして読んだ。",
            "家計簿" to "今月の出費を整理した。少し節約が必要だ。",
            "連絡" to "実家に電話をして近況報告をした。",
            "荷物受取" to "ネットで注文していたガジェットが届いた。",
            "靴磨き" to "革靴の手入れをした。ピカピカになると気持ちいい。",
            "ゴミ出し" to "粗大ゴミの手続きをして処分した。部屋が広くなった。",
            "明日の準備" to "明日の予定を確認し、持ち物を準備した。"
        )

        // ------------------------------------------------

        // 選択履歴保存用リスト
        val historyList: MutableList<DiaryItemTitleSelectionHistory> = mutableListOf()

        // 1. 先月から6ヶ月前までループ
        for (monthOffset in 1..6) {
            // 2. 各月7件を作成
            for (i in 0 until 7) {

                val uniqueIndex = (monthOffset * 7) + i
                val itemCount = (uniqueIndex % 5) + 1

                val today = LocalDate.now()
                val targetDate =
                    today.minusMonths(
                        monthOffset.toLong()
                    ).withDayOfMonth((i * 4) + 1)

                // マップの構築
                val itemTitles = mutableMapOf<Int, DiaryItemTitle?>()
                val itemComments = mutableMapOf<Int, DiaryItemComment?>()

                // Item 1 (常に存在)
                itemTitles[1] =
                    DiaryItemTitle(list1Dev[uniqueIndex % list1Dev.size].first).also {
                        historyList.add(
                            createSampleDiaryItemTitleSelectionHistory(it)
                        )
                    }
                itemComments[1] = DiaryItemComment(list1Dev[uniqueIndex % list1Dev.size].second)

                // Item 2
                if (itemCount >= 2) {
                    itemTitles[2] =
                        DiaryItemTitle(list2Food[uniqueIndex % list2Food.size].first).also {
                            historyList.add(
                                createSampleDiaryItemTitleSelectionHistory(it)
                            )
                        }
                    itemComments[2] = DiaryItemComment(list2Food[uniqueIndex % list2Food.size].second)
                }
                // Item 3
                if (itemCount >= 3) {
                    itemTitles[3] =
                        DiaryItemTitle(list3Health[uniqueIndex % list3Health.size].first).also {
                            historyList.add(
                                createSampleDiaryItemTitleSelectionHistory(it)
                            )
                        }
                    itemComments[3] = DiaryItemComment(list3Health[uniqueIndex % list3Health.size].second)
                }
                // Item 4
                if (itemCount >= 4) {
                    itemTitles[4] =
                        DiaryItemTitle(list4Hobby[uniqueIndex % list4Hobby.size].first).also {
                            historyList.add(
                                createSampleDiaryItemTitleSelectionHistory(it)
                            )
                        }
                    itemComments[4] = DiaryItemComment(list4Hobby[uniqueIndex % list4Hobby.size].second)
                }
                // Item 5
                if (itemCount >= 5) {
                    itemTitles[5] =
                        DiaryItemTitle(list5Life[uniqueIndex % list5Life.size].first).also {
                            historyList.add(
                                createSampleDiaryItemTitleSelectionHistory(it)
                            )
                        }
                    itemComments[5] = DiaryItemComment(list5Life[uniqueIndex % list5Life.size].second)
                }

                val sampleDiary = Diary.create(
                    id = DiaryId.generate(),
                    date = targetDate,
                    log = LocalDateTime.now(),
                    weather1 = Weather.of((uniqueIndex % 4) + 1),
                    weather2 = Weather.of(((uniqueIndex + 2) % 4) + 1),
                    condition = Condition.of((uniqueIndex % 5) + 1),
                    title = DiaryTitle("${targetDate.monthValue}月${targetDate.dayOfMonth}日の記録"),
                    itemTitles = itemTitles,
                    itemComments = itemComments,
                    imageFileName = null
                )

                diaryRepository.saveDiary(sampleDiary)

            }
        }
        saveSampleDiaryItemTitleSelectionHistories(historyList)
    }

    /**
     * サンプル日記の項目タイトルから、選択履歴オブジェクトを生成する。
     *
     * @param title 履歴として保存する項目タイトル。
     * @return 現在時刻が付与された、新しい[DiaryItemTitleSelectionHistory]インスタンス。
     */
    private fun createSampleDiaryItemTitleSelectionHistory(
        title: DiaryItemTitle
    ): DiaryItemTitleSelectionHistory {
        return DiaryItemTitleSelectionHistory(
            DiaryItemTitleSelectionHistoryId.generate(),
            title,
            LocalDateTime.now()
        )
    }

    /**
     * 生成されたサンプル用の項目タイトル選択履歴リストを保存する。
     *
     * リスト内の重複を排除し、最新のログ時刻を持つユニークな履歴のみを更新する。
     *
     * @param selectionList 保存する選択履歴のリスト。
     * @throws InvalidParameterException 引数のリストが空の場合。
     * @throws DataStorageException 履歴の更新に失敗した場合。
     */
    private suspend fun saveSampleDiaryItemTitleSelectionHistories(
        selectionList: List<DiaryItemTitleSelectionHistory>
    ) {
        if (selectionList.isEmpty()) throw InvalidParameterException()

        val latestUniqueList =
            selectionList
                .sortedByDescending { it.log }
                .distinctBy { it.title }
        diaryRepository.updateDiaryItemTitleSelectionHistory(latestUniqueList)
    }
}
