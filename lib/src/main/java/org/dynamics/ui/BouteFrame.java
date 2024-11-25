package org.dynamics.ui;

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

public class BouteFrame extends CommonFrame{
    private Db db;
    private Event event;
    private TablePair fixtureTableModel;
    private JPanel jscrollPanle = new JPanel();
    private JComboBox<Item> pairedOptions;
    private JButton findButton;
    public BouteFrame(String title, Db db) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
        this.db = db;
        List<String> paired = this.db.keyFilterBy("Event_");
        pairedOptions = comboBoxForItems("Events",paired,db);
    }

    public void southPanel(){
        JPanel panle = new JPanel();
        panle.setLayout(new BorderLayout());
        JButton updateMatch = new JButton("Save");
        updateMatch.setBackground(Color.GREEN);

        JButton mergeWithFixture = new JButton("Next Match");
        mergeWithFixture.setBackground(Color.GREEN);

        JButton shuffle = new JButton("Shuffle");
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
                            Event nEvent = eventPanel(succesors,db,dbEvent);
                            List<String> paired = this.db.keyFilterBy("Event_");
                            DefaultComboBoxModel<Item> comboBoxModel = new DefaultComboBoxModel<Item>();
                            List<Item> sortedItems = new LinkedList<>();
                            sortedItems.add(new Item(0L, "description"));
                            paired.forEach(s->{
                                try {
                                    Event event = db.findObject(s);
                                    String description = event.getEventName();
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
                }else{
                    alert("Event Not selected. Please select.");
                }

            } catch (IOException e) {
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
                    JButton clearSelection = new JButton("clear");
                    clearSelection.addActionListener(e->{
                        buttonGroup.clearSelection();
                        match.setSuccessor(new Person());
                    });
                    buttonGroup.add(clearSelection);
                    JPanel radioPanel = new JPanel();
                    radioPanel.setLayout(new BoxLayout(radioPanel,BoxLayout.X_AXIS));
                    radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    radioPanel.add(fromRadioButton);
                    radioPanel.add(toRadioButton);
                    radioPanel.add(clearSelection);

                    TitledBorder titleBorder = BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.BLACK, 0),
                            "(".concat(match.getMatchId().toString()).concat(")"),
                            TitledBorder.DEFAULT_JUSTIFICATION,
                            TitledBorder.DEFAULT_POSITION,
                            new Font("Serif",Font.ITALIC ,15),
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
        jsp.add(pairedOptions);
        jsp.add(this.findButton);
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
        this.fixtureTableModel= createTable(fixterPanel, new Vector<>(),Person.keys(),()->new LinkedHashMap<>(),null);
        JTabbedPane pane = new JTabbedPane();
        pane.add("Matcher List", new JScrollPane(jscrollPanle));
        pane.add("Fixture",fixterPanel);
        this.add(pane, BorderLayout.CENTER);
    }
}
