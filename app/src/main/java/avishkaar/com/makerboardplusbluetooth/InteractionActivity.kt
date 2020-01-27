package avishkaar.com.makerboardplusbluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_interaction.*
import java.util.*

class InteractionActivity : AppCompatActivity(), ServiceConnection,
    BluetoothService.OnBluetoothEventListener {
    var bluetoothService: BluetoothService? = null
    var deviceAddress: String? = null
    var saveDeviceData: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interaction)
        init()
        bindService()

        clearReceive.setOnClickListener {
            receivedText.text = ""
        }

        clearText.setOnClickListener {
            commandWriter.setText("")
        }


        sendString.setOnClickListener {
            bluetoothService?.sendToDevice(commandWriter.text.toString())
            commandWriter.setText("")
        }

        resetAddress.setOnClickListener {
            AlertDialog.Builder(this).setPositiveButton(
                "Yes"
            ) { _, _ ->
                saveDeviceData?.edit()?.putInt(Constants.SAVED_BOOLEAN, 0)?.apply()
                startActivity(Intent(this, DeviceDetailsActivity::class.java))
            }.setNegativeButton(
                "No"
            ) { dialogInterface, _ -> dialogInterface.cancel() }
                .setTitle("Reset UUIDs ?").setMessage("This will reset the UUIDs used in app")
                .create().show()
        }
    }


    private fun init() {
        val intent = intent
        deviceAddress = intent?.getStringExtra(Constants.deviceAddress)
        receivedText.movementMethod = ScrollingMovementMethod()
        saveDeviceData = getSharedPreferences(Constants.sharedPrefName, Context.MODE_PRIVATE)

    }

    override fun onServiceDisconnected(p0: ComponentName?) {

    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        (p1 as BluetoothService.LocalBinder).getService().also { bluetoothService = it }
        bluetoothService?.registerListener(this)
        bluetoothService?.passReferencesToService(
            deviceAddress,
            saveDeviceData?.getString(Constants.uuidForWrite, ""),
            saveDeviceData?.getString(Constants.uuidForNotify, ""),
            saveDeviceData?.getString(Constants.serviceUuid, "")
        )
    }


    private fun bindService() {
        bindService(Intent(this, BluetoothService::class.java), this, Context.BIND_AUTO_CREATE)
    }

    override fun onRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {

    }

    override fun onWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {

        Log.i(BluetoothService.TAG, "data sent = ${characteristic?.value}")

    }

    override fun onCharacteristicsChange(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        Log.i(BluetoothService.TAG, "data" + Arrays.toString(characteristic?.value))

        runOnUiThread {
            val temp = receivedText.text.toString()
            receivedText.text = temp.plus(Arrays.toString(characteristic?.value)).plus("\n")
        }

    }

    override fun onConnectionStatusChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTING) {
            connectionProgressBar.visibility = View.VISIBLE
        } else if (newState == BluetoothProfile.STATE_CONNECTED) {
            connectionProgressBar.visibility = View.INVISIBLE
        }
    }

    override fun onServiceDiscovered(gatt: BluetoothGatt?, status: Int) {
        bluetoothService?.getService()
    }

    override fun onErrorEncountered() {
        runOnUiThread {
            Toast.makeText(
                this,
                "Oops! something went wrong !,Either the device you are trying to connect to does'nt have the requisite service or characteristics",
                Toast.LENGTH_LONG
            ).show()
        }
        saveDeviceData?.edit()?.putInt(Constants.SAVED_BOOLEAN, 0)?.apply()
        startActivity(Intent(this, DeviceDetailsActivity::class.java))

    }

    override fun onDisconnected() {
        runOnUiThread { Toast.makeText(this, "Device disconnected", Toast.LENGTH_LONG).show() }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onResume() {
        super.onResume()
        bluetoothService?.registerListener(this)
        bindService()
    }

    override fun onPause() {
        super.onPause()
        bluetoothService?.registerListener(null)
        unbindService(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
        bluetoothService?.registerListener(null)
    }
}
