package com.abuidris.reports;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.abuidris.reports.data.model.ExpenseItem;
import com.abuidris.reports.data.model.Report;
import com.abuidris.reports.ui.viewmodel.ReportViewModel;
import com.abuidris.reports.util.NotificationDialog;
import com.abuidris.reports.util.SoundManager;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ReportDetailActivity extends AppCompatActivity {
    private ReportViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        String reportId = getIntent().getStringExtra("report_id");
        if (reportId == null && getIntent().getData() != null) {
            String path = getIntent().getData().getPath();
            if (path != null) reportId = path.replace("/", "");
        }

        if (reportId == null || reportId.isEmpty()) {
            finish();
            return;
        }

        NotificationDialog.checkAndShow(this, getIntent());

        String finalReportId = reportId;
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        viewModel.getAllReports().observe(this, reports -> {
            for (Report report : reports) {
                if (report.monthKey != null && report.monthKey.equals(finalReportId)) {
                    populateReport(report);
                    animateContent();
                    return;
                }
            }
        });
    }

    private void animateContent() {
        LinearLayout root = findViewById(R.id.reportDetailRoot);
        if (root == null) return;
        try {
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(500);
            root.startAnimation(fadeIn);
        } catch (Exception ignored) {}
    }

    private void animateViewsSequentially(View... views) {
        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            if (v == null) continue;
            v.setAlpha(0f);
            v.postDelayed(() -> {
                AlphaAnimation fade = new AlphaAnimation(0f, 1f);
                fade.setDuration(300);
                v.startAnimation(fade);
                v.setAlpha(1f);
            }, 100 + (i * 120L));
        }
    }

    private void populateReport(Report report) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);

        String title = report.monthName + " " + report.year;
        ((TextView) findViewById(R.id.detailMonthTitle)).setText(title);
        ((TextView) findViewById(R.id.detailSamples)).setText(fmt.format(report.samplesCount));
        ((TextView) findViewById(R.id.detailRevenue)).setText(fmt.format(report.totalRevenue));
        ((TextView) findViewById(R.id.detailExpenses)).setText(fmt.format(report.totalExpenses));
        ((TextView) findViewById(R.id.detailNetProfit)).setText(fmt.format(report.netProfit));

        TextView titleView = findViewById(R.id.detailMonthTitle);
        TextView samplesView = findViewById(R.id.detailSamples);
        TextView revenueView = findViewById(R.id.detailRevenue);
        TextView expensesView = findViewById(R.id.detailExpenses);
        TextView profitView = findViewById(R.id.detailNetProfit);
        animateViewsSequentially(titleView, samplesView, revenueView, expensesView, profitView);

        View.OnClickListener clickWithSound = v -> SoundManager.getInstance(this).playClick();
        titleView.setOnClickListener(clickWithSound);
        samplesView.setOnClickListener(clickWithSound);
        revenueView.setOnClickListener(clickWithSound);
        expensesView.setOnClickListener(clickWithSound);
        profitView.setOnClickListener(clickWithSound);

        LinearLayout expensesContainer = findViewById(R.id.expensesDetailLayout);
        expensesContainer.removeAllViews();

        List<ExpenseItem> expenseItems = report.expenseItems;
        if (expenseItems == null || expenseItems.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("لا توجد بنود مفصلة");
            emptyText.setTextColor(0xFF666666);
            emptyText.setTextSize(14);
            emptyText.setPadding(0, 8, 0, 8);
            expensesContainer.addView(emptyText);
            return;
        }
        for (int i = 0; i < expenseItems.size(); i++) {
            ExpenseItem item = expenseItems.get(i);
            View row = getLayoutInflater().inflate(R.layout.item_expense_row, null);
            TextView desc = row.findViewById(R.id.expenseDesc);
            TextView amount = row.findViewById(R.id.expenseAmount);
            desc.setText(item.description);
            amount.setText(fmt.format(item.amount));

            row.setOnClickListener(v -> SoundManager.getInstance(this).playClick());
            desc.setOnClickListener(v -> SoundManager.getInstance(this).playClick());
            amount.setOnClickListener(v -> SoundManager.getInstance(this).playClick());

            AlphaAnimation fade = new AlphaAnimation(0f, 1f);
            fade.setDuration(250);
            fade.setStartOffset(i * 40L);
            row.startAnimation(fade);

            expensesContainer.addView(row);

            if (i < expenseItems.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(0xFFCCCCCC);
                expensesContainer.addView(divider);
            }
        }
    }
}
