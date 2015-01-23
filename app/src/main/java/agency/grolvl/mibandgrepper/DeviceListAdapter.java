package agency.grolvl.mibandgrepper;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by tlk on 23/01/15.
 */
public class DeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> devices;

    public DeviceListAdapter()
    {
        this.devices = new ArrayList<>();
    }

    public void add(BluetoothDevice d)
    {
        this.devices.add(d);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return this.devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
