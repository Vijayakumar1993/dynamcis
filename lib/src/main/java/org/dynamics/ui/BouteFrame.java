package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Event;
import org.dynamics.model.Person;
import org.dynamics.util.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

public class BouteFrame extends CommonFrame{
    private Db db;
    private DefaultTableModel matchTableModle;
    private DefaultTableModel fixtureTableModel;
    public BouteFrame(String title, Db db) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
        this.db = db;
    }
    public void northPanel(){
        List<String> paired = this.db.keyFilterBy("Event_");
        JPanel jsp = new JPanel();
        jsp.setBorder(BorderFactory.createTitledBorder("Find"));
        jsp.setBackground(Color.WHITE);
        JComboBox<String> pairedOptions = comboBox(paired);
        pairedOptions.setBorder(BorderFactory.createTitledBorder("Boute List"));

        JButton submit = new JButton("Submit");
        submit.addActionListener(a->{
            try {
                Event event = db.findObject(pairedOptions.getSelectedItem().toString());
                this.fixtureTableModel.setRowCount(0);
                this.matchTableModle.setRowCount(0);

                event.getMatcher().getMatches().forEach(match -> {

                    Person fromPerson = match.getFrom();
                    Person toPerson = match.getTo();

                    Vector<String> data = new Vector<>();
                    data.add(fromPerson.getId()+"");
                    data.add(event.getTeamName());
                    data.add(fromPerson.getName());

                    data.add(toPerson.getId()+"");
                    data.add(event.getTeamName());
                    data.add(toPerson.getName());
                    this.matchTableModle.addRow(data);
                });
                Utility.converter(event.getFixture().getPersons()).forEach(vec->{
                    this.fixtureTableModel.addRow(vec);
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

        JPanel matcherPanel = new JPanel();
        matcherPanel.setLayout(new BorderLayout());

        JPanel fixterPanel = new JPanel();
        fixterPanel.setLayout(new BorderLayout());


        Vector<String> matcherColumn = new Vector<>();
        matcherColumn.add("From Id");
        matcherColumn.add("From Team Name");
        matcherColumn.add("From  Name");
        matcherColumn.add("To Id");
        matcherColumn.add("To Team Name");
        matcherColumn.add("To Name");

        this.matchTableModle =  createTable(matcherPanel, new Vector<>(),matcherColumn,()->new LinkedHashMap<>());
        this.fixtureTableModel= createTable(fixterPanel, new Vector<>(),Person.keys(),()->new LinkedHashMap<>());
        JTabbedPane pane = new JTabbedPane();
        pane.add("Matcher List",matcherPanel);
        pane.add("Fixture",fixterPanel);
        this.add(pane, BorderLayout.CENTER);
    }
}
