package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Event;
import org.dynamics.model.TablePair;
import org.dynamics.reports.EventListReport;
import org.dynamics.reports.Report;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EventListFrame extends CommonFrame{
    private Vector<String> events = new Vector<>();
    private List<Event> availableEvent = new LinkedList<>();
    private TablePair eventTablePair = null;
    private List<Event> filterEvents = new LinkedList<>();
    public EventListFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
    }

    public void northPanel(Db db){
        JPanel jsp = new JPanel();
        jsp.setLayout(new FlowLayout());

        JTextField eventId = textField();
        eventId.setBorder(BorderFactory.createTitledBorder("Event ID"));
        jsp.add(eventId);

        JTextField eventName = textField();
        eventName.setBorder(BorderFactory.createTitledBorder("Category Name"));
        jsp.add(eventName);

        JTextField teamName = textField();
        teamName.setBorder(BorderFactory.createTitledBorder("Weight Category"));
        jsp.add(teamName);

        JTextField description = textField();
        description.setBorder(BorderFactory.createTitledBorder("Description"));
        jsp.add(description);

        JPanel matchesRange = new JPanel();
        List<String> weightsList = IntStream.rangeClosed(0,100).mapToObj(a->a+"").collect(Collectors.toList());
        JComboBox<String> matchFrom = comboBox(weightsList);
        matchFrom.setBorder(BorderFactory.createTitledBorder("From"));
        matchesRange.add(matchFrom);

        JComboBox<String> matchTo = comboBox(weightsList);
        matchTo.setBorder(BorderFactory.createTitledBorder("To"));
        matchesRange.add(matchTo);

        matchesRange.setBorder(BorderFactory.createTitledBorder("Matches Range"));
        jsp.add(matchesRange);


        JPanel fixtures = new JPanel();
        List<String> fixturesList = IntStream.rangeClosed(0,100).mapToObj(a->a+"").collect(Collectors.toList());
        JComboBox<String> fixtursFrom = comboBox(fixturesList);
        fixtursFrom.setBorder(BorderFactory.createTitledBorder("From"));
        fixtures.add(fixtursFrom);

        JComboBox<String> fixtursTo = comboBox(fixturesList);
        fixtursTo.setBorder(BorderFactory.createTitledBorder("To"));
        fixtures.add(fixtursTo);

        fixtures.setBorder(BorderFactory.createTitledBorder("Fixtures Range"));
        jsp.add(fixtures);

        JButton find = new JButton("Find");
        find.addActionListener(e->{
            try{
                String selectedEventId = eventId.getText().toString().toLowerCase();
                String selectedEventName = eventName.getText().toString().toLowerCase();
                String selectedTeamName = teamName.getText().toString().toLowerCase();
                String selectedDescription = description.getText().toString().toLowerCase();
                String selectedmatchesFrom = matchFrom.getSelectedItem().toString().toLowerCase();
                String selectedmatchesTo = matchTo.getSelectedItem().toString().toLowerCase();
                String selectedfixturesFrom = fixtursFrom.getSelectedItem().toString().toLowerCase();
                String selectedfixturesTo = fixtursTo.getSelectedItem().toString().toLowerCase();

                this.filterEvents = availableEvent.stream().filter(event->{
                    if(!selectedEventId.isEmpty()){
                        return (""+event.getId()).toLowerCase().toLowerCase().contains(selectedEventId);
                    }else
                        return true;
                }).filter(event->{
                    if(!selectedEventName.isEmpty()){
                        return event.getEventName().toLowerCase().contains(selectedEventName);
                    }else
                        return true;
                }).filter(event->{
                    if(!selectedTeamName.isEmpty()){
                        return (event.getTeamName()).toLowerCase().contains(selectedTeamName);
                    }else
                        return true;
                }).filter(event->{
                    if(!selectedDescription.isEmpty()){
                        return (event.getDescription()).toLowerCase().contains(selectedDescription);
                    }else
                        return true;
                }).filter(event->{
                    int matchSize =event.getMatcher().getMatches().size();
                    int defaultFrom = selectedmatchesFrom.isEmpty()?0:Integer.parseInt(selectedmatchesFrom);
                    int defaultTo = selectedmatchesTo.isEmpty()?100:Integer.parseInt(selectedmatchesTo);
                    return matchSize>=defaultFrom && matchSize<defaultTo;
                }).filter(event->{
                    int matchSize =event.getFixture().getPersons().size();
                    int defaultFrom = selectedfixturesFrom.isEmpty()?0:Integer.parseInt(selectedfixturesFrom);
                    int defaultTo = selectedfixturesTo.isEmpty()?100:Integer.parseInt(selectedfixturesTo);
                    return matchSize>=defaultFrom && matchSize<defaultTo;
                }).collect(Collectors.toList());
                this.eventTablePair.getDefaultTableModel().setRowCount(0);
                this.filterEvents.forEach(event->{
                    System.out.println(event.getEventName());
                    eventTablePair.getDefaultTableModel().addRow(event.toVector());
                });
            }catch (Exception e1){
                e1.printStackTrace();
                alert(e1.getMessage());
            }
        });
        jsp.add(find);
        this.add(jsp, BorderLayout.NORTH);
    }
    public void listEvents(Db db){
        this.events = db.keyFilterBy("Event_");
        List<Vector<Object>> eventsDetails = events.stream().map(key->{
            try {
                Event ev = db.findObject(key);
                availableEvent.add(ev);
                return ev.toVector();
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        this.filterEvents =  availableEvent;
        this.eventTablePair = this.createTable(this,new Vector<>(eventsDetails),Event.keys(),()->new LinkedHashMap<>(),null);
    }

    public void southPanel(){
        JButton eventReport = new JButton("Generate Bout List");
        JPanel jsp = new JPanel();
        jsp.setLayout(new BorderLayout());
        ButtonGroup btn = new ButtonGroup();
        btn.add(eventReport);

        eventReport.addActionListener(e->{
            Report report = null;
            try {
                if(this.filterEvents.size()<=0){
                    alert("No Events are available to generate report.");
                    return;
                }

                //lets order the filter events as per the table
                int actualRowCount = this.eventTablePair.getjTable().getRowCount();
                for(int i=0;i<actualRowCount;i++){
                        System.out.println(this.eventTablePair.getjTable().getValueAt(i,0));
                }
                List<Long> compartor = IntStream.range(0,actualRowCount).mapToObj(i->Long.valueOf(this.eventTablePair.getjTable().getValueAt(i,0).toString())).collect(Collectors.toList());

                this.filterEvents = this.filterEvents.stream().sorted(Comparator.comparingLong(a->compartor.indexOf(a.getId()))).collect(Collectors.toList());
                Optional<String> saveFile = fileSaver();
                if(saveFile.isPresent()){
                    report = new EventListReport(saveFile.get()+".pdf");
                    String titile = JOptionPane.showInputDialog("Please enter the title of the report");
                    if(titile.length()>0){
                        report.generateReport(this.filterEvents,titile);
                    }else{
                        alert("Title is required for the report, please provide valid title.");
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
                alert(ex.getMessage());
            }

        });
        jsp.add(eventReport, BorderLayout.EAST);
        this.add(jsp, BorderLayout.SOUTH);
    }
}
