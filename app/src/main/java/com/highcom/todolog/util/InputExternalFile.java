package com.highcom.todolog.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.os.HandlerCompat;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.ToDoLogRepository;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InputExternalFile {
    private Context context;
    private Activity activity;
    private InputExternalFileListener listener;
    private Uri uri;
    private AlertDialog progressAlertDialog;
    private ProgressBar progressBar;

    public interface InputExternalFileListener {
        void importComplete();
    }

    private class BackgroundTask implements Runnable {
        private final android.os.Handler _handler;

        public BackgroundTask(android.os.Handler handler) {
            _handler = handler;
        }

        @WorkerThread
        @Override
        public void run() {
            ToDoLogRepository.getInstance(context).closeDatabase();

            ZipInputStream zipInputStream = null;
            ZipEntry zipEntry = null;
            BufferedOutputStream outputStream = null;
            int len = 0;
            int progressParam = 0;

            try {
                zipInputStream = new ZipInputStream(context.getContentResolver().openInputStream(uri));
                // ZIPファイルからDBファイルだけを抽出する
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    File newfile = new File(zipEntry.getName());

                    if (!newfile.getName().equals(SelectInputOutputFileDialog.TODOLOG_DB_NAME_WAL)
                        && !newfile.getName().equals(SelectInputOutputFileDialog.TODOLOG_DB_NAME_SHM)
                        && !newfile.getName().equals(SelectInputOutputFileDialog.TODOLOG_DB_NAME)) {
                        continue;
                    }

                    // 出力用ファイルストリームの生成
                    outputStream = new BufferedOutputStream(new FileOutputStream(context.getDatabasePath(newfile.getName()).getPath()));

                    // エントリの内容を出力
                    byte[] buffer = new byte[1024];
                    while ((len = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }

                    zipInputStream.closeEntry();
                    outputStream.close();
                    if (progressParam + 30 < 100) {
                        progressParam += 30;
                        progressBar.setProgress(progressParam);
                    }
                }

                zipInputStream.close();
            } catch (Exception exc) {
                Toast ts = Toast.makeText(context, context.getString(R.string.restore_failed_message), Toast.LENGTH_SHORT);
                ts.show();
            } finally {
                try {
                    if (zipInputStream != null) {
                        zipInputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                progressBar.setProgress(100);

                ToDoLogRepository.getInstance(context).openDatabase();

                PostExecutor postExecutor = new PostExecutor();
                _handler.post(postExecutor);
            }
        }
    }

    private class PostExecutor implements Runnable {
        @UiThread
        @Override
        public void run() {
            progressAlertDialog.dismiss();
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.data_restore))
                    .setMessage(context.getString(R.string.restore_complete_message) + System.getProperty("line.separator") + getFileNameByUri(context, uri))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            listener.importComplete();
                        }
                    })
                    .show();
        }
    }

    public InputExternalFile(Activity activity, InputExternalFileListener listener) {
        this.activity = activity;
        this.context = activity;
        this.listener = listener;
    }

    public void inputSelectFolder(final Uri uri) {
        this.uri = uri;
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.data_restore))
                .setMessage(context.getString(R.string.restore_message_front) + getFileNameByUri(context, uri) + System.getProperty("line.separator") + context.getString(R.string.restore_message_rear))
                .setPositiveButton(R.string.exec, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (importDatabase(uri)) {
                            execImportDatabase();
                        } else {
                            // 指定されたバックアップファイルが正しくないエラーを表示する
                            new AlertDialog.Builder(context)
                                    .setTitle(context.getString(R.string.data_restore))
                                    .setMessage(context.getString(R.string.restore_failed_error_data_message))
                                    .setPositiveButton(R.string.ok, null)
                                    .show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean importDatabase(final Uri uri) {
        boolean fileValidate[] = {false, false, false};
        ZipInputStream zipInputStream = null;
        ZipEntry zipEntry = null;
        try {
            zipInputStream = new ZipInputStream(context.getContentResolver().openInputStream(uri));

            // ZIPファイルにDBファイルが全て含まれているか確認する
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                if (fileName.contains(SelectInputOutputFileDialog.TODOLOG_DB_NAME_WAL)) {
                    fileValidate[2] = true;
                    continue;
                }

                if (fileName.contains(SelectInputOutputFileDialog.TODOLOG_DB_NAME_SHM)) {
                    fileValidate[1] = true;
                    continue;
                }

                if (fileName.contains(SelectInputOutputFileDialog.TODOLOG_DB_NAME)) {
                    fileValidate[0] = true;
                    continue;
                }
            }

            zipInputStream.close();

            if (fileValidate[0] && fileValidate[1] && fileValidate[2]) {
                return true;
            }
        } catch (Exception exc) {
            Toast ts = Toast.makeText(context, context.getString(R.string.restore_failed_message), Toast.LENGTH_SHORT);
            ts.show();
            return false;
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private void execImportDatabase() {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.data_restore))
                .setMessage(context.getString(R.string.restore_confirm_message))
                .setPositiveButton(R.string.exec, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取込み中のプログレスバーを表示する
                        progressAlertDialog = new AlertDialog.Builder(context)
                                .setTitle(R.string.restore_processing)
                                .setView(activity.getLayoutInflater().inflate(R.layout.alert_progressbar, null))
                                .create();
                        progressAlertDialog.show();
                        progressAlertDialog.setCancelable(false);
                        progressAlertDialog.setCanceledOnTouchOutside(false);
                        progressBar = progressAlertDialog.findViewById(R.id.ProgressBarHorizontal);

                        // ワーカースレッドで取込みを開始する
                        Looper mainLooper = Looper.getMainLooper();
                        Handler handler = HandlerCompat.createAsync(mainLooper);
                        BackgroundTask backgroundTask = new BackgroundTask(handler);
                        ExecutorService executorService  = Executors.newSingleThreadExecutor();
                        executorService.submit(backgroundTask);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String getFileNameByUri(Context context, Uri uri) {
        String fileName = "";
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver()
                .query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            }
            cursor.close();
        }

        return fileName;
    }
}
