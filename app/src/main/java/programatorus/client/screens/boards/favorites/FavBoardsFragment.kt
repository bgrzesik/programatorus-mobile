package programatorus.client.screens.boards.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import programatorus.client.databinding.FragmentFavBoardsListBinding



class FavBoardsListFragment : Fragment() {

    private var _binding: FragmentFavBoardsListBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFavBoardsListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.boards.enableTouch()
        binding.boards.setBoards(
            (1..10).map { FavBoardsListItem(it.toString()) }
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