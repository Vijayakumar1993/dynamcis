package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Event;
import org.dynamics.model.*;
import org.dynamics.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class BouteFrame extends CommonFrame{
    private Db db;
    private Event event;
    private TablePair fixtureTableModel;
    private JPanel jscrollPanle = new JPanel();
    private JComboBox<Item> pairedOptions;
    public BouteFrame(String title, Db db) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
        this.db = db;
        List<String> paired = this.db.keyFilterBy("Event_");
        pairedOptions = comboBoxForItems("Events",paired,db);
    }

    public void southPanel(){
        JPanel panle = new JPanel();
        panle.setLayout(new BorderLayout());
        JButton updateMatch = new JButton("Update");
        JButton mergeWithFixture = new JButton("Next");
        JButton freeze = new JButton("freeze");
        freeze.setPreferredSize(new Dimension(100,20));
        freeze.setBackground(Color.RED);

        mergeWithFixture.addActionListener(a->{
            if(this.event==null){
                alert("Event Not selected. Please select.");
                return;
            }
            try {
                Event dbEvent = db.findObject("Event_"+this.event.getId());
                String eventName = dbEvent.getEventName().concat("(").concat(dbEvent.getId().toString()).concat(")");
                List<Person> fixture = dbEvent.getFixture().getPersons();
                List<Person> succesors = dbEvent.getMatcher().getMatches().stream().map(matches->matches.getSuccessor()).collect(Collectors.toList());

                boolean isSuccssorNotPrepared = succesors.stream().anyMatch(s->s.getId()==0);

                if(isSuccssorNotPrepared){
                    alert("Kindly finish the event ("+eventName+") to move next.");
                }else{
                    succesors.addAll(fixture);
                    if(succesors.size()<=1){
                        Corner successorCorner = dbEvent.getMatcher().getMatches().stream().map(match ->{
                            if(match.getFrom().getId()==match.getSuccessor().getId()) return match.getFromCorner();
                            else return match.getToCorner();
                        } ).collect(Collectors.toList()).get(0);
                        Person winner = succesors.get(0);
                        dbEvent.getMatcher().setWinner(winner);
                        dbEvent.getMatcher().setWinnerCorder(successorCorner);
                        try {
                            this.db.insert("Event_"+dbEvent.getId(), dbEvent);

                            JPanel panel = new JPanel();
                            panel.setBackground(successorCorner.getColor()); // Set background color
                            JLabel wins = new JLabel(winner.getName());

                            if(successorCorner==Corner.RED){
                                wins.setForeground(Color.YELLOW);
                            }else{
                                wins.setForeground(Color.BLUE);
                            }
                            wins.setFont(new Font("Serif",Font.BOLD,12));
                            panel.add(wins);

                            confirmation("Winner",()->panel);
                            alert("No More matches for the event("+this.event.getEventName()+")");
                        } catch (IOException e) {
                            alert(e.getMessage());
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            eventPanel(succesors,db,dbEvent);
                            List<String> paired = this.db.keyFilterBy("Event_");
                            DefaultComboBoxModel<Item> comboBoxModel = new DefaultComboBoxModel<Item>();
                            List<Item> sortedItems = new LinkedList<>();
                            paired.forEach(s->{
                                try {
                                    Event event = db.findObject(s);
                                    String description = event.getDescription();
                                    sortedItems.add(new Item(event.getId(), description));
                                } catch (Exception e) {
                                    alert(e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                            sortedItems.sort(Comparator.comparing(Item::getDescription));
                            sortedItems.forEach(comboBoxModel::addElement);
                            pairedOptions.setModel(comboBoxModel);
                        } catch (IOException e) {
                            alert(e.getMessage());
                            e.printStackTrace();
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }

        });
        updateMatch.addActionListener(a->{
            try {
                if(this.event!=null){
                    db.insert("Event_"+this.event.getId().toString(),this.event);
                    alert("Event("+this.event.getEventName()+") updated successfully.");
                }else{
                    alert("Event Not selected. Please select.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
        });
        JPanel grp = new JPanel(new FlowLayout());
        grp.add(freeze);
        grp.add(updateMatch);
        grp.add(mergeWithFixture);
        panle.add(grp, BorderLayout.EAST);
        add(panle,BorderLayout.SOUTH);
    }

    public void northPanel(){

        JPanel jsp = new JPanel();
        jsp.setBorder(BorderFactory.createTitledBorder("Find"));
        jsp.setBackground(Color.WHITE);


        JButton submit = new JButton("Find");

        submit.addActionListener(a->{
            try {
                Item selectedItem = (Item)pairedOptions.getSelectedItem();
                if(selectedItem == null || selectedItem.getId()==0){
                    alert("Kindly select the event.");
                    return;
                }
                String selectedEventId = "Event_"+((Item) pairedOptions.getSelectedItem()).getId().toString();
                this.event = db.findObject(selectedEventId);
                this.fixtureTableModel.getDefaultTableModel().setRowCount(0);

                Matcher matcher = event.getMatcher();
                List<Match> matches = matcher.getMatches();
                jscrollPanle.removeAll();
                jscrollPanle.revalidate();
                jscrollPanle.repaint();

                JPanel innerPanle = new JPanel();
                innerPanle.setLayout(new GridLayout(matches.size(),2));
                matches.forEach(match -> {
                    ButtonGroup buttonGroup = new ButtonGroup();
                    Person fromPerson = match.getFrom();
                    Person toPerson = match.getTo();
                    Person succesor = match.getSuccessor();
                    JRadioButton fromRadioButton = new JRadioButton(event.getTeamName().concat("-").concat(fromPerson.getName()).concat("(").concat(fromPerson.getId() + "").concat(")"));
//                    fromRadioButton.setBackground(match.getFromCorner().getColor());
                    fromRadioButton.setForeground(Color.BLUE);
                    fromRadioButton.setFont(new Font("Serif",Font.BOLD,14));
                    System.out.println("succesor "+succesor.getId());

                    System.out.println("from person "+toPerson.getId());

                    if(succesor.getId()==fromPerson.getId()){
                        fromRadioButton.setSelected(true);
                    }




                    //to radio button
                    JRadioButton toRadioButton = new JRadioButton(event.getTeamName().concat("-").concat(toPerson.getName()).concat("(").concat(toPerson.getId() + "").concat(")"));
//                    toRadioButton.setBackground(match.getToCorner().getColor());
                    toRadioButton.setForeground(Color.RED);
                    toRadioButton.setFont(new Font("Serif",Font.BOLD,14));
                    System.out.println("succesor "+succesor.getId());

                    System.out.println("to person "+toPerson.getId());

                    if(succesor.getId()==toPerson.getId()){
                        toRadioButton.setSelected(true);
                    }

                    toRadioButton.addActionListener(e->{
                        System.out.println("updated ");
                        match.setSuccessor(toPerson);
                    });
                    fromRadioButton.addActionListener(e->{
                        System.out.println("updated ");
                        match.setSuccessor(fromPerson);
                    });
                    buttonGroup.add(fromRadioButton);
                    buttonGroup.add(toRadioButton);
                    innerPanle.add(fromRadioButton);
                    innerPanle.add(toRadioButton);
                });
                jscrollPanle.add(innerPanle, BorderLayout.CENTER);

                Utility.converter(event.getFixture().getPersons()).forEach(vec->{
                    this.fixtureTableModel.getDefaultTableModel().addRow(vec);
                });
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }

        });
        jsp.add(pairedOptions);
        jsp.add(submit);
        this.add(jsp,BorderLayout.NORTH);
    }

    public void centerPanel(){

        jscrollPanle.setLayout(new BorderLayout());

        JPanel fixterPanel = new JPanel();
        fixterPanel.setLayout(new BorderLayout());


        Vector<String> matcherColumn = new Vector<>();
        matcherColumn.add("Red Corner");
        matcherColumn.add("Blue Corner");
        matcherColumn.add("Winner");
        this.fixtureTableModel= createTable(fixterPanel, new Vector<>(),Person.keys(),()->new LinkedHashMap<>());
        JTabbedPane pane = new JTabbedPane();
        pane.add("Matcher List", new JScrollPane(jscrollPanle));
        pane.add("Fixture",fixterPanel);
        this.add(pane, BorderLayout.CENTER);
    }
}
