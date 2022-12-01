package programatorus.client.screens.firmware

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import programatorus.client.SharedRemoteContext
import programatorus.client.databinding.FragmentManageFirmwaresBinding
import programatorus.client.screens.firmware.all.AllFirmwaresListItem
import programatorus.client.screens.firmware.favorites.FavFirmwaresListItem
import programatorus.client.shared.LoadingDialog


class ManageFirmwaresFragment : Fragment() {

    private var _binding: FragmentManageFirmwaresBinding? = null

    private val binding get() = _binding!!

    private val firmwareService = SharedRemoteContext.firmwareService
    private val repository = firmwareService.repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentManageFirmwaresBinding.inflate(inflater, container, false)
        return binding.root

    }

    fun useAll() {
        binding.favFirmwares.visibility = View.GONE
        repository.setOrderedFavorites(
            favorites()
        )
        binding.allFirmwares.visibility = View.VISIBLE
    }

    fun useFavorites() {
        repository.updateState(
            all(),
            extractFavorites()
        )
        with(binding) {
            allFirmwares.visibility = View.GONE
            favFirmwares.setFirmwares(
                repository.getFavorites().map { FavFirmwaresListItem.from(it) }
            )
            binding.favFirmwares.visibility = View.VISIBLE
        }
    }

    private fun extractFavorites() =
        binding.allFirmwares.getFirmwares()
            .filter { it.isFavorite() }
            .map { it.asFirmware() }

    private fun all() =
        binding.allFirmwares.getFirmwares()
            .map { it.asFirmware() }

    private fun favorites() =
        binding.favFirmwares.getFirmwares()
            .map { it.asFirmware() }

    fun updateConfigurations() {
        repository.setState(
            binding.allFirmwares.getFirmwares().map { it.asFirmware() },
            binding.favFirmwares.getFirmwares().map { it.asFirmware() }
        )
        repository.updateState(
            all(),
            extractFavorites()
        )
        val dialog = LoadingDialog.loadingDialog(layoutInflater, requireContext())
        firmwareService.push().thenRun {
            dialog.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            favFirmwares.enableTouch()

            allFirmwares.setFirmwares(
                repository.getAll().map { AllFirmwaresListItem.from(it) }
            )

            favFirmwares.setFirmwares(
                repository.getFavorites().map { FavFirmwaresListItem.from(it) }
            )

            tabs.getTabAt(ALL)?.view?.setOnClickListener { useAll() }
            tabs.getTabAt(FAVORITES)?.view?.setOnClickListener { useFavorites() }

            // TODO: Remove later 
            btn.setOnClickListener {
                Log.d(
                    "fav list:",
                    "fav ${favFirmwares.getFirmwares()} \n all ${allFirmwares.getFirmwares()}"
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