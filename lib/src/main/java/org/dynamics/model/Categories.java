package org.dynamics.model;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Categories {
    KID,SUBJUNIOR,JUNIOR, SENIOR;

    public Categories toValue(String  data){
        return Arrays.stream(Categories.values()).filter(a->a.toString().equalsIgnoreCase(data)).collect(Collectors.toList()).get(0);
    }
}
