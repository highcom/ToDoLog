package com.highcom.todolog.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.highcom.todolog.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String pathList[] = new String[3];
            pathList[0] = context.getDatabasePath(SelectInputOutputFileDialog.TODOLOG_DB_NAME).getPath();
            pathList[1] = context.getDatabasePath(SelectInputOutputFileDialog.TODOLOG_DB_NAME_SHM).getPath();
            pathList[2] = context.getDatabasePath(SelectInputOutputFileDialog.TODOLOG_DB_NAME_WAL).getPath();
            String fileList[] = new String[3];
            fileList[0] = SelectInputOutputFileDialog.TODOLOG_DB_NAME;
            fileList[1] = SelectInputOutputFileDialog.TODOLOG_DB_NAME_SHM;
            fileList[2] = SelectInputOutputFileDialog.TODOLOG_DB_NAME_WAL;

            outputStream = context.getContentResolver().openOutputStream(uri);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            for (int i = 0; i < pathList.length; i++) {
                inputStream = new FileInputStream(pathList[i]);
                ZipEntry zipEntry = new ZipEntry(fileList[i]);
                zipOutputStream.putNextEntry(zipEntry);

                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    zipOutputStream.write(buf, 0, len);
                }

                inputStream.close();
                zipOutputStream.closeEntry();
            }

            zipOutputStream.close();
            outputStream.close();
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
