package org.dynamics.model;

import java.io.Serializable;
import java.util.Vector;

public class Person implements Serializable {
    private long id;
    private String name;
    private Gender gender;
    private Categories categories;
    private Double weight = 0d;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Categories getCategories() {
        return categories;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", categories=" + categories +
                ", weight=" + weight +
                '}';
    }

    public Vector<Object> toVector(){
        Vector<Object> vector = new Vector<>();
        vector.add(this.id+"");
        vector.add(this.name);
        vector.add(this.gender.toString());
        vector.add(this.categories.toString());
        vector.add(this.weight.toString());
        return vector;
    }
    public static Vector<String> keys(){
        Vector<String> keys = new Vector<>();
        keys.add("Id");
        keys.add("Name");
        keys.add("Gender");
        keys.add("Category");
        keys.add("Weight");
        return keys;
    }

}
