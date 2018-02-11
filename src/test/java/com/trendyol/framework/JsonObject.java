package com.trendyol.framework;

import com.google.gson.GsonBuilder;

public class JsonObject {
    public String toJSON() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
