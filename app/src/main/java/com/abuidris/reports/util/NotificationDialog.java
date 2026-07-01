package com.abuidris.reports.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotificationDialog {

    private static final String EXTRA_TITLE = "notif_title";
    private static final String EXTRA_BODY = "notif_body";

    private static final int DP_MARGIN = 24;
    private static final int DP_BORDER = 3;
    private static final int DP_PADDING = 20;
    private static final int DP_GAP_TITLE_BODY = 12;
    private static final int DP_GAP_BODY_BUTTON = 20;
    private static final int DP_BUTTON_HEIGHT = 52;
    private static final int DP_SHADOW_OFFSET = 4;
    private static final int DP_PRESS_SHIFT = 2;
    private static final int SP_TITLE = 18;
    private static final int SP_BODY = 14;
    private static final int SP_BUTTON = 15;

    public static void checkAndShow(Activity activity, Intent intent) {
        if (intent == null) return;
        String title = intent.getStringExtra(EXTRA_TITLE);
        String body = intent.getStringExtra(EXTRA_BODY);
        if (title == null || body == null) return;
        show(activity, title, body);
    }

    private static int dp(View v, float d) {
        float density = v.getResources().getDisplayMetrics().density;
        return (int) (d * density + 0.5f);
    }

    private static void show(Activity activity, String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Translucent_NoTitleBar);

        FrameLayout wrapper = new FrameLayout(activity);
        wrapper.setLayoutParams(new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // طبقة الظل — مستطيل أسود صلب مُزاح 4dp × 4dp
        View shadowLayer = new View(activity);
        GradientDrawable shadowBg = new GradientDrawable();
        shadowBg.setColor(Color.BLACK);
        shadowBg.setCornerRadius(0);
        shadowLayer.setBackground(shadowBg);
        FrameLayout.LayoutParams shadowParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        shadowLayer.setLayoutParams(shadowParams);
        // الظل يبقى يمين-تحت بصريًا بغض النظر عن اتجاه RTL
        wrapper.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        View card = activity.getLayoutInflater().inflate(
            com.abuidris.reports.R.layout.dialog_notification, null
        );
        TextView titleView = card.findViewById(com.abuidris.reports.R.id.dialogTitle);
        TextView bodyView = card.findViewById(com.abuidris.reports.R.id.dialogBody);
        Button okButton = card.findViewById(com.abuidris.reports.R.id.dialogOkButton);

        titleView.setText(title);
        bodyView.setText(body);

        AlertDialog dialog = builder.setView(wrapper).setCancelable(false).create();

        okButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    shadowParams.setMargins(dp(card, DP_PRESS_SHIFT), dp(card, DP_PRESS_SHIFT), 0, 0);
                    card.setTranslationX(dp(card, DP_PRESS_SHIFT));
                    card.setTranslationY(dp(card, DP_PRESS_SHIFT));
                    shadowLayer.requestLayout();
                    card.requestLayout();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    shadowParams.setMargins(dp(card, DP_SHADOW_OFFSET), dp(card, DP_SHADOW_OFFSET), 0, 0);
                    card.setTranslationX(0);
                    card.setTranslationY(0);
                    shadowLayer.requestLayout();
                    card.requestLayout();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.performClick();
                    }
                    return true;
            }
            return false;
        });
        okButton.setOnClickListener(v -> dialog.dismiss());

        wrapper.addView(shadowLayer);
        wrapper.addView(card);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8C000000")));
        dialog.show();
        // هامش 24dp من يمين ويسار الشاشة عشان النافذة ما تلتصق بالحافة
        android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        int marginDp = dp(card, 24);
        lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        lp.horizontalMargin = 0;
        lp.dimAmount = 0;
        dialog.getWindow().setAttributes(lp);
        // set padding on the decor view instead
        ((View) dialog.getWindow().getDecorView()).setPadding(marginDp, 0, marginDp, 0);
    }
}
