package com.example.banhangapp.utils;

import com.example.banhangapp.models.SellerInfo;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class SellerIdDeserializer implements JsonDeserializer<SellerInfo> {
    @Override
    public SellerInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            // If it's a string ID, create a SellerInfo with just the ID
            SellerInfo sellerInfo = new SellerInfo();
            sellerInfo.setId(json.getAsString());
            return sellerInfo;
        }
        if (json.isJsonObject()) {
            return context.deserialize(json, SellerInfo.class);
        }
        return null;
    }
}

