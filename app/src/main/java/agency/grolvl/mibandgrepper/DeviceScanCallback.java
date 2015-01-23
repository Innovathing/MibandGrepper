package agency.grolvl.mibandgrepper;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

/**
 * Created by tlk on 23/01/15.
 */
public class DeviceScanCallback extends ScanCallback {

    private static DeviceScanCallback instance = null;
    private DeviceListAdapter deviceListAdapter;

    private DeviceScanCallback(DeviceListAdapter d)
    {
        deviceListAdapter = d;
    }

    static public DeviceScanCallback getInstance(DeviceListAdapter d)
    {
        if(instance == null)
        {
            instance = new DeviceScanCallback(d);
        }
        return instance;
    }

    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        deviceListAdapter.add(result.getDevice()); // add on list when new device
    }

}
