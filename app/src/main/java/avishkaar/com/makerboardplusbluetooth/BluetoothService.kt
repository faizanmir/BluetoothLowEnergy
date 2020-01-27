package avishkaar.com.makerboardplusbluetooth

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*

class BluetoothService :
    Service() {
    var mIBinder = LocalBinder()
    var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    var mListener: OnBluetoothEventListener? = null
    var deviceAddress: String? = null
    var bluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    private var receivingBluetoothCharacteristics: BluetoothGattCharacteristic? = null
    private lateinit var uuidForReceivingCharacteristics:UUID ;
    private lateinit var uuidService :UUID
    private lateinit var uuidCharacteristic:UUID

    private var gattCallback: BluetoothGattCallback? = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            mListener?.onRead(gatt,characteristic,status)

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            mListener?.onWrite(gatt,characteristic,status)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            mListener?.onServiceDiscovered(gatt,status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            mListener?.onCharacteristicsChange(gatt,characteristic)

        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                print("Attempting service discovery...")
                bluetoothGatt!!.discoverServices()
               mListener?.onConnectionStatusChange(gatt,status,newState)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mListener?.onDisconnected()
            }
        }
    }

    interface OnBluetoothEventListener {
        fun onRead(gatt: BluetoothGatt?,characteristic: BluetoothGattCharacteristic?,status:Int)
        fun onWrite(gatt: BluetoothGatt?,characteristic: BluetoothGattCharacteristic?,status:Int)
        fun onCharacteristicsChange( gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?)
        fun onConnectionStatusChange(gatt: BluetoothGatt?, status: Int, newState: Int)
        fun onServiceDiscovered(gatt: BluetoothGatt?,status: Int)
        fun onErrorEncountered()
        fun onDisconnected()
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService {
            return this@BluetoothService
        }
    }

    companion object {
         val TAG  = this::class.java.canonicalName
    }

    fun passReferencesToService(address:String?,uuidForWriteCharacteristics:String?
                                ,uuidForNotification:String?,uuidForService:String?)
    {
        try {
            this.uuidCharacteristic = UUID.fromString(uuidForWriteCharacteristics)
            this.uuidForReceivingCharacteristics = UUID.fromString(uuidForNotification)
            this.uuidService = UUID.fromString(uuidForService)
            this.deviceAddress = address
            connect(address)
        } catch (e:Exception)
        {
            mListener?.onErrorEncountered()
            e.printStackTrace()
        }
    }


    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }


    override fun onBind(intent: Intent): IBinder {
        return mIBinder
    }


    private fun connect(address: String?) {
        bluetoothGatt =
            bluetoothAdapter?.getRemoteDevice(address)?.connectGatt(this, false, gattCallback)

    }


    fun getService() {
        val serviceList = bluetoothGatt!!.services
        for (bluetoothGattService in serviceList) {
            for (c in bluetoothGattService.characteristics) {
                if (bluetoothGattService.uuid == uuidService) {
                    val characteristicList = bluetoothGattService.characteristics
                    for (characteristic in characteristicList) {
                        if (characteristic.uuid == uuidCharacteristic) {
                            bluetoothGattCharacteristic = characteristic
                        } else if (characteristic.uuid == uuidForReceivingCharacteristics) {
                            receivingBluetoothCharacteristics = characteristic
                            registerForNotification(characteristic)
                        }
                    }

                }
            }
        }
        if(bluetoothGattCharacteristic == null && receivingBluetoothCharacteristics == null)
        {
            mListener?.onErrorEncountered()
        }

    }

    private fun registerForNotification(characteristic: BluetoothGattCharacteristic?)
    {

        Log.i(
            TAG, "\nUUID: " + characteristic!!.uuid + "" +
                    "\nBluetooth characteristics write" + bluetoothGatt!!.writeCharacteristic(characteristic) +
                    "\nBluetooth characteristic reads " + bluetoothGatt!!.readCharacteristic(characteristic) +
                    "\nProperties= " + characteristic.properties +
                    "\nPermissions = " + characteristic.permissions +
                    "\nvalue" + Arrays.toString(characteristic.value) +
                    "\nWrite Type" + characteristic.writeType
        )
        bluetoothGatt!!.setCharacteristicNotification(receivingBluetoothCharacteristics, true)
    }

    fun sendToDevice(str :String)
    {
        if(bluetoothGattCharacteristic != null) {
            bluetoothGattCharacteristic?.setValue(str)
            bluetoothGatt?.writeCharacteristic(bluetoothGattCharacteristic)
        }
    }



    fun registerListener( listener: OnBluetoothEventListener?)
    {
        this.mListener = listener
    }


}
