package agency.grolvl.mibandgrepper;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private DeviceListAdapter mDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT);
            finish();
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.monSuperSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mListView = (ListView) findViewById(R.id.monSuperListView);
        mDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_layout);
        mListView.setAdapter(mDeviceListAdapter);
        mListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, v.toString());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBt, REQUEST_ENABLE_BT);
        } else {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanDevices(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED)
        {
            finish();
            return;
        }

        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK)
        {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onRefresh() {
        scanDevices(true);
    }

    private void scanDevices(boolean enable)
    {
        if(enable) // do scan
        {
            Log.d(TAG, "start scanning");

            // Start Swipe animation and clear list
            mSwipeRefreshLayout.setRefreshing(true);
            mDeviceListAdapter.clear();

            // Stop scan in SCAN_PERIOD ms
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanDevices(false);
                }
            }, SCAN_PERIOD);

            // Start scan
            mBluetoothLeScanner.startScan(DeviceScanCallback.getInstance(mDeviceListAdapter));
        } else { // stop scan
            Log.d(TAG, "stop scanning");
            // Stop wipe animation
            mSwipeRefreshLayout.setRefreshing(false);

            // Stop scan
            mBluetoothLeScanner.stopScan(DeviceScanCallback.getInstance(mDeviceListAdapter));

        }
    }

}
