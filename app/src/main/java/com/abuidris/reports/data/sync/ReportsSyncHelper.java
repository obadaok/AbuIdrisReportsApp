package com.abuidris.reports.data.sync;

import androidx.annotation.NonNull;
import com.abuidris.reports.data.local.AppDatabase;
import com.abuidris.reports.data.model.Report;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportsSyncHelper {

    private final DatabaseReference reportsRef;
    private final AppDatabase appDatabase;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private ValueEventListener listener;

    public ReportsSyncHelper(AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
        this.reportsRef = FirebaseDatabase.getInstance().getReference("reports");
    }

    public void startSync() {
        if (listener != null) return;
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Report> byMonth = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Report r = child.getValue(Report.class);
                    if (r == null || r.monthKey == null || !r.monthKey.matches("\\d{4}-\\d{2}")) continue;
                    Report existing = byMonth.get(r.monthKey);
                    if (existing == null) {
                        byMonth.put(r.monthKey, r);
                        continue;
                    }
                    boolean candidateCanonical = child.getKey().equals(r.monthKey);
                    if (candidateCanonical || r.createdAt > existing.createdAt) {
                        byMonth.put(r.monthKey, r);
                    }
                }
                List<Report> reports = new ArrayList<>(byMonth.values());
                dbExecutor.execute(() -> appDatabase.reportDao().replaceAll(reports));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // لا تمسح الكاش عند فشل الشبكة — ابق على آخر بيانات معروفة
            }
        };
        reportsRef.addValueEventListener(listener);
    }

    public void stopSync() {
        if (listener != null) {
            reportsRef.removeEventListener(listener);
            listener = null;
        }
        dbExecutor.shutdown();
    }
}
