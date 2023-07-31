package com.example.hoxtonprayertimeapp.ui.prayer

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.hoxtonprayertimeapp.R
import com.example.hoxtonprayertimeapp.databinding.FragmentPrayer2Binding
import com.example.hoxtonprayertimeapp.utils.getCurrentGregorianDate
import com.example.hoxtonprayertimeapp.utils.isFridayToday
import com.google.firebase.Timestamp
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

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

        binding.gregoryTodayDateTv.text = getCurrentGregorianDate()

        val time = Timestamp.now().toDate()
        val df = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val f = df.format(time)

//        val tString = "05:30am"
//        val toTime = df.parse(tString)
        Timber.e("time: $f")

//        val viewModelFactory = PrayerViewModelFactory()
//        prayerViewModel = ViewModelProvider(this, viewModelFactory)[PrayerViewModel::class.java]

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

                    val df = SimpleDateFormat("hh:mm a", Locale.getDefault())

                    Timber.e(time.toString())
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