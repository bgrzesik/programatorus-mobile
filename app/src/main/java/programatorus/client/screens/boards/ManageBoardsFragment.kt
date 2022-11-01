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
import programatorus.client.screens.boards.all.AllBoardsListItem
import programatorus.client.screens.boards.favorites.FavBoardsListItem


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

    fun useAll() {
        binding.favBoards.visibility = View.GONE
        binding.allBoards.visibility = View.VISIBLE
    }

    fun useFavorites() {
        binding.allBoards.visibility = View.GONE
        binding.favBoards.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.favBoards.visibility = View.GONE
        binding.allBoards.visibility = View.VISIBLE

        binding.favBoards.enableTouch()
        binding.favBoards.setBoards(
            (1..12).map { FavBoardsListItem(it.toString()) }
        )


        binding.allBoards.setBoards(
            (1..12).map { AllBoardsListItem(it.toString()) }
        )

        Log.d("TABS", binding.tabs[0].toString())


        binding.tabs.getTabAt(0)?.view?.setOnClickListener{ useAll() }
        binding.tabs.getTabAt(1)?.view?.setOnClickListener{ useFavorites() }

        binding.btn.setOnClickListener {
            Log.d("fav list:", "fav ${binding.favBoards.getBoards()} \n all ${binding.allBoards.getBoards()}")
        }
        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}