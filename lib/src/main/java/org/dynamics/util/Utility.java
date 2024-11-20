package org.dynamics.util;

import org.dynamics.model.Person;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;

public class Utility {
    public static Integer getRandom(){
        Random random = new Random();
        return random.nextInt();
    }

    public Optional<List<Person>> filter(List<Person> persons){
        return Optional.of(persons);
    }

    public static Vector<Vector<String>> converter(List<Person> persons){
        return new Vector<>(persons.stream().map(a->a.toVector()).collect(Collectors.toList()));
    }
}
