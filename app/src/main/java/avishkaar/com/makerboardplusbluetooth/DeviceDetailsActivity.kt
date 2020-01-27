package avishkaar.com.makerboardplusbluetooth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_device_details.*
import pub.devrel.easypermissions.EasyPermissions


class DeviceDetailsActivity : AppCompatActivity() {
    var RC_LOCATION =  1;
    var perms = arrayOf<String>(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_details)
        val saveDeviceData = getSharedPreferences(Constants.sharedPrefName,Context.MODE_PRIVATE)
        if(saveDeviceData.getInt(Constants.SAVED_BOOLEAN,0)==1)
        {
            startActivity(Intent(this,ScanActivity::class.java))
        }

        saveServiceAndCharacteristics.setOnClickListener{
            if(EasyPermissions.hasPermissions(this,*perms)) {
                if ((serviceUUID.text.toString().length >= 36) && (writeCharacteristics?.text.toString().length >= 36) && (notifyUUID.text.toString().length >= 36)) {
                    saveDeviceData.edit()
                        .putString(Constants.serviceUuid, serviceUUID.text.toString())
                        .apply()
                    saveDeviceData.edit()
                        .putString(Constants.uuidForWrite, writeCharacteristics.text.toString())
                        .apply()
                    saveDeviceData.edit()
                        .putString(Constants.uuidForNotify, notifyUUID.text.toString())
                        .apply()
                    saveDeviceData.edit().putInt(Constants.SAVED_BOOLEAN, 1).apply()
                    startActivity(Intent(this, ScanActivity::class.java))
                } else {
                    Toast.makeText(this, "Invalid UUID's", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this,"Please grant permissions",Toast.LENGTH_LONG).show()
                EasyPermissions.requestPermissions(this,"Please grant permissions",RC_LOCATION, *perms)
            }
        }


    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
