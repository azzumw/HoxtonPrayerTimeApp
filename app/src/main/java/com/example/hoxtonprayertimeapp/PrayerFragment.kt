package com.example.hoxtonprayertimeapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.hoxtonprayertimeapp.databinding.FragmentPrayerBinding

class PrayerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding:FragmentPrayerBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_prayer, container, false)
        return binding.root
    }
}