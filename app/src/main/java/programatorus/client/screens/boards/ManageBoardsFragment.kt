package programatorus.client.screens.boards

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import programatorus.client.databinding.FragmentManageBoardsBinding


class ManageBoardsFragment : Fragment() {

    private var _binding: FragmentManageBoardsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentManageBoardsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.boards.enableTouch()

        binding.boards.setBoards(
            (1..10).map { BoardListItem(it.toString()) }
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