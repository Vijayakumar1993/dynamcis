package org.dynamics.model;

import java.io.Serializable;
import java.util.Vector;

public class Person implements Serializable {
    private int id;
    private String name;
    private Gender gender;
    private Categories categories;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", categories=" + categories +
                '}';
    }

    public Vector<String> toVector(){
        Vector<String> vector = new Vector<>();
        vector.add(this.id+"");
        vector.add(this.name);
        vector.add(this.gender.toString());
        vector.add(this.categories.toString());
        return vector;
    }
    public static Vector<String> keys(){
        Vector<String> keys = new Vector<>();
        keys.add("Id");
        keys.add("Name");
        keys.add("Gender");
        keys.add("Category");
        return keys;
    }

}
