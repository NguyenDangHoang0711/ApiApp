package com.example.banhangapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.R;
import com.example.banhangapp.models.Order;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;
    private boolean allowStatusUpdate; // Flag to control if status update button should be shown

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onOrderStatusChange(Order order, String newStatus);
    }

    public OrderAdapter(OnOrderClickListener listener) {
        this(listener, false); // Default: don't allow status update
    }

    public OrderAdapter(OnOrderClickListener listener, boolean allowStatusUpdate) {
        this.orders = new ArrayList<>();
        this.listener = listener;
        this.allowStatusUpdate = allowStatusUpdate;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        if (orders != null && position < orders.size()) {
            holder.bind(orders.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders != null ? newOrders : new ArrayList<>();
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId;
        private TextView tvTotalAmount;
        private com.google.android.material.chip.Chip tvStatus;
        private TextView tvDate;
        private TextView tvItemCount;
        private com.google.android.material.button.MaterialButton btnUpdateStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }

        public void bind(Order order) {
            if (tvOrderId != null) {
                String orderId = order.getId();
                if (orderId != null && orderId.length() > 8) {
                    tvOrderId.setText("Đơn hàng: " + orderId.substring(0, 8) + "...");
                } else {
                    tvOrderId.setText("Đơn hàng: " + orderId);
                }
            }

            if (tvTotalAmount != null) {
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvTotalAmount.setText(format.format(order.getFinalAmount()));
            }

            if (tvStatus != null) {
                String status = order.getStatus() != null ? order.getStatus() : "pending";
                String statusText = "";
                int chipBackgroundColor = R.color.info_light;
                int chipStrokeColor = R.color.info;
                
                switch (status) {
                    case "pending":
                        statusText = "⏳ Chờ xử lý";
                        chipBackgroundColor = R.color.warning_light;
                        chipStrokeColor = R.color.warning;
                        break;
                    case "confirmed":
                        statusText = "✅ Đã xác nhận";
                        chipBackgroundColor = R.color.info_light;
                        chipStrokeColor = R.color.info;
                        break;
                    case "shipping":
                        statusText = "🚚 Đang giao";
                        chipBackgroundColor = R.color.info_light;
                        chipStrokeColor = R.color.info;
                        break;
                    case "delivered":
                        statusText = "🎉 Đã giao";
                        chipBackgroundColor = R.color.success_light;
                        chipStrokeColor = R.color.success;
                        break;
                    case "cancelled":
                        statusText = "❌ Đã hủy";
                        chipBackgroundColor = R.color.error_light;
                        chipStrokeColor = R.color.error;
                        break;
                }
                tvStatus.setText(statusText);
                tvStatus.setChipBackgroundColorResource(chipBackgroundColor);
                tvStatus.setChipStrokeColorResource(chipStrokeColor);
            }

            if (tvDate != null && order.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvDate.setText(sdf.format(order.getCreatedAt()));
            }

            if (tvItemCount != null && order.getItems() != null) {
                int totalItems = 0;
                for (Order.OrderItem item : order.getItems()) {
                    totalItems += item.getQuantity();
                }
                tvItemCount.setText(totalItems + " sản phẩm");
            }

            if (btnUpdateStatus != null) {
                // Hide status update button for customers
                if (!allowStatusUpdate) {
                    btnUpdateStatus.setVisibility(View.GONE);
                } else {
                    // Only show for sellers/admins
                    String currentStatus = order.getStatus();
                    if ("pending".equals(currentStatus)) {
                        btnUpdateStatus.setText("Xác nhận");
                        btnUpdateStatus.setVisibility(View.VISIBLE);
                        btnUpdateStatus.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onOrderStatusChange(order, "confirmed");
                            }
                        });
                    } else if ("confirmed".equals(currentStatus)) {
                        btnUpdateStatus.setText("Bắt đầu giao");
                        btnUpdateStatus.setVisibility(View.VISIBLE);
                        btnUpdateStatus.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onOrderStatusChange(order, "shipping");
                            }
                        });
                    } else if ("shipping".equals(currentStatus)) {
                        btnUpdateStatus.setText("Hoàn thành");
                        btnUpdateStatus.setVisibility(View.VISIBLE);
                        btnUpdateStatus.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onOrderStatusChange(order, "delivered");
                            }
                        });
                    } else {
                        btnUpdateStatus.setVisibility(View.GONE);
                    }
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }
    }
}

