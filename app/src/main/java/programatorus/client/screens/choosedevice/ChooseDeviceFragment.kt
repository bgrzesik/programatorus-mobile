package programatorus.client.screens.choosedevice

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import programatorus.client.databinding.FragmentChooseDeviceBinding
import programatorus.client.device.DeviceAddress

@SuppressLint("MissingPermission")
class ChooseDeviceFragment : Fragment() {

    private lateinit var binding: FragmentChooseDeviceBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter

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

        requireContext().registerReceiver(discoveryHandler, IntentFilter(BluetoothDevice.ACTION_FOUND))


        binding.addItem.setOnClickListener {
            discoveryPermissions()
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            startActivityForResult(discoverableIntent, 2137)
            startDiscovery()
        }
        return binding.root
    }

    private fun discoveryPermissions() {
        when(checkSelfPermission(
            requireContext(), Manifest.permission.BLUETOOTH_ADMIN
        )){
            PackageManager.PERMISSION_DENIED -> {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADMIN), REQUEST_BT_ADMIN)
            }
            PackageManager.PERMISSION_GRANTED -> {}
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when(checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_SCAN
            )){
                PackageManager.PERMISSION_DENIED -> {
                    requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADMIN), REQUEST_BT_SCAN)
                }
                PackageManager.PERMISSION_GRANTED -> {}
            }
            when(checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_CONNECT
            )){
                PackageManager.PERMISSION_DENIED -> {
                    requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADMIN), REQUEST_BT_CONNECT)
                }
                PackageManager.PERMISSION_GRANTED -> {}
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when(checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            )){
                PackageManager.PERMISSION_DENIED -> {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_COARSE_LOCATION)
                }
                PackageManager.PERMISSION_GRANTED -> {}
            }
            when(checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            )){
                PackageManager.PERMISSION_DENIED -> {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
                }
                PackageManager.PERMISSION_GRANTED -> {}
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when(checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )){
                PackageManager.PERMISSION_DENIED -> {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_LOCATION)
                }
                PackageManager.PERMISSION_GRANTED -> {}
            }
        }
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
                if (resultCode == Activity.RESULT_OK) {
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
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
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
        val action = ChooseDeviceFragmentDirections.actionChooseDeviceToHome(address)
        findNavController().navigate(action)
    }

    companion object {
        const val REQUEST_ENABLE_BT = 101
        const val REQUEST_BT_ADMIN = 102
        const val REQUEST_COARSE_LOCATION = 103
        const val REQUEST_FINE_LOCATION = 104
        const val REQUEST_BT_CONNECT = 105
        const val REQUEST_BT_SCAN = 106
        const val REQUEST_BACKGROUND_LOCATION = 107
    }
}