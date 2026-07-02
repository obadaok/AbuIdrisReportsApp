package com.abuidris.reports;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.abuidris.reports.data.model.Report;
import com.abuidris.reports.ui.adapter.ReportsAdapter;
import com.abuidris.reports.ui.viewmodel.ReportViewModel;
import com.abuidris.reports.update.UpdateManager;
import com.abuidris.reports.util.NotificationDialog;

public class MainActivity extends AppCompatActivity {
    private ReportViewModel viewModel;
    private ReportsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        handleDeepLink(getIntent());
        NotificationDialog.checkAndShow(this, getIntent());

        UpdateManager um = AbuIdrisApp.getUpdateManager();
        if (um != null) um.processUpdate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateManager um = AbuIdrisApp.getUpdateManager();
        if (um != null) um.processUpdate(this);
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
