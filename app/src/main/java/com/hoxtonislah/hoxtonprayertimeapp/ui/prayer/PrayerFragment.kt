package com.hoxtonislah.hoxtonprayertimeapp.ui.prayer


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.hoxtonislah.hoxtonprayertimeapp.utils.isTodayFriday
import com.hoxtonislah.hoxtonprayertimeapp.R
import com.hoxtonislah.hoxtonprayertimeapp.databinding.FragmentPrayer2Binding
import com.hoxtonislah.hoxtonprayertimeapp.utils.liveDate
import com.hoxtonislah.hoxtonprayertimeapp.utils.liveTime
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime

class PrayerFragment : Fragment() {

    private var _binding: FragmentPrayer2Binding? = null
    private val binding get() = _binding!!

    private val prayerViewModel: PrayerViewModel by viewModel()

    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            liveDate = LocalDate.now()
            liveTime = LocalTime.now()

            when (intent?.action) {
                Intent.ACTION_DATE_CHANGED,Intent.ACTION_TIME_CHANGED -> {
                    lifecycleScope.launch {
                        prayerViewModel.clearDataFromLocal()
                    }
                    prayerViewModel.updateTheDates()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_prayer_2, container, false)

        binding.viewModel = prayerViewModel
        binding.lifecycleOwner = this

        hideCards()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.applicationContext?.registerReceiver(br, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
        })

        prayerViewModel.apiStatusLiveMerger.observe(viewLifecycleOwner) {
            if (it == ApiStatus.DONE) hideCards(false) else hideCards()
        }

        prayerViewModel.jamaahTimeCloudModel.observe(viewLifecycleOwner) {
            it?.let {
                Timber.e(it.dhuhr)
                if (isTodayFriday()) {
                    Timber.e("ITS FRIDAY")
                    if (it.secondJummah != null) {
                        swapDhuhrViewForTwoJummahView(true)

                    } else {
                        swapDhuhrViewForTwoJummahView()
                    }
                } else {
                    Timber.e("NO FRIDAY")
                    swapDhuhrViewForTwoJummahView()
                    binding.dhuhrTextview.text = getString(R.string.dhohar_text)
                }
            }
        }

//        prayerViewModel.prayerBeginTimesFromLocal.observe(viewLifecycleOwner){
//            if(it == null){
//                prayerViewModel.getPrayerBeginTimesFromRemote(LocalDate.now())
//            }
//        }


        //this takes care of updating data, when the app is not running.
        prayerViewModel.prayerBeginTimesFromLocal.observe(viewLifecycleOwner) {
            it?.let {
                if (liveDate.toString() != it.date) {
                    prayerViewModel.clearDataFromLocal()
                    prayerViewModel.updateTheDates()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateLiveDates()
    }

    private fun updateLiveDates(){
        liveDate = LocalDate.now()
        liveTime = LocalTime.now()
    }

    private fun swapDhuhrViewForTwoJummahView(secondJamaahExist: Boolean = false) {
        binding.dhuhrTextview.text = getString(R.string.jummah_text)
        if (secondJamaahExist) {
            binding.dhuhrJamaatTimeTv.visibility = TextView.GONE
            binding.jummahJamaatsContainer.visibility = TextView.VISIBLE
        } else {
            binding.dhuhrJamaatTimeTv.visibility = TextView.VISIBLE
            binding.jummahJamaatsContainer.visibility = TextView.GONE
        }
    }

    private fun hideCards(hide: Boolean = true) {
        if (hide) {
            binding.prayerTimetableCardview.visibility = View.GONE
            binding.broadcastPrayerCardview.visibility = View.GONE
        } else {
            binding.broadcastPrayerCardview.visibility = View.VISIBLE
            binding.prayerTimetableCardview.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.applicationContext?.unregisterReceiver(br)
        _binding = null
    }
}

// check network connectivity:
//https://stackoverflow.com/questions/17880287/android-programmatically-check-internet-connection-and-display-dialog-if-notco/17880697#17880697