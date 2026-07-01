package com.abuidris.reports;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.abuidris.reports.data.local.AppDatabase;
import com.abuidris.reports.data.sync.ReportsSyncHelper;
import com.abuidris.reports.update.UpdateManager;
import com.abuidris.reports.util.SoundManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashSet;
import java.util.Set;

public class AbuIdrisApp extends Application {
    public static final String CHANNEL_ID = "reports_channel";
    public static final String CHANNEL_NAME = "تقارير المعمل";
    private static final String PREFS_NAME = "notification_prefs";
    private static final String SHOWN_NOTIFS_KEY = "shown_notifications";
    private ReportsSyncHelper syncHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        listenForNotifications();
        AppDatabase db = AppDatabase.getInstance(this);
        syncHelper = new ReportsSyncHelper(db);
        syncHelper.startSync();

        SoundManager.getInstance(this).preload();

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        UpdateManager updateManager = new UpdateManager(this);
        updateManager.checkPendingInstall();
        updateManager.checkAndDownload();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("إشعارات التقارير الشهرية للمعمل");
            channel.setShowBadge(true);
            channel.enableVibration(true);

            Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
            channel.setSound(soundUri, new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void listenForNotifications() {
        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications");
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        notifRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                String key = snapshot.getKey();
                if (key == null) return;

                Set<String> shown = prefs.getStringSet(SHOWN_NOTIFS_KEY, new HashSet<>());
                if (shown.contains(key)) return;

                String title = snapshot.child("title").getValue(String.class);
                String body = snapshot.child("body").getValue(String.class);
                String reportId = snapshot.child("report_id").getValue(String.class);

                if (title == null || body == null) return;

                Intent intent;
                if (reportId != null && !reportId.isEmpty()) {
                    intent = new Intent(AbuIdrisApp.this, ReportDetailActivity.class);
                    intent.putExtra("report_id", reportId);
                } else {
                    intent = new Intent(AbuIdrisApp.this, MainActivity.class);
                }
                intent.putExtra("notif_title", title);
                intent.putExtra("notif_body", body);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                    AbuIdrisApp.this, key.hashCode(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                NotificationCompat.Builder builder = new NotificationCompat.Builder(AbuIdrisApp.this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

                NotificationManagerCompat manager = NotificationManagerCompat.from(AbuIdrisApp.this);
                manager.notify(key.hashCode(), builder.build());

                shown = new HashSet<>(shown);
                shown.add(key);
                prefs.edit().putStringSet(SHOWN_NOTIFS_KEY, shown).apply();
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
