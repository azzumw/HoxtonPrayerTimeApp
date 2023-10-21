package com.hoxtonislah.hoxtonprayertimeapp

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.hoxtonislah.hoxtonprayertimeapp.ui.prayer.ApiStatus
import com.google.android.material.snackbar.Snackbar

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

        ApiStatus.S_ERROR -> {
            statusImageView.visibility = View.GONE
            Snackbar.make(statusImageView.rootView,"Could not connect to London API!",Snackbar.LENGTH_SHORT).show()
        }

        else -> {
            statusImageView.visibility = View.GONE
        }
    }
}