package com.highcom.todolog.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.os.HandlerCompat;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.ToDoLogRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            ToDoLogRepository.getInstance(context).close();
            progressBar.setProgress(100);
            PostExecutor postExecutor = new PostExecutor();
            _handler.post(postExecutor);
        }
    }

    private class PostExecutor implements Runnable {
        @UiThread
        @Override
        public void run() {
            progressAlertDialog.dismiss();
            listener.importComplete();
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.data_restore))
                    .setMessage(context.getString(R.string.restore_complete_message) + System.getProperty("line.separator") + uri.getPath().replace(":", "/"))
                    .setPositiveButton(R.string.ok, null)
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
                .setMessage(context.getString(R.string.restore_message_front) + uri.getPath().replace(":", "/") + System.getProperty("line.separator") + context.getString(R.string.restore_message_rear))
                .setPositiveButton(R.string.exec, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (importDatabase(uri)) {
                            execImportDatabase();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean importDatabase(final Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);

            String path = context.getDatabasePath("todolog_database").getPath();
            File file = new File (path);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            inputStream.close();
        } catch (Exception exc) {
            Toast ts = Toast.makeText(context, context.getString(R.string.restore_failed_message), Toast.LENGTH_SHORT);
            ts.show();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    private void execImportDatabase()
    {
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
}
