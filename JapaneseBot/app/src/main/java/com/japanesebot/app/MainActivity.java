package com.japanesebot.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.content.SharedPreferences;

public class MainActivity extends Activity {

    TextView tvStatus, tvSignal, tvPair, tvScore;
    Button btnEnable, btnScan;
    Switch swAutoScan;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("japanesebot", MODE_PRIVATE);
        buildUI();
    }

    void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 60, 40, 40);
        root.setBackgroundColor(Color.parseColor("#0a0a0a"));

        // Header
        TextView header = new TextView(this);
        header.setText("JAPANESE BOT");
        header.setTextSize(28);
        header.setTextColor(Color.parseColor("#00ff88"));
        header.setGravity(Gravity.CENTER);
        header.setLetterSpacing(0.2f);
        header.setPadding(0, 0, 0, 4);
        root.addView(header);

        TextView sub = new TextView(this);
        sub.setText("FX + OTC Binary Signal Tool");
        sub.setTextSize(12);
        sub.setTextColor(Color.parseColor("#666666"));
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 40);
        root.addView(sub);

        // Status Card
        LinearLayout statusCard = makeCard("#111111");
        tvStatus = new TextView(this);
        tvStatus.setText("● Accessibility Service: Checking...");
        tvStatus.setTextColor(Color.parseColor("#ffaa00"));
        tvStatus.setTextSize(13);
        statusCard.addView(tvStatus);
        root.addView(statusCard);
        addSpacing(root, 16);

        // Enable Button
        btnEnable = new Button(this);
        btnEnable.setText("Enable Accessibility Service");
        btnEnable.setBackgroundColor(Color.parseColor("#1a1a1a"));
        btnEnable.setTextColor(Color.parseColor("#00ff88"));
        btnEnable.setPadding(20, 20, 20, 20);
        btnEnable.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        root.addView(btnEnable);
        addSpacing(root, 24);

        // Signal Display Card
        LinearLayout signalCard = makeCard("#111122");
        TextView sigLabel = new TextView(this);
        sigLabel.setText("SIGNAL");
        sigLabel.setTextColor(Color.parseColor("#555555"));
        sigLabel.setTextSize(11);
        sigLabel.setLetterSpacing(0.15f);
        signalCard.addView(sigLabel);

        tvSignal = new TextView(this);
        tvSignal.setText("—");
        tvSignal.setTextSize(52);
        tvSignal.setTextColor(Color.parseColor("#ffffff"));
        tvSignal.setGravity(Gravity.CENTER);
        signalCard.addView(tvSignal);

        tvPair = new TextView(this);
        tvPair.setText("Quotex open karein — pair select karein — SCAN dabayein");
        tvPair.setTextColor(Color.parseColor("#666666"));
        tvPair.setTextSize(12);
        tvPair.setGravity(Gravity.CENTER);
        signalCard.addView(tvPair);

        tvScore = new TextView(this);
        tvScore.setText("");
        tvScore.setTextColor(Color.parseColor("#888888"));
        tvScore.setTextSize(12);
        tvScore.setGravity(Gravity.CENTER);
        tvScore.setPadding(0, 8, 0, 0);
        signalCard.addView(tvScore);

        root.addView(signalCard);
        addSpacing(root, 16);

        // Auto Scan Toggle
        LinearLayout autoRow = new LinearLayout(this);
        autoRow.setOrientation(LinearLayout.HORIZONTAL);
        autoRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView autoLabel = new TextView(this);
        autoLabel.setText("Auto Scan (har 30 sec)");
        autoLabel.setTextColor(Color.parseColor("#aaaaaa"));
        autoLabel.setTextSize(14);
        autoLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        swAutoScan = new Switch(this);
        swAutoScan.setChecked(prefs.getBoolean("auto_scan", false));
        swAutoScan.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean("auto_scan", checked).apply();
            QuotexAccessibilityService.autoScan = checked;
        });
        autoRow.addView(autoLabel);
        autoRow.addView(swAutoScan);
        root.addView(autoRow);
        addSpacing(root, 16);

        // Scan Button
        btnScan = new Button(this);
        btnScan.setText("SCAN NOW");
        btnScan.setTextSize(18);
        btnScan.setBackgroundColor(Color.parseColor("#00ff88"));
        btnScan.setTextColor(Color.parseColor("#000000"));
        btnScan.setPadding(20, 30, 20, 30);
        btnScan.setOnClickListener(v -> {
            QuotexAccessibilityService.triggerScan(this);
            tvPair.setText("Scanning...");
            tvSignal.setText("...");
        });
        root.addView(btnScan);
        addSpacing(root, 12);

        // Disclaimer
        TextView disc = new TextView(this);
        disc.setText("Not financial advice. Educational use only.");
        disc.setTextColor(Color.parseColor("#333333"));
        disc.setTextSize(11);
        disc.setGravity(Gravity.CENTER);
        root.addView(disc);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    LinearLayout makeCard(String bg) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor(bg));
        card.setPadding(30, 24, 30, 24);
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        card.setLayoutParams(lp);
        return card;
    }

    void addSpacing(LinearLayout parent, int dp) {
        View v = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp);
        v.setLayoutParams(lp);
        parent.addView(v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
        // Listen for signal updates
        QuotexAccessibilityService.signalListener = (signal, pair, score) -> {
            runOnUiThread(() -> {
                if (signal.equals("UP")) {
                    tvSignal.setText("↑ UP");
                    tvSignal.setTextColor(Color.parseColor("#00ff88"));
                } else {
                    tvSignal.setText("↓ DOWN");
                    tvSignal.setTextColor(Color.parseColor("#ff4444"));
                }
                tvPair.setText(pair);
                tvScore.setText("Match Score: " + score + "%");
            });
        };
    }

    void updateServiceStatus() {
        if (isAccessibilityEnabled()) {
            tvStatus.setText("● Accessibility Service: Active");
            tvStatus.setTextColor(Color.parseColor("#00ff88"));
        } else {
            tvStatus.setText("● Accessibility Service: OFF — Enable karein");
            tvStatus.setTextColor(Color.parseColor("#ff4444"));
        }
    }

    boolean isAccessibilityEnabled() {
        String prefString = Settings.Secure.getString(
            getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString != null && prefString.contains(getPackageName());
    }
}
