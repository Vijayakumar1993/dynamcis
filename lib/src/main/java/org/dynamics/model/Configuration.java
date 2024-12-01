package org.dynamics.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Configuration implements Serializable {
    private Map<String, Object> keys = new LinkedHashMap<>();

    public Map<String, Object> getKeys() {
        return keys;
    }

    public void setKeys(Map<String, Object> keys) {
        this.keys = keys;
    }

    public Object get(String key){
        return keys.get(key);
    }

    public Vector<Object> toVector(){
        Vector<Object> vector = new Vector<>();
        this.keys.forEach((key,value)->{
            vector.add(value);
        });
        return vector;
    }

    public  static Vector<String> keys(){
        Vector<String> keys = new Vector<>();
        keys.add("Key");
        keys.add("Value");
        return keys;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "keys=" + keys +
                '}';
    }
}
