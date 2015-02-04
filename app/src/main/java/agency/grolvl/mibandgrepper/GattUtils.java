package agency.grolvl.mibandgrepper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by tlk on 30/01/15.
 */
public class GattUtils {

    private final static String TAG = "GattUtils";

    private static HashMap<UUID, String> attributes = new HashMap<>();

    private static String UUID_BASE = "-0000-1000-8000-00805f9b34fb";
    public static UUID SERVICE_GENERIC_ACCESS = UUID.fromString("00001800" + UUID_BASE);
    public static UUID SERVICE_GENERIC_ATTRIBUTE = UUID.fromString("00001801" + UUID_BASE);
    public static UUID SERVICE_IMMEDIATE_ALERT = UUID.fromString("00001802" + UUID_BASE);
    public static UUID SERVICE_MILI = UUID.fromString("0000fee0" + UUID_BASE);

    public static UUID CHARACTERISTIC_MILI_DEVICE_INFOS = UUID.fromString("0000ff01" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_DEVICE_NAME = UUID.fromString("0000ff02" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_NOTIFICATIONS = UUID.fromString("0000ff03" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_USER_INFOS = UUID.fromString("0000ff04" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_CONTROL_POINT = UUID.fromString("0000ff05" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_REALTIME_STEPS = UUID.fromString("0000ff06" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_ACTIVITY_DATA = UUID.fromString("0000ff07" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_FIRMWARE_DATA = UUID.fromString("0000ff08" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_LE_PARAMS = UUID.fromString("0000ff09" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_DATETIME = UUID.fromString("0000ff0a" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_STATS = UUID.fromString("0000ff0b" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_BATTERY = UUID.fromString("0000ff0c" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_TEST = UUID.fromString("0000ff0d" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_SENSOR_DATA = UUID.fromString("0000ff0e" + UUID_BASE);
    public static UUID CHARACTERISTIC_MILI_PAIR = UUID.fromString("0000ff0f" + UUID_BASE);
    /*
    Informations from https://developer.bluetooth.org/gatt/services/Pages/ServicesHome.aspx
    & reverse of miband app
     */
    static {
        attributes.put(SERVICE_GENERIC_ACCESS, "Generic access");
        attributes.put(SERVICE_GENERIC_ATTRIBUTE, "Generic attribute");
        attributes.put(SERVICE_IMMEDIATE_ALERT, "Immediate alert");
        attributes.put(SERVICE_MILI, "Mili Service");

        attributes.put(CHARACTERISTIC_MILI_DEVICE_INFOS, "Device infos");
        attributes.put(CHARACTERISTIC_MILI_DEVICE_NAME, "Device name");
        attributes.put(CHARACTERISTIC_MILI_NOTIFICATIONS, "Notifications");
        attributes.put(CHARACTERISTIC_MILI_USER_INFOS, "User informations");
        attributes.put(CHARACTERISTIC_MILI_CONTROL_POINT, "Control point");
        attributes.put(CHARACTERISTIC_MILI_REALTIME_STEPS, "Realtime steps");
        attributes.put(CHARACTERISTIC_MILI_ACTIVITY_DATA, "Activity data");
        attributes.put(CHARACTERISTIC_MILI_FIRMWARE_DATA, "Firmware data");
        attributes.put(CHARACTERISTIC_MILI_LE_PARAMS, "Low energy params");
        attributes.put(CHARACTERISTIC_MILI_DATETIME, "Datetime");
        attributes.put(CHARACTERISTIC_MILI_STATS, "Statistics");
        attributes.put(CHARACTERISTIC_MILI_BATTERY, "Battery");
        attributes.put(CHARACTERISTIC_MILI_TEST, "Test ?!?");
        attributes.put(CHARACTERISTIC_MILI_SENSOR_DATA, "Sensor data");
        attributes.put(CHARACTERISTIC_MILI_PAIR, "Pair");
    }

    public static String lookup(UUID uuid)
    {
        String name = attributes.get(uuid);
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

    public static boolean isReadable(BluetoothGattCharacteristic bluetoothGattCharacteristic)
    {
        int prop = bluetoothGattCharacteristic.getProperties();
        if((prop & BluetoothGattCharacteristic.PROPERTY_READ) != 0)
        {
            return true;
        }

        return false;
    }

    public static boolean isWritable(BluetoothGattCharacteristic bluetoothGattCharacteristic)
    {
        int prop = bluetoothGattCharacteristic.getProperties();
        if((prop & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
           (prop & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0)
        {
            return true;
        }

        return false;
    }

    public static String hexdump(BluetoothGattCharacteristic bluetoothGattCharacteristic)
    {
        StringWriter hexdump = new StringWriter();
        byte[] data = bluetoothGattCharacteristic.getValue();

        for(int i = 0; i < data.length; i++)
        {
            hexdump.append(String.format("%02x ", data[i]));
            if(i%7 == 0 && i != 0)
            {
                hexdump.append("\n");
            }
        }

        return hexdump.toString();
    }

}