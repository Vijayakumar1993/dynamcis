package org.dynamics.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Vector;

public class Person implements Serializable {
    private long id;
    private String name;
    private Gender gender;
    private Categories categories;
    private Double weight = 0d;
    private String teamName;

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

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
        vector.add(this.teamName);
        return vector;
    }

    public Boolean isValid(){
        return  !this.teamName.isEmpty() && !this.name.isEmpty() && !this.gender.toString().isEmpty() && !this.categories.toString().isEmpty() && !this.weight.toString().isEmpty()  ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static Vector<String> keys(){
        Vector<String> keys = new Vector<>();
        keys.add("Id");
        keys.add("Name");
        keys.add("Gender");
        keys.add("Category");
        keys.add("Weight");
        keys.add("Team Name");
        return keys;
    }

}
