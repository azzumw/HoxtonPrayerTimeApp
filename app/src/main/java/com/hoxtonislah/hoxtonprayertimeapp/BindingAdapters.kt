package com.hoxtonislah.hoxtonprayertimeapp

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.snackbar.Snackbar
import com.hoxtonislah.hoxtonprayertimeapp.ui.prayer.ApiStatus
import timber.log.Timber

@BindingAdapter("apiStatus")
fun bindStatus(statusImageView: ImageView, status: ApiStatus?) {
    when (status) {
        ApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
            Timber.e("BindAdapter: Loading image")
        }

        ApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.no_internet)
            Timber.e("BindAdapter: No int image")
        }

        else-> {
            statusImageView.visibility = View.GONE
            Timber.e("BindAdapter: DONE")
        }

        //        ApiStatus.S_ERROR -> {
//            statusImageView.visibility = View.GONE
//            Snackbar.make(statusImageView.rootView,"Network Offline",Snackbar.LENGTH_SHORT).show()
//        }
    }
}