package com.abuidris.reports.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.abuidris.reports.data.local.AppDatabase;
import com.abuidris.reports.data.model.Report;
import java.util.List;

public class ReportViewModel extends AndroidViewModel {

    private final LiveData<List<Report>> allReports;

    public ReportViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        allReports = db.reportDao().getAllReports();
    }

    public LiveData<List<Report>> getAllReports() {
        return allReports;
    }
}
