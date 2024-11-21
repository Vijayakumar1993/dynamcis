package org.dynamics.model;

import java.io.Serializable;

public class Match  implements Serializable {
    private Person from;
    private Person to;

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
    }

    public Person getTo() {
        return to;
    }

    public void setTo(Person to) {
        this.to = to;
    }
}
