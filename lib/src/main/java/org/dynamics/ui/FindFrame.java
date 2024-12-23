package org.dynamics.ui;

import com.itextpdf.text.DocumentException;
import org.dynamics.db.Db;
import org.dynamics.model.*;
import org.dynamics.model.Event;
import org.dynamics.reports.EventReport;
import org.dynamics.reports.FixturesPdf;
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
import java.util.stream.Stream;

public class FindFrame extends CommonFrame{
    private List<Person> persons;
    private List<Person> filteredPersons;
    private TablePair tableModel;
    private JLabel loger ;
    private JButton find;
    private FileImport fileKey;
    private Gender eventGender;
    private Categories eventCateogory;
    private String weightFrom = "";
    private String weightTo = "";;
    public FindFrame(String title, List<Person> persons, FileImport fileKey) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
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
        jsp.setLayout(new GridLayout(3,3));
        jsp.setBorder(BorderFactory.createTitledBorder("Find"));
        jsp.setBackground(Color.WHITE);

        JTextField ids = textField();
        ids.setBorder(BorderFactory.createTitledBorder("Player Id"));
        jsp.add(ids);

        //name filter
        JTextField nameField = textField();
        nameField.setBorder(BorderFactory.createTitledBorder("Name"));
        jsp.add(nameField);


        //name filter
        JTextField teamName = textField();
        teamName.setBorder(BorderFactory.createTitledBorder("Team Name"));
        jsp.add(teamName);

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
        JTextField weightFrom = numbrFiled();
        weightFrom.setBorder(BorderFactory.createTitledBorder("From"));
        weightPanel.add(weightFrom);

        JTextField weightTo = numbrFiled();
        weightTo.setBorder(BorderFactory.createTitledBorder("To"));
        weightPanel.add(weightTo);

        weightPanel.setBorder(BorderFactory.createTitledBorder("Weight Range"));
        jsp.add(weightPanel);

        //submit button
        this.find = new JButton("Find");
        this.find.addActionListener(a->{
            String selectedName = nameField.getText();
            String selectedId = ids.getText().toString();
            String selectedTeamName = teamName.getText().toString();
            String selecetdGender = genderBox.getSelectedItem().toString();
            String selectedCategory = categoresBox.getSelectedItem().toString();
            String selectedFrom = weightFrom.getText().toString();
            String selectedTo = weightTo.getText().toString();
            filteredPersons = persons.stream().filter(nameFilter->{
                        if(selectedName.length()>0){
                            return nameFilter.getName().toLowerCase().contains(selectedName.toLowerCase());
                        }else{
                            return true;
                        }
                    }).filter(teamFilter->{
                        if(selectedTeamName.length()>0){
                            return teamFilter.getTeamName().toLowerCase().contains(selectedTeamName.toLowerCase());
                        }else{
                            return true;
                        }
                    }).filter(genderFilter->{
                        if(selecetdGender!=""){
                            this.eventGender = Gender.valueOf(selecetdGender);
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
                            this.eventCateogory = Categories.valueOf(selectedCategory);
                            return categoryFilter.getCategories().toString().equalsIgnoreCase(selectedCategory);
                        }else{
                            return true;
                        }
                    }).filter(weightFilter->{
                        if(selectedFrom.length()>0){
                            this.weightFrom = "+".concat(selectedFrom);
                            return weightFilter.getWeight().compareTo(Double.parseDouble(selectedFrom))>=0;
                        }else{
                            this.weightFrom = "";
                            return true;
                        }
                    }).filter(weightFilter->{
                        if(selectedTo.length()>0){
                            this.weightTo = "-".concat(selectedTo);
                            return weightFilter.getWeight().compareTo(Double.parseDouble(selectedTo)) <= 0;
                        }else{
                            this.weightTo = "";
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
        createPerson.setBackground(new Color(0, 64, 0));
        createPerson.setForeground(Color.WHITE);

        JButton deletePlayer = new JButton("Delete Players");
        JButton report = new JButton("Generate PDF");
        report.setBackground(new Color(0, 0, 139));
        report.setForeground(Color.WHITE);


        deletePlayer.setBackground(Color.RED);
        deletePlayer.setForeground(Color.WHITE);
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
                            fileKey.setPerson(this.persons);
                            db.insert("File_"+fileKey.getId(),fileKey);
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

        report.addActionListener(e->{
            Optional<String> saveFile = fileSaver();
            if(saveFile.isPresent()){

                FixturesPdf reporter = null;
                try {
                    final Configuration configuration = db.findObject("configuration");
                    String title = (String)configuration.get("title");
                    List<Person> peoples = this.filteredPersons.size()>0?this.filteredPersons:this.persons;
                    Event dummyEvent = new Event();
                    dummyEvent.setEventName(title);
                    dummyEvent.setTeamName(title);
                    Matcher matcher = new Matcher();
                    matcher.setMatches(new LinkedList<>());
                    dummyEvent.setMatcher(matcher);
                    Fixture fixture = new Fixture();
                    fixture.setPersons(peoples);
                    dummyEvent.setFixture(fixture);
                    reporter = new FixturesPdf(saveFile.get().concat(".pdf"),configuration);
                    reporter.generateReport(dummyEvent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    alert(ex.getMessage());
                }
            }

        });
        createPerson.addActionListener(a->{
            JTextField name = textField();
            name.setBorder(BorderFactory.createTitledBorder("Player Name"));
            JTextField teamName = textField();
            teamName.setBorder(BorderFactory.createTitledBorder("Team Name"));
            JComboBox<String> gender =  comboBox(Arrays.stream(Gender.values()).map(as->as.toString()).collect(Collectors.toList()));
            gender.setBorder(BorderFactory.createTitledBorder("Gender"));
            JComboBox<String> category = comboBox(Arrays.stream(Categories.values()).map(as->as.toString()).collect(Collectors.toList()));
            category.setBorder(BorderFactory.createTitledBorder("Category"));
            JTextField weights = numbrFiled();
            weights.setBorder(BorderFactory.createTitledBorder("Weight"));

            JPanel jsp = new JPanel();
            jsp.setLayout(new GridLayout(5,1,10,10));
            jsp.add(name);
            jsp.add(teamName);
            jsp.add(gender);
            jsp.add(category);
            jsp.add(weights);

            int result = confirmation("Please enter Player details.", ()->jsp);

            if(result==JOptionPane.CANCEL_OPTION){
                return;
            }
            try{
                Object selectedGender = gender.getSelectedItem();
                Object selectedCategory = category.getSelectedItem();
                Object selectedWeight = weights.getText();
                Person person = new Person();
                person.setId(Utility.getRandom());
                person.setName(name.getText());
                person.setTeamName(teamName.getText());
                if(selectedGender!=""){
                    person.setGender(Gender.valueOf(gender.getSelectedItem().toString()));
                }
                if(selectedCategory!=""){
                    person.setCategories(Categories.valueOf(category.getSelectedItem().toString()));
                }
                if(selectedWeight!=""){
                    person.setWeight(Double.valueOf(weights.getText().toString()));
                }
                if(person.isValid()){
                    this.persons.add(person);
                    fileKey.setPerson(this.persons);
                    fileKey.setTotalCount(fileKey.getTotalCount()+1);
                    fileKey.setStatus(Status.FINISHED);
                    db.insert("File_"+fileKey.getId(),fileKey);
                    alert("Person "+person.getName()+" created successfully....!");
                    this.find.doClick();
                    createPerson.doClick();
                }else {
                    alert("Please fill all the fields.");
                }

            }catch (Exception e){
                e.printStackTrace();
                alert(e.getMessage());
            }
        });
        JPanel westPan = new JPanel();
        westPan.setLayout(new FlowLayout());
        westPan.add(report);
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
                    fileKey.setPerson(this.persons);
                    db.insert("File_"+this.fileKey.getId(),fileKey);
                } catch (IOException e) {
                    e.printStackTrace();
                    alert(e.getMessage());
                }
            }

        };
        actions.put("Create Fixtures",(event)->{
            List<Person> peoples = this.filteredPersons.size()>0?this.filteredPersons:this.persons;
            try {
                String eventWeight = Stream.of(this.weightFrom,this.weightTo).filter(a-> !Objects.equals(a, "")).collect(Collectors.joining(""));
                eventPanel(peoples,db,null,eventGender,eventCateogory,eventWeight);
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
        });
        this.tableModel =  createTable(this,Utility.converter(persons),Person.keys(),()->actions,consumer);
    }
}
