package com.example.banhangapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.adapter.CartAdapter;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Cart;
import com.example.banhangapp.models.CartItem;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvTotal;
    private Button btnCheckout;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private Cart cart;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_cart);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        recyclerView = findViewById(R.id.recyclerViewCart);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        View emptyState = findViewById(R.id.emptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        
        // Initialize adapter
        adapter = new CartAdapter(
            (item, newQuantity) -> updateCartItemQuantity(item, newQuantity),
            this::removeCartItem
        );
        recyclerView.setAdapter(adapter);
        
        // Log RecyclerView setup
        recyclerView.post(() -> {
            android.util.Log.d("CartActivity", "RecyclerView dimensions: " + 
                recyclerView.getWidth() + "x" + recyclerView.getHeight());
        });

        btnCheckout.setOnClickListener(v -> {
            if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) {
                Intent intent = new Intent(this, CustomerCheckoutActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            }
        });

        loadCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Call<Cart> call = apiService.getCart(token);
        
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cart = response.body();
                    android.util.Log.d("CartActivity", "Cart loaded with " + 
                        (cart.getItems() != null ? cart.getItems().size() : 0) + " items");
                    updateUI();
                } else {
                    String errorMsg = "Lỗi tải giỏ hàng: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                    }
                    android.util.Log.e("CartActivity", errorMsg);
                    Toast.makeText(CustomerCartActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                android.util.Log.e("CartActivity", "Network error loading cart", t);
                Toast.makeText(CustomerCartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void updateUI() {
        View emptyState = findViewById(R.id.emptyState);
        
        if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) {
            // Update adapter with cart items
            adapter.updateCartItems(cart.getItems());
            android.util.Log.d("CartActivity", "Adapter updated with " + cart.getItems().size() + " items");
            
            // Show RecyclerView, hide empty state
            recyclerView.setVisibility(View.VISIBLE);
            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
            
            // Calculate total
            double total = 0;
            for (CartItem item : cart.getItems()) {
                if (item.getProductId() != null) {
                    total += item.getProductId().getPrice() * item.getQuantity();
                }
            }
            tvTotal.setText(String.format("%,.0f đ", total));
        } else {
            adapter.updateCartItems(new java.util.ArrayList<>());
            tvTotal.setText("0 đ");
            
            // Hide RecyclerView, show empty state
            recyclerView.setVisibility(View.GONE);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
            
            android.util.Log.d("CartActivity", "Cart is empty, showing empty state");
        }
    }
    
    private void updateCartItemQuantity(CartItem item, int newQuantity) {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (item.getProductId() == null || item.getProductId().getId() == null) {
            Toast.makeText(this, "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        java.util.Map<String, Integer> quantityMap = new java.util.HashMap<>();
        quantityMap.put("quantity", newQuantity);
        
        Call<Cart> call = apiService.updateCartItem(token, item.getProductId().getId(), quantityMap);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cart = response.body();
                    updateUI();
                } else {
                    Toast.makeText(CustomerCartActivity.this, "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Toast.makeText(CustomerCartActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void removeCartItem(CartItem item) {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (item.getProductId() == null || item.getProductId().getId() == null) {
            Toast.makeText(this, "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Call<Cart> call = apiService.removeFromCart(token, item.getProductId().getId());
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cart = response.body();
                    Toast.makeText(CustomerCartActivity.this, "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                    updateUI(); // Update UI with new cart
                } else {
                    Toast.makeText(CustomerCartActivity.this, "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                android.util.Log.e("CartActivity", "Error removing item", t);
                Toast.makeText(CustomerCartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

