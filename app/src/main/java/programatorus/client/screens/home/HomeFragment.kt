package programatorus.client.screens.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import programatorus.client.R
import programatorus.client.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBoards.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_manageBoards)
        }

        binding.btnFirmware.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_manageBoards)
        }

        binding.btnUploadFile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_uploadFile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}