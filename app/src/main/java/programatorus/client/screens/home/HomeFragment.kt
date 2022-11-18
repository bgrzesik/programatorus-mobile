package programatorus.client.screens.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import programatorus.client.R
import programatorus.client.SharedContext
import programatorus.client.databinding.FragmentHomeBinding
import programatorus.client.device.BoundDevice


class HomeFragment : Fragment() {
    companion object{
        const val TAG = "HomeFragment"
    }

    private lateinit var mDevice: BoundDevice
    private val args: HomeFragmentArgs by navArgs()
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        SharedContext.boardsService.pull()
        SharedContext.firmwareService.pull()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireContext().also { context ->
            mDevice = BoundDevice(context, args.device)
            mDevice.bind()
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}