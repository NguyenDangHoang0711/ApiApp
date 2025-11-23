package com.example.banhangapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Cart;
import com.example.banhangapp.models.Order;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCheckoutActivity extends AppCompatActivity {
    private EditText etAddress, etPromotionCode;
    private RadioGroup rgPaymentMethod;
    private Button btnPlaceOrder;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_checkout);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        etAddress = findViewById(R.id.etAddress);
        etPromotionCode = findViewById(R.id.etPromotionCode);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String address = etAddress.getText().toString().trim();
        String promotionCode = etPromotionCode.getText().toString().trim();
        
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        String paymentMethod = "cash"; // default
        if (selectedId == R.id.rbCard) {
            paymentMethod = "card";
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // First verify cart has items before placing order
        android.util.Log.d("CheckoutActivity", "Verifying cart before placing order...");
        verifyCartBeforeOrder(token, paymentMethod, address, promotionCode);
    }
    
    private void verifyCartBeforeOrder(String token, String paymentMethod, String address, String promotionCode) {
        Call<com.example.banhangapp.models.Cart> cartCall = apiService.getCart(token);
        cartCall.enqueue(new Callback<com.example.banhangapp.models.Cart>() {
            @Override
            public void onResponse(Call<com.example.banhangapp.models.Cart> call, Response<com.example.banhangapp.models.Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.banhangapp.models.Cart cart = response.body();
                    int itemCount = cart.getItems() != null ? cart.getItems().size() : 0;
                    android.util.Log.d("CheckoutActivity", "Cart verified. Item count: " + itemCount);
                    
                    if (itemCount == 0) {
                        Toast.makeText(CustomerCheckoutActivity.this, "Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    // Cart has items, proceed with order
                    placeOrderRequest(token, paymentMethod, address, promotionCode);
                } else {
                    String errorMsg = "Lỗi kiểm tra giỏ hàng: Code " + response.code();
                    android.util.Log.e("CheckoutActivity", errorMsg);
                    Toast.makeText(CustomerCheckoutActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.banhangapp.models.Cart> call, Throwable t) {
                android.util.Log.e("CheckoutActivity", "Error verifying cart", t);
                Toast.makeText(CustomerCheckoutActivity.this, "Lỗi kiểm tra giỏ hàng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void placeOrderRequest(String token, String paymentMethod, String address, String promotionCode) {
        android.util.Log.d("CheckoutActivity", "Placing order with: paymentMethod=" + paymentMethod + ", address=" + address + ", promotionCode=" + promotionCode);
        
        ApiService.OrderRequest request = new ApiService.OrderRequest(paymentMethod, address, promotionCode);
        Call<Order> call = apiService.createOrder(token, request);

        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    android.util.Log.d("CheckoutActivity", "Order created successfully: " + order.getId());
                    Toast.makeText(CustomerCheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Lỗi đặt hàng: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                    }
                    android.util.Log.e("CheckoutActivity", errorMsg);
                    Toast.makeText(CustomerCheckoutActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                android.util.Log.e("CheckoutActivity", "Network error placing order", t);
                Toast.makeText(CustomerCheckoutActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
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

