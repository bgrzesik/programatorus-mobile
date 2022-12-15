package programatorus.client.screens.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import programatorus.client.R
import programatorus.client.RemoteContext
import programatorus.client.databinding.FragmentHomeBinding
import programatorus.client.device.BoundDevice
import programatorus.client.device.DeviceAddress
import programatorus.client.shared.LoadingDialog


class HomeFragment : Fragment() {
    companion object {
        const val TAG = "HomeFragment"
    }

    private lateinit var presenter: HomePresenter
    private lateinit var dialog: AlertDialog
    private val args: HomeFragmentArgs by navArgs()
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        presenter = HomePresenter(this, requireContext(), args.device)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
    }

    private fun setupButtons() {
        with(binding) {
            btnBoards.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_manageBoards)
            }

            btnFirmware.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_manageFirmwares)
            }

            btnUploadFile.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_uploadFile)
            }

            btnFlashRequest.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_flashRequest)
            }

            btnDebugger.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_debugger)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        showLoading()
        presenter.start()
    }

    fun showLoading() {
        dialog = LoadingDialog.loadingDialog(layoutInflater, requireContext())
    }

    fun stopLoading() {
        dialog.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HomePresenter(val view: HomeFragment, val context: Context, val device: DeviceAddress) {

    fun start() {
        val mDevice = BoundDevice(context, device)
        mDevice.bind()
        mDevice.onBind.thenAccept { device ->
            RemoteContext.start(device).thenAccept { _ ->
                view.stopLoading()
            }
        }
    }

}