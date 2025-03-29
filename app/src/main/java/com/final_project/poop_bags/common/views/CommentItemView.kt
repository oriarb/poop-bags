package com.final_project.poop_bags.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.final_project.poop_bags.databinding.ViewCommentItemBinding
import com.final_project.poop_bags.models.Comment
import com.final_project.poop_bags.models.User
import com.final_project.poop_bags.repository.UserRepository
import javax.inject.Inject

class CommentItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ViewCommentItemBinding =
        ViewCommentItemBinding.inflate(LayoutInflater.from(context), this)

    @Inject
    lateinit var userRepository: UserRepository

    @SuppressLint("SetTextI18n")
    suspend fun bind(comment: Comment) {
        try {
            binding.apply {
                commentText.text = comment.text
                val user: User? = userRepository.getUserById(comment.userId)

                if (user?.image != null && user.image != "") {
                    Glide.with(context)
                        .load(user.image)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(commentUserImage)
                }
            }
        } catch (e: Exception) {
            showError("Error binding station data: ${e.message}")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}