package programatorus.client.screens.boards.all

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import programatorus.client.databinding.FragmentAllBoardsListBinding


class AllBoardsListFragment : Fragment() {

    private var _binding: FragmentAllBoardsListBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAllBoardsListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.boards.setBoards(
            (1..10).map { AllBoardsListItem(it.toString()) }
        )

        binding.btn.setOnClickListener {
            Log.d("boards list:", binding.boards.getBoards().toString())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}