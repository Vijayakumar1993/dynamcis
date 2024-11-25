package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Categories;
import org.dynamics.model.Gender;
import org.dynamics.model.Person;
import org.dynamics.model.TablePair;
import org.dynamics.util.Utility;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
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
    private JButton find;
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

        JTextField ids = textField();
        ids.setBorder(BorderFactory.createTitledBorder("Player Id"));
        jsp.add(ids);

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
        this.find = new JButton("Find");
        this.find.addActionListener(a->{
            String selectedName = nameField.getText();
            String selectedId = ids.getText().toString();
            String selecetdGender = genderBox.getSelectedItem().toString();
            String selectedCategory = categoresBox.getSelectedItem().toString();
            String selectedFrom = weightFrom.getSelectedItem().toString();
            String selectedTo = weightTo.getSelectedItem().toString();
            filteredPersons = persons.stream().filter(nameFilter->{
                        if(selectedName.length()>0){
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
                    }).filter(idsFilter->{
                        if(selectedId.length()>0){
                            return (""+idsFilter.getId()).equalsIgnoreCase(selectedId);
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
        jsp.add(this.find);

        this.add(jsp,BorderLayout.NORTH);
    }

    public void southPanel(Db db){
        JButton createPerson = new JButton("Create Player");
        JButton deletePlayer = new JButton("Delete Players");
        deletePlayer.setBackground(Color.RED);
        deletePlayer.addActionListener(e->{

            Integer result = JOptionPane.showConfirmDialog(this, "Are you sure?");
            if(result == JOptionPane.YES_OPTION){
                try {
                    JTable jTable = tableModel.getjTable();
                    List<String> removalIds = new LinkedList<>();
                    IntStream.range(0,jTable.getRowCount()).forEach(a->{
                        String id = jTable.getValueAt(a,0).toString();
                        removalIds.add(id);
                    });
                    if(!removalIds.isEmpty()){
                        Integer options = JOptionPane.showConfirmDialog(this, "Total  "+removalIds.size()+" players are found to remove? \nAre you sure to remove?");
                        if(options == JOptionPane.YES_OPTION){
                            List<Person> removalPersons = this.persons.stream().filter(p->removalIds.contains(p.getId()+"")).collect(Collectors.toList());
                            boolean isRemoved = this.persons.removeAll(removalPersons);
                            System.out.println("Is Removed "+isRemoved);
                            db.insert(fileKey,this.persons);
                            alert(removalIds.size()+" Removed successfully.");
                            this.find.doClick();
                        }
                    }

                } catch (Exception ex) {
                    alert(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        createPerson.addActionListener(a->{
            JTextField name = textField();
            name.setBorder(BorderFactory.createTitledBorder("Player Name"));
            JComboBox<String> gender =  comboBox(Arrays.stream(Gender.values()).map(as->as.toString()).collect(Collectors.toList()));
            gender.setBorder(BorderFactory.createTitledBorder("Gender"));
            JComboBox<String> category = comboBox(Arrays.stream(Categories.values()).map(as->as.toString()).collect(Collectors.toList()));
            category.setBorder(BorderFactory.createTitledBorder("Category"));
            JComboBox<String> weights = comboBox(IntStream.rangeClosed(0,100).mapToObj(sa->sa+"").collect(Collectors.toList()));
            weights.setBorder(BorderFactory.createTitledBorder("Weight"));

            JPanel jsp = new JPanel();
            jsp.setLayout(new GridLayout(4,1,10,10));
            jsp.add(name);
            jsp.add(gender);
            jsp.add(category);
            jsp.add(weights);

            confirmation("Please enter Player details.", ()->jsp);

            try{
                Person person = new Person();
                person.setId(Utility.getRandom());
                person.setName(name.getText());
                person.setGender(Gender.valueOf(gender.getSelectedItem().toString()));
                person.setCategories(Categories.valueOf(category.getSelectedItem().toString()));
                person.setWeight(Double.valueOf(weights.getSelectedItem().toString()));
                this.persons.add(person);
                db.insert(fileKey,this.persons);
                alert("Person "+person.getName()+" created successfully....!");
            }catch (Exception e){
                e.printStackTrace();
                alert(e.getMessage());
            }
        });
        JPanel westPan = new JPanel();
        westPan.setLayout(new FlowLayout());
        westPan.add(this.loger);

        JPanel eastPan = new JPanel();
        westPan.setLayout(new FlowLayout());
        eastPan.add(deletePlayer);
        eastPan.add(createPerson);

        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(westPan, BorderLayout.WEST);
        jp.add(eastPan,BorderLayout.EAST);
        this.add(jp, BorderLayout.SOUTH);
    }

    public void addDetails(Db db){
        List<Map<Person, Person>> entries = new LinkedList<>();
        Map<String, ActionListener> actions = new LinkedHashMap<>();
        BiConsumer<JTable, TableModelEvent> consumer = (table,modelEvet)->{
            int row = modelEvet.getFirstRow();
            int column = 0;
            if(table.getRowCount()<=0) return;
            String personId = table.getValueAt(row,column).toString();
            System.out.println("Selected person "+personId);
            Person existingPerson = this.persons.stream().filter(people->people.getId()==Long.parseLong(personId)).collect(Collectors.toList()).get(0);
            if(existingPerson!=null){
                int firstRow = modelEvet.getFirstRow();
                int firstColumn = modelEvet.getColumn();
                System.out.println("Col "+firstColumn+" / "+firstRow);
                if(firstColumn==-1 || firstRow==-1) return;
                Object valueAt = table.getValueAt(firstRow, firstColumn);
                if(valueAt==null) return;
                String changedEvents = valueAt.toString();
                switch (modelEvet.getColumn()) {
                    case 1:
                        existingPerson.setName(changedEvents);
                        break;
                    case 2:
                        try {
                            Gender gender = Gender.valueOf(changedEvents);
                            existingPerson.setGender(gender);
                        } catch (Exception e) {
                            e.printStackTrace();
                            alert("Invalid Gender found at Row " + modelEvet.getFirstRow() + ", please update with" + Arrays.stream(Gender.values()).collect(Collectors.toList()));
                        }
                        break;
                    case 3:
                        Categories categories = null;
                        try {
                            categories = Categories.valueOf(changedEvents);
                            existingPerson.setCategories(categories);
                        } catch (Exception e) {
                            e.printStackTrace();
                            alert("Invalid Gender found at Row " + modelEvet.getFirstRow() + ", please update with" + Arrays.stream(Categories.values()).collect(Collectors.toList()));
                        }
                        break;
                    case 4:
                        try {
                            Double weight = Double.parseDouble(changedEvents);
                            existingPerson.setWeight(weight);
                        } catch (Exception e) {
                            e.printStackTrace();
                            alert("Invalid weight found at " + modelEvet.getFirstRow() + ", Please provide valid number");
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
