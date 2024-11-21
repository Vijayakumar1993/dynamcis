package org.dynamics.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Fixture  implements Serializable {
    private List<Person> persons;

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        List<Person> serializableSublit = new LinkedList<>();
        serializableSublit.addAll(persons);
        this.persons = serializableSublit;
    }
}
