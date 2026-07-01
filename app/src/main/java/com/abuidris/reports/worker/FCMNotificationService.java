package com.abuidris.reports.worker;

import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.abuidris.reports.AbuIdrisApp;
import com.abuidris.reports.MainActivity;
import com.abuidris.reports.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification() != null
            ? remoteMessage.getNotification().getTitle() : "معمل أبو إدريس";
        String body = remoteMessage.getNotification() != null
            ? remoteMessage.getNotification().getBody() : "";
        String reportId = remoteMessage.getData() != null
            ? remoteMessage.getData().get("report_id") : null;

        Intent notificationIntent;
        if (reportId != null && !reportId.isEmpty()) {
            notificationIntent = new Intent(this, com.abuidris.reports.ReportDetailActivity.class);
            notificationIntent.putExtra("report_id", reportId);
        } else {
            notificationIntent = new Intent(this, MainActivity.class);
        }
        notificationIntent.putExtra("notif_title", title);
        notificationIntent.putExtra("notif_body", body);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
            this, (int) System.currentTimeMillis(), notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AbuIdrisApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
