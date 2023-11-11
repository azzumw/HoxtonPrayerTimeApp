package com.hoxtonislah.hoxtonprayertimeapp.ui.prayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.hoxtonislah.hoxtonprayertimeapp.utils.isTodayFriday
import com.hoxtonislah.hoxtonprayertimeapp.R
import com.hoxtonislah.hoxtonprayertimeapp.databinding.FragmentPrayer2Binding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate

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

        showOrHideCards(true)

        prayerViewModel.apiStatusLiveMerger.observe(viewLifecycleOwner) {
            if (it == ApiStatus.DONE || it == ApiStatus.S_ERROR) {
                showOrHideCards(false)

            } else {
                showOrHideCards(true)
            }
        }


        prayerViewModel.fireStoreWeekModel.observe(viewLifecycleOwner) {

            if (it != null) {
                if (isTodayFriday(LocalDate.now())) {
                    if (it.secondJummah != null) {
                        replaceDhuhrViewWithJummahViewIfSecondJummahExists(true)

                    } else {
                        replaceDhuhrViewWithJummahViewIfSecondJummahExists()
                    }
                } else {
                    binding.dhuhrTextview.text = getString(R.string.dhohar_text)
                }

            } else {
                //show no data error animation

            }
        }
    }

    private fun replaceDhuhrViewWithJummahViewIfSecondJummahExists(secondJamaahExist: Boolean = false) {
        binding.dhuhrTextview.text = getString(R.string.jummah_text)
        if (secondJamaahExist) {
            binding.dhuhrJamaatTimeTv.visibility = TextView.GONE
            binding.jummahJamaatsContainer.visibility = TextView.VISIBLE
        } else {
            binding.dhuhrJamaatTimeTv.visibility = TextView.VISIBLE
            binding.jummahJamaatsContainer.visibility = TextView.GONE
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