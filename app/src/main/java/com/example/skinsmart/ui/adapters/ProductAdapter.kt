package com.example.skinsmart.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skinsmart.R
import com.example.skinsmart.data.network.MakeupProduct
import com.squareup.picasso.Picasso

class ProductAdapter(
    private var products: List<MakeupProduct>,
    private val onReviewClicked: (MakeupProduct) -> Unit,
    private val onSaveClicked: (MakeupProduct) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val productName: TextView = itemView.findViewById(R.id.tvProductName)
        val productBrand: TextView = itemView.findViewById(R.id.tvProductBrand)
        val productPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnReview: Button = itemView.findViewById(R.id.btnPostReview)
        val btnSave: Button = itemView.findViewById(R.id.btnSaveShelf)

        fun bind(product: MakeupProduct) {
            productName.text = product.name
            productBrand.text = product.brand ?: "Unknown Brand"
            productPrice.text = "$${product.price ?: "0.00"}"
            
            if (!product.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(product.imageUrl).into(productImage)
            }

            btnReview.setOnClickListener { onReviewClicked(product) }
            btnSave.setOnClickListener { onSaveClicked(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_makeup_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateData(newProducts: List<MakeupProduct>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
