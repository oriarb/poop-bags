package com.final_project.poop_bags.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.final_project.poop_bags.R
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.databinding.ViewStationItemBinding
import com.google.android.material.snackbar.Snackbar

class StationItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ViewStationItemBinding =
        ViewStationItemBinding.inflate(LayoutInflater.from(context), this)

    data class ViewConfig(
        val isFavorite: Boolean = false,
        val isDelete: Boolean = false,
        val isEdit: Boolean = false,
        val isLikeEnabled: Boolean = true
    )

    @SuppressLint("SetTextI18n")
    fun bind(
        station: Station,
        config: ViewConfig,
        onFavoriteClick: ((Station) -> Unit)? = null,
        onLikeClick: ((Station) -> Unit)? = null,
        onDeleteClick: ((Station) -> Unit)? = null,
        onEditClick: ((Station) -> Unit)? = null,
        isLiked: Boolean = false
    ) {
        try {
            binding.apply {
                stationTitle.text = station.name
                
                val likesText = station.likes.size.toString()
                if (likesCount.text != likesText) {
                    likesCount.text = likesText
                }
                
                val commentsText = station.comments.size.toString()
                if (commentsCount.text != commentsText) {
                    commentsCount.text = commentsText
                }

                if (stationImage.tag != station.imageUrl) {
                    try {
                        if (station.imageUrl.isEmpty()) {
                            stationImage.setImageResource(android.R.drawable.ic_menu_gallery)
                        } else {
                            Glide.with(context)
                                .load(station.imageUrl)
                                .centerCrop()
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .fallback(android.R.drawable.ic_menu_gallery)
                                .into(stationImage)
                        }
                        stationImage.tag = station.imageUrl
                    } catch (e: Exception) {
                        stationImage.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }

                if (likeButton.isSelected != isLiked) {
                    likeButton.isSelected = isLiked
                }

                favoriteButton.apply {
                    visibility = if (config.isFavorite) View.VISIBLE else View.GONE
                    setImageResource(
                        if (station.isFavorite) R.drawable.ic_star_filled
                        else R.drawable.ic_star_outline
                    )
                    imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.yellow)
                    )
                    setOnClickListener { 
                        try {
                            onFavoriteClick?.invoke(station)
                        } catch (e: Exception) {
                            showError("Error toggling favorite: ${e.message}")
                        }
                    }
                }

                likeButton.apply {
                    visibility = if (config.isLikeEnabled) View.VISIBLE else View.GONE
                    setOnClickListener { 
                        try {
                            onLikeClick?.invoke(station)
                        } catch (e: Exception) {
                            showError("Error toggling like: ${e.message}")
                        }
                    }
                }

                deleteButton.apply {
                    visibility = if (config.isDelete) View.VISIBLE else View.GONE
                    setOnClickListener { 
                        try {
                            onDeleteClick?.invoke(station)
                        } catch (e: Exception) {
                            showError("Error deleting station: ${e.message}")
                        }
                    }
                }

                editButton.apply {
                    visibility = if (config.isEdit) View.VISIBLE else View.GONE
                    setOnClickListener { 
                        try {
                            onEditClick?.invoke(station)
                        } catch (e: Exception) {
                            showError("Error editing station: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            showError("Error binding station data: ${e.message}")
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
    }
} 