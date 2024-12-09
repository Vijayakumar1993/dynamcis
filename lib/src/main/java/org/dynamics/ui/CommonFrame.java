package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Event;
import org.dynamics.model.*;
import org.dynamics.util.Utility;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class CommonFrame extends JFrame {

    private TablePair pair;
    private TablePair teamPair;
    private TablePair eventPair;
    private TablePair medels;
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("Fixtures List");
    private DefaultTreeModel treeModel = new DefaultTreeModel(root);
    private JTree jtree = new JTree(treeModel);
    public CommonFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        setTitle(title);
        UIManager.put("JTattoo.noText", true);
        Image icon = ImageIO.read(Objects.requireNonNull(CommonFrame.class.getResource("/logo.jpeg")));
        setIconImage(icon); // Set the icon for the JFrame
        getContentPane().setBackground(Color.WHITE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setVisible(true);
        UIManager.setLookAndFeel("com.jtattoo.plaf.texture.TextureLookAndFeel");
//        this.commonNorthPanel();
    }
    public void commonCenterPanel(Db db){
        Vector<String> columns = new Vector<>();
        columns.add("Fixture Id");
        columns.add("Weight Category");
        columns.add("Category Name");
        columns.add("Total No of Teams");
        columns.add("Total No of Match");

        Vector<Vector<Object>> rows = new Vector<>();
        Vector<Vector<Object>> teamRows = new Vector<>();
        Vector<Vector<Object>> eventRows = new Vector<>();
        List<String> eventKeys = db.keyFilterBy("Event_");
        if(eventKeys!=null && !eventKeys.isEmpty()){
            eventKeys.stream().sorted().forEach(event->{
                try {
                    Vector<Object> row = Utility.getFixtureRow(db, event);
                    if(!row.isEmpty()) rows.add(row);
                    Utility.getTeamRow(db,event,teamRows);
                    Utility.getEventRows(db,event,eventRows);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }

        //fixture details frame
        JPanel fixturesDetails = new JPanel();
        fixturesDetails.setLayout(new BorderLayout());
        this.pair = this.createTable(fixturesDetails,rows, columns, LinkedHashMap::new,null);


        //team based details
        Vector<String> teamColumns = new Vector<>();
        teamColumns.add("Event Id");
        teamColumns.add("Event Name");
        teamColumns.add("Team Name");
        teamColumns.add("Total no of Players");
        JPanel teamDetails = new JPanel();
        teamDetails.setLayout(new BorderLayout());
        this.teamPair = this.createTable(teamDetails,teamRows, teamColumns, LinkedHashMap::new,null);

        JPanel eventDetail = new JPanel();
        eventDetail.setLayout(new BorderLayout());
        this.eventPair = this.createTable(eventDetail,eventRows,Event.keys(), LinkedHashMap::new,null);

        JPanel medalDetails = new JPanel();
        medalDetails.setLayout(new BorderLayout());

        Vector<String> medals = new Vector<>();
        medals.add("Event");
        medals.add("Gold");
        medals.add("Silver");
        medals.add("Bronze 1");
        medals.add("Bronze 2");
        this.medels = this.createTable(medalDetails,new Vector<>(),medals, LinkedHashMap::new,null);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.WHITE);
        tabs.add("Fixture Details",fixturesDetails);
        tabs.add("Team Details",teamDetails);
        tabs.add("Event Details",eventDetail);
        tabs.add("Medals",medalDetails);
        add(tabs,BorderLayout.CENTER);
    }

    public void commonSouthPanal(Db db){
        JPanel jps = new JPanel();
        jps.setLayout(new BorderLayout());
        JButton refresh = new JButton(Utility.getImageIcon("/refresh.png"));
        JButton theme = new JButton(Utility.getImageIcon("/settings.png"));
        theme.addActionListener(e->{
            Utility.themes();
            SwingUtilities.updateComponentTreeUI(this);
        });
//        refresh.setBackground(Color.GREEN);
        refresh.addActionListener(e->{
            root.removeAllChildren();
            List<String> eventKeys = db.keyFilterBy("Event_");
            if(eventKeys!=null && !eventKeys.isEmpty()){
                eventKeys.stream().sorted().forEach(event->{
                    try {
                        Event ev = db.findObject(event);
                        if(ev.getParentEvent()==null){
                            Item item = new Item(ev.getId(),ev.getEventName().concat("("+ev.getTeamName()+")"));
                            DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
                            root.add(node);
                            treeModel.reload();
                        }
                    } catch (Exception es) {
                        es.printStackTrace();
                    }

                });
            }
        });
        jps.add(refresh,BorderLayout.EAST);
        jps.add(theme,BorderLayout.WEST);
        jps.setBorder(BorderFactory.createTitledBorder("Logger"));
        add(jps,BorderLayout.SOUTH);

    }
    public void commonWestPanel(Db db) throws IOException, ClassNotFoundException {
        List<String> eventKeys = db.keyFilterBy("Event_");
        if(eventKeys!=null && !eventKeys.isEmpty()){
            eventKeys.stream().sorted().forEach(event->{
                try {
                    Event ev = db.findObject(event);
                    if(ev.getParentEvent()==null){
                        Item item = new Item(ev.getId(),ev.getEventName().concat("("+ev.getTeamName()+")"));
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
                        root.add(node);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }

        jtree.addTreeSelectionListener(e->{
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtree.getLastSelectedPathComponent();
            if(node!=null){
                Object obj = node.getUserObject();
                if(obj instanceof Item){
                    Item item = (Item)node.getUserObject();
                    System.out.println(item);
                    this.pair.getDefaultTableModel().setRowCount(0);
                    this.teamPair.getDefaultTableModel().setRowCount(0);
                    this.eventPair.getDefaultTableModel().setRowCount(0);
                    this.medels.getDefaultTableModel().setRowCount(0);
                    try {
                        Vector<Object> row = Utility.getFixtureRow(db,"Event_"+item.getId());
                        if(!row.isEmpty()) this.pair.getDefaultTableModel().addRow(row);

                        Vector<Vector<Object>> teamRows = new Vector<>();
                        Utility.getTeamRow(db,"Event_"+item.getId(),teamRows);
                        teamRows.forEach(teamRow->{
                            this.teamPair.getDefaultTableModel().addRow(teamRow);
                        });
                        Vector<Vector<Object>> eventRows = new Vector<>();
                        Utility.getEventRows(db,"Event_"+item.getId(),eventRows);
                        eventRows.forEach(eventRow->{
                            this.eventPair.getDefaultTableModel().addRow(eventRow);
                        });

                        List<Event> existingEvents = Utility.toEventObject(db);
                        Event parEvent  = db.findObject("Event_"+item.getId()) ;
                        List<Event> subEvents = new LinkedList<>();
                        Utility.collectSubEvents(parEvent,existingEvents,subEvents);

                        List<Event> bronzeEvents = subEvents.stream().filter(esp->esp.getRoundOf()<=4 && esp.getRoundOf()>2).collect(Collectors.toList());
                        List<Event> goldEvents = subEvents.stream().filter(esp->esp.getRoundOf()<=2).collect(Collectors.toList());

                        Vector<String> medalRows = new Vector<>();
                        medalRows.add(item.toString());
                        if(!goldEvents.isEmpty()){
                            //only one match will be available...!
                            Match match= goldEvents.get(0).getMatcher().getMatches().get(0);
                            Person success = match.getSuccessor().getId() == match.getFrom().getId()?match.getFrom(): match.getTo();
                            Person silver = match.getSuccessor().getId() != match.getFrom().getId()?match.getFrom(): match.getTo();
                            medalRows.add(success.getName()+"("+success.getTeamName()+")");
                            medalRows.add(silver.getName()+"("+success.getTeamName()+")");
                        }
                        if(!bronzeEvents.isEmpty()){
                            //two match event
                            List<Match> mtchs = bronzeEvents.get(0).getMatcher().getMatches();
                            Person bronze1 = mtchs.get(0).getSuccessor().getId() != mtchs.get(0).getFrom().getId()?mtchs.get(0).getFrom(): mtchs.get(0).getTo();
                            Person bronze2 = mtchs.get(1).getSuccessor().getId() != mtchs.get(1).getFrom().getId()?mtchs.get(1).getFrom(): mtchs.get(1).getTo();


                            medalRows.add(bronze1.getName()+"("+bronze1.getTeamName()+")");
                            medalRows.add(bronze2.getName()+"("+bronze2.getTeamName()+")");
                        }
                        this.medels.getDefaultTableModel().addRow(medalRows);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }else{
                    List<String> oldKeys = db.keyFilterBy("Event_");
                    if(oldKeys!=null && !oldKeys.isEmpty()){
                        List<Event> existingEvents = Utility.toEventObject(db);
                        this.pair.getDefaultTableModel().setRowCount(0);
                        this.eventPair.getDefaultTableModel().setRowCount(0);
                        this.medels.getDefaultTableModel().setRowCount(0);
                        this.teamPair.getDefaultTableModel().setRowCount(0);
                        oldKeys.stream().sorted().forEach(event->{
                            Vector<Object> row = Utility.getFixtureRow(db,event);
                            if(!row.isEmpty()) this.pair.getDefaultTableModel().addRow(row);

                            Vector<Vector<Object>> teamRows = new Vector<>();
                            Utility.getTeamRow(db,event,teamRows);
                            teamRows.forEach(teamRow->{
                                this.teamPair.getDefaultTableModel().addRow(teamRow);
                            });
                            Vector<Vector<Object>> eventRows = new Vector<>();
                            try {
                                Utility.getEventRows(db,event,eventRows);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            eventRows.forEach(eventRow->{
                                this.eventPair.getDefaultTableModel().addRow(eventRow);
                            });
                            List<Event> subEvents = new LinkedList<>();
                            Event parEvent  = null;
                            try {
                                parEvent = db.findObject(event);
                            }  catch (Exception ex) {
                                ex.printStackTrace();
                                alert(ex.getMessage());
                            }
                            if(parEvent.getParentEvent()==null){
                                Utility.collectSubEvents(parEvent,existingEvents,subEvents);
                                List<Event> bronzeEvents = subEvents.stream().filter(esp->esp.getRoundOf()<=4 && esp.getRoundOf()>2).collect(Collectors.toList());
                                List<Event> goldEvents = subEvents.stream().filter(esp->esp.getRoundOf()<=2).collect(Collectors.toList());

                                Vector<String> medalRows = new Vector<>();
                                medalRows.add(parEvent.getEventName());
                                if(!goldEvents.isEmpty()){
                                    //only one match will be available...!
                                    Match match= goldEvents.get(0).getMatcher().getMatches().get(0);
                                    Person success = match.getSuccessor().getId() == match.getFrom().getId()?match.getFrom(): match.getTo();
                                    Person silver = match.getSuccessor().getId() != match.getFrom().getId()?match.getFrom(): match.getTo();
                                    medalRows.add(success.getName()+"("+success.getTeamName()+")");
                                    medalRows.add(silver.getName()+"("+success.getTeamName()+")");
                                }
                                if(!bronzeEvents.isEmpty()){
                                    //two match event
                                    List<Match> mtchs = bronzeEvents.get(0).getMatcher().getMatches();
                                    Person bronze1 = mtchs.get(0).getSuccessor().getId() != mtchs.get(0).getFrom().getId()?mtchs.get(0).getFrom(): mtchs.get(0).getTo();
                                    Person bronze2 = mtchs.get(1).getSuccessor().getId() != mtchs.get(1).getFrom().getId()?mtchs.get(1).getFrom(): mtchs.get(1).getTo();
                                    medalRows.add(bronze1.getName()+"("+bronze1.getTeamName()+")");
                                    medalRows.add(bronze2.getName()+"("+bronze2.getTeamName()+")");
                                }
                                this.medels.getDefaultTableModel().addRow(medalRows);
                            }
                        });
                    }
                }
            }
        });
        JPanel jsp = new JPanel();
        jsp.add(jtree);
        jsp.setBackground(Color.WHITE);
        jsp.setBorder(BorderFactory.createTitledBorder("Event Tree"));
        jsp.setMinimumSize(new Dimension(300,300));
        add(jsp, BorderLayout.WEST);
    }
    public void commonNorthPanel(Db db){
        JPanel jsp = new JPanel();
        jsp.setLayout(new BorderLayout());
        jsp.setBackground(Color.WHITE);
        jsp.setFont(new Font("Serif",Font.BOLD,12));
        JLabel welcomeLable = new JLabel(System.getProperty("user.name"), Utility.getImageIcon("/user.png"), JLabel.CENTER);
        welcomeLable.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        jsp.add(welcomeLable,BorderLayout.EAST);
        try {
            Configuration configuration =  db.findObject("configuration");
            if(configuration!=null){
                ImageIcon icon = new ImageIcon((String)configuration.get("right-logo"));
                JLabel imageLable = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(200,100,Image.SCALE_SMOOTH)));
                jsp.add(imageLable,BorderLayout.WEST);
                JLabel titlelable = new JLabel((String)configuration.get("title"));
                titlelable.setHorizontalAlignment(SwingConstants.CENTER); // Align horizontally
                titlelable.setVerticalAlignment(SwingConstants.CENTER);   // Align vertically
                titlelable.setFont(new Font("Serif",Font.BOLD,30));
                jsp.add(titlelable,BorderLayout.CENTER);
            }
        } catch (Exception es) {
            es.printStackTrace();
            alert(es.getMessage());
        }


        jsp.setBorder(BorderFactory.createTitledBorder("Welcome"));
        add(jsp, BorderLayout.NORTH);
    }
    public void menuBar(Map<String, Map<String, ActionListener>> menuItems){
        JMenuBar menuBar = new JMenuBar();
        menuItems.forEach((key,value)->{
            JMenu menu = new JMenu(key);
            value.forEach((subMenu, listener)->{
                JMenuItem menuItem = new JMenuItem(subMenu);
                menuItem.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
                menuItem.addActionListener(listener);
                menu.add(menuItem);
            });
            menuBar.add(menu);
        });
        setJMenuBar(menuBar);
    }

    protected void alert(String message){
        JOptionPane.showMessageDialog(this,message);
    }

    protected Optional<String> fileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION){
            return Optional.of(fileChooser.getSelectedFile().getAbsolutePath());
        }
        return Optional.empty();
    }
    protected Optional<String> fileSaver(){
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if(result == JFileChooser.APPROVE_OPTION){
            return Optional.of(fileChooser.getSelectedFile().getAbsolutePath());
        }
        return Optional.empty();
    }

    public Event eventPanel(List<Person> peoples, Db db, Event parentEvent, Gender gender, Categories categories) throws IOException {
        if(peoples.size()<=0){
            throw new IOException("Unable to create event for the peoples");
        }
        JTextField eventName = textField();
        eventName.setBorder(BorderFactory.createTitledBorder("Category Name"));
        if(parentEvent!=null){
            eventName.setText(parentEvent.getEventName());
        }

        JTextField teamName = textField();
        teamName.setBorder(BorderFactory.createTitledBorder("Weight Category"));
        if(parentEvent!=null){
            teamName.setText(parentEvent.getTeamName());
        }

        JTextField desciption = textField();
        desciption.setBorder(BorderFactory.createTitledBorder("Description"));
        if(parentEvent!=null){
            desciption.setText(parentEvent.getDescription());
        }

        JDatePickerImpl datePicker = datePicker(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now()));
        datePicker.setBorder(BorderFactory.createTitledBorder("Event Date"));

        JPanel jsp = new JPanel();
        jsp.setLayout(new GridLayout(4,1,10,10));
        jsp.add(eventName);
        jsp.add(teamName);
        jsp.add(desciption);
        jsp.add(datePicker);


        confirmation("Please enter the details.", ()->jsp);
        Event event1 = new Event();
        event1.setId(Utility.getRandom());
        event1.setEventName(eventName.getText());
        event1.setTeamName(teamName.getText());
        event1.setDescription(desciption.getText());
        event1.setParentEvent(parentEvent);
        event1.setSelectedGenderCategory(gender);
        event1.setSelecetedEventCategory(categories);

        java.util.Date normalDate = (Date) datePicker.getModel().getValue();
        event1.setEventDate(normalDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        if(event1.isValid()){
            LocalDate eventDte = ZonedDateTime.parse(datePicker.getModel().getValue().toString(),DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)).toLocalDate();
            event1.setEventDate(eventDte);
            Utility.createEvent(peoples,event1);
            db.insert("Event_"+event1.getId().toString(),event1);
            if(parentEvent!=null)
                db.insert("Event_"+parentEvent.getId().toString(),parentEvent);
            alert(event1.getEventName()+" is Created successfully for  the list of "+peoples.size()+" players.");
        }else{
            alert("Invalid entries for Event, Please enter correct details.");
        }
        return event1;
    }
    public TablePair createTable(Container frame, Vector<Vector<Object>> rows,
                                 Vector<String> columns, Supplier<Map<String, ActionListener>> rightClickOptions, BiConsumer<JTable, TableModelEvent> modelListener){
        DefaultTableModel model = new DefaultTableModel(rows, columns);
        JTable table = new JTable(model);
        table.getTableHeader().setFont(new Font("Serif",Font.BOLD,12));
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
//        table.setRowSorter(new TableRowSorter<TableModel>(model));


        // Enable row dragging
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add drag-and-drop support
        table.setTransferHandler(new TableRowTransferHandler(model));



        table.setRowHeight(30);
        JScrollPane jsp = new JScrollPane(table);
        jsp.setBorder(BorderFactory.createTitledBorder("Details"));
        jsp.setBackground(Color.WHITE);
        if(modelListener!=null){
            table.getModel().addTableModelListener(e->{
                modelListener.accept(table,e);
            });
        }
        popupMenu(table,rightClickOptions.get());
        frame.add(jsp, BorderLayout.CENTER);
        return new TablePair(model,table);
    }

    public Integer confirmation(String msg, Supplier<JComponent> supplier){
        return JOptionPane.showConfirmDialog(this,supplier.get(),msg,JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
    }

    public JComboBox<String> comboBox(List<String> data){
        data.add(0,"");
        JComboBox<String> comboBoxs = new JComboBox<>(new Vector<>(data));
        return comboBoxs;
    }

    public JComboBox<Item> comboBoxForItems(String title, List<String> paired, Db db, JComboBox<Item> pairedOptions){
        pairedOptions.addItem(new Item(0l,""));
        List<Item> sortedItems = new LinkedList<>();
        paired.forEach(s->{
            try {
                Event event = db.findObject(s);
                String description = event.getEventName().concat("("+event.getTeamName()+")("+event.getRoundOf()+")");
                sortedItems.add(new Item(event.getId(), description));
            } catch (Exception e) {
                alert(e.getMessage());
                e.printStackTrace();
            }
        });
        sortedItems.sort(Comparator.comparing(Item::getDescription));
        sortedItems.forEach(pairedOptions::addItem);
        pairedOptions.setBorder(BorderFactory.createTitledBorder(title));
        return pairedOptions;
    }

    public JTextField textField(){
        JTextField textField  = new JTextField();
        textField.setPreferredSize(new Dimension(300,50));
        return textField;
    }

    public void popupMenu(JComponent component, Map<String, ActionListener> events){
        class Popup extends JPopupMenu{
            Popup(){
                events.forEach((key,value)->{
                    JMenuItem jm = new JMenuItem(key);
                    jm.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
                    jm.addActionListener(value);
                    add(jm);
                });
            }
        }

        class PopupClickListener extends MouseAdapter{
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.isPopupTrigger()) doPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.isPopupTrigger()) doPopup(e);
            }
            private void doPopup(MouseEvent ne){
                Popup popup = new Popup();
                popup.show(ne.getComponent(),ne.getX(),ne.getY());
            }
        }
        component.addMouseListener(new PopupClickListener());
    }

    public JTextField numbrFiled(){
        JTextField decimalField = new JTextField(10);

        decimalField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String text = decimalField.getText();

                // Allow digits, one dot, and backspace
                if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }

                // Prevent multiple dots
                if (c == '.' && text.contains(".")) {
                    e.consume();
                }
            }
        });
        return decimalField;
    }

    public JDatePickerImpl datePicker(String defaultValue){
        UtilDateModel model = new UtilDateModel();
        model.setSelected(true);
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        return new JDatePickerImpl(datePanel, new JFormattedTextField.AbstractFormatter() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public Object stringToValue(String text) throws ParseException {
                return java.sql.Date.valueOf(LocalDate.parse(text, dateFormatter));
            }

            @Override
            public String valueToString(Object value) throws ParseException {
                if (value instanceof GregorianCalendar) {
                    GregorianCalendar calendar = (GregorianCalendar) value;
                    LocalDate localDate = calendar.toZonedDateTime().toLocalDate();
                    return localDate.toString(); // Format: yyyy-MM-dd
                }
                return defaultValue;
            }
        });
    }
    class TableRowTransferHandler extends TransferHandler {
        private final DefaultTableModel model;
        private int[] rows;

        public TableRowTransferHandler(DefaultTableModel model) {
            this.model = model;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTable table = (JTable) c;
            rows = table.getSelectedRows();
            return new StringSelection("");
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop();
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            JTable.DropLocation dropLocation = (JTable.DropLocation) support.getDropLocation();
            int dropRow = dropLocation.getRow();

            for (int row : rows) {
                Object[] rowData = new Object[model.getColumnCount()];
                for (int col = 0; col < model.getColumnCount(); col++) {
                    rowData[col] = model.getValueAt(row, col);
                }
                model.removeRow(row);
                model.insertRow(dropRow++, rowData);
            }

            return true;
        }
    }
}
