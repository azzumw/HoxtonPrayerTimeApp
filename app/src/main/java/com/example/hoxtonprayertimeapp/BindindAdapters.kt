package com.example.hoxtonprayertimeapp

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.hoxtonprayertimeapp.ui.prayer.ApiStatus

@BindingAdapter("fireStoreApiStatus")
fun bindStatus(statusImageView: ImageView, status: ApiStatus?) {
    when (status) {
        ApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }

        ApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.no_internet)
        }

        else -> {
            statusImageView.visibility = View.GONE
        }
    }
}