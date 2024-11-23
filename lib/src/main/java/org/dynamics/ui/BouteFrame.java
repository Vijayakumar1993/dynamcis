package org.dynamics.ui;

import org.checkerframework.checker.units.qual.C;
import org.dynamics.db.Db;
import org.dynamics.model.*;
import org.dynamics.model.Event;
import org.dynamics.util.Utility;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class BouteFrame extends CommonFrame{
    private Db db;
    private Event event;
    private TablePair fixtureTableModel;
    private JPanel jscrollPanle = new JPanel();
    public BouteFrame(String title, Db db) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
        this.db = db;
    }

    public void southPanel(){
        JPanel panle = new JPanel();
        panle.setLayout(new BorderLayout());
        JButton updateMatch = new JButton("Update Match");
        JButton mergeWithFixture = new JButton("Merge");

        mergeWithFixture.addActionListener(a->{
            String eventName = this.event.getEventName().concat("(").concat(event.getId().toString()).concat(")");
            List<Person> fixture = event.getFixture().getPersons();
            List<Person> succesors = event.getMatcher().getMatches().stream().map(matches->matches.getSuccessor()).collect(Collectors.toList());
            boolean isSuccssorNotPrepared = succesors.stream().anyMatch(s->s.getId()==0);

            if(fixture.size()<=0){
                alert("No Fixtures available to merge with Event("+eventName+")");
                if(!isSuccssorNotPrepared){
                    try {
                      eventPanel(succesors,db,this.event);
                    } catch (IOException e) {
                        alert(e.getMessage());
                        e.printStackTrace();
                    }
                }else {
                    alert("Fixture not able to merge until winner list finalized for the event ("+eventName+")");
                }
            }else{
                if(isSuccssorNotPrepared){
                    alert("Fixture not able to merge until winner list finalized for the event ("+eventName+")");
                }else{
                    succesors.addAll(event.getFixture().getPersons());
                    try {
                       eventPanel(succesors,db,this.event);
                    } catch (IOException e) {
                        alert(e.getMessage());
                        e.printStackTrace();
                    }
                }
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
        grp.add(updateMatch);
        grp.add(mergeWithFixture);
        panle.add(grp, BorderLayout.EAST);
        add(panle,BorderLayout.SOUTH);
    }

    public void northPanel(){
        List<String> paired = this.db.keyFilterBy("Event_");
        JPanel jsp = new JPanel();
        jsp.setBorder(BorderFactory.createTitledBorder("Find"));
        jsp.setBackground(Color.WHITE);
        JComboBox pairedOptions = comboBoxForItems("Events",paired,db);

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
