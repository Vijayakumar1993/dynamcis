package org.dynamics.util;

import org.checkerframework.checker.units.qual.C;
import org.dynamics.model.*;
import org.dynamics.model.Event;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        System.out.println("Matcher List "+matcher);
        System.out.println("Fixture List "+fixtureSize);

        List<Person> matcherList = persons.subList(0,matcher);
        List<Person> fixtureList = persons.subList(matcher,persons.size());
        List<Match> matchList = new LinkedList<>();
        for(int i=0;i<matcherList.size();i=i+2){
            Person fr = matcherList.get(i);
            Person tr = matcherList.get(i+1);
            System.out.println("From "+fr.getName()+" --- To "+tr.getName());
            Match match = new Match();
            match.setMatchId(Utility.getRandom());
            match.setFromCorner(Corner.BLUE);
            match.setToCorner(Corner.RED);
            match.setFrom(matcherList.get(i));
            match.setTo(matcherList.get(i+1));
            matchList.add(match);
        }

        int actualFixtureSize = fixtureList.size();
        if(actualFixtureSize%2!=0){
            fixtureList.add(createNaPerson());
        }
        //fixture matches
        for(int i=0;i<fixtureList.size();i=i+1){
            Person fr = fixtureList.get(i);
            Person tr = fixtureList.get(i);
            System.out.println("From "+fr.getName()+" --- To "+tr.getName());
            Match match = new Match();
            match.setMatchId(Utility.getRandom());
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
}
