package com.abuidris.reports.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.abuidris.reports.R;
import com.abuidris.reports.data.model.Report;
import com.abuidris.reports.util.SoundManager;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {
    private List<Report> reports;
    private final OnReportClickListener listener;
    private int lastAnimatedPosition = -1;

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    public ReportsAdapter(OnReportClickListener listener) {
        this.listener = listener;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = reports.get(position);
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);

        String title = report.monthName + " " + report.year;
        holder.monthText.setText(title);
        holder.revenueText.setText(fmt.format(report.totalRevenue));
        holder.samplesText.setText(fmt.format(report.samplesCount));

        holder.itemView.setOnClickListener(v -> {
            SoundManager.getInstance(v.getContext()).playClick();
            if (listener != null) listener.onReportClick(report);
        });

        runItemAnimation(holder.itemView, position);
    }

    private void runItemAnimation(View view, int position) {
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            TranslateAnimation slide = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f
            );
            slide.setDuration(300 + (position * 50));
            slide.setStartOffset(50);
            view.startAnimation(slide);

            AlphaAnimation fade = new AlphaAnimation(0f, 1f);
            fade.setDuration(300 + (position * 50));
            fade.setStartOffset(50);
            view.startAnimation(fade);
        }
    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView monthText, revenueText, samplesText;

        ViewHolder(View itemView) {
            super(itemView);
            monthText = itemView.findViewById(R.id.reportMonthText);
            revenueText = itemView.findViewById(R.id.revenueText);
            samplesText = itemView.findViewById(R.id.samplesText);
        }
    }
}
