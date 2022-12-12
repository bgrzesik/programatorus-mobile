package programatorus.client.screens.choosedevice

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import programatorus.client.databinding.FragmentChooseDeviceBinding
import programatorus.client.device.DeviceAddress
import programatorus.client.screens.choosedevice.devicelist.DeviceListItem


@SuppressLint("MissingPermission")
class ChooseDeviceFragment : Fragment() {

    private lateinit var binding: FragmentChooseDeviceBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = activity?.intent ?: return

        val deviceType = intent.getStringExtra("deviceType") ?: return
        val deviceAddr = intent.getStringExtra("deviceAddr") ?: return

        val device: DeviceAddress = when (deviceType) {
            "bluetooth" -> DeviceAddress.BluetoothDevice(deviceAddr)
            "tcp" -> DeviceAddress.TcpDevice(deviceAddr, 2137)
            else -> return
        }

        val action = ChooseDeviceFragmentDirections.actionChooseDeviceToHome(device)

        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bluetoothAdapter = (requireContext().getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(
                enableIntent,
                REQUEST_ENABLE_BT
            )
        }
        binding = FragmentChooseDeviceBinding.inflate(inflater, container, false)

        binding.pairedDevices.setClickListener {
            navigateToHome(DeviceAddress.BluetoothDevice(it.address))
        }

        binding.discoveredDevices.setClickListener {
            navigateToHome(DeviceAddress.BluetoothDevice(it.address))
        }

        requireContext().registerReceiver(discoveryHandler, IntentFilter(BluetoothDevice.ACTION_FOUND))


        binding.discovery.setOnClickListener {
            runDiscovery()
        }
        return binding.root
    }

    private fun runDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            ))
        } else {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
            ))
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("BT permissions", "${it.key} = ${it.value}")
            }
            startDiscovery()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPairedDevices()
    }

    private fun startDiscovery() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    setPairedDevices()
                } else {
                    val activity = activity
                    activity?.finish()
                    }
                }
            else -> {}
        }
    }


    private val discoveryHandler: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    addDevice(device)
                }
            }
        }
    }

    private fun addDevice(device: BluetoothDevice) {
        binding.discoveredDevices.addDevice(DeviceListItem.from(device))
    }

    private fun setPairedDevices() {
        binding.pairedDevices.setDevices(
            bluetoothAdapter.bondedDevices.map { DeviceListItem.from(it) })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    fun navigateToHome(address: DeviceAddress.BluetoothDevice) {
        bluetoothAdapter.cancelDiscovery()
        val action = ChooseDeviceFragmentDirections.actionChooseDeviceToHome(address)
        findNavController().navigate(action)
    }

    companion object {
        const val REQUEST_ENABLE_BT = 101
    }
}
