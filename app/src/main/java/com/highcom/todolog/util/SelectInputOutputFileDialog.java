package com.highcom.todolog.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.highcom.todolog.R;

public class SelectInputOutputFileDialog {
    private Context context;
    private InputOutputFileDialogListener inputOutputFileDialogListener;
    private int checkedItem = -1;
    private String[] items;

    public interface InputOutputFileDialogListener {
        void onSelectOperationClicked(String path);
    }

    public SelectInputOutputFileDialog(Context context, InputOutputFileDialogListener inputOutputFileDialogListener) {
        this.context = context;
        this.inputOutputFileDialogListener = inputOutputFileDialogListener;
        this.init();
    }

    public void init() {
        items = new String[2];
        items[0] = context.getString(R.string.data_backup);
        items[1] = context.getString(R.string.data_restore);
    }

    public AlertDialog.Builder createOpenFileDialog() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.data_select_operation))
                .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedItem = which;
                    }
                })
                .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        if (checkedItem == 0 || checkedItem == 1) {
                            inputOutputFileDialogListener.onSelectOperationClicked(items[checkedItem]);
                        } else {
                            Toast ts = Toast.makeText(context, context.getString(R.string.data_select_operation_err_message), Toast.LENGTH_SHORT);
                            ts.show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        return builder;
    }
}
