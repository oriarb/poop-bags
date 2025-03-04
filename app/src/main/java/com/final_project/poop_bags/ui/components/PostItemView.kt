import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.final_project.poop_bags.data.models.Post
import com.final_project.poop_bags.databinding.ViewPostItemBinding
import com.google.android.material.card.MaterialCardView

class PostItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private var binding: ViewPostItemBinding =
        ViewPostItemBinding.inflate(LayoutInflater.from(context), this)

    data class ViewConfig(
        val isFavorite: Boolean = false,
        val isDelete: Boolean = false,
        val isEdit: Boolean = false
    )

    @SuppressLint("SetTextI18n")
    fun bind(
        post: Post,
        config: ViewConfig,
        onFavoriteClick: ((Post) -> Unit)? = null,
        onDeleteClick: ((Post) -> Unit)? = null,
        onEditClick: ((Post) -> Unit)? = null
    ) {
        binding.apply {
            titleText.text = post.title
            likesCount.text = post.likesCount.toString()
            commentsCount.text = post.commentsCount.toString()
            
            Glide.with(context)
                .load(post.imageUrl)
                .centerCrop()
                .into(postImage)

            // Configure buttons visibility and click listeners
            favoriteButton.apply {
                visibility = if (config.isFavorite) View.VISIBLE else View.GONE
                setOnClickListener { onFavoriteClick?.invoke(post) }
            }

            deleteButton.apply {
                visibility = if (config.isDelete) View.VISIBLE else View.GONE
                setOnClickListener { onDeleteClick?.invoke(post) }
            }

            editButton.apply {
                visibility = if (config.isEdit) View.VISIBLE else View.GONE
                setOnClickListener { onEditClick?.invoke(post) }
            }
        }
    }
} 