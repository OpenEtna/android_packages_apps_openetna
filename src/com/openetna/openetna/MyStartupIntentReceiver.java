package com.openetna.openetna;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyStartupIntentReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!MainActivity.isGappsInstalled()) {
            Intent nintent = new Intent(context, MainActivity.class);
            nintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(nintent);
        }
    }
}
