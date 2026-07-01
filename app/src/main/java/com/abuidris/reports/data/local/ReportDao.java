package com.abuidris.reports.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import com.abuidris.reports.data.model.Report;
import java.util.List;

@Dao
public abstract class ReportDao {

    @Query("SELECT * FROM reports ORDER BY year DESC, monthKey DESC")
    public abstract LiveData<List<Report>> getAllReports();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<Report> reports);

    @Query("DELETE FROM reports")
    public abstract void deleteAll();

    @Transaction
    public void replaceAll(List<Report> reports) {
        deleteAll();
        insertAll(reports);
    }
}
