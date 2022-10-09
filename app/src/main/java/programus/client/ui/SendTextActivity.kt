package programus.client.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import programus.client.R
import programus.client.transport.ConnectionState
import programus.client.transport.ITransportClient
import programus.client.transport.bt.BluetoothTransport
import programus.client.transport.wrapper.Transport
import programus.proto.GenericMessage
import programus.proto.TestMessage
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

class SendTextActivity : AppCompatActivity() {
    var chosenBoard: TextView? = null
    var chosenBinFile: TextView? = null
    var msgEdit: EditText? = null
    var submitMsg: Button? = null
    var activity: AppCompatActivity = this

    //    var service: ConnectionService? = null
    private var deviceAddress: String? = null
    private var msgView: ListView? = null
    private var boardsView: ListView? = null
    private var binaryView: ListView? = null
    private var msgLog: ArrayAdapter<String>? = null
    private var boardFiles: ArrayAdapter<String>? = null
    private var binFiles: ArrayAdapter<String>? = null

    private fun handleServerMessage(bytes: List<Byte>?) {
        if (bytes == null) return
        Log.d(TAG, "handleServerMessage: " + bytes.size)
        Log.d(TAG, "handleServerMessage: $bytes")
        val arr = ByteArray(bytes.size - 5)
        var j = 0
        for (b in bytes.subList(5, bytes.size)) {
            arr[j] = b
            j++
        }
        Log.d(TAG, "handleServerMessage: " + arr[arr.size - 1])
        val msg = String(arr, StandardCharsets.UTF_8)
        msgLog?.add("server:  $msg")
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "creating")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communications)
        msgLog = ArrayAdapter<String>(this, R.layout.msg)
        boardFiles = ArrayAdapter<String>(this, R.layout.msg)
        binFiles = ArrayAdapter<String>(this, R.layout.msg)
        val newint: Intent = getIntent()
        deviceAddress = newint.getStringExtra(DeviceListActivity.EXTRA_ADDRESS)
        msgEdit = findViewById<View>(R.id.editText) as EditText?
        chosenBoard = findViewById<View>(R.id.boardsText) as TextView?
        chosenBinFile = findViewById<View>(R.id.binaryText) as TextView?
//        service = ConnectionService(handler, deviceAddress)

        // TODO: Handle permissions
        startTransport()
    }

    private fun startTransport() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val device = adapter.getRemoteDevice(deviceAddress)

        val transport = Transport({ client, executor ->
            BluetoothTransport(
                this@SendTextActivity,
                device,
                client,
                executor
            )
        }, object : ITransportClient {
            override fun onMessageReceived(message: GenericMessage) {
                println(message)
            }

            override fun onError() {
                println("onError")
            }

            override fun onStateChanged(state: ConnectionState) {
                println(state)
            }
        })

        transport.send(GenericMessage.newBuilder()
            .setSessionId(0)
            .setTest(TestMessage.newBuilder()
                .setValue("Test"))
            .build())
    }


    override fun onStart() {
        super.onStart()
        msgEdit = findViewById<View>(R.id.editText) as EditText?
        submitMsg = findViewById<View>(R.id.sendButton) as Button?
        binaryView = findViewById<View>(R.id.binaryView) as ListView?
        binaryView!!.adapter = binFiles
        binaryView!!.onItemClickListener = binariesClickListener
        boardsView = findViewById<View>(R.id.boardsView) as ListView?
        boardsView!!.adapter = boardFiles
        boardsView!!.onItemClickListener = boardsClickListener
        msgView = findViewById<View>(R.id.msgView) as ListView?
        msgView!!.adapter = msgLog
    }

    private val boardsClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { av, v, arg2, arg3 ->
            val filename: String = (v as TextView).getText().toString()
            chosenBoard?.text = filename
        }
    private val binariesClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { av, v, arg2, arg3 ->
            val filename: String = (v as TextView).getText().toString()
            chosenBinFile?.text = filename
        }

    companion object {
        private const val TAG = "TextActivity"
    }
}