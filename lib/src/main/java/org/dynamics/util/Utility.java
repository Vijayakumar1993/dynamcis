package org.dynamics.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.dynamics.db.Db;
import org.dynamics.model.Event;
import org.dynamics.model.*;
import org.dynamics.ui.CommonFrame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utility {
    private static final Logger logger = LogManager.getLogger(Utility.class);

    public static String CSV="csv";
    public static List<String> IMAGE_EXTENSION = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "ico", "svg", "heic"
    );

    public static  Map<String, String> CONSTANT_MAP;
    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("Contact", "/contactus.png");
        tempMap.put("Imports", "/list.png");
        tempMap.put("New Players", "/newplayer.png");
        tempMap.put("Search Players", "/searchplayer.png");
        tempMap.put("Search Events", "/searchevents.png");
        tempMap.put("List Events", "/listevents.png");
        tempMap.put("Configure", "/settings.png");
        tempMap.put("Import Players", "/import.png");
        CONSTANT_MAP = Collections.unmodifiableMap(tempMap);
    }

    public static List<String> CONFIGURATIONS = Stream.of("club-title","left-logo","right-logo","watermark-logo","title","website","address","phone-number").collect(Collectors.toList());
    public static Long getRandom(){
        Random random = new Random();
        long tenDigitNumber = (long)(1_000_000_000L + random.nextDouble() * 9_000_000_000L);
        return Math.abs(tenDigitNumber);
    }
    public static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        Date date = new Date();
        return dateFormat.format(date);
    }
    public static final Consumer<JLabel> CONSUMER_DEFAULT = t -> {
        // No operation performed
    };
    public Optional<List<Person>> filter(List<Person> persons){
        return Optional.of(persons);
    }

    public static Vector<Vector<Object>> converter(List<Person> persons){
        return new Vector<>(persons.stream().map(a->a.toVector()).collect(Collectors.toList()));
    }


    public static void createEvent(List<Person> persons, Event event){
        Matcher matches = new Matcher();
        List<Map<Person, Person>> op = new LinkedList<>();
        Map<String, Integer> keyPair = fixtureAndMatcher(persons.size());
        Integer fixtureSize = keyPair.get("fixture");
        Integer matcher = keyPair.get("matcher");
        Integer roundOf = keyPair.get("roundOf");

        logger.info("Matcher List " + matcher);
        logger.info("Fixture List "+fixtureSize);

        List<Event> events = findListOfEvents(event);
        Optional<Integer> sumOptional = events.stream().map(a->{
            int size =0;
            Matcher matcher1 = a.getMatcher();
            if(matcher1!=null){
                size = matcher1.getMatches().size();
            }
            return size;
        }).reduce(Integer::sum);
        int appenderSize = sumOptional.orElse(0);
        List<Person> matcherList = persons.subList(0,matcher);
        List<Person> fixtureList = persons.subList(matcher,persons.size());
        List<Match> matchList = new LinkedList<>();
        for(int i=0;i<matcherList.size();i=i+2){
            appenderSize = appenderSize+1;
            Person fr = matcherList.get(i);
            Person tr = matcherList.get(i+1);
            logger.info("From "+fr.getName()+" --- To "+tr.getName());
            Match match = new Match();
            match.setMatchId((long) (appenderSize));
            match.setFromCorner(Corner.BLUE);
            match.setToCorner(Corner.RED);
            match.setFrom(matcherList.get(i));
            match.setTo(matcherList.get(i+1));
            matchList.add(match);
        }

        //fixture matches
        for(int i=0;i<fixtureList.size();i=i+1){
            appenderSize = appenderSize+1;
            Person fr = fixtureList.get(i);
            Person tr = fixtureList.get(i);
            logger.info("From "+fr.getName()+" --- To "+tr.getName());
            Match match = new Match();
            match.setMatchId((long)appenderSize);
            match.setFromCorner(Corner.BLUE);
            match.setToCorner(Corner.RED);
            match.setFrom(fr);
            match.setPrimary(true);
            match.setTo(tr);
            matchList.add(match);
        }
        matches.setMatches(matchList);
        matches.setInitialPlayersList(persons);
        Fixture fixture = new Fixture();
        List<Person> fixtures = new LinkedList<>(persons.subList(matcher,persons.size()));
        fixture.setPersons(fixtures);
        event.setRoundOf(roundOf);
        event.setMatcher(matches);
        event.setFixture(fixture);
    }

    public static Person createNaPerson(){
        Person person = new Person();
        person.setName("NA");
        return person;
    }
    public static Map<String, Integer> fixtureAndMatcher(int size){
        Function<Integer,Integer> powerOf = a-> (int)Math.pow((double) 2,a);
        int set = 2;
        Map<String, Integer> keyPair = new LinkedHashMap<>();
        for(int i=1; i<=size; i=i+1){
            set = powerOf.apply(i);
            if(set>=size){
                break;
            }
        }

        logger.info("Chosen Set "+set);
        Integer fixture = set-size;
        Integer matcher = size-fixture;
        keyPair.put("fixture",fixture);
        keyPair.put("matcher", matcher);
        keyPair.put("roundOf", set);
        return keyPair;
    }

    public static void setPanelEnabled(JPanel panel,boolean enabled){
        for (Component comp : panel.getComponents()) {
            comp.setEnabled(enabled);
            if(!enabled){
                comp.setBackground(Color.GRAY);
                comp.setForeground(Color.GRAY);
            }
            if (comp instanceof JPanel) {
                setPanelEnabled((JPanel) comp, enabled); // Recursively handle nested panels
            }
        }
    }

    public static List<Event> findListOfEvents(Event event){
        List<Event> listOfEvents = new LinkedList<>();
        listOfEvents.add(event);
        while(event.getParentEvent()!=null){
            listOfEvents.add(event.getParentEvent());
            event = event.getParentEvent();
        }
        Collections.reverse(listOfEvents);
        return listOfEvents;
    }

    public static JDialog jDialog(String title){
        JDialog popup = new JDialog();
        popup.setTitle(title);
        popup.setSize(300, 100);
        popup.setLocationRelativeTo(null); // Center on screen
        return popup;
    }

    public static void getEventRows(Db db, String event, Vector<Vector<Object>> teamRows) throws IOException, ClassNotFoundException {
        Event ev = db.findObject(event);
        if(ev.getParentEvent()==null){
            List<Event> events = toEventObject(db);
            if(!events.isEmpty()){
                List<Event> subEvents = new LinkedList<>();
                collectSubEvents(ev,events,subEvents);
                teamRows.add(ev.toVector());//parent event
                subEvents.forEach(s->{
                    teamRows.add(s.toVector());
                });
            }
        }
    }

    public static void getTeamRow(Db db, String event, Vector<Vector<Object>> teamRows){
        try {
            Event ev = db.findObject(event);
            if(ev.getParentEvent()==null){


                List<Match> matches = ev.getMatcher().getMatches();
                List<Person> fromNames = matches.stream().map(Match::getFrom).collect(Collectors.toList());
                List<Person> toNames = matches.stream().map(Match::getTo).collect(Collectors.toList());
                fromNames.addAll(toNames);
                List<Person> uniquePeople = fromNames.stream()
                        .collect(Collectors.toMap(
                                Person::getId,  // Key is the name
                                person -> person, // Value is the person
                                (existing, replacement) -> existing)) // In case of duplicates, keep the existing one
                        .values()
                        .stream()
                        .collect(Collectors.toList());

                if(!uniquePeople.isEmpty()){
                    uniquePeople.stream().collect(Collectors.groupingBy(Person::getTeamName)).forEach((key,value)->{
                        Vector<Object> teamRow = new Vector<>();
                        teamRow.add(ev.getId());
                        teamRow.add(ev.getEventName());
                        teamRow.add(key);
                        teamRow.add(value.size());
                        teamRows.add(teamRow);
                    });
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred", e);
            e.printStackTrace();
        }
    }
    public static Vector<Object> getFixtureRow(Db db, String event){
        Vector<Object> row = new Vector<>();
        try {
            Event ev = db.findObject(event);
            if(ev.getParentEvent()==null){
                List<Match> matches = ev.getMatcher().getMatches().stream().filter(m->!m.isPrimary()).collect(Collectors.toList());
                Set<String> fromTeamNames =  ev.getMatcher().getMatches().stream().map(a->a.getFrom().getTeamName()).collect(Collectors.toSet());
                Set<String> toTeamNames =  ev.getMatcher().getMatches().stream().map(a->a.getTo().getTeamName()).collect(Collectors.toSet());
                fromTeamNames.addAll(toTeamNames);

                row.add(ev.getId().toString());
                row.add(ev.getTeamName());
                row.add(ev.getEventName());
                row.add(fromTeamNames.size()+"");
                row.add(matches.size()+"");

            }
        } catch (Exception e) {
            logger.error("An error occurred", e);
            e.printStackTrace();
        }
        return  row;
    }

    public static List<Event> toEventObject(Db db){
        try {
            List<String> keys = db.keyFilterBy("Event_");
            if(!keys.isEmpty()){
                List<Event> events = keys.stream().map(key->{
                    try {
                        return (Event)db.findObject(key);
                    } catch (Exception e) {
                        return null;
                    }
                }).collect(Collectors.toList());
                return events;
            }
        }catch (Exception e){
            logger.error("An error occurred", e);
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    public static void collectSubEvents(Event parentEvent, List<Event> events, List<Event> subEvents) {
        for (Event e : events) {
            if (e.getParentEvent() != null && Objects.equals(e.getParentEvent().getId(), parentEvent.getId())) {
                subEvents.add(e);
                collectSubEvents(e, events, subEvents);
            }
        }
    }

    public static ImageIcon getImageIcon(String path){
        ImageIcon refreshIcon = new ImageIcon(Objects.requireNonNull(CommonFrame.class.getResource(path)));
        Image scaledImage = refreshIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    public static void setBackgroundWhite(Component component) {
        if (component instanceof JComponent) {
            component.setBackground(Color.WHITE);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setBackgroundWhite(child); // Recursive call for child components
            }
        }
    }

    public static void themes(){
        try {
            String[] themes = {
                    "","Aero", "Aluminium", "Bernstein", "Fast",
                    "Graphite", "HiFi", "Luna", "McWin",
                    "Mint", "Noire", "Smart", "Texture"
            };
            String selectedTheme = (String) JOptionPane.showInputDialog(
                    null,
                    "Select a theme:",
                    "Select Themes",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    themes,
                    themes[0]
            );
            if(selectedTheme==null) return;

            switch (selectedTheme) {
                case "Aero": UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel"); break;
                case "Aluminium": UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel"); break;
                case "Bernstein": UIManager.setLookAndFeel("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel"); break;
                case "Fast": UIManager.setLookAndFeel("com.jtattoo.plaf.fast.FastLookAndFeel"); break;
                case "Graphite": UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel"); break;
                case "HiFi": UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel"); break;
                case "Luna": UIManager.setLookAndFeel("com.jtattoo.plaf.luna.LunaLookAndFeel"); break;
                case "McWin": UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel"); break;
                case "Mint": UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel"); break;
                case "Noire": UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel"); break;
                case "Smart": UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel"); break;
                case "Texture": UIManager.setLookAndFeel("com.jtattoo.plaf.texture.TextureLookAndFeel"); break;
                default: UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            logger.error("An error occurred", e);
            e.printStackTrace();
        }
    }

    public static Person getPesonById(List<Person> person, Long id){
        return person.stream().filter(p->p.getId() == id).collect(Collectors.toList()).get(0);
    }

    public static String getOrDefaultConfiguration(Configuration configuration,String key){
        if(configuration==null)
        {
            return "";
        }else{
            try{
                return (String) configuration.get(key);
            }catch (Exception e){
                e.printStackTrace();
                return "";
            }
        }
    }

    public static JLabel getBasicLable(Configuration configuration, String key, Consumer<JLabel> labelConsumer){
        JLabel cont = new JLabel(getOrDefaultConfiguration(configuration,key));
        cont.setAlignmentX(Component.CENTER_ALIGNMENT);
        cont.setFont(new Font("Serif",Font.BOLD,25));
        cont.setForeground(Color.WHITE);
        labelConsumer.accept(cont);
        return cont;
    }
    public static JWindow createLoadingWindow() {
        // Create a JWindow to display loading image
        JWindow window = new JWindow();
        window.setSize(300, 300);
        window.setLocationRelativeTo(null); // Center the window on the screen
        window.setBackground(Color.WHITE);
        // Set the content of the window
        JLabel label = Utility.gradiantLable("Loading...!");
        label.setForeground(Color.BLUE);
        label.setFont(new Font("Serif",Font.BOLD,25));
        label.setAlignmentX(JFrame.CENTER_ALIGNMENT);
        JPanel ps = new JPanel();
        ps.setBackground(Color.WHITE);
        ps.setLayout(new FlowLayout(FlowLayout.CENTER));
        ps.add(new JLabel(Utility.getImageIcon("/logo.png")));
        ps.add(Box.createVerticalStrut(10)); // Add some space between the image and text

        ps.add(label);
        window.getContentPane().add(ps, BorderLayout.CENTER);
//        window.getContentPane().setBackground(Color.WHITE);
        return window;
    }

    public static String getFileType(String file){
        Tika tika = new Tika();
        String fileType = tika.detect(file);
        logger.info(fileType);
        return fileType;
    }

    public static JLabel gradiantLable(String name){
        if(name==null){
            name = "";
        }
        return new JLabel(name, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create a gradient for the text
                GradientPaint gradientPaint = new GradientPaint(
                        0, 0, Color.BLACK, getWidth(), 0, Color.YELLOW
                );
                g2d.setPaint(gradientPaint);

                // Draw the gradient text
                FontMetrics fontMetrics = g2d.getFontMetrics(getFont());
                int x = (getWidth() - fontMetrics.stringWidth(getText())) / 2;
                int y = (getHeight() + fontMetrics.getAscent()) / 2 - fontMetrics.getDescent();
                g2d.drawString(getText(), x, y);
            }
        };
    }

    public static Map<String, Person> goldEvents(List<Event> subEvents){
        Map<String, Person> map = new LinkedHashMap<>();
        List<Event> goldEvents=  subEvents.stream().filter(esp->esp.getRoundOf()<=2).collect(Collectors.toList());
        if(!goldEvents.isEmpty()){
            Match match= goldEvents.get(0).getMatcher().getMatches().get(0);
            Person success = match.getSuccessor().getId() == match.getFrom().getId()?match.getFrom(): match.getTo();
            Person silver = match.getSuccessor().getId() != match.getFrom().getId()?match.getFrom(): match.getTo();
            map.put("gold",success);
            map.put("silver",silver);
        }
        return map;
    }
    public  static Map<String, Person>  bronzeEvents(List<Event> subEvents){
        Map<String, Person> map = new LinkedHashMap<>();
        List<Event> bronzeEvents = subEvents.stream().filter(esp->esp.getRoundOf()<=4 && esp.getRoundOf()>2).collect(Collectors.toList());
        if(!bronzeEvents.isEmpty()){
            //two match event
            List<Match> mtchs = bronzeEvents.get(0).getMatcher().getMatches().stream().filter(m->!m.isPrimary()).collect(Collectors.toList());
            Person bronze1 = mtchs.get(0).getSuccessor().getId() != mtchs.get(0).getFrom().getId()?mtchs.get(0).getFrom(): mtchs.get(0).getTo();
            map.put("bronze1",bronze1);
            if(mtchs.size()>=2){
                Person bronze2 = mtchs.get(1).getSuccessor().getId() != mtchs.get(1).getFrom().getId()?mtchs.get(1).getFrom(): mtchs.get(1).getTo();
                map.put("bronze2",bronze2);
            }
        }
        return map;
    }
}
