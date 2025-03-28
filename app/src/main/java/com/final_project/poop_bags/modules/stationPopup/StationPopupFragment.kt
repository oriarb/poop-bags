package com.final_project.poop_bags.modules.stationPopup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.final_project.poop_bags.R
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.utils.LocationUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

private const val ARG_STATION = "station"

class StationPopupFragment: BottomSheetDialogFragment() {

    private var station: Station? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            station = it.getParcelable(ARG_STATION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragement_station_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.station_name).text = station?.name
        view.findViewById<TextView>(R.id.likes_count).text = "${station?.likes?.size ?: 0}"
        view.findViewById<TextView>(R.id.comments_count).text = "${station?.comments?.size ?: 0}"
        Picasso.get()
            .load(station?.imageUrl)
            .error(R.drawable.ic_missing_image)
            .into(view.findViewById<ImageView>(R.id.station_image))

        view.findViewById<ImageView>(R.id.star_icon).visibility =
            if (station?.isFavorite == true) View.VISIBLE else View.GONE

        lifecycleScope.launch {
            station?.let {
                LocationUtil(requireContext()).getDistanceFromCurrentLocation(it.latitude, it.longitude)
                    .collect { distance ->
                        view.findViewById<TextView>(R.id.station_distance).text =
                            if (distance != null) "%,.0fm away".format(distance) else "Distance unavailable"
                    }
            }
        }

        val viewDetailsButton = view.findViewById<Button>(R.id.view_details)

        viewDetailsButton.setOnClickListener {
            station?.let {
                val bundle = Bundle().apply {
                    putString("stationId", it.id)
                }
                findNavController().navigate(R.id.action_navigation_map_to_stationDetailsFragment, bundle)
            }
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(station: Station) =
            StationPopupFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_STATION, station)
                }
            }
    }
}