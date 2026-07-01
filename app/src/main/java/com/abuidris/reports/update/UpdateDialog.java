package com.abuidris.reports.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abuidris.reports.R;

public class UpdateDialog {

    private final AlertDialog dialog;
    private final TextView statusText;
    private final ProgressBar progressBar;

    private UpdateDialog(AlertDialog dialog, TextView statusText, ProgressBar progressBar) {
        this.dialog = dialog;
        this.statusText = statusText;
        this.progressBar = progressBar;
    }

    public static UpdateDialog show(Context context, String versionName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Translucent_NoTitleBar);

        FrameLayout wrapper = new FrameLayout(context);
        wrapper.setLayoutParams(new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        View shadowLayer = new View(context);
        GradientDrawable shadowBg = new GradientDrawable();
        shadowBg.setColor(Color.BLACK);
        shadowBg.setCornerRadius(0);
        shadowLayer.setBackground(shadowBg);
        FrameLayout.LayoutParams shadowParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        shadowLayer.setLayoutParams(shadowParams);
        wrapper.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        float density = context.getResources().getDisplayMetrics().density;
        int dp = (int) density;
        int paddingDp = (int) (20 * density + 0.5f);
        card.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setStroke((int) (3 * density + 0.5f), Color.BLACK);
        cardBg.setCornerRadius(0);
        card.setBackground(cardBg);

        TextView titleView = new TextView(context);
        titleView.setText("تحديث التطبيق");
        titleView.setTextColor(Color.BLACK);
        titleView.setTextSize(18);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        int marginDp12 = (int) (12 * density + 0.5f);
        ((LinearLayout.LayoutParams) titleView.getLayoutParams()).bottomMargin = marginDp12;

        TextView versionView = new TextView(context);
        versionView.setText("الإصدار: " + versionName);
        versionView.setTextColor(Color.BLACK);
        versionView.setTextSize(13);
        versionView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        versionView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ((LinearLayout.LayoutParams) versionView.getLayoutParams()).bottomMargin = marginDp12;

        TextView statusView = new TextView(context);
        statusView.setText("جاري تحميل التحديث...");
        statusView.setTextColor(Color.BLACK);
        statusView.setTextSize(14);
        statusView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        statusView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ((LinearLayout.LayoutParams) statusView.getLayoutParams()).bottomMargin = marginDp12;

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        LayerDrawable progressDrawable = (LayerDrawable) progressBar.getProgressDrawable();
        progressDrawable.getDrawable(0).setColorFilter(Color.parseColor("#CCCCCC"), PorterDuff.Mode.SRC_IN);
        progressDrawable.getDrawable(1).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        progressBar.setProgressDrawable(progressDrawable);

        int progressMargin = (int) (8 * density + 0.5f);
        FrameLayout progressWrapper = new FrameLayout(context);
        progressWrapper.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) (32 * density + 0.5f)
        ));
        progressWrapper.addView(progressBar, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            (int) (24 * density + 0.5f),
            android.view.Gravity.CENTER_VERTICAL
        ));

        card.addView(titleView);
        card.addView(versionView);
        card.addView(statusView);
        card.addView(progressWrapper);

        AlertDialog dialog = builder.setView(wrapper).setCancelable(false).create();

        wrapper.addView(shadowLayer);
        wrapper.addView(card);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8C000000")));
        dialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0;
        dialog.getWindow().setAttributes(lp);

        int marginDp24 = (int) (24 * density + 0.5f);
        ((View) dialog.getWindow().getDecorView()).setPadding(marginDp24, 0, marginDp24, 0);

        return new UpdateDialog(dialog, statusView, progressBar);
    }

    public void setProgress(int percent) {
        progressBar.setProgress(percent);
        statusText.setText("جاري تحميل التحديث... " + percent + "%");
    }

    public void setStatus(String text) {
        statusText.setText(text);
    }

    public void setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
