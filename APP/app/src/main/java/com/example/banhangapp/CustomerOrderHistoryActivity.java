package com.example.banhangapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.adapter.OrderAdapter;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Order;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerOrderHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        recyclerView = findViewById(R.id.recyclerViewOrders);
        emptyState = findViewById(R.id.emptyState);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        
        // Initialize adapter
        adapter = new OrderAdapter(new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                // Handle order click - could show order details
                Toast.makeText(CustomerOrderHistoryActivity.this, "Đơn hàng: " + order.getId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onOrderStatusChange(Order order, String newStatus) {
                // Handle status change - customers typically can't change status
                Toast.makeText(CustomerOrderHistoryActivity.this, "Không thể thay đổi trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
        
        // Initialize with empty list
        adapter.updateOrders(new ArrayList<>());

        loadOrders();
    }

    private void loadOrders() {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("OrderHistory", "Loading orders with token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        Call<List<Order>> call = apiService.getOrders(token);
        
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    android.util.Log.d("OrderHistory", "Received " + orders.size() + " orders");
                    
                    // Update adapter with orders
                    adapter.updateOrders(orders);
                    
                    // Update UI visibility
                    if (orders.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        if (emptyState != null) {
                            emptyState.setVisibility(View.VISIBLE);
                        }
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        if (emptyState != null) {
                            emptyState.setVisibility(View.GONE);
                        }
                    }
                } else {
                    String errorMsg = "Lỗi tải đơn hàng: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                    }
                    android.util.Log.e("OrderHistory", errorMsg);
                    Toast.makeText(CustomerOrderHistoryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                String errorMsg = "Lỗi kết nối: ";
                if (t.getMessage() != null) {
                    errorMsg += t.getMessage();
                } else {
                    errorMsg += "Không thể kết nối đến server";
                }
                android.util.Log.e("OrderHistory", "Network error loading orders", t);
                Toast.makeText(CustomerOrderHistoryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

