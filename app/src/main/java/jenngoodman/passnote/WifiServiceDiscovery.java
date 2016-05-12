package jenngoodman.passnote;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jenngoodman.passnote.MessageHomeFragment.MessageTarget;
import jenngoodman.passnote.WiFiDirectServicesList.WiFiDevicesAdapter;

public class WifiServiceDiscovery extends ActionBarActivity implements Handler.Callback, MessageTarget, WifiP2pManager.ConnectionInfoListener, WiFiDirectServicesList.DeviceClickListener {

    public static final String TAG = "wifidirect";
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "Friend";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    static final int SERVER_PORT = 4545;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager manager;
    private Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    private Handler handler = new Handler(this);
    private MessageHomeFragment chatFragment;
    private WiFiDirectServicesList servicesList;

    private TextView statusTxt;

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    startActivity(new Intent(this, SettingChange.class));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_service);
        statusTxt = (TextView) findViewById(R.id.status_changing);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        startRegistrationAndDiscovery();

        servicesList = new WiFiDirectServicesList();
        getFragmentManager().beginTransaction().add(R.id.containers_root, servicesList, "services").commit();
    }

    protected void onRestart() {
        Fragment fragment = getFragmentManager().findFragmentByTag("services");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
        super.onRestart();
    }

    protected void onStop() {
        if (manager != null && channel != null) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason:" + reasonCode);
                }
            });
        }
        super.onStop();
    }

    private void startRegistrationAndDiscovery() {

        Map<String, String> record = new HashMap<>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                appendStatus("Added Local Service");
            }

            @Override
            public void onFailure(int reason) {
                appendStatus("Failed to add a service");
            }
        });
        discoverService();
    }

    private void discoverService() {

        manager.setDnsSdResponseListeners(channel, new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {

                if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

                    WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager().findFragmentByTag("services");
                    if (fragment != null) {

                        WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment.getListAdapter());

                        WiFiP2pService service = new WiFiP2pService();
                        service.device = srcDevice;
                        service.instanceName = instanceName;
                        service.serviceRegistrationType = registrationType;
                        adapter.add(service);
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "HelloServiceAvailable" + instanceName);
                    }
                }
            }

        }, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                Log.d(TAG, srcDevice.deviceName + " is" + txtRecordMap.get(TXTRECORD_PROP_AVAILABLE));
            }

        });

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        manager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Added service discovery request");
            }

            @Override
            public void onFailure(int reason) {
                appendStatus("Failed added service discovery request");
            }
        });

        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Service discovery initiated");
            }

            @Override
            public void onFailure(int reason) {
                appendStatus("Service discovery failed");
            }
        });
    }

    public void connectP2p(WiFiP2pService service) {

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null) {

            manager.removeServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                }
            });
            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    appendStatus("Connecting to service");
                }

                @Override
                public void onFailure(int reason) {
                    appendStatus("Failed connecting to service");
                }
            });
        }
    }

    public boolean handleMessage(Message msg) {

        switch (msg.what) {

            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;

                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                (chatFragment).pushMessage("Peer: " + readMessage);
                break;
            case MY_HANDLE:
                Object obj = msg.obj;
                (chatFragment).setChatManager((MessageManager) obj);

        }
        return true;
    }

    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        Thread handler;
        if (p2pInfo.isGroupOwner) {

            Log.d(TAG, "Connected as group owner");
            try {
                handler = new GroupOwnerSocketHandler(this.getHandler());
                handler.start();
            } catch (IOException e) {
                Log.d(TAG, "Failed to create a server thread - " + e.getMessage());
                return;
            }
        } else {
            Log.d(TAG, "Connected as peer");
            handler = new ClientSocketHandler(this.getHandler(), p2pInfo.groupOwnerAddress);
            handler.start();
        }
        chatFragment = new MessageHomeFragment();
        getFragmentManager().beginTransaction().replace(R.id.containers_root, chatFragment).commit();
        statusTxt.setVisibility(View.GONE);
    }

    public void appendStatus(String status) {
        String[] current = (statusTxt.getText().toString()).split("\n");
        statusTxt.setText("");
        if (current.length == 1) {
            statusTxt.append(current[0] + "\n");
        }
        if (current.length > 1) {
            statusTxt.append(current[current.length - 2] + "\n");
            statusTxt.append(current[current.length - 1] + "\n");
        }
        statusTxt.append(status);
    }
}