package com.abuidris.reports.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "reports")
public class Report {
    @PrimaryKey
    @NonNull
    public String monthKey = "";

    public String monthName;
    public int year;
    public int samplesCount;
    public long totalRevenue;
    public long totalExpenses;
    public long netProfit;
    public long createdAt;
    public List<ExpenseItem> expenseItems;
}
