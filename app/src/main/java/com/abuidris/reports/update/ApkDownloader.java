package com.abuidris.reports.update;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApkDownloader {

    public interface Callback {
        void onProgress(int percent);
        void onComplete(File apkFile);
        void onError(String error);
    }

    public static void download(Context context, String url, Callback callback) {
        new Thread(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            final File[] apkRef = new File[1];
            try {
                URL downloadUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.connect();

                int totalSize = conn.getContentLength();
                apkRef[0] = new File(context.getExternalFilesDir(null), "update.apk");
                File apkFile = apkRef[0];

                if (apkFile.exists()) apkFile.delete();

                InputStream input = conn.getInputStream();
                FileOutputStream output = new FileOutputStream(apkFile);

                byte[] buffer = new byte[8192];
                int downloaded = 0;
                int len;
                while ((len = input.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                    downloaded += len;
                    if (totalSize > 0) {
                        int percent = (int) ((long) downloaded * 100 / totalSize);
                        mainHandler.post(() -> callback.onProgress(percent));
                    }
                }
                output.close();
                input.close();

                if (totalSize > 0 && apkFile.length() != totalSize) {
                    apkFile.delete();
                    mainHandler.post(() -> callback.onError("حجم الملف لا يتطابق مع الحجم المعلن"));
                    return;
                }

                mainHandler.post(() -> callback.onComplete(apkRef[0]));

            } catch (Exception e) {
                if (apkRef[0] != null && apkRef[0].exists()) apkRef[0].delete();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }
}
