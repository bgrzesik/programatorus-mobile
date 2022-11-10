package programatorus.client.screens.boards

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import programatorus.client.SharedData
import programatorus.client.databinding.FragmentManageBoardsBinding
import programatorus.client.screens.boards.all.AllBoardsListItem
import programatorus.client.screens.boards.favorites.FavBoardsListItem


class ManageBoardsFragment : Fragment() {

    private var _binding: FragmentManageBoardsBinding? = null

    private val binding get() = _binding!!

    private val configurationsManager = SharedData.boardManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentManageBoardsBinding.inflate(inflater, container, false)
        return binding.root

    }

    fun useAll() {
        binding.favBoards.visibility = View.GONE
        configurationsManager.setOrderedFavorites(
            favorites()
        )
        binding.allBoards.visibility = View.VISIBLE
    }

    fun useFavorites() {
        configurationsManager.updateState(
            all(),
            extractFavorites()
        )
        with(binding) {
            allBoards.visibility = View.GONE
            favBoards.setBoards(
                configurationsManager.getFavorites().map { FavBoardsListItem.from(it) }
            )
            binding.favBoards.visibility = View.VISIBLE
        }
    }

    private fun extractFavorites() =
        binding.allBoards.getBoards()
            .filter { it.isFavorite() }
            .map { it.asBoard() }

    private fun all() =
        binding.allBoards.getBoards()
            .map { it.asBoard() }

    private fun favorites() =
        binding.favBoards.getBoards()
            .map { it.asBoard() }

    fun updateConfigurations() {
        configurationsManager.setState(
            binding.favBoards.getBoards().map { it.asBoard() },
            binding.allBoards.getBoards().map { it.asBoard() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.favBoards.visibility = View.GONE
        binding.allBoards.visibility = View.VISIBLE

        binding.favBoards.enableTouch()

        binding.allBoards.setBoards(
            configurationsManager.getAll().map { AllBoardsListItem.from(it) }
        )

        binding.favBoards.setBoards(
            configurationsManager.getFavorites().map { FavBoardsListItem.from(it) }
        )

        binding.tabs.getTabAt(ALL)?.view?.setOnClickListener { useAll() }
        binding.tabs.getTabAt(FAVORITES)?.view?.setOnClickListener { useFavorites() }

        binding.btn.setOnClickListener {
            Log.d(
                "fav list:",
                "fav ${binding.favBoards.getBoards()} \n all ${binding.allBoards.getBoards()}"
            )
        }

    }

    override fun onDestroyView() {
        updateConfigurations()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val ALL = 0
        val FAVORITES = 1
    }
}