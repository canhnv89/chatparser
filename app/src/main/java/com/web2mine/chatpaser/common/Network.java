package com.web2mine.chatpaser.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.web2mine.chatpaser.R;

/**
 * Network utility class
 */
public class Network {

    interface NetworkState {
        int NOT_AVAILABLE = 0;
        int AVAILABLE = 1;
    }

    public static int mCurrentNetworkState = NetworkState.AVAILABLE;

    /**
     * Check if network connection is available or not
     *
     * @param context
     * @return true if network is available
     */
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * Show toast message network status is changed
     *
     * @param context Application context
     */
    public static void showNotificationIfNoNetwork(Context context) {
        boolean available = isNetworkAvailable(context);

        if (!available && mCurrentNetworkState == NetworkState.AVAILABLE) {
            mCurrentNetworkState = NetworkState.NOT_AVAILABLE;
            Toast.makeText(context, R.string.toast_no_network, Toast.LENGTH_SHORT).show();
        } else if (available && mCurrentNetworkState == NetworkState.NOT_AVAILABLE) {
            mCurrentNetworkState = NetworkState.AVAILABLE;
            Toast.makeText(context, R.string.toast_network_available, Toast.LENGTH_SHORT).show();
        }
    }

    private static final BroadcastReceiver mNetworkChangeReceiver = new NetworkChangeReceiver();

    public static void registerNetworkChangeReceiver(Context context)
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        context.registerReceiver(mNetworkChangeReceiver, filter);
    }

    public static void unregisterNetworkChangeReceiver(Context context)
    {
        context.unregisterReceiver(mNetworkChangeReceiver);
    }

    /**
     * Receiver to detect network change
     */
    public static class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            showNotificationIfNoNetwork(context);
        }
    }
}
