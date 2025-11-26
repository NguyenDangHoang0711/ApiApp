package com.example.banhangapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Product;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProductDetailActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvDescription, tvCategory, tvBrand, tvColor, tvSize, tvQuantity;
    private ImageView ivProduct;
    private Button btnAddToCart;
    private String productId;
    private ApiService apiService;
    private SharedPreferencesHelper prefsHelper;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_product_detail);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvBrand = findViewById(R.id.tvBrand);
        tvColor = findViewById(R.id.tvColor);
        tvSize = findViewById(R.id.tvSize);
        tvQuantity = findViewById(R.id.tvQuantity);
        ivProduct = findViewById(R.id.ivProduct);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        btnAddToCart.setOnClickListener(v -> addToCart());

        loadProduct();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProduct() {
        Call<Product> call = apiService.getProductById(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    displayProduct(product);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(CustomerProductDetailActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProduct(Product product) {
        // Save product for addToCart
        currentProduct = product;
        
        if (tvName != null) {
            tvName.setText(product.getName() != null ? product.getName() : "N/A");
        }
        
        if (tvPrice != null) {
            tvPrice.setText(String.format("%,.0f VNĐ", product.getPrice()));
        }
        
        if (tvDescription != null) {
            tvDescription.setText(product.getDescription() != null ? product.getDescription() : "Không có mô tả");
        }
        
        if (tvCategory != null) {
            tvCategory.setText("Danh mục: " + (product.getCategory() != null ? product.getCategory() : "N/A"));
        }
        
        if (tvBrand != null) {
            tvBrand.setText("Thương hiệu: " + (product.getBrand() != null ? product.getBrand() : "N/A"));
        }
        
        if (tvColor != null) {
            tvColor.setText("Màu sắc: " + (product.getColor() != null ? product.getColor() : "N/A"));
        }
        
        if (tvSize != null) {
            tvSize.setText("Kích cỡ: " + (product.getSize() != null ? product.getSize() : "N/A"));
        }
        
        if (tvQuantity != null) {
            tvQuantity.setText("Số lượng: " + product.getQuantity());
        }
        
        // Load product image
        if (ivProduct != null) {
            if (product.getImages() != null && product.getImages().length > 0 && 
                product.getImages()[0] != null && !product.getImages()[0].isEmpty()) {
                // Load image from URL using Glide
                Glide.with(this)
                    .load(product.getImages()[0])
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(ivProduct);
            } else {
                // Use placeholder if no image URL
                ivProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
        
        if (btnAddToCart != null) {
            btnAddToCart.setEnabled(product.isInStock() && product.getQuantity() > 0);
        }
    }

    private void addToCart() {
        if (currentProduct == null) {
            Toast.makeText(this, "Sản phẩm chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String token = prefsHelper.getToken();
        
        if (token == null || token.isEmpty()) {
            android.util.Log.e("ProductDetail", "Token is null or empty");
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentProduct.getId() == null || currentProduct.getId().isEmpty()) {
            android.util.Log.e("ProductDetail", "Product ID is null or empty");
            Toast.makeText(this, "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable button while adding to cart
        if (btnAddToCart != null) {
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Đang thêm...");
        }
        
        android.util.Log.d("ProductDetail", "Adding product to cart: " + currentProduct.getName() + " (ID: " + currentProduct.getId() + ")");
        
        ApiService.CartItemRequest request = new ApiService.CartItemRequest(currentProduct.getId(), 1);
        Call<com.example.banhangapp.models.Cart> call = apiService.addToCart(token, request);
        
        call.enqueue(new Callback<com.example.banhangapp.models.Cart>() {
            @Override
            public void onResponse(Call<com.example.banhangapp.models.Cart> call, Response<com.example.banhangapp.models.Cart> response) {
                // Re-enable button
                if (btnAddToCart != null) {
                    btnAddToCart.setEnabled(true);
                    btnAddToCart.setText("🛒 Thêm vào giỏ hàng");
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    com.example.banhangapp.models.Cart cart = response.body();
                    android.util.Log.d("ProductDetail", "Product added to cart successfully. Cart has " + 
                        (cart.getItems() != null ? cart.getItems().size() : 0) + " items");
                    Toast.makeText(CustomerProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Lỗi thêm vào giỏ hàng: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += " - " + errorBody;
                            android.util.Log.e("ProductDetail", "Add to cart error: " + errorBody);
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                        android.util.Log.e("ProductDetail", "Error reading error body", e);
                    }
                    android.util.Log.e("ProductDetail", errorMsg);
                    Toast.makeText(CustomerProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.banhangapp.models.Cart> call, Throwable t) {
                // Re-enable button
                if (btnAddToCart != null) {
                    btnAddToCart.setEnabled(true);
                    btnAddToCart.setText("🛒 Thêm vào giỏ hàng");
                }
                
                String errorMsg = "Lỗi kết nối: ";
                if (t.getMessage() != null) {
                    errorMsg += t.getMessage();
                } else {
                    errorMsg += "Không thể kết nối đến server";
                }
                android.util.Log.e("ProductDetail", "Add to cart network error", t);
                Toast.makeText(CustomerProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}

