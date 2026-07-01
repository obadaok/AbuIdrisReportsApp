package com.abuidris.reports.update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

public class UpdateManager {

    private static final String PREFS_NAME = "update_prefs";
    private static final String KEY_PENDING_VERSION = "pending_version_code";
    private static final String KEY_APK_PATH = "pending_apk_path";

    private final Context context;

    public UpdateManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void checkAndDownload() {
        VersionChecker.check(context, new VersionChecker.Callback() {
            @Override
            public void onResult(int remoteVersionCode, String apkUrl, String versionName) {
                showProgressAndDownload(remoteVersionCode, apkUrl, versionName);
            }

            @Override
            public void onError(String error) {
                android.util.Log.w("UpdateManager", "Version check error: " + error);
            }

            @Override
            public void onUpToDate() {
            }
        });
    }

    public void checkPendingInstall() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int pendingVersion = prefs.getInt(KEY_PENDING_VERSION, -1);
        if (pendingVersion == -1) return;

        int currentVersion = getLocalVersionCode();

        if (pendingVersion <= currentVersion) {
            String apkPath = prefs.getString(KEY_APK_PATH, null);
            if (apkPath != null) new File(apkPath).delete();
            prefs.edit().clear().apply();
            return;
        }

        String apkPath = prefs.getString(KEY_APK_PATH, null);
        if (apkPath == null) {
            prefs.edit().clear().apply();
            return;
        }

        File apkFile = new File(apkPath);
        if (apkFile.exists()) {
            promptInstall(apkFile);
        } else {
            prefs.edit().clear().apply();
        }
    }

    private int getLocalVersionCode() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    private void showProgressAndDownload(int versionCode, String apkUrl, String versionName) {
        UpdateDialog dialog = UpdateDialog.show(context, versionName);
        dialog.setCancelable(false);

        ApkDownloader.download(context, apkUrl, new ApkDownloader.Callback() {
            @Override
            public void onProgress(int percent) {
                dialog.setProgress(percent);
            }

            @Override
            public void onComplete(File apkFile) {
                dialog.dismiss();
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                    .putInt(KEY_PENDING_VERSION, versionCode)
                    .putString(KEY_APK_PATH, apkFile.getAbsolutePath())
                    .apply();
                promptInstall(apkFile);
            }

            @Override
            public void onError(String error) {
                dialog.dismiss();
            }
        });
    }

    private void promptInstall(File apkFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                Intent settingsIntent = new Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + context.getPackageName())
                );
                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);
                return;
            }
        }

        Uri apkUri = FileProvider.getUriForFile(
            context,
            context.getPackageName() + ".fileprovider",
            apkFile
        );

        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(installIntent);
    }
}
