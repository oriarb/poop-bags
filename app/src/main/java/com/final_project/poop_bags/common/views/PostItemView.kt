package com.final_project.poop_bags.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.final_project.poop_bags.R
import com.final_project.poop_bags.models.Post
import com.final_project.poop_bags.databinding.ViewPostItemBinding
import com.google.android.material.snackbar.Snackbar

class PostItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ViewPostItemBinding =
        ViewPostItemBinding.inflate(LayoutInflater.from(context), this)

    data class ViewConfig(
        val isFavorite: Boolean = false,
        val isDelete: Boolean = false,
        val isEdit: Boolean = false,
        val isLikeEnabled: Boolean = true
    )

    @SuppressLint("SetTextI18n")
    fun bind(
        post: Post,
        config: ViewConfig,
        onFavoriteClick: ((Post) -> Unit)? = null,
        onLikeClick: ((Post) -> Unit)? = null,
        onDeleteClick: ((Post) -> Unit)? = null,
        onEditClick: ((Post) -> Unit)? = null,
        isLiked: Boolean = false
    ) {
        try {
            binding.apply {
                titleText.text = post.title
                addressText.text = post.address
                likesCount.text = post.likesCount.toString()
                commentsCount.text = post.commentsCount.toString()
                
                try {
                    if (post.imageUrl.isEmpty()) {
                        postImage.setImageResource(android.R.drawable.ic_menu_gallery)
                    } else {
                        Glide.with(context)
                            .load(post.imageUrl)
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .fallback(R.drawable.sample_post_image)
                            .into(postImage)
                    }
                } catch (e: Exception) {
                    postImage.setImageResource(R.drawable.sample_post_image)
                }

                favoriteButton.apply {
                    visibility = if (config.isFavorite) View.VISIBLE else View.GONE
                    setImageResource(
                        if (post.isFavorite) R.drawable.ic_star_filled
                        else R.drawable.ic_star_outline
                    )
                    setOnClickListener { 
                        try {
                            onFavoriteClick?.invoke(post)
                        } catch (e: Exception) {
                            showError("Error toggling favorite: ${e.message}")
                        }
                    }
                }

                likeButton.apply {
                    visibility = if (config.isLikeEnabled) View.VISIBLE else View.GONE
                    isSelected = isLiked
                    setOnClickListener { 
                        try {
                            onLikeClick?.invoke(post)
                        } catch (e: Exception) {
                            showError("Error toggling like: ${e.message}")
                        }
                    }
                }

                deleteButton.apply {
                    visibility = if (config.isDelete) View.VISIBLE else View.GONE
                    setOnClickListener { 
                        try {
                            onDeleteClick?.invoke(post)
                        } catch (e: Exception) {
                            showError("Error deleting post: ${e.message}")
                        }
                    }
                }

                editButton.apply {
                    visibility = if (config.isEdit) View.VISIBLE else View.GONE
                    setOnClickListener { 
                        try {
                            onEditClick?.invoke(post)
                        } catch (e: Exception) {
                            showError("Error editing post: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            showError("Error binding post data: ${e.message}")
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
    }
} 