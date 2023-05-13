package com.javetest.helio;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.os.Build;

import androidx.core.content.ContextCompat;

/**
 * class can perform different sanity checks, for example checking if any connection method like wifi or bluetooth is enabled
 */
public class SanityChecks {

    /**
     * function checks the availability of wifi, bluetooth and mobile data
     * @return false if any connection method is available, true otherwise
     */
    public static boolean connectivitiesDisabled(Context context)
    {
        boolean a = serviceIsDisabled(context);

        return false;
    }

    /**
     * @return true if service is disabled, false otherwise
     */
    private static boolean serviceIsDisabled(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // Check if the connected network is a mobile data connection, if so, a service connection is enabled, so return false
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) return false;
            }
        }
        //otherwise, return true
        return true;
    }

    /**
     * @return true if service is disabled, false otherwise
     */
    private static boolean bluetoothIsDisabled(Context context)
    {
        int bluetoothPermissionResult = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH);
        int bluetoothAdminPermissionResult = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN);

        return bluetoothPermissionResult == PackageManager.PERMISSION_GRANTED && bluetoothAdminPermissionResult == PackageManager.PERMISSION_GRANTED;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return !(bluetoothAdapter != null & bluetoothAdapter.isEnabled());
        
    }

    /**
     * @return true if service is disabled, false otherwise
     */
    private static boolean nfcIsDisabled(Context context)
    {
;

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        return !(nfcAdapter != null && nfcAdapter.isEnabled());
    }

}
