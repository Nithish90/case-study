package com.trendyol.framework;


import java.util.HashMap;
import java.util.Map;

public class Environment extends JsonObject {

    public String id;
    public String name;

    public Map<String, String> properties;

    public Environment() {
        properties = new HashMap<String,String>();
    }

    public Environment(String id) {
        this();
        this.id = id;
    }
}
