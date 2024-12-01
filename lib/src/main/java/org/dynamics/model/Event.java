package org.dynamics.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Vector;

public class Event implements Serializable {
    private Long id;
    private Categories selecetedEventCategory;
    private Gender selectedGenderCategory;
    private String eventName;
    private String teamName;
    private String description;
    private Fixture fixture;
    private Matcher matcher;
    private Event parentEvent;
    private LocalDate eventDate;

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public Categories getSelecetedEventCategory() {
        return selecetedEventCategory;
    }

    public void setSelecetedEventCategory(Categories selecetedEventCategory) {
        this.selecetedEventCategory = selecetedEventCategory;
    }

    public Gender getSelectedGenderCategory() {
        return selectedGenderCategory;
    }

    public void setSelectedGenderCategory(Gender selectedGenderCategory) {
        this.selectedGenderCategory = selectedGenderCategory;
    }

    public Event getParentEvent() {
        return parentEvent;
    }

    public void setParentEvent(Event parentEvent) {
        this.parentEvent = parentEvent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Vector<Object> toVector(){
        Vector<Object> vector = new Vector<>();
        vector.add(this.id+"");
        if(this.getParentEvent()!=null){
            vector.add(this.getParentEvent().getEventName());
        }else{
            vector.add("NA");
        }

        vector.add(this.getTeamName());
        vector.add(this.getEventName());
        vector.add(this.getDescription());
        vector.add(this.getEventDate().toString());
        vector.add(this.matcher.getMatches().size());
        vector.add(this.getFixture().getPersons().size());
        if(this.matcher.getWinner()!=null){
            vector.add(this.matcher.getWinner().getName());
        }else{
            vector.add("Not Yet Decided");
        }
        return vector;
    }
    public static Vector<String> keys(){
        Vector<String> keys = new Vector<>();
        keys.add("Id");
        keys.add("Event initiated from");
        keys.add("Category Name");
        keys.add("Weight Category");
        keys.add("Description");
        keys.add("Event Date");
        keys.add("Total Matches");
        keys.add("Total Fixtures");
        keys.add("Winner");
        return keys;
    }
}
