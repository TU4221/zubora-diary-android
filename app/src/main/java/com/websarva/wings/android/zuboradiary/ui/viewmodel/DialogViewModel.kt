package com.websarva.wings.android.zuboradiary.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class DialogViewModel @Inject constructor(
    handle: SavedStateHandle,
    loadThemeColorSettingUseCase: LoadThemeColorSettingUseCase
) : ViewModel() {

    companion object {
        private const val SAVED_THEME_COLOR_KEY = "themeColor"
    }

    val themeColor: StateFlow<ThemeColorUi?> =
        loadThemeColorSettingUseCase()
            .map {
                when (it) {
                    is UseCaseResult.Success -> it.value.themeColor.toUiModel()
                    is UseCaseResult.Failure -> {
                        it.exception.fallbackSetting.themeColor.toUiModel()
                    }
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                handle[SAVED_THEME_COLOR_KEY]
            )
}
