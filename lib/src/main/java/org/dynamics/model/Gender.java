package org.dynamics.model;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Gender {
    MALE,FEMALE;
    public Gender toValue(String  data){
        return Arrays.stream(Gender.values()).filter(a->a.toString().toLowerCase().equalsIgnoreCase(data)).collect(Collectors.toList()).get(0);
    }
    }
