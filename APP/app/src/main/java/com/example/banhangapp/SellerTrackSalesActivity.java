package com.example.banhangapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.banhangapp.adapter.OrderAdapter;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Order;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerTrackSalesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_track_sales);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        adapter = new OrderAdapter(new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                // Show order details
            }

            @Override
            public void onOrderStatusChange(Order order, String newStatus) {
                updateOrderStatus(order, newStatus);
            }
        }, true); // Allow status update for sellers
        recyclerView.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<Order>> call = apiService.getOrders(token);
        
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    android.util.Log.d("SellerOrders", "Loaded " + orders.size() + " orders");
                    adapter.updateOrders(orders);
                    Toast.makeText(SellerTrackSalesActivity.this, "Đã tải " + orders.size() + " đơn hàng", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Lỗi tải dữ liệu: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                    }
                    android.util.Log.e("SellerOrders", errorMsg);
                    Toast.makeText(SellerTrackSalesActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                android.util.Log.e("SellerOrders", "Network error", t);
                Toast.makeText(SellerTrackSalesActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void updateOrderStatus(Order order, String newStatus) {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", newStatus);

        Call<Order> call = apiService.updateOrderStatus(token, order.getId(), statusMap);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SellerTrackSalesActivity.this, "Đã cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
                    loadOrders(); // Reload list
                } else {
                    String errorMsg = "Lỗi cập nhật trạng thái";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += ": " + response.message();
                    }
                    Toast.makeText(SellerTrackSalesActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                android.util.Log.e("SellerOrders", "Network error updating order status", t);
                Toast.makeText(SellerTrackSalesActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

