package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Event;
import org.dynamics.model.*;
import org.dynamics.reports.EventReport;
import org.dynamics.util.Utility;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BouteFrame extends CommonFrame{
    private Db db;
    private  JPanel leftButtons ;
    private JLabel LOGGER = new JLabel();
    private Event event;
    private TablePair fixtureTableModel;
    private JPanel jscrollPanle = new JPanel(new GridBagLayout());
    private JComboBox<Item> pairedOptions = new JComboBox<>();
    private List<String> paired = new LinkedList<>();
    private JButton findButton;
    private JButton updateMatch;
    private JButton shuffle = new JButton("Shuffle");
    private JComboBox<String> rndFrom;
    private JComboBox<String> eventStatus;
    private JComboBox<String> gender;
    private JComboBox<String> category;

    public BouteFrame(String title, Db db) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        super(title);
        this.db = db;
        this.paired = this.db.keyFilterBy("Event_");
        comboBoxForItems("Events",paired,db,pairedOptions);
    }

    public void southPanel(){
        JPanel panle = new JPanel();
        panle.setLayout(new BorderLayout());
        updateMatch = new JButton("Save");
        updateMatch.setBackground(new Color(0, 64, 0));
        updateMatch.setForeground(Color.WHITE);

        JButton mergeWithFixture = new JButton("Next Match");
        mergeWithFixture.setBackground(new Color(0, 64, 0));
        mergeWithFixture.setForeground(Color.WHITE);

        JButton clear = new JButton("Clear");
        clear.setPreferredSize(new Dimension(100,20));
        clear.setBackground(Color.RED);
        clear.setForeground(Color.WHITE);
        JButton boutReport = new JButton("Generate Bout PDF");
        boutReport.setBackground(new Color(0, 0, 139));
        boutReport.setForeground(Color.WHITE);
        shuffle.setPreferredSize(new Dimension(100,20));
        shuffle.setBackground(Color.RED);
        shuffle.setForeground(Color.WHITE);

        clear.addActionListener(a->{
            if(this.event==null){
                alert("Event Not selected. Please select.");
                return;
            }
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
            List<Person> from = matches.stream().filter(m->!m.isPrimary()).map(Match::getFrom).collect(Collectors.toList());
            List<Person> to = matches.stream().filter(m->!m.isPrimary()).map(Match::getTo).collect(Collectors.toList());
            List<Person> fixtures = this.event.getFixture().getPersons();

            from.addAll(to);
            from.addAll(matches.stream().filter(Match::isPrimary).map(Match::getFrom).collect(Collectors.toList()));
//            from.addAll(fixtures);

            System.out.println("From size is "+from.size());
            from = from.stream().distinct().collect(Collectors.toList());
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
                            Configuration configuration = db.findObject("configuration");
                            report = new EventReport(saveFile.get().concat(".pdf"),configuration);
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
            System.out.println("Next Match Event is "+this.event.getTeamName());
            try {
                Event dbEvent = db.findObject("Event_"+this.event.getId());
                String eventName = dbEvent.getEventName().concat("(").concat(dbEvent.getId().toString()).concat(")");
                List<Person> fixture = dbEvent.getFixture().getPersons();

                List<Person> succesors = new LinkedList<>();
                dbEvent.getMatcher().getMatches().forEach(ass->{
                    succesors.add(ass.getSuccessor());
                });
                boolean isSuccssorNotPrepared = succesors.stream().anyMatch(s->s.getId()==0);

                if(isSuccssorNotPrepared){
                    alert("Kindly finish the event ("+eventName+") to move next.");
                }else{
                    if(succesors.size()<=1){
                        Corner successorCorner = dbEvent.getMatcher().getMatches().stream().map(match ->{
                            if(match.getFrom().getId()==match.getSuccessor().getId()) return match.getFromCorner();
                            else return match.getToCorner();
                        } ).collect(Collectors.toList()).get(0);
                        Person winner = succesors.get(0);
                        dbEvent.getMatcher().setWinner(winner);
                        dbEvent.getMatcher().setWinnerCorder(successorCorner);
                        dbEvent.setStatus(Status.FINISHED);
                        try {
                            this.db.insert("Event_"+dbEvent.getId(), dbEvent);

                            JPanel panel = new JPanel();
                            panel.setBackground(successorCorner.getColor()); // Set background color
                            JLabel wins = new JLabel(winner.getName());

                            if(successorCorner==Corner.RED){
                                wins.setForeground(Color.WHITE);
                            }else{
                                wins.setForeground(Color.WHITE);
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
                            Event nEvent = eventPanel(succesors,db,dbEvent,dbEvent.getSelectedGenderCategory(),dbEvent.getSelecetedEventCategory(),dbEvent.getTeamName());
                            if(nEvent==null) return;
                            List<String> paired = this.db.keyFilterBy("Event_");
                            DefaultComboBoxModel<Item> comboBoxModel = new DefaultComboBoxModel<Item>();
                            List<Item> sortedItems = new LinkedList<>();
                            sortedItems.add(new Item(0L, ""));
                            paired.forEach(s->{
                                try {
                                    Event event = db.findObject(s);
                                    String description = event.getEventName().concat("("+event.getTeamName()+") ("+event.getRoundOf()+")"+" ("+event.getStatus()+")");
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
                            String categorySelectedItem = category.getSelectedItem()!=null? category.getSelectedItem().toString():"";
                            String genderSelectedItems = gender.getSelectedItem()!=""?gender.getSelectedItem().toString():"";
                            String roundOffSelectedItem = rndFrom.getSelectedItem()!=""?rndFrom.getSelectedItem().toString():"";
                            String statusItem = eventStatus.getSelectedItem()!=""?eventStatus.getSelectedItem().toString():"";
                            findActions(genderSelectedItems,categorySelectedItem,roundOffSelectedItem,statusItem);
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
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
        });
        JPanel rightButtons = new JPanel(new FlowLayout());
        leftButtons = new JPanel(new FlowLayout());
        leftButtons.add(clear);
        leftButtons.add(shuffle);
        leftButtons.add(boutReport);
        rightButtons.add(updateMatch);
        rightButtons.add(mergeWithFixture);

        panle.add(LOGGER,BorderLayout.CENTER);
        panle.add(leftButtons, BorderLayout.WEST);
        panle.add(rightButtons, BorderLayout.EAST);
        add(panle,BorderLayout.SOUTH);
    }

    public void northPanel(){

        JPanel jsp = new JPanel();
        jsp.setBorder(BorderFactory.createTitledBorder("Find"));
        jsp.setBackground(Color.WHITE);
        jsp.setLayout(new GridLayout(2,2));


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

                //ensure that the parent event is present of not
                List<Event> events = Utility.findListOfEvents(this.event);
                if(!events.isEmpty()){
                    jscrollPanle.removeAll();
                    jscrollPanle.revalidate();
                    jscrollPanle.repaint();

                    JDialog jd = Utility.jDialog("Loading, Please wait....!");
                    jd.setVisible(true);
                    System.out.println("List of events "+events);
                    for(Event ev: events){
                        System.out.println(ev.getEventName());
                        Matcher matcher = ev.getMatcher();
                        boolean isLastEvent = events.indexOf(ev)==events.size()-1;
                        System.out.println("Is Last Event "+isLastEvent);


                        List<Match> matches = matcher.getMatches();
                        JPanel innerPanle = new JPanel();
                        innerPanle.setLayout(new GridLayout(matches.size(),events.size(),2,2));

                        List<Person> completePlayers = matcher.getInitialPlayersList();

                        matches.forEach(match -> {
                            Person fromPerson = match.getFrom();
                            Person toPerson = match.getTo();
                            String fromText = fromPerson.getName().concat(" ("+fromPerson.getTeamName()+")");
                            String toText = toPerson.getName().concat(" ("+toPerson.getTeamName()+")");

                            System.out.println("from text "+fromText);
                            System.out.println("to text "+toText);

                            JButton toButton = new JButton(toText);
                            toButton.setMaximumSize(new Dimension(300,50));
                            toButton.setBackground(match.getToCorner().getColor());
                            toButton.setForeground(Color.WHITE);


                            JButton fromButton = new JButton(fromText);
                            fromButton.setMaximumSize(new Dimension(300,50));
                            fromButton.setBackground(match.getFromCorner().getColor());
                            fromButton.setForeground(Color.WHITE);

                            JButton successorButton = new JButton("NA");
                            if(isLastEvent){
                                successorButton.setVisible(true);
                                Person successor = match.getSuccessor();
                                if(successor.getId()!=0){
                                    successorButton.setText(successor.getName());
                                }
                                if(successor.getId()==fromPerson.getId()){
                                    successorButton.setText(fromText);
                                    successorButton.setForeground(Color.WHITE);
                                    successorButton.setBackground(match.getFromCorner().getColor());
                                }else if(successor.getId()==toPerson.getId()){
                                    successorButton.setText(toText);
                                    successorButton.setForeground(Color.WHITE);
                                    successorButton.setBackground(match.getToCorner().getColor());
                                }
                            }else{
                                successorButton.setVisible(false);
                            }

                            JPanel matcherPanel = new JPanel();

                            if(match.isPrimary()){
                                TitledBorder titledBorder = BorderFactory.createTitledBorder("Bye #"+match.getMatchId());
                                titledBorder.setTitleFont(new Font("Comic Sans MS", Font.ITALIC | Font.BOLD, 12));
                                titledBorder.setTitleColor(new Color(204, 85, 51));
                                matcherPanel.setBorder(titledBorder);
                            }else{
                                TitledBorder titledBorder = BorderFactory.createTitledBorder("Match #"+match.getMatchId());
                                titledBorder.setTitleFont(new Font("Comic Sans MS", Font.ITALIC | Font.BOLD, 12));
                                titledBorder.setTitleColor(new Color(204, 85, 51));
                                matcherPanel.setBorder(titledBorder);
                            }
                            matcherPanel.setLayout(new GridLayout(3,2,0,0));
                            matcherPanel.add(fromButton);


                            if(isLastEvent ){
                                ImageIcon imageIcon = Utility.getImageIcon("/edit.png");
                                Image scaledImage = imageIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                                JButton editButton = new JButton(new ImageIcon(scaledImage));
                                editButton.setPreferredSize(new Dimension(20,20));
                                JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Use FlowLayout for button control
                                panel.add(editButton);
                                matcherPanel.add(panel);
                                editButton.addActionListener(e->{
                                    JPanel editPanle = new JPanel();
                                    editPanle.setLayout(new GridLayout(3,1));
                                    JComboBox<Item> fromComboBox = new JComboBox<>();
                                    comboxBoxForGenericItem("Blue Corner",completePlayers,fromComboBox,fromPerson);
                                    editPanle.add(fromComboBox);
                                    JComboBox<Item> toComboBox = new JComboBox<>();
                                    comboxBoxForGenericItem("Red Corner",completePlayers,toComboBox,toPerson);
                                    editPanle.add(toComboBox);
                                    JLabel alertOptins = new JLabel("<html>Manual allocations are not recommended<br/> due to Manual errors.</html>");
                                    alertOptins.setFont(new Font("Serif",Font.BOLD,15));
                                    alertOptins.setForeground(Color.RED);
                                    editPanle.add(alertOptins);
                                    int resul = confirmation("Manual Allocation for Match  #"+match.getMatchId(),()->editPanle);
                                    if(resul == JOptionPane.YES_OPTION){
                                        Vector<Vector<Object>> rows = new Vector<>();
                                        try {
                                            Utility.getEventRows(db,"Event_"+ev.getId(),rows);
                                            if(rows.size()>1){
                                                alert("Sub events are already exists, Manual Allocations won't support once sub events are generated.");
                                                return;
                                            }
                                        } catch (Exception ex) {
                                            alert(ex.getMessage());
                                            ex.printStackTrace();
                                        }


                                        if(match.getSuccessor().getId()!=0){
                                            int result = JOptionPane.showConfirmDialog(null, "Already Winner chosen, Do you want re-allocate?");
                                            if(result == JOptionPane.YES_OPTION){
                                                Long fromId = ((Item) Objects.requireNonNull(fromComboBox.getSelectedItem())).getId();
                                                Long toId = ((Item) Objects.requireNonNull(toComboBox.getSelectedItem())).getId();
                                                match.setFrom(Utility.getPesonById(completePlayers,fromId));
                                                match.setTo(Utility.getPesonById(completePlayers,toId));
                                                match.setSuccessor(new Person());
                                                successorButton.doClick();
                                                this.updateMatch.doClick();
                                                this.findButton.doClick();
                                            }
                                        }else{
                                            Long fromId = ((Item) Objects.requireNonNull(fromComboBox.getSelectedItem())).getId();
                                            Long toId = ((Item) Objects.requireNonNull(toComboBox.getSelectedItem())).getId();
                                            if(fromId!=0){
                                                match.setFrom(Utility.getPesonById(completePlayers,fromId));
                                            }
                                            if(toId!=0){
                                                match.setTo(Utility.getPesonById(completePlayers,toId));
                                            }
                                            this.updateMatch.doClick();
                                            this.findButton.doClick();
                                        }
                                    }
                                });
                            }
                            else{
                                matcherPanel.add(new JLabel(" "));
                            }

                            matcherPanel.add(new JLabel(" "));
                            matcherPanel.add(successorButton);
                            matcherPanel.add(toButton);
                            matcherPanel.add(new JLabel(" "));

                            innerPanle.add(matcherPanel);

                            //listeners
                            fromButton.addActionListener(e->{
                                System.out.println("updated ");
                                match.setSuccessor(fromPerson);
                                successorButton.setText(fromText);
                                successorButton.setForeground(Color.WHITE);
                                successorButton.setBackground(match.getFromCorner().getColor());
                            });
                            toButton.addActionListener(e->{
                                System.out.println("updated ");
                                match.setSuccessor(toPerson);
                                successorButton.setText(toText);
                                successorButton.setForeground(Color.WHITE);
                                successorButton.setBackground(match.getToCorner().getColor());
                            });
                            successorButton.addActionListener(e->{
                                match.setSuccessor(new Person());
                                successorButton.setText("NA");
                                successorButton.setForeground(Color.WHITE);
                                successorButton.setBackground(Color.BLACK);
                            });
                            if(match.isPrimary() && isLastEvent && match.getSuccessor().getId()==0){
                                successorButton.setText(fromText);
                                successorButton.setForeground(Color.WHITE);
                                match.setSuccessor(fromPerson);
                                if(matches.indexOf(match)%2==0){
                                    successorButton.setBackground(match.getToCorner().getColor());
                                }else{
                                    successorButton.setBackground(match.getFromCorner().getColor());
                                }

//                                fromButton.doClick();
                            }
                            //end of listeners
                        });

                        jscrollPanle.add(innerPanle);
                      /*  Utility.converter(event.getFixture().getPersons()).forEach(vec->{
                            this.fixtureTableModel.getDefaultTableModel().addRow(vec);
                        });*/
                    }
                    jd.setVisible(false);
                    if(this.event!=null && this.event.getParentEvent()==null){
                        leftButtons.setVisible(true);
                    }else
                    {
                        leftButtons.setVisible(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }

        });


        category = comboBox(Arrays.stream(Categories.values()).map(as->as.toString()).collect(Collectors.toList()));
        category.setBorder(BorderFactory.createTitledBorder("Category"));

        gender =  comboBox(Arrays.stream(Gender.values()).map(as->as.toString()).collect(Collectors.toList()));
        gender.setBorder(BorderFactory.createTitledBorder("Gender"));

        List<String> roundOffList =  IntStream.rangeClosed(1, 20)
                .mapToObj(i -> ((int) Math.pow(2, i))+"")
                .collect(Collectors.toList());

        rndFrom = comboBox(roundOffList);
        rndFrom.setBorder(BorderFactory.createTitledBorder("Round off"));

        eventStatus = comboBox(Arrays.stream(Status.values()).map(Enum::toString).collect(Collectors.toList()));
        eventStatus.setBorder(BorderFactory.createTitledBorder("Status"));

        category.addActionListener(a->{
            String categorySelectedItem = category.getSelectedItem()!=null? category.getSelectedItem().toString():"";
            String genderSelectedItems = gender.getSelectedItem()!=""?gender.getSelectedItem().toString():"";
            String roundOffSelectedItem = rndFrom.getSelectedItem()!=""?rndFrom.getSelectedItem().toString():"";
            String statusItem = eventStatus.getSelectedItem()!=""?eventStatus.getSelectedItem().toString():"";
            findActions(genderSelectedItems,categorySelectedItem,roundOffSelectedItem,statusItem);
        });

        gender.addActionListener(a->{
            String categorySelectedItem = category.getSelectedItem()!=null? category.getSelectedItem().toString():"";
            String genderSelectedItems = gender.getSelectedItem()!=""?gender.getSelectedItem().toString():"";
            String roundOffSelectedItem = rndFrom.getSelectedItem()!=""?rndFrom.getSelectedItem().toString():"";
            String statusItem = eventStatus.getSelectedItem()!=""?eventStatus.getSelectedItem().toString():"";
            findActions(genderSelectedItems,categorySelectedItem,roundOffSelectedItem,statusItem);
        });

        rndFrom.addActionListener(a->{
            String categorySelectedItem = category.getSelectedItem()!=null? category.getSelectedItem().toString():"";
            String genderSelectedItems = gender.getSelectedItem()!=""?gender.getSelectedItem().toString():"";
            String roundOffSelectedItem = rndFrom.getSelectedItem()!=""?rndFrom.getSelectedItem().toString():"";
            String statusItem = eventStatus.getSelectedItem()!=""?eventStatus.getSelectedItem().toString():"";
            findActions(genderSelectedItems,categorySelectedItem,roundOffSelectedItem,statusItem);
        });
        eventStatus.addActionListener(a->{
            String categorySelectedItem = category.getSelectedItem()!=null? category.getSelectedItem().toString():"";
            String genderSelectedItems = gender.getSelectedItem()!=""?gender.getSelectedItem().toString():"";
            String roundOffSelectedItem = rndFrom.getSelectedItem()!=""?rndFrom.getSelectedItem().toString():"";
            String statusItem = eventStatus.getSelectedItem()!=""?eventStatus.getSelectedItem().toString():"";
            findActions(genderSelectedItems,categorySelectedItem,roundOffSelectedItem,statusItem);
        });
        jsp.add(category);
        jsp.add(gender);
        jsp.add(rndFrom);
        jsp.add(eventStatus);
        jsp.add(pairedOptions);


        jsp.add(this.findButton);
        this.add(jsp,BorderLayout.NORTH);
    }


    public void findActions(String genderSelectedItem, String categorySelectedItem, String roundOff, String eventStatus){
        if(!genderSelectedItem.isEmpty() || !categorySelectedItem.isEmpty()|| !roundOff.isEmpty() || !eventStatus.isEmpty()){
            int roundOffValue = roundOff.isEmpty()?0:Integer.parseInt(roundOff);
            this.pairedOptions.removeAllItems();
            this.pairedOptions.addItem(new Item(0L,""));
            this.paired = this.db.keyFilterBy("Event_");
            List<Event> existingPairs = this.paired.stream().map(pair -> {
                        try {
                            return (Event) db.findObject(pair);
                        } catch (Exception e) {
                            e.printStackTrace();
                            alert(e.getMessage());
                        }
                        return null;
                    })
                    .filter(genderFilter -> {
                        if (!genderSelectedItem.isEmpty()) {
                            String gnCat = genderFilter.getSelectedGenderCategory() != null ? genderFilter.getSelectedGenderCategory().toString() : "";
                            return gnCat.equalsIgnoreCase(genderSelectedItem);
                        } else {
                            return true;
                        }
                    }).filter(categoryFilter -> {
                        if (!categorySelectedItem.isEmpty()) {
                            String gnCat = categoryFilter.getSelecetedEventCategory() != null ? categoryFilter.getSelecetedEventCategory().toString() : "";
                            return gnCat.equalsIgnoreCase(categorySelectedItem);
                        } else {
                            return true;
                        }
                    }).filter(Objects::nonNull).filter(roundOffFilter->{
                        if(roundOffValue!=0){
                            return Objects.equals(roundOffFilter.getRoundOf(), roundOffValue);
                        }else{
                            return true;
                        }
                    }).filter(Objects::nonNull).filter(eventFilter->{
                        if(!Objects.equals(eventStatus, "")){
                            return Objects.equals(eventFilter.getStatus().toString(), eventStatus);
                        }else{
                            return true;
                        }
                    })
                    .collect(Collectors.toList());

            existingPairs.forEach(es->{
                try {
                    String description = es.getEventName().concat("("+es.getTeamName()+") ("+es.getRoundOf()+")"+" ("+es.getStatus()+")");
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

//        jscrollPanle.setLayout(new GridBagLayout());

        JPanel fixterPanel = new JPanel();
        fixterPanel.setLayout(new BorderLayout());


        Vector<String> matcherColumn = new Vector<>();
        matcherColumn.add("Red Corner");
        matcherColumn.add("Blue Corner");
        matcherColumn.add("Winner");
        this.fixtureTableModel= createTable(fixterPanel, new Vector<>(),Person.keys(),()->new LinkedHashMap<>(),null);
        JTabbedPane pane = new JTabbedPane();
        pane.setBackground(Color.WHITE);
        JScrollPane sc = new JScrollPane(jscrollPanle);
        sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sc.getVerticalScrollBar().setUnitIncrement(50);
        sc.getHorizontalScrollBar().setUnitIncrement(50);
        sc.setBackground(Color.WHITE);
        pane.add("Matcher List", sc);
        this.add(pane, BorderLayout.CENTER);
    }
}
