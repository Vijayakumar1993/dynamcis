package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.*;
import org.dynamics.model.Event;
import org.dynamics.util.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FindFrame extends CommonFrame{
    private List<Person> persons;
    private List<Person> filteredPersons;
    private TablePair tableModel;
    private JLabel loger ;
    public FindFrame(String title, List<Person> persons) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
        this.persons = persons;
        this.filteredPersons = new LinkedList<>();
        this.loger = new JLabel("Total : "+this.persons.size());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    public void northpanel(){
        JPanel jsp = new JPanel();
        jsp.setBorder(BorderFactory.createTitledBorder("Find"));
        jsp.setBackground(Color.WHITE);
        //name filter
        JTextField nameField = textField();
        nameField.setBorder(BorderFactory.createTitledBorder("Name"));
        jsp.add(nameField);

        //gender filter
        JComboBox<String> genderBox = comboBox(Arrays.stream(Gender.values()).map(a->a.toString()).collect(Collectors.toList()));
        genderBox.setBorder(null);
        genderBox.setBorder(BorderFactory.createTitledBorder("Gender"));
        jsp.add(genderBox);

        //Categories filter
        JComboBox<String> categoresBox = comboBox(Arrays.stream(Categories.values()).map(a->a.toString()).collect(Collectors.toList()));
        categoresBox.setBorder(BorderFactory.createTitledBorder("Categories"));
        jsp.add(categoresBox);

        //submit button
        JButton submit = new JButton("Find");
        submit.addActionListener(a->{
            String selectedName = nameField.getText();
            String selecetdGender = genderBox.getSelectedItem().toString();
            String selectedCategory = categoresBox.getSelectedItem().toString();
            filteredPersons = persons.stream().filter(nameFilter->{
                if(selectedName!=""){
                    return nameFilter.getName().toLowerCase().contains(selectedName.toLowerCase());
                }else{
                    return true;
                }
            }).filter(genderFilter->{
                if(selecetdGender!=""){
                    return genderFilter.getGender().toString().equalsIgnoreCase(selecetdGender);
                }else{
                    return true;
                }
            }).filter(categoryFilter->{
                if(selectedCategory!=""){
                    return categoryFilter.getCategories().toString().equalsIgnoreCase(selectedCategory);
                }else{
                    return true;
                }
            }).collect(Collectors.toList());
            tableModel.getDefaultTableModel().setRowCount(0);
            filteredPersons.forEach(per->{
                tableModel.getDefaultTableModel().addRow(per.toVector());
            });

            this.loger.setText("Total : "+filteredPersons.size());
        });
        jsp.add(submit);

        this.add(jsp,BorderLayout.NORTH);
    }

    public void southPanel(){
        this.add(this.loger, BorderLayout.SOUTH);
    }

    public void addDetails(Db db){
        List<Map<Person, Person>> entries = new LinkedList<>();
        Map<String, ActionListener> actions = new LinkedHashMap<>();
        actions.put("Create Event",(event)->{
            List<Person> peoples = this.filteredPersons.size()>0?this.filteredPersons:this.persons;
            try {
                eventPanel(peoples,db,null);
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
        });
        this.tableModel =  createTable(this,Utility.converter(persons),Person.keys(),()->actions);
    }
}
