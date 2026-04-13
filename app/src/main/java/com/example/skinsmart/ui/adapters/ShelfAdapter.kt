package com.example.skinsmart.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skinsmart.R
import com.example.skinsmart.data.local.ShelfProduct
import com.squareup.picasso.Picasso

class ShelfAdapter(
    private var shelfItems: List<ShelfProduct>,
    private val onDeleteClicked: (ShelfProduct) -> Unit
) : RecyclerView.Adapter<ShelfAdapter.ShelfViewHolder>() {

    inner class ShelfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.ivShelfImage)
        val name: TextView = itemView.findViewById(R.id.tvShelfName)
        val brand: TextView = itemView.findViewById(R.id.tvShelfBrand)
        val note: TextView = itemView.findViewById(R.id.tvShelfNote)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteShelf)

        fun bind(product: ShelfProduct) {
            name.text = product.name
            brand.text = product.brand ?: "Unknown Brand"
            note.text = if(product.privateNote.isNullOrEmpty()) "No notes yet" else product.privateNote
            
            if (!product.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(product.imageUrl).into(image)
            } else {
                image.setImageResource(R.drawable.ic_launcher_background)
            }

            btnDelete.setOnClickListener { onDeleteClicked(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shelf_product, parent, false)
        return ShelfViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
        holder.bind(shelfItems[position])
    }

    override fun getItemCount() = shelfItems.size

    fun updateData(newItems: List<ShelfProduct>) {
        shelfItems = newItems
        notifyDataSetChanged()
    }
}
