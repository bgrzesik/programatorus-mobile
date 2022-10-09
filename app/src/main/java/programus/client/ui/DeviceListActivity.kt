package programus.client.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.content.Intent
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.view.View
import android.widget.*
import programus.client.R
import programus.client.transport.bt.BluetoothTransport
import java.util.ArrayList

class DeviceListActivity : AppCompatActivity() {
    var mDeviceList: ListView? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mPairedDevices: Set<BluetoothDevice>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        mDeviceList = findViewById<View>(R.id.listView) as ListView
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(applicationContext, "Bluetooth Device Not Available", Toast.LENGTH_LONG)
                .show()
            finish()
        } else {
            if (mBluetoothAdapter!!.isEnabled) {
                listPairedDevices()
            } else {
                //Ask to the user turn the bluetooth on
                val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf("Manifest.permission.BLUETOOTH_CONNECT"),
                        1234
                    )
                }
                startActivity(turnBTon)
            }
        }
    }

    private fun listPairedDevices() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1234
            )
        }

        mPairedDevices = mBluetoothAdapter!!.bondedDevices!!
        val list = ArrayList<String>()
        if (mPairedDevices!!.isNotEmpty()) {
            for (bt in mPairedDevices!!) {
                list.add("${bt.name} ${bt.address}")
            }
        } else {
            Toast.makeText(
                applicationContext,
                "No Paired Bluetooth Devices Found.",
                Toast.LENGTH_LONG
            ).show()
        }
        val adapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        mDeviceList!!.adapter = adapter
        mDeviceList!!.onItemClickListener =
            myListClickListener //Method called when the device from the list is clicked
    }

    private val myListClickListener =
        OnItemClickListener { av, v, arg2, arg3 -> // Get the device MAC address, the last 17 chars in the View
            val info = (v as TextView).text.toString()
            val address = info.substring(info.length - 17)
            // Make an intent to start next activity.
            val i = Intent(this@DeviceListActivity, SendTextActivity::class.java)
            //Change the activity.
            i.putExtra(EXTRA_ADDRESS, address) //this will be received at CommunicationsActivity
            startActivity(i)
        }

    companion object {
        var EXTRA_ADDRESS = "device_address"
    }
}