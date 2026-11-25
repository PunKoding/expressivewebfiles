package com.fotospixel.transfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.widget.Toast;

import java.util.Locale;

/**
 * Receives PackageInstaller commit status callbacks for in-app APK updates.
 */
public final class UpdateInstallReceiver extends BroadcastReceiver {

    @Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !AppUpdater.ACTION_INSTALL_STATUS.equals(intent.getAction())) {
            return;
        }
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION: {
                Intent confirmIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (confirmIntent != null) {
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(confirmIntent);
                }
                break;
            }
            case PackageInstaller.STATUS_SUCCESS:
                AppUpdater.cleanOldUpdates(context);
                Toast.makeText(context, "Actualización instalada correctamente", Toast.LENGTH_SHORT).show();
                break;
            case PackageInstaller.STATUS_FAILURE_ABORTED:
                Toast.makeText(context, "Actualización cancelada", Toast.LENGTH_SHORT).show();
                break;
            default: {
                String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                String upper = msg == null ? "" : msg.toUpperCase(Locale.US);
                if (upper.contains("INCOMPATIBLE") || upper.contains("SIGNATURE")) {
                    Toast.makeText(context,
                            "Error de firma: el APK no coincide con la clave instalada",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context,
                            "Error al instalar actualización: " + (msg == null ? ("código " + status) : msg),
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
