package agency.grolvl.mibandgrepper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by tlk on 30/01/15.
 */
public class GattUtils {

    private static HashMap<String, String> attributes = new HashMap<>();

    private static String UUID_BASE = "-0000-1000-8000-00805f9b34fb";

    /*
    Informations from https://developer.bluetooth.org/gatt/services/Pages/ServicesHome.aspx
     */
    static {
        attributes.put("00001800" + UUID_BASE, "Generic access");
        attributes.put("00001801" + UUID_BASE, "Generic attribute");
        attributes.put("00001802" + UUID_BASE, "Immediate alert");
        attributes.put("0000fee0" + UUID_BASE, "Mili Service");

        attributes.put("0000ff01" + UUID_BASE, "Device infos");
        attributes.put("0000ff02" + UUID_BASE, "Device name");
        attributes.put("0000ff03" + UUID_BASE, "Notifications");
        attributes.put("0000ff04" + UUID_BASE, "User informations");
        attributes.put("0000ff05" + UUID_BASE, "Control point");
        attributes.put("0000ff06" + UUID_BASE, "Realtime steps");
        attributes.put("0000ff07" + UUID_BASE, "Activity data");
        attributes.put("0000ff08" + UUID_BASE, "Firmware data");
        attributes.put("0000ff09" + UUID_BASE, "Low energy params");
        attributes.put("0000ff0a" + UUID_BASE, "Datetime");
        attributes.put("0000ff0b" + UUID_BASE, "Statistics");
        attributes.put("0000ff0c" + UUID_BASE, "Battery");
        attributes.put("0000ff0d" + UUID_BASE, "Test ?!?");
        attributes.put("0000ff0e" + UUID_BASE, "Sensor data");
        attributes.put("0000ff0f" + UUID_BASE, "Pair");
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

    public static String lookup(BluetoothGattCharacteristic bluetoothGattCharacteristic)
    {
        return lookup(bluetoothGattCharacteristic.getUuid());
    }

}