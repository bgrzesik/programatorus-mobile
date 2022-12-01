package programatorus.client.screens.boards

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import programatorus.client.SharedRemoteContext
import programatorus.client.databinding.FragmentManageBoardsBinding
import programatorus.client.device.BoundDevice
import programatorus.client.screens.boards.all.AllBoardsListItem
import programatorus.client.screens.boards.favorites.FavBoardsListItem
import programatorus.client.shared.LoadingDialog


class ManageBoardsFragment : Fragment() {
    private lateinit var mDevice: BoundDevice
    private var _binding: FragmentManageBoardsBinding? = null

    private val binding get() = _binding!!

    private val boardsService = SharedRemoteContext.boardsService
    private val repository = boardsService.repository


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentManageBoardsBinding.inflate(inflater, container, false)
        return binding.root

    }

    fun useAll() {
        binding.favBoards.visibility = View.GONE
        repository.setOrderedFavorites(
            favorites()
        )
        binding.allBoards.visibility = View.VISIBLE
    }

    fun useFavorites() {
        repository.updateState(
            all(),
            extractFavorites()
        )
        with(binding) {
            allBoards.visibility = View.GONE
            favBoards.setBoards(
                repository.getFavorites().map { FavBoardsListItem.from(it) }
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
        repository.setState(
            binding.allBoards.getBoards().map { it.asBoard() },
            binding.favBoards.getBoards().map { it.asBoard() }
        )
        repository.updateState(
            all(),
            extractFavorites()
        )
        val dialog = LoadingDialog.loadingDialog(layoutInflater, requireContext())
        boardsService.push().thenRun {
            dialog.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            favBoards.enableTouch()

            allBoards.setBoards(
                repository.getAll().map { AllBoardsListItem.from(it) }
            )

            favBoards.setBoards(
                repository.getFavorites().map { FavBoardsListItem.from(it) }
            )

            tabs.getTabAt(ALL)?.view?.setOnClickListener { useAll() }
            tabs.getTabAt(FAVORITES)?.view?.setOnClickListener { useFavorites() }

            // TODO: Remove later 
            btn.setOnClickListener {
                Log.d(
                    "fav list:",
                    "fav ${favBoards.getBoards()} \n all ${allBoards.getBoards()}"
                )
            }
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