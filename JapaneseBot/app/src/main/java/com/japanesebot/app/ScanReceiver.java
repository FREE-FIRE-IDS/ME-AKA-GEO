package com.japanesebot.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScanReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Trigger scan from notification button
        QuotexAccessibilityService service = QuotexAccessibilityService.getInstance();
        if (service != null) {
            service.performScan();
        }
    }
}
