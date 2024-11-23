package org.dynamics.model;

import java.io.Serializable;

public class Event implements Serializable {
    private Integer id;
    private String eventName;
    private String teamName;
    private String description;
    private Fixture fixture;
    private Matcher matcher;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public Fixture getFixture() {
        return fixture;
    }

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }
    public Boolean isValid(){
        return  !this.teamName.isEmpty() && !this.eventName.isEmpty();
    }
}
