package programatorus.client.screens.flashrequest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import programatorus.client.R
import programatorus.client.SharedContext
import programatorus.client.databinding.FragmentFlashRequestBinding
import programatorus.client.databinding.FragmentHomeBinding


class FlashRequestFragment : Fragment() {

    private var _binding: FragmentFlashRequestBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFlashRequestBinding.inflate(inflater, container, false)

        SharedContext.boardsService.pull()
        SharedContext.firmwareService.pull()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmit.setOnClickListener {
            sendRequest()
        }
    }

    fun sendRequest() {
        // TODO: MSG
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}