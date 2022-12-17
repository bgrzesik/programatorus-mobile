package programatorus.client.screens.firmware

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import programatorus.client.MyApplication
import programatorus.client.RemoteContext
import programatorus.client.databinding.FragmentManageFirmwaresBinding
import programatorus.client.screens.firmware.all.AllFirmwaresListItem
import programatorus.client.screens.firmware.favorites.FavFirmwaresListItem
import programatorus.client.shared.LoadingDialog


class ManageFirmwaresFragment : Fragment() {

    private var _binding: FragmentManageFirmwaresBinding? = null

    private val binding get() = _binding!!

    private val firmwareService = RemoteContext.firmwareService
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
        setOrderedFavorites()
        binding.allFirmwares.visibility = View.VISIBLE
    }

    private fun setOrderedFavorites() {
        repository.setOrderedFavorites(
                favorites()
        )
    }

    fun useFavorites() {
        setOrderedFavorites()
        addNewFavorites()
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

    fun updateRemoteConfig() {
        persistState()
        addNewFavorites()

        val dialog = LoadingDialog.loadingDialog(layoutInflater, requireContext())
        firmwareService.push().thenRun {
            dialog.dismiss()
        }
    }

    private fun persistState() {
        repository.setState(
                binding.allFirmwares.getFirmwares().map { it.asFirmware() },
                binding.favFirmwares.getFirmwares().map { it.asFirmware() }
        )
    }

    private fun addNewFavorites() {
        repository.updateState(
                all(),
                extractFavorites()
        )
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
        }
        

    }

    override fun onDestroyView() {
        updateRemoteConfig()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val ALL = 0
        val FAVORITES = 1
    }
}