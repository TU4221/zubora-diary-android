package com.websarva.wings.android.zuboradiary.ui;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public abstract class BaseDatePickerDialogFragment extends DialogFragment{

    protected SettingsViewModel settingsViewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        settingsViewModel = createSettingsViewModel();

        // MEMO:MaterialDatePickerはDialogクラスを作成できないのでダミーDialogを作成して戻り値として返し
        //      MaterialDatePicker#show()でDatePickerDialogを表示する。ダミーDialogも重なって表示されるので、
        //      MaterialDatePickerに追加したリスナーでダミーDialogを閉じる(Dialog#dismiss())。
        Dialog dummyDialog = new Dialog(requireContext());

        MaterialDatePicker<Long> datePicker = createDatePickerDialog(dummyDialog);
        datePicker.show(getChildFragmentManager(),"");

        return dummyDialog;
    }

    @NonNull
    private SettingsViewModel createSettingsViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        SettingsViewModel settingsViewModel = provider.get(SettingsViewModel.class);
        return Objects.requireNonNull(settingsViewModel);
    }

    @NonNull
    protected final ThemeColor requireThemeColor() {
        return settingsViewModel.loadThemeColorSettingValue();
    }

    @NonNull
    private MaterialDatePicker<Long> createDatePickerDialog(Dialog dummyDialog) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();

        int themeResId = requireThemeColor().getDatePickerDialogThemeResId();
        builder.setTheme(themeResId);

        LocalDate initialDate = createInitialDate();
        Objects.requireNonNull(initialDate);
        long initialEpochMilli =
                initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        builder.setSelection(initialEpochMilli);

        MaterialDatePicker<Long> datePicker = builder.build();

        setUpOnClickListener(datePicker, dummyDialog);

        return datePicker;
    }

    protected abstract LocalDate createInitialDate();

    private void setUpOnClickListener(MaterialDatePicker<Long> datePicker, Dialog dummyDialog) {
        Objects.requireNonNull(datePicker);
        Objects.requireNonNull(dummyDialog);

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Objects.requireNonNull(selection);

            // 選択日付型変換(EpochMilli -> LocalDate)
            Instant instant = Instant.ofEpochMilli(selection);
            LocalDate selectedDate = LocalDate.ofInstant(instant, ZoneId.systemDefault());
            Objects.requireNonNull(selectedDate);
            handleOnPositiveButtonClick(selectedDate);
            dummyDialog.dismiss();
        });

        datePicker.addOnNegativeButtonClickListener(v -> {
            Objects.requireNonNull(v);

            handleOnNegativeButtonClick(v);
            dummyDialog.dismiss();
        });

        datePicker.addOnCancelListener(dialog -> {
            Objects.requireNonNull(dialog);

            handleOnCancel(dialog);
            dummyDialog.dismiss();
        });

        datePicker.addOnDismissListener(dialog -> {
            Objects.requireNonNull(dialog);

            handleOnDismiss(dialog);
            dummyDialog.dismiss();
        });
    }

    protected abstract void handleOnPositiveButtonClick(@NonNull LocalDate selectedDate);

    protected abstract void handleOnNegativeButtonClick(@NonNull View v);

    protected abstract void handleOnCancel(@NonNull DialogInterface dialog);

    protected abstract void handleOnDismiss(@NonNull DialogInterface dialog);
}
