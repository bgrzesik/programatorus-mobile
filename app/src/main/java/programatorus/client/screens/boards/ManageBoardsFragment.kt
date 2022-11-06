package programatorus.client.screens.boards

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import com.google.android.material.tabs.TabLayout
import programatorus.client.databinding.FragmentManageBoardsBinding
import programatorus.client.model.Board
import programatorus.client.screens.boards.all.AllBoardsListItem
import programatorus.client.screens.boards.favorites.FavBoardsListItem
import programatorus.client.shared.ConfigurationsManager


class ManageBoardsFragment : Fragment() {

    private var _binding: FragmentManageBoardsBinding? = null

    private val binding get() = _binding!!

    private val configurationsManager = ConfigurationsManager<Board>(
        (1..12).map { Board(it.toString(), false) },
        (1..12).map { Board(it.toString(), false) }
    )



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentManageBoardsBinding.inflate(inflater, container, false)
        return binding.root

    }

    fun useAll() {
        binding.favBoards.visibility = View.GONE
        binding.allBoards.visibility = View.VISIBLE
    }

    fun useFavorites() {
        with(binding) {
            allBoards.visibility = View.GONE
            favBoards.setBoards(
                allBoards.getBoards()
                    .filter { it.isFavorite() }
                    .map { FavBoardsListItem(it.name) }
            )
            binding.favBoards.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.favBoards.visibility = View.GONE
        binding.allBoards.visibility = View.VISIBLE

        binding.favBoards.enableTouch()
//        binding.favBoards.setBoards(
//            (1..12).map { FavBoardsListItem(it.toString()) }
//        )
//
//
//        binding.allBoards.setBoards(
//            (1..12).map { AllBoardsListItem(it.toString()) }
//        )

        binding.allBoards.setBoards(
            configurationsManager.getAll().map { AllBoardsListItem.from(it) }
        )

        binding.favBoards.setBoards(
            configurationsManager.getFavorites().map { FavBoardsListItem.from(it) }
        )

        binding.tabs.getTabAt(ALL)?.view?.setOnClickListener{ useAll() }
        binding.tabs.getTabAt(FAVORITES)?.view?.setOnClickListener{ useFavorites() }

        binding.btn.setOnClickListener {
            Log.d("fav list:", "fav ${binding.favBoards.getBoards()} \n all ${binding.allBoards.getBoards()}")
        }
        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val ALL = 0
        val FAVORITES = 1
    }
}