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
import android.Manifest;

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
    public boolean performChecks(Context context, Activity activity){
        try {
            isWifiWorking(context, activity);
            isCellularWorking(context, activity);
        }catch(PermissionException e)
        {
            activity.runOnUiThread(() ->Toast.makeText(activity, "Permission must be granted for wifi and cellular connection and camera. Check the Settings and Enable them.", Toast.LENGTH_SHORT).show());
            return false;
        }catch(UnexpectedConnectivityExceptions e)
        {
            activity.runOnUiThread(() ->Toast.makeText(activity, "Unexpected Connectivity detected. The phone might be compromised. For security reasons, all data will be erased.", Toast.LENGTH_LONG).show()); //TODO diese nachricht kann verunsichern
            TotalAnnilihator totalAnnilihator = new TotalAnnilihator();
            totalAnnilihator.clearAll(context);
            return false;
        }
        return true;
    }


    public void checkAllPermissions(Context context, Activity activity) throws PermissionException
    {
        //TODO DIE RAUSKOMMENTIERTEN LINES SIND NUR ZUM DEBUGGEN RAUSKOMMENTIERT!!!!
        //this.checkPermission(context, activity, "cellular");
        //this.checkPermission(context, activity, "wifi");
        this.checkPermission(context, activity, "camera");
    }

    /**
     * funciton checks for required permissions and blocks the procedure of the code if the user doesnt grant necessary permissions
     */
    public void checkPermission(Context context, Activity activity, String permission) throws PermissionException {
        int PERMISSION_REQUEST_CODE = 1;

        String permissionString;

        boolean permissionGranted;
        if (permission.equals("wifi"))
        {
            permissionString = Manifest.permission.ACCESS_WIFI_STATE;
        }
        else if (permission.equals("cellular"))
        {
            permissionString = Manifest.permission.ACCESS_NETWORK_STATE;
        }
        else if(permission.equals("camera"))
        {
            permissionString = Manifest.permission.CAMERA;
        }
        else
        {throw new IllegalArgumentException("Argument 'permission' must be either 'cellular', 'wifi' or 'camera'");}

        permissionGranted = ContextCompat.checkSelfPermission(activity, permissionString) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) throw new PermissionException("permission not granted");
    }

    /**
     * checks if the phone is connected to cellular
     * @param context context of the application
     * @throws UnexpectedConnectivityExceptions if cellular is connected
     */
    private void isCellularWorking(Context context, Activity activity) throws UnexpectedConnectivityExceptions, PermissionException {
        this.checkPermission(context, activity, "cellular");

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
    private void isWifiWorking(Context context, Activity activity) throws PermissionException, UnexpectedConnectivityExceptions
    {
        //first check if the application has access permission to the wifi
        this.checkPermission(context, activity, "wifi");

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

}
