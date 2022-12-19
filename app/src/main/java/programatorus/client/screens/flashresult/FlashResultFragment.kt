package programatorus.client.screens.flashresult

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs

import programatorus.client.databinding.FragmentFlashResultBinding

class FlashResultFragment: Fragment() {

    private val args: FlashResultFragmentArgs by navArgs()

    private var _binding: FragmentFlashResultBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFlashResultBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.message.movementMethod = ScrollingMovementMethod()
        binding.message.text = args.message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
