package org.dynamics.util;

import org.dynamics.db.Db;
import org.dynamics.model.*;
import org.dynamics.model.Event;
import org.dynamics.ui.CommonFrame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utility {

    public static Long getRandom(){
        Random random = new Random();
        return Math.abs(Long.valueOf(random.nextInt()));
    }

    public Optional<List<Person>> filter(List<Person> persons){
        return Optional.of(persons);
    }

    public static Vector<Vector<Object>> converter(List<Person> persons){
        return new Vector<>(persons.stream().map(a->a.toVector()).collect(Collectors.toList()));
    }


    public static void createEvent(List<Person> persons, Event event){
        Matcher matches = new Matcher();
        List<Map<Person, Person>> op = new LinkedList<>();
        Map<String, Integer> keyPair = fixtureAndMatcher(persons.size());
        Integer fixtureSize = keyPair.get("fixture");
        Integer matcher = keyPair.get("matcher");
        Integer roundOf = keyPair.get("roundOf");

        System.out.println("Matcher List "+matcher);
        System.out.println("Fixture List "+fixtureSize);

        List<Event> events = findListOfEvents(event);
        Optional<Integer> sumOptional = events.stream().map(a->{
            int size =0;
            Matcher matcher1 = a.getMatcher();
            if(matcher1!=null){
                size = matcher1.getMatches().size();
            }
            return size;
        }).reduce(Integer::sum);
        int appenderSize = sumOptional.orElse(0);
        List<Person> matcherList = persons.subList(0,matcher);
        List<Person> fixtureList = persons.subList(matcher,persons.size());
        List<Match> matchList = new LinkedList<>();
        for(int i=0;i<matcherList.size();i=i+2){
            appenderSize = appenderSize+1;
            Person fr = matcherList.get(i);
            Person tr = matcherList.get(i+1);
            System.out.println("From "+fr.getName()+" --- To "+tr.getName());
            Match match = new Match();
            match.setMatchId((long) (appenderSize));
            match.setFromCorner(Corner.BLUE);
            match.setToCorner(Corner.RED);
            match.setFrom(matcherList.get(i));
            match.setTo(matcherList.get(i+1));
            matchList.add(match);
        }

        //fixture matches
        for(int i=0;i<fixtureList.size();i=i+1){
            appenderSize = appenderSize+1;
            Person fr = fixtureList.get(i);
            Person tr = fixtureList.get(i);
            System.out.println("From "+fr.getName()+" --- To "+tr.getName());
            Match match = new Match();
            match.setMatchId((long)appenderSize);
            match.setFromCorner(Corner.BLUE);
            match.setToCorner(Corner.RED);
            match.setFrom(fr);
            match.setPrimary(true);
            match.setTo(tr);
            matchList.add(match);
        }
        matches.setMatches(matchList);
        Fixture fixture = new Fixture();
        List<Person> fixtures = new LinkedList<>(persons.subList(matcher,persons.size()));
        fixture.setPersons(fixtures);
        event.setRoundOf(roundOf);
        event.setMatcher(matches);
        event.setFixture(fixture);
    }

    public static Person createNaPerson(){
        Person person = new Person();
        person.setName("NA");
        return person;
    }
    public static Map<String, Integer> fixtureAndMatcher(int size){
        Function<Integer,Integer> powerOf = a-> (int)Math.pow((double) 2,a);
        int set = 2;
        Map<String, Integer> keyPair = new LinkedHashMap<>();
        for(int i=1; i<=size; i=i+1){
            set = powerOf.apply(i);
            if(set>=size){
                break;
            }
        }

        System.out.println("Chosen Set "+set);
        Integer fixture = set-size;
        Integer matcher = size-fixture;
        keyPair.put("fixture",fixture);
        keyPair.put("matcher", matcher);
        keyPair.put("roundOf", set);
        return keyPair;
    }

    public static void setPanelEnabled(JPanel panel,boolean enabled){
        for (Component comp : panel.getComponents()) {
            comp.setEnabled(enabled);
            if(!enabled){
                comp.setBackground(Color.GRAY);
                comp.setForeground(Color.GRAY);
            }
            if (comp instanceof JPanel) {
                setPanelEnabled((JPanel) comp, enabled); // Recursively handle nested panels
            }
        }
    }

    public static List<Event> findListOfEvents(Event event){
        List<Event> listOfEvents = new LinkedList<>();
        listOfEvents.add(event);
        while(event.getParentEvent()!=null){
            listOfEvents.add(event.getParentEvent());
            event = event.getParentEvent();
        }
        Collections.reverse(listOfEvents);
        return listOfEvents;
    }

    public static JDialog jDialog(String title){
        JDialog popup = new JDialog();
        popup.setTitle(title);
        popup.setSize(300, 100);
        popup.setLocationRelativeTo(null); // Center on screen
        return popup;
    }

    public static void getEventRows(Db db, String event, Vector<Vector<Object>> teamRows) throws IOException, ClassNotFoundException {
        Event ev = db.findObject(event);
        if(ev.getParentEvent()==null){
            List<Event> events = toEventObject(db);
            if(!events.isEmpty()){
                List<Event> subEvents = new LinkedList<>();
                collectSubEvents(ev,events,subEvents);
                teamRows.add(ev.toVector());//parent event
                subEvents.forEach(s->{
                    teamRows.add(s.toVector());
                });
            }
        }
    }

    public static void getTeamRow(Db db, String event, Vector<Vector<Object>> teamRows){
        try {
            Event ev = db.findObject(event);
            if(ev.getParentEvent()==null){


                List<Match> matches = ev.getMatcher().getMatches();
                List<Person> fromNames = matches.stream().map(Match::getFrom).collect(Collectors.toList());
                List<Person> toNames = matches.stream().map(Match::getTo).collect(Collectors.toList());
                fromNames.addAll(toNames);
                List<Person> uniquePeople = fromNames.stream()
                        .collect(Collectors.toMap(
                                Person::getId,  // Key is the name
                                person -> person, // Value is the person
                                (existing, replacement) -> existing)) // In case of duplicates, keep the existing one
                        .values()
                        .stream()
                        .collect(Collectors.toList());

                if(!uniquePeople.isEmpty()){
                    uniquePeople.stream().collect(Collectors.groupingBy(Person::getTeamName)).forEach((key,value)->{
                        Vector<Object> teamRow = new Vector<>();
                        teamRow.add(ev.getId());
                        teamRow.add(ev.getEventName());
                        teamRow.add(key);
                        teamRow.add(value.size());
                        teamRows.add(teamRow);
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Vector<Object> getFixtureRow(Db db, String event){
        Vector<Object> row = new Vector<>();
        try {
            Event ev = db.findObject(event);
            if(ev.getParentEvent()==null){
                List<Match> matches = ev.getMatcher().getMatches().stream().filter(m->!m.isPrimary()).collect(Collectors.toList());
                Set<String> fromTeamNames =  ev.getMatcher().getMatches().stream().map(a->a.getFrom().getTeamName()).collect(Collectors.toSet());
                Set<String> toTeamNames =  ev.getMatcher().getMatches().stream().map(a->a.getTo().getTeamName()).collect(Collectors.toSet());
                fromTeamNames.addAll(toTeamNames);

                row.add(ev.getId().toString());
                row.add(ev.getTeamName());
                row.add(ev.getEventName());
                row.add(fromTeamNames.size()+"");
                row.add(matches.size()+"");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  row;
    }

    public static List<Event> toEventObject(Db db){
        try {
            List<String> keys = db.keyFilterBy("Event_");
            if(!keys.isEmpty()){
                List<Event> events = keys.stream().map(key->{
                    try {
                        return (Event)db.findObject(key);
                    } catch (Exception e) {
                        return null;
                    }
                }).collect(Collectors.toList());
                return events;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    public static void collectSubEvents(Event parentEvent, List<Event> events, List<Event> subEvents) {
        for (Event e : events) {
            if (e.getParentEvent() != null && Objects.equals(e.getParentEvent().getId(), parentEvent.getId())) {
                subEvents.add(e);
                collectSubEvents(e, events, subEvents);
            }
        }
    }

    public static ImageIcon getImageIcon(String path){
        ImageIcon refreshIcon = new ImageIcon(Objects.requireNonNull(CommonFrame.class.getResource(path)));
        Image scaledImage = refreshIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
}
