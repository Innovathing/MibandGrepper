package agency.grolvl.mibandgrepper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DeviceActivity extends ActionBarActivity {

    public static final String EXTRAS_DEVICE = "DEVICE";

    private BluetoothLeService mBluetoothLeService;
    private String mBluetoothDeviceAddress;

    private ExpandableListView mExpandableListView;
    private SimpleExpandableListAdapter mSimpleExpandableListAdapter;
    private ArrayList<HashMap<String, String>> gattServicesData = new ArrayList<>();
    private ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();

    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";
    private static final String LIST_RAW = "RAW";

    private static final String TAG = "DeviceActivity";

    // Manage Service lifecycle
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if(!mBluetoothLeService.initialize())
            {
                Toast.makeText(DeviceActivity.this, R.string.device_not_found, Toast.LENGTH_SHORT);
                finish();
            }
            mBluetoothLeService.connect(mBluetoothDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "receive action : " +action);
            if(action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
                displayGattServices(mBluetoothLeService.getGattServices());
            }
        }
    };

    private void displayGattServices(ArrayList<HashMap<String, Object>> gattServices) {
        for(HashMap<String, Object> serviceMap : gattServices)
        {
            HashMap<String, String> currentService = new HashMap<>();
            ArrayList<HashMap<String, String>> currentServiceCharacteristics = new ArrayList<>();
            BluetoothGattService service = (BluetoothGattService) serviceMap.get(BluetoothLeService.ENTRY_SERVICE);

            currentService.put(LIST_NAME, GattUtils.lookup(service));
            currentService.put(LIST_UUID, service.getUuid().toString());
            gattServicesData.add(currentService);

            List<BluetoothGattCharacteristic> gattCharacteristics = (List<BluetoothGattCharacteristic>) serviceMap.get(BluetoothLeService.ENTRY_CHARACTERISTICS);
            for(BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
            {
                HashMap<String, String> currentCharacteristic = new HashMap<>();
                currentCharacteristic.put(LIST_NAME, GattUtils.lookup(gattCharacteristic));
                currentCharacteristic.put(LIST_UUID, gattCharacteristic.getUuid().toString());
                if(GattUtils.isReadable(gattCharacteristic))
                {
                    currentCharacteristic.put(LIST_RAW, gattCharacteristic.getStringValue(0));
                } else {
                    currentCharacteristic.put(LIST_RAW, "");
                }
                currentServiceCharacteristics.add(currentCharacteristic);
            }

            gattCharacteristicData.add(currentServiceCharacteristics);
        }
        mSimpleExpandableListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        mExpandableListView = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mSimpleExpandableListAdapter = new SimpleExpandableListAdapter(
                this,
                gattServicesData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] {android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                R.layout.device_characteristic,
                new String[] {LIST_NAME, LIST_UUID, LIST_RAW},
                new int[] {R.id.chara_name, R.id.chara_uuid, R.id.chara_raw}
        );
        mExpandableListView.setAdapter(mSimpleExpandableListAdapter);

        Intent intent = getIntent();
        mBluetoothDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE);

        Intent bluetoothLeServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(bluetoothLeServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if(mBluetoothLeService != null)
        {
            mBluetoothLeService.connect(mBluetoothDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSimpleExpandableListAdapter.notifyDataSetInvalidated();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
