package org.dynamics.ui;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dynamics.db.Db;
import org.dynamics.model.*;
import org.dynamics.model.Event;
import org.dynamics.reports.EventListReport;
import org.dynamics.reports.Report;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EventListFrame extends CommonFrame{
    private static final Logger logger = LogManager.getLogger(EventListFrame.class);
    private Vector<String> events = new Vector<>();
    private List<Event> availableEvent = new LinkedList<>();
    private TablePair eventTablePair = null;
    private List<Event> filterEvents = new LinkedList<>();
    private JButton find = new JButton("Find");
    public EventListFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        super(title);
    }

    public void northPanel(Db db){
        JPanel jsp = new JPanel();
        jsp.setLayout(new GridLayout(3,3));
        jsp.setBorder(BorderFactory.createTitledBorder("Event List"));

        JTextField eventId = textField();
        eventId.setBorder(BorderFactory.createTitledBorder("Event ID"));
        jsp.add(eventId);


        JTextField teamName = textField();
        teamName.setBorder(BorderFactory.createTitledBorder("Weight Category"));
        jsp.add(teamName);


        JComboBox<String> eventStatus = comboBox(Arrays.stream(Status.values()).map(Enum::toString).collect(Collectors.toList()));
        eventStatus.setBorder(BorderFactory.createTitledBorder("Status"));
        jsp.add(eventStatus);

        JComboBox<String> genderBox = comboBox(Arrays.stream(Gender.values()).map(a->a.toString()).collect(Collectors.toList()));
        genderBox.setBorder(null);
        genderBox.setBorder(BorderFactory.createTitledBorder("Gender"));
        jsp.add(genderBox);


        List<String> weightsList =  IntStream.rangeClosed(1, 20)
                .mapToObj(i -> ((int) Math.pow(2, i))+"")
                .collect(Collectors.toList());
        weightsList.add(0,"");
        JComboBox<String> rndFrom = new JComboBox<>(new Vector<>(weightsList));
        rndFrom.setBorder(BorderFactory.createTitledBorder("Round off"));
        jsp.add(rndFrom);

        JTextField description = textField();
        description.setBorder(BorderFactory.createTitledBorder("Description"));
        jsp.add(description);


        JComboBox<String> eventName = comboBox(Arrays.stream(Categories.values()).map(Enum::toString).collect(Collectors.toList()));
        eventName.setBorder(BorderFactory.createTitledBorder("Category Name"));
        jsp.add(eventName);

        find.addActionListener(e->{
            try{
                String selectedEventId = eventId.getText().toString().toLowerCase();
                String selectedEventName = eventName.getSelectedItem().toString().toLowerCase();
                String selectedTeamName = teamName.getText().toString().toLowerCase();
                String selectedDescription = description.getText().toString().toLowerCase();
                String selectedmatchesFrom = rndFrom.getSelectedItem().toString().toLowerCase();
                String selectedGender = genderBox.getSelectedItem().toString().toLowerCase();
                String selectedStatus = eventStatus.getSelectedItem().toString().toLowerCase();
                this.availableEvent = db.keyFilterBy("Event_").stream().map(avr->{
                    try {
                        return (Event)db.findObject(avr);
                    } catch (Exception ex) {
                        logger.error("An error occurred", ex);
                        ex.printStackTrace();
                        alert(ex.getMessage());
                    }
                    return null;
                }).collect(Collectors.toList());
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
                    if(!selectedGender.isEmpty()){
                        return (event.getSelectedGenderCategory().toString()).toLowerCase().equals(selectedGender);
                    }else
                        return true;
                }).filter(event->{
                    if(!selectedDescription.isEmpty()){
                        return (event.getDescription()).toLowerCase().contains(selectedDescription);
                    }else
                        return true;
                }).filter(event->{
                    if(!selectedStatus.isEmpty()){
                        return (event.getStatus().toString()).toLowerCase().contains(selectedStatus);
                    }else
                        return true;
                }).filter(event->{
                    if(!selectedmatchesFrom.isEmpty()){
                        int matchSize = event.getRoundOf();
                        int defaultFrom = selectedmatchesFrom.isEmpty()?0:Integer.parseInt(selectedmatchesFrom);
                        return matchSize==defaultFrom;
                    }
                    return true;
                }).collect(Collectors.toList());
                this.eventTablePair.getDefaultTableModel().setRowCount(0);
                this.filterEvents.forEach(event->{
                    logger.info(event.getEventName());
                    eventTablePair.getDefaultTableModel().addRow(event.toVector());
                });
            }catch (Exception e1){
                logger.error("An error occurred", e1);
                e1.printStackTrace();
                alert(e1.getMessage());
            }
        });
        find.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        jsp.add(find);
        this.add(new JScrollPane(jsp), BorderLayout.NORTH);
    }
    public void listEvents(Db db){
        this.events = db.keyFilterBy("Event_");
        List<Vector<Object>> eventsDetails = events.stream().map(key->{
            try {
                Event ev = db.findObject(key);
                availableEvent.add(ev);
                return ev.toVector();
            } catch (Exception e) {
                logger.error("An error occurred", e);
                e.printStackTrace();
                alert(e.getMessage());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        this.filterEvents =  availableEvent;
        this.eventTablePair = this.createTable(this,new Vector<>(eventsDetails),Event.keys(),()->new LinkedHashMap<>(),null);
    }

    public void southPanel(Db db){
        JButton eventReport = new JButton("Generate Bout List");
        eventReport.setBackground(new Color(0, 0, 139));
        eventReport.setForeground(Color.WHITE);
        JButton deleteEvent = new JButton("Delete Event");
        deleteEvent.setForeground(Color.WHITE);
        deleteEvent.setBackground(Color.RED);
        JPanel jsp = new JPanel();
        jsp.setLayout(new BorderLayout());
        ButtonGroup btn = new ButtonGroup();
        btn.add(deleteEvent);
        btn.add(eventReport);

        deleteEvent.addActionListener(a->{
            this.filterEvents.forEach(event->{
                logger.info("Event "+event.getId()+" deletion started");
                int result = JOptionPane.showConfirmDialog(null,"Are you sure to remove "+event.getEventName()+"?");
                if(JOptionPane.YES_OPTION == result){
                    db.delete("Event_"+event.getId());
                    logger.info("Event "+event.getId()+" deletion ends");
                    this.find.doClick();
                }
            });
        });

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
                    logger.info(this.eventTablePair.getjTable().getValueAt(i,0));
                }
                List<Long> compartor = IntStream.range(0,actualRowCount).mapToObj(i->Long.valueOf(this.eventTablePair.getjTable().getValueAt(i,0).toString())).collect(Collectors.toList());

                this.filterEvents = this.filterEvents.stream().sorted(Comparator.comparingLong(a->compartor.indexOf(a.getId()))).collect(Collectors.toList());
                Optional<String> saveFile = fileSaver();
                if(saveFile.isPresent()){
                    Configuration configuration = new Configuration();
                    try {
                        configuration =  db.findObject("configuration");
                    } catch (Exception es) {
                        es.printStackTrace();
                        alert(es.getMessage());
                    }
                    report = new EventListReport(saveFile.get()+".pdf",configuration);
                    report.generateReport(this.filterEvents,"");
                }
            } catch (Exception ex) {
                logger.error("An error occurred", ex);
                ex.printStackTrace();
                alert(ex.getMessage());
            }

        });
        jsp.add(deleteEvent, BorderLayout.WEST);
        jsp.add(eventReport, BorderLayout.EAST);
        this.add(jsp, BorderLayout.SOUTH);
    }
}
