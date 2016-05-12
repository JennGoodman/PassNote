package jenngoodman.passnote;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Activity activity;


    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, Activity activity) {

        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.d(WifiServiceDiscovery.TAG, "Action: " + action);

        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) {
                return;
            }
            NetworkInfo networkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                Log.d(WifiServiceDiscovery.TAG,
                        "Connected to p2p network. Requesting network details");
                manager.requestConnectionInfo(channel, (ConnectionInfoListener) activity);
            } else {
                // It's a disconnect
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
                .equals(action)) {
            WifiP2pDevice device = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d(WifiServiceDiscovery.TAG, "Device status - " + device.status);
        }

    }
}
