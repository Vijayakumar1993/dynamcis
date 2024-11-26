package org.dynamics.ui;

import org.checkerframework.checker.units.qual.C;
import org.dynamics.db.Db;
import org.dynamics.model.Event;
import org.dynamics.model.*;
import org.dynamics.reports.EventReport;
import org.dynamics.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BouteFrame extends CommonFrame{
    private Db db;
    private Event event;
    private TablePair fixtureTableModel;
    private JPanel jscrollPanle = new JPanel();
    private JComboBox<Item> pairedOptions = new JComboBox<>();
    private List<String> paired = new LinkedList<>();
    private JButton findButton;
    private JButton shuffle = new JButton("Shuffle");
    public BouteFrame(String title, Db db) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
        this.db = db;
        this.paired = this.db.keyFilterBy("Event_");
        comboBoxForItems("Events",paired,db,pairedOptions);
    }

    public void southPanel(){
        JPanel panle = new JPanel();
        panle.setLayout(new BorderLayout());
        JButton updateMatch = new JButton("Save");
        updateMatch.setBackground(Color.GREEN);

        JButton mergeWithFixture = new JButton("Next Match");
        mergeWithFixture.setBackground(Color.GREEN);

        JButton clear = new JButton("Clear");
        clear.setPreferredSize(new Dimension(100,20));
        clear.setBackground(Color.RED);
        JButton boutReport = new JButton("Generate Bout PDF");
        shuffle.setPreferredSize(new Dimension(100,20));
        shuffle.setBackground(Color.RED);

        clear.addActionListener(a->{
            this.event.getMatcher().getMatches().forEach(match->{
                match.setSuccessor(new Person());
            });
            try {
                db.insert("Event_"+this.event.getId(),this.event);
            } catch (IOException e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
            this.findButton.doClick();
        });
        shuffle.addActionListener(e->{
            if(this.event==null){
                alert("Event Not selected. Please select.");
                return;
            }

            try {
                this.event =  db.findObject("Event_"+this.event.getId());
            } catch (Exception ex) {
                ex.printStackTrace();
                alert(ex.getMessage());
            }
            if(this.event.getMatcher().getMatches().stream().anyMatch(a->a.getSuccessor().getId()!=0)){
                alert("Match Started, You can't shuffle.");
                return;
            }
            Event existingEvent = this.event;
            Event newEvent = new Event();
            newEvent.setParentEvent(existingEvent.getParentEvent());
            newEvent.setEventName(this.event.getEventName());
            newEvent.setDescription(this.event.getDescription());
            newEvent.setTeamName(this.event.getTeamName());
            newEvent.setId(this.event.getId());

            //take matched persons and fixture and shuffle and save, re run
            List<Match> matches = this.event.getMatcher().getMatches();
            List<Person> from = matches.stream().map(Match::getFrom).collect(Collectors.toList());
            List<Person> to = matches.stream().map(Match::getTo).collect(Collectors.toList());
            List<Person> fixtures = this.event.getFixture().getPersons();

            from.addAll(to);
            from.addAll(fixtures);

            Collections.shuffle(from);
            db.delete("Event_"+this.event.getId());
            Utility.createEvent(from,newEvent);
            try {
                db.insert("Event_"+newEvent.getId().toString(),newEvent);
            } catch (Exception ex) {
                ex.printStackTrace();
                alert(ex.getMessage());
            }
            if(this.findButton!=null){
                this.findButton.doClick();
            }
        });
        boutReport.addActionListener(d->{
            EventReport report = null;
            try {
                if(this.event!=null){
                    Event ev = db.findObject("Event_"+this.event.getId());
                    if(ev!=null){
                        Optional<String> saveFile = fileSaver();
                        if(saveFile.isPresent()){
                            report = new EventReport(saveFile.get().concat(".pdf"));
                            report.generateReport(ev);
                        }
                    }
                }else {
                    alert("Event Not selected. Please select.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }



        });
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
                            Event nEvent = eventPanel(succesors,db,dbEvent,dbEvent.getSelectedGenderCategory(),dbEvent.getSelecetedEventCategory());
                            List<String> paired = this.db.keyFilterBy("Event_");
                            DefaultComboBoxModel<Item> comboBoxModel = new DefaultComboBoxModel<Item>();
                            List<Item> sortedItems = new LinkedList<>();
                            sortedItems.add(new Item(0L, "description"));
                            paired.forEach(s->{
                                try {
                                    Event event = db.findObject(s);
                                    String description = event.getEventName().concat("("+event.getTeamName()+")");
                                    sortedItems.add(new Item(event.getId(), description));
                                } catch (Exception e) {
                                    alert(e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                            sortedItems.sort(Comparator.comparing(Item::getDescription));
                            sortedItems.forEach(model->{
                                comboBoxModel.addElement(model);
                                System.out.println(model.getId()+" ------- "+nEvent.getId());
                                if(model.getId().equals(nEvent.getId())){
                                    System.out.println("Equal id found to select");
                                    comboBoxModel.setSelectedItem(model);
                                }
                            });
                            pairedOptions.setModel(comboBoxModel);
                            this.findButton.doClick();
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
                    db.findObject("Event_"+this.event.getId().toString());
                }else{
                    alert("Event Not selected. Please select.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
        });
        JPanel rightButtons = new JPanel(new FlowLayout());
        JPanel leftButtons = new JPanel(new FlowLayout());
        leftButtons.add(clear);
        leftButtons.add(shuffle);
        leftButtons.add(boutReport);
        rightButtons.add(updateMatch);
        rightButtons.add(mergeWithFixture);
        panle.add(leftButtons, BorderLayout.WEST);
        panle.add(rightButtons, BorderLayout.EAST);
        add(panle,BorderLayout.SOUTH);
    }

    public void northPanel(){

        JPanel jsp = new JPanel();
        jsp.setBorder(BorderFactory.createTitledBorder("Find"));
        jsp.setBackground(Color.WHITE);


        this.findButton = new JButton("Find");

        findButton.addActionListener(a->{
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
                innerPanle.setLayout(new BoxLayout(innerPanle,BoxLayout.Y_AXIS));
                matches.forEach(match -> {
                    ButtonGroup buttonGroup = new ButtonGroup();
                    JButton successorButton = new JButton("NA");
                    successorButton.setFont(new Font("Serif",Font.BOLD,14));
                    Person fromPerson = match.getFrom();
                    Person toPerson = match.getTo();
                    Person succesor = match.getSuccessor();
                    JButton fromRadioButton = new JButton(fromPerson.getTeamName().concat("-").concat(fromPerson.getName()).concat("(").concat(fromPerson.getId() + "").concat(")"));
//                    fromRadioButton.setBackground(match.getFromCorner().getColor());
                    fromRadioButton.setBackground(match.getFromCorner().getColor());
                    fromRadioButton.setForeground(match.getFromCorner().getColor());
                    fromRadioButton.setPreferredSize(new Dimension(100,50));
                    fromRadioButton.setFont(new Font("Serif",Font.BOLD,14));
                    System.out.println("succesor "+succesor.getId());

                    System.out.println("from person "+toPerson.getId());

                    if(succesor.getId()==fromPerson.getId()){
                        fromRadioButton.setSelected(true);
                        successorButton.setText(succesor.getTeamName().concat("-").concat(succesor.getName()).concat("(").concat(succesor.getId() + "").concat(")"));
                        successorButton.setForeground(match.getFromCorner().getColor());
                        successorButton.setBackground(match.getFromCorner().getColor());
                    }
                    if(succesor.getId()==toPerson.getId()){
                        successorButton.setText(succesor.getTeamName().concat("-").concat(succesor.getName()).concat("(").concat(succesor.getId() + "").concat(")"));
                        successorButton.setForeground(match.getToCorner().getColor());
                        successorButton.setBackground(match.getToCorner().getColor());
                    }

                    //to radio button
                    JButton toRadioButton = new JButton(toPerson.getTeamName().concat("-").concat(toPerson.getName()).concat("(").concat(toPerson.getId() + "").concat(")"));
//                    toRadioButton.setBackground(match.getToCorner().getColor());
                    toRadioButton.setBackground(match.getToCorner().getColor());
                    toRadioButton.setForeground(match.getToCorner().getColor());
                    toRadioButton.setPreferredSize(new Dimension(100,50));
                    toRadioButton.setFont(new Font("Serif",Font.BOLD,14));
                    System.out.println("succesor "+succesor.getId());

                    System.out.println("to person "+toPerson.getId());

                    toRadioButton.addActionListener(e->{
                        System.out.println("updated ");
                        match.setSuccessor(toPerson);
                        Person succes = match.getSuccessor();
                        successorButton.setText(toPerson.getTeamName().concat("-").concat(succes.getName()).concat("(").concat(succes.getId() + "").concat(")"));
                        successorButton.setForeground(match.getToCorner().getColor());
                        successorButton.setBackground(match.getToCorner().getColor());
                    });
                    fromRadioButton.addActionListener(e->{
                        System.out.println("updated ");
                        match.setSuccessor(fromPerson);
                        Person succes = match.getSuccessor();
                        successorButton.setText(fromPerson.getTeamName().concat("-").concat(succes.getName()).concat("(").concat(succes.getId() + "").concat(")"));
                        successorButton.setForeground(match.getFromCorner().getColor());
                        successorButton.setBackground(match.getFromCorner().getColor());
                    });
                    buttonGroup.add(fromRadioButton);
                    buttonGroup.add(toRadioButton);
                    JButton clearSelection = new JButton("clear");
                    successorButton.addActionListener(e->{
                        match.setSuccessor(new Person());
                        successorButton.setText("NA");
                        successorButton.setBackground(null);
                        successorButton.setForeground(null);
                    });
                    buttonGroup.add(clearSelection);
                    JPanel radioPanel = new JPanel();
                    radioPanel.setMaximumSize(new Dimension(800,30));
                    radioPanel.setLayout(new GridLayout(4,2));
                    radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    radioPanel.add(fromRadioButton);
                    radioPanel.add(new JLabel());
                    radioPanel.add(new JLabel());
                    radioPanel.add(successorButton);
                    radioPanel.add(toRadioButton);
                    radioPanel.add(new JLabel());
                    Utility.setPanelEnabled(radioPanel,match.isPrimary());

                    TitledBorder titleBorder = BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.BLACK, 0),
                            match.getMatchId().toString(),
                            TitledBorder.DEFAULT_JUSTIFICATION,
                            TitledBorder.DEFAULT_POSITION,
                            new Font("Serif",Font.BOLD ,15),
                            new Color(220, 20, 60)
                    );
                    radioPanel.setBorder(titleBorder);
                    innerPanle.setAlignmentX(Component.LEFT_ALIGNMENT);
                    innerPanle.add(radioPanel);

                    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                    separator.setPreferredSize(new Dimension(0, 1)); // Reduce height
                    separator.setBorder(new EmptyBorder(0, 0, 0, 0)); // Remove any additional padding or margin

                    innerPanle.add(separator);
                });

                jscrollPanle.add(innerPanle, BorderLayout.NORTH);

                Utility.converter(event.getFixture().getPersons()).forEach(vec->{
                    this.fixtureTableModel.getDefaultTableModel().addRow(vec);
                });
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }

        });


        JComboBox<String> category = comboBox(Arrays.stream(Categories.values()).map(as->as.toString()).collect(Collectors.toList()));
        category.setBorder(BorderFactory.createTitledBorder("Category"));

        JComboBox<String> gender =  comboBox(Arrays.stream(Gender.values()).map(as->as.toString()).collect(Collectors.toList()));
        gender.setBorder(BorderFactory.createTitledBorder("Gender"));

        category.addActionListener(a->{
            String categorySelectedItem = category.getSelectedItem()!=null? category.getSelectedItem().toString():"";
            String genderSelectedItems = gender.getSelectedItem()!=""?gender.getSelectedItem().toString():"";
            findActions(genderSelectedItems,categorySelectedItem);
        });

        gender.addActionListener(a->{
            String categorySelectedItem = category.getSelectedItem()!=null? category.getSelectedItem().toString():"";
            String genderSelectedItems = gender.getSelectedItem()!=""?gender.getSelectedItem().toString():"";
            findActions(genderSelectedItems,categorySelectedItem);
        });
        jsp.add(category);
        jsp.add(gender);
        jsp.add(pairedOptions);


        jsp.add(this.findButton);
        this.add(jsp,BorderLayout.NORTH);
    }


    public void findActions(String genderSelectedItem, String categorySelectedItem){
        if(!genderSelectedItem.isEmpty() || !categorySelectedItem.isEmpty()){
            this.pairedOptions.removeAllItems();
            this.pairedOptions.addItem(new Item(0L,""));
            this.paired = this.db.keyFilterBy("Event_");
            List<Event> existingPairs = this.paired.stream().map(pair-> {
                        try {
                            return (Event)db.findObject(pair);
                        } catch (Exception e) {
                            e.printStackTrace();
                            alert(e.getMessage());
                        }
                        return  null;
                    })
                    .filter(genderFilter->{
                        if(!genderSelectedItem.isEmpty() ){
                            String gnCat = genderFilter.getSelectedGenderCategory()!=null?genderFilter.getSelectedGenderCategory().toString():"";
                            return  gnCat.equalsIgnoreCase(genderSelectedItem);
                        }else{
                            return true;
                        }
                    }).filter(categoryFilter->{
                        if(!categorySelectedItem.isEmpty() ){
                            String gnCat = categoryFilter.getSelecetedEventCategory()!=null?categoryFilter.getSelecetedEventCategory().toString():"";
                            return  gnCat.equalsIgnoreCase(categorySelectedItem);
                        }else{
                            return true;
                        }
                    })
                    .collect(Collectors.toList());

            existingPairs.forEach(es->{
                try {
                    String description = es.getEventName().concat("("+es.getTeamName()+")");
                    this.pairedOptions.addItem(new Item(es.getId(),description));
                } catch (Exception e) {
                    e.printStackTrace();
                    alert(e.getMessage());
                }
            });
        }else{
            this.pairedOptions.removeAllItems();
            comboBoxForItems("Events",this.paired,db,this.pairedOptions);
        }
    }
    public void centerPanel(){

        jscrollPanle.setLayout(new BorderLayout());

        JPanel fixterPanel = new JPanel();
        fixterPanel.setLayout(new BorderLayout());


        Vector<String> matcherColumn = new Vector<>();
        matcherColumn.add("Red Corner");
        matcherColumn.add("Blue Corner");
        matcherColumn.add("Winner");
        this.fixtureTableModel= createTable(fixterPanel, new Vector<>(),Person.keys(),()->new LinkedHashMap<>(),null);
        JTabbedPane pane = new JTabbedPane();
        JScrollPane sc = new JScrollPane(jscrollPanle);
        sc.getVerticalScrollBar().setUnitIncrement(50);
        sc.getHorizontalScrollBar().setUnitIncrement(50);
        pane.add("Matcher List", sc);
        pane.add("Buyer",fixterPanel);
        this.add(pane, BorderLayout.CENTER);
    }
}
