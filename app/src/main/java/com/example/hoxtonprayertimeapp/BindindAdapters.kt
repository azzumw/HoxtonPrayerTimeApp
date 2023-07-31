package com.example.hoxtonprayertimeapp

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.hoxtonprayertimeapp.ui.prayer.FireStoreStatus

@BindingAdapter("fireStoreApiStatus")
fun bindStatus(statusImageView: ImageView, status: FireStoreStatus?) {
    when (status) {
        FireStoreStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }
        FireStoreStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.no_internet)
        }
        FireStoreStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}