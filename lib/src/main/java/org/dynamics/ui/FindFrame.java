package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.*;
import org.dynamics.model.Event;
import org.dynamics.util.Utility;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FindFrame extends CommonFrame{
    private List<Person> persons;
    private List<Person> filteredPersons;
    private TablePair tableModel;
    private JLabel loger ;
    private String fileKey;
    public FindFrame(String title, List<Person> persons, String fileKey) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
        this.persons = persons;
        this.fileKey = fileKey;
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

        JPanel weightPanel = new JPanel();
        List<String> weightsList = IntStream.rangeClosed(0,100).mapToObj(a->a+"").collect(Collectors.toList());
        JComboBox<String> weightFrom = comboBox(weightsList);
        weightFrom.setBorder(BorderFactory.createTitledBorder("From"));
        weightPanel.add(weightFrom);

        JComboBox<String> weightTo = comboBox(weightsList);
        weightTo.setBorder(BorderFactory.createTitledBorder("To"));
        weightPanel.add(weightTo);

        weightPanel.setBorder(BorderFactory.createTitledBorder("Weight Range"));
        jsp.add(weightPanel);

        //submit button
        JButton submit = new JButton("Find");
        submit.addActionListener(a->{
            String selectedName = nameField.getText();
            String selecetdGender = genderBox.getSelectedItem().toString();
            String selectedCategory = categoresBox.getSelectedItem().toString();
            String selectedFrom = weightFrom.getSelectedItem().toString();
            String selectedTo = weightTo.getSelectedItem().toString();
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
                    }).filter(weightFilter->{
                        if(selectedFrom!=""){
                            return weightFilter.getWeight().compareTo(Double.parseDouble(selectedFrom))>=1;
                        }else{
                            return true;
                        }
                    }).filter(weightFilter->{
                        if(selectedTo!=""){
                            return weightFilter.getWeight().compareTo(Double.parseDouble(selectedTo)) < 0;
                        }else{
                            return true;
                        }
                    })
                    .collect(Collectors.toList());
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
        BiConsumer<JTable, TableModelEvent> consumer = (table,modelEvet)->{
            int row = modelEvet.getFirstRow();
            System.out.println(row);
            if(row==0){
                return;
            }
            String personId = table.getValueAt(modelEvet.getFirstRow(),0).toString();
            Person existingPerson = this.persons.stream().filter(people->people.getId()==Long.parseLong(personId)).collect(Collectors.toList()).get(0);
            if(existingPerson!=null){
                int firstRow = modelEvet.getFirstRow();
                int firstColumn = modelEvet.getColumn();
                System.out.println("Col "+firstColumn+" / "+firstRow);
                if(firstColumn==-1 || firstRow==-1) return;
                Object valueAt = table.getValueAt(firstRow, firstColumn);
                if(valueAt==null) return;
                String changedEvents = valueAt.toString();
                switch (modelEvet.getColumn()){
                    case 1:
                        existingPerson.setName(changedEvents);
                        break;
                    case 2:
                        try{
                            Gender gender = Gender.valueOf(changedEvents);
                            existingPerson.setGender(gender);
                        }catch (Exception e){
                            e.printStackTrace();
                            alert("Invalid Gender found at Row "+modelEvet.getFirstRow()+", please update with"+Gender.values().toString());
                        }
                        break;
                    case 3:
                        try{
                            Categories categories = Categories.valueOf(changedEvents);
                            existingPerson.setCategories(categories);
                        }catch (Exception e){
                            e.printStackTrace();
                            alert("Invalid Gender found at Row "+modelEvet.getFirstRow()+", please update with"+Gender.values().toString());
                        }
                        break;
                    case 4:
                        try {
                           Double weight =  Double.parseDouble(changedEvents);
                           existingPerson.setWeight(weight);
                        }catch (Exception e){
                            e.printStackTrace();
                            alert("Invalid weight found at "+modelEvet.getFirstRow()+", Please provide valid number");
                        }
                        break;
                    default:
                        alert("Invalid Column found.");

                }
                try {
                    db.insert(this.fileKey,this.persons);
                } catch (IOException e) {
                    e.printStackTrace();
                    alert(e.getMessage());
                }
            }

        };
        actions.put("Create Event",(event)->{
            List<Person> peoples = this.filteredPersons.size()>0?this.filteredPersons:this.persons;
            try {
                eventPanel(peoples,db,null);
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
        });
        this.tableModel =  createTable(this,Utility.converter(persons),Person.keys(),()->actions,consumer);
    }
}
