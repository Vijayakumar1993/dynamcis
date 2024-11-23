package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.*;
import org.dynamics.model.Event;
import org.dynamics.util.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CommonFrame extends JFrame {
    public CommonFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        setTitle(title);
        setVisible(true);
        getContentPane().setBackground(Color.WHITE);
        setSize(new Dimension(300,300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        this.commonNorthPanel();
    }

    public void commonNorthPanel(){
        JLabel nameLable = new JLabel("Welcome "+System.getProperty("user.name"));
        add(nameLable, BorderLayout.NORTH);
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

    public Event eventPanel(List<Person> peoples, Db db, Event parentEvent) throws IOException {
        JTextField eventName = textField();
        eventName.setBorder(BorderFactory.createTitledBorder("Event Name"));
        JTextField teamName = textField();
        teamName.setBorder(BorderFactory.createTitledBorder("Team Name"));
        JTextField desciption = textField();
        desciption.setBorder(BorderFactory.createTitledBorder("Description"));

        JPanel jsp = new JPanel();
        jsp.setLayout(new GridLayout(3,1));
        jsp.add(eventName);
        jsp.add(teamName);
        jsp.add(desciption);

        confirmation("Please enter event details.", ()->jsp);
        Event event1 = new Event();
        event1.setId(Utility.getRandom());
        event1.setEventName(eventName.getText());
        event1.setTeamName(teamName.getText());
        event1.setDescription(desciption.getText());
        event1.setParentEvent(parentEvent);
        if(event1.isValid()){

            Utility.createEvent(peoples,event1);
            db.insert("Event_"+event1.getId().toString(),event1);
            if(parentEvent!=null)
                db.insert("Event_"+parentEvent.getId().toString(),parentEvent);
            alert("Event Created successfully for  the list of "+peoples.size()+" players.");
        }else{
            alert("Invalid entries for Event, Please enter correct details.");
        }
        return event1;
    }
    public TablePair createTable(Container frame, Vector<Vector<Object>> rows,
                                 Vector<String> columns, Supplier<Map<String, ActionListener>> rightClickOptions){
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
        popupMenu(table,rightClickOptions.get());
        frame.add(jsp, BorderLayout.CENTER);
        return new TablePair(model,table);
    }

    public void confirmation(String msg, Supplier<JComponent> supplier){
        JOptionPane.showConfirmDialog(this,supplier.get(),msg,JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
    }

    public JComboBox<String> comboBox(List<String> data){
        data.add(0,"");
        JComboBox<String> comboBoxs = new JComboBox<>(new Vector<>(data));
        return comboBoxs;
    }

    public JComboBox<Item> comboBoxForItems(String title, List<String> paired, Db db){
        JComboBox<Item> pairedOptions = new JComboBox<>();
        pairedOptions.addItem(new Item(0l,""));
        List<Item> sortedItems = new LinkedList<>();
        paired.forEach(s->{
            try {
                Event event = db.findObject(s);
                String description = event.getDescription();
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
}
