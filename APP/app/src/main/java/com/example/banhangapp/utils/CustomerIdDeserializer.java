package com.example.banhangapp.utils;

import com.example.banhangapp.models.CustomerInfo;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class CustomerIdDeserializer implements JsonDeserializer<CustomerInfo> {
    @Override
    public CustomerInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            // If it's a string ID, create a CustomerInfo with just the ID
            CustomerInfo customerInfo = new CustomerInfo();
            customerInfo.setId(json.getAsString());
            return customerInfo;
        }
        if (json.isJsonObject()) {
            return context.deserialize(json, CustomerInfo.class);
        }
        return null;
    }
}

