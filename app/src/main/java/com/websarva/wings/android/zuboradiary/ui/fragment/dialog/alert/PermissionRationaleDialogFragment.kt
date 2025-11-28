package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.permission.RequestPermissionType
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.asString

/**
 * 特定の機能に必要な権限が許可されていない場合に、その理由を説明し、許可を促すための警告ダイアログ。
 */
class PermissionRationaleDialogFragment : BaseAlertDialogFragment() {

    private val requestPermissionType: RequestPermissionType by lazy {
        PermissionRationaleDialogFragmentArgs
            .fromBundle(requireArguments())
            .requestPermissionType
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_permission_title)
    }

    override fun createMessage(): String {
        val firstMessage = getString(R.string.dialog_permission_first_message)
        val secondMessage = requestPermissionType.asString(requireContext())
        val thirdMessage = getString(R.string.dialog_permission_third_message)
        return firstMessage + secondMessage + thirdMessage
    }

    override fun handleOnPositiveButtonClick() {
        setResult(RESULT_KEY, DialogResult.Positive(requestPermissionType))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    internal companion object {
        /** このダイアログから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + PermissionRationaleDialogFragment::class.java.name
    }
}
