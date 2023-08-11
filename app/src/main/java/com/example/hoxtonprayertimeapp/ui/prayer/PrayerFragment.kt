package com.example.hoxtonprayertimeapp.ui.prayer

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.hoxtonprayertimeapp.R
import com.example.hoxtonprayertimeapp.databinding.FragmentPrayer2Binding
import com.example.hoxtonprayertimeapp.utils.getCurrentGregorianDate
import com.example.hoxtonprayertimeapp.utils.isFridayToday

class PrayerFragment : Fragment() {

    private var _binding: FragmentPrayer2Binding? = null
    private val binding get() = _binding!!

    private val prayerViewModel: PrayerViewModel by viewModels()

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val viewModelFactory = PrayerViewModelFactory()
//        prayerViewModel = ViewModelProvider(this, viewModelFactory)[PrayerViewModel::class.java]

        prayerViewModel.status.observe(viewLifecycleOwner) {
            if (it == FireStoreStatus.DONE) {
                binding.prayerTimetableCardview.visibility = View.VISIBLE
                binding.broadcastPrayerCardview.visibility = View.VISIBLE
            } else {
                binding.prayerTimetableCardview.visibility = View.GONE
                binding.broadcastPrayerCardview.visibility = View.GONE
            }
        }

        prayerViewModel.prayer.observe(viewLifecycleOwner) {
            if (it != null) {
                if (isFridayToday()) {
                    //if second jummah exists
                    if (it.secondJummah != null) {
                        replaceDhuhrWithJummah(true)
                        binding.jummahJamaatOneTimeTv.text = it.firstJummah
                        binding.jummahJamaatTwoTimeTv.text = it.secondJummah

                    } else {
                        replaceDhuhrWithJummah(false)
                        binding.dhuhrJamaatTimeTv.text = it.firstJummah
                    }

                    binding.dhuhrTextview.text = getString(R.string.jummah_text)

                } else {
                    replaceDhuhrWithJummah(false)
                    binding.dhuhrJamaatTimeTv.text = it.dhuhr
                }

                binding.apply {

                    fajrJamaatTimeTv.text = it.fajar
                    asrJamaatTimeTv.text = it.asr
                    maghribJamaatTimeTv.text = it.maghrib
                    ishaJamaatTimeTv.text = it.isha
                }
            } else {
                //show no data error animation

            }

            displayNextPrayer()
        }
    }

    private fun displayNextPrayer() {
        prayerViewModel.nextJamaat.observe(viewLifecycleOwner) {
            if (it == "Good Night") {
                binding.nextPrayerLabel.visibility = View.GONE
            } else binding.nextPrayerLabel.visibility = View.VISIBLE

            binding.nextPrayerText.text = it.substringBefore(' ')
            binding.nextPrayerTime.text = it.substringAfter(' ')
        }
    }


    private fun replaceDhuhrWithJummah(isFriday: Boolean) {
        if (isFriday) {
            binding.dhuhrJamaatTimeTv.visibility = TextView.GONE
            binding.jummahJamaatsContainer.visibility = TextView.VISIBLE
        } else {
            binding.dhuhrTextview.text = getString(R.string.dhohar_text)
            binding.dhuhrJamaatTimeTv.visibility = TextView.VISIBLE
            binding.jummahJamaatsContainer.visibility = TextView.GONE
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}