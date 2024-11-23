package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Event;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

public class EventListFrame extends CommonFrame{
    public EventListFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
    }

    public void listEvents(Db db){
        Vector<String> events = db.keyFilterBy("Event_");
        List<Vector<Object>> eventsDetails = events.stream().map(key->{
            try {
                Event ev = db.findObject(key);
                return ev.toVector();
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        this.createTable(this,new Vector<>(eventsDetails),Event.keys(),()->new LinkedHashMap<>());
    }
}
