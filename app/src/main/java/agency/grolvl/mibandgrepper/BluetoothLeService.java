package agency.grolvl.mibandgrepper;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class BluetoothLeService extends Service {

    private final static String TAG = "BluetoothLeService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> mGattServices;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "agency.grolvl.mibandgrepper.ACTION_GATT_CONNECTED";

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        BluetoothLeService getService()
        {
            return BluetoothLeService.this;
        }
    }

        private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState == BluetoothProfile.STATE_CONNECTED)
            {
                setSate(STATE_CONNECTED);
                broadcastUpdate(ACTION_GATT_CONNECTED);
                mBluetoothGatt.discoverServices();
            } else if(newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                setSate(STATE_DISCONNECTED);
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

//                    HashMap<String, String> currentService = new HashMap<>();
//                    currentService.put(LIST_NAME, GattUtils.lookup(gs));
//                    currentService.put(LIST_UUID, gs.getUuid().toString());
//                    gattServicesData.add(currentService);
                }
//                mSimpleExpandableListAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "onServicesDiscovered status : " + status);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        if(mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null)
            {
                Toast.makeText(this, R.string.btmanager_not_found, Toast.LENGTH_SHORT);
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(this, R.string.btadapter_not_found, Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    public void connect(String deviceAddr) {

        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddr);
        if(mBluetoothDevice == null)
        {
            Toast.makeText(this, R.string.device_not_found, Toast.LENGTH_SHORT);
            return;
        }

        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);
        setSate(STATE_CONNECTING);
    }

    public void close()
    {
        if(mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private void setSate(int mConnectionState) {

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

    private void broadcastUpdate(String action)
    {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

}
