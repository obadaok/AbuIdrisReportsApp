package com.abuidris.reports.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {

    private static final String PREFS_NAME = "update_prefs";
    private static final String KEY_LAST_CHECK = "last_check_timestamp";
    private static final long CHECK_INTERVAL_MS = 60 * 1000;

    private static final String GITHUB_API = "https://api.github.com/repos/obadaok/AbuIdrisReportsApp/releases/latest";

    public interface Callback {
        void onResult(int remoteVersionCode, String apkUrl, String versionName);
        void onError(String error);
        void onUpToDate();
    }

    public static void check(Context context, Callback callback) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastCheck = prefs.getLong(KEY_LAST_CHECK, 0);
        if (System.currentTimeMillis() - lastCheck < CHECK_INTERVAL_MS) {
            callback.onUpToDate();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(GITHUB_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int status = conn.getResponseCode();
                if (status != 200) {
                    BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = err.readLine()) != null) sb.append(line);
                    err.close();
                    callback.onError("GitHub API error " + status + ": " + sb);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                org.json.JSONObject json = new org.json.JSONObject(response.toString());
                String tagName = json.optString("tag_name", "");
                int versionCode = extractVersionCode(tagName);

                if (versionCode == 0) {
                    callback.onError("لم نتمكن من قراءة رقم الإصدار من: " + tagName);
                    return;
                }

                int localVersionCode = getLocalVersionCode(context);

                if (versionCode <= localVersionCode) {
                    callback.onUpToDate();
                    return;
                }

                String apkUrl = null;
                org.json.JSONArray assets = json.optJSONArray("assets");
                if (assets != null) {
                    for (int i = 0; i < assets.length(); i++) {
                        org.json.JSONObject asset = assets.getJSONObject(i);
                        String name = asset.optString("name", "");
                        if (name.endsWith(".apk")) {
                            apkUrl = asset.optString("browser_download_url");
                            break;
                        }
                    }
                }

                if (apkUrl == null) {
                    callback.onError("لم يتم العثور على ملف APK في الإصدار الأخير");
                    return;
                }

                prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply();
                callback.onResult(versionCode, apkUrl, tagName);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private static int getLocalVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private static int extractVersionCode(String tagName) {
        if (tagName == null || tagName.isEmpty()) return 0;
        String digits = tagName.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
