package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Event;
import org.dynamics.model.*;
import org.dynamics.util.Utility;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class CommonFrame extends JFrame {

    private TablePair pair;
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("Fixtures List");
    private DefaultTreeModel treeModel = new DefaultTreeModel(root);
    private JTree jtree = new JTree(treeModel);
    public CommonFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        setTitle(title);
        getContentPane().setBackground(Color.WHITE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        setVisible(true);
//        this.commonNorthPanel();
    }

    public void westPanel(Db db){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        List<Event> events = new LinkedList<>();
        List<String> eventKeys = db.keyFilterBy("Event_");
        if(eventKeys!=null && !eventKeys.isEmpty()){
            eventKeys.stream().sorted().forEach(event->{
                try {
                    Event ev = db.findObject(event);
                    events.add(ev);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        events.forEach(a->{
            System.out.println(a.getEventDate());
            if(a.getEventDate()!=null){
                if(LocalDate.now().isBefore(a.getEventDate()) || LocalDate.now().isEqual(a.getEventDate())){
                    JLabel lab =new JLabel(a.getEventDate().toString()+": "+a.getEventName());
                    lab.setFont(new Font("Serif",Font.BOLD,12));
                    panel.add(lab);
                    panel.add(new JLabel(" "));
                }
            }
        });

        panel.setBorder(BorderFactory.createTitledBorder("Upcoming Events"));
        add(new JScrollPane(panel),BorderLayout.EAST);
    }
    public void commonCenterPanel(Db db){
        Vector<String> columns = new Vector<>();
        columns.add("Fixture Id");
        columns.add("Category Name");
        columns.add("Weight Category");
        columns.add("Total No of Teams");
        columns.add("Total No of Match");

        Vector<Vector<Object>> rows = new Vector<>();
        List<String> eventKeys = db.keyFilterBy("Event_");
        if(eventKeys!=null && !eventKeys.isEmpty()){
            eventKeys.stream().sorted().forEach(event->{
                try {
                    Event ev = db.findObject(event);
                    if(ev.getParentEvent()==null){
                        List<Match> matches = ev.getMatcher().getMatches();
                        Set<String> fromTeamNames = matches.stream().map(a->a.getFrom().getTeamName()).collect(Collectors.toSet());
                        Set<String> toTeamNames = matches.stream().map(a->a.getTo().getTeamName()).collect(Collectors.toSet());
                        fromTeamNames.addAll(toTeamNames);
                        Vector<Object> row = new Vector<>();
                        row.add(ev.getId().toString());
                        row.add(ev.getTeamName());
                        row.add(ev.getEventName());
                        row.add(fromTeamNames.size()+"");
                        row.add(matches.size()+"");
                        rows.add(row);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });
        }
        this.pair = this.createTable(this,rows, columns,()->new LinkedHashMap<>(),null);
    }

    public void commonSouthPanal(Db db){
        JPanel jps = new JPanel();
        jps.setLayout(new BorderLayout());
        JButton refresh = new JButton("Refresh");
        refresh.setBackground(Color.GREEN);
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
                        throw new RuntimeException(es);
                    }

                });
            }
        });
        jps.add(refresh,BorderLayout.WEST);
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
                    throw new RuntimeException(e);
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
                    try {
                        Event ev = db.findObject("Event_"+item.getId());
                        List<Match> matches = ev.getMatcher().getMatches();
                        Set<String> fromTeamNames = matches.stream().map(a->a.getFrom().getTeamName()).collect(Collectors.toSet());
                        Set<String> toTeamNames = matches.stream().map(a->a.getTo().getTeamName()).collect(Collectors.toSet());
                        fromTeamNames.addAll(toTeamNames);
                        Vector<String> row = new Vector<>();
                        row.add(ev.getId().toString());
                        row.add(ev.getTeamName());
                        row.add(ev.getEventName());
                        row.add(fromTeamNames.size()+"");
                        row.add(matches.size()+"");
                        this.pair.getDefaultTableModel().addRow(row);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }else{
                    Vector<Vector<Object>> rows = new Vector<>();
                    List<String> oldKeys = db.keyFilterBy("Event_");
                    if(oldKeys!=null && !oldKeys.isEmpty()){
                        this.pair.getDefaultTableModel().setRowCount(0);
                        oldKeys.stream().sorted().forEach(event->{
                            try {
                                Event ev = db.findObject(event);
                                if(ev.getParentEvent()==null){
                                    List<Match> matches = ev.getMatcher().getMatches();
                                    Set<String> fromTeamNames = matches.stream().map(a->a.getFrom().getTeamName()).collect(Collectors.toSet());
                                    Set<String> toTeamNames = matches.stream().map(a->a.getTo().getTeamName()).collect(Collectors.toSet());
                                    fromTeamNames.addAll(toTeamNames);
                                    Vector<Object> row = new Vector<>();
                                    row.add(ev.getId().toString());
                                    row.add(ev.getTeamName());
                                    row.add(ev.getEventName());
                                    row.add(fromTeamNames.size()+"");
                                    row.add(matches.size()+"");
                                    this.pair.getDefaultTableModel().addRow(row);
                                }
                            } catch (Exception e1) {
                                throw new RuntimeException(e1);
                            }

                        });
                    }
                }
            }
        });
        JPanel jsp = new JPanel();
        jsp.add(jtree);
        jsp.setBorder(BorderFactory.createTitledBorder("Event Tree"));
        jsp.setMinimumSize(new Dimension(300,300));
        add(jsp, BorderLayout.WEST);
    }
    public void commonNorthPanel(Db db){
        JPanel jsp = new JPanel();
        jsp.setLayout(new BorderLayout());
        jsp.setBackground(Color.WHITE);
        jsp.setFont(new Font("Serif",Font.BOLD,12));
        JLabel welcomeLable =new JLabel("Welcome "+System.getProperty("user.name"));
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
        JTextField teamName = textField();
        teamName.setBorder(BorderFactory.createTitledBorder("Weight Category"));
        JTextField desciption = textField();
        desciption.setBorder(BorderFactory.createTitledBorder("Description"));
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
        table.setRowSorter(new TableRowSorter<TableModel>(model));
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
                String description = event.getEventName().concat("("+event.getTeamName()+")");
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

}
