package com.javetest.helio;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.security.Permission;
import java.util.List;
import java.util.Set;

/**
 * class can perform different sanity checks, for example checking if any connection method like wifi or bluetooth is enabled
 */
public class SanityChecks {

    /**
     * function checks the availability of wifi, bluetooth and mobile data
     * @return false if any connection method is available, true otherwise
     */
    public boolean wifi(Context context, Activity activity){
        try {
            //isWifiWorking(context);
            isCellularWorking(context);
        }/*catch(PermissionException e)
        {
            activity.runOnUiThread(() ->Toast.makeText(activity, "Permission must be granted for wifi . . . etc", Toast.LENGTH_SHORT).show());
            return false;
        }*/catch(UnexpectedConnectivityExceptions e)
        {
            activity.runOnUiThread(() ->Toast.makeText(activity, "Unexpected Connectivity detected. The phone might be compromised", Toast.LENGTH_LONG).show()); //TODO diese nachricht kann verunsichern
            return false;
        }
        return true;
    }

    /**
     * checks if the phone is connected to cellular
     * @param context context of the application
     * @throws UnexpectedConnectivityExceptions if cellular is connected
     */
    private void isCellularWorking(Context context) throws UnexpectedConnectivityExceptions
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //check if the phone is connected to cellular
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    throw new UnexpectedConnectivityExceptions("connectivity CELLULAR unexpectedly works");
                }
            } else {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected()) {
                    throw new UnexpectedConnectivityExceptions("connectivity CELLULAR unexpectedly works");
                }
            }
        }
    }

    /**
     * @return true if service is disabled, false otherwise
     */
    private void isWifiWorking(Context context) throws PermissionException, UnexpectedConnectivityExceptions
    {

        int PERMISSION_REQUEST_CODE = 1;
        String permission = "android.permission.ACCESS_WIFI_STATE";

        //first check if the application has access permission to the wifi
        this.checkPermission(permission, PERMISSION_REQUEST_CODE, context);

        //check if wifi is enabled
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        //check if the phone is connected to a wifi network
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    throw new UnexpectedConnectivityExceptions("connectivity WIFI unexpectedly works");
                }
            } else {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                    throw new UnexpectedConnectivityExceptions("connectivity WIFI unexpectedly works");
                }
            }
        }

        //check if the phone can find any wifi networks nearby
        if (wifiManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                wifiManager.startScan();
                List<ScanResult> scanResults = wifiManager.getScanResults();
                if (scanResults != null && !scanResults.isEmpty())
                {
                    throw new UnexpectedConnectivityExceptions("connectivity WIFI unexpectedly works");
                }
            }

        }
    }

    /**
     * @return true if service is disabled, false otherwise
     */
    private Activity getActivity(Context context)
    {
        if (context == null)
        {
            return null;
        }
        else if (context instanceof ContextWrapper)
        {
            if (context instanceof Activity)
            {
                return (Activity) context;
            }
            else
            {
                return getActivity(((ContextWrapper) context).getBaseContext());
            }
        }
        return null;
    }

    /**
     *
     * @param permission string that specifies the permission to check
     */
    private void checkPermission(String permission, int PERMISSION_REQUEST_CODE, Context context) throws PermissionException {
        //check if wifi permission is Granted and request it otherwise
        boolean permissionGranted = context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted)
        {
            ActivityCompat.requestPermissions(getActivity(context), new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
        //check if the permission is granted now
        if (!(context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED))
        {
            throw new PermissionException("permission " + permission + " was not granted");
        }
    }
}
