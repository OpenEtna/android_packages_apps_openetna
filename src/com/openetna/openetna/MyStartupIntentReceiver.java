package com.openetna.openetna;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyStartupIntentReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!MainActivity.isGappsInstalled()) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction("com.openetna.openetna.MainActivity");
            context.startActivity(serviceIntent);
        }
    }
}
