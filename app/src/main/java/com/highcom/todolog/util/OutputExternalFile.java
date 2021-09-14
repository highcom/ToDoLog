package com.highcom.todolog.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.ToDoLogRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public class OutputExternalFile {
    private Context context;

    public OutputExternalFile(Context context) {
        this.context = context;
    }

    public void outputSelectFolder(final Uri uri) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.data_backup))
                .setMessage(context.getString(R.string.backup_message_front) + uri.getPath().replace(":", "/") + System.getProperty("line.separator") + context.getString(R.string.backup_message_rear))
                .setPositiveButton(R.string.exec, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exportDatabase(uri);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean exportDatabase(final Uri uri) {
        OutputStream outputStream = null;
        try {
//            ToDoLogRepository.getInstance(context).closeDatabase();
            String path = context.getDatabasePath("todolog_database").getPath();
            File file = new File (path);
            InputStream inputStream = new FileInputStream(file);
            outputStream = context.getContentResolver().openOutputStream(uri);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
//            ToDoLogRepository.getInstance(context).openDatabase();
        } catch (FileNotFoundException exc) {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.data_backup))
                    .setMessage(context.getString(R.string.no_access_message))
                    .setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + context.getPackageName()));
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.end, null)
                    .show();
            return false;
        } catch (Exception exc) {
            Toast ts = Toast.makeText(context, context.getString(R.string.backup_failed_message), Toast.LENGTH_SHORT);
            ts.show();
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.data_backup))
                .setMessage(context.getString(R.string.backup_complete_message) + System.getProperty("line.separator") + uri.getPath().replace(":", "/"))
                .setPositiveButton(R.string.ok, null)
                .show();
        return true;
    }
}
