package com.example.banhangapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.banhangapp.R;
import com.example.banhangapp.models.CartItem;
import com.example.banhangapp.models.Product;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnQuantityChangeListener onQuantityChangeListener;
    private OnRemoveItemListener onRemoveItemListener;

    public interface OnQuantityChangeListener {
        void onQuantityChange(CartItem item, int newQuantity);
    }

    public interface OnRemoveItemListener {
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(OnQuantityChangeListener onQuantityChangeListener, 
                       OnRemoveItemListener onRemoveItemListener) {
        this.onQuantityChangeListener = onQuantityChangeListener;
        this.onRemoveItemListener = onRemoveItemListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("CartAdapter", "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        CartViewHolder holder = new CartViewHolder(view);
        android.util.Log.d("CartAdapter", "ViewHolder created");
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        android.util.Log.d("CartAdapter", "onBindViewHolder called for position: " + position);
        if (cartItems != null && position < cartItems.size()) {
            try {
                CartItem item = cartItems.get(position);
                android.util.Log.d("CartAdapter", "Binding item: " + 
                    (item.getProductId() != null ? item.getProductId().getName() : "null") + 
                    " at position " + position);
                holder.bind(item);
            } catch (Exception e) {
                android.util.Log.e("CartAdapter", "Error binding item at position " + position, e);
                e.printStackTrace();
            }
        } else {
            android.util.Log.w("CartAdapter", "Cannot bind: cartItems=" + 
                (cartItems != null ? cartItems.size() : "null") + ", position=" + position);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateCartItems(List<CartItem> newItems) {
        this.cartItems = newItems != null ? newItems : new java.util.ArrayList<>();
        android.util.Log.d("CartAdapter", "Updating cart items: " + this.cartItems.size() + " items");
        notifyDataSetChanged();
        android.util.Log.d("CartAdapter", "notifyDataSetChanged called. Item count: " + getItemCount());
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;
        private TextView productName;
        private TextView productPrice;
        private TextView productQuantity;
        private com.google.android.material.button.MaterialButton btnDecrease;
        private com.google.android.material.button.MaterialButton btnIncrease;
        private com.google.android.material.button.MaterialButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(CartItem cartItem) {
            try {
                Product product = cartItem.getProductId();
                android.util.Log.d("CartAdapter", "Binding cart item. Product: " + 
                    (product != null ? product.getName() : "null"));
                
                if (product != null) {
                    if (productName != null) {
                        productName.setText(product.getName() != null ? product.getName() : "N/A");
                    } else {
                        android.util.Log.e("CartAdapter", "productName TextView is null!");
                    }
                    
                    if (productPrice != null) {
                        productPrice.setText(formatPrice(product.getPrice()));
                    } else {
                        android.util.Log.e("CartAdapter", "productPrice TextView is null!");
                    }
                    
                    if (productQuantity != null) {
                        productQuantity.setText(String.valueOf(cartItem.getQuantity()));
                    } else {
                        android.util.Log.e("CartAdapter", "productQuantity TextView is null!");
                    }

                // Handle image
                if (product.getImages() != null && product.getImages().length > 0 && 
                    product.getImages()[0] != null && !product.getImages()[0].isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(product.getImages()[0])
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(productImage);
                } else {
                    productImage.setImageResource(R.drawable.placeholder_image);
                }

                // Quantity controls
                btnDecrease.setOnClickListener(v -> {
                    int newQuantity = Math.max(1, cartItem.getQuantity() - 1);
                    if (onQuantityChangeListener != null) {
                        onQuantityChangeListener.onQuantityChange(cartItem, newQuantity);
                    }
                });

                btnIncrease.setOnClickListener(v -> {
                    int newQuantity = cartItem.getQuantity() + 1;
                    if (onQuantityChangeListener != null) {
                        onQuantityChangeListener.onQuantityChange(cartItem, newQuantity);
                    }
                });

                // Remove button
                if (btnRemove != null) {
                    btnRemove.setOnClickListener(v -> {
                        if (onRemoveItemListener != null) {
                            onRemoveItemListener.onRemoveItem(cartItem);
                        }
                    });
                }
                
                android.util.Log.d("CartAdapter", "Cart item bound successfully: " + product.getName());
            } else {
                android.util.Log.e("CartAdapter", "Product is null in cart item!");
            }
            } catch (Exception e) {
                android.util.Log.e("CartAdapter", "Error in bind()", e);
                e.printStackTrace();
            }
        }

        private String formatPrice(double price) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return format.format(price);
        }
    }
}

