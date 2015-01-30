package agency.grolvl.mibandgrepper;

import android.bluetooth.BluetoothGattService;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by tlk on 30/01/15.
 */
public class GattUtils {

    private static HashMap<String, String> attributes = new HashMap<>();

    private static String UUID_BASE = "-0000-1000-8000-00805f9b34fb";

    static {
        attributes.put("00001800" + UUID_BASE, "Zboub");
    }

    public static String lookup(UUID uuid)
    {
        String name = attributes.get(uuid.toString());
        return name == null ? uuid.toString() : name;
    }

    public static String lookup(BluetoothGattService bluetoothGattService)
    {
        return lookup(bluetoothGattService.getUuid());
    }

}