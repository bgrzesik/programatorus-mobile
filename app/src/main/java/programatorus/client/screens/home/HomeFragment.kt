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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import programatorus.client.R
import programatorus.client.SharedRemoteContext
import programatorus.client.databinding.FragmentHomeBinding
import programatorus.client.device.BoundDevice
import programatorus.client.device.DeviceAddress
import programatorus.client.shared.LoadingDialog


class HomeFragment : Fragment() {
    companion object{
        const val TAG = "HomeFragment"
    }

    private lateinit var presenter: HomePresenter
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

        binding.btnBoards.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_manageBoards)
        }

        binding.btnFirmware.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_manageFirmwares)
        }

        binding.btnUploadFile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_uploadFile)
        }

        binding.btnFlashRequest.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_flashRequest)
        }

        binding.btnDebugger.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_debugger)
        }
    }

    override fun onResume() {
        super.onResume()

//        showLoading()
        val dialog = LoadingDialog.loadingDialog(layoutInflater, requireContext())
        presenter.start(dialog)
//        showContents()
    }

//    fun showLoading() {
//        binding.loadingLayout.loadingLayout.visibility=View.VISIBLE
//    }
//
//    fun stopLoading() {
//            binding.loadingLayout.loadingLayout.visibility=View.GONE
//    }
//
//    fun showContents() {
//        binding.btns.visibility=View.VISIBLE
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HomePresenter(val view: HomeFragment, val context: Context, val device: DeviceAddress) {

    private lateinit var mDevice: BoundDevice
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    fun start(dialog: AlertDialog){
            mDevice = BoundDevice(context, device)
            mDevice.bind()
            mDevice.onBind.thenAccept { device ->
                    SharedRemoteContext.start(device).thenAccept { _ ->
                        dialog.dismiss()
//                        view.stopLoading()
//                        view.showContents()
                    }
//                    view.stopLoading()
                }
//        view.stopLoading()

//                SharedContext.getFirmwareBlocking()
//                SharedContext.getBoardsBlocking()
//                view.stopLoading()

//            .invokeOnCompletion { _ -> view.stopLoading() }
    }

}