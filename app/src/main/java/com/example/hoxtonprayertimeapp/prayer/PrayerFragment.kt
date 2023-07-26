package com.example.hoxtonprayertimeapp.prayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.hoxtonprayertimeapp.R
import com.example.hoxtonprayertimeapp.databinding.FragmentPrayer2Binding
import com.example.hoxtonprayertimeapp.utils.getCurrentGregorianDate
import com.example.hoxtonprayertimeapp.utils.isFridayToday

class PrayerFragment : Fragment() {

    private var _binding: FragmentPrayer2Binding? = null
    private val binding get() = _binding!!

    private lateinit var prayerViewModel: PrayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_prayer_2, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gregoryTodayDateTv.text = getCurrentGregorianDate()
//        binding.islamicDateTv.text = getCurrentIslamicDate()

        val viewModelFactory = PrayerViewModelFactory()
        prayerViewModel = ViewModelProvider(this, viewModelFactory)[PrayerViewModel::class.java]

        prayerViewModel.prayer.observe(viewLifecycleOwner)  {
            if (it != null) {
                if (isFridayToday()){
                    //if second jummah exists
                    if(it.secondJummah != null){
                        replaceDhuhrWithJummah(true)
                        binding.jummahJamaatOneTimeTv.text = getString(R.string.pm,it.firstJummah)
                        binding.jummahJamaatTwoTimeTv.text = getString(R.string.pm,it.secondJummah)

                    }else {
                        replaceDhuhrWithJummah(false)
                        binding.dhuhrJamaatTimeTv.text = getString(R.string.pm,it.firstJummah)
                    }

                    binding.dhuhrTextview.text = getString(R.string.jummah_text)

                }else{
                    replaceDhuhrWithJummah(false)
                    binding.dhuhrJamaatTimeTv.text = getString(R.string.pm,it.dhuhr)
                }

                binding.apply {
                    fajrJamaatTimeTv.text = getString(R.string.am, it.fajar)
                    asrJamaatTimeTv.text = getString(R.string.pm, it.asr)
                    maghribJamaatTimeTv.text = getString(R.string.pm, it.maghrib)
                    ishaJamaatTimeTv.text = getString(R.string.pm, it.isha)
                }
            }else{
                //show no data error animation

            }
        }
    }


    private fun replaceDhuhrWithJummah(isFriday:Boolean){
        if (isFriday){
            binding.dhuhrJamaatTimeTv.visibility = TextView.GONE
            binding.jummahJamaatsContainer.visibility = TextView.VISIBLE
        }else{
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