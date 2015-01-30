package agency.grolvl.mibandgrepper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;


public class DeviceActivity extends ActionBarActivity {


    public static final String EXTRAS_DEVICE = "DEVICE";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private List<BluetoothGattService> mGattServices;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static final String TAG = "DeviceActivity";

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState == BluetoothProfile.STATE_CONNECTED)
            {
                setmConnectionState(STATE_CONNECTED);
                mBluetoothGatt.discoverServices();
            } else if(newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                setmConnectionState(STATE_DISCONNECTED);
            } else {
                Log.d(TAG, "zarb state : " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS && mConnectionState == STATE_CONNECTED)
            {
                mGattServices = mBluetoothGatt.getServices();
                for(BluetoothGattService gs : mGattServices)
                {
                    Log.d(TAG, "service : " + GattUtils.lookup(gs));
                }
            } else {
                Log.d(TAG, "onServicesDiscovered status : " + status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        Intent intent = getIntent();
        String device = intent.getStringExtra(EXTRAS_DEVICE);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(device);

        if(mBluetoothDevice == null)
        {
            Toast.makeText(this, R.string.device_not_found, Toast.LENGTH_SHORT);
            finish();
        }

        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);
        setmConnectionState(STATE_CONNECTING);

    }

    private void setmConnectionState(int mConnectionState) {

        if(mConnectionState == STATE_DISCONNECTED)
        {
            Log.d(TAG, "STATE_DISCONNECTED");
        } else if(mConnectionState == STATE_CONNECTING)
        {
            Log.d(TAG, "STATE_CONNECTING");
        } else {
            Log.d(TAG, "STATE_CONNECTED");
        }

        this.mConnectionState = mConnectionState;
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
