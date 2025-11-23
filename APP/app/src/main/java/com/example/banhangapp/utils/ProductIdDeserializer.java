package com.example.banhangapp.utils;

import com.example.banhangapp.models.Product;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class ProductIdDeserializer implements JsonDeserializer<Product> {
    @Override
    public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            // If it's a string ID, create a Product with just the ID
            Product product = new Product();
            product.setId(json.getAsString());
            return product;
        }
        if (json.isJsonObject()) {
            return context.deserialize(json, Product.class);
        }
        return null;
    }
}

