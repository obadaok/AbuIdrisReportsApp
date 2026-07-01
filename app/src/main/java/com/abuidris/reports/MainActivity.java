package com.abuidris.reports;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.abuidris.reports.data.model.Report;
import com.abuidris.reports.ui.adapter.ReportsAdapter;
import com.abuidris.reports.ui.viewmodel.ReportViewModel;
import com.abuidris.reports.util.NotificationDialog;

public class MainActivity extends AppCompatActivity {
    private ReportViewModel viewModel;
    private ReportsAdapter adapter;
    private View permissionOverlay;
    private boolean uiInitialized;

    private final ActivityResultLauncher<String> requestNotificationPermission =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkInstallPermission();
            }
        });

    private final ActivityResultLauncher<Intent> requestInstallPermission =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            onResume();
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkAllPermissions()) {
            initUi();
        } else {
            showPermissionOverlay();
            requestMissingPermissions();
        }

        handleDeepLink(getIntent());
        NotificationDialog.checkAndShow(this, getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionOverlay != null && checkAllPermissions()) {
            hidePermissionOverlay();
            initUi();
        }
    }

    private boolean checkAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (!getPackageManager().canRequestPackageInstalls()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private void requestMissingPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkInstallPermission();
        }
    }

    private void checkInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (!getPackageManager().canRequestPackageInstalls()) {
                    Intent intent = new Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + getPackageName())
                    );
                    requestInstallPermission.launch(intent);
                }
            } catch (Exception e) {
            }
        }
    }

    private void showPermissionOverlay() {
        android.widget.FrameLayout overlay = new android.widget.FrameLayout(this);
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(0xCCFFFFFF);

        float density = getResources().getDisplayMetrics().density;
        int padding = (int) (24 * density + 0.5f);

        android.widget.LinearLayout card = new android.widget.LinearLayout(this);
        card.setOrientation(android.widget.LinearLayout.VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setPadding(padding, padding, padding, padding);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(android.graphics.Color.WHITE);
        bg.setStroke((int) (3 * density + 0.5f), android.graphics.Color.BLACK);
        bg.setCornerRadius(0);
        card.setBackground(bg);

        android.widget.FrameLayout.LayoutParams cardParams = new android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) (24 * density + 0.5f);
        cardParams.setMargins(margin, 0, margin, 0);
        cardParams.gravity = android.view.Gravity.CENTER;

        TextView title = new TextView(this);
        title.setText("الأذونات المطلوبة");
        title.setTextColor(android.graphics.Color.BLACK);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        title.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((android.widget.LinearLayout.LayoutParams) title.getLayoutParams()).bottomMargin = (int) (12 * density + 0.5f);

        TextView msg = new TextView(this);
        msg.setText("يرجى منح الأذونات التالية لتتمكن من استخدام التطبيق:\n\n• الإشعارات ( notifications )\n• تثبيت التحديثات (install updates)\n\nسيتم طلبها الآن.");
        msg.setTextColor(android.graphics.Color.BLACK);
        msg.setTextSize(14);
        msg.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        msg.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((android.widget.LinearLayout.LayoutParams) msg.getLayoutParams()).bottomMargin = (int) (16 * density + 0.5f);

        card.addView(title);
        card.addView(msg);

        android.widget.FrameLayout.LayoutParams shadowParams = new android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        shadowParams.setMargins(margin + (int) (4 * density), (int) (4 * density),
            margin, 0);
        shadowParams.gravity = android.view.Gravity.CENTER;

        View shadow = new View(this);
        android.graphics.drawable.GradientDrawable shadowBg = new android.graphics.drawable.GradientDrawable();
        shadowBg.setColor(android.graphics.Color.BLACK);
        shadowBg.setCornerRadius(0);
        shadow.setBackground(shadowBg);

        overlay.addView(shadow, shadowParams);
        overlay.addView(card, cardParams);

        addContentView(overlay, overlay.getLayoutParams());
        permissionOverlay = overlay;
    }

    private void hidePermissionOverlay() {
        if (permissionOverlay != null) {
            ((ViewGroup) permissionOverlay.getParent()).removeView(permissionOverlay);
            permissionOverlay = null;
        }
    }

    private void initUi() {
        if (uiInitialized) return;
        uiInitialized = true;

        RecyclerView recyclerView = findViewById(R.id.reportsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            LayoutAnimationController anim = AnimationUtils.loadLayoutAnimation(
                this, android.R.anim.slide_in_left);
            recyclerView.setLayoutAnimation(anim);
        } catch (Exception e) {
        }

        adapter = new ReportsAdapter(report -> {
            Intent intent = new Intent(MainActivity.this, ReportDetailActivity.class);
            intent.putExtra("report_id", report.monthKey);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        viewModel.getAllReports().observe(this, reports -> {
            adapter.setReports(reports);
            recyclerView.scheduleLayoutAnimation();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLink(intent);
        NotificationDialog.checkAndShow(this, intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) return;
        String path = intent.getData().getPath();
        if (path != null && path.startsWith("/")) {
            String reportId = path.substring(1);
            Intent detailIntent = new Intent(this, ReportDetailActivity.class);
            detailIntent.putExtra("report_id", reportId);
            startActivity(detailIntent);
        }
    }
}
