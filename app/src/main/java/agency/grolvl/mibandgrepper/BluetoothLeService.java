package agency.grolvl.mibandgrepper;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BluetoothLeService extends Service {

    private final static String TAG = "BluetoothLeService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private final String mFlagWait = "";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "agency.grolvl.mibandgrepper.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "agency.grolvl.mibandgrepper.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_CONNECTING = "agency.grolvl.mibandgrepper.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "agency.grolvl.mibandgrepper.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ENTRY_SERVICE = "SERVICE";
    public final static String ENTRY_CHARACTERISTICS = "CHARACTERISTICS";

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
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.d(TAG, "onServicesDiscovered status : " + status);
            }
        }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                synchronized (mFlagWait)
                {
                    mFlagWait.notifyAll();
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
            Toast.makeText(this, R.string.btadapter_not_found, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void connect(String deviceAddr) {

        BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddr);
        if(mBluetoothDevice == null)
        {
            Toast.makeText(this, R.string.device_not_found, Toast.LENGTH_SHORT).show();
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
            broadcastUpdate(ACTION_GATT_DISCONNECTED);
        } else if(mConnectionState == STATE_CONNECTING)
        {
            Log.d(TAG, "STATE_CONNECTING");
            broadcastUpdate(ACTION_GATT_CONNECTING);
        } else {
            Log.d(TAG, "STATE_CONNECTED");
            broadcastUpdate(ACTION_GATT_CONNECTED);
        }

        this.mConnectionState = mConnectionState;
    }

    private void broadcastUpdate(String action)
    {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * Retrieve services with associated characteristics
     * The return is an ArrayList of HashMap
     * This HashMap contains two entry :
     *  - "service" : the service object (BluetoothGattService)
     *  - "characteristics" : an ArrayList of BluetoothGattCharacteristic (ArrayList<BluetoothGattCharacteristic>)
     * So the return type is : ArrayList<HashMap<String, Object>>
     * It can be seen as a JSON :
     * [
     *      {
     *          "service": BluetoothGattService,
     *          "characteristics": [
     *              BluetoothGattCharacteristic,
     *              BluetoothGattCharacteristic,
     *              BluetoothGattCharacteristic
     *              ]
     *      },
     *      {
     *          "service": BluetoothGattService,
     *          "characteristics": [
     *              BluetoothGattCharacteristic
     *              ]
     *      }
     * ]
     *
     * @return services with associated characteristics
     */
    public ArrayList<HashMap<String, Object>> getGattServices() {
        Log.d(TAG, "getGattServices");
        ArrayList<HashMap<String, Object>> ret = new ArrayList<>();

        List<BluetoothGattService> services = mBluetoothGatt.getServices();
        for(BluetoothGattService service : services)
        {
            Log.d(TAG, "new service");
            HashMap<String, Object> currentService = new HashMap<>();
            currentService.put(ENTRY_SERVICE, service);

            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for(BluetoothGattCharacteristic characteristic : characteristics)
            {
                if(GattUtils.isReadable(characteristic)) { //  retrieve data if its possible
                    synchronized (mFlagWait) {
                        try {
                            mBluetoothGatt.readCharacteristic(characteristic);
                            mFlagWait.wait(); // wait until characteristic is readed
                            Log.d(TAG, "new chara");
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }

            currentService.put(ENTRY_CHARACTERISTICS, characteristics);
            ret.add(currentService);
        }

        Log.d(TAG, "getGattServices finished");
        return ret;
    }

}
