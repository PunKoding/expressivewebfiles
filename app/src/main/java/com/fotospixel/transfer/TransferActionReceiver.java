package com.fotospixel.transfer;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Explicit BroadcastReceiver for notification action buttons so stopping the
 * server or cancelling transfers works reliably even under Android 8/9
 * background execution limits and OEM background managers.
 */
public final class TransferActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TransferService.ACTION_STOP.equals(action)) {
            TransferService.stopServiceCompletely(context);
        } else if (TransferService.ACTION_CANCEL_TRANSFERS.equals(action)) {
            TransferStats.getInstance().cancelAllTransfers();
            TransferService.notifyStateChangedIfRunning();
        } else if (TransferService.ACTION_CANCEL_FILE_OPS.equals(action)) {
            FileOpMonitor.getInstance().cancelAll();
            NotificationManager nm = (NotificationManager) context.getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(TransferService.NOTIF_FILE_OP_ID);
            }
            TransferService.notifyStateChangedIfRunning();
        }
    }
}
