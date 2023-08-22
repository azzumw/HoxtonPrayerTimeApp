package com.example.hoxtonprayertimeapp.ui.prayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.hoxtonprayertimeapp.R
import com.example.hoxtonprayertimeapp.database.PrayerBeginningTimesDatabase
import com.example.hoxtonprayertimeapp.databinding.FragmentPrayer2Binding
import com.example.hoxtonprayertimeapp.utils.isFridayToday

class PrayerFragment : Fragment() {

    private var _binding: FragmentPrayer2Binding? = null
    private val binding get() = _binding!!

    //    private val prayerViewModel: PrayerViewModel by viewModels()
    private lateinit var prayerViewModel: PrayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_prayer_2, container, false)

        val database  = PrayerBeginningTimesDatabase.getDatabase(requireContext())
        val viewModelFactory = PrayerViewModelFactory(database.prayerDao)

        prayerViewModel = ViewModelProvider(this, viewModelFactory)[PrayerViewModel::class.java]

        binding.viewModel = prayerViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prayerViewModel.status.observe(viewLifecycleOwner) {
            if (it == FireStoreStatus.DONE) {
                binding.prayerTimetableCardview.visibility = View.VISIBLE
                binding.broadcastPrayerCardview.visibility = View.VISIBLE
            } else {
                binding.prayerTimetableCardview.visibility = View.GONE
                binding.broadcastPrayerCardview.visibility = View.GONE
            }
        }

        prayerViewModel.week.observe(viewLifecycleOwner) {
            if (it != null) {
                if (replaceDhuhrWithJummah(isFridayToday())) {
                    if (it.secondJummah != null) {
                        binding.jummahJamaatOneTimeTv.text = it.firstJummah
                        binding.jummahJamaatTwoTimeTv.text = it.secondJummah

                    } else {
                        binding.dhuhrJamaatTimeTv.text = it.firstJummah
                    }
                } else {
                    binding.dhuhrJamaatTimeTv.text = it.dhuhr
                }

                binding.apply {

                    fajrJamaatTimeTv.text = it.fajar
                    asrJamaatTimeTv.text = it.asr
                    ishaJamaatTimeTv.text = it.isha
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}