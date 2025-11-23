package com.example.banhangapp.models;

import com.example.banhangapp.utils.CustomerIdDeserializer;
import com.example.banhangapp.utils.ProductIdDeserializer;
import com.example.banhangapp.utils.SellerIdDeserializer;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class Order {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("customerId")
    @JsonAdapter(CustomerIdDeserializer.class)
    private CustomerInfo customerId;
    
    @SerializedName("sellerId")
    @JsonAdapter(SellerIdDeserializer.class)
    private SellerInfo sellerId;  // Can be SellerInfo object or null
    
    @SerializedName("items")
    private List<OrderItem> items;
    
    @SerializedName("totalAmount")
    private double totalAmount;
    
    @SerializedName("discountAmount")
    private double discountAmount;
    
    @SerializedName("finalAmount")
    private double finalAmount;
    
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    @SerializedName("deliveryAddress")
    private String deliveryAddress;
    
    @SerializedName("status")
    private String status; // "pending", "confirmed", "shipping", "delivered", "cancelled"
    
    @SerializedName("promotionId")
    private String promotionId;
    
    @SerializedName("createdAt")
    private Date createdAt;
    
    @SerializedName("updatedAt")
    private Date updatedAt;

    public static class OrderItem {
        @SerializedName("productId")
        @JsonAdapter(ProductIdDeserializer.class)
        private Product productId;  // Can be Product object or String ID
        
        @SerializedName("quantity")
        private int quantity;
        
        @SerializedName("price")
        private double price;

        public Product getProductId() { return productId; }
        public void setProductId(Product productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public CustomerInfo getCustomerId() { return customerId; }
    public void setCustomerId(CustomerInfo customerId) { this.customerId = customerId; }
    
    // Helper method to get customer ID as string
    public String getCustomerIdString() {
        if (customerId == null) return null;
        return customerId.getId();
    }

    public SellerInfo getSellerId() { return sellerId; }
    public void setSellerId(SellerInfo sellerId) { this.sellerId = sellerId; }
    
    // Helper method to get seller ID as string
    public String getSellerIdString() {
        if (sellerId == null) return null;
        return sellerId.getId();
    }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }
}

