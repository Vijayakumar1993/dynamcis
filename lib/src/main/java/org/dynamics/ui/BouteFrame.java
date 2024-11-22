package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.*;
import org.dynamics.model.Event;
import org.dynamics.util.Utility;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BouteFrame extends CommonFrame{
    private Db db;
    private TablePair matchTableModle;
    private TablePair fixtureTableModel;
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
        pairedOptions.setBorder(BorderFactory.createTitledBorder("Event List"));

        JButton submit = new JButton("Submit");
        submit.addActionListener(a->{
            try {
                String selectedEventId = pairedOptions.getSelectedItem().toString();
                if(selectedEventId.isEmpty()){
                    alert("Kindly select the event.");
                    return;
                }
                Event event = db.findObject(selectedEventId);
                this.fixtureTableModel.getDefaultTableModel().setRowCount(0);
                this.matchTableModle.getDefaultTableModel().setRowCount(0);


                Enumeration<TableColumn> columns = this.matchTableModle.getjTable().getColumnModel().getColumns();
                this.matchTableModle.getDefaultTableModel().addColumn("Winner");

                event.getMatcher().getMatches().forEach(match -> {

                    Person fromPerson = match.getFrom();
                    Person toPerson = match.getTo();

                    Vector<Object> data = new Vector<>();
                    data.add(event.getTeamName().concat("-").concat(fromPerson.getName()).concat("(").concat(fromPerson.getId()+"").concat(")"));
                    data.add(event.getTeamName().concat("-").concat(toPerson.getName()).concat("(").concat(toPerson.getId()+"").concat(")"));

                    this.matchTableModle.getDefaultTableModel().addRow(data);
                });


                this.matchTableModle.getjTable().getColumn("Winner").setCellRenderer(new RadioButtonRenderer());
                this.matchTableModle.getjTable().getColumn("Winner").setCellEditor(new RadioButtonEditor());


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

        JPanel matcherPanel = new JPanel();
        matcherPanel.setLayout(new BorderLayout());

        JPanel fixterPanel = new JPanel();
        fixterPanel.setLayout(new BorderLayout());


        Vector<String> matcherColumn = new Vector<>();
        matcherColumn.add("Red Corner");
        matcherColumn.add("Blue Corner");
        matcherColumn.add("Winner");

        this.matchTableModle =  createTable(matcherPanel, new Vector<>(),matcherColumn,()->new LinkedHashMap<>());
        this.fixtureTableModel= createTable(fixterPanel, new Vector<>(),Person.keys(),()->new LinkedHashMap<>());
        JTabbedPane pane = new JTabbedPane();
        pane.add("Matcher List",matcherPanel);
        pane.add("Fixture",fixterPanel);
        this.add(pane, BorderLayout.CENTER);
    }
}
