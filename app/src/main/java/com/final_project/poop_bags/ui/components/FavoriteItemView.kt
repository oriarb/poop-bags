import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.final_project.poop_bags.data.models.Post
import com.final_project.poop_bags.databinding.ViewFavoriteItemBinding
import com.google.android.material.card.MaterialCardView


class FavoriteItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private var binding: ViewFavoriteItemBinding =
        ViewFavoriteItemBinding.inflate(LayoutInflater.from(context), this)

    @SuppressLint("SetTextI18n")
    fun bind(post: Post, onFavoriteClick: (Post) -> Unit) {
        binding.apply {
            titleText.text = post.title
            likesCount.text = post.likesCount.toString()
            commentsCount.text = post.commentsCount.toString()
            
            Glide.with(context)
                .load(post.imageUrl)
                .centerCrop()
                .into(postImage)
                
            favoriteButton.setOnClickListener {
                onFavoriteClick(post)
            }
        }
    }
} 