package org.dynamics.model;

import java.io.Serializable;

public class Match  implements Serializable {
    private Person from;
    private Corner fromCorner;
    private Person to;
    private Corner toCorner;
    private Person successor = new Person();

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
    }

    public Person getTo() {
        return to;
    }

    public Corner getFromCorner() {
        return fromCorner;
    }

    public void setFromCorner(Corner fromCorner) {
        this.fromCorner = fromCorner;
    }

    public Corner getToCorner() {
        return toCorner;
    }

    public void setToCorner(Corner toCorner) {
        this.toCorner = toCorner;
    }

    public Person getSuccessor() {
        return successor;
    }

    public void setSuccessor(Person successor) {
        this.successor = successor;
    }

    public void setTo(Person to) {
        this.to = to;
    }
}
