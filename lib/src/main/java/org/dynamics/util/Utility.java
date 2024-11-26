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
        Collections.shuffle(persons);
        Map<String, Integer> keyPair = fixtureAndMatcher(persons.size());
        Integer fixtureSize = keyPair.get("fixture");
        Integer matcher = keyPair.get("matcher");

        System.out.println("Matcher List "+matcher);
        System.out.println("Fixture List "+fixtureSize);
        //matcher only need to shuffle
        List<Person> matcherList = persons.subList(0,matcher);
        List<Match> matchList = new LinkedList<>();
        for(int i=0;i<matcherList.size();i=i+2){
            Match match = new Match();
            match.setMatchId(Utility.getRandom());
            match.setFromCorner(Corner.BLUE);
            match.setToCorner(Corner.RED);
            match.setFrom(matcherList.get(i));
            match.setTo(matcherList.get(i+1));
            match.setPrimary(true);
            matchList.add(match);
        }
        matches.setMatches(matchList);
        Fixture fixture = new Fixture();
        List<Person> fixtures = new LinkedList<>(persons.subList(matcher,persons.size()));

        if(fixtures.size()%2!=0){
            Person buyer = fixtures.get(0);
            fixture.setPersons(Stream.of(buyer).collect(Collectors.toList()));
            fixtures.remove(0);
        }
        for(int i=0;i<fixtures.size();i=i+2){
            Match match = new Match();
            match.setMatchId(Utility.getRandom());
            match.setFromCorner(Corner.BLUE);
            match.setToCorner(Corner.RED);
            match.setFrom(fixtures.get(i));
            match.setTo(fixtures.get(i+1));
            match.setPrimary(false);
            matchList.add(match);
        }

        System.out.println(matches.getMatches().size());
        System.out.println(fixture.getPersons().size());
        event.setMatcher(matches);
        event.setFixture(fixture);
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
}
