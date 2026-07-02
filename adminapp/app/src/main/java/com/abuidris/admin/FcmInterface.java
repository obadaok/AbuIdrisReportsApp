package com.abuidris.admin;

import android.webkit.JavascriptInterface;

public class FcmInterface {

    public interface UiCallback {
        void onResult(String json);
    }

    private UiCallback callback;

    public void setCallback(UiCallback callback) {
        this.callback = callback;
    }

    @JavascriptInterface
    public void sendNotification(String title, String body) {
        FcmSender.sendNotification(title, body, new FcmSender.Callback() {
            @Override
            public void onSuccess(String message) {
                if (callback != null) {
                    callback.onResult("{\"success\":true,\"message\":\"" + message + "\"}");
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onResult("{\"success\":false,\"error\":\"" + error.replace("\"", "'") + "\"}");
                }
            }
        });
    }
}
