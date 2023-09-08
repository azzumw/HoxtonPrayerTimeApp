package com.example.hoxtonprayertimeapp.ui.prayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.example.hoxtonprayertimeapp.utils.fromLocalTimeToString
import com.example.hoxtonprayertimeapp.utils.fromStringToLocalTime
import com.example.hoxtonprayertimeapp.utils.isTodayFriday
import com.hoxtonislah.hoxtonprayertimeapp.R
import com.hoxtonislah.hoxtonprayertimeapp.databinding.FragmentPrayer2Binding
import org.koin.androidx.viewmodel.ext.android.viewModel

class PrayerFragment : Fragment() {

    private var _binding: FragmentPrayer2Binding? = null
    private val binding get() = _binding!!

    private val prayerViewModel: PrayerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_prayer_2, container, false)

        binding.viewModel = prayerViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fromLocalTimeToString(fromStringToLocalTime("18:30:00")!!)


        prayerViewModel.apiStatusLiveMerger.observe(viewLifecycleOwner) {
            if (it == ApiStatus.DONE || it == ApiStatus.S_ERROR) {
                showOrHideCards(false)

            } else {
                showOrHideCards(true)
            }
        }

        prayerViewModel.fireStoreWeekModel.observe(viewLifecycleOwner) {
            if (it != null) {
                if (replaceDhuhrWithJummah(isTodayFriday())) {
                    if (it.secondJummah != null) {
                        binding.jummahJamaatOneTimeTv.text = it.firstJummah
                        binding.jummahJamaatTwoTimeTv.text = it.secondJummah

                    } else {
                        binding.dhuhrJamaatTimeTv.text = it.firstJummah
                    }
                } else {
                    binding.dhuhrJamaatTimeTv.text = it.dhuhr
                }

            } else {
                //show no data error animation

            }
        }
    }


    private fun replaceDhuhrWithJummah(isFriday: Boolean): Boolean {
        return if (isFriday) {
            binding.dhuhrJamaatTimeTv.visibility = TextView.GONE
            binding.jummahJamaatsContainer.visibility = TextView.VISIBLE
            binding.dhuhrTextview.text = getString(R.string.jummah_text)
            true
        } else {
            binding.dhuhrTextview.text = getString(R.string.dhohar_text)
            binding.dhuhrJamaatTimeTv.visibility = TextView.VISIBLE
            binding.jummahJamaatsContainer.visibility = TextView.GONE
            false
        }
    }

    private fun showOrHideCards(hide: Boolean) {
        if (hide) {
            binding.prayerTimetableCardview.visibility = View.GONE
            binding.broadcastPrayerCardview.visibility = View.GONE
        } else {
            binding.broadcastPrayerCardview.visibility = View.VISIBLE
            binding.prayerTimetableCardview.visibility = View.VISIBLE
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

// check network connectivity:
//https://stackoverflow.com/questions/17880287/android-programmatically-check-internet-connection-and-display-dialog-if-notco/17880697#17880697