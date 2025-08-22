package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryListViewModel.Companion.NUM_LOAD_ITEMS
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate
import kotlin.jvm.Throws

internal class RefreshDiaryListUseCase(
    private val loadDiaryListUseCase: LoadDiaryListUseCase,
    private val updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        currentList: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): DefaultUseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>> {
        val logMsg = "日記リスト再読込_"
        Log.i(logTag, "${logMsg}開始")

        var numLoadItems = currentList.countDiaries()
        // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
        //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
        //      対策として下記コードを記述。
        if (numLoadItems < NUM_LOAD_ITEMS) {
            numLoadItems = NUM_LOAD_ITEMS
        }
        try {
            val loadedDiaryList =
                loadDiaryList(
                    numLoadItems,
                    0,
                    startDate
                )
            val resultList = updateDiaryListFooter(loadedDiaryList, startDate)

            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(resultList)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }

    @Throws(DomainException::class)
    private suspend fun loadDiaryList(
        numLoadItems: Int,
        loadOffset: Int,
        startDate: LocalDate?
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        val result =
            loadDiaryListUseCase(
                numLoadItems,
                loadOffset,
                startDate
            )
        return when (result) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    @Throws(DomainException::class)
    private suspend fun updateDiaryListFooter(
        list: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        return when (val result = updateDiaryListFooterUseCase(list, startDate)) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }
}
