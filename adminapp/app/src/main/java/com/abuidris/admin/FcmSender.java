package com.abuidris.admin;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

public class FcmSender {

    private static final String CLIENT_EMAIL = "firebase-adminsdk-fbsvc@bida-ai-999.iam.gserviceaccount.com";
    private static final String PROJECT_ID = "bida-ai-999";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";

    private static final String PRIVATE_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n" +
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDnv4t7Zepz1jxK\n" +
        "+dmfYjY3AqvEK0yoCI1ww+hCXBzhOfAP9InZU69RnWFj8poKEH92O3JTTEMT6BMI\n" +
        "p+RHY2/VFFi+3P4XwIRsOuT42CrPaiw1TNFbccI59osRYbTggklW1/5tY1FkhWY/\n" +
        "sXlQ51HCrZa5QzFB4iJ8teZ6Ek/IXgWBP4hcC8GO2wR1zHvmWpYQHHCVOCsX92Iy\n" +
        "kyT5rPEcvfHEAPt8fNZ5L8AlpNo22YMHp1nCWBHICfr//YjzYj86sn61xyB9jgNf\n" +
        "ku+erQqbfMxYhHg/rAeE9l/M8KIWY56Xl50aD6Kt0ghSKRI5BArrYUHzR8LgIKXL\n" +
        "rXumvAInAgMBAAECggEACq7Esjp/dTmB6/7abkNrtlt0KYXJcdF7G6CcOzrdxfAS\n" +
        "xdmC5Ta7VSrJC39qZF5gUcXpvjc8vFUtU2/R0QyfYhYfBSVQvS2NrSOF3Iayan89\n" +
        "bHOPDwUOa/eWb4Wufc7jrJImU0qJvPV9QiBfNECFf1DbKs0THzNaXZ9Q8X2YmebN\n" +
        "JU/0vjo71n2sblAhGX2N1EQlqrmQALKBvUvGbVLqFBkPdEyZF4kVQiaBFaNzrHHM\n" +
        "sEpFrqO36Y9JP1Np8Os0g3l4pVC/dMquyNwqlDs07KBTMvhpsqFj5NFL2PH+l3pO\n" +
        "8lQr7qRq/61lws0fbnJTLnoF4bw61l90oqxBeQCrmQKBgQD/HCC2PAZmvnMJGtpX\n" +
        "whxfx9HvJzQ3FAtui28nDC56x1mkiX8ZYD2H48a+wc9IKT6x22zYPA9dlbwWaCgm\n" +
        "K9wsnwsf01M9yM90j+fxVli09PQgQW20vyU/UAEuAS5cAt0J8J0UPXss7xNyGXIN\n" +
        "6Ync17Z1BKV5oor462W4miyoHQKBgQDojoy5XYtSyX+3Oa4lBW1M7FmYUggg59Rb\n" +
        "ZDysvP5iwe6is21XzZf2byCzwYLsJAVS/LFQYfs0YoEtVUGpQUhuEd4aQ1yvpZBp\n" +
        "DkuJ5o61FQOPbEkgoLhlNneNoj/l2y6VnXpM91QAy60X220E2vruidzETyS8xmpJ\n" +
        "om3h9RMoEwKBgGApuir3dXJFWBeWh7eM9CUmrXv4yzX32FdTOBMABIVDoAblzYND\n" +
        "Q+51bfV6GSiaY4enrt/Tw/cC+mN336qD3frz+L2Ga1pe19SYalYOtmF/9IY0I6Z+\n" +
        "Zwv2nHs69r64VZnpmUyH4GaPuETKUrPr1IvqxFxX5Ah1vO0XzsBHXY4BAoGABr/M\n" +
        "WbzbREXQzRKBeVWU/HyHhSIsVjgkM5rPH/xELb2PHW2zqK6w0FgdTNo64HuTI/tt\n" +
        "cfXGApkxQo2M4EqivX8LQRvfXsGhVUsy6gek8KBxgAIWhTnk4BsakSO6UwNvcKwn\n" +
        "VoB/EZ7XoS+fFFr+mVmq7I+Xc1sWzphGNB6Wtb0CgYEA8CbfirzYXzaYCAZJ4QGM\n" +
        "M1VCAlFn9S0qvcMzpKQUcFT+grOORSpy64+rU8s+hhXQhNG0LQvL8/24oTso7244\n" +
        "IBAr6g30C80AoQ9gfCU84Lg6K3g/Lp101l8pAKYQHoSunDsCebuIUkRO4JN3JSoE\n" +
        "wX643JrSwCoWyeObMLjLORY=\n" +
        "-----END PRIVATE KEY-----";

    public interface Callback {
        void onSuccess(String message);
        void onError(String error);
    }

    public static void sendNotification(String title, String body, Callback callback) {
        new Thread(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            try {
                String accessToken = getAccessToken();
                if (accessToken == null) {
                    mainHandler.post(() -> callback.onError("فشل الحصول على رمز الوصول"));
                    return;
                }

                JSONObject message = new JSONObject();
                JSONObject msgData = new JSONObject();
                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);
                msgData.put("notification", notification);
                msgData.put("topic", "all");
                msgData.put("android", new JSONObject()
                    .put("priority", "high")
                    .put("notification", new JSONObject()
                        .put("channel_id", "reports_channel")
                        .put("sound", "default")));
                message.put("message", msgData);

                URL url = new URL(FCM_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                OutputStream os = conn.getOutputStream();
                os.write(message.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int code = conn.getResponseCode();
                BufferedReader reader;
                if (code >= 200 && code < 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                }
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                if (code >= 200 && code < 300) {
                    mainHandler.post(() -> callback.onSuccess("تم إرسال الإشعار بنجاح"));
                } else {
                    mainHandler.post(() -> callback.onError("خطأ " + code + ": " + response));
                }

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    private static String getAccessToken() throws Exception {
        String jwt = createJwt();
        URL url = new URL(TOKEN_URI);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        String body = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=" + jwt;
        OutputStream os = conn.getOutputStream();
        os.write(body.getBytes(StandardCharsets.UTF_8));
        os.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();

        JSONObject json = new JSONObject(response.toString());
        return json.optString("access_token", null);
    }

    private static String createJwt() throws Exception {
        String privateKeyPEM = PRIVATE_KEY_PEM
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

        byte[] keyBytes = Base64.decode(privateKeyPEM, Base64.DEFAULT);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        long now = System.currentTimeMillis() / 1000;
        JSONObject header = new JSONObject();
        header.put("alg", "RS256");
        header.put("typ", "JWT");

        JSONObject payload = new JSONObject();
        payload.put("iss", CLIENT_EMAIL);
        payload.put("scope", "https://www.googleapis.com/auth/firebase.messaging");
        payload.put("aud", TOKEN_URI);
        payload.put("exp", now + 3600);
        payload.put("iat", now);

        String headerB64 = base64Url(header.toString().getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64Url(payload.toString().getBytes(StandardCharsets.UTF_8));
        String toSign = headerB64 + "." + payloadB64;

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(toSign.getBytes(StandardCharsets.UTF_8));
        String sigB64 = base64Url(signature.sign());

        return toSign + "." + sigB64;
    }

    private static String base64Url(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_PADDING | Base64.NO_WRAP)
            .replace('+', '-')
            .replace('/', '_');
    }
}
