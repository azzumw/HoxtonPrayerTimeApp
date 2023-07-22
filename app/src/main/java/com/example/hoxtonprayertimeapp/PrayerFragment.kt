package com.example.hoxtonprayertimeapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.hoxtonprayertimeapp.databinding.FragmentPrayer2Binding

class PrayerFragment : Fragment() {

    private var _binding:FragmentPrayer2Binding? = null
    private val binding get() = _binding!!

    private lateinit var prayerViewModel:PrayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_prayer_2, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gregoryTodayDateTv.text = getCurrentGregorianDate()
//        binding.islamicDateTv.text = getCurrentIslamicDate()

        val viewModelFactory = PrayerViewModelFactory()
        prayerViewModel = ViewModelProvider(this, viewModelFactory)[PrayerViewModel::class.java]



    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}